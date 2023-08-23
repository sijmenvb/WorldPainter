/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.exporters;

import org.pepsoft.minecraft.Direction;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Material;
import org.pepsoft.minecraft.TileEntity;
import org.pepsoft.util.Box;
import org.pepsoft.util.MathUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.exporting.*;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.objects.WPObject;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.util.WPObjectUtils;

import javax.vecmath.Point3i;
import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Random;

import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.minecraft.Material.*;
import static org.pepsoft.worldpainter.Platform.Capability.NAME_BASED;
import static org.pepsoft.worldpainter.Platform.Capability.WATERLOGGED_LEAVES;
import static org.pepsoft.worldpainter.objects.WPObject.*;

/**
 * An exporter which knows how to render {@link WPObject}s to a
 * {@link MinecraftWorld}.
 *
 * @author pepijn
 */
public abstract class WPObjectExporter<L extends Layer> extends AbstractLayerExporter<L> {
    public WPObjectExporter(Dimension dimension, Platform platform, ExporterSettings settings, L layer) {
        super(dimension, platform, settings, layer);
    }

    /**
     * Export an object to the world, taking into account the blocks that are
     * already there.
     *
     * @param world     The Minecraft world to which to export the object.
     * @param dimension The dimension corresponding to the exported world, used
     *                  to determine the terrain height.
     * @param platform
     * @param object    The object to export.
     * @param x         The X coordinate at which to export the object.
     * @param y         The Y coordinate at which to export the object.
     * @param z         The Z coordinate at which to export the object.
     */
    public static void renderObject(MinecraftWorld world, Dimension dimension, Platform platform, WPObject object,
            int x, int y, int z) {
        renderObject(world, dimension, platform, object, x, y, z, false);
    }

    /**
     * Export an object to the world, optionally taking into account the blocks
     * that are already there.
     *
     * @param world      The Minecraft world to which to export the object.
     * @param dimension  The dimension corresponding to the exported world, used
     *                   to determine the terrain height.
     * @param platform
     * @param object     The object to export.
     * @param x          The X coordinate at which to export the object.
     * @param y          The Y coordinate at which to export the object.
     * @param z          The Z coordinate at which to export the object.
     * @param obliterate When {@code true}, all blocks of the object are
     *                   placed regardless of what is already there. When
     *                   {@code false},
     *                   rules are followed and some or all blocks may not be
     *                   placed,
     *                   depending on what is already there.
     */
    public static void renderObject(MinecraftWorld world, Dimension dimension, Platform platform, WPObject object,
            int x, int y, int z, boolean obliterate) {
        try {
            final Point3i dim = object.getDimensions();
            final Point3i offset = object.getOffset();
            final int minHeight = world.getMinHeight(), maxHeight = world.getMaxHeight();
            if ((z + offset.z + dim.z - 1) >= maxHeight) {
                // Object doesn't fit in the world vertically
                return;
            }
            final int undergroundMode = object.getAttribute(ATTRIBUTE_UNDERGROUND_MODE);
            final int leafDecayMode = object.getAttribute(ATTRIBUTE_LEAF_DECAY_MODE);
            final boolean bottomless = dimension.isBottomless();
            final Material replaceMaterial;
            if (object.hasAttribute(ATTRIBUTE_REPLACE_WITH_AIR_MATERIAL)) {
                replaceMaterial = object.getAttribute(ATTRIBUTE_REPLACE_WITH_AIR_MATERIAL);
            } else if (object.hasAttribute(ATTRIBUTE_REPLACE_WITH_AIR)) {
                int[] ids = object.getAttribute(ATTRIBUTE_REPLACE_WITH_AIR);
                replaceMaterial = Material.get(ids[0], ids[1]);
            } else {
                replaceMaterial = null;
            }
            final boolean replaceBlocks = replaceMaterial != null;
            final boolean extendFoundation = object.getAttribute(ATTRIBUTE_EXTEND_FOUNDATION);
            final boolean waterLoggedLeaves = platform.capabilities.contains(WATERLOGGED_LEAVES);
            final boolean connectBlocks = object.getAttribute(ATTRIBUTE_CONNECT_BLOCKS)
                    && platform.capabilities.contains(NAME_BASED);
            // System.out.println("Object dimensions: " + dim + ", origin: " + orig);
            for (int dx = 0; dx < dim.x; dx++) {
                for (int dy = 0; dy < dim.y; dy++) {
                    final int worldX = x + dx + offset.x;
                    final int worldY = y + dy + offset.y;
                    final int terrainHeight = dimension.getIntHeightAt(worldX, worldY);
                    for (int dz = 0; dz < dim.z; dz++) {
                        if (object.getMask(dx, dy, dz)) {
                            final Material objectMaterial = object.getMaterial(dx, dy, dz);
                            final Material finalMaterial = (replaceBlocks && (objectMaterial == replaceMaterial)) ? AIR
                                    : objectMaterial;
                            final int worldZ = z + dz + offset.z;
                            if (worldZ < minHeight + (bottomless || obliterate ? 0 : 1)) {
                                continue;
                            } else if (obliterate) {
                                placeBlock(world, worldX, worldY, worldZ, finalMaterial, leafDecayMode,
                                        waterLoggedLeaves, connectBlocks);
                            } else {
                                final Material existingMaterial = world.getMaterialAt(worldX, worldY, worldZ);
                                if (worldZ <= terrainHeight) {
                                    switch (undergroundMode) {
                                        case COLLISION_MODE_ALL:
                                            // Replace every block
                                            placeBlock(world, worldX, worldY, worldZ, finalMaterial, leafDecayMode,
                                                    waterLoggedLeaves, connectBlocks);
                                            break;
                                        case COLLISION_MODE_SOLID:
                                            // Only replace if object block is solid
                                            if (!finalMaterial.veryInsubstantial) {
                                                placeBlock(world, worldX, worldY, worldZ, finalMaterial, leafDecayMode,
                                                        waterLoggedLeaves, connectBlocks);
                                            }
                                            break;
                                        case COLLISION_MODE_NONE:
                                            // Only replace less solid blocks
                                            if (existingMaterial.veryInsubstantial) {
                                                placeBlock(world, worldX, worldY, worldZ, finalMaterial, leafDecayMode,
                                                        waterLoggedLeaves, connectBlocks);
                                            }
                                            break;
                                    }
                                } else {
                                    // Above ground only replace less solid blocks
                                    if (existingMaterial.veryInsubstantial) {
                                        placeBlock(world, worldX, worldY, worldZ, finalMaterial, leafDecayMode,
                                                waterLoggedLeaves, connectBlocks);
                                    }
                                }
                            }
                            if (extendFoundation && (dz == 0) && (terrainHeight != Integer.MIN_VALUE)
                                    && (worldZ > terrainHeight) && (!finalMaterial.veryInsubstantial)) {
                                int legZ = worldZ - 1;
                                while ((legZ >= minHeight)
                                        && world.getMaterialAt(worldX, worldY, legZ).veryInsubstantial) {
                                    placeBlock(world, worldX, worldY, legZ, finalMaterial, leafDecayMode,
                                            waterLoggedLeaves, connectBlocks);
                                    legZ--;
                                }
                            }
                        }
                    }
                }
            }
            List<Entity> entities = object.getEntities();
            if (entities != null) {
                for (Entity entity : entities) {
                    double[] relPos = entity.getRelPos();
                    double entityX = x + relPos[0] + offset.x,
                            entityY = y + relPos[2] + offset.y,
                            entityZ = z + relPos[1] + offset.z;
                    if ((entityZ < minHeight) || (entityZ >= maxHeight)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("NOT adding entity " + entity.getId() + " @ " + entityX + "," + entityY + ","
                                    + entityZ + " because z coordinate is out of range!");
                        }
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Adding entity " + entity.getId() + " @ " + entityX + "," + entityY + ","
                                    + entityZ);
                        }
                        // Make sure each entity has a unique ID, otherwise
                        // Minecraft will see them all as duplicates and remove
                        // them:
                        entity.setUUID(UUID.randomUUID());
                        world.addEntity(entityX, entityY, entityZ, entity);
                    }
                }
            }
            List<TileEntity> tileEntities = object.getTileEntities();
            if (tileEntities != null) {
                for (TileEntity tileEntity : tileEntities) {
                    final int tileEntityX = x + tileEntity.getX() + offset.x,
                            tileEntityY = y + tileEntity.getZ() + offset.y,
                            tileEntityZ = z + tileEntity.getY() + offset.z;
                    final String entityId = tileEntity.getId();
                    if ((tileEntityZ < minHeight) || (tileEntityZ >= maxHeight)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("NOT adding tile entity " + entityId + " @ " + tileEntityX + "," + tileEntityY
                                    + "," + tileEntityZ + " because z coordinate is out of range!");
                        }
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Adding tile entity " + entityId + " @ " + tileEntityX + "," + tileEntityY
                                    + "," + tileEntityZ);
                        }
                        world.addTileEntity(tileEntityX, tileEntityY, tileEntityZ, tileEntity);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    e.getMessage() + " (object: " + object.getName() + " at " + x + "," + y + "," + z + ")", e);
        }
    }

    /**
     * Export an object to the world upside-down, optionally taking into account the
     * blocks that are already there. This
     * method does fewer checks than
     * {@link #renderObject(MinecraftWorld, Dimension, Platform, WPObject, int, int, int, boolean)}
     * because they don't make sense when rendering an object upside-down. It treats
     * the location as if it is entirely
     * above-ground.
     *
     * @param world    The Minecraft world to which to export the object.
     * @param platform
     * @param object   The object to export.
     * @param x        The X coordinate at which to export the object.
     * @param y        The Y coordinate at which to export the object.
     * @param z        The Z coordinate at which to export the object.
     */
    public static void renderObjectInverted(MinecraftWorld world, Platform platform, WPObject object, int x, int y,
            int z) {
        try {
            final Point3i dim = object.getDimensions();
            final Point3i offset = object.getOffset();
            final int minHeight = world.getMinHeight(), maxHeight = world.getMaxHeight();
            if ((z - offset.z) >= maxHeight) {
                // Object doesn't fit in the world vertically
                return;
            }
            final int leafDecayMode = object.getAttribute(ATTRIBUTE_LEAF_DECAY_MODE);
            final Material replaceMaterial;
            if (object.hasAttribute(ATTRIBUTE_REPLACE_WITH_AIR_MATERIAL)) {
                replaceMaterial = object.getAttribute(ATTRIBUTE_REPLACE_WITH_AIR_MATERIAL);
            } else if (object.hasAttribute(ATTRIBUTE_REPLACE_WITH_AIR)) {
                final int[] ids = object.getAttribute(ATTRIBUTE_REPLACE_WITH_AIR);
                replaceMaterial = Material.get(ids[0], ids[1]);
            } else {
                replaceMaterial = null;
            }
            final boolean replaceBlocks = replaceMaterial != null;
            final boolean waterLoggedLeaves = platform.capabilities.contains(WATERLOGGED_LEAVES);
            final boolean connectBlocks = object.getAttribute(ATTRIBUTE_CONNECT_BLOCKS)
                    && platform.capabilities.contains(NAME_BASED);
            for (int dx = 0; dx < dim.x; dx++) {
                for (int dy = 0; dy < dim.y; dy++) {
                    final int worldX = x + dx + offset.x;
                    final int worldY = y + dy + offset.y;
                    for (int dz = 0; dz < dim.z; dz++) {
                        if (object.getMask(dx, dy, dz)) {
                            final Material objectMaterial = object.getMaterial(dx, dy, dz);
                            final Material finalMaterial = (replaceBlocks && (objectMaterial == replaceMaterial)) ? AIR
                                    : objectMaterial;
                            final int worldZ = z - dz - offset.z;
                            final Material existingMaterial = world.getMaterialAt(worldX, worldY, worldZ);
                            // Only replace less solid blocks
                            if (existingMaterial.veryInsubstantial) {
                                placeBlock(world, worldX, worldY, worldZ, finalMaterial, leafDecayMode,
                                        waterLoggedLeaves, connectBlocks);
                            }
                        }
                    }
                }
            }
            List<Entity> entities = object.getEntities();
            if (entities != null) {
                for (Entity entity : entities) {
                    double[] relPos = entity.getRelPos();
                    double entityX = x + relPos[0] + offset.x,
                            entityY = y + relPos[2] + offset.y,
                            entityZ = z - relPos[1] - offset.z;
                    if ((entityZ < minHeight) || (entityZ >= maxHeight)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("NOT adding entity " + entity.getId() + " @ " + entityX + "," + entityY + ","
                                    + entityZ + " because z coordinate is out of range!");
                        }
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Adding entity " + entity.getId() + " @ " + entityX + "," + entityY + ","
                                    + entityZ);
                        }
                        // Make sure each entity has a unique ID, otherwise
                        // Minecraft will see them all as duplicates and remove
                        // them:
                        entity.setUUID(UUID.randomUUID());
                        world.addEntity(entityX, entityY, entityZ, entity);
                    }
                }
            }
            List<TileEntity> tileEntities = object.getTileEntities();
            if (tileEntities != null) {
                for (TileEntity tileEntity : tileEntities) {
                    final int tileEntityX = x + tileEntity.getX() + offset.x,
                            tileEntityY = y + tileEntity.getZ() + offset.y,
                            tileEntityZ = z - tileEntity.getY() - offset.z;
                    final String entityId = tileEntity.getId();
                    if ((tileEntityZ < minHeight) || (tileEntityZ >= maxHeight)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("NOT adding tile entity " + entityId + " @ " + tileEntityX + "," + tileEntityY
                                    + "," + tileEntityZ + " because z coordinate is out of range!");
                        }
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Adding tile entity " + entityId + " @ " + tileEntityX + "," + tileEntityY
                                    + "," + tileEntityZ);
                        }
                        world.addTileEntity(tileEntityX, tileEntityY, tileEntityZ, tileEntity);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    e.getMessage() + " (object: " + object.getName() + " at " + x + "," + y + "," + z + ")", e);
        }
    }

    /**
     * Check whether the coordinates of the extents of the object make sense. In
     * other words: whether it could potentially be placeable at all given its
     * dimensions and location.
     *
     * @return {@code true} if the object could potentially be placeable
     *         and the caller can proceed with further checks.
     */
    public static boolean isSane(WPObject object, int x, int y, int z, int minHeight, int maxHeight) {
        final Point3i dimensions = object.getDimensions();
        final Point3i offset = object.getOffset();
        if ((((long) x + offset.x) < Integer.MIN_VALUE)
                || (((long) x + dimensions.x - 1 + offset.x) > Integer.MAX_VALUE)) {
            // The object extends beyond the limits of a 32 bit signed integer in the X
            // dimension
            return false;
        }
        if ((((long) y + offset.y) < Integer.MIN_VALUE)
                || (((long) y + dimensions.y - 1 + offset.y) > Integer.MAX_VALUE)) {
            // The object extends beyond the limits of a 32 bit signed integer in the Y
            // dimension
            return false;
        }
        if (((long) z + offset.z) >= maxHeight) {
            // The object is entirely above maxHeight
            return false;
        }
        if (((long) z + dimensions.z - 1 + offset.z) < minHeight) {
            // The object is entirely below bedrock
            return false;
        }
        return true;
    }

    /**
     * Checks block by block and taking the object's collision mode attributes
     * and other rules into account whether it can be placed at a particular
     * location. This is a slow operation, so use
     * {@link #isSane(WPObject, int, int, int, int, int)} first to weed out objects
     * for which this check does not even apply.
     *
     * @return {@code true} if the object may be placed at the specified
     *         location according to its collision mode attributes.
     */
    public static boolean isRoom(final MinecraftWorld world, final Dimension dimension, final WPObject object,
            final int x, final int y, final int z, final Placement placement) {
        final Point3i dimensions = object.getDimensions();
        final Point3i offset = object.getOffset();
        final int collisionMode = object.getAttribute(ATTRIBUTE_COLLISION_MODE), minHeight = world.getMinHeight(),
                maxHeight = world.getMaxHeight();
        final boolean collideWithFloor = !object.getAttribute(ATTRIBUTE_SPAWN_ON_WATER_NO_COLLIDE);
        final boolean allowConnectingBlocks = false, bottomlessWorld = dimension.isBottomless();
        final int minZ = minHeight + (bottomlessWorld ? 0 : 1);
        // Check if the object fits vertically
        if (((long) z + dimensions.z - 1 + offset.z) >= maxHeight) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "No room for object " + object.getName() + " @ " + x + "," + y + "," + z + " with placement "
                                + placement + " because it does not fit below the map height of " + maxHeight);
            }
            return false;
        }
        if (((long) z + dimensions.z - 1 + offset.z) < minHeight) {
            if (logger.isTraceEnabled()) {
                logger.trace("No room for object " + object.getName() + " @ " + x + "," + y + "," + z
                        + " with placement " + placement + " because it is entirely below the bedrock");
            }
            return false;
        }
        if ((placement == Placement.ON_LAND) && (collisionMode != COLLISION_MODE_NONE)) {
            // Check block by block whether there is room
            for (int dx = 0; dx < dimensions.x; dx++) {
                for (int dy = 0; dy < dimensions.y; dy++) {
                    final int worldX = x + dx + offset.x, worldY = y + dy + offset.y;
                    for (int dz = 0; dz < dimensions.z; dz++) {
                        if (object.getMask(dx, dy, dz)) {
                            final Material objectBlock = object.getMaterial(dx, dy, dz);
                            if (!objectBlock.veryInsubstantial) {
                                final int worldZ = z + dz + offset.z;
                                if (worldZ < minZ) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                + "," + z + " with placement " + placement
                                                + " because it extends below the bottom of the map");
                                    }
                                    return false;
                                } else if (worldZ >= maxHeight) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                + "," + z + " with placement " + placement
                                                + " because it extends above the top of the map");
                                    }
                                    return false;
                                } else if (worldZ > dimension.getIntHeightAt(worldX, worldY)) {
                                    Material material = world.getMaterialAt(worldX, worldY, worldZ);
                                    if ((collisionMode == COLLISION_MODE_ALL)
                                            ? ((material != AIR) && (!material.isNamed(MC_WATER))
                                                    && (!material.isNamed(MC_LAVA)))
                                            : (!material.veryInsubstantial)) {
                                        // The block is above ground, it is present in the
                                        // custom object, is substantial, and there is already a
                                        // substantial block at the same location in the world;
                                        // there is no room for this object
                                        if (logger.isTraceEnabled()) {
                                            logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                    + "," + z + " with placement " + placement
                                                    + " due to collision with existing above ground block of type "
                                                    + world.getMaterialAt(worldX, worldY, worldZ));
                                        }
                                        return false;
                                    } else if ((!allowConnectingBlocks)
                                            && wouldConnect(world, worldX, worldY, worldZ, objectBlock)) {
                                        if (logger.isTraceEnabled()) {
                                            logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                    + "," + z + " with placement " + placement
                                                    + " because it would cause a connecting block");
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (placement == Placement.FLOATING) {
            for (int dx = 0; dx < dimensions.x; dx++) {
                for (int dy = 0; dy < dimensions.y; dy++) {
                    final int worldX = x + dx + offset.x, worldY = y + dy + offset.y;
                    final int terrainHeight = dimension.getIntHeightAt(worldX, worldY);
                    for (int dz = 0; dz < dimensions.z; dz++) {
                        if (object.getMask(dx, dy, dz)) {
                            final Material objectBlock = object.getMaterial(dx, dy, dz);
                            if (!objectBlock.veryInsubstantial) {
                                final int worldZ = z + dz + offset.z;
                                if (worldZ < minZ) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                + "," + z + " with placement " + placement
                                                + " because it extends below the bottom of the map");
                                    }
                                    return false;
                                } else if (worldZ >= maxHeight) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                + "," + z + " with placement " + placement
                                                + " because it extends above the top of the map");
                                    }
                                    return false;
                                } else if ((worldZ <= terrainHeight) && collideWithFloor) {
                                    // A solid block in the object collides with
                                    // the floor
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                + "," + z + " with placement " + placement
                                                + " due to collision with floor");
                                    }
                                    return false;
                                } else if ((worldZ > terrainHeight) && (collisionMode != COLLISION_MODE_NONE)) {
                                    if ((collisionMode == COLLISION_MODE_ALL)
                                            ? (!world.getMaterialAt(worldX, worldY, worldZ)
                                                    .isNamedOneOf(AIR_AND_FLUIDS))
                                            : (!world.getMaterialAt(worldX, worldY, worldZ).veryInsubstantial)) {
                                        // The block is present in the custom object, is
                                        // substantial, and there is already a
                                        // substantial block at the same location in the
                                        // world; there is no room for this object
                                        if (logger.isTraceEnabled()) {
                                            logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                    + "," + z + " with placement " + placement
                                                    + " due to collision with existing above ground block of type "
                                                    + world.getMaterialAt(worldX, worldY, worldZ));
                                        }
                                        return false;
                                    } else if ((!allowConnectingBlocks)
                                            && wouldConnect(world, worldX, worldY, worldZ, objectBlock)) {
                                        if (logger.isTraceEnabled()) {
                                            logger.trace("No room for object " + object.getName() + " @ " + x + "," + y
                                                    + "," + z + " with placement " + placement
                                                    + " because it would cause a connecting block");
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("There is room for object " + object.getName() + " @ " + x + "," + y + "," + z
                    + " with placement " + placement);
        }
        return true;
    }

    /**
     * Determine whether placing a block of the specified type at the specified
     * location in the specified world would cause the block to connect to
     * surrounding blocks (for instance a fence block to a solid block, or
     * another fence block, but not another fence of a different type).
     */
    private static boolean wouldConnect(MinecraftWorld world, int worldX, int worldY, int worldZ,
            Material objectBlock) {
        if (WPObjectUtils.wouldConnect(objectBlock, world.getMaterialAt(worldX - 1, worldY, worldZ))) {
            if (logger.isTraceEnabled()) {
                logger.trace(objectBlock + " @ " + worldX + "," + worldY + "," + worldZ + " would connect to "
                        + world.getMaterialAt(worldX - 1, worldY, worldZ) + " @ dx = -1");
            }
            return true;
        } else if (WPObjectUtils.wouldConnect(objectBlock, world.getMaterialAt(worldX, worldY - 1, worldZ))) {
            if (logger.isTraceEnabled()) {
                logger.trace(objectBlock + " @ " + worldX + "," + worldY + "," + worldZ + " would connect to "
                        + world.getMaterialAt(worldX, worldY - 1, worldZ) + " @ dy = -1");
            }
            return true;
        } else if (WPObjectUtils.wouldConnect(objectBlock, world.getMaterialAt(worldX + 1, worldY, worldZ))) {
            if (logger.isTraceEnabled()) {
                logger.trace(objectBlock + " @ " + worldX + "," + worldY + "," + worldZ + " would connect to "
                        + world.getMaterialAt(worldX + 1, worldY, worldZ) + " @ dx = 1");
            }
            return true;
        } else if (WPObjectUtils.wouldConnect(objectBlock, world.getMaterialAt(worldX, worldY + 1, worldZ))) {
            if (logger.isTraceEnabled()) {
                logger.trace(objectBlock + " @ " + worldX + "," + worldY + "," + worldZ + " would connect to "
                        + world.getMaterialAt(worldX, worldY + 1, worldZ) + " @ dy = 1");
            }
            return true;
        } else {
            return false;
        }
    }

    private static Box getBounds(WPObject object, int x, int y, int z) {
        final Point3i dimensions = object.getDimensions();
        final Point3i offset = object.getOffset();
        return new Box(x + offset.x, x + offset.x + dimensions.x,
                y + offset.y, y + offset.y + dimensions.y,
                z + offset.z, z + offset.z + dimensions.z);
    }

    private static void placeBlock(MinecraftWorld world, int x, int y, int height, Material material, int leafDecayMode,
            boolean waterloggedLeaves, boolean connectBlocks) {
        final boolean materialHasPropertyWaterlogged;
        if (material.leafBlock) {
            materialHasPropertyWaterlogged = waterloggedLeaves;
            if (leafDecayMode != LEAF_DECAY_NO_CHANGE) {
                if (leafDecayMode == LEAF_DECAY_ON) {
                    material = material.withProperty(PERSISTENT, false);
                } else {
                    material = material.withProperty(PERSISTENT, true);
                }
            }
        } else {
            materialHasPropertyWaterlogged = material.hasProperty(WATERLOGGED);
        }
        final Material existingMaterial = world.getMaterialAt(x, y, height);
        final boolean existingMaterialContainsWater = existingMaterial.containsWater();
        // Manage the waterlogged property, but only if we're confident what it should
        // be based on the block that is
        // already there
        if ((existingMaterial.translucent || existingMaterial.hasProperty(WATERLOGGED))
                && materialHasPropertyWaterlogged) {
            if (existingMaterialContainsWater) {
                material = material.withProperty(WATERLOGGED, true);
            } else {
                material = material.withProperty(WATERLOGGED, false);
            }
        }
        // Manage the cardinal direction properties for connecting blocks, if requested
        Set<Direction> checkReverseConnections = null;
        if (connectBlocks) {
            if (material.connectingBlock) {
                // The object block is a connecting block; check around it for other connecting
                // blocks or solid blocks
                if (WPObjectUtils.wouldConnect(material, world.getMaterialAt(x - 1, y, height))) {
                    material = material.withProperty(WEST, true);
                    checkReverseConnections = EnumSet.of(Direction.WEST);
                }
                if (WPObjectUtils.wouldConnect(material, world.getMaterialAt(x, y - 1, height))) {
                    material = material.withProperty(NORTH, true);
                    if (checkReverseConnections == null) {
                        checkReverseConnections = EnumSet.of(Direction.NORTH);
                    } else {
                        checkReverseConnections.add(Direction.NORTH);
                    }
                }
                if (WPObjectUtils.wouldConnect(material, world.getMaterialAt(x + 1, y, height))) {
                    material = material.withProperty(EAST, true);
                    if (checkReverseConnections == null) {
                        checkReverseConnections = EnumSet.of(Direction.EAST);
                    } else {
                        checkReverseConnections.add(Direction.EAST);
                    }
                }
                if (WPObjectUtils.wouldConnect(material, world.getMaterialAt(x, y + 1, height))) {
                    material = material.withProperty(SOUTH, true);
                    if (checkReverseConnections == null) {
                        checkReverseConnections = EnumSet.of(Direction.SOUTH);
                    } else {
                        checkReverseConnections.add(Direction.SOUTH);
                    }
                }
            } else if (material.solid && material.opaque) {
                // The object block is not a connecting block, but it *is* solid and opaque, so
                // check for surrounding
                // connecting blocks which might need to be connected to it
                if (WPObjectUtils.wouldConnect(material, world.getMaterialAt(x - 1, y, height))) {
                    checkReverseConnections = EnumSet.of(Direction.WEST);
                }
                if (WPObjectUtils.wouldConnect(material, world.getMaterialAt(x, y - 1, height))) {
                    if (checkReverseConnections == null) {
                        checkReverseConnections = EnumSet.of(Direction.NORTH);
                    } else {
                        checkReverseConnections.add(Direction.NORTH);
                    }
                }
                if (WPObjectUtils.wouldConnect(material, world.getMaterialAt(x + 1, y, height))) {
                    if (checkReverseConnections == null) {
                        checkReverseConnections = EnumSet.of(Direction.EAST);
                    } else {
                        checkReverseConnections.add(Direction.EAST);
                    }
                }
                if (WPObjectUtils.wouldConnect(material, world.getMaterialAt(x, y + 1, height))) {
                    if (checkReverseConnections == null) {
                        checkReverseConnections = EnumSet.of(Direction.SOUTH);
                    } else {
                        checkReverseConnections.add(Direction.SOUTH);
                    }
                }
            }
        }
        // Don't replace water with insubstantial blocks that don't have a waterlogged
        // property (assume such a block
        // would be washed away), except air. We are slightly guessing at what the user
        // would want to happen here...
        if ((!material.veryInsubstantial) || (!existingMaterialContainsWater) || material.containsWater()
                || (material == AIR)) {
            world.setMaterialAt(x, y, height, material);
            if (connectBlocks && (checkReverseConnections != null)) {
                makeReverseConnections(world, x, y, height, checkReverseConnections);
            }
        }
    }

    /**
     * Check neighbouring blocks to see if they need to be connected back to the
     * object block.
     */
    private static void makeReverseConnections(MinecraftWorld world, int x, int y, int height,
            Set<Direction> directions) {
        for (Direction direction : directions) {
            final int neighbourX = x + direction.getDx(), neighbourY = y + direction.getDy();
            Material neighbouringMaterial = world.getMaterialAt(neighbourX, neighbourY, height);
            if (neighbouringMaterial.connectingBlock) {
                switch (direction) {
                    case WEST:
                        neighbouringMaterial = neighbouringMaterial.withProperty(EAST, true);
                        break;
                    case NORTH:
                        neighbouringMaterial = neighbouringMaterial.withProperty(SOUTH, true);
                        break;
                    case EAST:
                        neighbouringMaterial = neighbouringMaterial.withProperty(WEST, true);
                        break;
                    case SOUTH:
                        neighbouringMaterial = neighbouringMaterial.withProperty(NORTH, true);
                        break;
                }
                world.setMaterialAt(neighbourX, neighbourY, height, neighbouringMaterial);
            }
        }
    }

    // TODO centralise block merging logic and make it more readable by being
    // systematic (haven't we already done that somewhere?)
    // For merging (new fluids are leading):
    // Existing block: | air | water | lava | insubstantial | solid |
    // New block: air | | x | x | | |
    // water | | | | | |
    // lava | | | | | |
    // insubstantial | | | | | |
    // solid | | | | | |
    // x = new block replaces existing block
    // Manage water separately:
    // If the new block contains water and the existing block is not solid,
    // the final block must contain water. If that is not possible it should
    // be replaced with water
    // If the new block does not contain water and the existing block is not solid,
    // the final block must not contain water. If that is not possible it
    // should be replaced with air

    // For custom objects (fluids should be merged):

    private static final String[] AIR_AND_FLUIDS = { MC_AIR, MC_WATER, MC_LAVA };
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WPObjectExporter.class);

    public static class WPObjectFixup implements Fixup {
        public WPObjectFixup(WPObject object, int x, int y, int z, Placement placement) {
            this.object = object;
            this.x = x;
            this.y = y;
            this.z = z;
            this.placement = placement;
        }

        @Override
        public void fixup(MinecraftWorld world, Dimension dimension, Platform platform,
                WorldExportSettings worldExportSettings, ExportSettings exportSettings) {
            // Recheck whether there is room
            if (isRoom(world, dimension, object, x, y, z, placement)) {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Placing custom object " + object.getName() + " @ " + x + "," + y + "," + z + " in fixup");
                }
                WPObjectExporter.renderObject(world, dimension, platform, object, x, y, z);

                // Reapply the Frost layer to the area, if necessary
                final boolean applyFrost;
                final ExporterSettings frostLayerSettings;
                if (dimension.getBitLayerValueAt(Frost.INSTANCE, x, y)) {
                    applyFrost = true;
                    frostLayerSettings = null;
                } else {
                    frostLayerSettings = dimension.getLayerSettings(Frost.INSTANCE);
                    applyFrost = (frostLayerSettings != null) && frostLayerSettings.isApplyEverywhere();
                }
                if (applyFrost) {
                    final FrostExporter frostExporter = new FrostExporter(dimension, platform, frostLayerSettings);
                    final Point3i offset = object.getOffset();
                    final Point3i dim = object.getDimensions();
                    final Rectangle area = new Rectangle(x + offset.x, y + offset.y, dim.x, dim.y);
                    frostExporter.addFeatures(area, null, world);
                }

                // Fixups are done *after* post-processing, so post process again
                final Box bounds = getBounds(object, x, y, z);
                // Include the layer below and above the object for post-processing, as those
                // blocks may also have been
                // affected
                bounds.setZ1(Math.max(bounds.getZ1() - 1, world.getMinHeight()));
                bounds.setZ2(Math.min(bounds.getZ2() + 1, world.getMaxHeight()));
                try {
                    PlatformManager.getInstance().getPostProcessor(platform).postProcess(world, bounds, exportSettings,
                            null);
                } catch (ProgressReceiver.OperationCancelled e) {
                    // Can't happen since we don't pass in a progress receiver
                    throw new InternalError();
                }

                // Fixups are done *after* calculating the block properties, so we have to do
                // that again (if requested)
                if ((exportSettings instanceof BlockBasedExportSettings)
                        && BlockPropertiesCalculator.isBlockPropertiesPassNeeded(platform, worldExportSettings,
                                (BlockBasedExportSettings) exportSettings)) {
                    recalculateBlockProperties(world, bounds, platform, worldExportSettings,
                            (BlockBasedExportSettings) exportSettings);
                }
            } else if (logger.isTraceEnabled()) {
                logger.trace(
                        "No room for custom object " + object.getName() + " @ " + x + "," + y + "," + z + " in fixup");
            }
        }

        private void recalculateBlockProperties(final MinecraftWorld world, final Box lightBox, final Platform platform,
                final WorldExportSettings worldExportSettings, final BlockBasedExportSettings exportSettings) {
            BlockPropertiesCalculator blockPropertiesCalculator = new BlockPropertiesCalculator(world, platform,
                    worldExportSettings, exportSettings);
            // Transpose coordinates from WP to MC coordinate system. Also expand the box to
            // light around it and try to
            // account for uneven terrain underneath the object
            Box dirtyArea = new Box(lightBox.getX1() - 1, lightBox.getX2() + 1,
                    MathUtils.clamp(world.getMinHeight(), lightBox.getZ1() - 4, world.getMaxHeight() - 1),
                    lightBox.getZ2(), lightBox.getY1() - 1, lightBox.getY2() + 1);
            if (dirtyArea.getVolume() == 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Dirty area for lighting calculation is empty; skipping lighting calculation");
                }
                return;
            }
            blockPropertiesCalculator.setDirtyArea(dirtyArea);
            if (logger.isTraceEnabled()) {
                logger.trace("Recalculating light in " + blockPropertiesCalculator.getDirtyArea());
            }
            blockPropertiesCalculator.firstPass();
            while (blockPropertiesCalculator.secondPass())
                ;
            blockPropertiesCalculator.finalise();
        }

        private final WPObject object;
        private final int x, y, z;
        private final Placement placement;

        private static final long serialVersionUID = 1L;
    }

    public enum Placement {
        NONE, FLOATING, ON_LAND
    }
}