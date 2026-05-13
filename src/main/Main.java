package main;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // creating window
        JFrame window = new JFrame("Chess Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false); // we don't want it to resizable so we made it false

        // add gamepanel to the window
        GamePanel gp = new GamePanel(); // instantiating the GamePanel as a "gp"
        window.add(gp); // adding it to the window and packing it
        window.pack(); // by packing like this the window adjust its size to this gp

        window.setLocationRelativeTo(null); // for make it pop out to the center of the monitor we said null
        window.setVisible(true); // for us to see the JFrame window

        gp.launchGame(); // once the window is created, the program will call this method
        // and start the thread after that call run method

    }
}