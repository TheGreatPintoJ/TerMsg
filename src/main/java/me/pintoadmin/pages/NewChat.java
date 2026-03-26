package me.pintoadmin.pages;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;
import me.pintoadmin.*;

import java.io.*;

public class NewChat {
    private final MessageManager messageManager = MessageManager.getInstance();
    private final Terminal terminal;
    private final Screen screen;
    private final TextGraphics g;
    private final int WIDTH;
    private final int HEIGHT;
    public NewChat(Terminal terminal, Screen screen, TextGraphics g) throws IOException {
        this.terminal = terminal;
        this.screen = screen;
        this.g = g;
        WIDTH = g.getSize().getColumns();
        HEIGHT = g.getSize().getRows();
        createPage();
    }
    private String oldHost = "";
    private String[] host = {"", ""};
    private String[] port = {"51337", ""};
    private String[] name = {"", ""};
    private boolean editMode = false;
    public NewChat(Terminal terminal, Screen screen, TextGraphics g, String host) throws IOException {
        this.terminal = terminal;
        this.screen = screen;
        this.g = g;
        oldHost = host;
        this.host[0] = host;
        this.port[0] = messageManager.getPort(host) != null ? messageManager.getPort(host) : port[0];
        this.name[0] = messageManager.getName(host) != null ? messageManager.getName(host) : host;
        editMode = true;
        WIDTH = g.getSize().getColumns();
        HEIGHT = g.getSize().getRows();
        createPage();
    }
    private int[] cursorPos = {-1,-1};

    private enum boxes {
        HOST,
        PORT,
        NAME,
        SAVE
    }
    private boxes selectedBox = boxes.HOST;

    private void createPage() throws IOException {
        screen.clear();
        screen.setCursorPosition(null);

        g.setForegroundColor(Main.GREEN);
        g.setBackgroundColor(Main.BLACK);
        g.fill(' '); // Reset Screen

        Main.drawBox(1, 0, WIDTH-1, HEIGHT-1, g);
        Main.drawText(WIDTH/2, 2, editMode ? "EDIT CONTACT" : "NEW CHAT", g);

        updateInputs();
        handleInput();
    }
    private void updateInputs() throws IOException {
        screen.setCursorPosition(new TerminalPosition(cursorPos[0], cursorPos[1]));
        g.setForegroundColor(Main.GREEN);
        g.setBackgroundColor(Main.BLACK);

        int leftBoxEdge = 11;
        int leftTextEdge = 3;
        int boxYOffset = 3;
        int boxYGap = 4;

        int currentY = boxYOffset+boxYGap;
        // Draw "Host" box
        Main.drawTextLeft(leftTextEdge, currentY, "Host", g);
        Main.drawBox(leftBoxEdge, currentY-1, WIDTH-3, currentY+1, g);
        String fullHost = host[0]+host[1];
        if(!fullHost.isBlank()) Main.drawTextLeft(leftBoxEdge+1, currentY, fullHost, g);
        if(selectedBox == boxes.HOST) {
            cursorPos[0] = leftBoxEdge+1+fullHost.length();
            cursorPos[1] = currentY;
        }

        // Draw "Port" box
        currentY += boxYGap;
        Main.drawTextLeft(leftTextEdge, currentY, "Port", g);
        Main.drawBox(leftBoxEdge, currentY-1, WIDTH-3, currentY+1, g);
        String fullPort = port[0]+port[1];
        if(!fullPort.isBlank()) Main.drawTextLeft(leftBoxEdge+1, currentY, fullPort, g);
        if(selectedBox == boxes.PORT) {
            cursorPos[0] = leftBoxEdge+1+fullPort.length();
            cursorPos[1] = currentY;
        }

        // Draw "Name" box
        currentY += boxYGap;
        Main.drawTextLeft(leftTextEdge, currentY, "Name", g);
        Main.drawBox(leftBoxEdge, currentY-1, WIDTH-3, currentY+1, g);
        String fullName = name[0]+name[1];
        if(!fullName.isBlank()) Main.drawTextLeft(leftBoxEdge+1, currentY, fullName, g);
        if(selectedBox == boxes.NAME) {
            cursorPos[0] = leftBoxEdge+1+fullName.length();
            cursorPos[1] = currentY;
        }

        // Draw "Save" button
        currentY += boxYGap;
        if(selectedBox == boxes.SAVE){
            screen.setCursorPosition(null);
            g.setForegroundColor(Main.BLACK);
            g.setBackgroundColor(Main.GREEN);
        } else screen.setCursorPosition(new TerminalPosition(cursorPos[0], cursorPos[1]));
        Main.drawText(WIDTH/2, currentY, " SAVE ", g);

        screen.refresh();
    }
    private void handleInput() throws IOException {
        while (true) {
            KeyStroke key = screen.pollInput();
            if (key == null) continue;
            if (key.getKeyType() == KeyType.Escape) {
                screen.setCursorPosition(null);
                break;
            }
            if (key.getKeyType() == KeyType.ArrowDown || key.getKeyType() == KeyType.Tab) {
                if (selectedBox == boxes.HOST) selectedBox = boxes.PORT;
                else if (selectedBox == boxes.PORT) selectedBox = boxes.NAME;
                else if (selectedBox == boxes.NAME) selectedBox = boxes.SAVE;
                updateInputs();
            } else if (key.getKeyType() == KeyType.ArrowUp || key.getKeyType() == KeyType.ReverseTab) {
                if (selectedBox == boxes.SAVE) selectedBox = boxes.NAME;
                else if (selectedBox == boxes.NAME) selectedBox = boxes.PORT;
                else if (selectedBox == boxes.PORT) selectedBox = boxes.HOST;
                updateInputs();
            } else if (key.getKeyType() == KeyType.Enter && selectedBox == boxes.SAVE) {
                String fullHost = host[0]+host[1];
                String fullPort = port[0]+port[1];
                String fullName = name[0]+name[1];

                if(editMode)
                    messageManager.editChat(oldHost, fullHost, fullPort, fullName);
                else messageManager.newChat(fullHost, fullPort, fullName);
                break;
            } else {
                String[] typingMsg =
                    switch (selectedBox){
                        case HOST -> host;
                        case PORT -> port;
                        case NAME -> name;
                        case SAVE -> new String[2];
                    };
                if (key.getKeyType() == KeyType.ArrowLeft) {
                    if (!typingMsg[0].isEmpty()) {
                        typingMsg[1] = typingMsg[0].charAt(typingMsg[0].length() -1) + typingMsg[1];
                        typingMsg[0] = typingMsg[0].substring(0, typingMsg[0].length() - 1);

                        cursorPos[0]--;
                        updateCursor();
                        screen.refresh();
                    }
                } else if (key.getKeyType() == KeyType.ArrowRight) {
                    if (!typingMsg[1].isEmpty()) {
                        typingMsg[0] += typingMsg[1].charAt(0);
                        typingMsg[1] = typingMsg[1].substring(1);

                        cursorPos[0]++;
                        updateCursor();
                        screen.refresh();
                    }
                } else if (key.getKeyType() == KeyType.Character) {
                    g.setCharacter(cursorPos[0], cursorPos[1], key.getCharacter());
                    cursorPos[0]++;
                    updateCursor();
                    Main.drawTextLeft(cursorPos[0], cursorPos[1], typingMsg[1], g);
                    screen.refresh();
                    typingMsg[0] += key.getCharacter();
                } else if (key.getKeyType() == KeyType.Backspace && selectedBox != boxes.SAVE) {
                    g.setCharacter(cursorPos[0] - 1, cursorPos[1], ' ');
                    if (!typingMsg[0].isEmpty()) {
                        cursorPos[0]--;
                        updateCursor();
                        typingMsg[0] = typingMsg[0].substring(0, typingMsg[0].length() - 1);
                    }
                    Main.drawTextLeft(cursorPos[0], cursorPos[1], typingMsg[1]+" ", g);
                    screen.refresh();
                }
            }
        }
    }
    private void updateCursor(){
        screen.setCursorPosition(new TerminalPosition(cursorPos[0], cursorPos[1]));
    }
}
