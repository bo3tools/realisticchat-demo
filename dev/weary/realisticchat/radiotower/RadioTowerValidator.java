package dev.weary.realisticchat.radiotower;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.util.Vector;

public class RadioTowerValidator {

    private static final Vector DIRECTION_EAST = new Vector(1, 0, 0);
    private static final Vector DIRECTION_WEST = new Vector(-1, 0, 0);
    private static final Vector DIRECTION_NORTH = new Vector(0, 0, -1);
    private static final Vector DIRECTION_SOUTH = new Vector(0, 0, 1);

    private static class AdjacentLocation {
        Location location;
        Vector vector;

        public boolean isMountedOnBlock() {
            Directional directionData = (Directional) location.getBlock().getBlockData();
            return directionData.getFacing().getDirection().equals(this.vector);
        }

        AdjacentLocation(Location location, Vector vector) {
            this.location = location.clone().add(vector);
            this.vector = vector;
        }
    }

    private static AdjacentLocation[] getAdjacentLocations(Location location) {
        return new AdjacentLocation[] {
                new AdjacentLocation(location, DIRECTION_EAST),
                new AdjacentLocation(location, DIRECTION_WEST),
                new AdjacentLocation(location, DIRECTION_NORTH),
                new AdjacentLocation(location, DIRECTION_SOUTH)
        };
    }

    private static boolean canSeeSkylight(Location location) {
        int maxHeight = location.getWorld().getMaxHeight();

        while (location.getY() < maxHeight && location.getBlock().getType().isAir()) {
            location.add(0.0, 1.0, 0.0);
        }

        return location.getY() == maxHeight;
    }

    @SuppressWarnings("ConstantConditions")
    public static TowerState getTowerState(Location location) {
        if (location == null) {
            return TowerState.NOT_A_TOWER;
        }

        if (location.getBlock().getType() != RadioTowerManager.BASE_MATERIAL) {
            return TowerState.NOT_A_TOWER;
        }

        int antennaHeight = RadioTower.getAntennaHeight(location);
        if (antennaHeight < RadioTowerManager.ANTENNA_MIN_HEIGHT) {
            return TowerState.ANTENNA_TOO_SHORT.setAntennaHeight(antennaHeight);
        }

        Block redstoneTorchBlock = null;
        Block leverBlock = null;
        Block signBlock = null;

        for (AdjacentLocation adjacentLocation: getAdjacentLocations(location)) {
            Block nearbyBlock = adjacentLocation.location.getBlock();

            if (nearbyBlock.getType() == Material.REDSTONE_WALL_TORCH && adjacentLocation.isMountedOnBlock()) {
                redstoneTorchBlock = nearbyBlock;
            }

            if (nearbyBlock.getType() == Material.LEVER && adjacentLocation.isMountedOnBlock()) {
                leverBlock = nearbyBlock;
            }

            if (RadioTowerManager.isWallSign(nearbyBlock.getType()) && adjacentLocation.isMountedOnBlock()) {
                signBlock = nearbyBlock;
            }
        }

        if (redstoneTorchBlock == null) {
            return TowerState.NEEDS_REDSTONE_TORCH.setAntennaHeight(antennaHeight).setComponents(redstoneTorchBlock, leverBlock, signBlock);
        }

        if (leverBlock == null) {
            return TowerState.NEEDS_LEVER.setAntennaHeight(antennaHeight).setComponents(redstoneTorchBlock, leverBlock, signBlock);
        }

        if (signBlock == null) {
            return TowerState.NEEDS_SIGN.setAntennaHeight(antennaHeight).setComponents(redstoneTorchBlock, leverBlock, signBlock);
        }

        // Todo: Can also trigger if antenna is too long
        Location lastAntennaLocation = location.clone().add(0, antennaHeight + 1, 0);
        if (!canSeeSkylight(lastAntennaLocation)) {
            return TowerState.NEEDS_SUNLIGHT.setAntennaHeight(antennaHeight).setComponents(redstoneTorchBlock, leverBlock, signBlock);
        }

        return TowerState.VALID.setAntennaHeight(antennaHeight).setComponents(redstoneTorchBlock, leverBlock, signBlock);
    }
}
