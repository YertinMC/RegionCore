package top.yertinmc.regioncore;

import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A region data type manager.
 *
 * @param <W> The type of <code>World</code>
 */
@SuppressWarnings("unused")
public class RegionDataManager<W> {

    private final RegionDataDefinition<W> definition;
    private final Logger logger;
    private final File baseDirectory;
    private final Map<W, WorldRegionDataManager<W>> worldManagers = new HashMap<>();

    public RegionDataManager(RegionDataDefinition<W> definition, Logger logger, File baseDirectory) {
        this.definition = definition;
        this.logger = logger;
        this.baseDirectory = baseDirectory;
        if (!baseDirectory.exists())
            //noinspection ResultOfMethodCallIgnored
            baseDirectory.mkdirs();
    }

    /**
     * Get a world region data manager for the world.
     *
     * @param world The world
     * @return The manager
     */
    public WorldRegionDataManager<W> getManager(W world) {
        if (!worldManagers.containsKey(world)) {
            synchronized (this) {
                if (!worldManagers.containsKey(world)) {
                    worldManagers.put(world, new WorldRegionDataManager<>(this, world));
                }
            }
        }
        return worldManagers.get(world);
    }

    /**
     * Load a chunk.
     *
     * @param world The world
     * @param x     The X position of the chunk
     * @param z     The Z position of the chunk
     */
    public void loadChunk(W world, int x, int z) {
        getManager(world).loadChunk(x, z);
    }

    /**
     * Unload a chunk.
     *
     * @param world The world
     * @param x     The X position of the chunk
     * @param z     The Z position of the chunk
     */
    public void unloadChunk(W world, int x, int z) {
        getManager(world).unloadChunk(x, z);
    }

    /**
     * Write all loaded and modified regions.
     */
    public void write() {
        synchronized (this) {
            for (WorldRegionDataManager<W> manager : worldManagers.values()) {
                manager.write();
            }
        }
    }

    /**
     * Get the data of a block.
     *
     * @param world The world
     * @param x     The X position of the block
     * @param y     The Y position of the block
     * @param z     The Z position of the block
     * @return The data of the block if created and not empty, else null
     */
    public Object get(W world, int x, int y, int z) {
        return getManager(world).get(x, y, z);
    }

    /**
     * Set the data of a block.
     *
     * @param world The world
     * @param x     The X position of the block
     * @param y     The Y position of the block
     * @param z     The Z position of the block
     * @param data  The data of the block to set
     */
    public void set(W world, int x, int y, int z, Object data) {
        getManager(world).set(x, y, z, data);
    }

    /**
     * Remove the data of a block.
     *
     * @param world The world
     * @param x     The X position of the block
     * @param y     The Y position of the block
     * @param z     The Z position of the block
     */
    public void remove(W world, int x, int y, int z) {
        set(world, x, y, z, null);
    }

    /**
     * Move the data of a block to another block.
     * The data of the origin block will be remove.
     *
     * @param world The world
     * @param x1    The X position of the origin block
     * @param y1    The Y position of the origin block
     * @param z1    The Z position of the origin block
     * @param x2    The X position of the new block
     * @param y2    The Y position of the new block
     * @param z2    The Z position of the new block
     */
    public void move(W world, int x1, int y1, int z1, int x2, int y2, int z2) {
        move(world, x1, y1, z1, world, x2, y2, z2);
    }

    /**
     * Move the data of a block to another block.
     * The data of the origin block will be remove.
     *
     * @param world1 The world with the origin block
     * @param x1     The X position of the origin block
     * @param y1     The Y position of the origin block
     * @param z1     The Z position of the origin block
     * @param world2 The world with the new block
     * @param x2     The X position of the new block
     * @param y2     The Y position of the new block
     * @param z2     The Z position of the new block
     */
    public void move(W world1, int x1, int y1, int z1, W world2, int x2, int y2, int z2) {
        set(world2, x2, y2, z2, get(world2, x1, y1, z1));
        remove(world1, x1, y1, z1);
    }

    /**
     * Get the base directory to save region data.
     *
     * @return The base directory
     */
    public File getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * Get all the world manager loaded.
     *
     * @return The managers
     */
    public Map<W, WorldRegionDataManager<W>> getAllLoadedWorldManagers() {
        return worldManagers;
    }

    /**
     * Get the region data definition of this manager.
     *
     * @return The region data definition
     */
    public RegionDataDefinition<W> getDefinition() {
        return definition;
    }

    /**
     * Get the logger.
     *
     * @return The logger
     */
    public Logger getLogger() {
        return logger;
    }

}
