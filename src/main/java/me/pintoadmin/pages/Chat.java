package me.pintoadmin.pages;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import me.pintoadmin.*;

import java.util.*;

public class Chat {
    private final TextGraphics g;
    public Chat(TextGraphics g){
        this.g = g;
        createPage();
    }

    private List<String> contacts = List.of("Person 1", "Person 2", "Person 3");

    private int selectedContact;

    private void createPage(){
        g.fill(' '); // Reset Screen

        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        Main.drawBox(0,0, WIDTH/3, HEIGHT-1, g);
        Main.drawBox(WIDTH/3+1, 2, WIDTH-1, HEIGHT-1, g);

        Main.drawText(WIDTH/6, 1,  "CONTACTS", g);
        g.drawLine(1, 2, WIDTH/3-1, 2, Main.HORIZONTAL);

        int idx = 2;
        for(String entry : contacts){
            Main.drawTextLeft(3, idx*2-1, entry, g);
            g.drawLine(1, idx*2, WIDTH/3-1, idx*2, Main.HORIZONTAL);
            idx++;
        }

        JsonParser parser = new JsonParser();
        int leftMsgX = WIDTH/3+2;
        int yLevel = HEIGHT-3;

        for(String line : parser.getLines("localhost")){
            String topBubbleLine = "╭";
            for(int i = 0; i < line.length(); i++)
                topBubbleLine +="─";
            topBubbleLine+="╮";

            String bottomBubbleLine = "╰";
            for(int i = 0; i < line.length(); i++)
                bottomBubbleLine +="─";
            bottomBubbleLine+="╯";

            Main.drawTextLeft(leftMsgX+1, yLevel-1, topBubbleLine, g);
            Main.drawTextLeft(leftMsgX, yLevel, "<╡"+line+"│", g);
            Main.drawTextLeft(leftMsgX+1, yLevel+1, bottomBubbleLine, g);

            yLevel -= 3;
        }
    }
}
