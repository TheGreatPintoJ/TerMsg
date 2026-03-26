package me.pintoadmin.pages;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import me.pintoadmin.Main;

import java.io.IOException;
import java.util.List;

public class Settings {
    private final Screen screen;
    private final TextGraphics g;
    public Settings(Screen screen, TextGraphics g) throws IOException {
        this.screen = screen;
        this.g = g;
        createPage();
    }

    private final List<String> menuEntries = List.of("Color", "Quit");
    private int selectedEntry = 0;
    private TerminalPosition oldPos;

    private void createPage() throws IOException {
        screen.clear();
        oldPos = screen.getCursorPosition();
        screen.setCursorPosition(null);

        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        g.setForegroundColor(Main.GREEN);
        g.setBackgroundColor(Main.BLACK);
        Main.drawBox(WIDTH/4, HEIGHT/4, 3*WIDTH/4, 3*HEIGHT/4, g);
        Main.drawText(WIDTH/2, HEIGHT/4+1, "SETTINGS", g);

        int idx = 1;
        for(String entry : menuEntries){
            if(idx-1 == selectedEntry){
                g.setForegroundColor(Main.TRUE_BLACK);
                g.setBackgroundColor(Main.GREEN);
            } else {
                g.setForegroundColor(Main.GREEN);
                g.setBackgroundColor(Main.BLACK);
            }
            Main.drawText(WIDTH/2, HEIGHT/4+2+(2*idx), " "+entry+" ", g);
            idx++;
        }

        screen.refresh();
        handleInput();
    }

    private void colorSelect() throws IOException {
        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        Main.drawBox(WIDTH/4,HEIGHT/4, (WIDTH-1)/4*3, (HEIGHT-1)/4*3, g);
        Main.drawText((WIDTH/2)-1, (HEIGHT/2)-1, "WELCOME TO TERMSG", g);

        screen.refresh();
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
                            break mainLoop;
                        case 1:
                            screen.close();
                            throw new RuntimeException("quit");
                    }
                case KeyType.Escape:
                    break mainLoop;
                default:
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break mainLoop;
                    }
            }
        }
        screen.setCursorPosition(oldPos);
    }
}
