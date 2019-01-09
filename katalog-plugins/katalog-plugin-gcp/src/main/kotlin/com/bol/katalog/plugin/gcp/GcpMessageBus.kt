package com.bol.katalog.plugin.gcp

import com.bol.katalog.messaging.MessageBus
import com.google.api.gax.batching.BatchingSettings
import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.cloud.pubsub.v1.*
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub
import com.google.cloud.pubsub.v1.stub.SubscriberStub
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings
import com.google.protobuf.ByteString
import com.google.pubsub.v1.*
import mu.KotlinLogging
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PreDestroy

class GcpMessageBus(
    private val credentialsProvider: CredentialsProvider,
    private val projectId: String
) : MessageBus {
    private val log = KotlinLogging.logger {}

    private var publishers = ConcurrentHashMap<String, Publisher>()

    private var subscribers = ConcurrentHashMap<String, SubscriberStub>()

    private var topicAdminClient: TopicAdminClient

    private var subscriptionAdminClient: SubscriptionAdminClient

    private var batchingSettings: BatchingSettings = BatchingSettings.newBuilder()
        .setIsEnabled(false)
        .build()

    init {
        val topicAdminSettings = TopicAdminSettings.newBuilder()
            .setCredentialsProvider(credentialsProvider)
            .build()
        topicAdminClient = TopicAdminClient.create(topicAdminSettings)

        val subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
            .setCredentialsProvider(credentialsProvider)
            .build()
        subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)
    }

    @PreDestroy
    fun preDestroy() {
        publishers.values.forEach { it.shutdown() }
    }

    override suspend fun publish(queue: String, task: Any) {
        val publisher = publishers.getOrPut(queue) {
            try {
                topicAdminClient.createTopic(getQueueName(queue))
            } catch (e: AlreadyExistsException) {
            }

            Publisher.newBuilder(getQueueName(queue))
                .setBatchingSettings(batchingSettings)
                .setCredentialsProvider(credentialsProvider)
                .build()
        }

        val pubsubMessage = PubsubMessage.newBuilder()
            .putAttributes("type", task::class.java.name)
            .setData(ByteString.copyFromUtf8(GcpObjectMapper.get().writeValueAsString(task)))
            .build()

        publisher.publish(pubsubMessage)
    }

    override suspend fun receive(queue: String, handler: suspend (Any) -> Unit) {
        val subscriber = subscribers.getOrPut(queue) {
            try {
                subscriptionAdminClient.createSubscription(
                    getSubscriptionName(queue),
                    getQueueName(queue),
                    PushConfig.getDefaultInstance(),
                    Duration.ofMinutes(5).seconds.toInt()
                )
            } catch (e: AlreadyExistsException) {
            }

            val subscriberStubSettings = SubscriberStubSettings.newBuilder()
                .setTransportChannelProvider(
                    SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
                        .setMaxInboundMessageSize(20 shl 20) // 20MB
                        .build()
                )
                .build()

            GrpcSubscriberStub.create(subscriberStubSettings)
        }

        val pullRequest = PullRequest.newBuilder()
            .setMaxMessages(100)
            .setReturnImmediately(true)
            .setSubscription(getSubscriptionName(queue).toString())
            .build()

        val pullResponse = subscriber.pullCallable().call(pullRequest)
        val ackIds = mutableListOf<String>()

        pullResponse.receivedMessagesList.forEach { receivedMessage ->
            val clazz = Class.forName(receivedMessage.message.attributesMap["type"])
            val task = GcpObjectMapper.get().readValue(receivedMessage.message.data.toByteArray(), clazz)

            try {
                log.debug("Received message: {}", task)
                handler(task)
                ackIds += receivedMessage.ackId
            } catch (e: Exception) {
                log.warn("Caught exception when handling message", e)
            }
        }

        if (ackIds.isNotEmpty()) {
            val acknowledgeRequest = AcknowledgeRequest.newBuilder()
                .setSubscription(getSubscriptionName(queue).toString())
                .addAllAckIds(ackIds)
                .build()
            subscriber.acknowledgeCallable().call(acknowledgeRequest)
        }
    }

    private fun getQueueName(queue: String) = ProjectTopicName.of(projectId, queue)

    private fun getSubscriptionName(subscription: String) =
        ProjectSubscriptionName.of(projectId, subscription)
}