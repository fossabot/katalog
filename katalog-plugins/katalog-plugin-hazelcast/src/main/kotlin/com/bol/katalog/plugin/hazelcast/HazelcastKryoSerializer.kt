package com.bol.katalog.plugin.hazelcast

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import org.objenesis.strategy.StdInstantiatorStrategy

class HazelcastKryoSerializer : StreamSerializer<Any> {
    private val kryoPool = Pool(16) {
        val kryo = Kryo()

        // Use whatever classloader is used by Hazelcast in Kryo as well, to prevent ClassCastExceptions
        kryo.classLoader = Thread.currentThread().contextClassLoader

        // Allow Kryo to use Objenesis to instantiate objects (useful for no-arg constructor Java classes)
        kryo.instantiatorStrategy = Kryo.DefaultInstantiatorStrategy(StdInstantiatorStrategy())
        kryo
    }

    override fun getTypeId() = 1000

    override fun destroy() {
    }

    override fun write(output: ObjectDataOutput, obj: Any) {
        kryoPool.lease { kryo ->
            Output(1024 * 8, 1024 * 64).use { kryoOutput ->
                kryo.writeClassAndObject(kryoOutput, obj)
                output.writeByteArray(kryoOutput.toBytes())
            }
        }
    }

    override fun read(input: ObjectDataInput): Any {
        return kryoPool.lease { kryo ->
            Input(input.readByteArray()).use { kryoInput ->
                kryo.readClassAndObject(kryoInput)
            }
        }
    }
}