package com.bol.katalog.plugin.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * This class is implemented in Java because otherwise serialization will fail.
 * It simply receives a JSON command from another Hazelcast node and sends it to the HazelcastAggregateContext.
 */
public class HandleCommandTask implements Callable<String>, Serializable, HazelcastInstanceAware {
    private final String handlerType;
    private final String message;
    private HazelcastAggregateContext aggregateContext;

    public HandleCommandTask(String handlerType, String message) {
        this.handlerType = handlerType;
        this.message = message;
    }

    @Override
    public String call() {
        return aggregateContext.onJsonCommand(handlerType, message);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcast) {
        this.aggregateContext = (HazelcastAggregateContext) hazelcast.getUserContext().get(HazelcastAggregateContext.class.toString());
    }
}
