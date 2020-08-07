package dev.weary;

import dev.weary.module.Module;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MinemanPlugin extends JavaPlugin implements Listener {

    public static MinemanPlugin instance;

    public MinemanPlugin() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Module.loadModules(this);
    }

    @Override
    public void onDisable() {
        Module.unloadModules();
    }
}
