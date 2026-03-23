package me.pintoadmin.pages;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import me.pintoadmin.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static me.pintoadmin.Main.main;

public class Chat {
    private final Terminal terminal;
    private final Screen screen;
    private final TextGraphics g;
    public Chat(Terminal terminal, Screen screen, TextGraphics g) throws IOException {
        this.terminal = terminal;
        this.screen = screen;
        this.g = g;
        createPage();
    }

    private List<String> contacts = new ArrayList<>();

    private int selectedContact = 0;

    private void createPage() throws IOException {
        g.fill(' '); // Reset Screen

        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        g.setForegroundColor(Main.GREEN);
        g.setBackgroundColor(Main.BLACK);
        Main.drawBox(0,0, WIDTH/3, HEIGHT-1, g);
        Main.drawBox(WIDTH/3+1, 2, WIDTH-1, HEIGHT-1, g);

        Main.drawText(WIDTH/6, 1,  "CONTACTS", g);
        g.drawLine(1, 2, WIDTH/3-1, 2, Main.HORIZONTAL);

        

        int idx = 2;
        for(String entry : contacts){
            if(contacts.indexOf(entry) == selectedContact){
                g.setForegroundColor(Main.TRUE_BLACK);
                g.setBackgroundColor(Main.GREEN);
            } else {
                g.setForegroundColor(Main.GREEN);
                g.setBackgroundColor(Main.BLACK);
            }
            Main.drawTextLeft(3, idx*2-1, entry, g);

            g.setForegroundColor(Main.GREEN);
            g.setBackgroundColor(Main.BLACK);
            g.drawLine(1, idx*2, WIDTH/3-1, idx*2, Main.HORIZONTAL);
            idx++;
        }

        JsonParser parser = new JsonParser();
        int leftMsgX = WIDTH/3+2;
        int rightMsgX = WIDTH-2;
        int yLevel = HEIGHT-3;
        int maxHeight = 3;

        if(selectedContact != -1){
            JSONArray messages = parser.getMsgs(contacts.get(selectedContact));
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

                List<String> messageLines = getLines(WIDTH, message);
                int longest = getLongest(messageLines);
                if(received) {

                    Main.drawTextLeft(leftMsgX + 1, yLevel - messageLines.size(), getTopBubble(longest), g);
                    int index = messageLines.size()-1;
                    for(String line : messageLines) {
                        int endSpace = longest-line.length();
                        if(line.equals(messageLines.getLast()))
                            Main.drawTextLeft(leftMsgX, yLevel, "<╡" + line +whitespace(endSpace)+"│", g);
                        else Main.drawTextLeft(leftMsgX+1, yLevel-index, "│" + line +whitespace(endSpace)+"│", g);
                        index--;
                    }
                    Main.drawTextLeft(leftMsgX + 1, yLevel + 1, getBottomBubble(longest), g);
                } else {

                    Main.drawTextRight(rightMsgX - 1, yLevel - messageLines.size(), getTopBubble(longest), g);
                    int index = messageLines.size()-1;
                    for(String line : messageLines) {
                        int endSpace = longest-line.length();
                        if(line.equals(messageLines.getLast()))
                            Main.drawTextRight(rightMsgX, yLevel, "│" + line +whitespace(endSpace)+"╞>", g);
                        else Main.drawTextRight(rightMsgX+1, yLevel-index, "│" + line +whitespace(endSpace)+"│", g);
                        index--;
                    }
                    Main.drawTextRight(rightMsgX - 1, yLevel + 1, getBottomBubble(longest), g);
                }

                if(!read){
                    TextColor color = new TextColor.RGB(0, 200, 0);
                    g.setForegroundColor(color);
                }
                yLevel -= 3;
                if (yLevel <= maxHeight) break;
                screen.refresh();
            }
        }
        screen.refresh();
        handleInput();
    }

    private void handleInput() throws IOException {
        mainLoop:
        while(true) {
            KeyStroke key = screen.pollInput();
            if(key == null) continue;
            switch (key.getKeyType()) {
                case KeyType.ArrowDown:
                    if(selectedContact < contacts.size()-1) selectedContact++;
                    createPage();
                    break mainLoop;
                case KeyType.ArrowUp:
                    if(selectedContact > 0) selectedContact--;
                    createPage();
                    break mainLoop;
                case KeyType.Character:
                    if(key.getCharacter() == 'q') {
                        screen.close();
                        throw new RuntimeException("quit");
                    }
                case KeyType.Escape:
                    new Settings(terminal, screen, g);
                    createPage();
                default:
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break mainLoop;
                    }
            }
        }
    }

    private static List<String> getLines(int WIDTH, String message) {
        int leftXCutoff = WIDTH /3+1;

        List<String> messageLines = new ArrayList<>();
        List<String> messageWords = List.of(message.split(" "));

        String currentLine = "";
        for(String word : messageWords){
            if(word.length() + currentLine.length() <= leftXCutoff && !word.contains("\n"))
                currentLine += word+" ";
            else {
                messageLines.add(currentLine);
                currentLine = word+" ";
            }
        }
        if(!messageLines.contains(currentLine)) messageLines.add(currentLine);
        return messageLines;
    }

    private int getLongest(List<String> input){
        int longest = 0;
        for(String str : input){
            if(longest < str.length()){
                longest = str.length();
            }
        }
        return longest;
    }

    private String whitespace(int amnt){
        String str = "";
        for(int i = 0; i < amnt; i++){
            str += " ";
        }
        return str;
    }

    private String getTopBubble(int width){
        String topBubbleLine = "╭";
        for (int j = 0; j < width; j++)
            topBubbleLine += "─";
        topBubbleLine += "╮";
        return topBubbleLine;
    }
    private String getBottomBubble(int width){
        String topBubbleLine = "╰";
        for (int j = 0; j < width; j++)
            topBubbleLine += "─";
        topBubbleLine += "╯";
        return topBubbleLine;
    }
}
