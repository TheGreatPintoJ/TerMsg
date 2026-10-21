package me.pintoadmin.pages;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import me.pintoadmin.Main;
import org.w3c.dom.*;

import java.io.*;
import java.util.*;

public class Settings {
    private final Screen screen;
    private final TextGraphics g;
    private final File configFile;
    public Settings() throws IOException {
        this.screen = Main.screen;
        this.g = Main.g;
        configFile = new File(Main.getDataDir()+".config.yml");
        createPage();
    }

    private final Map<String, TextColor> colorOptions = Map.of(
            "Green", TextColor.ANSI.GREEN,
            "Blue", TextColor.ANSI.BLUE,
            "Red", TextColor.ANSI.RED,
            "White", TextColor.ANSI.WHITE
    );
    private int selectedColorEntry = -1;
    private boolean textColorSelected = true;

    private boolean portSaveSelected = true;

    private final List<String> menuEntries = List.of("Color", "Port", "Quit");
    private int selectedEntry = 0;
    private TerminalPosition oldPos;

    private void createPage() throws IOException {
        screen.clear();
        oldPos = screen.getCursorPosition();
        screen.setCursorPosition(null);

        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        g.setForegroundColor(Main.TEXTCOLOR);
        g.setBackgroundColor(Main.BACKGROUNDCOLOR);
        Main.drawBox(WIDTH/4, HEIGHT/4, 3*WIDTH/4, 3*HEIGHT/4);
        Main.drawText(WIDTH/2, HEIGHT/4+1, "SETTINGS");

        int idx = 1;
        for(String entry : menuEntries){
            if(idx-1 == selectedEntry){
                g.setForegroundColor(Main.BLACK);
                g.setBackgroundColor(Main.TEXTCOLOR);
            } else {
                g.setForegroundColor(Main.TEXTCOLOR);
                g.setBackgroundColor(Main.BACKGROUNDCOLOR);
            }
            Main.drawText(WIDTH/2, HEIGHT/4+2+(2*idx), " "+entry+" ");
            idx++;
        }

        screen.refresh();
        handleInput();
    }

    private void colorSelect() throws IOException {
        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        screen.clear();

        //Main.drawBox(WIDTH/4,HEIGHT/4, (WIDTH-1)/4*3, (HEIGHT-1)/4*3);
        Main.drawText((WIDTH/2)-1, (HEIGHT/3)-1, "TEXT COLOR");
        int index = 1;
        for(Map.Entry<String, TextColor> colorEntry : colorOptions.entrySet()){
            String name = colorEntry.getKey();
            TextColor color = colorEntry.getValue();

            g.setForegroundColor(color);
            Main.drawText((WIDTH/2)-1, (HEIGHT/3)+index, name);

            index++;
        }

        Main.resetColors(g);

        if(selectedColorEntry != -1)
            g.setCharacter((WIDTH/2)-5,
                    textColorSelected ? (HEIGHT/3)+selectedColorEntry+1
                                      : (HEIGHT/3)+selectedColorEntry+colorOptions.size()+4,
                    '>');

        screen.refresh();
        handleColorInput();
    }
    private void handleColorInput() throws IOException {
        mainLoop:
        while (true){
            KeyStroke key = screen.pollInput();
            if(key == null) continue;
            if(selectedColorEntry == -1) selectedColorEntry = 0;
            switch (key.getKeyType()) {
                case KeyType.ArrowDown:
                    if(selectedColorEntry < colorOptions.size()-1) selectedColorEntry++;
//                    else {
//                        textColorSelected = false;
//                        selectedColorEntry = 0;
//                    }
                    colorSelect();
                    break mainLoop;
                case KeyType.ArrowUp:
                    if(selectedColorEntry > 0) selectedColorEntry--;
//                    else {
//                        textColorSelected = true;
//                        selectedColorEntry = colorOptions.size()-1;
//                    }
                    colorSelect();
                    break mainLoop;

                case KeyType.Enter:
                    // save color selection
                    String name = new ArrayList<>(colorOptions.keySet()).get(selectedColorEntry);
                    TextColor color = new ArrayList<>(colorOptions.values()).get(selectedColorEntry);
                    if(textColorSelected) Main.setTextColor(color);
                    else Main.setBackgroundColor(name.equals("Black") ? color : TextColor.ANSI.BLACK);
                    Main.configLoader.setTextColor(name);
                    break mainLoop;
                case KeyType.Escape:
                    break mainLoop;
            }
        }
    }

    private void portSelect() throws IOException {
        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        screen.clear();
        Main.resetColors(g);

        Main.drawText((WIDTH/2)-1, (HEIGHT/3)-1, "LISTENING PORT");
        Main.drawBox(WIDTH/4, HEIGHT/4, WIDTH*3/4, HEIGHT*3/4);

        if(!portSaveSelected) {
            g.setForegroundColor(Main.BACKGROUNDCOLOR);
            g.setBackgroundColor(Main.TEXTCOLOR);
        }
        Main.drawBox(WIDTH/2-4, HEIGHT/4+5, WIDTH/2+3, HEIGHT/4+7);

        Main.resetColors(g);
        Main.drawTextLeft(WIDTH/2-3, HEIGHT/4+6, String.valueOf(NetworkingManager.PORT));

        if(portSaveSelected){
            g.setForegroundColor(Main.BACKGROUNDCOLOR);
            g.setBackgroundColor(Main.TEXTCOLOR);
        }
        Main.drawText(WIDTH/2, HEIGHT/2+4,"SAVE");

        Main.resetColors(g);

        screen.refresh();
        handlePortInput();
    }
    private void handlePortInput() throws IOException {
        mainLoop:
        while (true){
            KeyStroke key = screen.pollInput();
            if(key == null) continue;
            if(selectedColorEntry == -1) selectedColorEntry = 0;
            switch (key.getKeyType()) {
                case KeyType.Tab, KeyType.ReverseTab:
                    portSaveSelected = !portSaveSelected;
                    portSelect();
                    break;
                case KeyType.ArrowUp:
                    if(portSaveSelected) break;
                    if(NetworkingManager.PORT <= 65535) NetworkingManager.PORT++;
                    portSelect();
                    break mainLoop;
                case KeyType.ArrowDown:
                    if(portSaveSelected) break;
                    if(NetworkingManager.PORT >= 1) NetworkingManager.PORT--;
                    portSelect();
                    break mainLoop;
                case KeyType.Enter:
                    // save color selection
//                    ConfigLoader.getInstance().savePort();
                    NetworkingManager.getInstance().reload();
                    break mainLoop;
                case KeyType.Escape:
                    break mainLoop;
            }
        }
    }

    private void handleInput() throws IOException {
        mainLoop:
        while(true) {
            KeyStroke key = screen.pollInput();
            if(key == null) continue;
            switch (key.getKeyType()) {
                case KeyType.ArrowDown:
                    if(selectedEntry < menuEntries.size()-1) selectedEntry++;
                    createPage();
                    break mainLoop;
                case KeyType.ArrowUp:
                    if(selectedEntry > 0) selectedEntry--;
                    createPage();
                    break mainLoop;
                case KeyType.Character:
                    if(key.getCharacter() == 'q') {
                        screen.close();
                        throw new RuntimeException("quit");
                    }

                case KeyType.Enter:
                    switch (selectedEntry){
                        case 0:
                            colorSelect();
                            createPage();
                            break mainLoop;
                        case 1:
                            portSelect();
                            break mainLoop;
                        case 2:
                            screen.close();
                            throw new RuntimeException("quit");
                    }
                case KeyType.Escape:
                    break mainLoop;
            }
        }
        screen.setCursorPosition(oldPos);
    }
}
