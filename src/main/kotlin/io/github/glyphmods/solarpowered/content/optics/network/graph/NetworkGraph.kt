package io.github.glyphmods.solarpowered.content.optics.network.graph

import io.github.glyphmods.solarpowered.content.optics.Link
import io.github.glyphmods.solarpowered.content.optics.OpticalBlockEntity
import net.minecraft.core.BlockPos

open class NetworkGraph : AbstractNetworkGraph {
    private val adjacency = hashMapOf<OpticalBlockEntity, MutableMap<BlockPos, Link>>()
    private val blockEntityPositions = hashMapOf<BlockPos, OpticalBlockEntity>()
    private val linkTraversals = hashMapOf<BlockPos, MutableSet<Link>>()

    open fun addEdge(from: OpticalBlockEntity, to: BlockPos, link: Link) {
        adjacency[from]!![to] = link
        for (pos in link.traversedBlocks) {
            linkTraversals.getOrPut(pos, ::mutableSetOf).add(link)
        }
    }

    open fun removeEdge(from: OpticalBlockEntity, to: BlockPos) {
        adjacency[from]!!.remove(to)!!.let { link ->
            for (pos in link.traversedBlocks) {
                linkTraversals[pos]!!.remove(link)
                if (linkTraversals[pos]!!.isEmpty()) {
                    linkTraversals.remove(pos)
                }
            }
        }
    }

    override fun hasEdge(from: OpticalBlockEntity, to: BlockPos) =
        to in adjacency[from]!!

    override fun outEdges(vertex: OpticalBlockEntity) =
        adjacency[vertex]!!.toMap()

    override fun inEdges(vertex: BlockPos) =
        adjacency.filter { vertex in it.value }.values.map { it.toMap() }
            .reduceOrNull { acc, mutableMap -> acc + mutableMap } ?: mapOf()

    open fun addVertex(vertex: OpticalBlockEntity) {
        assert(vertex !in adjacency) { "$vertex is already in this graph!" }
        adjacency[vertex] = mutableMapOf()
        blockEntityPositions[vertex.blockPos] = vertex
    }

    open fun removeVertex(vertex: OpticalBlockEntity) {
        inEdges(vertex.blockPos).forEach { removeEdge(blockEntityPositions[it.key]!!, vertex.blockPos) }
        outEdges(vertex).forEach { removeEdge(vertex, it.key) }
        adjacency.remove(vertex)
        blockEntityPositions.remove(vertex.blockPos)
    }

    override fun vertices() = adjacency.keys.toSet()
    override fun clear() {
        adjacency.clear()
        blockEntityPositions.clear()
    }

    override fun hashCode() =
        adjacency.map { (be, links) ->
            be.blockPos.hashCode() + links.map { (pos, link) -> pos.hashCode() + link.hashCode() }.sum()
        }.sum()

    fun linksAtPos(pos: BlockPos) = linkTraversals[pos]?.toSet()
    fun adjacency() = adjacency as Map<OpticalBlockEntity, Map<BlockPos, Link>>
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkGraph

        return adjacency == other.adjacency
    }
}