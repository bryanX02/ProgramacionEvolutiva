package controlador;

import javax.swing.SwingUtilities;

import vista.JFramePoblacionRobot;


// Esta clase es la encargada de arrancar el programa y controlarlo
public class ControladorRobot {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
            JFramePoblacionRobot frame = new JFramePoblacionRobot();
            frame.setVisible(true);
        });

	}

}
