package io.github.glyphmods.solarpowered.content.optics.network

import io.github.glyphmods.solarpowered.content.optics.Link
import io.github.glyphmods.solarpowered.infrastructure.BlockEntityDescriptor
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

sealed class OpticalNetworkChangePacket {
    abstract val id: Long
    abstract val dimension: ResourceKey<Level>
    abstract val checksum: Int

    abstract fun serialize(buf: FriendlyByteBuf)
}

data class OpticalNetworkSyncRequestPacket(
    val id: Long,
    val dimension: ResourceLocation
) {
    constructor(buf: FriendlyByteBuf) : this(
        buf.readLong(),
        buf.readResourceLocation()
    )

    fun serialize(buf: FriendlyByteBuf) {
        buf.writeLong(id)
        buf.writeResourceLocation(dimension)
    }
}

data class OpticalNetworkSyncPacket(
    override val id: Long,
    override val dimension: ResourceKey<Level>,
    override val checksum: Int,
    val adjacency: Map<BlockEntityDescriptor, Map<BlockPos, Link>>
) : OpticalNetworkChangePacket() {
    constructor(buf: FriendlyByteBuf) : this(
        buf.readLong(),
        buf.readResourceKey(Registries.DIMENSION),
        buf.readInt(),
        buf.readMap(::BlockEntityDescriptor) { adjBuf ->
            adjBuf.readMap(
                FriendlyByteBuf::readBlockPos,
                Link::deserialize
            )
        }
    )

    override fun serialize(buf: FriendlyByteBuf) {
        buf.writeLong(id)
        buf.writeResourceKey(dimension)
        buf.writeInt(checksum)
        buf.writeMap(
            adjacency,
            { mapBuf, vertex ->
                vertex.serialize(mapBuf)
            },
            { mapBuf, adjacencies ->
                mapBuf.writeMap(
                    adjacencies,
                    { adjBuf, pos -> adjBuf.writeBlockPos(pos) },
                    { adjBuf, link -> link.serialize(adjBuf) }
                )
            }
        )
    }
}

enum class GraphChangeType {
    ADD, REMOVE
}

data class OpticalNetworkVertexChangePacket(
    override val id: Long,
    override val dimension: ResourceKey<Level>,
    override val checksum: Int,
    val type: GraphChangeType,
    val vertex: BlockEntityDescriptor
) : OpticalNetworkChangePacket() {
    constructor(buf: FriendlyByteBuf) : this(
        buf.readLong(),
        buf.readResourceKey(Registries.DIMENSION),
        buf.readInt(),
        buf.readEnum(GraphChangeType::class.java),
        BlockEntityDescriptor(buf)
    )

    override fun serialize(buf: FriendlyByteBuf) {
        buf.writeLong(id)
        buf.writeResourceKey(dimension)
        buf.writeInt(checksum)
        buf.writeEnum(type)
        vertex.serialize(buf)
    }
}

data class OpticalNetworkEdgeChangePacket(
    override val id: Long,
    override val dimension: ResourceKey<Level>,
    override val checksum: Int,
    val type: GraphChangeType,
    val from: BlockEntityDescriptor,
    val to: BlockPos,
    val link: Link?,
) : OpticalNetworkChangePacket() {
    constructor(buf: FriendlyByteBuf) : this(
        buf.readLong(),
        buf.readResourceKey(Registries.DIMENSION),
        buf.readInt(),
        buf.readEnum(GraphChangeType::class.java),
        BlockEntityDescriptor(buf),
        buf.readBlockPos(),
        buf.readNullable { b -> Link.deserialize(b) }
    )

    override fun serialize(buf: FriendlyByteBuf) {
        buf.writeLong(id)
        buf.writeResourceKey(dimension)
        buf.writeInt(checksum)
        buf.writeEnum(type)
        from.serialize(buf)
        buf.writeBlockPos(to)
        if (link == null) {
            buf.writeBoolean(false)
        } else {
            buf.writeBoolean(true)
            link.serialize(buf)
        }
    }
}