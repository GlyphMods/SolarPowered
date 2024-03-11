package io.github.glyphmods.solarpowered.content.optics

import com.simibubi.create.CreateClient
import io.github.glyphmods.solarpowered.content.optics.network.manager.ClientOpticalNetworkManager
import io.github.glyphmods.solarpowered.infrastructure.NetworkColors
import io.github.glyphmods.solarpowered.infrastructure.addDirectionVector
import net.minecraft.client.Minecraft

object OpticalDebugger {
    val minecraft = Minecraft.getInstance()

    fun tick() {
        if (!minecraft.options.renderDebug) {
            return
        }
        val level = minecraft.level
        val networks = (level?.let { ClientOpticalNetworkManager.getAllNetworks(it) } ?: return)
        for ((id, network) in networks) {
            val color = NetworkColors.colorForId(id).color
            for ((target, link) in network.getGraph().edges()) {
                if (!level.isLoaded(target)) continue
                CreateClient.OUTLINER.showLine(
                    link,
                    link.origin,
                    link.hit ?: link.origin.addDirectionVector(link.direction, 3.0)
                )
                    .colored(color)
                    .lineWidth(if (link.hit != null) 0.2f else 0.1f)
            }
        }
    }
}