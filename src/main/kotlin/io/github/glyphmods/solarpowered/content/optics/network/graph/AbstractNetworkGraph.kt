package io.github.glyphmods.solarpowered.content.optics.network.graph

import io.github.glyphmods.solarpowered.content.optics.Link
import io.github.glyphmods.solarpowered.content.optics.OpticalBlockEntity
import net.minecraft.core.BlockPos

interface AbstractNetworkGraph {
    fun hasEdge(from: OpticalBlockEntity, to: BlockPos): Boolean
    fun outEdges(vertex: OpticalBlockEntity): Map<BlockPos, Link>
    fun inEdges(vertex: BlockPos): Map<BlockPos, Link>
    fun vertices(): Set<OpticalBlockEntity>
    fun clear()
}