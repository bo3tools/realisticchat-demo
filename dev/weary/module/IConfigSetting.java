package dev.weary.module;

import org.jetbrains.annotations.NotNull;

public interface IConfigSetting {
    @NotNull String getYamlName();
    @NotNull Object getDefaultValue();
}
