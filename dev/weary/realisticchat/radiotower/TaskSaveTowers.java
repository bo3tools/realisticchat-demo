package dev.weary.realisticchat.radiotower;

import dev.weary.module.StorageFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;

public class TaskSaveTowers extends BukkitRunnable {
    private RadioTowerManager towerManager;
    private StorageFile storageFile;

    public TaskSaveTowers(RadioTowerManager towerManager, StorageFile storageFile) {
        this.towerManager = towerManager;
        this.storageFile = storageFile;
    }

    @Override
    public void run() {
        try {
            BufferedWriter writer = storageFile.getWriter();
            for (Location location : this.towerManager.getTowerLocations()) {
                String line = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "\n";
                writer.write(line);
            }
            writer.close();
        }
        catch (Exception e) {
            Bukkit.getLogger().severe("Cannot save tower locations to " + storageFile.getServerFileLocation());
            e.printStackTrace();
        }

        // Bukkit.getServer().broadcastMessage("All towers saved!");
    }
}
