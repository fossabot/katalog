package com.bol.katalog.plugin.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * This class is implemented in Java because otherwise serialization will fail.
 * It simply receives a serializable command from another Hazelcast node and sends it to the HazelcastAggregateContext.
 */
public class HandleCommandTask implements Callable<SerializableResult>, Serializable, HazelcastInstanceAware {
    private final String handlerType;
    private final SerializableCommand serializableCommand;
    private HazelcastAggregateContext aggregateContext;

    public HandleCommandTask(String handlerType, SerializableCommand serializableCommand) {
        this.handlerType = handlerType;
        this.serializableCommand = serializableCommand;
    }

    @Override
    public SerializableResult call() {
        return aggregateContext.onSerializableCommand(handlerType, serializableCommand);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcast) {
        this.aggregateContext = (HazelcastAggregateContext) hazelcast.getUserContext().get(HazelcastAggregateContext.class.toString());
    }
}
