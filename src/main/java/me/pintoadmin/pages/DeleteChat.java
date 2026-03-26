package me.pintoadmin.pages;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import me.pintoadmin.Main;
import me.pintoadmin.MessageManager;

import java.io.IOException;

public class DeleteChat {
    private final TextGraphics g;
    private final Screen screen;
    private final String host;
    public DeleteChat(Screen screen, TextGraphics g, String hostToDelete) throws IOException {
        this.g = g;
        this.screen = screen;
        host = hostToDelete;
        createPage();
    }

    private boolean yesSelected = false;

    private void createPage() throws IOException {
        int WIDTH = g.getSize().getColumns();
        int HEIGHT = g.getSize().getRows();

        screen.clear();
        Main.resetColors(g);

        Main.drawBox(WIDTH/4,HEIGHT/4, (WIDTH-1)/4*3, (HEIGHT-1)/4*3, g);
        Main.drawText(WIDTH/2, (HEIGHT/2)-4, "CONFIRM", g);
        Main.drawText(WIDTH/2, (HEIGHT/2)-3, "DELETE \""+host+"\"?", g);

        if(!yesSelected){
            g.setForegroundColor(Main.BLACK);
            g.setBackgroundColor(Main.GREEN);
        }
        Main.drawTextRight(WIDTH/2-3, HEIGHT/2, "  NO ", g);
        if(yesSelected){
            g.setForegroundColor(Main.BLACK);
            g.setBackgroundColor(Main.GREEN);
        } else Main.resetColors(g);
        Main.drawTextLeft(WIDTH/2+3, HEIGHT/2, " YES ", g);

        screen.refresh();
        handleInput();
    }

    private void handleInput() throws IOException {
        while (true){
            KeyStroke key = screen.pollInput();
            if(key == null) continue;
            if(key.getKeyType() == KeyType.ArrowRight){
                yesSelected = true;
                createPage();
                break;
            } else if(key.getKeyType() == KeyType.ArrowLeft){
                yesSelected = false;
                createPage();
                break;
            } else if(key.getKeyType() == KeyType.Enter){
                if(yesSelected) MessageManager.getInstance().removeChat(host);
                break;
            }
        }
    }
}
