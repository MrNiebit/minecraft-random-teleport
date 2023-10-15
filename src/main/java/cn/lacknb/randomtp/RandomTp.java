package cn.lacknb.randomtp;

import cn.lacknb.randomtp.listen.RandomTpListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class RandomTp extends JavaPlugin {

    private static final Logger log = Logger.getLogger("RandomTp");

    @Override
    public void onEnable() {
        // Plugin startup logic
        RandomTpListener tpListener = new RandomTpListener();
        Objects.requireNonNull(getCommand("giveTp")).setExecutor(tpListener);
        log.info("random tp plugin is loading !");
        getServer().getPluginManager().registerEvents(tpListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
