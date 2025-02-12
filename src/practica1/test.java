package practica1;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class test extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					test frame = new test();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// public AlgoritmoGenetico(int tamPoblacion, int maxGeneraciones, double probCruce, double probMutacion,int tamTorneo) {
		AlgoritmoGenetico algoritmo = new AlgoritmoGenetico(10, 10, 0.5, 0.5, 10);
		
		IndividuoFuncion1[] generacion1 = algoritmo.obtenerNuevaGeneracion();
		System.out.println(generacion1[0].toString());
		for (IndividuoFuncion1 individuo: generacion1) {
			
			System.out.println(individuo.getValor());
			
		}
	}

	/**
	 * Create the frame.
	 */
	public test() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
	}

}
