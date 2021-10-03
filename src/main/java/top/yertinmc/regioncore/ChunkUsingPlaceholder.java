package top.yertinmc.regioncore;

/**
 * A placeholder object to mark an un-initialized chunk in using.
 */
public final class ChunkUsingPlaceholder {

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static final ChunkUsingPlaceholder INSTANCE = new ChunkUsingPlaceholder();

    private ChunkUsingPlaceholder() {
    }

}
