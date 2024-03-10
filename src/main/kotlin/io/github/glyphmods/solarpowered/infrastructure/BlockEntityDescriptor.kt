package io.github.glyphmods.solarpowered.infrastructure

import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.registries.ForgeRegistries

data class BlockEntityDescriptor(
    val type: ResourceLocation,
    val pos: BlockPos
) {
    constructor(buf: FriendlyByteBuf) : this(
        buf.readResourceLocation(),
        buf.readBlockPos()
    )

    fun serialize(buf: FriendlyByteBuf) {
        buf.writeResourceLocation(type)
        buf.writeBlockPos(pos)
    }

    fun toBlockEntity(level: LevelAccessor): BlockEntity {
        return checkNotNull(checkNotNull(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(type)) {
            "$type is not a valid block entity!"
        }.getBlockEntity(level, pos)) {
            "Block entity at $pos is not a $type!"
        }
    }

    @JvmName("toCheckedBlockEntity")
    inline fun <reified T : BlockEntity> toBlockEntity(level: LevelAccessor): T {
        val be = toBlockEntity(level)
        if (be !is T) {
            error("Block entity at $pos is a $type, which is not a subclass of ${T::class.qualifiedName!!}!")
        }
        return be
    }
}

fun BlockEntity.toDescriptor() =
    BlockEntityDescriptor(
        ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type)!!,
        blockPos
    )