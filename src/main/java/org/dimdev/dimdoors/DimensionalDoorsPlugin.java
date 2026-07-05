package org.dimdev.dimdoors;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.dimdev.dimdoors.commands.DimensionalDoorsPluginCommand;
import org.dimdev.dimdoors.systems.FabricOfRealityPlacementSystem;

import java.util.logging.Level;

/**
 * DimensionalDoors - A Hytale server plugin.
 */
public class DimensionalDoorsPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static DimensionalDoorsPlugin instance;

    public DimensionalDoorsPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static DimensionalDoorsPlugin getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        LOGGER.at(Level.INFO).log("[DimensionalDoors] Setting up...");

        registerCommands();
        registerListeners();
        registerSystems();

        LOGGER.at(Level.INFO).log("[DimensionalDoors] Setup complete!");
    }

    private void registerCommands() {
        try {
            getCommandRegistry().registerCommand(new DimensionalDoorsPluginCommand());
            LOGGER.at(Level.INFO).log("[DimensionalDoors] Registered /dimdoors command");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[DimensionalDoors] Failed to register commands");
        }
    }

    private void registerListeners() {
    }

    private void registerSystems() {
        try {
            getEntityStoreRegistry().registerSystem(new FabricOfRealityPlacementSystem());
            LOGGER.at(Level.INFO).log("[DimensionalDoors] Registered Fabric of Reality placement system");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[DimensionalDoors] Failed to register ECS systems");
        }
    }

    @Override
    protected void start() {
        LOGGER.at(Level.INFO).log("[DimensionalDoors] Started!");
        LOGGER.at(Level.INFO).log("[DimensionalDoors] Use /dimdoors help for commands");
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("[DimensionalDoors] Shutting down...");
        instance = null;
    }
}
