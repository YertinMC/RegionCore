package top.yertinmc.regioncore;

import org.jetbrains.annotations.Nullable;
import top.yertinmc.regioncore.io.ChunkData;
import top.yertinmc.regioncore.io.LayerData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A region data world manager.
 *
 * @param <W> The type of <code>World</code>
 */
@SuppressWarnings("unused")
public class WorldRegionDataManager<W> {

    private final RegionDataManager<W> manager;
    private final File baseDirectory;
    private final Map<Integer, Map<Integer, LoadedRegionData<W>>> loadedRegions = new HashMap<>();
    private final W world;

    public WorldRegionDataManager(RegionDataManager<W> manager, W world) {
        this.manager = manager;
        this.world = world; // set first for getWorldName() calling
        this.baseDirectory = new File(manager.getBaseDirectory(), getWorldName());
        assert baseDirectory.mkdir();
    }

    /**
     * Get the name of this world.
     *
     * @return The name
     * @see RegionDataDefinition#worldNameProvider
     */
    public String getWorldName() {
        return manager.getDefinition().worldNameProvider.apply(world);
    }

    /**
     * Load a region if this region not loaded.
     *
     * @param x The X position of the region
     * @param z The Z position of the region
     */
    public void loadRegion(int x, int z) {
        synchronized (this) {
            if (isRegionLoaded(x, z))
                return;
            if (!loadedRegions.containsKey(x))
                loadedRegions.put(x, new HashMap<>());
            loadedRegions.get(x).put(z, new LoadedRegionData<>(this, world, x, z));
        }
    }

    /**
     * Unload a region if this region loaded.
     *
     * @param x The X position of the region
     * @param z The Z position of the region
     */
    public void unloadRegion(int x, int z) {
        synchronized (this) {
            if (!isRegionLoaded(x, z))
                return;
            Map<Integer, LoadedRegionData<W>> xRegions = loadedRegions.get(x);
            xRegions.get(z).write();
            xRegions.remove(z);
            if (xRegions.isEmpty())
                loadedRegions.remove(x);
        }
    }

    /**
     * Is a region loaded?
     *
     * @param x The X position of the region
     * @param z The Z position of the region
     */
    public boolean isRegionLoaded(int x, int z) {
        synchronized (this) {
            if (!loadedRegions.containsKey(x))
                return false;
            return loadedRegions.get(x).containsKey(z);
        }
    }

    /**
     * Get the loaded region instance.
     *
     * @param x The X position of the region
     * @param z The Z position of the region
     * @return The region instance
     */
    public LoadedRegionData<W> getRegion(int x, int z) {
        Map<Integer, LoadedRegionData<W>> xMap = loadedRegions.get(x);
        if (xMap == null)
            return null;
        return xMap.get(z);
    }

    /**
     * Load a chunk if the region with the chunk not loaded.
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     */
    public void loadChunk(int x, int z) {
        synchronized (this) {
            final int regionSize = manager.getDefinition().regionSize;
            int regionX = x / regionSize;
            int regionZ = z / regionSize;
            loadRegion(regionX, regionZ);
            Object chunk = getChunk(x, z);
            if (chunk instanceof ChunkData) {
                ((ChunkData) chunk).markUsing();
            } else {
                getRegion(regionX, regionZ).setChunkUsingPlaceholder(x % regionSize, z % regionSize);
            }
        }
    }

    /**
     * Unload a chunk and unload the region if no using chunks in the region.
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     */
    public void unloadChunk(int x, int z) {
        synchronized (this) {
            final int regionSize = manager.getDefinition().regionSize;
            int regionX = x / regionSize;
            int regionZ = z / regionSize;
            if (!isRegionLoaded(regionX, regionZ))
                return;
            LoadedRegionData<W> region = getRegion(regionX, regionZ);
            Object chunk = getChunk(x, z);
            if (chunk instanceof ChunkData) {
                ((ChunkData) chunk).resetUsing();
            } else {
                region.resetChunkUsingPlaceholder(x % regionSize, z % regionSize);
            }
            if (!region.isUsing())
                unloadRegion(regionX, regionZ);
        }
    }

    /**
     * Is a region with a chunk loaded?
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     * @return TRUE if loaded.
     */
    public boolean isChunkLoaded(int x, int z) {
        final int regionSize = manager.getDefinition().regionSize;
        return isRegionLoaded(x / regionSize, z / regionSize);
    }

    /**
     * Is a chunk in using?
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     * @return TRUE if loaded.
     */
    public boolean isChunkUsing(int x, int z) {
        return isChunkLoaded(x, z) && (getChunk(x, z) instanceof ChunkUsingPlaceholder
                || ((ChunkData) getChunk(x, z)).isUsing());
    }

    /**
     * Get a chunk with given chunk position.
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     * @return The chunk data or UsingChunkPlaceholder
     */
    public Object getChunk(int x, int z) {
        final int regionSize = manager.getDefinition().regionSize;
        LoadedRegionData<W> region = getRegion(x / regionSize, z / regionSize);
        if (region == null)
            return null;
        return region.getData().getChunkData(
                x % regionSize, z % regionSize);
    }

    /**
     * Get a chunk with given block position.
     *
     * @param x The X position of the block
     * @param z The Z position of the block
     * @return The chunk data
     */
    public ChunkData getBlockChunk(int x, int z) {
        final int chunkWidth = getChunkWidth();
        Object object = getChunk(x / chunkWidth, z / chunkWidth);
        if (object instanceof ChunkData) {
            return (ChunkData) object;
        } else {
            return null;
        }
    }

    /**
     * Save all regions loaded.
     */
    public void write() {
        synchronized (this) {
            for (Map<Integer, LoadedRegionData<W>> yMap : loadedRegions.values()) {
                for (LoadedRegionData<W> regionData : yMap.values()) {
                    regionData.write();
                }
            }
        }
    }

    /**
     * Get the data of a block
     *
     * @param x The X position of the block
     * @param y The Y position of the block
     * @param z The Z position of the block
     * @return The data of the required block
     */
    public Object get(int x, int y, int z) {
        synchronized (this) {
            final int chunkWidth = getChunkWidth();
            ChunkData chunk = getBlockChunk(x, z);
            if (chunk == null) // Chunk not loaded
                return null;
            LayerData layer = chunk.getLayer(y);
            if (layer == null) // Empty layer
                return null;
            return layer.getBlock(x % chunkWidth, z % chunkWidth);
        }
    }

    /**
     * Set the data of a block
     *
     * @param x    The X position of the block
     * @param y    The Y position of the block
     * @param z    The Z position of the block
     * @param data The data to set
     */
    public void set(int x, int y, int z, Object data) {
        synchronized (this) {
            final int regionSize = manager.getDefinition().regionSize;
            final int chunkWidth = getChunkWidth();
            LoadedRegionData<W> region = getRegion(x / chunkWidth / regionSize,
                    z / chunkWidth / regionSize);
            if (region == null) // Check region loaded
                throw new IllegalStateException("Region not loaded for block pos " + x + ", " + y + ", " + z + " but trying to set.");
            region.markDirty(); // Mark dirty
            int regionOffsetX = x % regionSize;
            int regionOffsetZ = z % regionSize;
            int chunkPosX = regionOffsetX / chunkWidth;
            int chunkPosZ = regionOffsetZ / chunkWidth;
            int chunkOffsetX = regionOffsetX % chunkWidth;
            int chunkOffsetZ = regionOffsetZ % chunkWidth;
            @Nullable Object chunkData = data == null ? region.getData().getChunkData(chunkPosX, chunkPosZ) :
                    region.getData().getOrInitChunkData(chunkPosX, chunkPosZ);
            if (chunkData instanceof ChunkData) {
                ChunkData chunk = (ChunkData) chunkData;
                @Nullable LayerData layer = data == null ? chunk.getLayer(y) : chunk.getOrInitLayer(y);
                if (layer != null)
                    layer.setBlock(chunkOffsetX, chunkOffsetZ, data);
            }
        }
    }

    /**
     * Get the height of this world.
     *
     * @return The height
     * @see RegionDataDefinition#getWorldHeight(Object)
     */
    public int getHeight() {
        return getManager().getDefinition().getWorldHeight(world);
    }

    /**
     * Get the width of a chunk.
     *
     * @return The width
     * @see RegionDataDefinition#chunkWidth
     */
    public int getChunkWidth() {
        return getManager().getDefinition().chunkWidth;
    }

    /**
     * How many blocks in a layer?
     *
     * @return The count
     */
    public int getLayerBlockCount() {
        int width = getChunkWidth();
        return width * width;
    }

    /**
     * Get the root region data manager.
     *
     * @return The manager
     */
    public RegionDataManager<W> getManager() {
        return manager;
    }

    /**
     * Get the base directory to save files for this manager.
     *
     * @return The directory
     */
    public File getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * Get all loaded regions.
     *
     * @return Loaded regions, <code>Map(X pos, Map(Y pos, Data))</code>
     */
    public Map<Integer, Map<Integer, LoadedRegionData<W>>> getLoadedRegions() {
        return loadedRegions;
    }

    /**
     * Get the world for this manager.
     *
     * @return The world
     */
    public W getWorld() {
        return world;
    }

}
