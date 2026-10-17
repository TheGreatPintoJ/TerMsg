package me.pintoadmin.pages;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import me.pintoadmin.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Chat {
    private final Screen screen;
    private final TextGraphics g;
    private final int WIDTH;
    private final int HEIGHT;
    private int sidebarWidth;
    private int messagesBoxOffset = 0;
    public Chat() throws IOException {
        this.screen = Main.screen;
        this.g = Main.g;
        WIDTH = g.getSize().getColumns();
        HEIGHT = g.getSize().getRows();
        sidebarWidth = 40;
        createPage();
    }

    private final List<String> contacts = new ArrayList<>();
    private final Map<String, String> contactMap = new HashMap<>(); // Name to Host

    private int selectedContact = -1;
    private enum cursorSel {
            MSGBOX,
            CONTACT,
            NEWMSG
    }
    private cursorSel selected = cursorSel.CONTACT;

    private int[] writingCursor = new int[2];
    private String[] typingMsg = {"", ""}; // 0-before cursor 1-after cursor
    private final List<String> beforeLines = new ArrayList<>();
    private final List<String> afterLines = new ArrayList<>();

    private final int unreadDimFactor = 75;

    //↕

    private void createPage() throws IOException {
        createPage(0);
        handleInput();
    }
    private void createPage(int offset) throws IOException {
        screen.clear();

        g.setForegroundColor(Main.TEXTCOLOR);
        g.setBackgroundColor(Main.BACKGROUNDCOLOR);
        g.fill(' '); // Reset Screen

        contacts.clear(); // Clear contacts
        contactMap.clear();
        File[] files = new File(Main.getDataDir()).listFiles();
        if(files != null) {
            for (File file : files) {
                if(file.getName().equals(".config.yml")) continue;
                if (file.isFile()) {
                    String host = file.getName();
                    String name = MessageManager.getInstance().getName(host);
                    contacts.add(name);
                    contactMap.put(name, host);
                }
            }
        }

        int idx = 2;
        for(String entry : contacts){
            if(contacts.indexOf(entry) == selectedContact && selected != cursorSel.NEWMSG){
                g.setForegroundColor(Main.BLACK);
                g.setBackgroundColor(Main.TEXTCOLOR);
            } else {
                g.setForegroundColor(Main.TEXTCOLOR);
                g.setBackgroundColor(Main.BACKGROUNDCOLOR);
            }
            Main.drawTextLeft(2, idx*2-1, entry);

            g.setForegroundColor(Main.TEXTCOLOR);
            g.setBackgroundColor(Main.BACKGROUNDCOLOR);
            g.drawLine(1, idx*2, sidebarWidth-1, idx*2, Main.HORIZONTAL);
            idx++;
        }

        updateMessages(messagesBoxOffset);
        screen.refresh();
    }
    private void updateMessages(int offset) throws IOException {
        // Message Loading
        MessageManager msgMan = MessageManager.getInstance();
        int leftMsgX = sidebarWidth+2;
        int rightMsgX = WIDTH-1;
        int yLevel = HEIGHT-5-offset;
        int maxHeight = 3;
        writingCursor[0] = sidebarWidth+3;
        writingCursor[1] = HEIGHT-2;

        if(selectedContact != -1){
            JSONArray messages = msgMan.getMsgs(contactMap.get(contacts.get(selectedContact)));
            for (int i = 0; i < messages.length(); i++) {
                JSONObject msg = messages.getJSONObject(i);
                String message = msg.getString("message");
                boolean received = msg.getBoolean("received");
                boolean read = msg.getBoolean("read");

                if(!read){
                    int newRed = Main.TEXTCOLOR.getRed() - unreadDimFactor;
                    int newGreen = Main.TEXTCOLOR.getGreen() - unreadDimFactor;
                    int newBlue = Main.TEXTCOLOR.getBlue() - unreadDimFactor;
                    TextColor color = new TextColor.RGB(Math.max(newRed, 0), Math.max(newGreen, 0), Math.max(newBlue, 0));
                    g.setForegroundColor(color);
                }

                List<String> messageLines = getLines(WIDTH-sidebarWidth-20, message);
                int longest = getLongest(messageLines);
                if(received) {
                    Main.drawTextLeft(leftMsgX + 1, yLevel - messageLines.size(), getTopBubble(longest));
                    int index = messageLines.size()-1;
                    for(String line : messageLines) {
                        int endSpace = longest-line.length();
                        if(line.equals(messageLines.getLast()))
                            Main.drawTextLeft(leftMsgX, yLevel, "<╡ " + line +whitespace(endSpace)+" │");
                        else Main.drawTextLeft(leftMsgX+1, yLevel-index, "│ " + line +whitespace(endSpace)+" │");
                        index--;
                    }
                    Main.drawTextLeft(leftMsgX + 1, yLevel + 1, getBottomBubble(longest));
                } else {
                    Main.drawTextRight(rightMsgX - 1, yLevel - messageLines.size(), getTopBubble(longest));
                    int index = messageLines.size()-1;
                    for(String line : messageLines) {
                        int endSpace = longest-line.length();
                        if(line.equals(messageLines.getLast()))
                            Main.drawTextRight(rightMsgX, yLevel, "│ " + line +whitespace(endSpace)+" ╞>");
                        else Main.drawTextRight(rightMsgX-1, yLevel-index, "│ " + line +whitespace(endSpace)+" │");
                        index--;
                    }
                    Main.drawTextRight(rightMsgX - 1, yLevel + 1, getBottomBubble(longest));
                }

                if(!read){
                    TextColor color = Main.TEXTCOLOR;
                    g.setForegroundColor(color);
                }
                yLevel -= 2+messageLines.size();
                if (yLevel <= maxHeight) break;
                screen.refresh();
            }
            Main.drawTextLeft(0,0, whitespace(WIDTH));
            Main.drawTextLeft(0,1, whitespace(WIDTH));
            Main.drawText((sidebarWidth+WIDTH)/2, 1, contacts.get(selectedContact));
        }

        // Update page
        Main.drawBox(0,0, sidebarWidth, HEIGHT-1);
        Main.drawBox(sidebarWidth+1, 2, WIDTH-1, HEIGHT-1);

        Main.drawText(sidebarWidth/2, 1,  "CONTACTS");
        g.drawLine(1, 2, sidebarWidth-1, 2, Main.HORIZONTAL);

        g.drawLine(sidebarWidth+2, HEIGHT-3-offset, WIDTH-2, HEIGHT-3-offset, '=');

        if(selected == cursorSel.NEWMSG){
            g.setForegroundColor(Main.BACKGROUNDCOLOR);
            g.setBackgroundColor(Main.TEXTCOLOR);
        }
        g.putString(sidebarWidth-2, 1, "＋", SGR.BOLD);
        if(selected == cursorSel.NEWMSG){
            g.setForegroundColor(Main.TEXTCOLOR);
            g.setBackgroundColor(Main.BACKGROUNDCOLOR);
        }
        screen.refresh();
    }

    private void handleInput() throws IOException {
        mainLoop:
        while(true) {
            KeyStroke key = screen.pollInput();
            if(key == null) continue;
            if (key.getKeyType() == KeyType.Escape) {
                new Settings();
                createPage();
            }
            if(selected == cursorSel.MSGBOX) {
                if (key.getKeyType() == KeyType.ArrowLeft) {
                    if (!typingMsg[0].isEmpty()) {
                        typingMsg[1] = typingMsg[0].charAt(typingMsg[0].length() -1) + typingMsg[1];
                        typingMsg[0] = typingMsg[0].substring(0, typingMsg[0].length() - 1);

                        writingCursor[0]--;
                        updateCursor();
                        screen.refresh();
                    }
                } else if (key.getKeyType() == KeyType.ArrowRight) {
                    if (!typingMsg[1].isEmpty()) {
                        typingMsg[0] += typingMsg[1].charAt(0);
                        typingMsg[1] = typingMsg[1].substring(1);

                        writingCursor[0]++;
                        updateCursor();
                        screen.refresh();
                    }
                } else if (key.getKeyType() == KeyType.ArrowUp){
                    if (!beforeLines.isEmpty()) {
                        afterLines.add(typingMsg[0]+typingMsg[1]);

                        int place = typingMsg[0].length();
                        typingMsg[0] = beforeLines.getLast().substring(0, place);
                        typingMsg[1] = beforeLines.getLast().substring(place);

                        beforeLines.removeLast();

                        writingCursor[1]--;
                        updateCursor();
                        screen.refresh();
                    }
                } else if (key.getKeyType() == KeyType.ArrowDown){
                    if (!afterLines.isEmpty()) {
                        beforeLines.add(typingMsg[0]+typingMsg[1]);

                        int place = typingMsg[0].length();
                        typingMsg[0] = afterLines.getLast().substring(0, place);
                        if(place < afterLines.getLast().length())
                            typingMsg[1] = afterLines.getLast().substring(place);
                        else typingMsg[1] = "";

                        afterLines.removeLast();

                        writingCursor[1]++;
                        updateCursor();
                        screen.refresh();
                    }
                } else if (key.getKeyType() == KeyType.Character) {
                    if(writingCursor[0] < WIDTH-3
                        && writingCursor[0]+typingMsg[1].length() < WIDTH-3) {
                        // valid char
                        g.setCharacter(writingCursor[0], writingCursor[1], key.getCharacter());
                        writingCursor[0]++;
                        Main.drawTextLeft(writingCursor[0], writingCursor[1], typingMsg[1]);
                        typingMsg[0] += key.getCharacter();
                    } else if(writingCursor[0] >= WIDTH-3){
                        // broke newline with cursor
                        typingMsg[0] += key.getCharacter();
                        reformatMessageBox();
                    } else {
                        typingMsg[0] += key.getCharacter();
                        reformatMessageBox(); // broke newline with chars after cursor
                    }
//                    typingMsg[0] += key.getCharacter();
//                    reformatMessageBox();
                    updateCursor();
                    screen.refresh();
                } else if (key.getKeyType() == KeyType.Backspace) {
                    if (!typingMsg[0].isEmpty()) {
                        g.setCharacter(writingCursor[0] - 1, writingCursor[1], ' ');
                        writingCursor[0]--;
                        updateCursor();
                        Main.drawTextLeft(writingCursor[0], writingCursor[1], typingMsg[1]);
                        typingMsg[0] = typingMsg[0].substring(0, typingMsg[0].length() - 1);
                    } else {
                        if(!beforeLines.isEmpty()) {
                            int numLines = beforeLines.size();
                            typingMsg[0] = beforeLines.getLast().substring(0, beforeLines.getLast().length()-1);
                            beforeLines.removeLast();
                            createPage(numLines-1);

                            for(String line : beforeLines){
                                Main.drawTextLeft(writingCursor[0], writingCursor[1]-(numLines-1-beforeLines.indexOf(line)), line);
                            }
                            Main.drawTextLeft(writingCursor[0], writingCursor[1], typingMsg[0]);
                            writingCursor[0] = WIDTH/3+3+typingMsg[0].length();
                            updateCursor();
                            screen.refresh();
                        }
                    }
                    screen.refresh();
                } else if (key.getKeyType() == KeyType.Enter) {
                    if (key.isShiftDown()) {
                        typingMsg[0] += "⏎";
                        beforeLines.add(typingMsg[0]);
                        typingMsg[0] = "";

                        //writingCursor[0] = WIDTH / 3 + 3;
                        //writingCursor[1]++;
                        reformatMessageBox();
                        screen.refresh();
                    } else {
                        List<String> allLines = beforeLines;
                        allLines.add(typingMsg[0] + typingMsg[1]);
                        allLines.addAll(afterLines);
                        String fullMsg = String.join(" ", allLines);
                        MessageManager.getInstance().sendMessage(fullMsg, contactMap.get(contacts.get(selectedContact)));
                        beforeLines.clear();
                        afterLines.clear();
                        typingMsg = new String[]{"", ""};
                        writingCursor = new int[]{sidebarWidth + 3, HEIGHT - 2};
                        updateCursor();
                        createPage();
                        break;
                    }
                } else if (key.getKeyType() == KeyType.Tab) {
                    selected = cursorSel.CONTACT;
                    screen.setCursorPosition(null);
                    screen.refresh();
                }
            } else if(selected == cursorSel.NEWMSG){
                if(key.getKeyType() == KeyType.ArrowDown){
                    selected = cursorSel.CONTACT;
                    createPage();
                    break;
                } else if(key.getKeyType() == KeyType.Enter){
                    new NewChat();
                    createPage();
                    break;
                }
            } else if(selected == cursorSel.CONTACT){
                switch (key.getKeyType()) {
                    case KeyType.ArrowDown:
                        if (selectedContact < contacts.size() - 1) selectedContact++;
                        beforeLines.clear();
                        afterLines.clear();
                        typingMsg = new String[]{"", ""};
                        createPage();
                        break mainLoop;
                    case KeyType.ArrowUp:
                        if (selectedContact > 0) selectedContact--;
                        else selected = cursorSel.NEWMSG;
                        beforeLines.clear();
                        afterLines.clear();
                        typingMsg = new String[]{"", ""};
                        createPage();
                        break mainLoop;
                    case KeyType.ArrowRight, KeyType.Tab:
                        if(key.isAltDown() && key.getKeyType() == KeyType.ArrowRight){
                            if(sidebarWidth >= WIDTH/2) break;
                            sidebarWidth++;
                            createPage();
                        } else {
                            if (selectedContact == -1) break;
                            selected = cursorSel.MSGBOX;
                            updateCursor();
                            updateMessages(0);
                            screen.refresh();
                        }
                        break;
                    case KeyType.ArrowLeft:
                        if(key.isAltDown()){
                            if(sidebarWidth <= 3+getLongest(contacts)) break;
                            sidebarWidth--;
                            createPage();
                        }
                        break;
                    case KeyType.Character:
                        if (key.getCharacter() == 'q') {
                            screen.close();
                            throw new RuntimeException("quit");
                        } else if(key.getCharacter() == 'e'){
                            new NewChat(contactMap.get(contacts.get(selectedContact)));
                            createPage();
                        } else if(key.getCharacter() == 'd'){
                            new DeleteChat(contactMap.get(contacts.get(selectedContact)));
                            selectedContact--;
                            createPage();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void updateCursor(){
        screen.setCursorPosition(new TerminalPosition(writingCursor[0], writingCursor[1]));
    }
    private void reformatMessageBox() throws IOException {
        String allBeforeLines = String.join("", beforeLines);
        allBeforeLines += typingMsg[0];
        beforeLines.clear();
        beforeLines.addAll(getLines(WIDTH/2, allBeforeLines));
        typingMsg[0] = beforeLines.getLast();
        beforeLines.removeLast();

        int offset = typingMsg[0].length();

        String allAfterLines = typingMsg[1];
        allAfterLines += String.join(" ", afterLines);
        afterLines.clear();
        afterLines.addAll(getLines(offset, WIDTH/2, allAfterLines));
        typingMsg[1] = afterLines.getFirst();
        afterLines.removeFirst();

        createPage(beforeLines.size()+afterLines.size());
        for(String line : beforeLines)
            Main.drawTextLeft(writingCursor[0], writingCursor[1]-afterLines.size()-(beforeLines.size()-beforeLines.indexOf(line)), line);
        for(String line : afterLines)
            Main.drawTextLeft(writingCursor[0], writingCursor[1]-afterLines.indexOf(line), line);

        Main.drawTextLeft(writingCursor[0], writingCursor[1], typingMsg[0]);
        writingCursor[0] = WIDTH/3+3+typingMsg[0].length();
        writingCursor[1] -= afterLines.size();
        Main.drawTextLeft(writingCursor[0], writingCursor[1], typingMsg[1]);

        updateCursor();
    }

    private static List<String> getLines(int maxLength, String message) {
        return getLines(0, maxLength, message);
    }
    private static List<String> getLines(int offset, int maxLength, String message) {
        List<String> messageLines = new ArrayList<>();
        List<String> messageWords = List.of(message.split(" "));

        String currentLine = "";
        for(String word : messageWords){
            if(word.length() + currentLine.length() + offset <= maxLength && !word.contains("⏎"))
                currentLine += word+" ";
            else {
                if(word.contains("⏎")) currentLine += word.substring(0, word.indexOf("⏎")+1);
                messageLines.add(currentLine);
                if(word.contains("⏎")) currentLine = word.substring(word.indexOf("⏎")+1)+" ";
                else currentLine = word+" ";
            }
            offset = 0;
        }
        if(!messageLines.contains(currentLine.trim())) messageLines.add(currentLine.trim());
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
        String topBubbleLine = "╭─";
        for (int j = 0; j < width+1; j++)
            topBubbleLine += "─";
        topBubbleLine += "╮";
        return topBubbleLine;
    }
    private String getBottomBubble(int width){
        String topBubbleLine = "╰─";
        for (int j = 0; j < width+1; j++)
            topBubbleLine += "─";
        topBubbleLine += "╯";
        return topBubbleLine;
    }
}
