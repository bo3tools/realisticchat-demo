package dev.weary.realisticchat.radiotower;

import org.bukkit.scheduler.BukkitRunnable;

public class TaskUpdateTowers extends BukkitRunnable {
    private RadioTowerManager towerManager;

    public TaskUpdateTowers(RadioTowerManager towerManager) {
        this.towerManager = towerManager;
    }

    @Override
    public void run() {
        this.towerManager.updateTowers();
    }
}
