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

    private List<String> contacts = List.of("Person 1", "Person 2");

    private void createPage(){
        g.fill(' '); // Reset Screen

        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();


        Main.drawBox(0,0, WIDTH/3, HEIGHT-1, g);
        Main.drawBox(WIDTH/3+1, 0, WIDTH-1, HEIGHT-1, g);

        int idx = 1;
        for(String entry : contacts){
            Main.drawTextLeft(3, idx*3, entry, g);
            g.drawLine(1, idx*3+2, WIDTH/3-1, idx*3+2, '-');
            idx++;
        }

//        Main.drawText((WIDTH/2)-1, (HEIGHT/2)-1, "Chat", g);
    }
}
