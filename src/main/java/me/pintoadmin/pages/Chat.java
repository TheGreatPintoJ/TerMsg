package me.pintoadmin.pages;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import me.pintoadmin.*;

public class Base {
    private final TextGraphics g;
    public Base(TextGraphics g){
        this.g = g;
        createPage();
    }

    private void createPage(){
        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        TextColor color = new TextColor.RGB(0, 200, 0);
        g.setForegroundColor(color);

        Main.drawBox(WIDTH/4,HEIGHT/4, (WIDTH-1)/4*3, (HEIGHT-1)/4*3, g);
        Main.drawText((WIDTH/2)-1, (HEIGHT/2)-1, "WELCOME TO TERMSG", g);
    }
}
