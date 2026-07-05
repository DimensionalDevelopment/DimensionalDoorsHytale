package org.dimdev.dimdoors.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * Main command for DimensionalDoors plugin.
 *
 * Usage:
 * - /dimdoors help - Show available commands
 * - /dimdoors info - Show plugin information
 * - /dimdoors reload - Reload plugin configuration
 */
public class DimensionalDoorsPluginCommand extends AbstractCommandCollection {
    public DimensionalDoorsPluginCommand() {
        super("dimdoors", "DimensionalDoors plugin commands");
        addSubCommand(new HelpSubCommand());
        addSubCommand(new InfoSubCommand());
        addSubCommand(new ReloadSubCommand());
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
}
