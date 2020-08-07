package dev.weary.module;

import dev.weary.MinemanPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Logger;

public class StorageFile {
    private static final JavaPlugin pluginInstance = MinemanPlugin.instance;

    private static final Logger pluginLogger = pluginInstance.getLogger();
    private static File storageFolder = new File(pluginInstance.getDataFolder(), "storage");

    static {
        storageFolder.mkdirs();
    }

    private File storageFile;

    public String getServerFileLocation() {
        return String.join(File.separator, "plugins", pluginInstance.getName(), storageFolder.getName(), storageFile.getName());
    }

    private void createIfNotExists() throws IOException {
        if (!this.storageFile.exists()) {
            this.storageFile.createNewFile();
        }
    }

    public StorageFile(String fileName) {
        this.storageFile = new File(storageFolder, fileName);
    }

    public BufferedReader getReader() {
        try {
            this.createIfNotExists();
            return new BufferedReader(new FileReader(this.storageFile));
        }
        catch (Exception e) {
            pluginLogger.severe("Cannot read storage file " + this.getServerFileLocation());
            e.printStackTrace();
        }

        return null;
    }

    public BufferedWriter getWriter() {
        try {
            this.createIfNotExists();
            return new BufferedWriter(new FileWriter(this.storageFile));
        }
        catch (Exception e) {
            pluginLogger.severe("Cannot write storage file " + this.getServerFileLocation());
            e.printStackTrace();
        }

        return null;
    }
}
