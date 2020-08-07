package dev.weary.realisticchat.radiotower;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.time.LocalTime;

public class RadioTower {
    private final static String STATUS_NO_SIGNAL = ChatColor.DARK_GRAY + "           No Signal";
    private final static String STATUS_ENABLED = ChatColor.DARK_GREEN + "                   On";
    private final static String STATUS_DISABLED = ChatColor.DARK_GRAY + "                  Off";
    private final static String STATUS_BROKEN = ChatColor.DARK_RED + "             Broken";

    private final Location location;
    private final Block redstoneTorchBlock;
    private final Block leverBlock;
    private final Block signBlock;
    private final Block baseBlock;
    private boolean wasEnabled = false;

    public RadioTower(Location location) {
        this.location = location;

        TowerState towerState = RadioTowerValidator.getTowerState(location);
        this.redstoneTorchBlock = towerState.getRedstoneTorchBlock();
        this.leverBlock = towerState.getLeverBlock();
        this.signBlock = towerState.getSignBlock();
        this.baseBlock = location.getBlock();

        // Bukkit.getServer().broadcastMessage("Creating tower now!");

        wasEnabled = false;
        setSignBlockText(DisplayTime.NONE, STATUS_NO_SIGNAL);
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public boolean updateTower(TowerUpdateCause updateCause) {
        // Bukkit.getServer().broadcastMessage("  I'm being updated because " + updateCause);

        if (!checkComponentsPresent()) {
            destroyTower();
            return false;
        }

        boolean hasPower = this.baseBlock.getBlockPower() > 0;
        LocalTime now = LocalTime.now();
        DisplayTime displayTime = new DisplayTime(now.getHour(), now.getMinute(), now.getSecond());

        if (hasPower && !wasEnabled) {
            RadioTowerManager.EFFECT_ANTENNA_SPARKS.playEffect(getLocation(), getAntennaHeight());
        }

        if (hasPower) {
            setSignBlockText(displayTime, STATUS_ENABLED);
            wasEnabled = true;
        }
        else {
            wasEnabled = false;
            setSignBlockText(displayTime, STATUS_DISABLED);
        }

        return true;
    }

    private void destroyTower() {
        // Bukkit.getServer().broadcastMessage("  I'm being destroyed");
        wasEnabled = false;
        setSignBlockText(DisplayTime.NONE, STATUS_BROKEN);

        World world = this.baseBlock.getWorld();
        Location effectLocation = getLocation().add(0.5, 0.5, 0.5);
        world.playSound(effectLocation, Sound.ENTITY_ITEM_BREAK, 1, 0);
        world.spawnParticle(Particle.BLOCK_CRACK, effectLocation, 100, 0.25, 0.25, 0.25, 3, RadioTowerManager.BASE_MATERIAL.createBlockData());
    }

    private void setSignBlockText(DisplayTime displayTime, String rightStatus) {
        if (this.signBlock != null && RadioTowerManager.isWallSign(signBlock.getType())) {
            Sign signData = (Sign) this.signBlock.getState();
            signData.setLine(0, "Next Broadcast");
            signData.setLine(1, "in " + displayTime.toString());
            signData.setLine(2, "");
            signData.setLine(3, rightStatus);
            signData.update();
        }
    }

    // Todo: Check for skylight
    private boolean checkComponentsPresent() {
        boolean redstoneTorchPresent = redstoneTorchBlock != null && redstoneTorchBlock.getType() == Material.REDSTONE_WALL_TORCH;
        boolean leverPresent = leverBlock != null && leverBlock.getType() == Material.LEVER;
        boolean signPresent = signBlock != null && RadioTowerManager.isWallSign(signBlock.getType());
        boolean baseBlockPresent = baseBlock != null && baseBlock.getType() == RadioTowerManager.BASE_MATERIAL;

        return redstoneTorchPresent && leverPresent && signPresent && baseBlockPresent;
    }

    private int getAntennaHeight() {
        return getAntennaHeight(getLocation());
    }

    public static int getAntennaHeight(Location location) {
        Location antennaBase = location.clone().add(0, 1, 0);
        int worldHeight = location.getWorld().getMaxHeight();
        int antennaMaxHeight = RadioTowerManager.ANTENNA_MAX_HEIGHT;
        int maxHeight = Math.min(antennaBase.getBlockY() + antennaMaxHeight - 1, worldHeight);
        int antennaHeight = 0;

        while (antennaBase.getBlock().getType() == RadioTowerManager.ANTENNA_MATERIAL && antennaBase.getBlockY() <= maxHeight) {
            antennaBase.add(0, 1, 0);
            antennaHeight++;
        }

        return antennaHeight;
    }

    public static boolean canBecomeTower(Location location) {
        TowerState towerState = RadioTowerValidator.getTowerState(location);
        Bukkit.getServer().broadcastMessage("Tower state is: " + towerState);
        return towerState == TowerState.VALID;
    }
}
