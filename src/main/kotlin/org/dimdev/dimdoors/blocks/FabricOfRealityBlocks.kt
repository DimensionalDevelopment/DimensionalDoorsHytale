package org.dimdev.dimdoors.blocks

import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
import com.hypixel.hytale.server.core.universe.world.World
import org.joml.Vector3i

/**
 * Shared identifiers and helpers for the Fabric of Reality proof-of-concept block set.
 */
object FabricOfRealityBlocks {

    private val DYE_COLORS = listOf(
        "White",
        "Orange",
        "Magenta",
        "Light_Blue",
        "Yellow",
        "Lime",
        "Pink",
        "Gray",
        "Light_Gray",
        "Cyan",
        "Purple",
        "Blue",
        "Brown",
        "Green",
        "Red",
        "Black",
    )

    private val fabricBlockIds = buildSet {
        add("DimDoors_Fabric_Of_Reality")
        add("DimDoors_Ancient_Fabric_Of_Reality")

        DYE_COLORS
            .filterNot { it == "Black" }
            .forEach { color ->
                add("DimDoors_${color}_Altered_Fabric_Of_Reality")
                add("DimDoors_${color}_Ancient_Altered_Fabric_Of_Reality")
            }
    }

    fun isFabricBlockId(blockId: String?): Boolean {
        if (blockId.isNullOrBlank()) {
            return false
        }

        return fabricBlockIds.contains(blockId.substringAfterLast(':'))
    }

    data class FabricTarget(val position: Vector3i, val blockType: BlockType)

    fun findFabricTarget(world: World, targetBlock: Vector3i): FabricTarget? {
        val position = Vector3i(targetBlock)
        val chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z)
        val chunk = world.getChunkIfLoaded(chunkIndex) ?: return null

        val blockType = chunk.getBlockType(position.x, position.y, position.z) ?: return null
        if (!isFabricBlockId(blockType.id)) {
            return null
        }

        return FabricTarget(position, blockType)
    }
}
