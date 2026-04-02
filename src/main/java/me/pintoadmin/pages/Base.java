package me.pintoadmin.pages;

import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.screen.Screen;
import me.pintoadmin.*;

import java.io.IOException;

public class Base {
    private final TextGraphics g;
    private final Screen screen;
    public Base() throws IOException {
        this.g = Main.g;
        this.screen = Main.screen;
        createPage();
    }

    private void createPage() throws IOException {
        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        Main.drawBox(WIDTH/4,HEIGHT/4, (WIDTH-1)/4*3, (HEIGHT-1)/4*3);
        Main.drawText((WIDTH/2)-1, (HEIGHT/2)-1, "WELCOME TO TERMSG");

        screen.refresh();
    }
}
