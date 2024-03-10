package io.github.glyphmods.solarpowered.content.optics.network

import com.simibubi.create.foundation.utility.WorldHelper
import io.github.glyphmods.solarpowered.content.optics.OpticalBlockEntity
import io.github.glyphmods.solarpowered.content.optics.network.graph.AbstractNetworkGraph
import io.github.glyphmods.solarpowered.content.optics.network.graph.NetworkGraph
import io.github.glyphmods.solarpowered.content.optics.network.manager.ClientOpticalNetworkManager
import io.github.glyphmods.solarpowered.infrastructure.SolarPacketHandler
import net.minecraft.client.multiplayer.ClientLevel
import org.apache.logging.log4j.LogManager

class ClientOpticalNetwork(override val id: Long, override val level: ClientLevel) :
    AbstractOpticalNetwork<ClientLevel>() {
    private val graph = NetworkGraph()
    private val LOGGER = LogManager.getLogger("ClientOpticalNetwork/${id}")

    init {
        assert(level.isClientSide)
        LOGGER.debug("Network created in world {}", WorldHelper.getDimensionID(level))
    }

    fun handleChangePacket(packet: OpticalNetworkChangePacket) {
        if (packet !is OpticalNetworkSyncPacket && graph.hashCode() != packet.checksum) {
            LOGGER.warn("Checksum does not match (L ${graph.hashCode()} R ${packet.checksum}), requesting resync")
            SolarPacketHandler.CHANNEL.sendToServer(OpticalNetworkSyncRequestPacket(id, level.dimension().location()))
            return
        }
        when (packet) {
            is OpticalNetworkSyncPacket -> {
                LOGGER.debug("Performing full network resynchronization!")
                graph.clear()
                if (packet.adjacency.isEmpty()) {
                    LOGGER.debug("exploding myself :(")
                    ClientOpticalNetworkManager.destroyNetwork(level, id)
                } else {
                    for ((descriptor, adjacencies) in packet.adjacency) {
                        val be = descriptor.toBlockEntity<OpticalBlockEntity>(level)
                        graph.addVertex(be)
                        for ((pos, link) in adjacencies) {
                            graph.addEdge(be, pos, link)
                        }
                    }
                }
            }

            is OpticalNetworkEdgeChangePacket -> {
                LOGGER.debug(
                    "Edge {}: {} -> {}",
                    if (packet.type == GraphChangeType.ADD) "added" else "removed",
                    packet.from,
                    packet.to
                )
                when (packet.type) {
                    GraphChangeType.ADD -> graph.addEdge(packet.from.toBlockEntity<_>(level), packet.to, packet.link!!)
                    GraphChangeType.REMOVE -> graph.removeEdge(packet.from.toBlockEntity<_>(level), packet.to)
                }
            }

            is OpticalNetworkVertexChangePacket -> {
                LOGGER.debug(
                    "Vertex {}: {} @ {}",
                    if (packet.type == GraphChangeType.ADD) "added" else "removed",
                    packet.vertex.type,
                    packet.vertex.pos
                )
                when (packet.type) {
                    GraphChangeType.ADD -> graph.addVertex(packet.vertex.toBlockEntity<_>(level))
                    GraphChangeType.REMOVE -> graph.removeVertex(packet.vertex.toBlockEntity<_>(level))
                }
            }
        }
    }

    override fun getGraph(): AbstractNetworkGraph = graph
}