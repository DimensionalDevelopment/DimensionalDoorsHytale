package org.dimdev.dimdoors.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.dimdev.dimdoors.DimensionalDoorsPlugin;

/**
 * /dimdoors reload - Reload plugin configuration
 */
public class ReloadSubCommand extends CommandBase {
    public ReloadSubCommand() {
        super("reload", "Reload plugin configuration");
        setPermissionGroup(GameMode.Creative);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void executeSync(CommandContext context) {
        DimensionalDoorsPlugin plugin = DimensionalDoorsPlugin.getInstance();

        if (plugin == null) {
            context.sendMessage(Message.raw("Error: Plugin not loaded"));
            return;
        }

        context.sendMessage(Message.raw("Reloading DimensionalDoors..."));

        // TODO: Add your reload logic here.

        context.sendMessage(Message.raw("DimensionalDoors reloaded successfully!"));
    }
}
