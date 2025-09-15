package vista;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import modelo.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYItemRenderer; // CAMBIO: Importación añadida
import org.jfree.data.xy.*;

public class JFramePoblacionRobot extends JFrame {
    private JPanel contentPane;
    private JTextField txtPoblacion, txtGeneraciones, txtCruce, txtMutacion, txtElitismo;
    private JComboBox<String> cmbSeleccion, cmbCruce, cmbMutacion;
    private JButton btnEjecutar;
    private ChartPanel chartPanel;
    private JTextArea txtResultados; 

    public JFramePoblacionRobot() {
        setTitle("Algoritmo Genético - Población Robot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 600);
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JPanel panelConfiguracion = createPanel();
        contentPane.add(panelConfiguracion, BorderLayout.WEST);

        // Se crea el ChartPanel una sola vez para poder actualizarlo
        chartPanel = new ChartPanel(null);
        contentPane.add(chartPanel, BorderLayout.CENTER); // Se añade directamente al centro

        // CAMBIO: Crear y añadir el panel de texto para los resultados en la parte inferior
        txtResultados = new JTextArea(4, 20); // 5 filas de altura
        txtResultados.setEditable(false);
        txtResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollResultados = new JScrollPane(txtResultados);
        scrollResultados.setBorder(BorderFactory.createTitledBorder("Mejor Ruta Encontrada"));
        contentPane.add(scrollResultados, BorderLayout.SOUTH);
        
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
    	 
    	panelConfiguracion.add(new JLabel("Método de Selección:"));
    	cmbSeleccion = new JComboBox<>(new String[]{
    		"Ruleta", "Torneo Probabilístico", "Torneo Determinístico", 
    		"Estocástico Universal", "Truncamiento", "Restos", "Ranking"
    		});
    	 panelConfiguracion.add(cmbSeleccion);
    	 
    	 panelConfiguracion.add(new JLabel("Método de Cruce:"));
    	 // Se eliminan los cruces que no aplican a permutaciones
    	 cmbCruce = new JComboBox<>(new String[]{
    		 "PMX", "OX", "OXPP", "CX", "CO", "ERX", "Propio", "Ninguno"
    	 	});
    	 panelConfiguracion.add(cmbCruce);
    	 
    	 panelConfiguracion.add(new JLabel("Método de Mutación:"));
    	 cmbMutacion = new JComboBox<>(new String[]{
    		"Inserción", "Intercambio", "Inversión", "Heurística", "Propia", "Ninguno"
    	 	});
    	 panelConfiguracion.add(cmbMutacion);
    	 
    	 btnEjecutar = new JButton("Ejecutar Algoritmo");
    	 panelConfiguracion.add(btnEjecutar);
    	 
    	 return panelConfiguracion; 
    }

    private void ejecutarAlgoritmo() {
        // Obtener parámetros
        int tamPoblacion = Integer.parseInt(txtPoblacion.getText());
        int maxGeneraciones = Integer.parseInt(txtGeneraciones.getText());
        double probCruce = Double.parseDouble(txtCruce.getText()) / 100.0;
        double probMutacion = Double.parseDouble(txtMutacion.getText()) / 100.0;
        double elitismo = Double.parseDouble(txtElitismo.getText()) / 100.0;
        String metodoSeleccion = (String) cmbSeleccion.getSelectedItem();
        String metodoCruce = (String) cmbCruce.getSelectedItem();
        String metodoMutacion = (String) cmbMutacion.getSelectedItem();

        // Crear objetos y ejecutar
        AlgoritmoGenetico algoritmo = new AlgoritmoGenetico(tamPoblacion, maxGeneraciones, probCruce, probMutacion, elitismo, 3);
        PoblacionRobot poblacion = new PoblacionRobot(tamPoblacion, algoritmo);
        poblacion.iniciarGeneracion();

        AlgoritmoGenetico.EvolucionResult resultado = algoritmo.evolucionar(poblacion, metodoSeleccion, metodoCruce, metodoMutacion);
        
        // MOSTRAR RESULTADOS
        actualizarGrafica(resultado);
        
        // CAMBIO: Actualizar el JTextArea en lugar de mostrar un JOptionPane
        String resultadoStr = "Fitness (distancia) = " + resultado.mejorAbsolutoFinal.getFitness() + 
                              "\nGeneración encontrada = " + resultado.generacionMejor +
                              "\nRuta: " + formatRuta((IndividuoRobot)resultado.mejorAbsolutoFinal);
        txtResultados.setText(resultadoStr);

        // Mostrar mapa en consola
        System.out.println("===== Mejor Individuo Absoluto =====");
        System.out.println(resultadoStr);
        System.out.println("====================================");
        mostrarMapa((IndividuoRobot)resultado.mejorAbsolutoFinal);
    }
    
    private void actualizarGrafica(AlgoritmoGenetico.EvolucionResult resultado) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(crearSerie("Mejor por Generación", resultado.mejoresFitness));
        dataset.addSeries(crearSerie("Mejor Absoluto", resultado.mejoresAbsolutos));
        dataset.addSeries(crearSerie("Fitness Medio", resultado.fitnessMedios));

        JFreeChart chart = ChartFactory.createXYLineChart("Evolución del Fitness", "Generación", "Fitness",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        BasicStroke stroke = new BasicStroke(2.0f); // Grosor de 2.0
        renderer.setSeriesStroke(0, stroke);
        renderer.setSeriesStroke(1, stroke);
        renderer.setSeriesStroke(2, stroke);
        // Fin del bloque de cambio
        
        chartPanel.setChart(chart); // Actualizar la gráfica en el panel existente
    }
    
   private XYSeries crearSerie(String nombre, List<Double> datos) {
        XYSeries series = new XYSeries(nombre);
        for (int i = 0; i < datos.size(); i++) {
            series.add(i, datos.get(i));
        }
        return series;
    }

    private String formatRuta(IndividuoRobot individuo) {
        StringBuilder rutaStr = new StringBuilder();
        rutaStr.append("BASE → ");
        for (int habitacion : individuo.getCromosoma()) {
            rutaStr.append(habitacion).append(" → ");
        }
        rutaStr.append("BASE");
        return rutaStr.toString();
    }

    private void mostrarMapa(IndividuoRobot mejor) {
        String[][] mapa = mejor.generarMapa();
        StringBuilder sb = new StringBuilder();
        int columnas = mapa[0].length;
        int anchoCelda = 4;
        sb.append("+").append("-".repeat(columnas * anchoCelda)).append("+\n");
        for (String[] fila : mapa) {
            sb.append("|");
            for (String celda : fila) {
                sb.append(String.format("%-" + anchoCelda + "s", celda));
            }
            sb.append("|\n");
        }
        sb.append("+").append("-".repeat(columnas * anchoCelda)).append("+\n");
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFramePoblacionRobot frame = new JFramePoblacionRobot();
            frame.setVisible(true);
        });
    }
}