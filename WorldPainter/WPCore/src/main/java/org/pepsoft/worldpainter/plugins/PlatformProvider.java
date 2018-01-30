package org.pepsoft.worldpainter.plugins;

import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.ChunkStore;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.exporting.PostProcessor;
import org.pepsoft.worldpainter.exporting.WorldExporter;
import org.pepsoft.worldpainter.mapexplorer.MapRecognizer;

import java.io.File;
import java.util.List;

/**
 * Created by Pepijn on 12-2-2017.
 */
public interface PlatformProvider extends Provider<Platform> {
    /**
     * Create a new, empty chunk for a platform supported by this plugin.
     *
     * @param platform The platform for which to create a chunk.
     * @param x The X coordinate (in chunks) of the chunk to create.
     * @param z The Z coordinate (in chunks) of the chunk to create.
     * @param maxHeight The height (in blocks) of the chunk to create.
     * @return The newly created chunk.
     */
    Chunk createChunk(Platform platform, int x, int z, int maxHeight);

    /**
     * Obtain a {@link ChunkStore} which will save chunks in the format of the
     * platform, for a platform supported by this plugin and for a specific map
     * base directory and dimension number.
     *
     * @param platform The platform for which to provide a chunk store.
     * @param worldDir The map base directory for which to provide a chunk
     *                 store.
     * @param dimension The dimension number for which to provide a chunk store.
     * @return A chunk store which will write chunks in the appropriate format
     *     for the specified dimension under the specified base directory.
     */
    ChunkStore getChunkStore(Platform platform, File worldDir, int dimension);

    /**
     * Obtain a {@link WorldExporter} for the platform currently configured in
     * the specified world.
     *
     * @param world2 The world to export.
     * @return A world exporter which will export the specified world.
     */
    WorldExporter getExporter(World2 world2);

    /**
     * Get the default directory to select on the Export screen for a
     * platform supported by this plugin.
     *
     * @param platform The platform for which to provide the default export
     *                 directory.
     * @return The default export directory for the specified platform.
     */
    File getDefaultExportDir(Platform platform);

    /**
     * Obtain a {@link PostProcessor} for a platform supported by this plugin.
     *
     * @param platform The platform for which to provide a post processor.
     * @return A post processor for the specified platform.
     */
    PostProcessor getPostProcessor(Platform platform);

    /**
     * Obtain a {@link MapRecognizer} which can recognize directories as
     * containing maps corresponding to any platform supported by this
     * plugin.
     *
     * @return A {@link MapRecognizer} which can recognize directories as
     * containing maps corresponding to any platform supported by this
     * plugin.
     */
    MapRecognizer getMapRecognizer();
}