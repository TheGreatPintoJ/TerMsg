package me.pintoadmin;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;
import me.pintoadmin.pages.*;

import java.io.*;

public class Main {
    public static TextColor BLACK = new TextColor.RGB(0,0,0);
    public static TextColor GREEN = new TextColor.RGB(0, 200, 0);

    public static TextColor TEXTCOLOR = GREEN;
    public static TextColor BACKGROUNDCOLOR = null;

    public static char HORIZONTAL = '─';
    public static char VERTICAL = '│';
    public static char TOPLEFT = '┌';
    public static char TOPRIGHT = '┐';
    public static char BOTTOMLEFT = '└';
    public static char BOTTOMRIGHT = '┘';

    public static Terminal terminal;
    public static Screen screen;
    public static TextGraphics g;

    public static ConfigLoader configLoader = new ConfigLoader();
    public static NetworkingManager netManager = new NetworkingManager();

    private enum OSTYPES {
        WINDOWS,
        MAC,
        LINUX
    }

    /*             ╭──────────────────────╮
    MESSAGEBUBBLE <╡ TEXT HERE            │
                   ╰──────────────────────╯
     */

    public static void main(String[] args) {
        File dataDir = new File(getDataDir());
        try {
            if(!dataDir.mkdir() && !dataDir.isDirectory())
                throw new RuntimeException("Could not create data directory");

            terminal = new DefaultTerminalFactory().createTerminal();
            screen = new TerminalScreen(terminal);
            screen.startScreen();

            g = screen.newTextGraphics();
            BLACK = g.getBackgroundColor();
            TEXTCOLOR = TextColor.ANSI.valueOf(configLoader.getTextColor().toUpperCase());
            g.setForegroundColor(TEXTCOLOR);

            screen.setCursorPosition(null);

            new Chat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException ignored) {}
        netManager.deinit();
    }

    public static void drawBox(int startX, int startY, int endX, int endY){
        g.drawLine(startX+1, startY, endX-1, startY, HORIZONTAL); // Top
        g.drawLine(startX+1, endY, endX-1, endY, HORIZONTAL); // Bottom
        g.drawLine(startX, startY+1, startX, endY-1, VERTICAL); // Left
        g.drawLine(endX, startY+1, endX, endY-1, VERTICAL); // Right

        setChar(startX, startY, TOPLEFT); // Top Left Corner
        setChar(endX, startY, TOPRIGHT); // Top Right Corner
        setChar(startX, endY, BOTTOMLEFT); // Bottom Left Corner
        setChar(endX, endY, BOTTOMRIGHT); // Bottom Right Corner
    }

    public static void drawText(int x, int y, String text){
        x -= (text.length()/2);
        for(char c : text.toCharArray()){
            g.setCharacter(x++, y, c);
        }
    }
    public static void drawTextLeft(int x, int y, String text){
        for(char c : text.toCharArray()){
            g.setCharacter(x++, y, c);
        }
    }
    public static void drawTextRight(int x, int y, String text){
        x -= text.length();
        for(char c : text.toCharArray()){
            g.setCharacter(x++, y, c);
        }
    }

    private static void setChar(int x, int y, char c){
        TextCharacter charred = g.getCharacter(x, y);
        if(charred == null) {
            g.setCharacter(x,y,c);
            return;
        }

        char existingChar = charred.getCharacterString().charAt(0);

        if(existingChar == HORIZONTAL) {
            if (c == TOPLEFT || c == TOPRIGHT) {
                g.setCharacter(x, y, '┬');
            } else if (c == BOTTOMLEFT || c == BOTTOMRIGHT) {
                g.setCharacter(x, y, '┴');
            }
        } else if(existingChar == VERTICAL){
            if (c == TOPLEFT || c == BOTTOMLEFT) {
                g.setCharacter(x, y, '├');
            } else if (c == TOPRIGHT || c == BOTTOMRIGHT) {
                g.setCharacter(x, y, '┤');
            }
        } else {
            g.setCharacter(x,y,c);
        }
    }
    public static void resetColors(TextGraphics g){
        g.setBackgroundColor(BACKGROUNDCOLOR);
        g.setForegroundColor(TEXTCOLOR);
    }
    public static void setTextColor(TextColor color){
        TEXTCOLOR = color;
    }
    public static void setBackgroundColor(TextColor color){
        BACKGROUNDCOLOR = color;
    }

    private static OSTYPES getOS(){
        String os = System.getProperty("os.name").toLowerCase();
        OSTYPES type = OSTYPES.LINUX;

        if (os.contains("win")) {
            type = OSTYPES.WINDOWS;
        } else if (os.contains("mac")) {
            type = OSTYPES.MAC;
        }

        return type;
    }
    public static String getDataDir(){
        return switch (getOS()) {
                    case LINUX -> System.getenv("HOME") + "/.local/share/termsg/";
                    case WINDOWS -> System.getenv("LOCALAPPDATA")+"/termsg/";
                    case MAC -> System.getProperty("user.home") + "/Library/Application Support/termsg/";
                };

    }
}