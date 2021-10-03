package top.yertinmc.regioncore;

import top.yertinmc.regioncore.io.ChunkData;
import top.yertinmc.regioncore.io.RegionData;

import java.io.*;

/**
 * A loaded region data.
 *
 * @param <W> The type of <code>World</code>
 */
@SuppressWarnings("unused")
public class LoadedRegionData<W> {

    private final WorldRegionDataManager<W> manager;
    private final File file;
    private final RegionData data;
    private final W world;
    private final int x;
    private final int z;
    private boolean dirty = false;

    public LoadedRegionData(WorldRegionDataManager<W> manager, W world, int x, int z) {
        this.manager = manager;
        this.file = new File(new File(manager.getBaseDirectory(), "r".concat(Integer.toString(x))),
                "r".concat(Integer.toString(z)).concat(manager.getManager().getDefinition().fileSuffix));
        this.world = world;
        this.x = x;
        this.z = z;
        if (file.exists()) {
            try (DataInputStream is = new DataInputStream(new FileInputStream(file))) {
                this.data = RegionData.read(manager, is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            data = new RegionData(manager);
            dirty = true;
        }
    }

    /**
     * Save this region data.
     */
    public void write() {
        if (dirty) {
            try {
                if (!file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.getAbsoluteFile().getParentFile().mkdirs();
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                }
                try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
                    data.write(os);
                }
                dirty = false;
            } catch (IOException | AssertionError e) {
                manager.getManager().getLogger().error("Error saving loaded region data at {},{},{} to {}", world, x, z, file);
                System.err.println("Error saving loaded region data at " + world
                        + ", " + x + ", " + z + " to " + file);
                e.printStackTrace();
                File backupFile = new File(file + ".backup");
                try {
                    assert backupFile.exists() || backupFile.createNewFile();
                    try (DataOutputStream os = new DataOutputStream(new FileOutputStream(backupFile))) {
                        data.write(os);
                    }
                } catch (IOException e1) {
                    manager.getManager().getLogger().error("Error saving backup region data at {},{},{} to {}", world, x, z, backupFile);
                    System.err.println("Error saving backup region data at " + world
                            + ", " + x + ", " + z + " to " + backupFile);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Is this region using?
     *
     * @return TRUE if using
     */
    public boolean isUsing() {
        for (Object chunk : getData().getChunks()) {
            if (chunk instanceof ChunkUsingPlaceholder) {
                return true;
            } else if ((chunk instanceof ChunkData) && ((ChunkData) chunk).isUsing()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mark a chunk in using with setting placeholder.
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     */
    public void setChunkUsingPlaceholder(int x, int z) {
        data.getChunks()[data.getIndex(x, z)] = ChunkUsingPlaceholder.INSTANCE;
    }

    /**
     * Mark a chunk not in using with setting null.
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     */
    public void resetChunkUsingPlaceholder(int x, int z) {
        data.getChunks()[data.getIndex(x, z)] = null;
    }

    /**
     * Mark this region changed.
     *
     * @see LoadedRegionData#isDirty()
     * @see LoadedRegionData#resetDirty()
     */
    public void markDirty() {
        dirty = true;
    }

    /**
     * Mark this region not changed.
     *
     * @see LoadedRegionData#isDirty()
     * @see LoadedRegionData#markDirty()
     */
    public void resetDirty() {
        dirty = false;
    }

    /**
     * Get the world region data manager.
     *
     * @return The manager
     */
    public WorldRegionDataManager<W> getManager() {
        return manager;
    }

    /**
     * Get the file of this region.
     *
     * @return The file
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the region data.
     *
     * @return The data
     */
    public RegionData getData() {
        return data;
    }

    /**
     * Get the world with this region.
     *
     * @return The world
     */
    public W getWorld() {
        return world;
    }

    /**
     * Get the X position of the region.
     *
     * @return The X position
     */
    public int getX() {
        return x;
    }

    /**
     * Get the Z position of the region.
     *
     * @return The Z position
     */
    public int getZ() {
        return z;
    }

    /**
     * Is this region changed?
     *
     * @return TRUE if changed
     * @see LoadedRegionData#markDirty()
     * @see LoadedRegionData#resetDirty()
     */
    public boolean isDirty() {
        return dirty;
    }

}
