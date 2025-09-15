package controlador;

import javax.swing.SwingUtilities;

import vista.JFramePoblacionHormiga;


// Esta clase es la encargada de arrancar el programa y controlarlo
public class ControladorHormiga {

	public static void main(String[] args) {

		// Es importante ejecutar la GUI en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            JFramePoblacionHormiga frame = new JFramePoblacionHormiga();
            frame.setVisible(true);
        });

	}

}
