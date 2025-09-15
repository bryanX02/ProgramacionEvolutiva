package modelo;

import java.util.Random;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;



/**
 * Clase que gestiona una población de individuos Hormiga y aplica el ciclo evolutivo.
 * Hereda de una clase base Poblacion (ArrayList) y contiene la lógica de selección,
 * cruce y mutación específica para el problema de la hormiga.
 */
public class PoblacionHormiga extends Poblacion<IndividuoHormiga> {

	/** Coeficiente para penalizar el tamaño del árbol en el fitness de selección. */
    private final double COEFICIENTE_PARQUEDAD = 0.01; // Puedes ajustar este valor
    /** Caché para almacenar el fitness bruto (comida comida) de cada individuo por generación. */
    private Map<IndividuoHormiga, Double> fitnessBrutoCache;
    /** Caché para almacenar el fitness ajustado por parquedad, usado para la selección. */
    private Map<IndividuoHormiga, Double> fitnessAjustadoCache;
    
    /** Tamaño deseado para la población. */
    private int tamPoblacion;
    /** Referencia al objeto que contiene los parámetros del Algoritmo Genético. */
    private AlgoritmoGenetico algoritmo;
    /** Mejor individuo encontrado en todas las generaciones (histórico). */
    private static IndividuoHormiga mejorIndividuoAbsoluto = null; // Inicializamos a null

    /**
     * Constructor de la población.
     * @param tamPoblacion Tamaño de la población a mantener.
     * @param algoritmo Objeto con los parámetros del AG.
     */
    public PoblacionHormiga(int tamPoblacion, AlgoritmoGenetico algoritmo) {
        super(); // Llama al constructor de ArrayList
        this.tamPoblacion = tamPoblacion;
        this.algoritmo = algoritmo;
        this.fitnessBrutoCache = new HashMap<>();
        this.fitnessAjustadoCache = new HashMap<>();
    }

    /**
     * Método que obtiene el tamaño configurado para la población.
     * @return El tamaño de la población.
     */
    public int getTamPoblacion() {
        return tamPoblacion;
    }

    /**
     * Método que establece el tamaño de la población.
     * (Nota: cambiar el tamaño a mitad de ejecución puede requerir lógica adicional)
     * @param tamPoblacion El nuevo tamaño deseado.
     */
    public void setTamPoblacion(int tamPoblacion) {
        this.tamPoblacion = tamPoblacion;
        // Podríamos necesitar ajustar el tamaño actual de la lista aquí si cambia dinámicamente
    }

    /**
     * Método que inicializa la primera generación de la población.
     * Crea 'tamPoblacion' individuos Hormiga con árboles aleatorios.
     */
    @Override
    public void iniciarGeneracion() {
        this.clear(); // Limpiamos la población anterior si existiera
        for (int i = 0; i < tamPoblacion; i++) {
            // Creamos un nuevo individuo usando los parámetros del algoritmo
            this.add(new IndividuoHormiga(
                    algoritmo.getProfundidadInicial(),
                    algoritmo.getMetodoInicializacion(),
                    algoritmo.getProfMaxBloating()
            ));
        }
         // Reiniciamos el mejor absoluto al iniciar una nueva ejecución completa
         mejorIndividuoAbsoluto = null;
    }

    /**
     * Evalúa la población completa, calculando y cacheando el fitness bruto (comida)
     * y el fitness ajustado por parquedad (para selección).
    */
    public void evaluarPoblacionConParquedad() {
        if (this.isEmpty()) return;

        fitnessBrutoCache.clear();
        fitnessAjustadoCache.clear();

        for (IndividuoHormiga individuo : this) {
            // 1. Calculamos el fitness bruto (comida consumida)
            double fitnessBruto = individuo.evaluar();

            // 2. Calculamos el tamaño del árbol para la penalización
            int tamanoArbol = (individuo.getRaiz() != null) ? individuo.getRaiz().contarNodos() : 0;
            
            // 3. Calculamos el fitness ajustado para la selección
            double penalizacion = COEFICIENTE_PARQUEDAD * tamanoArbol;
            double fitnessAjustado = fitnessBruto - penalizacion;

            // 4. Guardamos ambos valores en sus respectivas cachés
            fitnessBrutoCache.put(individuo, fitnessBruto);
            fitnessAjustadoCache.put(individuo, fitnessAjustado);
        }
    }
    
    /**
     * Método que calcula el fitness medio de la población actual.
     * El fitness de una hormiga es la cantidad de comida que consume (maximizar).
     * @return El valor del fitness medio, o 0 si la población está vacía.
     */
    @Override
    public double getFitnessMedio() {
        // Usamos el método evaluar() de cada individuo, que devuelve la comida consumida.
        return this.isEmpty() ? 0.0 : this.stream().mapToDouble(IndividuoHormiga::evaluar).average().orElse(0.0);
        // Nota: llamar a evaluar() aquí recalcula el fitness cada vez.
        // Sería más eficiente calcularlo una vez por generación y almacenarlo si el rendimiento es crítico.
    }

    /**
     * Método que devuelve el valor extremo (óptimo) de fitness posible para este problema.
     * En este caso, es el número total de bocados de comida (maximizar).
     * @return El fitness máximo posible (89).
     */
    @Override
    public double getExtremo() {
        // El objetivo es comer los 89 bocados
        return 89.0;
    }

    /**
     * Método que selecciona una nueva población a partir de la actual usando un método específico.
     * @param metodoSeleccion Nombre del método de selección ("Ruleta", "Torneo Determinístico", etc.).
     * @return Una nueva instancia de PoblacionHormiga con los individuos seleccionados.
     */
    @Override
    public PoblacionHormiga seleccionarSegun(String metodoSeleccion) {
        // Aseguramos que la población actual no esté vacía
         if (this.isEmpty()) {
              throw new IllegalStateException("No se puede seleccionar de una población vacía.");
         }

        return switch (metodoSeleccion.toLowerCase()) { // Usamos toLowerCase para flexibilidad
            case "ruleta" -> seleccionRuleta();
            case "torneo probabilístico" -> seleccionTorneoProbabilistico();
            case "torneo determinístico" -> seleccionTorneoDeterministico();
            case "estocástico universal" -> seleccionEstocasticaUniversal();
            case "truncamiento" -> seleccionTruncamiento();
            case "restos" -> seleccionRestos();
            case "ranking" -> seleccionRanking();
            default -> throw new IllegalArgumentException("Método de selección no válido: " + metodoSeleccion);
        };
    }
    
    public void mutarPoblacionEspecifica(String metodoMutacion) {
        switch (metodoMutacion.toLowerCase()) {
            case "terminal":
                this.mutacionTerminal(); // Llama al método que itera y aplica mutación terminal
                break;
            case "funcional":
                this.mutacionFuncional();
                break;
            case "subárbol":
                this.mutacionSubarbol();
                break;
            case "hoist":
                this.mutacionHoist();
                break;
            default:
                System.err.println("Advertencia: Método de mutación desconocido '" + metodoMutacion + "'. No se aplicó mutación.");
                // Opcionalmente, aplicar una por defecto o lanzar excepción
                break;
        }
    }

    // --- Métodos de Selección (Implementados para MAXIMIZAR fitness) ---

    /**
     * Método privado que realiza la selección por Ruleta.
     * Los individuos con mayor fitness (más comida) tienen mayor probabilidad.
     * @return Nueva población seleccionada.
     */
    private PoblacionHormiga seleccionRuleta() {
    	// Usamos el fitness ajustado por parquedad para la selección
        double sumaFitness = fitnessAjustadoCache.values().stream().mapToDouble(d -> Math.max(0, d)).sum();
        PoblacionHormiga nuevaGeneracion = new PoblacionHormiga(tamPoblacion, algoritmo);

        if (sumaFitness <= 0) { // Fallback si todos los fitness ajustados son <= 0
            for (int i = 0; i < tamPoblacion; i++) {
                nuevaGeneracion.add(this.get(algoritmo.rand.nextInt(this.size())).clone());
            }
            return nuevaGeneracion;
        }

        for (int i = 0; i < tamPoblacion; i++) {
            double r = algoritmo.rand.nextDouble() * sumaFitness;
            double acumulado = 0.0;
            for (int j = 0; j < this.size(); j++) {
                acumulado += Math.max(0, fitnessAjustadoCache.get(this.get(j)));
                if (acumulado >= r) {
                    nuevaGeneracion.add(this.get(j).clone());
                    break;
                }
                if (j == this.size() - 1) {
                    nuevaGeneracion.add(this.get(j).clone());
                }
            }
        }
        return nuevaGeneracion;
    }

    /**
     * Método privado que realiza la selección por Torneo Determinístico.
     * Elige N participantes al azar y selecciona al MEJOR (mayor fitness).
     * @return Nueva población seleccionada.
     */
    private PoblacionHormiga seleccionTorneoDeterministico() {
        // Usamos probabilidad 1.0 para asegurar que siempre gana el mejor
        return seleccionTorneo(algoritmo.getTamTorneo(), 1.0);
    }

    /**
     * Método privado que realiza la selección por Torneo Probabilístico.
     * Elige N participantes al azar, selecciona al mejor y al segundo mejor,
     * y elige al mejor con una probabilidad P, o al segundo mejor con 1-P.
     * @return Nueva población seleccionada.
     */
    private PoblacionHormiga seleccionTorneoProbabilistico() {
        return seleccionTorneo(algoritmo.getTamTorneo(), algoritmo.getProbTorneoProb());
    }

    /**
     * Método privado base para la selección por Torneo.
     * @param tamTorneo Número de individuos que participan en cada torneo.
     * @param probMejor Probabilidad de seleccionar al mejor (vs al segundo mejor en probabilístico).
     * @return Nueva población seleccionada.
     */
    private PoblacionHormiga seleccionTorneo(int tamTorneo, double probMejor) {
        PoblacionHormiga nuevaGeneracion = new PoblacionHormiga(tamPoblacion, algoritmo);
        int popActualSize = this.size();
        if (popActualSize == 0) return nuevaGeneracion; // Si la población actual está vacía

        for (int i = 0; i < tamPoblacion; i++) {
            // Seleccionamos N participantes aleatorios para el torneo
            List<IndividuoHormiga> participantes = new ArrayList<>(tamTorneo);
            for (int j = 0; j < tamTorneo; j++) {
                participantes.add(this.get(algoritmo.rand.nextInt(popActualSize)));
            }

            // Encontramos al mejor (mayor fitness) y al segundo mejor
            participantes.sort(Comparator.comparingDouble(ind -> fitnessAjustadoCache.get(ind)).reversed()); // Orden descendente por fitness
            IndividuoHormiga mejor = participantes.get(0);
            // Si hay más de un participante, el segundo mejor es el siguiente, si no, es el mismo mejor.
            IndividuoHormiga segundoMejor = (participantes.size() > 1) ? participantes.get(1) : mejor;

            // Elegimos entre el mejor y el segundo mejor según la probabilidad
            if (probMejor >= 1.0 || algoritmo.rand.nextDouble() < probMejor) {
                nuevaGeneracion.add(mejor.clone());
            } else {
                nuevaGeneracion.add(segundoMejor.clone());
            }
        }
        return nuevaGeneracion;
    }

    /**
     * Método privado que realiza la selección Estocástica Universal.
     * Similar a la ruleta, pero con múltiples puntos de selección equiespaciados.
     * @return Nueva población seleccionada.
     */
    private PoblacionHormiga seleccionEstocasticaUniversal() {
        double[] fitness = this.stream()
                               .mapToDouble(ind -> Math.max(0.0, ind.evaluar()))
                               .toArray();
        double sumaFitness = Arrays.stream(fitness).sum();

        PoblacionHormiga nuevaGeneracion = new PoblacionHormiga(tamPoblacion, algoritmo);

        if (sumaFitness <= 0) { // Si todos tienen fitness 0, seleccionamos aleatoriamente
            for (int i = 0; i < tamPoblacion; i++) {
                nuevaGeneracion.add(this.get(algoritmo.rand.nextInt(this.size())).clone());
            }
            return nuevaGeneracion;
        }

        double distancia = sumaFitness / tamPoblacion; // Distancia entre punteros
        double inicio = algoritmo.rand.nextDouble() * distancia; // Punto de inicio aleatorio
        int indiceActual = 0;
        double acumulado = fitness[0];

        for (int i = 0; i < tamPoblacion; i++) {
            double puntoSeleccion = inicio + i * distancia; // Puntero actual
            // Avanzamos en la 'ruleta' hasta encontrar el segmento correcto
            while (acumulado < puntoSeleccion && indiceActual < this.size() - 1) {
                indiceActual++;
                acumulado += fitness[indiceActual];
            }
            nuevaGeneracion.add(this.get(indiceActual).clone());
        }
        return nuevaGeneracion;
    }

    /**
     * Método privado que realiza la selección por Truncamiento.
     * Ordena la población y selecciona repetidamente al P% MEJOR (mayor fitness).
     * @return Nueva población seleccionada.
     */
    private PoblacionHormiga seleccionTruncamiento() {
        double porcentaje = algoritmo.getPorcTruncamiento();
        // Ordenamos la población actual por fitness en orden DESCENDENTE (mejor primero)
        this.sort(Comparator.comparingDouble(ind -> fitnessAjustadoCache.get(ind)).reversed());

        int limite = Math.max(1, (int) (this.size() * porcentaje)); // Cuántos mejores vamos a copiar (al menos 1)
        PoblacionHormiga nuevaGeneracion = new PoblacionHormiga(tamPoblacion, algoritmo);

        // Copiamos repetidamente los 'limite' mejores individuos hasta llenar la nueva población
        for (int i = 0; i < tamPoblacion; i++) {
            nuevaGeneracion.add(this.get(i % limite).clone()); // Usamos módulo para ciclar entre los mejores
        }
        return nuevaGeneracion;
    }

    /**
     * Método privado que realiza la selección por Restos (Remainder Stochastic Sampling).
     * Asigna copias enteras según la parte entera del fitness esperado y
     * selecciona el resto usando Ruleta sobre las partes fraccionarias.
     * @return Nueva población seleccionada.
     */
    private PoblacionHormiga seleccionRestos() {
        double[] fitness = this.stream()
                                .mapToDouble(ind -> Math.max(0.0, ind.evaluar()))
                                .toArray();
        double sumaFitness = Arrays.stream(fitness).sum();
        PoblacionHormiga nuevaGeneracion = new PoblacionHormiga(tamPoblacion, algoritmo);

        if (sumaFitness <= 0) { // Caso especial: todos fitness 0
             for (int i = 0; i < tamPoblacion; i++) {
                nuevaGeneracion.add(this.get(algoritmo.rand.nextInt(this.size())).clone());
            }
            return nuevaGeneracion;
        }

        double fitnessMedio = sumaFitness / this.size();
        double[] fitnessEsperado = Arrays.stream(fitness).map(f -> f / fitnessMedio).toArray();
        int[] copiasEnteras = Arrays.stream(fitnessEsperado).mapToInt(fe -> (int) fe).toArray();
        double[] parteFraccionaria = new double[this.size()];
        for(int i=0; i<this.size(); ++i) {
            parteFraccionaria[i] = fitnessEsperado[i] - copiasEnteras[i];
        }
        double sumaFraccionaria = Arrays.stream(parteFraccionaria).sum();

        // 1. Añadimos las copias enteras
        for (int i = 0; i < this.size(); i++) {
            for (int j = 0; j < copiasEnteras[i]; j++) {
                if (nuevaGeneracion.size() < tamPoblacion) {
                    nuevaGeneracion.add(this.get(i).clone());
                } else {
                    break; // Evitar exceder tamaño de población
                }
            }
             if (nuevaGeneracion.size() >= tamPoblacion) break;
        }

        // 2. Seleccionamos el resto usando Ruleta sobre las partes fraccionarias
        int restantes = tamPoblacion - nuevaGeneracion.size();
        if (restantes > 0 && sumaFraccionaria > 0) {
            for (int i = 0; i < restantes; i++) {
                double r = algoritmo.rand.nextDouble() * sumaFraccionaria;
                double acumulado = 0.0;
                for (int j = 0; j < this.size(); j++) {
                    acumulado += parteFraccionaria[j];
                    if (acumulado >= r) {
                        nuevaGeneracion.add(this.get(j).clone());
                        break;
                    }
                     if (j == this.size() - 1) { // Fallback por precisión
                         nuevaGeneracion.add(this.get(j).clone());
                         break;
                     }
                }
                 // Evitar bucle infinito si la nueva generación ya está llena (no debería pasar aquí)
                 if (nuevaGeneracion.size() >= tamPoblacion) break;
            }
        }
         // Si aún faltan por errores de precisión o sumaFraccionaria=0, rellenamos aleatoriamente
         while(nuevaGeneracion.size() < tamPoblacion) {
              nuevaGeneracion.add(this.get(algoritmo.rand.nextInt(this.size())).clone());
         }

        return nuevaGeneracion;
    }

    /**
     * Método privado que realiza la selección por Ranking.
     * Ordena los individuos y asigna probabilidades de selección basadas en su posición (ranking).
     * @return Nueva población seleccionada.
     */
    private PoblacionHormiga seleccionRanking() {
        // Ordenamos la población actual por fitness en orden DESCENDENTE (mejor primero)
        List<IndividuoHormiga> ordenada = this.stream()
                                          .sorted(Comparator.comparingDouble(ind -> fitnessAjustadoCache.get(ind)).reversed())
                                          .collect(Collectors.toList());

        double[] probabilidades = new double[this.size()];
        double sumaProbabilidades = 0.0;
        // Asignamos probabilidades linealmente según el ranking (mejor rank = mayor prob)
        // SP = 1.5 (Selective Pressure, ajustable, 1.0 a 2.0)
        double SP = 1.5;
        for (int i = 0; i < this.size(); i++) {
            // Formula lineal: P(i) = (SP + ( (1-SP) * i / (N-1) ) ) / N
             // Simplificada: P(i) = (N - i) / suma(1..N) -> mejor individuo (i=0) tiene N, peor (i=N-1) tiene 1
             probabilidades[i] = (double)(this.size() - i); // Rank inverso (el mejor tiene N, el siguiente N-1, ...)
             sumaProbabilidades += probabilidades[i]; // Suma(1..N) = N*(N+1)/2
        }

        PoblacionHormiga nuevaGeneracion = new PoblacionHormiga(tamPoblacion, algoritmo);

        if (sumaProbabilidades <= 0) { // Caso improbable
             for (int i = 0; i < tamPoblacion; i++) {
                nuevaGeneracion.add(this.get(algoritmo.rand.nextInt(this.size())).clone());
            }
            return nuevaGeneracion;
        }


        // Hacemos N selecciones tipo ruleta sobre las probabilidades del ranking
        for (int i = 0; i < tamPoblacion; i++) {
            double r = algoritmo.rand.nextDouble() * sumaProbabilidades;
            double acumulado = 0.0;
            for (int j = 0; j < this.size(); j++) { // Iteramos sobre la población ordenada
                acumulado += probabilidades[j];
                if (acumulado >= r) {
                    nuevaGeneracion.add(ordenada.get(j).clone()); // Añadimos clon del individuo correspondiente al rank j
                    break;
                }
                 if (j == this.size() - 1) { // Fallback por precisión
                     nuevaGeneracion.add(ordenada.get(j).clone());
                     break;
                 }
            }
        }
        return nuevaGeneracion;
    }

    // --- Métodos de Mutación (Llaman a los métodos del individuo) ---

    /**
     * Método que aplica mutación a la población según el método configurado en AlgoritmoGenetico.
     * O aplica una mezcla/secuencia de mutaciones.
     */
    public void mutarPoblacion() {
        // Aplicamos las 4 (o más) mutaciones requeridas secuencialmente.
        // Cada individuo tiene una probabilidad P de sufrir CADA TIPO de mutación.
        // Podrías querer una lógica diferente (p.ej., solo un tipo de mutación por individuo).
        mutacionTerminal();
        mutacionFuncional();
        mutacionSubarbol();
        mutacionHoist();
    }


    /**
     * Método que aplica la mutación de tipo terminal a individuos de la población.
     * Cada individuo tiene una probabilidad 'probMutacion' de ser mutado.
     */
    public void mutacionTerminal() {
         for (IndividuoHormiga ind : this) {
             // Decidimos si este individuo debe sufrir este tipo de mutación
             if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                 ind.mutacionTerminal(); // Llamamos al método del individuo (sin args)
             }
         }
    }

    /**
     * Método que aplica la mutación funcional a individuos de la población.
     * Cada individuo tiene una probabilidad 'probMutacion' de ser mutado.
     */
    public void mutacionFuncional() {
         for (IndividuoHormiga ind : this) {
            if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                 ind.mutacionFuncional(); // Llamamos al método del individuo (sin args)
             }
        }
    }

    /**
     * Método que aplica la mutación de subárbol a individuos de la población.
     * Cada individuo tiene una probabilidad 'probMutacion' de ser mutado.
     */
    public void mutacionSubarbol() {
        for (IndividuoHormiga ind : this) {
            if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                 ind.mutacionSubarbol(); // Llamamos al método del individuo (sin args)
             }
        }
    }

    /**
     * Método que aplica la mutación de poda (Hoist) a individuos de la población.
     * Cada individuo tiene una probabilidad 'probMutacion' de ser mutado.
     */
    public void mutacionHoist() {
         for (IndividuoHormiga ind : this) {
            if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                 ind.mutacionHoist(); // Llamamos al método del individuo (sin args)
            }
        }
    }

    // --- Método de Cruce ---

    /**
     * Método que aplica el cruce a pares de individuos seleccionados en la población.
     * Utiliza el método `cruzarCon` de `IndividuoHormiga`.
     */
    public void cruzarPoblacion() {
        // Barajamos la población para cruzar pares aleatorios
        Collections.shuffle(this, algoritmo.rand);

        // Iteramos por pares de individuos
        // Cruzamos individuos con una probabilidad 'probCruce'
        for (int i = 0; i < this.size() - 1; i += 2) { // -1 para asegurar que i+1 es válido
            IndividuoHormiga p1 = this.get(i);
            IndividuoHormiga p2 = this.get(i + 1);

            // Decidimos si este par se cruza
            if (algoritmo.rand.nextDouble() < algoritmo.getProbCruce()) {
                p1.cruzarCon(p2); // El método cruzarCon modifica p1 y p2 directamente
            }
            // Los individuos modificados (o no) permanecen en la población
        }
    }

    // --- Métodos de Seguimiento del Mejor Individuo ---
    
    public Map<IndividuoHormiga, Double> getFitnessBrutoCache() {
        return this.fitnessBrutoCache;
    }

    /**
     * Método que obtiene el mejor individuo de la población actual (mayor fitness).
     * @return El mejor IndividuoHormiga de la generación actual.
     */
    @Override
    public IndividuoHormiga getMejorIndividuo() {
    	if (this.isEmpty() || fitnessBrutoCache.isEmpty()) return null;
        // El "mejor" se basa en el rendimiento real (fitness bruto), no en el de selección
        return Collections.max(this, Comparator.comparingDouble(ind -> fitnessBrutoCache.get(ind)));
    }

    /**
     * Método que actualiza el mejor individuo absoluto encontrado hasta ahora.
     * Compara el mejor de la generación actual con el mejor absoluto histórico.
     * @param mejorIndividuoGeneracion El mejor individuo de la generación actual.
     */
    public void actualizarAbsoluto(IndividuoHormiga mejorIndividuoGeneracion) {
    	if (mejorIndividuoGeneracion == null || fitnessBrutoCache.isEmpty()) return;
        
        double fitnessMejorGen = fitnessBrutoCache.getOrDefault(mejorIndividuoGeneracion, 0.0);
        double fitnessMejorAbs = (mejorIndividuoAbsoluto != null) ? mejorIndividuoAbsoluto.evaluar() : -1.0;

        // Comparamos el rendimiento real (fitness bruto)
        if (mejorIndividuoAbsoluto == null || fitnessMejorGen > fitnessMejorAbs) {
            mejorIndividuoAbsoluto = mejorIndividuoGeneracion.clone();
        }
    }

    /**
     * Método que establece directamente el mejor individuo absoluto (p.ej., al cargar estado).
     * @param ind El individuo a establecer como mejor absoluto.
     */
    public void setAbsoluto(IndividuoHormiga ind) {
         // Asegurarse de que ind es del tipo correcto
         if (ind instanceof IndividuoHormiga) {
              mejorIndividuoAbsoluto = (IndividuoHormiga) ind.clone(); // Guardar copia
         } else if (ind == null) {
              mejorIndividuoAbsoluto = null;
         }
         // Podríamos lanzar una excepción si el tipo no es correcto
    }

    /**
     * Método que obtiene el mejor individuo absoluto encontrado hasta el momento.
     * @return El mejor IndividuoHormiga histórico.
     */
    @Override
    public IndividuoHormiga getAbsoluto() {
        return mejorIndividuoAbsoluto;
    }
}