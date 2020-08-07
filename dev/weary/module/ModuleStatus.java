package dev.weary.module;

public class ModuleStatus {
    public String moduleName;
    public boolean isActive;

    ModuleStatus(String moduleName, boolean isActive) {
        this.moduleName = moduleName;
        this.isActive = isActive;
    }
}
