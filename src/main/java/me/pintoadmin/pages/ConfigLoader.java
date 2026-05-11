package me.pintoadmin.pages;

import me.pintoadmin.*;
import org.yaml.snakeyaml.*;

import java.io.*;
import java.util.*;

public class ConfigLoader {
    private File configFile;
    private final Yaml yaml = new Yaml();
    private final Map<String, Object> rootLevel = new HashMap<>();

    public ConfigLoader() {
        reloadConfig();
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
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("textColor", "Green");

            FileWriter writer = new FileWriter(configFile.getPath());
            yaml.dump(defaultData, writer);
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
}
