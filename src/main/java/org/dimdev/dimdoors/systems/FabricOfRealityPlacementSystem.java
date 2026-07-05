package org.dimdev.dimdoors.systems;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.dimdev.dimdoors.blocks.FabricOfRealityBlocks;
import org.joml.Vector3d;
import org.joml.Vector3i;

/**
 * Replaces a Fabric of Reality block when a normal block is placed against it.
 */
public class FabricOfRealityPlacementSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private static final double EYE_HEIGHT = 1.62;
    private static final double EPSILON = 1.0E-7;

    public FabricOfRealityPlacementSystem() {
        super(PlaceBlockEvent.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Query<EntityStore> getQuery() {
        return (Query<EntityStore>) (Object) Archetype.empty();
    }

    @Override
    public void handle(
            int index,
            ArchetypeChunk<EntityStore> archetypeChunk,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer,
            PlaceBlockEvent event
    ) {
        if (event.isCancelled()) {
            return;
        }

        Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
        if (isPlayerCrouching(playerRef, commandBuffer)) {
            return;
        }

        ItemStack itemInHand = event.getItemInHand();
        if (itemInHand == null || ItemStack.isEmpty(itemInHand)) {
            return;
        }

        String replacementBlockId = itemInHand.getBlockKey();
        if (replacementBlockId == null) {
            return;
        }



        ReplacementBlock replacementBlock = resolveReplacementBlock(replacementBlockId);

        if (replacementBlock == null || FabricOfRealityBlocks.isFabricBlock(replacementBlock.getBlockType())) {
            return;
        }

        World world = commandBuffer.getExternalData().getWorld();
        TransformComponent transform = commandBuffer.getComponent(
                playerRef,
                TransformComponent.getComponentType()
        );
        if (transform == null) {
            return;
        }

        Vector3i direction = placementExitDirection(
                playerRef,
                commandBuffer,
                transform.getPosition(),
                event.getTargetBlock()
        );
        if (direction == null) {
            return;
        }

        Vector3i fabricPosition = new Vector3i(event.getTargetBlock()).add(direction);
        FabricOfRealityBlocks.FabricTarget fabricTarget = FabricOfRealityBlocks.findFabricTarget(world, fabricPosition);
        if (fabricTarget == null) {
            return;
        }

        event.setCancelled(true);

        Vector3i targetPosition = fabricTarget.position();
        WorldChunk fabricChunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(targetPosition.x, targetPosition.z));
        if (fabricChunk == null) {
            return;
        }

        fabricChunk.setBlock(
                targetPosition.x,
                targetPosition.y,
                targetPosition.z,
                replacementBlock.getIndex(),
                replacementBlock.getBlockType(),
                event.getRotation().index(),
                0,
                0
        );
    }

    private boolean isPlayerCrouching(
            Ref<EntityStore> playerRef,
            CommandBuffer<EntityStore> commandBuffer
    ) {
        MovementStatesComponent movementStatesComponent = (MovementStatesComponent) commandBuffer.getComponent(
                playerRef,
                MovementStatesComponent.getComponentType()
        );
        if (movementStatesComponent == null) {
            return false;
        }

        MovementStates movementStates = movementStatesComponent.getMovementStates();
        return movementStates != null && (movementStates.crouching || movementStates.forcedCrouching);
    }

    private Vector3i placementExitDirection(
            Ref<EntityStore> playerRef,
            CommandBuffer<EntityStore> commandBuffer,
            Vector3d playerPosition,
            Vector3i targetBlock
    ) {
        Vector3d eyePosition = new Vector3d(playerPosition.x, playerPosition.y + EYE_HEIGHT, playerPosition.z);
        HeadRotation headRotation = (HeadRotation) commandBuffer.getComponent(playerRef, HeadRotation.getComponentType());
        Vector3d rayDirection = headRotation != null && headRotation.getDirection() != null
                ? headRotation.getDirection()
                : new Vector3d(
                        targetBlock.x + 0.5 - eyePosition.x,
                        targetBlock.y + 0.5 - eyePosition.y,
                        targetBlock.z + 0.5 - eyePosition.z
                );

        Vector3i exitDirection = rayExitDirection(eyePosition, rayDirection, targetBlock);
        return exitDirection != null ? exitDirection : closestAxisDirection(rayDirection);
    }

    private Vector3i rayExitDirection(Vector3d origin, Vector3d direction, Vector3i targetBlock) {
        AxisTrace trace = new AxisTrace();

        double minX = targetBlock.x;
        double minY = targetBlock.y;
        double minZ = targetBlock.z;
        double maxX = minX + 1.0;
        double maxY = minY + 1.0;
        double maxZ = minZ + 1.0;

        boolean intersectsTarget =
                processAxis(trace, origin.x, direction.x, minX, maxX, new Vector3i(-1, 0, 0), new Vector3i(1, 0, 0))
                        && processAxis(trace, origin.y, direction.y, minY, maxY, new Vector3i(0, -1, 0), new Vector3i(0, 1, 0))
                        && processAxis(trace, origin.z, direction.z, minZ, maxZ, new Vector3i(0, 0, -1), new Vector3i(0, 0, 1))
                        && trace.tExit >= 0.0
                        && trace.tExit >= Math.max(trace.tEnter, 0.0);

        return intersectsTarget ? trace.exitDirection : null;
    }

    private boolean processAxis(
            AxisTrace trace,
            double originValue,
            double directionValue,
            double minValue,
            double maxValue,
            Vector3i negativeDirection,
            Vector3i positiveDirection
    ) {
        if (Math.abs(directionValue) < EPSILON) {
            return originValue >= minValue && originValue <= maxValue;
        }

        double nearT;
        double farT;
        Vector3i farDirection;
        if (directionValue > 0.0) {
            nearT = (minValue - originValue) / directionValue;
            farT = (maxValue - originValue) / directionValue;
            farDirection = positiveDirection;
        } else {
            nearT = (maxValue - originValue) / directionValue;
            farT = (minValue - originValue) / directionValue;
            farDirection = negativeDirection;
        }

        if (nearT > trace.tEnter) {
            trace.tEnter = nearT;
        }
        if (farT < trace.tExit) {
            trace.tExit = farT;
            trace.exitDirection = farDirection;
        }

        return trace.tEnter <= trace.tExit;
    }

    private Vector3i closestAxisDirection(Vector3d vector) {
        double absX = Math.abs(vector.x);
        double absY = Math.abs(vector.y);
        double absZ = Math.abs(vector.z);

        if (absX == 0.0 && absY == 0.0 && absZ == 0.0) {
            return null;
        }
        if (absX >= absY && absX >= absZ) {
            return new Vector3i(sign(vector.x), 0, 0);
        }
        if (absY >= absX && absY >= absZ) {
            return new Vector3i(0, sign(vector.y), 0);
        }
        return new Vector3i(0, 0, sign(vector.z));
    }

    private ReplacementBlock resolveReplacementBlock(String blockId) {
        BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
        int index = assetMap.getIndex(blockId);
        BlockType blockType = assetMap.getAsset(blockId);
        if (blockType == null || index == Integer.MIN_VALUE) {
            return null;
        }

        return new ReplacementBlock(index, blockType);
    }

    private int sign(double value) {
        return value < 0.0 ? -1 : 1;
    }

    private static final class AxisTrace {
        private double tEnter = Double.NEGATIVE_INFINITY;
        private double tExit = Double.POSITIVE_INFINITY;
        private Vector3i exitDirection;
    }

    private static final class ReplacementBlock {
        private final int index;
        private final BlockType blockType;

        private ReplacementBlock(int index, BlockType blockType) {
            this.index = index;
            this.blockType = blockType;
        }

        private int getIndex() {
            return index;
        }

        private BlockType getBlockType() {
            return blockType;
        }
    }
}
