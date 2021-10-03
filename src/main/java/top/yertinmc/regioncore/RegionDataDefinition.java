package top.yertinmc.regioncore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A type of region data.
 *
 * @param <W> The type of <code>World</code>
 */
public class RegionDataDefinition<W> {

    /**
     * The size of a region in chunks.
     */
    public final int regionSize;

    /**
     * The width and height of a chunk.
     */
    public final int chunkWidth;

    /**
     * The height for worlds not defined in <code>worldHeights</code>.
     */
    public final int defaultWorldHeight;

    /**
     * Some worlds with special heights.
     */
    public final @NotNull Map<W, Integer> worldHeights;

    /**
     * The getter of world name.
     */
    public final @NotNull Function<W, String> worldNameProvider;

    /**
     * The compare function of worlds.
     */
    public final @NotNull BiFunction<W, W, Boolean> worldEquals;

    /**
     * The serializer for a block data.
     */
    public final @NotNull Function<Object, byte[]> dataSerializer;

    /**
     * The deserializer for a block data.
     */
    public final @NotNull Function<byte[], Object> dataDeserializer;

    /**
     * Is a data empty?
     */
    public final @NotNull Function<Object, Boolean> dataIsEmpty;

    /**
     * The suffix of data files. Usually starts with <code>.</code>
     */
    public final @NotNull String fileSuffix;

    public RegionDataDefinition(int regionSize, int chunkWidth, int defaultWorldHeight,
                                @NotNull Map<W, Integer> worldHeights, @NotNull Function<W, String> worldNameProvider,
                                @NotNull BiFunction<W, W, Boolean> worldEquals, @NotNull Function<Object, byte[]> dataSerializer,
                                @NotNull Function<byte[], Object> dataDeserializer, @NotNull Function<Object, Boolean> dataIsEmpty,
                                @NotNull String fileSuffix) {
        this.regionSize = regionSize;
        this.chunkWidth = chunkWidth;
        this.defaultWorldHeight = defaultWorldHeight;
        this.worldHeights = worldHeights;
        this.worldNameProvider = worldNameProvider;
        this.worldEquals = worldEquals;
        this.dataSerializer = dataSerializer;
        this.dataDeserializer = dataDeserializer;
        this.dataIsEmpty = dataIsEmpty;
        this.fileSuffix = fileSuffix;
    }

    /**
     * Get the world height for a world.
     * If the given world has been defined in <code>worldHeights</code>, the value in <code>worldHeights</code> will be
     * returned. Else, <code>defaultWorldHeight</code> will be returned.
     *
     * @param world The world
     * @return The height
     * @see RegionDataDefinition#defaultWorldHeight
     * @see RegionDataDefinition#worldHeights
     */
    public int getWorldHeight(W world) {
        if (worldHeights.containsKey(world))
            return worldHeights.get(world);
        return defaultWorldHeight;
    }

    @SuppressWarnings("unused")
    public static class Builder<W> {

        private int regionSize = 32;
        private int chunkWidth = 16;
        private int defaultWorldHeight = 256;
        private Map<W, Integer> worldHeights = new HashMap<>();
        private Function<W, String> worldNameProvider = Objects::toString;
        private BiFunction<W, W, Boolean> worldEquals = Objects::equals;
        private Function<Object, byte[]> dataSerializer = (data) -> (byte[]) data;
        private Function<byte[], Object> dataDeserializer = (data) -> data;
        private Function<Object, Boolean> dataIsEmpty = (data) -> false;
        private String fileSuffix = ".dat";

        public RegionDataDefinition<W> build() {
            return new RegionDataDefinition<>(regionSize, chunkWidth, defaultWorldHeight, worldHeights,
                    worldNameProvider, worldEquals, dataSerializer, dataDeserializer, dataIsEmpty, fileSuffix);
        }

        public Builder<W> regionSize(int regionSize) {
            this.regionSize = regionSize;
            return this;
        }

        public Builder<W> chunkWidth(int chunkWidth) {
            this.chunkWidth = chunkWidth;
            return this;
        }

        public Builder<W> defaultWorldHeight(int defaultWorldHeight) {
            this.defaultWorldHeight = defaultWorldHeight;
            return this;
        }

        public Builder<W> worldHeight(W world, int height) {
            this.worldHeights.put(world, height);
            return this;
        }

        public Builder<W> worldHeights(Map<W, Integer> worldHeights) {
            this.worldHeights = worldHeights;
            return this;
        }

        public Builder<W> worldName(Function<W, String> worldNameProvider) {
            this.worldNameProvider = worldNameProvider;
            return this;
        }

        public Builder<W> worldEquals(BiFunction<W, W, Boolean> worldEquals) {
            this.worldEquals = worldEquals;
            return this;
        }

        public Builder<W> dataSerializer(Function<Object, byte[]> dataSerializer) {
            this.dataSerializer = dataSerializer;
            return this;
        }

        public Builder<W> dataDeserializer(Function<byte[], Object> dataDeserializer) {
            this.dataDeserializer = dataDeserializer;
            return this;
        }

        public Builder<W> dataIsEmpty(Function<Object, Boolean> dataIsEmpty) {
            this.dataIsEmpty = dataIsEmpty;
            return this;
        }

        public Builder<W> fileSuffix(String fileSuffix) {
            this.fileSuffix = fileSuffix;
            return this;
        }

    }

}
