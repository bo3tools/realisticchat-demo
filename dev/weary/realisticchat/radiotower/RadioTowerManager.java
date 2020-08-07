package dev.weary.realisticchat.radiotower;

import dev.weary.realisticchat.RealisticChatSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RadioTowerManager implements Listener {
    private Map<Location, RadioTower> radioTowers = new ConcurrentHashMap<>();
    private JavaPlugin plugin;

    public static EffectAntennaSparks EFFECT_ANTENNA_SPARKS;
    public static Material BASE_MATERIAL;
    public static Material ANTENNA_MATERIAL;
    public static int ANTENNA_MIN_HEIGHT;
    public static int ANTENNA_MAX_HEIGHT;
    public static int CHECK_ANTENNA_HEIGHT = 10;

    public RadioTowerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.updateConfig();
    }

    public void updateConfig() {
        BASE_MATERIAL = RealisticChatSettings.BASE_MATERIAL.asMaterial();
        ANTENNA_MATERIAL = RealisticChatSettings.ANTENNA_MATERIAL.asMaterial();
        ANTENNA_MIN_HEIGHT = RealisticChatSettings.ANTENNA_MIN_HEIGHT.asInteger();
        ANTENNA_MAX_HEIGHT = RealisticChatSettings.ANTENNA_MAX_HEIGHT.asInteger();
        EFFECT_ANTENNA_SPARKS = new EffectAntennaSparks(this.plugin);
    }

    public void tryRegisterTower(Location location) {
        if (this.radioTowers.containsKey(location)) {
            Bukkit.getServer().broadcastMessage("Tower already exists, won't register");
            return;
        }

        if (RadioTower.canBecomeTower(location)) {
            Bukkit.getServer().broadcastMessage("Valid tower, registering");
            this.radioTowers.put(location, new RadioTower(location));
        }
    }

    public Set<Location> getTowerLocations() {
        return this.radioTowers.keySet();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Location location = block.getLocation();

        // It's not possible to put the base last because it won't have any
        // required components attached to it (e.g redstone torches), so
        // we're not checking for it.

        if (block.getType() == ANTENNA_MATERIAL) {
            for (int i = 0; i < CHECK_ANTENNA_HEIGHT; i++) {
                location.add(0, -1, 0);
                if (!location.getBlock().getType().equals(ANTENNA_MATERIAL)) {
                    break;
                }
            }

            if (location.getBlock().getType().equals(BASE_MATERIAL)) {
                this.tryRegisterTower(location);
            }
        }

        else if (block.getType() == Material.REDSTONE_WALL_TORCH) {
            RedstoneWallTorch torchData = (RedstoneWallTorch) block.getBlockData();
            location.add(torchData.getFacing().getOppositeFace().getDirection());

            if (location.getBlock().getType().equals(BASE_MATERIAL)) {
                this.tryRegisterTower(location);
            }
        }

        else if (block.getType() == Material.LEVER) {
            Switch leverData = (Switch) block.getBlockData();
            location.add(leverData.getFacing().getOppositeFace().getDirection());

            if (location.getBlock().getType().equals(BASE_MATERIAL)) {
                this.tryRegisterTower(location);
            }
        }

        else if (isWallSign(block.getType())) {
            WallSign signData = (WallSign) block.getBlockData();
            location.add(signData.getFacing().getOppositeFace().getDirection());

            if (location.getBlock().getType().equals(BASE_MATERIAL)) {
                this.tryRegisterTower(location);
            }
        }
    }

    @EventHandler
    public void onRedstonePowerChange(BlockRedstoneEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE_WALL_TORCH) {
            RedstoneWallTorch torchData = (RedstoneWallTorch) event.getBlock().getBlockData();
            Location mountLocation = event.getBlock().getLocation().add(torchData.getFacing().getOppositeFace().getDirection());
            if (mountLocation.getBlock().getType() == BASE_MATERIAL) {
                if (this.radioTowers.containsKey(mountLocation)) {
                    updateTowerAndRemoveIfDestroyed(this.radioTowers.get(mountLocation), TowerUpdateCause.REDSTONE_CHANGE);
                }
                else {
                    tryRegisterTower(mountLocation);
                }
            }
        }
    }

    public static boolean isWallSign(Material material) {
        switch (material) {
            case OAK_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case ACACIA_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case WARPED_WALL_SIGN:
            case CRIMSON_WALL_SIGN:
                return true;
            default:
                return false;
        }
    }

    private void updateTowerAndRemoveIfDestroyed(RadioTower radioTower, TowerUpdateCause updateCause) {
        if (!radioTower.updateTower(updateCause)) {
            this.radioTowers.remove(radioTower.getLocation());
        }
    }

    public void updateTowers() {
        for (RadioTower radioTower: this.radioTowers.values()) {
            updateTowerAndRemoveIfDestroyed(radioTower, TowerUpdateCause.GLOBAL_TICK);
        }
    }
}