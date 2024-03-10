package io.github.glyphmods.solarpowered.content.optics

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.toVec3i
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.toVector3f

open class SunlightBeam(
    val origin: Vec3,
    val direction: Vec3i,
    val intensity: Int,
) {
    constructor(buf: FriendlyByteBuf) : this(
        Vec3(buf.readVector3f()),
        Vec3(buf.readVector3f()).toVec3i(),
        buf.readInt()
    )

    override fun toString(): String {
        return "<${this::class.simpleName} @ $origin ${direction}>"
    }

    override fun hashCode() =
        origin.hashCode() + direction.hashCode() + intensity.hashCode()

    override fun equals(other: Any?) =
        other is SunlightBeam && other.origin == origin && other.direction == direction && other.intensity == intensity

    open fun serialize(buf: FriendlyByteBuf) {
        buf.writeVector3f(origin.toVector3f())
        buf.writeVector3f(direction.toVector3f())
        buf.writeInt(intensity)
    }
}

open class Link(
    origin: Vec3,
    direction: Vec3i,
    intensity: Int,
    val traversedBlocks: MutableSet<BlockPos>,
    val hit: Vec3?
) : SunlightBeam(
    origin,
    direction, intensity
) {
    override fun serialize(buf: FriendlyByteBuf) {
        super.serialize(buf)
        buf.writeCollection(traversedBlocks) { b, pos -> b.writeBlockPos(pos) }
        buf.writeNullable(hit?.toVector3f(), FriendlyByteBuf::writeVector3f)
    }

    override fun toString(): String {
        return "<${this::class.simpleName} @ $origin $direction -> $hit>"
    }

    override fun equals(other: Any?) =
        other is Link && other.origin == origin && other.direction == direction && other.intensity == intensity && other.hit == hit

    override fun hashCode() =
        origin.hashCode() + direction.hashCode() + intensity.hashCode() + hit.hashCode()

    companion object {
        fun deserialize(buf: FriendlyByteBuf) =
            Link(
                Vec3(buf.readVector3f()),
                Vec3(buf.readVector3f()).toVec3i(),
                buf.readInt(),
                buf.readCollection(::LinkedHashSet) { b -> b.readBlockPos() },
                buf.readNullable(FriendlyByteBuf::readVector3f)?.let { Vec3(it) }
            )
    }
}

//class ConnectedLink(
//    origin: Vec3,
//    direction: Vec3i,
//    intensity: Int,
//    traversedBlocks: MutableSet<BlockPos>,
//    val target: Vec3,
//) : Link(origin, direction, intensity, traversedBlocks) {
//    override fun toString(): String {
//        return "<${this::class.simpleName} @ ${origin} ${direction} to ${target}>"
//    }
//
//    override fun serialize(buf: FriendlyByteBuf) {
//        super.serialize(buf, true)
//        buf.writeCollection(traversedBlocks) { b, pos -> b.writeBlockPos(pos) }
//        buf.writeVector3f(target.toVector3f())
//    }
//}