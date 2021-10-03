package top.yertinmc.regioncore.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * A layer of data.
 */
@SuppressWarnings("unused")
public class LayerData {

    private final ChunkData chunk;
    private final Object[] data;

    public LayerData(ChunkData chunk) {
        this.chunk = chunk;
        this.data = new Object[chunk.getRegion().getManager().getLayerBlockCount()];
    }

    /**
     * Get the index for a block in data array provided by <code>getData</code>
     *
     * @param x The X position of the block
     * @param z The Z position of the block
     * @return The index
     * @see LayerData#getData() ()
     */
    public int getIndex(int x, int z) {
        return Math.abs(x) * chunk.getRegion().getManager().getChunkWidth() + Math.abs(z);
    }

    /**
     * Serialize this layer.
     *
     * @param os The output stream
     */
    public void write(DataOutputStream os) throws IOException {
        for (Object datum : data) {
            if (datum != null) {
                byte[] bytes;
                if (datum instanceof byte[]) {
                    bytes = (byte[]) datum;
                } else {
                    bytes = getChunk().getRegion().getManager().getManager().getDefinition().dataSerializer.apply(datum);
                }
                if (bytes.length > Short.MAX_VALUE)
                    throw new UnsupportedOperationException("Block data too large.");
                os.writeShort(bytes.length);
                os.writeBytes(new String(bytes));
            } else {
                os.writeShort(0);
            }
        }
    }

    /**
     * Is this layer empty?
     *
     * @return TRUE if empty
     */
    public boolean isEmpty() {
        for (Object datum : data) {
            if (datum != null)
                return true;
        }
        return false;
    }

    /**
     * Get a block data in this layer.
     *
     * @param x The X position of the block
     * @param z The Z position of the block
     * @return The data
     */
    public Object getBlock(int x, int z) {
        int index = getIndex(x, z);
        Object datum = data[index];
        if (datum == null)
            return null;
        if (datum instanceof byte[]) {
            data[index] = chunk.getRegion().getManager().getManager().getDefinition().dataDeserializer.apply((byte[]) datum);
            return data[index];
        } else {
            return datum;
        }
    }

    /**
     * Set a block data in this layer.
     *
     * @param x    The X position of the block
     * @param z    The Z position of the block
     * @param data The data
     */
    public void setBlock(int x, int z, Object data) {
        if (data != null && getChunk().getRegion().getManager().getManager().getDefinition().dataIsEmpty.apply(data))
            data = null;
        this.data[getIndex(x, z)] = data;
    }

    /**
     * Deserialize a layer.
     *
     * @param chunk The chunk with this layer
     * @param is    The input stream
     */
    public static LayerData read(ChunkData chunk, DataInputStream is) throws IOException {
        LayerData data = new LayerData(chunk);
        for (int i = 0; i < data.data.length; i++) {
            short size = is.readShort();
            if (size == 0) {
                data.data[i] = null;
            } else {
                byte[] buf = new byte[size];
                if (is.read(buf, 0, size) != size)
                    throw new EOFException("Unexpected end of block data");
                data.data[i] = buf;
            }
        }
        return data;
    }

    /**
     * Get the chunk with this layer.
     *
     * @return The layer
     */
    public @NotNull ChunkData getChunk() {
        return chunk;
    }

    /**
     * Get all data. Value may be deserialized value, a byte array or null.
     *
     * @return The data
     */
    public @Nullable Object[] getData() {
        return data;
    }

}
