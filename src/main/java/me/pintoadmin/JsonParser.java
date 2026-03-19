package me.pintoadmin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public List<String> getLines(String ip){
        Main.OSTYPES os = Main.getOS();
        String dataDir =
                switch (os){
                    case LINUX -> System.getenv("HOME")+"/.local/share/termsg/";
                    case WINDOWS -> System.getenv("LOCALAPPDATA");
                    case MAC -> System.getProperty("user.home") + "/Library/Application Support/";
                };
        File chatFile = new File(dataDir+ip);

        try {
            List<String> lines = Files.readAllLines(chatFile.toPath()).reversed();
            lines.removeIf(String::isEmpty);
            return lines;
        } catch (IOException e){
            return List.of("ERROR: "+e.getMessage());
        }
    }
}
