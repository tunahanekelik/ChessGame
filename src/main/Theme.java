package main;

import java.awt.Color;

public class Theme {

    public String name;
    public Color lightSquare;
    public Color darkSquare;

    public Theme(String name, Color light, Color dark) {
        this.name = name;
        this.lightSquare = light;
        this.darkSquare = dark;
    }

    // colour schemes for the board
    public static final Theme[] BOARD_THEMES = {
        new Theme("Classic", new Color(210, 165, 125), new Color(175, 115, 70)),
        new Theme("Ocean",   new Color(200, 210, 180), new Color(100, 150, 180)),
        new Theme("Forest",  new Color(200, 210, 180), new Color(100, 160, 100)),
        new Theme("Desert",  new Color(230, 215, 180), new Color(200, 170, 110)),
        new Theme("Midnight",new Color(70, 70, 90),    new Color(40, 40, 60)),
    };
}
