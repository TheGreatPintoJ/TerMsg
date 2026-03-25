package me.pintoadmin;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;
import me.pintoadmin.pages.*;
import org.w3c.dom.Text;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.Set;

public class Main {
    public static TextColor BLACK = new TextColor.RGB(0, 0, 0);
    public static TextColor TRUE_BLACK = new TextColor.RGB(0, 0, 0);
    public static TextColor GREEN = new TextColor.RGB(0, 200, 0);

    public static char HORIZONTAL = '─';
    public static char VERTICAL = '│';
    public static char TOPLEFT = '┌';
    public static char TOPRIGHT = '┐';
    public static char BOTTOMLEFT = '└';
    public static char BOTTOMRIGHT = '┘';

    public enum OSTYPES {
        WINDOWS,
        MAC,
        LINUX
    };

    /*             ╭──────────────────────╮
    MESSAGEBUBBLE <╡ TEXT HERE            │
                   ╰──────────────────────╯
     */

    public static void main(String[] args) {
        File dataDir = new File(getDataDir());
        try {
            if(!dataDir.mkdir() && !dataDir.isDirectory())
                throw new RuntimeException("Could not create data directory");

            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();

            TextGraphics g = screen.newTextGraphics();
            BLACK = g.getBackgroundColor();
            g.setForegroundColor(GREEN);

            screen.setCursorPosition(null);

            new Chat(terminal, screen, g);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException ignored){}
    }

    public static void drawBox(int startX, int startY, int endX, int endY, TextGraphics g){
        g.drawLine(startX+1, startY, endX-1, startY, HORIZONTAL); // Top
        g.drawLine(startX+1, endY, endX-1, endY, HORIZONTAL); // Bottom
        g.drawLine(startX, startY+1, startX, endY-1, VERTICAL); // Left
        g.drawLine(endX, startY+1, endX, endY-1, VERTICAL); // Right

        setChar(startX, startY, TOPLEFT, g); // Top Left Corner
        setChar(endX, startY, TOPRIGHT, g); // Top Right Corner
        setChar(startX, endY, BOTTOMLEFT, g); // Bottom Left Corner
        setChar(endX, endY, BOTTOMRIGHT, g); // Bottom Right Corner
    }

    public static void drawText(int x, int y, String text, TextGraphics g){
        x -= (text.length()/2);
        for(char c : text.toCharArray()){
            g.setCharacter(x++, y, c);
        }
    }
    public static void drawTextLeft(int x, int y, String text, TextGraphics g){
        for(char c : text.toCharArray()){
            g.setCharacter(x++, y, c);
        }
    }
    public static void drawTextRight(int x, int y, String text, TextGraphics g){
        x -= text.length();
        for(char c : text.toCharArray()){
            g.setCharacter(x++, y, c);
        }
    }

    private static  void setChar(int x, int y, char c, TextGraphics g){
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

    public static OSTYPES getOS(){
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
                    case WINDOWS -> System.getenv("LOCALAPPDATA")+"/termsg";
                    case MAC -> System.getProperty("user.home") + "/Library/Application Support/";
                };

    }
}