package dev.weary.realisticchat;

import dev.weary.module.IConfigSetting;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public enum RealisticChatSettings implements IConfigSetting {
    UPDATE_TICKS("towers.update-ticks", 20),
    BASE_MATERIAL("towers.base-material", "minecraft:iron_block"),
    ANTENNA_MATERIAL("towers.antenna-material", "minecraft:iron_bars"),
    ANTENNA_MIN_HEIGHT("towers.antenna-min-height", 5),
    ANTENNA_MAX_HEIGHT("towers.antenna-max-height", 60);

    private String yamlName;
    private Object defaultValue;

    RealisticChatSettings(String yamlName, Object defaultValue) {
        this.yamlName = yamlName;
        this.defaultValue = defaultValue;
    }

    public int asInteger() {
        return RealisticChatModule.instance.yamlConfig.getInt(yamlName, (Integer) defaultValue);
    }

    public Material asMaterial() {
        String materialName = RealisticChatModule.instance.yamlConfig.getString(yamlName, (String) defaultValue);
        return Material.matchMaterial(materialName);
    }

    @NotNull
    public String getYamlName() {
        return this.yamlName;
    }

    @NotNull
    public Object getDefaultValue() {
        return this.defaultValue;
    }
}
