package dev.weary.realisticchat;

import dev.weary.module.Module;
import dev.weary.module.StorageFile;
import dev.weary.realisticchat.radiotower.RadioTowerManager;
import dev.weary.realisticchat.radiotower.TaskSaveTowers;
import dev.weary.realisticchat.radiotower.TaskUpdateTowers;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.weary.realisticchat.RealisticChatSettings.*;

public class RealisticChatModule extends Module implements Listener {

    public static RealisticChatModule instance;


    public RealisticChatModule() {
        super("RealisticChat", "Manages player chat and adds new mechanics", "chat.yml");
        instance = this;
    }

    private JavaPlugin pluginInstance;
    private RadioTowerManager towerManager;
    private StorageFile towersFile;
    private BukkitTask towerUpdateTask;

    @Override
    protected boolean loadModule(JavaPlugin plugin) {
        this.initializeDefaultConfig(RealisticChatSettings.values());

        this.pluginInstance = plugin;
        this.towerManager = new RadioTowerManager(plugin);
        this.towersFile = new StorageFile("towers.csv");

        List<Location> towerLocations = loadTowerLocations();
        System.out.println("Loaded " + towerLocations.size() + " tower locations, registering...");

        for (Location location: towerLocations) {
            this.towerManager.tryRegisterTower(location);
        }

        System.out.println("All towers registered!");

        this.rescheduleTowerUpdateTask();
        this.registerEvents(this);
        this.registerEvents(this.towerManager);

        return true;
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        this.saveTowersAsync();
    }

    private void rescheduleTowerUpdateTask() {
        if (this.towerUpdateTask != null) {
            this.towerUpdateTask.cancel();
        }

        if (this.towerManager != null) {
            this.towerUpdateTask = new TaskUpdateTowers(this.towerManager).runTaskTimer(this.pluginInstance, 0, UPDATE_TICKS.asInteger());
        }
    }

    private void updateConfig() {
        if (this.towerManager != null) {
            this.towerManager.updateConfig();
        }
    }

    private List<Location> loadTowerLocations() {
        List<Location> towerLocations = new ArrayList<>();
        BufferedReader reader = this.towersFile.getReader();

        try {
            int lineNumber = 1;

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (line.charAt(0) == '#') {
                    continue;
                }

                String[] values = line.split(",");
                if (values.length != 4) {
                    Bukkit.getLogger().warning("Expected 4 values on line " + lineNumber + " in " + towersFile.getServerFileLocation());
                    continue;
                }

                World world = Bukkit.getServer().getWorld(values[0]);
                int blockX = Integer.parseInt(values[1]);
                int blockY = Integer.parseInt(values[2]);
                int blockZ = Integer.parseInt(values[3]);

                try {
                    Location location = new Location(world, blockX, blockY, blockZ);
                    towerLocations.add(location);
                }
                catch (Exception e) {
                    Bukkit.getLogger().warning("Cannot parse location on line " + lineNumber + " in " + towersFile.getServerFileLocation());
                    e.printStackTrace();
                }

                lineNumber++;
            }
        }
        catch (Exception e) {
            Bukkit.getLogger().severe("Cannot load tower locations from " + towersFile.getServerFileLocation());
            e.printStackTrace();
        }

        try {
            reader.close();
        }
        catch (Exception ignored) {}

        return towerLocations;
    }

    public TaskSaveTowers createSaveTask() {
        return new TaskSaveTowers(this.towerManager, this.towersFile);
    }

    public void saveTowersAsync() {
        CompletableFuture.runAsync(createSaveTask());
    }

    @Override
    protected void onConfigLoaded() {
        this.rescheduleTowerUpdateTask();
        this.updateConfig();
    }

    @Override
    protected void unloadModule() {
        this.saveTowersAsync();
    }
}
