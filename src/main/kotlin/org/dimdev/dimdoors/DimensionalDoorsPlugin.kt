package org.dimdev.dimdoors

import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.event.EventRegistry
import com.hypixel.hytale.logger.HytaleLogger
import org.dimdev.dimdoors.commands.DimensionalDoorsPluginCommand
import org.dimdev.dimdoors.listeners.PlayerListener
import org.dimdev.dimdoors.systems.FabricOfRealityPlacementSystem
import java.util.logging.Level

/**
 * DimensionalDoors - A Hytale server plugin.
 */
class DimensionalDoorsPlugin(init: JavaPluginInit) : JavaPlugin(init) {

    companion object {
        private val LOGGER = HytaleLogger.forEnclosingClass()

        @JvmStatic
        var instance: DimensionalDoorsPlugin? = null
            private set
    }

    init {
        instance = this
    }

    override fun setup() {
        LOGGER.at(Level.INFO).log("[DimensionalDoors] Setting up...")

        // Register commands
        registerCommands()

        // Register event listeners
        registerListeners()

        // Register ECS systems
        registerSystems()

        LOGGER.at(Level.INFO).log("[DimensionalDoors] Setup complete!")
    }

    private fun registerCommands() {
        try {
            commandRegistry.registerCommand(DimensionalDoorsPluginCommand())
            LOGGER.at(Level.INFO).log("[DimensionalDoors] Registered /dimdoors command")
        } catch (e: Exception) {
            LOGGER.at(Level.WARNING).withCause(e).log("[DimensionalDoors] Failed to register commands")
        }
    }

    private fun registerListeners() {
        val eventBus: EventRegistry = eventRegistry

        try {
            PlayerListener().register(eventBus)
            LOGGER.at(Level.INFO).log("[DimensionalDoors] Registered player event listeners")
        } catch (e: Exception) {
            LOGGER.at(Level.WARNING).withCause(e).log("[DimensionalDoors] Failed to register listeners")
        }
    }

    private fun registerSystems() {
        try {
            entityStoreRegistry.registerSystem(FabricOfRealityPlacementSystem())
            LOGGER.at(Level.INFO).log("[DimensionalDoors] Registered Fabric of Reality placement system")
        } catch (e: Exception) {
            LOGGER.at(Level.WARNING).withCause(e).log("[DimensionalDoors] Failed to register ECS systems")
        }
    }

    override fun start() {
        LOGGER.at(Level.INFO).log("[DimensionalDoors] Started!")
        LOGGER.at(Level.INFO).log("[DimensionalDoors] Use /dimdoors help for commands")
    }

    override fun shutdown() {
        LOGGER.at(Level.INFO).log("[DimensionalDoors] Shutting down...")
        instance = null
    }
}
