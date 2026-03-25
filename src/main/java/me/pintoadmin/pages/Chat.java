package me.pintoadmin.pages;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import me.pintoadmin.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Chat {
    private final Terminal terminal;
    private final Screen screen;
    private final TextGraphics g;
    private final int WIDTH;
    private final int HEIGHT;
    public Chat(Terminal terminal, Screen screen, TextGraphics g) throws IOException {
        this.terminal = terminal;
        this.screen = screen;
        this.g = g;
        WIDTH = g.getSize().getColumns();
        HEIGHT = g.getSize().getRows();
        createPage();
    }

    private final List<String> contacts = new ArrayList<>();

    private int selectedContact = -1;
    private enum cursorSel {
            MSGBOX,
            CONTACT,
            NEWMSG
    }
    private cursorSel selected = cursorSel.CONTACT;

    private int[] writingCursor = new int[2];
    private String typingMsg = "";

    private void createPage() throws IOException {
        g.fill(' '); // Reset Screen

        g.setForegroundColor(Main.GREEN);
        g.setBackgroundColor(Main.BLACK);
        Main.drawBox(0,0, WIDTH/3, HEIGHT-1, g);
        Main.drawBox(WIDTH/3+1, 2, WIDTH-1, HEIGHT-1, g);

        Main.drawText(WIDTH/6, 1,  "CONTACTS", g);
        g.drawLine(1, 2, WIDTH/3-1, 2, Main.HORIZONTAL);

        g.drawLine(WIDTH/3+2, HEIGHT-3, WIDTH-2, HEIGHT-3, '=');

        if(selected == cursorSel.NEWMSG){
            g.setForegroundColor(Main.BLACK);
            g.setBackgroundColor(Main.GREEN);
        }
        g.putString(WIDTH/3-2, 1, "＋", SGR.BOLD);
        if(selected == cursorSel.NEWMSG){
            g.setForegroundColor(Main.GREEN);
            g.setBackgroundColor(Main.BLACK);
        }

        contacts.clear(); // Clear contacts
        File[] files = new File(Main.getDataDir()).listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    contacts.add(file.getName());
                }
            }
        }

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

        updateMessages();
        screen.refresh();
        handleInput();
    }
    private void updateMessages() throws IOException {
        MessageManager msgMan = MessageManager.getInstance();
        int leftMsgX = WIDTH/3+2;
        int rightMsgX = WIDTH-2;
        int yLevel = HEIGHT-5;
        int maxHeight = 3;
        writingCursor[0] = WIDTH/3+3;
        writingCursor[1] = HEIGHT-2;

        if(selectedContact != -1){
            Main.drawText(2*WIDTH/3, 1, contacts.get(selectedContact), g);

            JSONArray messages = msgMan.getMsgs(contacts.get(selectedContact));

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

                List<String> messageLines = getLines(WIDTH/2, message);
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
                        else Main.drawTextRight(rightMsgX-1, yLevel-index, "│" + line +whitespace(endSpace)+"│", g);
                        index--;
                    }
                    Main.drawTextRight(rightMsgX - 1, yLevel + 1, getBottomBubble(longest), g);
                }

                if(!read){
                    TextColor color = new TextColor.RGB(0, 200, 0);
                    g.setForegroundColor(color);
                }
                yLevel -= 2+messageLines.size();
                if (yLevel <= maxHeight) break;
                screen.refresh();
            }
        }
    }

    private void handleInput() throws IOException {
        mainLoop:
        while(true) {
            KeyStroke key = screen.pollInput();
            if(key == null) continue;
            if(selected == cursorSel.MSGBOX) {
                if (key.getKeyType() == KeyType.Character) {
                    g.setCharacter(writingCursor[0], writingCursor[1], key.getCharacter());
                    writingCursor[0]++;
                    setMsgBoxCursor();
                    screen.refresh();
                    typingMsg += key.getCharacter();
                } else if (key.getKeyType() == KeyType.Backspace) {
                    g.setCharacter(writingCursor[0] - 1, writingCursor[1], ' ');
                    if (!typingMsg.isEmpty()) {
                        writingCursor[0]--;
                        setMsgBoxCursor();
                        typingMsg = typingMsg.substring(0, typingMsg.length() - 1);
                    }
                    screen.refresh();
                } else if (key.getKeyType() == KeyType.Enter) {
                    MessageManager.getInstance().sendMessage(typingMsg, contacts.get(selectedContact));
                    typingMsg = "";
                    writingCursor = new int[]{WIDTH / 3 + 3, HEIGHT - 2};
                    setMsgBoxCursor();
                    createPage();
                    break;
                } else if (key.getKeyType() == KeyType.Tab) {
                    selected = cursorSel.CONTACT;
                    screen.setCursorPosition(null);
                    screen.refresh();
                } else if (key.getKeyType() == KeyType.Escape) {
                    new Settings(terminal, screen, g);
                    createPage();
                }
            } else if(selected == cursorSel.NEWMSG){
                if(key.getKeyType() == KeyType.ArrowDown){
                    selected = cursorSel.CONTACT;
                    createPage();
                    break;
                } else if(key.getKeyType() == KeyType.Enter){
                    new NewChat(terminal, screen, g);
                    createPage();
                    break;
                }
            } else if(selected == cursorSel.CONTACT){
                switch (key.getKeyType()) {
                    case KeyType.ArrowDown:
                        if (selectedContact < contacts.size() - 1) selectedContact++;
                        createPage();
                        break mainLoop;
                    case KeyType.ArrowUp:
                        if (selectedContact > 0) selectedContact--;
                        else selected = cursorSel.NEWMSG;
                        createPage();
                        break mainLoop;
                    case KeyType.ArrowRight, KeyType.Tab:
                        selected = cursorSel.MSGBOX;
                        setMsgBoxCursor();
                        updateMessages();
                        break;
                    case KeyType.Character:
                        if (key.getCharacter() == 'q') {
                            screen.close();
                            throw new RuntimeException("quit");
                        }
                        break;
                    case KeyType.Escape:
                        new Settings(terminal, screen, g);
                        createPage();
                        break mainLoop;
                    default:
                        break;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
    }

    private void setMsgBoxCursor(){
        screen.setCursorPosition(new TerminalPosition(writingCursor[0], writingCursor[1]));
    }

    private static List<String> getLines(int maxLength, String message) {
        List<String> messageLines = new ArrayList<>();
        List<String> messageWords = List.of(message.split(" "));

        String currentLine = "";
        for(String word : messageWords){
            if(word.length() + currentLine.length() <= maxLength && !word.contains("\\n"))
                currentLine += word+" ";
            else {
                messageLines.add(currentLine);
                if(word.contains("\\n")) continue;
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
