package me.pintoadmin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    public String getMsg(String ip, int idx) {
        Main.OSTYPES os = Main.getOS();
        String dataDir =
            switch (os){
                case LINUX -> System.getenv("HOME")+"/.local/share/termsg/";
                case WINDOWS -> System.getenv("LOCALAPPDATA");
                case MAC -> System.getProperty("user.home") + "/Library/Application Support/";
            };
        File chatFile = new File(dataDir+ip);

        try {
            List<String> lines = Files.readAllLines(chatFile.toPath());
            List<String> revLines = lines.reversed();
            revLines.removeIf(String::isEmpty);
            return revLines.get(idx);
        } catch (IOException e){
            return "ERROR: "+e.getMessage();
        }
    }

    public JSONArray getMsgs(String ip) throws IOException {
        Main.OSTYPES os = Main.getOS();
        String dataDir =
                switch (os){
                    case LINUX -> System.getenv("HOME")+"/.local/share/termsg/";
                    case WINDOWS -> System.getenv("LOCALAPPDATA");
                    case MAC -> System.getProperty("user.home") + "/Library/Application Support/";
                };
        File chatFile = new File(dataDir+ip);

        String content = new String(Files.readAllBytes(chatFile.toPath()));
        JSONObject root = new JSONObject(content);
        JSONArray messages = root.getJSONArray("messages");
        return messages;
    }
}
