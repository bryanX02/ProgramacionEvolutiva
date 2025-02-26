package controlador;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import modelo.AlgoritmoGenetico;
import modelo.IndividuoFuncion1;
import modelo.PoblacionFun1;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Test extends JFrame {

    private JPanel contentPane;

    public static void main(String[] args) {
    	EventQueue.invokeLater(() -> {
            // Parámetros del algoritmo genético
            int tamPoblacion = 100;
            int maxGeneraciones = 100;
            double probCruce = 0.7;
            double probMutacion = 0.01;

            // Inicializar el algoritmo genético
            AlgoritmoGenetico algoritmo = new AlgoritmoGenetico(tamPoblacion, maxGeneraciones, probCruce, probMutacion, 0.5, 3);
            PoblacionFun1 poblacion = new PoblacionFun1(tamPoblacion, algoritmo);
            
            // 1️ Iniciar la población
            poblacion.iniciarGeneracion();

            // Guardar la evolución del fitness
            ArrayList<PoblacionFun1> generaciones = new ArrayList<>();
            generaciones.add(poblacion);

            // 2️ Evaluar la población inicial
            System.out.println("Generación 1, Fitness Global: " + poblacion.getFitnessMedio());

            // 3️ Evolución del algoritmo
            for (int i = 2; i <= maxGeneraciones; i++) {
                // Selección
                PoblacionFun1 nuevaPoblacion = poblacion.seleccionRuleta();

                // Cruce (se aplica dentro de la población)
                nuevaPoblacion.cruceMonopunto();  // O nuevaPoblacion.cruceUniforme();

                // Mutación
                nuevaPoblacion.mutacion();

                // Evaluar la nueva población
                generaciones.add(nuevaPoblacion);
                System.out.println("Generación " + i + ", Fitness Global: " + nuevaPoblacion.getFitnessMedio());

                // Reemplazar la población actual con la nueva
                poblacion = nuevaPoblacion;
            }

            // 4️  Devolver el mejor individuo encontrado
            IndividuoFuncion1 mejorIndividuo = poblacion.getMejorIndividuo();
            System.out.println("Mejor Individuo: " + mejorIndividuo);

            // Mostrar la gráfica con la evolución del fitness
        });
    }
}
