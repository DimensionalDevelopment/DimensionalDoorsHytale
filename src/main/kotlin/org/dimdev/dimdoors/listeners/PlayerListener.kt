package org.dimdev.dimdoors.listeners

import com.hypixel.hytale.event.EventRegistry
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent
import com.hypixel.hytale.server.core.inventory.ItemStack
import org.dimdev.dimdoors.blocks.FabricOfRealityBlocks
import java.util.logging.Level

/**
 * Listener for player connection events.
 */
class PlayerListener {

    companion object {
        private val LOGGER = HytaleLogger.forEnclosingClass()
    }

    /**
     * Register all player event listeners.
     */
    fun register(eventBus: EventRegistry) {

    }

    private fun isReplacementInteraction(actionType: InteractionType): Boolean {
        return actionType == InteractionType.Secondary || actionType == InteractionType.Use
    }

    private fun isPlayerCrouching(event: PlayerInteractEvent): Boolean {
        val playerRef = event.playerRef ?: return false
        if (!playerRef.isValid) {
            return false
        }

        val world = event.player?.world ?: return false
        val movementStates = world.entityStore.store
            .getComponent(playerRef, MovementStatesComponent.getComponentType())
            ?.movementStates
            ?: return false

        return movementStates.crouching || movementStates.forcedCrouching
    }
}
