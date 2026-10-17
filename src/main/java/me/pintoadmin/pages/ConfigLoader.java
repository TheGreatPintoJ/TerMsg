package me.pintoadmin.pages;

import me.pintoadmin.*;
import org.yaml.snakeyaml.*;

import java.io.*;
import java.util.*;

public class ConfigLoader {
    private static volatile ConfigLoader instance;
    private File configFile;
    private final Yaml yaml = new Yaml();
    private final Map<String, Object> rootLevel = new HashMap<>();

    private ConfigLoader() {
        reloadConfig();
    }

    public static ConfigLoader getInstance(){
        if (instance == null) { // First check (no logging/locking overhead)
            synchronized (ConfigLoader.class) {
                if (instance == null) { // Second check (inside synchronization)
                    instance = new ConfigLoader();
                }
            }
        }
        return instance;
    }

    public void reloadConfig(){
        configFile = new File(Main.getDataDir()+".config.yml");

        try {
            if(!configFile.exists()) repopulateConfig();
            InputStream inputStream = new FileInputStream(configFile.getPath());
            rootLevel.clear();
            rootLevel.putAll(yaml.load(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void repopulateConfig(){
        try {
            if(!configFile.exists()) configFile.createNewFile();
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("textColor", "Green");
            defaultData.put("port", 51337);

            FileWriter writer = new FileWriter(configFile.getPath());
            yaml.dump(defaultData, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateConfig(){
        try {
            FileWriter writer = new FileWriter(configFile.getPath());
            yaml.dump(rootLevel, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTextColor(){
        return (String) rootLevel.getOrDefault("textColor", "Green");
    }
    public void setTextColor(String color){
        rootLevel.remove("textColor");
        rootLevel.put("textColor", color);
        updateConfig();
    }

    public Integer getPort(){
        return (Integer) rootLevel.getOrDefault("port", 51337);
    }
    public void savePort(){
        rootLevel.remove("port");
        rootLevel.put("port", NetworkingManager.PORT);
        updateConfig();
    }
}
