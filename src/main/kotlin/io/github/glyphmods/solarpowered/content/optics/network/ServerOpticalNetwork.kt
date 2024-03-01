package io.github.glyphmods.solarpowered.content.optics.network

import com.simibubi.create.foundation.utility.WorldHelper
import io.github.glyphmods.solarpowered.content.optics.Link
import io.github.glyphmods.solarpowered.content.optics.OpticalBlockEntity
import io.github.glyphmods.solarpowered.content.optics.SunlightBeam
import io.github.glyphmods.solarpowered.content.optics.network.graph.ServerNetworkGraph
import io.github.glyphmods.solarpowered.content.optics.network.manager.ServerOpticalNetworkManager
import io.github.glyphmods.solarpowered.infrastructure.addDirectionVector
import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import org.apache.logging.log4j.LogManager

class ServerOpticalNetwork(
    override val id: Long,
    override val level: ServerLevel
) : AbstractOpticalNetwork<ServerLevel>() {
    override val graph = ServerNetworkGraph()

    private val LOGGER = LogManager.getLogger("ServerOpticalNetwork/${id}")

    init {
        assert(!level.isClientSide)
        LOGGER.debug("Network created in world {}", WorldHelper.getDimensionID(level))
    }

    fun add(member: OpticalBlockEntity) {
        LOGGER.debug("Adding block entity {} to network", member)
        if (member.opticalNetworkId == null) {
            LOGGER.debug("{} is not in a network", member)
        } else {
            val other = ServerOpticalNetworkManager.getOrCreateNetwork(level, member.opticalNetworkId!!)
            LOGGER.debug("{} is already in network {}, merging", member, other)
            other.graph.vertices().forEach(other::remove)
        }
        graph.addVertex(member)
        member.opticalNetworkId = id
        member.onAddedToNetwork(this)
        propagate(member)
    }

    fun propagate(member: OpticalBlockEntity) {
        assert(member.opticalNetworkId == id) { "Attempted to propagate be $member (at ${member.blockPos}), which is in network ${member.opticalNetworkId}!" }
        LOGGER.debug("Propagating beams for {}", member)
        val incomingLinks = graph.inEdges(member.blockPos)
        LOGGER.debug("Incoming links: {}", incomingLinks)
        val oldOutgoingLinks = graph.outEdges(member).values.toSet()
        val newOutgoingLinks = member.getEmittedRays(incomingLinks.values.toSet()).map(::raycastBeam).toSet()
        LOGGER.debug("Old outgoing links: {}", oldOutgoingLinks)
        LOGGER.debug("New outgoing links: {}", newOutgoingLinks)
        if (incomingLinks.isEmpty() && newOutgoingLinks.isEmpty()) {
            remove(member)
            return
        }
        // added links
        for (link in newOutgoingLinks.subtract(oldOutgoingLinks)) {
            LOGGER.debug("Adding link: {}", link)
            graph.addEdge(member, link.traversedBlocks.last(), link)
            val be = level.getBlockEntity(link.traversedBlocks.last())
            if (be is OpticalBlockEntity) {

                if (be.opticalNetworkId == id) {
                    LOGGER.debug("{} is a member of our network, propagating to it", be)
                    propagate(be)
                } else {
                    LOGGER.debug("Adding {} to our network", be)
                    add(be)
                }
            } else {
                LOGGER.debug("Link target ({}) is not an OpticalBlockEntity", be)
            }
        }
        // removed links
        for (link in oldOutgoingLinks.subtract(newOutgoingLinks)) {
            LOGGER.debug("Removing link: {}", link)
            val be = level.getBlockEntity(link.traversedBlocks.last())
            if (be is OpticalBlockEntity) {
                if (be.opticalNetworkId != id) {
                    error("Block entity $be pointed to by $link is not a member of our network!")
                }
                graph.removeEdge(member, be.blockPos)
                propagate(be)
            }
        }
        // TODO update traversed blockpos-es
    }

    fun remove(member: OpticalBlockEntity) {
        LOGGER.debug("Removing member {}", member)
        val incoming = graph.inEdges(member.blockPos)
        val outgoing = graph.outEdges(member)
        graph.removeVertex(member)
        LOGGER.debug("Breaking incoming links: {}", incoming)
        for (edge in incoming) {
            val be = level.getBlockEntity(edge.key)
            if (be is OpticalBlockEntity) {
                propagate(be)
            }
        }
        LOGGER.debug("Removing outgoing links: {}", outgoing)
        for (edge in outgoing.filter { it.value.hit != null }) {
            val pos = BlockPos.containing(edge.value.hit!!)
            val be = level.getBlockEntity(pos)
            if (be is OpticalBlockEntity) {
                propagate(be)
            }
        }
        member.opticalNetworkId = null
        if (graph.vertices().isEmpty()) {
            LOGGER.debug("exploding myself :(")
            ServerOpticalNetworkManager.destroyNetwork(level, id)
        }
    }

    fun raycastBeam(beam: SunlightBeam): Link {
        val traversedBlocks = mutableSetOf<BlockPos>()
        val originPos = BlockPos.containing(beam.origin)
        val hit: BlockHitResult =
            BlockGetter.traverseBlocks(beam.origin, beam.origin.addDirectionVector(beam.direction, 128.0), ClipContext(
                beam.origin,
                beam.origin.addDirectionVector(beam.direction, 128.0),
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                null
            ), { ctx, pos ->
                if (originPos == pos) {
                    null
                } else {
                    traversedBlocks.add((pos as MutableBlockPos).immutable())
                    val state = level.getBlockState(pos)
                    level.clipWithInteractionOverride(
                        ctx.from,
                        ctx.to,
                        pos,
                        ctx.getBlockShape(state, level, pos),
                        state
                    )
                }
            }, { ctx ->
                val direction = ctx.from.subtract(ctx.to)
                BlockHitResult.miss(
                    ctx.to,
                    Direction.getNearest(direction.x, direction.y, direction.z),
                    BlockPos.containing(ctx.to)
                )
            })
        return when (hit.type) {
            HitResult.Type.MISS -> Link(beam.origin, beam.direction, beam.intensity, traversedBlocks, null)
            HitResult.Type.BLOCK -> Link(beam.origin, beam.direction, beam.intensity, traversedBlocks, hit.location)
            HitResult.Type.ENTITY -> error("Got entity hit result somehow!")
            null -> error("Hit type is null!")
        }
    }

    fun notifyBlockModified(pos: BlockPos) {
        graph.linksAtPos(pos)?.also { LOGGER.debug("Block at {} was modified!", pos) }?.forEach { link ->
            val originPos = BlockPos.containing(link.origin)
            val be = level.getBlockEntity(originPos)
            if (be !is OpticalBlockEntity) {
                error("Block entity $be at $originPos (origin of link $link) is not optical!")
            }
            propagate(be)
        }
    }
}