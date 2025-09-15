package modelo;

import modelo.AlgoritmoGenetico; // Importa tus clases
import modelo.IndividuoHormiga;
import modelo.PoblacionHormiga;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*; // Para ejecución paralela opcional

/**
 * Clase para realizar una búsqueda aleatoria de buenos parámetros
 * para el Algoritmo Genético del problema de la Hormiga.
 * ADVERTENCIA: Este proceso puede ser EXTREMADAMENTE LENTO.
 */
public class ParameterTuner {

    // --- Configuración de la Búsqueda Aleatoria ---
    private static final int NUM_PARAM_SETS_TO_TRY = 20; // ¿Cuántos conjuntos de parámetros probar? ¡Aumentar con cuidado!
    private static final int RUNS_PER_PARAM_SET = 3;   // ¿Cuántas veces ejecutar el AG para cada conjunto? (>=1)

    // --- Rangos y Opciones de Parámetros a Explorar ---
    // Define aquí los rangos o listas de valores que quieres probar para cada parámetro

    // Población y Generaciones
    private static final int[] POP_SIZES = {100}; // Opciones discretas
    private static final int[] MAX_GENERATIONS = {100};

    // Probabilidades (rangos continuos, ajusta min/max/paso si prefieres)
    private static final double MIN_PROB_CRUCE = 0.0;
    private static final double MAX_PROB_CRUCE = 0.9;
    private static final double MIN_PROB_MUTACION = 0.0;
    private static final double MAX_PROB_MUTACION = 0.9;

    // Profundidades y Método Inicialización
    private static final int[] PROF_INICIAL = {4, 5, 6, 7};
    private static final String[] MET_INICIALIZACION = {"Ramped", "Creciente"}; // Completa suele ser peor
    private static final int[] PROF_BLOATING = {10, 12, 15, 17};

    // Selección
    private static final String[] MET_SELECCION = {"Torneo Determinístico", "Torneo Probabilístico", "Estocástico Universal", "Truncamiento", "Restos", "Ranking", "Ruleta"};
    private static final int[] TAM_TORNEO = {3, 5, 7}; // Relevante para Torneo

    // Mutación
    private static final String[] MET_MUTACION = {"Subárbol", "Terminal", "Funcional", "Hoist"};

    // Elitismo
    private static final double MIN_ELITISMO = 0.00;
    private static final double MAX_ELITISMO = 0.10;

    // Generador aleatorio para la búsqueda
    private static final Random tunerRand = new Random();

    // Clase interna para guardar un conjunto de parámetros y su puntuación
    private static class ParameterSet {
        int tamPoblacion;
        int maxGeneraciones;
        double probCruce;
        double probMutacion;
        int profInicial;
        String metInicializacion;
        int profBloating;
        String metSeleccion;
        int tamTorneo; // Puede no usarse dependiendo de metSeleccion
        String metMutacion;
        double elitismo;

        double score = -1.0; // Puntuación (ej: fitness promedio del mejor absoluto)

        // Método para generar un conjunto aleatorio
        static ParameterSet generateRandom() {
            ParameterSet params = new ParameterSet();
            params.tamPoblacion = POP_SIZES[tunerRand.nextInt(POP_SIZES.length)];
            params.maxGeneraciones = MAX_GENERATIONS[tunerRand.nextInt(MAX_GENERATIONS.length)];
            params.probCruce = MIN_PROB_CRUCE + tunerRand.nextDouble() * (MAX_PROB_CRUCE - MIN_PROB_CRUCE);
            params.probMutacion = MIN_PROB_MUTACION + tunerRand.nextDouble() * (MAX_PROB_MUTACION - MIN_PROB_MUTACION);
            params.profInicial = PROF_INICIAL[tunerRand.nextInt(PROF_INICIAL.length)];
            params.metInicializacion = MET_INICIALIZACION[tunerRand.nextInt(MET_INICIALIZACION.length)];
            params.profBloating = PROF_BLOATING[tunerRand.nextInt(PROF_BLOATING.length)];
            params.metSeleccion = MET_SELECCION[tunerRand.nextInt(MET_SELECCION.length)];
            params.tamTorneo = TAM_TORNEO[tunerRand.nextInt(TAM_TORNEO.length)];
            params.metMutacion = MET_MUTACION[tunerRand.nextInt(MET_MUTACION.length)];
            params.elitismo = MIN_ELITISMO + tunerRand.nextDouble() * (MAX_ELITISMO - MIN_ELITISMO);

            // Validaciones simples (ej: profBloating > profInicial)
            if (params.profBloating <= params.profInicial) {
                params.profBloating = params.profInicial + tunerRand.nextInt(5) + 1; // Asegura al menos 1 más
            }

            return params;
        }

        @Override
        public String toString() {
            return String.format(
                    "Pop:%d, Gen:%d, PcC:%.2f, PcM:%.2f, Elit:%.2f, ProfI:%d, MetI:%s, ProfB:%d, Sel:%s(T%d), Mut:%s -> Score: %.2f",
                    tamPoblacion, maxGeneraciones, probCruce, probMutacion, elitismo,
                    profInicial, metInicializacion, profBloating,
                    metSeleccion, tamTorneo, metMutacion, score
            );
        }
    }


    /**
     * Función que evalúa un conjunto de parámetros ejecutando el AG varias veces.
     * @param params El conjunto de parámetros a evaluar.
     * @return El fitness promedio (o máximo) del mejor individuo encontrado en las ejecuciones.
     */
    private static double evaluateParameters(ParameterSet params) {
        System.out.printf("Evaluando: Pop:%d, Gen:%d, PcC:%.2f, PcM:%.2f, Elit:%.2f, PI:%d, MI:%s, PB:%d, Sel:%s(T%d), Mut:%s%n",
                params.tamPoblacion, params.maxGeneraciones, params.probCruce, params.probMutacion, params.elitismo,
                params.profInicial, params.metInicializacion, params.profBloating,
                params.metSeleccion, params.tamTorneo, params.metMutacion);

        double totalBestFitness = 0;
        double maxBestFitness = -1; // Para guardar el máximo si prefieres esa métrica

        for (int run = 0; run < RUNS_PER_PARAM_SET; run++) {
            // 1. Configurar el AG con los parámetros dados
            AlgoritmoGenetico ag = new AlgoritmoGenetico();
            ag.setProbCruce(params.probCruce);
            ag.setProbMutacion(params.probMutacion);
            ag.setProfundidadInicial(params.profInicial);
            ag.setMetodoInicializacion(params.metInicializacion);
            ag.setProfMaxBloating(params.profBloating);
            ag.setTamTorneo(params.tamTorneo); // Asegúrate que tu clase AG tenga este setter
            // Otros parámetros como probTorneoProb si es necesario...

            // 2. Crear e inicializar la población
            PoblacionHormiga poblacion = new PoblacionHormiga(params.tamPoblacion, ag);
            poblacion.iniciarGeneracion(); // Reinicia el mejor absoluto estático también
            IndividuoHormiga runBestAbsolute = null;

            // 3. Ejecutar el ciclo evolutivo (similar a doInBackground pero sin publish)
            for (int i = 0; i < params.maxGeneraciones; i++) {
                // Elitismo
                int numElite = (int) (params.tamPoblacion * params.elitismo);
                List<IndividuoHormiga> elite = new ArrayList<>();
                if (numElite > 0 && !poblacion.isEmpty()) {
                    poblacion.sort(Comparator.comparingDouble(IndividuoHormiga::evaluar).reversed());
                    for (int k = 0; k < numElite && k < poblacion.size(); ++k) {
                        elite.add(poblacion.get(k).clone());
                    }
                }

                // Selección
                PoblacionHormiga seleccionados = poblacion.seleccionarSegun(params.metSeleccion);

                // Cruce
                seleccionados.cruzarPoblacion();

                // Mutación (usando el método específico)
                // ¡Asegúrate que este método existe y funciona en PoblacionHormiga!
                seleccionados.mutarPoblacionEspecifica(params.metMutacion);

                // Elitismo (Reemplazo)
                 if (numElite > 0 && !elite.isEmpty() && !seleccionados.isEmpty()) {
                    seleccionados.sort(Comparator.comparingDouble(IndividuoHormiga::evaluar)); // Peor primero
                    for (int k = 0; k < numElite && k < elite.size() && k < seleccionados.size(); ++k) {
                        seleccionados.set(k, elite.get(k));
                    }
                }

                // Actualizar absoluto (usa el método que actualiza el estático)
                IndividuoHormiga mejorGen = seleccionados.getMejorIndividuo();
                seleccionados.actualizarAbsoluto(mejorGen);
                runBestAbsolute = seleccionados.getAbsoluto(); // Obtiene el mejor de esta corrida

                // Condición de parada temprana si se alcanza el óptimo
                if (runBestAbsolute != null && runBestAbsolute.evaluar() >= 89.0) {
                    //System.out.println("   (Run " + (run+1) + ": Óptimo encontrado en gen " + i + ")");
                    break;
                }

                // Siguiente generación
                poblacion = seleccionados;
            } // Fin bucle generaciones

            // Registrar el mejor fitness de esta ejecución
            double bestFitnessRun = (runBestAbsolute != null) ? runBestAbsolute.evaluar() : 0.0;
            totalBestFitness += bestFitnessRun;
            if (bestFitnessRun > maxBestFitness) {
                maxBestFitness = bestFitnessRun;
            }
            System.out.printf("   Run %d/%d -> Mejor Fitness: %.1f%n", run + 1, RUNS_PER_PARAM_SET, bestFitnessRun);

        } // Fin bucle runs por parámetro

        // Calcular la puntuación (ej: promedio, o el máximo)
        // double score = totalBestFitness / RUNS_PER_PARAM_SET; // Promedio
        double score = maxBestFitness; // Usar el máximo puede ser mejor si buscas "capaz de encontrar"

        System.out.printf("   Score para este set (Max Fitness): %.2f%n", score);
        return score;
    }


    /**
     * Método principal para ejecutar la búsqueda aleatoria.
     */
    public static void main(String[] args) {
        System.out.println("--- Iniciando Búsqueda Aleatoria de Parámetros ---");
        System.out.println("Probando " + NUM_PARAM_SETS_TO_TRY + " conjuntos de parámetros.");
        System.out.println("Cada conjunto se evaluará " + RUNS_PER_PARAM_SET + " veces.");
        System.out.println("¡Esto puede tardar MUCHO TIEMPO!");

        ParameterSet bestParamSet = null;
        double bestScore = -1.0;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_PARAM_SETS_TO_TRY; i++) {
            System.out.println("\n--- Probando Conjunto de Parámetros #" + (i + 1) + "/" + NUM_PARAM_SETS_TO_TRY + " ---");
            ParameterSet currentParams = ParameterSet.generateRandom();
            double currentScore = evaluateParameters(currentParams);
            currentParams.score = currentScore;

            if (currentScore > bestScore) {
                System.out.println("*** ¡Nuevo mejor encontrado! ***");
                bestScore = currentScore;
                bestParamSet = currentParams; // Guardamos la referencia
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000; // Duración en segundos

        System.out.println("\n--- Búsqueda Aleatoria Finalizada ---");
        System.out.println("Tiempo total: " + duration + " segundos (" + duration/60.0 + " minutos)");
        if (bestParamSet != null) {
            System.out.println("Mejor conjunto de parámetros encontrado:");
            System.out.println(bestParamSet.toString());
        } else {
            System.out.println("No se encontraron resultados válidos.");
        }
        System.out.println("------------------------------------");
    }
}