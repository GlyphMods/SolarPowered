package io.github.glyphmods.solarpowered.content.optics.network.graph

import io.github.glyphmods.solarpowered.content.optics.Link
import io.github.glyphmods.solarpowered.content.optics.OpticalBlockEntity
import io.github.glyphmods.solarpowered.content.optics.network.*
import io.github.glyphmods.solarpowered.infrastructure.SolarPacketHandler
import io.github.glyphmods.solarpowered.infrastructure.toDescriptor
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraftforge.network.PacketDistributor

class SyncedNetworkGraph(val id: Long, val dimension: ResourceKey<Level>) : NetworkGraph() {
    override fun addVertex(vertex: OpticalBlockEntity) {
        sendPacket(
            OpticalNetworkVertexChangePacket(
                id,
                dimension,
                hashCode(),
                GraphChangeType.ADD,
                vertex.toDescriptor()
            )
        )
        super.addVertex(vertex)
    }

    override fun addEdge(from: OpticalBlockEntity, to: BlockPos, link: Link) {
        sendPacket(
            OpticalNetworkEdgeChangePacket(
                id,
                dimension,
                hashCode(),
                GraphChangeType.ADD,
                from.toDescriptor(),
                link.traversedBlocks.last(),
                link
            )
        )
        super.addEdge(from, to, link)
    }

    override fun removeEdge(from: OpticalBlockEntity, to: BlockPos) {
        sendPacket(
            OpticalNetworkEdgeChangePacket(
                id,
                dimension,
                hashCode(),
                GraphChangeType.REMOVE,
                from.toDescriptor(),
                to,
                null
            )
        )
        super.removeEdge(from, to)
    }

    override fun removeVertex(vertex: OpticalBlockEntity) {
        sendPacket(
            OpticalNetworkVertexChangePacket(
                id,
                dimension,
                hashCode(),
                GraphChangeType.REMOVE,
                vertex.toDescriptor()
            )
        )
        super.removeVertex(vertex)
    }

    fun fullSync() {
        sendPacket(OpticalNetworkSyncPacket(
            id,
            dimension,
            hashCode(),
            adjacency().mapKeys { it.key.toDescriptor() }
        ))
    }

    private fun sendPacket(packet: OpticalNetworkChangePacket) {
        SolarPacketHandler.CHANNEL.send(
            PacketDistributor.DIMENSION.with { dimension },
            packet
        )
    }
}