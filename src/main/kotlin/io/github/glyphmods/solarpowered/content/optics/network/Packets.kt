package io.github.glyphmods.solarpowered.content.optics.network

import io.github.glyphmods.solarpowered.content.optics.Link
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.registries.ForgeRegistries

data class BlockEntityDescriptor(
    val key: ResourceKey<BlockEntityType<*>>,
    val pos: BlockPos
) {
    fun serialize(buf: FriendlyByteBuf) {
        buf.writeResourceKey(key)
        buf.writeBlockPos(pos)
    }

    companion object {
        fun deserialize(buf: FriendlyByteBuf) =
            BlockEntityDescriptor(
                buf.readResourceKey(ForgeRegistries.BLOCK_ENTITY_TYPES.registryKey),
                buf.readBlockPos()
            )
    }
}

fun BlockEntity.toDescriptor() =
    BlockEntityDescriptor(
        ForgeRegistries.BLOCK_ENTITY_TYPES.getResourceKey(type).get(),
        blockPos
    )

class OpticalNetworkSyncPacket(
    val type: Type,
    val id: Long,
    val dimension: ResourceLocation,
    val links: Map<Link, BlockEntityDescriptor>,
    val connectionPositions: Map<BlockPos, Set<Link>>
) {
    constructor(buf: FriendlyByteBuf) : this(
        buf.readEnum(Type::class.java),
        buf.readLong(),
        buf.readResourceLocation(),
        buf.readMap(Link::deserialize, BlockEntityDescriptor::deserialize),
        buf.readMap(FriendlyByteBuf::readBlockPos) { b -> b.readCollection(::HashSet, Link::deserialize) },
    )

    fun serialize(buf: FriendlyByteBuf) {
        buf.writeEnum(type)
        buf.writeLong(id)
        buf.writeResourceLocation(dimension)
        buf.writeMap(
            links,
            { b, link -> link.serialize(b) },
            { b, descriptor -> descriptor.serialize(b) }
        )
        buf.writeMap(
            connectionPositions,
            { b, pos -> b.writeBlockPos(pos) },
            { b, links -> b.writeCollection(links) { c, link -> link.serialize(c) } }
        )
    }

    enum class Type {
        ADD, REMOVE
    }
}