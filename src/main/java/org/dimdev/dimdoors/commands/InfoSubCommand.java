package org.dimdev.dimdoors.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.dimdev.dimdoors.DimensionalDoorsPlugin;

/**
 * /dimdoors info - Show plugin information
 */
public class InfoSubCommand extends CommandBase {
    public InfoSubCommand() {
        super("info", "Show plugin information");
        setPermissionGroup(GameMode.Creative);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void executeSync(CommandContext context) {
        DimensionalDoorsPlugin plugin = DimensionalDoorsPlugin.getInstance();

        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("=== DimensionalDoors Info ==="));
        context.sendMessage(Message.raw("Name: DimensionalDoors"));
        context.sendMessage(Message.raw("Version: 1.0.0"));
        context.sendMessage(Message.raw("Author: Waterpicker"));
        context.sendMessage(Message.raw("Status: " + (plugin != null ? "Running" : "Not loaded")));
        context.sendMessage(Message.raw("===================="));
    }
}
