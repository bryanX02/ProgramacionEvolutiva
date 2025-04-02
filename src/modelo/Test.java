package modelo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        // Definir la ruta específica dada en el problema
        List<Integer> rutaDada = Arrays.asList(19, 3, 15, 10, 6, 17, 1, 13, 9, 5, 
                                               2, 14, 18, 7, 12, 20, 4, 16, 11, 8);

        // Crear el individuo con la ruta dada
        IndividuoRobot individuo = new IndividuoRobot();
        individuo.setCromosoma(rutaDada);

        // Calcular y mostrar el fitness
        double fitness = individuo.getFitness();
        System.out.println("===== Prueba de Fitness =====");
        System.out.println("Ruta dada: " + rutaDada);
        System.out.println("Fitness calculado: " + fitness);
        System.out.println("=============================");
    }
}
