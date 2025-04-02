package vista;

import javax.swing.*;
import java.awt.*;
import modelo.*;
import java.util.ArrayList;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

public class JFramePoblacionRobot extends JFrame {
    private JPanel contentPane;
    private JTextField txtPoblacion, txtGeneraciones, txtCruce, txtMutacion, txtElitismo;
    private JComboBox<String> cmbSeleccion, cmbCruce, cmbMutacion;
    private JButton btnEjecutar;
    private XYSeries fitnessSeries;
    private XYSeriesCollection dataset;
    private JFreeChart chart;

    public JFramePoblacionRobot() {
        setTitle("Algoritmo Genético - Población Robot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 600);
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JPanel panelConfiguracion = createPanel();
        contentPane.add(panelConfiguracion, BorderLayout.WEST);

        JPanel panelGrafica = new JPanel(new BorderLayout());
        fitnessSeries = new XYSeries("Mejor Fitness");
        dataset = new XYSeriesCollection(fitnessSeries);
        chart = ChartFactory.createXYLineChart("Evolución del Fitness", "Generaciones", "Fitness", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        panelGrafica.add(chartPanel, BorderLayout.EAST);
        contentPane.add(panelGrafica, BorderLayout.EAST);

        btnEjecutar.addActionListener(e -> ejecutarAlgoritmo());
    }

    private JPanel createPanel() {
        JPanel panelConfiguracion = new JPanel();
        panelConfiguracion.setLayout(new BoxLayout(panelConfiguracion, BoxLayout.Y_AXIS));

        panelConfiguracion.add(new JLabel("Tamaño de población:"));
        txtPoblacion = new JTextField("1000");
        panelConfiguracion.add(txtPoblacion);

        panelConfiguracion.add(new JLabel("Número de generaciones:"));
        txtGeneraciones = new JTextField("1000");
        panelConfiguracion.add(txtGeneraciones);

        panelConfiguracion.add(new JLabel("Probabilidad de cruce (%):"));
        txtCruce = new JTextField("50");
        panelConfiguracion.add(txtCruce);

        panelConfiguracion.add(new JLabel("Probabilidad de mutación (%):"));
        txtMutacion = new JTextField("50");
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
        cmbCruce = new JComboBox<>(new String[]{
            "Monopunto", "Uniforme", "PMX", "OX", "OXPP", "CX", "CO", "ERX", "Propio", "Ninguno"
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
        // Obtenemos los parámetros de configuración desde la interfaz
        int tamPoblacion = Integer.parseInt(txtPoblacion.getText());
        int maxGeneraciones = Integer.parseInt(txtGeneraciones.getText());
        double probCruce = Double.parseDouble(txtCruce.getText()) / 100.0;
        double probMutacion = Double.parseDouble(txtMutacion.getText()) / 100.0;
        double elitismo = Double.parseDouble(txtElitismo.getText()) / 100.0;

        String metodoSeleccion = (String) cmbSeleccion.getSelectedItem();
        String metodoCruce = (String) cmbCruce.getSelectedItem();
        String metodoMutacion = (String) cmbMutacion.getSelectedItem();

        // Inicializamos el algoritmo y la población
        AlgoritmoGenetico algoritmo = new AlgoritmoGenetico(tamPoblacion, maxGeneraciones, probCruce, probMutacion, elitismo, 3);
        PoblacionRobot poblacion = new PoblacionRobot(tamPoblacion, algoritmo);
        poblacion.iniciarGeneracion();

        // Series para la gráfica de evolución del fitness
        XYSeries mejorSeries = new XYSeries("Mejor Fitness", true, false);
        XYSeries mejorAbsolutoSeries = new XYSeries("Mejor Absoluto", true, false);
        XYSeries mediaSeries = new XYSeries("Fitness Medio", true, false);

        // Guardamos el mejor individuo absoluto desde la primera generación
        IndividuoRobot mejorAbsoluto = (IndividuoRobot) poblacion.getMejorIndividuo().clone();

        // Agregar datos a la gráfica
        mejorSeries.add(0, mejorAbsoluto.getFitness());
        mejorAbsolutoSeries.add(0, mejorAbsoluto.getFitness());
        mediaSeries.add(0, poblacion.getFitnessMedio());
        
        // Bucle de evolución
        for (int i = 1; i < maxGeneraciones; i++) {
        	
            // Selección, cruce y mutación
            PoblacionRobot nuevaPoblacion = poblacion.seleccionarSegun(metodoSeleccion);
            nuevaPoblacion.cruzarSegun(metodoCruce);
            nuevaPoblacion.mutarSegun(metodoMutacion);

            // Obtener el mejor de la generación
            IndividuoRobot mejorGeneracion = nuevaPoblacion.getMejorIndividuo();

            // Actualizar mejor absoluto
            if (mejorGeneracion.getFitness() < mejorAbsoluto.getFitness()) {
                mejorAbsoluto = (IndividuoRobot) mejorGeneracion.clone();
            }

            // Agregar datos a la gráfica
            mejorSeries.add(i, mejorGeneracion.getFitness());
            mejorAbsolutoSeries.add(i, mejorAbsoluto.getFitness());
            mediaSeries.add(i, nuevaPoblacion.getFitnessMedio());

            // Pasamos a la siguiente generación
            poblacion = nuevaPoblacion;
        }

        // Actualizar la gráfica con los datos de evolución
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(mejorSeries);
        dataset.addSeries(mejorAbsolutoSeries);
        dataset.addSeries(mediaSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Evolución del Fitness", "Generaciones", "Fitness",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        ChartPanel chartPanel = new ChartPanel(chart);
        contentPane.add(chartPanel, BorderLayout.EAST);
        contentPane.revalidate();

        // Mostrar mejor individuo absoluto en la interfaz
        String resultado = "Mejor ruta encontrada:\n" +
                "Fitness = " + mejorAbsoluto.getFitness() + 
                "\nRuta: " + formatRuta(mejorAbsoluto);
        
        JOptionPane.showMessageDialog(this, resultado, "Resultado", JOptionPane.INFORMATION_MESSAGE);

        // Mostramos mejor individuo absoluto en la consola
        System.out.println("===== Mejor Individuo Absoluto =====");
        System.out.println("Fitness: " + mejorAbsoluto.getFitness());
        System.out.println("Ruta: " + formatRuta(mejorAbsoluto));
        System.out.println("====================================");

        // Mostramos mapa del mejor individuo absoluto
        mostrarMapa(mejorAbsoluto);
    }

    // Método para formatear la ruta del robot con flechas
    private String formatRuta(IndividuoRobot individuo) {
        StringBuilder rutaStr = new StringBuilder();
        for (int habitacion : individuo.getCromosoma()) {
            rutaStr.append(habitacion).append("→");
        }
        rutaStr.setLength(rutaStr.length() - 1); // Eliminar última flecha
        return rutaStr.toString();
    }
   

    private void mostrarMapa(IndividuoRobot mejor) {
        String[][] mapa = mejor.generarMapa();
        StringBuilder sb = new StringBuilder();

        int columnas = mapa[0].length;
        int anchoCelda = 3; // Para asegurar ancho uniforme de cada celda

        // Borde superior
        sb.append("+").append("-".repeat(columnas * anchoCelda)).append("+\n");

        for (String[] fila : mapa) {
            sb.append("|");
            for (String celda : fila) {
                sb.append(String.format("%-3s", celda)); // Ajustar a 3 espacios fijos
            }
            sb.append("|\n");
        }

        // Borde inferior
        sb.append("+").append("-".repeat(columnas * anchoCelda)).append("+\n");

        System.out.println(sb.toString()); // También imprimir en consola para debug
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFramePoblacionRobot frame = new JFramePoblacionRobot();
            frame.setVisible(true);
        });
    }
}