package top.yertinmc.regioncore.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.yertinmc.regioncore.ChunkUsingPlaceholder;
import top.yertinmc.regioncore.WorldRegionDataManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A region. A set of <code>ChunkData</code>.
 *
 * @see ChunkData
 */
public class RegionData {

    private final @NotNull WorldRegionDataManager<?> manager;
    private final @Nullable Object[] chunks;

    public RegionData(@NotNull WorldRegionDataManager<?> manager) {
        this.manager = manager;
        final int regionSize = manager.getManager().getDefinition().regionSize;
        this.chunks = new Object[regionSize * regionSize];
    }

    /**
     * Get the index for a chunk in chunk array provided by <code>getChunks</code>
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     * @return The index
     * @see RegionData#getChunks()
     */
    public int getIndex(int x, int z) {
        return Math.abs(x) * (manager.getManager().getDefinition().regionSize) + Math.abs(z);
    }

    /**
     * Serialize this region.
     *
     * @param os The output stream
     */
    public void write(DataOutputStream os) throws IOException {
        for (int i = 0; i < chunks.length; i++) {
            Object chunk = chunks[i];
            if (!(chunk instanceof ChunkData) || !((ChunkData) chunk).isEmpty()) {
                os.writeBoolean(false);
                if (chunk instanceof ChunkData) // Empty
                    chunks[i] = ((ChunkData) chunk).isUsing() ? ChunkUsingPlaceholder.INSTANCE : null;
            } else {
                os.writeBoolean(true);
                ((ChunkData) chunk).write(os);
            }
        }
    }

    /**
     * Deserialize a region.
     *
     * @param manager The world region data manager
     * @param is      The input stream
     */
    public static RegionData read(WorldRegionDataManager<?> manager, DataInputStream is) throws IOException {
        RegionData data = new RegionData(manager);
        for (int i = 0; i < data.chunks.length; i++) {
            if (is.readBoolean()) {
                data.chunks[i] = ChunkData.read(data, is);
            } else {
                data.chunks[i] = null;
            }
        }
        return data;
    }

    /**
     * Get or initialize the chunk data with given chunk position.
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     * @return The chunk data
     */
    public ChunkData getOrInitChunkData(int x, int z) {
        Object data = getChunkData(x, z);
        if (data == null || data instanceof ChunkUsingPlaceholder) {
            boolean isUsing = data != null;
            data = chunks[getIndex(x, z)] = new ChunkData(this);
            if (isUsing)
                ((ChunkData) data).markUsing();
        }
        return (ChunkData) data;
    }

    /**
     * Get the chunk data with given chunk position.
     *
     * @param x The X position of the chunk
     * @param z The Z position of the chunk
     * @return The chunk data or UsingChunkPlaceholder
     */
    public @Nullable Object getChunkData(int x, int z) {
        return chunks[getIndex(x, z)];
    }

    /**
     * Get the world region data manager.
     *
     * @return The manager
     */
    public @NotNull WorldRegionDataManager<?> getManager() {
        return manager;
    }

    /**
     * Get all the chunks in this region, un-initialized chunks will be null.
     *
     * @return The chunks or UsingChunkPlaceholder
     */
    public @Nullable Object[] getChunks() {
        return chunks;
    }

}
