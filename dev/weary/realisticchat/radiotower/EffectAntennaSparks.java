package dev.weary.realisticchat.radiotower;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectAntennaSparks {
    private JavaPlugin plugin;

    public EffectAntennaSparks(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void playEffect(Location location, int height) {
        World world = location.getWorld();
        Location effectLocation = location.clone().add(0.5, 0.5, 0.5);
        int endY = (int) effectLocation.getY() + height;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (effectLocation.getBlockY() <= endY) {
                    world.spawnParticle(Particle.CRIT, effectLocation, 3, 0.1f, 0.1f, 0.1f, 0.5f, null, true);
                    world.playSound(effectLocation, Sound.ITEM_FLINTANDSTEEL_USE, 0.2f, 1.3f);
                    effectLocation.add(0, 0.5, 0);
                }
                else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
