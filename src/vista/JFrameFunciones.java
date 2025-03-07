package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import modelo.*;


public class JFrameFunciones extends JFrame {
	
	private JPanel contentPane;
    private JTextField txtPoblacion, txtGeneraciones, txtCruce, txtMutacion, txtElitismo, txtDimensiones;
    private JComboBox<String> cmbFuncion, cmbSeleccion, cmbCruce, cmbMutacion;
    private JButton btnEjecutar;
    private ChartPanel chartPanel;
    
    public JFrameFunciones() {
    	
    	// Creacion de la ventana
    	setTitle("Algoritmo genético - Practica 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 600);
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);
        
        JPanel panelConfiguracion = createPanel();
        
        contentPane.add(panelConfiguracion, BorderLayout.WEST);
        
        // Panel para la gráfica
        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(600, 500));
        contentPane.add(chartPanel, BorderLayout.CENTER);
        
        btnEjecutar.addActionListener(e -> ejecutarAlgoritmo());
    	
        
    	
    }

    private JPanel createPanel() {
    	
    	JPanel panelConfiguracion = new JPanel();
    	
    	panelConfiguracion.setLayout(new BoxLayout(panelConfiguracion, BoxLayout.Y_AXIS));
    	
        panelConfiguracion.add(new JLabel("Tamaño de población:"));
        txtPoblacion = new JTextField("100");
        panelConfiguracion.add(txtPoblacion);
        
        panelConfiguracion.add(new JLabel("Número de generaciones:"));
        txtGeneraciones = new JTextField("100");
        panelConfiguracion.add(txtGeneraciones);
        
        panelConfiguracion.add(new JLabel("Probabilidad de cruce (%):"));
        txtCruce = new JTextField("60");
        panelConfiguracion.add(txtCruce);
        
        panelConfiguracion.add(new JLabel("Probabilidad de mutación (%):"));
        txtMutacion = new JTextField("5");
        panelConfiguracion.add(txtMutacion);
        
        panelConfiguracion.add(new JLabel("Porcentaje de elitismo:"));
        txtElitismo = new JTextField("0");
        panelConfiguracion.add(txtElitismo);
        
        panelConfiguracion.add(new JLabel("Número de dimensiones:"));
        txtDimensiones = new JTextField("2");
        panelConfiguracion.add(txtDimensiones);
        
        panelConfiguracion.add(new JLabel("Función Matemática"));
        cmbFuncion = new JComboBox<>(new String[]{"F1: Calibracion y prueba", "F2: Mishra Bird", 
        		"F3: Schubert", "F4: Michalewicz Binarios", "F5: Michalewicz Reales"});
        panelConfiguracion.add(cmbFuncion);
        
        panelConfiguracion.add(new JLabel("Método de Selección:"));
        cmbSeleccion = new JComboBox<>(new String[]{"Ruleta", "Torneo Probabilístico", "Torneo Determinístico", "Estocástico Universal", "Truncamiento", "Restos"});
        panelConfiguracion.add(cmbSeleccion);
        
        panelConfiguracion.add(new JLabel("Método de Cruce:"));
        cmbCruce = new JComboBox<>(new String[]{"Monopunto", "Uniforme", "Ninguno"});
        panelConfiguracion.add(cmbCruce);
        
        panelConfiguracion.add(new JLabel("Método de Mutación:"));
        cmbMutacion = new JComboBox<>(new String[]{"Mutación Básica", "Ninguno"});
        panelConfiguracion.add(cmbMutacion);
        
        btnEjecutar = new JButton("Ejecutar Algoritmo");
        panelConfiguracion.add(btnEjecutar);
    	
    	return panelConfiguracion;
    	
    }
    
    private void ejecutarAlgoritmo() {
    	
    	// Se obtienen los parametros seleccionados por el usuario
    	int tamPoblacion = Integer.parseInt(txtPoblacion.getText());
        int maxGeneraciones = Integer.parseInt(txtGeneraciones.getText());
        double probCruce = Double.parseDouble(txtCruce.getText()) / 100.0;
        double probMutacion = Double.parseDouble(txtMutacion.getText()) / 100.0;
        double elitismo = Double.parseDouble(txtElitismo.getText()) / 100.0;
        int dimensiones = Integer.parseInt(txtDimensiones.getText());

        String funcion = (String) cmbFuncion.getSelectedItem();
        String metodoSeleccion = (String) cmbSeleccion.getSelectedItem();
        String metodoCruce = (String) cmbCruce.getSelectedItem();
        String metodoMutacion = (String) cmbMutacion.getSelectedItem();
        
        // Se crea el algoriutomo y la poblacion
        AlgoritmoGenetico algoritmo = new AlgoritmoGenetico(tamPoblacion, maxGeneraciones, probCruce, probMutacion, elitismo, 3);
        Poblacion poblacion = obtenerPoblacionFuncion(funcion, tamPoblacion, algoritmo);
        
        // Se inician las generaciones (individuos random)
        if (funcion == "F4: Michalewicz Binarios" || funcion == "F5: Michalewicz Reales") {
        	poblacion.iniciarGeneracionDimensionada(dimensiones);
        }else {
        	poblacion.iniciarGeneracion();
        }
        
        // Y ahora se aplicara el algoritmo de evolucion e iremos guardando las generaciones
        ArrayList<Poblacion> generaciones = new ArrayList<>();
        generaciones.add(poblacion);
        
        // Variables que registraran la evolucion para mostrar en la gr�fica
        XYSeries mejorSeries = new XYSeries("Mejor Fitness", true, false);
        XYSeries mejorAbsolutoSeries = new XYSeries("Mejor Absoluto", true, false);
        XYSeries mediaSeries = new XYSeries("Fitness Medio", true, false);
        
        // Y ahora se aplicara el algoritmo seg�n el n�mero de generaciones a generar
        for (int i = 0; i < maxGeneraciones; i++) {
        	
        	// Seleccion
            Poblacion nuevaPoblacion = poblacion.seleccionarSegun(metodoSeleccion);

            // Cruze
            nuevaPoblacion.cruzarSegun(metodoCruce);
            
            // Mutacion
            if (!"Ninguno".equals(metodoMutacion)) {
                nuevaPoblacion.mutacion();
            }
            
            // Registramos la nueva generacion
            generaciones.add(nuevaPoblacion);
            
            // Obtenemos el mejor individuo y vemos si es el mejor entre generaciones
            
            Individuo<?> mejor = nuevaPoblacion.getMejorIndividuo();
            poblacion.actualizarAbsoluto(mejor);
            
            mejorSeries.add(i, mejor.getFitness());
            mejorAbsolutoSeries.add(i, nuevaPoblacion.getAbsoluto().getFitness());
            mediaSeries.add(i, nuevaPoblacion.getFitnessMedio());
            
            poblacion = nuevaPoblacion;
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(mejorSeries);
        dataset.addSeries(mejorAbsolutoSeries);
        dataset.addSeries(mediaSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Evolución del Fitness", "Generación", "Fitness",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        
        chartPanel.setChart(chart);
        
        JOptionPane.showMessageDialog(this, "Mejor valor encontrado: " + poblacion.getAbsoluto().getFitness(), "Resultado", JOptionPane.INFORMATION_MESSAGE);
    	
    }

	private Poblacion obtenerPoblacionFuncion(String funcion, int tamPoblacion, AlgoritmoGenetico algoritmo) {
		
		Poblacion poblacion;
		
		switch (funcion) {
		
			case "F1: Calibracion y prueba":
				poblacion = new PoblacionFun1(tamPoblacion, algoritmo);
				break;		
			case "F2: Mishra Bird":
				poblacion = new PoblacionFun2(tamPoblacion, algoritmo);
				break;
			case "F3: Schubert":
				poblacion = new PoblacionFun3(tamPoblacion, algoritmo);
				break;
			case "F4: Michalewicz Binarios":
				poblacion = new PoblacionFun4(tamPoblacion, algoritmo);
				break;
			case "F5: Michalewicz Reales":
				poblacion = new PoblacionFun5(tamPoblacion, algoritmo);
				break;
			default:
				throw new IllegalArgumentException("Algo ha ido muyyy mal");
		
		}
      
		return poblacion;
	}
    
}
