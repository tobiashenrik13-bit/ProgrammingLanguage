import java.awt.*;
import javax.swing.*;

public class Game_Engine extends JPanel {

    static class Square {
        int x, y, width, height;

        Square(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    Square square1 = new Square(20, 20, 100, 100);

    public static void main(String[] args) {

        Game_Engine engine = new Game_Engine();

        JPanel box = new JPanel();
        box.setBackground(Color.red);
        box.setBounds(engine.square1.x, engine.square1.y, engine.square1.width, engine.square1.height);

        JFrame frame = new JFrame("JPanel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null); // set BEFORE adding components

        frame.add(box);
        frame.add(engine);

        engine.setBounds(0, 0, 300, 300);

        frame.setSize(300, 300);
        frame.setVisible(true);
    }
}
