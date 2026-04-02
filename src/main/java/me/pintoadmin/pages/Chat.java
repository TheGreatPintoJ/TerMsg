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
    public Chat(Screen screen, TextGraphics g) throws IOException {
        this.screen = screen;
        this.g = g;
        WIDTH = g.getSize().getColumns();
        HEIGHT = g.getSize().getRows();
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
    private List<String> beforeLines = new ArrayList<>();
    private List<String> afterLines = new ArrayList<>();

    private void createPage() throws IOException {
        createPage(0);
        handleInput();
    }
    private void createPage(int offset) throws IOException {
        screen.clear();

        g.setForegroundColor(Main.GREEN);
        g.setBackgroundColor(Main.BLACK);
        g.fill(' '); // Reset Screen

        Main.drawBox(0,0, WIDTH/3, HEIGHT-1, g);
        Main.drawBox(WIDTH/3+1, 2, WIDTH-1, HEIGHT-1, g);

        Main.drawText(WIDTH/6, 1,  "CONTACTS", g);
        g.drawLine(1, 2, WIDTH/3-1, 2, Main.HORIZONTAL);

        g.drawLine(WIDTH/3+2, HEIGHT-3-offset, WIDTH-2, HEIGHT-3-offset, '=');

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
        contactMap.clear();
        File[] files = new File(Main.getDataDir()).listFiles();
        if(files != null) {
            for (File file : files) {
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

        updateMessages(offset);
        screen.refresh();
    }
    private void updateMessages(int offset) throws IOException {
        MessageManager msgMan = MessageManager.getInstance();
        int leftMsgX = WIDTH/3+2;
        int rightMsgX = WIDTH-2;
        int yLevel = HEIGHT-5-offset;
        int maxHeight = 3;
        writingCursor[0] = WIDTH/3+3;
        writingCursor[1] = HEIGHT-2;

        if(selectedContact != -1){
            Main.drawText(2*WIDTH/3, 1, contacts.get(selectedContact), g);

            JSONArray messages = msgMan.getMsgs(contactMap.get(contacts.get(selectedContact)));

            for (int i = 0; i < messages.length(); i++) {
                JSONObject msg = messages.getJSONObject(i);
                String message = msg.getString("message");
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
                            Main.drawTextLeft(leftMsgX, yLevel, "<╡ " + line +whitespace(endSpace)+"│", g);
                        else Main.drawTextLeft(leftMsgX+1, yLevel-index, "│ " + line +whitespace(endSpace)+"│", g);
                        index--;
                    }
                    Main.drawTextLeft(leftMsgX + 1, yLevel + 1, getBottomBubble(longest), g);
                } else {
                    Main.drawTextRight(rightMsgX - 1, yLevel - messageLines.size(), getTopBubble(longest), g);
                    int index = messageLines.size()-1;
                    for(String line : messageLines) {
                        int endSpace = longest-line.length();
                        if(line.equals(messageLines.getLast()))
                            Main.drawTextRight(rightMsgX, yLevel, "│ " + line +whitespace(endSpace)+"╞>", g);
                        else Main.drawTextRight(rightMsgX-1, yLevel-index, "│ " + line +whitespace(endSpace)+"│", g);
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
            if (key.getKeyType() == KeyType.Escape) {
                new Settings(screen, g);
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
                } else if (key.getKeyType() == KeyType.Character) {
                    if(writingCursor[0] < WIDTH-3) {
                        g.setCharacter(writingCursor[0], writingCursor[1], key.getCharacter());
                        writingCursor[0]++;
                        updateCursor();
                        Main.drawTextLeft(writingCursor[0], writingCursor[1], typingMsg[1], g);
                        typingMsg[0] += key.getCharacter();
                    } else {
                        typingMsg[0] += key.getCharacter();
//                      split last word
                        String lastWord = "";
                        if(key.getCharacter() != ' ') {
                            lastWord = Arrays.stream(typingMsg[0].split(" ")).toList().getLast();
                            typingMsg[0] = typingMsg[0].substring(0, typingMsg[0].length() - lastWord.length()); // crop by last word length
                        }
                        beforeLines.add(typingMsg[0]);
                        typingMsg[0] = lastWord;
                        int numLines = beforeLines.size();
//                      bump messages up
                        createPage(numLines);
//                      bump typingmsg up
                        for(String line : beforeLines){
                            Main.drawTextLeft(writingCursor[0], writingCursor[1]-(numLines-beforeLines.indexOf(line)), line, g);
                        }
                        if(key.getCharacter() != ' ') {
                            Main.drawTextLeft(writingCursor[0], writingCursor[1], typingMsg[0], g);
//                          reset cursor to word length
                            writingCursor[0] += typingMsg[0].length();
                        }
                    }
                    updateCursor();
                    screen.refresh();
                } else if (key.getKeyType() == KeyType.Backspace) {
                    if (!typingMsg[0].isEmpty()) {
                        g.setCharacter(writingCursor[0] - 1, writingCursor[1], ' ');
                        writingCursor[0]--;
                        updateCursor();
                        typingMsg[0] = typingMsg[0].substring(0, typingMsg[0].length() - 1);
                    } else {
                        int numLines = beforeLines.size();
                        typingMsg[0] = beforeLines.getLast().substring(0, beforeLines.getLast().length()-1);
                        beforeLines.removeLast();
                        createPage(numLines-1);

                        for(String line : beforeLines){
                            Main.drawTextLeft(writingCursor[0], writingCursor[1]-(numLines-1-beforeLines.indexOf(line)), line, g);
                        }
                        Main.drawTextLeft(writingCursor[0], writingCursor[1], typingMsg[0], g);
                        writingCursor[0] = WIDTH/3+3+typingMsg[0].length();
                        updateCursor();
                        screen.refresh();
                    }
                    screen.refresh();
                } else if (key.getKeyType() == KeyType.Enter) {
                    List<String> allLines = beforeLines;
                    allLines.add(typingMsg[0]+typingMsg[1]);
                    allLines.addAll(afterLines);
                    String fullMsg = "";
                    for(String line : allLines) fullMsg += line+" ";
                    MessageManager.getInstance().sendMessage(fullMsg, contactMap.get(contacts.get(selectedContact)));
                    typingMsg = new String[]{"", ""};
                    writingCursor = new int[]{WIDTH / 3 + 3, HEIGHT - 2};
                    updateCursor();
                    createPage();
                    break;
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
                    new NewChat(screen, g);
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
                        updateCursor();
                        updateMessages(0);
                        screen.refresh();
                        break;
                    case KeyType.Character:
                        if (key.getCharacter() == 'q') {
                            screen.close();
                            throw new RuntimeException("quit");
                        } else if(key.getCharacter() == 'e'){
                            new NewChat(screen, g, contactMap.get(contacts.get(selectedContact)));
                            createPage();
                        } else if(key.getCharacter() == 'd'){
                            new DeleteChat(screen, g, contactMap.get(contacts.get(selectedContact)));
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
        String topBubbleLine = "╭─";
        for (int j = 0; j < width; j++)
            topBubbleLine += "─";
        topBubbleLine += "╮";
        return topBubbleLine;
    }
    private String getBottomBubble(int width){
        String topBubbleLine = "╰─";
        for (int j = 0; j < width; j++)
            topBubbleLine += "─";
        topBubbleLine += "╯";
        return topBubbleLine;
    }
}
