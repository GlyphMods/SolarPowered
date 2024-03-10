package io.github.glyphmods.solarpowered.content.optics.network

import com.simibubi.create.foundation.utility.WorldHelper
import io.github.glyphmods.solarpowered.content.optics.network.graph.AbstractNetworkGraph
import net.minecraft.world.level.LevelAccessor

sealed class AbstractOpticalNetwork<T : LevelAccessor> {
    abstract val id: Long
    abstract val level: T
    abstract fun getGraph(): AbstractNetworkGraph

    override fun toString() = "<${this::class.simpleName} $id in ${WorldHelper.getDimensionID(level)}>"
}