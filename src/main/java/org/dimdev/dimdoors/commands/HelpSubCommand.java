package org.dimdev.dimdoors.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

/**
 * /dimdoors help - Show available commands
 */
public class HelpSubCommand extends CommandBase {
    public HelpSubCommand() {
        super("help", "Show available commands");
        setPermissionGroup(GameMode.Creative);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void executeSync(CommandContext context) {
        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("=== DimensionalDoors Commands ==="));
        context.sendMessage(Message.raw("/dimdoors help - Show this help message"));
        context.sendMessage(Message.raw("/dimdoors info - Show plugin information"));
        context.sendMessage(Message.raw("/dimdoors reload - Reload configuration"));
        context.sendMessage(Message.raw("========================"));
    }
}
