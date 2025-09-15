package vista;

import javax.swing.*;
import java.awt.*;

public class MapaPanel extends JPanel {
    private String[][] mapa;

    public MapaPanel(String[][] mapa) {
        this.mapa = mapa;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cellSize = 30; // Tamaño de cada celda en píxeles

        for (int i = 0; i < mapa.length; i++) {
            for (int j = 0; j < mapa[i].length; j++) {
                // Dibujar celda
                g.setColor(Color.WHITE);
                g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);

                // Dibujar contenido de la celda
                if (mapa[i][j].trim().equals("■")) {
                    g.setColor(Color.BLACK);
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                } else if (mapa[i][j].trim().equals("*")) {
                    g.setColor(Color.BLUE);
                    g.fillOval(j * cellSize, i * cellSize, cellSize, cellSize);
                } else if (!mapa[i][j].trim().isEmpty()) {
                    g.setColor(Color.RED);
                    g.drawString(mapa[i][j].trim(), j * cellSize + 10, i * cellSize + 20);
                }
            }
        }
    }
}