package vista;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import modelo.AlgoritmoGenetico;
import modelo.IndividuoFuncion3;
import modelo.PoblacionFun3;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Funcion3 extends JFrame {

    private JPanel contentPane;
    private JTextField txtPoblacion, txtGeneraciones, txtCruce, txtMutacion, txtElitismo;
    private JComboBox<String> cmbSeleccion, cmbCruce, cmbMutacion;
    private JButton btnEjecutar;
    private ChartPanel chartPanel;
    
    public Funcion3() {
        setTitle("Configuración del Algoritmo Genético");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 600);
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);
        
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
        
        contentPane.add(panelConfiguracion, BorderLayout.WEST);
        
        // Panel para la gráfica
        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(600, 500));
        contentPane.add(chartPanel, BorderLayout.CENTER);
        
        btnEjecutar.addActionListener(e -> ejecutarAlgoritmo());
    }

    private void ejecutarAlgoritmo() {
        int tamPoblacion = Integer.parseInt(txtPoblacion.getText());
        int maxGeneraciones = Integer.parseInt(txtGeneraciones.getText());
        double probCruce = Double.parseDouble(txtCruce.getText()) / 100.0;
        double probMutacion = Double.parseDouble(txtMutacion.getText()) / 100.0;
        double elitismo = Double.parseDouble(txtElitismo.getText()) / 100.0;
        
        String metodoSeleccion = (String) cmbSeleccion.getSelectedItem();
        String metodoCruce = (String) cmbCruce.getSelectedItem();
        String metodoMutacion = (String) cmbMutacion.getSelectedItem();
        
        AlgoritmoGenetico algoritmo = new AlgoritmoGenetico(tamPoblacion, maxGeneraciones, probCruce, probMutacion, elitismo, 3);
        PoblacionFun3 poblacion = new PoblacionFun3(tamPoblacion, algoritmo);
        
        poblacion.iniciarGeneracion();
        ArrayList<PoblacionFun3> generaciones = new ArrayList<>();
        generaciones.add(poblacion);
        
        XYSeries mejorSeries = new XYSeries("Mejor Fitness", true, false);
        XYSeries mejorAbsolutoSeries = new XYSeries("Mejor Absoluto", true, false);
        XYSeries mediaSeries = new XYSeries("Fitness Medio", true, false);
        
        double mejorAbsoluto = Double.MAX_VALUE;
        
        for (int i = 1; i <= maxGeneraciones; i++) {
            PoblacionFun3 nuevaPoblacion = poblacion.seleccionarSegun(metodoSeleccion);
            nuevaPoblacion.cruzarSegun(metodoCruce);
            if (!"Ninguno".equals(metodoMutacion)) {
                nuevaPoblacion.mutacion();
            }
            generaciones.add(nuevaPoblacion);
            
            double mejor = nuevaPoblacion.getMejorIndividuo().getFitness();
            
            // Actualizamos el mejor fitness absoluto encontrado
            if (mejor < mejorAbsoluto) {
                mejorAbsoluto = mejor;
            }
            
            mejorSeries.add(i, mejor);
            mejorAbsolutoSeries.add(i, mejorAbsoluto);
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
        
        // Invertimos el gráfico para la minimización
        chartPanel.setChart(chart);
        
        JOptionPane.showMessageDialog(this, "Mejor valor encontrado: " + mejorAbsoluto, "Resultado", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
        	Funcion3 frame = new Funcion3();
            frame.setVisible(true);
        });
    }
}
