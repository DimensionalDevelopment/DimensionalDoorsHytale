package org.dimdev.dimdoors.blocks;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Shared identifiers and helpers for the Fabric of Reality proof-of-concept block set.
 */
public final class FabricOfRealityBlocks {
    public static final String FABRIC_OF_REALITY_TAG = "DimDoors=FabricOfReality";

    private FabricOfRealityBlocks() {
    }

    public static boolean isFabricBlock(BlockType blockType) {
        return blockType != null && hasExpandedTag(blockType, FABRIC_OF_REALITY_TAG);
    }

    public static FabricTarget findFabricTarget(World world, Vector3i targetBlock) {
        Vector3i position = new Vector3i(targetBlock);
        long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z);
        WorldChunk chunk = world.getChunkIfLoaded(chunkIndex);
        if (chunk == null) {
            return null;
        }

        BlockType blockType = chunk.getBlockType(position.x, position.y, position.z);
        if (!isFabricBlock(blockType)) {
            return null;
        }

        return new FabricTarget(position, blockType);
    }

    private static boolean hasExpandedTag(BlockType blockType, String tag) {
        if (blockType.getData() == null) {
            return false;
        }

        int tagIndex = AssetRegistry.getTagIndex(tag);
        return tagIndex != AssetRegistry.TAG_NOT_FOUND
                && blockType.getData().getExpandedTagIndexes().contains(tagIndex);
    }

    public record FabricTarget(Vector3i position, BlockType blockType) {}
}
