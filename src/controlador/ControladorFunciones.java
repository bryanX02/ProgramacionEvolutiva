package controlador;

import java.awt.EventQueue;

import vista.JFrameFunciones;

// Esta clase es la encargada de arrancar el programa y controlarlo
public class ControladorFunciones {

	public static void main(String[] args) {

		EventQueue.invokeLater(() -> {
        	JFrameFunciones frame = new JFrameFunciones();
            frame.setVisible(true);
        });

	}

}
