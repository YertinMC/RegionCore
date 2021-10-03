package top.yertinmc.regioncore.bukkit;

public class RegionCoreTrivialAutoSaver extends Thread {

    public RegionCoreTrivialAutoSaver() {
        setName("RegionCore::Trivial::AutoSaver");
        setPriority(MAX_PRIORITY);
    }

    @Override
    public void run() {
        RegionCore.TRIVIAL.write();
    }

}
