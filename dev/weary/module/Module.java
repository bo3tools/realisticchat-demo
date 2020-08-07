package dev.weary.module;

import dev.weary.realisticchat.RealisticChatModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class Module {
    private static final String CONFIG_MODULE_ENABLED = "enabled";

    private String moduleName;
    private String moduleDescription;
    private String configFileName;
    private boolean isActive;

    public YamlConfiguration yamlConfig;

    protected Module(@NotNull String moduleName, @NotNull String moduleDescription, @NotNull String configFileName) {
        this.moduleName = moduleName;
        this.moduleDescription = moduleDescription;
        this.configFileName = configFileName;
    }

    protected abstract boolean loadModule(JavaPlugin plugin);
    protected abstract void unloadModule();
    protected void onConfigLoaded() {}

    protected void registerEvents(@NotNull Listener listener) {
        pluginInstance.getServer().getPluginManager().registerEvents(listener, pluginInstance);
    }

    protected void initializeDefaultConfig(IConfigSetting[] configSettings) {
        if (this.configFileName == null) {
            throw new IllegalStateException("Cannot initialize default config of module " + this.moduleName + " without a specified config file");
        }

        for (IConfigSetting configSetting : configSettings) {
            if (this.yamlConfig.get(configSetting.getYamlName()) == null) {
                this.yamlConfig.set(configSetting.getYamlName(), configSetting.getDefaultValue());
            }
        }

        this.saveConfig();
        this.loadConfig();
    }

    private String getConfigHeader() {
        return this.moduleName + " module configuration file\n" + this.moduleDescription + "\n";
    }

    private void loadConfig() {
        if (this.configFileName != null) {
            File configFile = new File(pluginInstance.getDataFolder(), configFileName);
            this.yamlConfig = new YamlConfiguration().options().header(getConfigHeader()).configuration();

            if (!configFile.exists()) {
                return;
            }

            try {
                this.yamlConfig.load(configFile);
                this.onConfigLoaded();
            }
            catch (InvalidConfigurationException | IOException e) {
                logger.severe("  Cannot load config file of module " + this.moduleName);
                e.printStackTrace();
            }
        }
    }
    private void saveConfig() {
        if (this.yamlConfig == null) {
            return;
        }

        try {
            File configFile = new File(pluginInstance.getDataFolder(), configFileName);
            this.yamlConfig.save(configFile);
        } catch (IOException e) {
            logger.severe("  Cannot save config file of module " + this.moduleName);
            e.printStackTrace();
        }
    }

    private static Logger logger = Bukkit.getLogger();
    private static List<Module> allModules = new ArrayList<>();
    private static JavaPlugin pluginInstance = null;
    private static boolean modulesWereLoaded = false;

    private static void initializeModule(Module module) {
        logger.info("  Initializing module " + module.moduleName);
        if (module.isActive) {
            throw new IllegalStateException("Cannot initialize an active module " + module.moduleName);
        }

        if (module.configFileName != null) {
            module.loadConfig();

            Boolean isEnabled = (Boolean) module.yamlConfig.get(CONFIG_MODULE_ENABLED);
            if (isEnabled == null) {
                module.yamlConfig.set(CONFIG_MODULE_ENABLED, true);
                module.saveConfig();
                isEnabled = true;
            }

            if (!isEnabled) {
                logger.info("  Module is disabled in config, skipping.");
                return;
            }
        }

        if (module.loadModule(pluginInstance)) {
            module.isActive = true;
            allModules.add(module);
        }
    }

    public static void unloadModules() {
        if (!modulesWereLoaded) {
            throw new IllegalStateException("Modules not loaded yet");
        }

        for (Module module: allModules) {
            module.unloadModule();
            module.isActive = false;
        }
    }
    public static void reloadConfigs() {
        if (!modulesWereLoaded) {
            throw new IllegalStateException("Modules not loaded yet");
        }

        for (Module module: allModules) {
            module.loadConfig();
        }
    }
    public static void loadModules(JavaPlugin javaPlugin) {
        if (modulesWereLoaded) {
            throw new IllegalStateException("Modules were already loaded");
        }

        modulesWereLoaded = true;
        pluginInstance = javaPlugin;
        logger.info("Initializing all modules...");
        initializeAllModules();
    }
    public static List<ModuleStatus> getModuleStatusList() {
        return allModules.stream().map(module -> new ModuleStatus(module.moduleName, module.isActive)).collect(Collectors.toList());
    }

    private static void initializeAllModules() {
        initializeModule(new RealisticChatModule());
    }
}
