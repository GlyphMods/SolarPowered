package io.github.glyphmods.solarpowered.content

import com.simibubi.create.foundation.block.IBE
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import io.github.glyphmods.solarpowered.content.optics.OpticalSourceBlockEntity
import io.github.glyphmods.solarpowered.content.optics.SunlightBeam
import io.github.glyphmods.solarpowered.registry.SolarBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class TestLaserBlock(pProperties: Properties) : Block(pProperties), IBE<TestLaserBlockEntity> {
    override fun getBlockEntityClass(): Class<TestLaserBlockEntity> =
        TestLaserBlockEntity::class.java

    override fun getBlockEntityType(): BlockEntityType<out TestLaserBlockEntity> =
        SolarBlockEntities.TEST_LASER.get()

}

class TestLaserBlockEntity(type: BlockEntityType<*>?, pos: BlockPos, state: BlockState) : OpticalSourceBlockEntity(
    type, pos,
    state
) {
    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>?) {

    }

    override fun getEmittedRays(receivedRays: Set<SunlightBeam>): Set<SunlightBeam> {
        return setOf(SunlightBeam(worldPosition.center, Direction.UP.normal, 15))
    }

}