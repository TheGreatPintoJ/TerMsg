package me.pintoadmin.pages;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.screen.Screen;
import me.pintoadmin.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class Chat {
    private final Screen screen;
    private final TextGraphics g;
    public Chat(Screen screen, TextGraphics g) throws IOException {
        this.screen = screen;
        this.g = g;
        createPage();
    }

    private List<String> contacts = List.of("Person 1", "Person 2", "Person 3");

    private int selectedContact = -1;

    private void createPage() throws IOException {
        g.fill(' '); // Reset Screen

        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        Main.drawBox(0,0, WIDTH/3, HEIGHT-1, g);
        Main.drawBox(WIDTH/3+1, 2, WIDTH-1, HEIGHT-1, g);

        Main.drawText(WIDTH/6, 1,  "CONTACTS", g);
        g.drawLine(1, 2, WIDTH/3-1, 2, Main.HORIZONTAL);

        int idx = 2;
        for(String entry : contacts){
            Main.drawTextLeft(3, idx*2-1, entry, g);
            g.drawLine(1, idx*2, WIDTH/3-1, idx*2, Main.HORIZONTAL);
            idx++;
        }

        JsonParser parser = new JsonParser();
        int leftMsgX = WIDTH/3+2;
        int rightMsgX = WIDTH-2;
        int yLevel = HEIGHT-3;
        int maxHeight = 3;


        JSONArray messages = parser.getMsgs("localhost");
        for (int i = 0; i < messages.length(); i++) {
            JSONObject msg = messages.getJSONObject(i);
            String message = msg.getString("message");
            String timestamp = msg.getString("timestamp");
            boolean received = msg.getBoolean("received");
            boolean read = msg.getBoolean("read");

            if(!read){
                TextColor color = new TextColor.RGB(0, 150, 0);
                g.setForegroundColor(color);
            }

            String bottomBubbleLine = "╰";
            for (int j = 0; j < message.length(); j++)
                bottomBubbleLine += "─";
            bottomBubbleLine += "╯";

            if(received) {
                Main.drawTextLeft(leftMsgX + 1, yLevel - 1, getTopBubble(message.length()), g);
                Main.drawTextLeft(leftMsgX, yLevel, "<╡" + message + "│", g);
                Main.drawTextLeft(leftMsgX + 1, yLevel + 1, bottomBubbleLine, g);
            } else {
                int messageX = rightMsgX - 3 - message.length();
                int leftXCutoff = WIDTH/3+1;
//                if(messageX <= WIDTH/3+1){
//                    messageX = WIDTH/3+2;
//                    message = message.substring(0, WIDTH-(WIDTH/3+2));
//                }

                List<String> messageLines = new ArrayList<>();
                List<String> messageWords = List.of(message.split(" "));

                String currentLine = "";
                for(String word : messageWords){
                    if(word.length() + currentLine.length() <= leftXCutoff)
                        currentLine += word+" ";
                    else {
                        messageLines.add(currentLine);
                        currentLine = word+" ";
                    }
                }
                if(messageLines.isEmpty()) messageLines.add(currentLine);

                Main.drawTextLeft(messageX, yLevel - messageLines.size(), getTopBubble(message.length()), g);
                int index = messageLines.size();
                for(String line : messageLines) {
                    if(messageLines.indexOf(line) == messageLines.size()-1)
                        Main.drawTextRight(rightMsgX, yLevel+index, "│" + line + "╞>", g);
                    else Main.drawTextRight(rightMsgX, yLevel, "│" + line + "│", g);
                    index--;
                }
                Main.drawTextLeft(messageX, yLevel + 1, bottomBubbleLine, g);
            }


            if(!read){
                TextColor color = new TextColor.RGB(0, 200, 0);
                g.setForegroundColor(color);
            }
            yLevel -= 3;
            if (yLevel <= maxHeight) break;
        }
    }

    private String getTopBubble(int width){
        String topBubbleLine = "╭";
        for (int j = 0; j < width; j++)
            topBubbleLine += "─";
        topBubbleLine += "╮";
        return topBubbleLine;
    }
}
