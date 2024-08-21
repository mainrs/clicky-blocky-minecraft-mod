package net.zerotask.minecraft.clickthrough.client

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory

object ItemFrameClickThroughClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("item-frame-click-through-client")

    override fun onInitializeClient() {
        logger.info("initialized client")
    }
}

fun releaseWindowControlsInBreakpoints(): Boolean {
    GLFW.glfwSetInputMode(
        MinecraftClient.getInstance().window.handle,
        GLFW.GLFW_CURSOR,
        GLFW.GLFW_CURSOR_NORMAL
    )
    return true
}
