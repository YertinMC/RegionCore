package top.yertinmc.regioncore.bukkit;

public class RegionCoreTrivialAutoSaver extends Thread {

    public RegionCoreTrivialAutoSaver() {
        setName("RegionCore::Trivial::AutoSaver");
        setPriority(1000);
    }

    @Override
    public void run() {
        RegionCore.TRIVIAL.write();
    }

}
