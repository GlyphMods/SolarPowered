package io.github.glyphmods.solarpowered.content.optics.network.manager

import io.github.glyphmods.solarpowered.content.optics.network.AbstractOpticalNetwork
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import kotlin.random.Random

// behold, a clone of TorquePropagator
sealed class AbstractOpticalNetworkManager<T : LevelAccessor> {
    protected val networks = hashMapOf<LevelAccessor, MutableMap<Long, AbstractOpticalNetwork<T>>>()
    val randomSource = Random.Default

    fun getNetwork(level: T, id: Long): AbstractOpticalNetwork<T>? =
        networks[level]?.get(id)

    fun destroyNetwork(level: T, id: Long) =
        networks[level]?.remove(id)

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : Level> getManagerForLevel(level: T): AbstractOpticalNetworkManager<T> =
            if (level.isClientSide) {
                ClientOpticalNetworkManager
            } else {
                ServerOpticalNetworkManager
            } as AbstractOpticalNetworkManager<T>
    }
}

