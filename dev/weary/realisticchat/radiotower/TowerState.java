package dev.weary.realisticchat.radiotower;

import org.bukkit.block.Block;

public enum TowerState {
    NOT_A_TOWER,
    ANTENNA_TOO_SHORT,
    NEEDS_REDSTONE_TORCH,
    NEEDS_LEVER,
    NEEDS_SIGN,
    NEEDS_SUNLIGHT,
    VALID(true);

    private boolean validState;
    private int antennaHeight = -1;

    private Block redstoneTorchBlock;
    private Block leverBlock;
    private Block signBlock;

    public boolean isValidTower() {
        return this.validState;
    }

    public int getAntennaHeight() {
        return this.antennaHeight;
    }

    public TowerState setAntennaHeight(int antennaHeight) {
        this.antennaHeight = antennaHeight;
        return this;
    }

    public TowerState setComponents(Block redstoneTorchBlock, Block leverBlock, Block signBlock) {
        this.redstoneTorchBlock = redstoneTorchBlock;
        this.leverBlock = leverBlock;
        this.signBlock = signBlock;
        return this;
    }

    public Block getRedstoneTorchBlock() {
        return this.redstoneTorchBlock;
    }

    public Block getLeverBlock() {
        return this.leverBlock;
    }

    public Block getSignBlock() {
        return this.signBlock;
    }

    TowerState() {
        this.validState = false;
    }

    TowerState(boolean validState) {
        this.validState = validState;
    }
}
