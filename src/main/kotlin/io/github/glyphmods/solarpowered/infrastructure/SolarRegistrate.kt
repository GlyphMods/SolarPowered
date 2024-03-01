package io.github.glyphmods.solarpowered.infrastructure

import com.jozufozu.flywheel.api.MaterialManager
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance
import com.simibubi.create.foundation.data.CreateBlockEntityBuilder
import com.simibubi.create.foundation.data.CreateRegistrate
import com.tterrag.registrate.AbstractRegistrate
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory
import com.tterrag.registrate.builders.BuilderCallback
import com.tterrag.registrate.util.OneTimeEventReceiver
import com.tterrag.registrate.util.nullness.NonNullSupplier
import io.github.glyphmods.solarpowered.SolarPowered
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.util.NonNullPredicate
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.util.function.BiFunction

class SolarRegistrate(modId: String) : CreateRegistrate(modId) {
    override fun getModEventBus(): IEventBus {
        // me when
        return MOD_BUS
    }

    override fun <T : BlockEntity, P : Any> blockEntity(
        parent: P, name: String,
        factory: BlockEntityFactory<T>
    ): CreateBlockEntityBuilder<T, P> {
        return entry(name) { callback: BuilderCallback ->
            EEBlockEntityBuilder.create(
                this,
                parent,
                name,
                callback,
                factory
            )
        } as (CreateBlockEntityBuilder<T, P>)
    }
}

// A carbon copy of Create's BlockEntityBuilder, except it uses our registrate so as not to die from an unexpected context type
// thanks much, create devs
class EEBlockEntityBuilder<T : BlockEntity, P>(
    owner: AbstractRegistrate<*>, parent: P & Any, name: String, callback: BuilderCallback,
    factory: BlockEntityFactory<T>
) :
    CreateBlockEntityBuilder<T, P>(owner, parent, name, callback, factory) {
    private var instanceFactory: NonNullSupplier<BiFunction<MaterialManager, T, BlockEntityInstance<in T>>>? =
        null
    private lateinit var renderNormally: NonNullPredicate<T>

    private val deferredValidBlocks: MutableCollection<NonNullSupplier<out MutableCollection<NonNullSupplier<out Block>>>> =
        ArrayList()

    override fun validBlocksDeferred(
        blocks: NonNullSupplier<out MutableCollection<NonNullSupplier<out Block?>>?>
    ): EEBlockEntityBuilder<T, P> {
        deferredValidBlocks.add(blocks)
        return this
    }

    override fun createEntry(): BlockEntityType<T> {
        deferredValidBlocks.stream()
            .map { obj -> obj.get() }
            .flatMap { obj -> obj.stream() }
            .forEach(::validBlock)
        return super.createEntry()
    }

    override fun instance(
        instanceFactory: NonNullSupplier<BiFunction<MaterialManager, T, BlockEntityInstance<in T>>>,
        renderNormally: Boolean
    ): EEBlockEntityBuilder<T, P> {
        return instance(instanceFactory) { be: T -> renderNormally }
    }

    override fun instance(
        instanceFactory: NonNullSupplier<BiFunction<MaterialManager, T, BlockEntityInstance<in T>>>,
        renderNormally: NonNullPredicate<T>
    ): EEBlockEntityBuilder<T, P> {
        if (this.instanceFactory == null) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable { this.registerInstance() } }
        }

        this.instanceFactory = instanceFactory
        this.renderNormally = renderNormally

        return this
    }

    override fun registerInstance() {
        OneTimeEventReceiver.addModListener(
            SolarPowered.REGISTRATE,
            FMLClientSetupEvent::class.java
        ) {
            val instanceFactory = this.instanceFactory
            if (instanceFactory != null) {
                val renderNormally: NonNullPredicate<T> = this.renderNormally
                InstancedRenderRegistry.configure(entry)
                    .factory(instanceFactory.get())
                    .skipRender { be -> !renderNormally.test(be) }
                    .apply()
            }
        }
    }

    companion object {
        fun <T : BlockEntity, P : Any> create(
            owner: AbstractRegistrate<*>, parent: P,
            name: String, callback: BuilderCallback, factory: BlockEntityFactory<T>
        ): EEBlockEntityBuilder<T, P> {
            return EEBlockEntityBuilder(owner, parent, name, callback, factory)
        }
    }
}