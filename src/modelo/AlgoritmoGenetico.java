package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AlgoritmoGenetico {
    public Random rand = new Random(); // Generador aleatorio
    private double probMutacion = 0.1;
    private double probCruce = 0.6;
    private int profundidadInicial = 6; // Profundidad para generar árboles iniciales
    private String metodoInicializacion = "Ramped"; // "Completa", "Creciente", "Ramped"
    private int profMaxBloating = 10; // Profundidad máxima post-operaciones
    private int tamTorneo = 3; // Tamaño para selección por torneo
    private double probTorneoProb = 0.75; // Probabilidad de elegir al mejor en torneo probabilístico
    private double porcTruncamiento = 0.5; // Porcentaje para selección por truncamiento

<<<<<<< HEAD
    // Getters (necesarios para PoblacionHormiga)
    public double getProbMutacion() { return probMutacion; }
    public double getProbCruce() { return probCruce; }
    public int getProfundidadInicial() { return profundidadInicial; }
    public String getMetodoInicializacion() { return metodoInicializacion; }
    public int getProfMaxBloating() { return profMaxBloating; }
    public int getTamTorneo() { return tamTorneo; }
    public double getProbTorneoProb() { return probTorneoProb; }
    public double getPorcTruncamiento() { return porcTruncamiento; }

    // Setters (para configurar desde fuera)
    public void setProbMutacion(double probMutacion) { this.probMutacion = probMutacion; }
    public void setProbCruce(double probCruce) { this.probCruce = probCruce; }
    public void setProfundidadInicial(int profundidadInicial) { this.profundidadInicial = profundidadInicial; }
    public void setMetodoInicializacion(String metodoInicializacion) { this.metodoInicializacion = metodoInicializacion; }
    public void setProfMaxBloating(int profMaxBloating) { this.profMaxBloating = profMaxBloating; }
    public void setTamTorneo(int tamTorneo) { this.tamTorneo = tamTorneo; }
    public void setProbTorneoProb(double probTorneoProb) { this.probTorneoProb = probTorneoProb; }
    public void setPorcTruncamiento(double porcTruncamiento) { this.porcTruncamiento = porcTruncamiento; }
}
=======
    private final int tamPoblacion;
    private final int maxGeneraciones;
    private final double probCruce;
    private final double probMutacion;
    private final double elitismo;
    public final Random rand;

    // Clase interna para devolver los resultados de la evolución
    public static class EvolucionResult {
        public final List<Double> mejoresFitness = new ArrayList<>();
        public final List<Double> mejoresAbsolutos = new ArrayList<>();
        public final List<Double> fitnessMedios = new ArrayList<>();
        public int generacionMejor;
        public Individuo<?> mejorAbsolutoFinal;
    }

    public AlgoritmoGenetico(int tamPoblacion, int maxGeneraciones, double probCruce, double probMutacion, double elitismo, int tamTorneo) {
        this.tamPoblacion = tamPoblacion;
        this.maxGeneraciones = maxGeneraciones;
        this.probCruce = probCruce;
        this.probMutacion = probMutacion;
        this.elitismo = elitismo;
        this.rand = new Random();
    }

    public double getProbCruce() { return probCruce; }
    public double getProbMutacion() { return probMutacion; }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public EvolucionResult evolucionar(PoblacionRobot poblacion, String metodoSeleccion, String metodoCruce, String metodoMutacion) {
        EvolucionResult resultado = new EvolucionResult();
        

        // Comparador para ordenar de mejor a peor (menor fitness es mejor)
        Comparator<IndividuoRobot> comparadorMejorAMenor = (ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness());
        
        // Guardar el mejor individuo inicial
        poblacion.setAbsoluto(poblacion.getMejorIndividuo());
        resultado.mejorAbsolutoFinal = poblacion.getAbsoluto();
        resultado.generacionMejor = 0;

        for (int i = 0; i < maxGeneraciones; i++) {
            // 1. ELITISMO: Guardar los mejores
            int numElite = (int) (this.tamPoblacion * this.elitismo);
            List<IndividuoRobot> elite = new ArrayList<>();
            if (numElite > 0) {
                poblacion.sort(comparadorMejorAMenor);
                for (int j = 0; j < numElite; j++) {
                    elite.add(poblacion.get(j).clone());
                }
            }

            // 2. SELECCIÓN, CRUCE Y MUTACIÓN
            PoblacionRobot nuevaPoblacion = poblacion.seleccionarSegun(metodoSeleccion);
            nuevaPoblacion.cruzarSegun(metodoCruce);
            nuevaPoblacion.mutarSegun(metodoMutacion); 

            // 3. ELITISMO: Reintroducir a los mejores
            if (numElite > 0) {
                // Ordenar de peor a mejor y reemplazar a los peores
                nuevaPoblacion.sort(comparadorMejorAMenor.reversed());
                for (int j = 0; j < numElite; j++) {
                    nuevaPoblacion.set(j, elite.get(j));
                }
            }
            
            // 4. ACTUALIZAR Y REGISTRAR DATOS
            Individuo mejorGeneracion = nuevaPoblacion.getMejorIndividuo();
            nuevaPoblacion.actualizarAbsoluto(mejorGeneracion);
            
            // Si el absoluto de esta generación es nuevo, actualizamos el resultado
            if (nuevaPoblacion.getAbsoluto() != resultado.mejorAbsolutoFinal) {
                resultado.mejorAbsolutoFinal = nuevaPoblacion.getAbsoluto();
                resultado.generacionMejor = i;
            }
            
            // Guardar datos para la gráfica
            resultado.mejoresFitness.add(mejorGeneracion.getFitness());
            resultado.fitnessMedios.add(nuevaPoblacion.getFitnessMedio());
            resultado.mejoresAbsolutos.add(resultado.mejorAbsolutoFinal.getFitness());
            
            poblacion = nuevaPoblacion;
        }

        return resultado;
    }
}
>>>>>>> f57f63b81a2b0567e614f18b601bf74dc467974d
