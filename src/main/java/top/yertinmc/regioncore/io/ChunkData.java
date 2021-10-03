package top.yertinmc.regioncore.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The chunk data. A set of <code>LayerData</code>
 *
 * @see LayerData
 */
@SuppressWarnings("unused")
public class ChunkData {

    private @NotNull
    final RegionData region;
    private @Nullable
    final LayerData[] layers;
    private boolean using;

    public ChunkData(RegionData region) {
        this.region = region;
        layers = new LayerData[region.getManager().getHeight()];
    }

    /**
     * Is this chunk no data?
     *
     * @return TRUE if empty
     */
    public boolean isEmpty() {
        for (LayerData layer : layers) {
            if (layer != null && layer.isEmpty())
                return true;
        }
        return false;
    }

    /**
     * Serialize this chunk.
     *
     * @param os The output stream
     */
    public void write(DataOutputStream os) throws IOException {
        for (int i = 0; i < layers.length; i++) {
            LayerData layer = layers[i];
            if (layer != null && layer.isEmpty()) {
                os.writeBoolean(true);
                layer.write(os);
            } else {
                if (layer != null) // Empty
                    layers[i] = null;
                os.writeBoolean(false);
            }
        }
    }

    /**
     * Deserialize a chunk.
     *
     * @param region The region with the chunk
     * @param is     The input stream
     */
    public static ChunkData read(RegionData region, DataInputStream is) throws IOException {
        ChunkData data = new ChunkData(region);
        for (int i = 0; i < data.layers.length; i++) {
            if (is.readBoolean()) {
                data.layers[i] = LayerData.read(data, is);
            } else {
                data.layers[i] = null;
            }
        }
        return data;
    }

    /**
     * Get a layer in this chunk.
     *
     * @return The layer
     */
    public @Nullable LayerData getLayer(int y) {
        return layers[y];
    }

    /**
     * Get an exists layer or initialize a new layer in this chunk.
     *
     * @return The layer
     */
    public LayerData getOrInitLayer(int y) {
        LayerData layer = getLayer(y);
        if (layer == null)
            layer = layers[y] = new LayerData(this);
        return layer;
    }

    /**
     * Mark this chunk in using.
     *
     * @see ChunkData#isUsing
     * @see ChunkData#resetUsing
     */
    public void markUsing() {
        using = true;
    }

    /**
     * Mark this chunk not in using.
     *
     * @see ChunkData#isUsing
     * @see ChunkData#markUsing
     */
    public void resetUsing() {
        using = false;
    }

    /**
     * Get the region with this chunk.
     *
     * @return The region
     */
    public @NotNull RegionData getRegion() {
        return region;
    }

    /**
     * Get the layers in this chunk.
     *
     * @return The layers
     */
    public @Nullable LayerData[] getLayers() {
        return layers;
    }

    /**
     * Is this chunk in using?
     *
     * @return TRUE if using
     * @see ChunkData#markUsing
     * @see ChunkData#resetUsing
     */
    public boolean isUsing() {
        return using;
    }

}
