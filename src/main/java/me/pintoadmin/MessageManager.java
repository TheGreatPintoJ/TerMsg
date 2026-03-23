package me.pintoadmin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageManager {
    private static volatile MessageManager INSTANCE; // volatile ensures visibility across threads
    private MessageManager() {}

    public static MessageManager getInstance() {
        if (INSTANCE == null) { // First check
            synchronized (MessageManager.class) {
                if (INSTANCE == null) { // Second check
                    INSTANCE = new MessageManager();
                }
            }
        }
        return INSTANCE;
    }

    public void sendMessage(String message, String host) throws IOException {
        String dataDir = Main.getDataDir();
        File chatFile = new File(dataDir + host);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // 1. Create the JSON object
        JSONObject obj = new JSONObject();
        obj.put("message", message);
        obj.put("timestamp", LocalDateTime.now().format(dateTimeFormatter));
        obj.put("received", false);
        obj.put("read", false);

        JSONArray arr = getMsgs(host);
        arr.put(obj);

        JSONObject mainObj = new JSONObject();
        mainObj.put("name", getName(host));
        mainObj.put("messages", arr);

        // 2. Write the JSON string to a file
        try (FileWriter file = new FileWriter(chatFile)) {
            file.write(mainObj.toString(4)); //pretty-printing (indentation)
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getMsgs(String host)  {
        try {
            String dataDir = Main.getDataDir();
            File chatFile = new File(dataDir + host);

            String content = new String(Files.readAllBytes(chatFile.toPath()));
            JSONObject root = new JSONObject(content);
            JSONArray messages = root.getJSONArray("messages");

            List<JSONObject> messageList = new ArrayList<>();

            // 1. Convert JSONArray to List
            for (int i = 0; i < messages.length(); i++) {
                messageList.add(messages.getJSONObject(i));
            }

            // 2. Sort the List using a Comparator
            messageList.sort((a, b) -> {
                String valA = a.optString("timestamp", "");
                String valB = b.optString("timestamp", "");
                return valB.compareTo(valA);
            });

            // 3. Convert sorted List back to JSONArray
            JSONArray sortedMessages = new JSONArray();
            for (JSONObject obj : messageList) {
                sortedMessages.put(obj);
            }

            return sortedMessages;
        } catch (Exception e){
            return new JSONArray();
        }
    }

    public String getName(String host) throws IOException{
        try {
            String dataDir = Main.getDataDir();
            File chatFile = new File(dataDir + host);

            String content = new String(Files.readAllBytes(chatFile.toPath()));
            JSONObject root = new JSONObject(content);
            return root.getString("name");
        } catch (FileNotFoundException e){
            return host;
        }
    }
}
