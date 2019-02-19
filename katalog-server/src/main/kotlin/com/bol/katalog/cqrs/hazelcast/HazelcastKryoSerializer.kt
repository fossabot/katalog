package com.bol.katalog.cqrs.hazelcast

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.pool.KryoPool
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import de.javakaffee.kryoserializers.*
import org.objenesis.strategy.StdInstantiatorStrategy
import java.util.*

/**
 * In order to properly serialize the various Kotlin data classes and classes used internally by Hazelcast we use
 * Kryo. This has support for all the various exotic classes that need to be serialized.
 *
 * Kryo is not thread-safe, so we create a pool of Kryo instances that can be leased and used.
 */
class HazelcastKryoSerializer : StreamSerializer<Any> {
    private val kryoPool: KryoPool = KryoPool.Builder {
        val kryo = Kryo()

        // Use whatever classloader is used by Hazelcast in Kryo as well, to prevent ClassCastExceptions
        kryo.classLoader = Thread.currentThread().contextClassLoader

        // Allow Kryo to use Objenesis to instantiate objects (useful for no-arg constructor Java classes)
        kryo.instantiatorStrategy = Kryo.DefaultInstantiatorStrategy(StdInstantiatorStrategy())

        // Register some extra serializers (e.g. used by Spring Session)
        kryo.register(Arrays.asList("")::class.java, ArraysAsListSerializer())
        kryo.register(Collections.EMPTY_LIST::class.java, CollectionsEmptyListSerializer())
        kryo.register(Collections.EMPTY_MAP::class.java, CollectionsEmptyMapSerializer())
        kryo.register(Collections.EMPTY_SET::class.java, CollectionsEmptySetSerializer())
        kryo.register(Collections.singletonList("")::class.java, CollectionsSingletonListSerializer())
        kryo.register(Collections.singleton("")::class.java, CollectionsSingletonSetSerializer())
        kryo.register(Collections.singletonMap("", "")::class.java, CollectionsSingletonMapSerializer())
        UnmodifiableCollectionsSerializer.registerSerializers(kryo)

        kryo
    }.build()

    override fun getTypeId() = 1000

    override fun destroy() {
        // Nothing to destroy
    }

    override fun write(output: ObjectDataOutput, obj: Any) {
        kryoPool.run { kryo ->
            Output(1024 * 8, 1024 * 64).use { kryoOutput ->
                kryo.writeClassAndObject(kryoOutput, obj)
                output.writeByteArray(kryoOutput.toBytes())
            }
        }
    }

    override fun read(input: ObjectDataInput): Any {
        return kryoPool.run { kryo ->
            Input(input.readByteArray()).use { kryoInput ->
                kryo.readClassAndObject(kryoInput)
            }
        }
    }
}