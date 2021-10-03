package top.yertinmc.regioncore.bukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yertinmc.regioncore.RegionDataDefinition;
import top.yertinmc.regioncore.RegionDataManager;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
public class RegionCore extends JavaPlugin {

    public static final Logger LOGGER_TRIVIAL = LoggerFactory.getLogger("RegionData/Trivial");
    public static final Gson GSON = new GsonBuilder().create();
    @SuppressWarnings({"Convert2MethodRef", "SpellCheckingInspection"})
    public static final RegionDataManager<World> TRIVIAL = new RegionDataManager<World>(new RegionDataDefinition.Builder<World>()
            .regionSize(64)
            .chunkWidth(16)
            .defaultWorldHeight(256)
            .worldName((world) -> world.getName()) // for 1.12.2 compat
            .worldEquals((w1, w2) -> w1.getUID().equals(w2.getUID()))
            .dataSerializer((data) -> GSON.toJson(data).getBytes())
            .dataDeserializer((data) -> GSON.fromJson(new String(data), JsonObject.class))
            .dataIsEmpty((data) -> ((JsonObject) data).size() == 0)
            .build(), LOGGER_TRIVIAL, new File("regioncore_trivial").getAbsoluteFile());

    static {
        Runtime.getRuntime().addShutdownHook(new RegionCoreTrivialAutoSaver());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        int time = getConfig().getInt("trivial_auto_save_period", 60000);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, TRIVIAL::write, time, time);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        TRIVIAL.write();
    }

    public static void onBlockRemove(BlockEvent event) {
        onBlockRemove(event.getBlock());
    }

    public static void onBlockRemove(Block block) {
        TRIVIAL.remove(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    public static void onBlockMove(World world, Location oldLocation, Location newLocation) {
        TRIVIAL.move(world, oldLocation.getBlockX(), oldLocation.getBlockY(), oldLocation.getBlockZ(),
                newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ());
    }

    public static void onBlockPiston(BlockPistonEvent event, List<Block> blocks) {
        Vector offsetToNewBlock = event.getDirection().getDirection().multiply(-1);
        for (Block block : blocks) {
            onBlockMove(block.getWorld(), block.getLocation(), block.getLocation().add(offsetToNewBlock));
        }
    }

    public static class EventListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChunkLoad(ChunkLoadEvent event) {
            TRIVIAL.loadChunk(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChunkUnload(ChunkUnloadEvent event) {
            TRIVIAL.unloadChunk(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBlockBreak(BlockBreakEvent event) {
            onBlockRemove(event);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBlockBurn(BlockBurnEvent event) {
            onBlockRemove(event);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBlockExplode(BlockExplodeEvent event) {
            onBlockRemove(event);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBlockFade(BlockFadeEvent event) {
            onBlockRemove(event);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onEntityChangeBlock(EntityChangeBlockEvent event) {
            onBlockRemove(event.getBlock());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPistonExtend(BlockPistonExtendEvent event) {
            onBlockPiston(event, event.getBlocks());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPistonRetract(BlockPistonRetractEvent event) {
            onBlockPiston(event, event.getBlocks());
        }

    }

}
