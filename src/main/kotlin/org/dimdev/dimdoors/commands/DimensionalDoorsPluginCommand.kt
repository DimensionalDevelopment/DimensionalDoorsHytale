package org.dimdev.dimdoors.commands

import com.hypixel.hytale.protocol.GameMode
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase

/**
 * Main command for DimensionalDoors plugin.
 *
 * Usage:
 * - /dimdoors help - Show available commands
 * - /dimdoors info - Show plugin information
 * - /dimdoors reload - Reload plugin configuration
 */
class DimensionalDoorsPluginCommand : AbstractCommandCollection("dimdoors", "DimensionalDoors plugin commands") {

    init {
        addSubCommand(HelpSubCommand())
        addSubCommand(InfoSubCommand())
        addSubCommand(ReloadSubCommand())
    }

    override fun canGeneratePermission(): Boolean = false
}

/**
 * /dimdoors help - Show available commands
 */
class HelpSubCommand : CommandBase("help", "Show available commands") {

    init {
        setPermissionGroup(GameMode.Creative)
    }

    override fun canGeneratePermission(): Boolean = false

    override fun executeSync(context: CommandContext) {
        context.sendMessage(Message.raw(""))
        context.sendMessage(Message.raw("=== DimensionalDoors Commands ==="))
        context.sendMessage(Message.raw("/dimdoors help - Show this help message"))
        context.sendMessage(Message.raw("/dimdoors info - Show plugin information"))
        context.sendMessage(Message.raw("/dimdoors reload - Reload configuration"))
        context.sendMessage(Message.raw("========================"))
    }
}

/**
 * /dimdoors info - Show plugin information
 */
class InfoSubCommand : CommandBase("info", "Show plugin information") {

    init {
        setPermissionGroup(GameMode.Creative)
    }

    override fun canGeneratePermission(): Boolean = false

    override fun executeSync(context: CommandContext) {
        val plugin = org.dimdev.dimdoors.DimensionalDoorsPlugin.instance

        context.sendMessage(Message.raw(""))
        context.sendMessage(Message.raw("=== DimensionalDoors Info ==="))
        context.sendMessage(Message.raw("Name: DimensionalDoors"))
        context.sendMessage(Message.raw("Version: 1.0.0"))
        context.sendMessage(Message.raw("Author: Waterpicker"))
        context.sendMessage(Message.raw("Status: " + if (plugin != null) "Running" else "Not loaded"))
        context.sendMessage(Message.raw("===================="))
    }
}

/**
 * /dimdoors reload - Reload plugin configuration
 */
class ReloadSubCommand : CommandBase("reload", "Reload plugin configuration") {

    init {
        setPermissionGroup(GameMode.Creative)
    }

    override fun canGeneratePermission(): Boolean = false

    override fun executeSync(context: CommandContext) {
        val plugin = org.dimdev.dimdoors.DimensionalDoorsPlugin.instance

        if (plugin == null) {
            context.sendMessage(Message.raw("Error: Plugin not loaded"))
            return
        }

        context.sendMessage(Message.raw("Reloading DimensionalDoors..."))

        // TODO: Add your reload logic here

        context.sendMessage(Message.raw("DimensionalDoors reloaded successfully!"))
    }
}