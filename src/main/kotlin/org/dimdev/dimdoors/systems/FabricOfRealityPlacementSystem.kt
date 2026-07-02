package org.dimdev.dimdoors.systems

import com.hypixel.hytale.component.Archetype
import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.EntityEventSystem
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import org.dimdev.dimdoors.blocks.FabricOfRealityBlocks
import org.joml.Vector3d
import org.joml.Vector3i
import kotlin.math.abs

/**
 * Replaces a Fabric of Reality block when a normal block is placed against it.
 */
class FabricOfRealityPlacementSystem : EntityEventSystem<EntityStore, PlaceBlockEvent>(PlaceBlockEvent::class.java) {

    companion object {
        private const val EYE_HEIGHT = 1.62
        private const val EPSILON = 1.0E-7
    }

    override fun getQuery(): Query<EntityStore> = Archetype.empty()

    override fun handle(
        index: Int,
        archetypeChunk: ArchetypeChunk<EntityStore>,
        store: Store<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>,
        event: PlaceBlockEvent,
    ) {
        if (event.isCancelled) {
            return
        }

        val playerRef = archetypeChunk.getReferenceTo(index)
        if (isPlayerCrouching(playerRef, commandBuffer)) {
            return
        }

        val itemInHand = event.itemInHand ?: return
        if (ItemStack.isEmpty(itemInHand)) {
            return
        }

        val replacementBlockId = itemInHand.blockKey ?: return
        if (FabricOfRealityBlocks.isFabricBlockId(replacementBlockId)) {
            return
        }

        val world = commandBuffer.externalData.world
        val transform = commandBuffer.getComponent(playerRef, TransformComponent.getComponentType()) ?: return
        val direction = placementExitDirection(playerRef, commandBuffer, transform.position, event.targetBlock) ?: return
        val fabricPosition = Vector3i(event.targetBlock).add(direction)
        val fabricTarget = FabricOfRealityBlocks.findFabricTarget(world, fabricPosition) ?: return

        event.setCancelled(true)

        val replacementBlock = resolveReplacementBlock(replacementBlockId) ?: return
        val fabricChunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(fabricTarget.position.x, fabricTarget.position.z))
            ?: return

        fabricChunk.setBlock(
            fabricTarget.position.x,
            fabricTarget.position.y,
            fabricTarget.position.z,
            replacementBlock.index,
            replacementBlock.blockType,
            event.rotation.index(),
            0,
            0,
        )
    }

    private fun isPlayerCrouching(
        playerRef: Ref<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>,
    ): Boolean {
        val movementStates = commandBuffer
            .getComponent(playerRef, MovementStatesComponent.getComponentType())
            ?.movementStates
            ?: return false

        return movementStates.crouching || movementStates.forcedCrouching
    }

    private fun placementExitDirection(
        playerRef: Ref<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>,
        playerPosition: Vector3d,
        targetBlock: Vector3i,
    ): Vector3i? {
        val eyePosition = Vector3d(playerPosition.x, playerPosition.y + EYE_HEIGHT, playerPosition.z)
        val rayDirection = commandBuffer
            .getComponent(playerRef, HeadRotation.getComponentType())
            ?.direction
            ?: Vector3d(
                targetBlock.x + 0.5 - eyePosition.x,
                targetBlock.y + 0.5 - eyePosition.y,
                targetBlock.z + 0.5 - eyePosition.z,
            )

        return rayExitDirection(eyePosition, rayDirection, targetBlock)
            ?: closestAxisDirection(rayDirection)
    }

    private fun rayExitDirection(origin: Vector3d, direction: Vector3d, targetBlock: Vector3i): Vector3i? {
        var tEnter = Double.NEGATIVE_INFINITY
        var tExit = Double.POSITIVE_INFINITY
        var exitDirection: Vector3i? = null

        fun processAxis(
            originValue: Double,
            directionValue: Double,
            minValue: Double,
            maxValue: Double,
            negativeDirection: Vector3i,
            positiveDirection: Vector3i,
        ): Boolean {
            if (abs(directionValue) < EPSILON) {
                return originValue >= minValue && originValue <= maxValue
            }

            val nearT: Double
            val farT: Double
            val farDirection: Vector3i
            if (directionValue > 0.0) {
                nearT = (minValue - originValue) / directionValue
                farT = (maxValue - originValue) / directionValue
                farDirection = positiveDirection
            } else {
                nearT = (maxValue - originValue) / directionValue
                farT = (minValue - originValue) / directionValue
                farDirection = negativeDirection
            }

            if (nearT > tEnter) {
                tEnter = nearT
            }
            if (farT < tExit) {
                tExit = farT
                exitDirection = farDirection
            }

            return tEnter <= tExit
        }

        val minX = targetBlock.x.toDouble()
        val minY = targetBlock.y.toDouble()
        val minZ = targetBlock.z.toDouble()
        val maxX = minX + 1.0
        val maxY = minY + 1.0
        val maxZ = minZ + 1.0

        val intersectsTarget =
            processAxis(origin.x, direction.x, minX, maxX, Vector3i(-1, 0, 0), Vector3i(1, 0, 0)) &&
                processAxis(origin.y, direction.y, minY, maxY, Vector3i(0, -1, 0), Vector3i(0, 1, 0)) &&
                processAxis(origin.z, direction.z, minZ, maxZ, Vector3i(0, 0, -1), Vector3i(0, 0, 1)) &&
                tExit >= 0.0 &&
                tExit >= maxOf(tEnter, 0.0)

        return if (intersectsTarget) exitDirection else null
    }

    private fun closestAxisDirection(vector: Vector3d): Vector3i? {
        val absX = abs(vector.x)
        val absY = abs(vector.y)
        val absZ = abs(vector.z)

        return when {
            absX == 0.0 && absY == 0.0 && absZ == 0.0 -> null
            absX >= absY && absX >= absZ -> Vector3i(sign(vector.x), 0, 0)
            absY >= absX && absY >= absZ -> Vector3i(0, sign(vector.y), 0)
            else -> Vector3i(0, 0, sign(vector.z))
        }
    }

    private fun resolveReplacementBlock(blockId: String): ReplacementBlock? {
        val assetMap = BlockType.getAssetMap()
        val index = assetMap.getIndex(blockId)
        val blockType = assetMap.getAsset(blockId) ?: return null
        if (index == Int.MIN_VALUE) {
            return null
        }

        return ReplacementBlock(index, blockType)
    }

    private fun sign(value: Double): Int {
        return if (value < 0.0) -1 else 1
    }

    private data class ReplacementBlock(val index: Int, val blockType: BlockType)
}
