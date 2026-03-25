package me.pintoadmin.pages;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;
import me.pintoadmin.*;

import java.io.*;

public class NewChat {
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
    private int[] cursorPos = {-1,-1};

    private String host = "";
    private String port = "1337";
    private String name = "";

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
        Main.drawText(WIDTH/2, 2, "NEW MESSAGE", g);

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
        if(!host.isBlank()) Main.drawTextLeft(leftBoxEdge+1, currentY, host, g);
        if(selectedBox == boxes.HOST) {
            cursorPos[0] = leftBoxEdge+1+host.length();
            cursorPos[1] = currentY;
        }

        // Draw "Port" box
        currentY += boxYGap;
        Main.drawTextLeft(leftTextEdge, currentY, "Port", g);
        Main.drawBox(leftBoxEdge, currentY-1, WIDTH-3, currentY+1, g);
        if(!port.isBlank()) Main.drawTextLeft(leftBoxEdge+1, currentY, port, g);
        if(selectedBox == boxes.PORT) {
            cursorPos[0] = leftBoxEdge+1+port.length();
            cursorPos[1] = currentY;
        }

        // Draw "Name" box
        currentY += boxYGap;
        Main.drawTextLeft(leftTextEdge, currentY, "Name", g);
        Main.drawBox(leftBoxEdge, currentY-1, WIDTH-3, currentY+1, g);
        if(!name.isBlank()) Main.drawTextLeft(leftBoxEdge+1, currentY, name, g);
        if(selectedBox == boxes.NAME) {
            cursorPos[0] = leftBoxEdge+1+name.length();
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
        mainLoop:
        while(true) {
            KeyStroke key = screen.pollInput();
            if (key == null) continue;
            if (key.getKeyType() == KeyType.Escape) {
                screen.setCursorPosition(null);
                break;
            }
            if (key.getKeyType() == KeyType.ArrowDown || key.getKeyType() == KeyType.Tab){
                if(selectedBox == boxes.HOST) selectedBox = boxes.PORT;
                else if(selectedBox == boxes.PORT) selectedBox = boxes.NAME;
                else if(selectedBox == boxes.NAME) selectedBox = boxes.SAVE;
                updateInputs();
            } else if(key.getKeyType() == KeyType.ArrowUp || key.getKeyType() == KeyType.ReverseTab){
                if(selectedBox == boxes.SAVE) selectedBox = boxes.NAME;
                else if(selectedBox == boxes.NAME) selectedBox = boxes.PORT;
                else if(selectedBox == boxes.PORT) selectedBox = boxes.HOST;
                updateInputs();
            } else if(key.getKeyType() == KeyType.Enter && selectedBox == boxes.SAVE){
                // SAVE LOGIC
                MessageManager.getInstance().newChat(host, port, name);
                break;
            } else if(key.getKeyType() == KeyType.Character){
                char c = key.getCharacter();
                if(selectedBox == boxes.HOST) host += c;
                if(selectedBox == boxes.PORT) port += c;
                if(selectedBox == boxes.NAME) name += c;
                g.setCharacter(cursorPos[0], cursorPos[1], c);
                cursorPos[0]++;
                screen.setCursorPosition(new TerminalPosition(cursorPos[0], cursorPos[1]));
                screen.refresh();
            } else if(key.getKeyType() == KeyType.Backspace && selectedBox != boxes.SAVE){
                if(selectedBox == boxes.HOST) host = host.substring(0, host.length()-1);
                if(selectedBox == boxes.PORT) port = port.substring(0, port.length()-1);
                if(selectedBox == boxes.NAME) name = name.substring(0, name.length()-1);
                g.setCharacter(cursorPos[0]-1, cursorPos[1], ' ');
                cursorPos[0]--;
                screen.setCursorPosition(new TerminalPosition(cursorPos[0], cursorPos[1]));
                screen.refresh();
            }
        }
    }
}
