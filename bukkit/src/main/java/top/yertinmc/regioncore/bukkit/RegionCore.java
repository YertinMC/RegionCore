package top.yertinmc.regioncore.bukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yertinmc.regioncore.RegionDataDefinition;
import top.yertinmc.regioncore.RegionDataManager;

import java.io.File;

@SuppressWarnings("unused")
public class RegionCore extends JavaPlugin {

    public static final Logger LOGGER = LoggerFactory.getLogger("BlockPlant/RegionData");
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
            .build(), LOGGER, new File("regioncore_trivial").getAbsoluteFile());

}
