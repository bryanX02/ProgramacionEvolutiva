package modelo;

import java.util.Random;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class IndividuoHormiga extends Individuo<Nodo> {
	
	// --- Variables globales ---
	private boolean[][] comida;
	private int x, y, direccion; // posición y dirección de la hormiga
	private int pasos;
	private int comidaConsumida;

    private Nodo raiz;
    private static final Random random = new Random();
    private int profundidadMaximaControlBloating = 10;

    // --- Constructores ---

    /**
     * Constructor que genera un individuo con un árbol aleatorio.
     * @param profundidadMaxima Profundidad máxima para la generación inicial.
     * @param metodoInicializacion Método de generación ("Completa", "Creciente", "Ramped").
     * @param profMaxBloating Profundidad máxima para control de bloating.
     */
    public IndividuoHormiga(int profundidadMaxima, String metodoInicializacion, int profMaxBloating) {
        this.profundidadMaximaControlBloating = profMaxBloating;
        this.raiz = generarArbol(profundidadMaxima, metodoInicializacion, 0);
        // No inicializamos simulación aquí, se hace antes de evaluar.
    }

    /**
     * Constructor vacío para usos como clonación o inicialización diferida.
     * @param profMaxBloating Profundidad máxima para control de bloating.
     */
    public IndividuoHormiga(int profMaxBloating) {
        this.profundidadMaximaControlBloating = profMaxBloating;
        this.raiz = null; // O un árbol por defecto mínimo si se prefiere
    }

    // --- Métodos Principales de Simulación y Evaluación ---

    /**
     * Método que inicializa el estado de la hormiga para una nueva simulación.
     * Resetea posición, dirección, pasos, comida consumida y copia el mapa de comida inicial.
     */
    public void inicializarSimulacion() {
        boolean[][] comidaOriginal = generarMapaComidaInicial();
        // Hacemos una copia profunda del mapa original estático
        this.comida = new boolean[32][32];
        for (int i = 0; i < 32; i++) {
            this.comida[i] = Arrays.copyOf(comidaOriginal[i], 32);
        }

        this.x = 0;
        this.y = 0;
        // La dirección 1 es Este según el enunciado (0,0 mirando al Este)
        this.direccion = 1; // 0=Norte, 1=Este, 2=Sur, 3=Oeste
        this.pasos = 0;
        this.comidaConsumida = 0;
    }

    /**
     * Método que evalúa el rendimiento (fitness) de la hormiga ejecutando su árbol.
     * Simula el comportamiento de la hormiga paso a paso hasta el límite de pasos o hasta que coma todo.
     * @return La cantidad de comida consumida, que es el fitness.
     */
    public int evaluar() {
        inicializarSimulacion(); // ¡MUY IMPORTANTE: Reiniciamos estado antes de cada evaluación!
        if (raiz == null) return 0; // Si no hay árbol, no podemos hacer nada

        Queue<Terminal> accionesPendientes = new LinkedList<>();

        // Bucle principal de simulación: continúa mientras queden pasos y comida
        while (this.pasos < 400 && this.comidaConsumida < 89) {

            // Si no hay acciones planeadas, interpretamos el árbol para obtener la siguiente secuencia
            if (accionesPendientes.isEmpty()) {
                obtenerSiguienteSecuenciaAcciones(this.raiz, accionesPendientes);
                // Si después de interpretar el árbol sigue sin haber acciones (árbol inválido/vacío?), terminamos
                if (accionesPendientes.isEmpty()) {
                    break;
                }
            }

            // Ejecutamos la siguiente acción de la secuencia planeada
            Terminal accionActual = accionesPendientes.poll();
            if (accionActual != null) {
                ejecutarAccion(accionActual); // Este método actualiza x, y, dir, pasos, comidaConsumida
            }
            // El bucle while comprobará automáticamente si hemos alcanzado el límite de pasos o comida
        }
        return this.comidaConsumida;
    }

    /**
     * Método privado recursivo que interpreta el árbol y llena la cola con la secuencia de acciones terminales.
     * Se llama cuando la cola de acciones está vacía durante la evaluación.
     * @param nodo Nodo actual del árbol a interpretar.
     * @param acciones Cola donde se añadirán las acciones terminales encontradas.
     */
    private void obtenerSiguienteSecuenciaAcciones(Nodo nodo, Queue<Terminal> acciones) {
        // Condiciones de parada de la recursión o interpretación
        if (nodo == null || this.pasos >= 400 || this.comidaConsumida >= 89) {
            return;
        }

        if (nodo.esTerminal()) {
            // Si encontramos un terminal, lo añadimos a la cola de acciones
            acciones.add(nodo.terminal);
        } else {
            // Si es una función, la interpretamos según su tipo
            switch (nodo.funcion) {
                case SICOMIDA:
                    // Evaluamos la condición y seguimos por la rama correspondiente
                    // La condición (hayComidaDelante) se evalúa en el momento de la interpretación
                    if (hayComidaDelante()) {
                        obtenerSiguienteSecuenciaAcciones(nodo.hijo1, acciones);
                    } else {
                        obtenerSiguienteSecuenciaAcciones(nodo.hijo2, acciones);
                    }
                    break;
                case PROG2:
                    // Ejecutamos secuencialmente: interpretamos el primer hijo, luego el segundo
                    obtenerSiguienteSecuenciaAcciones(nodo.hijo1, acciones);
                    obtenerSiguienteSecuenciaAcciones(nodo.hijo2, acciones);
                    break;
                case PROG3:
                    // Ejecutamos secuencialmente: interpretamos los tres hijos en orden
                    obtenerSiguienteSecuenciaAcciones(nodo.hijo1, acciones);
                    obtenerSiguienteSecuenciaAcciones(nodo.hijo2, acciones);
                    obtenerSiguienteSecuenciaAcciones(nodo.hijo3, acciones);
                    break;
            }
        }
    }


    /**
     * Método que ejecuta una única acción terminal (AVANZA, DERECHA, IZQUIERDA).
     * Actualiza el estado de la hormiga (posición, dirección, comida, pasos).
     * @param terminal La acción terminal a ejecutar.
     */
    private void ejecutarAccion(Terminal terminal) {
        // Siempre consumimos un paso por acción terminal
        this.pasos++;
        if (this.pasos > 400) return; // No hacemos nada si ya hemos superado el límite

        switch (terminal) {
            case AVANZA:
                // Calculamos la nueva posición
                int nextX = this.x, nextY = this.y;
                switch (this.direccion) {
                    case 0: nextY = (this.y - 1 + 32) % 32; break; // Norte
                    case 1: nextX = (this.x + 1) % 32;     break; // Este
                    case 2: nextY = (this.y + 1) % 32;     break; // Sur
                    case 3: nextX = (this.x - 1 + 32) % 32; break; // Oeste
                }
                // Movemos la hormiga
                this.x = nextX;
                this.y = nextY;
                // Comprobamos si hay comida en la nueva casilla y la consumimos
                if (this.comida[this.y][this.x]) {
                    this.comida[this.y][this.x] = false; // Comemos
                    this.comidaConsumida++;
                }
                break; // Fin AVANZA

            case DERECHA:
                this.direccion = (this.direccion + 1) % 4; // 0->1, 1->2, 2->3, 3->0
                break;

            case IZQUIERDA:
                this.direccion = (this.direccion + 3) % 4; // 0->3, 1->0, 2->1, 3->2
                break;
        }
    }

    /**
     * Método que calcula y devuelve la ruta seguida por la hormiga durante una simulación.
     * Ejecuta una simulación completa desde el inicio usando la lógica de 'evaluar'.
     * @return Una matriz booleana donde true indica que la hormiga pasó por esa celda.
     */
    public boolean[][] obtenerRutaRecorrida() {
        boolean[][] ruta = new boolean[32][32];
        inicializarSimulacion(); // Reiniciamos estado para la simulación de ruta
        if (raiz == null) return ruta; // Si no hay árbol, la ruta es solo el inicio

        Queue<Terminal> accionesPendientes = new LinkedList<>();
        ruta[this.y][this.x] = true; // Marcamos la posición inicial

        // Bucle principal de simulación para la ruta
        while (this.pasos < 400 && this.comidaConsumida < 89) {
            if (accionesPendientes.isEmpty()) {
                obtenerSiguienteSecuenciaAcciones(this.raiz, accionesPendientes);
                if (accionesPendientes.isEmpty()) {
                    break;
                }
            }

            Terminal accionActual = accionesPendientes.poll();
            if (accionActual != null) {
                ejecutarAccion(accionActual);
                // Marcamos la nueva posición DESPUÉS de ejecutar la acción
                ruta[this.y][this.x] = true;
            }
        }
        return ruta;
    }


    /**
     * Método que obtiene el fitness del individuo.
     * Llama a evaluar() para calcular la comida consumida.
     * @return El valor del fitness (comida consumida).
     */
    @Override
    public double getFitness() {
        // El fitness es directamente la cantidad de comida consumida
        return (double) evaluar();
    }

    /**
     * Método privado que comprueba si hay comida en la celda directamente enfrente de la hormiga.
     * No consume pasos.
     * @return true si hay comida delante, false en caso contrario.
     */
    private boolean hayComidaDelante() {
        int nx = this.x, ny = this.y;
        // Calculamos la casilla delante según la dirección actual
        switch (this.direccion) {
            case 0: ny = (this.y - 1 + 32) % 32; break; // Norte
            case 1: nx = (this.x + 1) % 32;     break; // Este
            case 2: ny = (this.y + 1) % 32;     break; // Sur
            case 3: nx = (this.x - 1 + 32) % 32; break; // Oeste
        }
        // Devolvemos true si hay comida en la casilla calculada (nx, ny)
        // Nos aseguramos que nx, ny estén dentro de los límites (aunque % debería manejarlo)
        if (ny >= 0 && ny < 32 && nx >= 0 && nx < 32) {
            return this.comida[ny][nx];
        }
        return false; // Fuera de límites (no debería pasar con %)
    }

    // --- Métodos de Generación de Árboles ---

    /**
     * Método estático que genera el mapa booleano inicial a partir de las coordenadas predefinidas.
     * @return Una matriz booleana 32x32 con la disposición inicial de la comida.
     */
    public static boolean[][] generarMapaComidaInicial() {
        boolean[][] mapa = new boolean[32][32]; // Inicializado a false por defecto
        for (int[] coord : SANTA_FE_TRAIL_COORDS) {
            if (coord[0] >= 0 && coord[0] < 32 && coord[1] >= 0 && coord[1] < 32) {
                mapa[coord[0]][coord[1]] = true;
            }
        }
        return mapa;
    }

    /**
     * Método privado recursivo que genera un árbol de programa aleatorio.
     * @param profundidadMaxima Profundidad máxima deseada para el árbol.
     * @param metodo Método de generación ("Completa", "Creciente", "Ramped").
     * @param nivelActual Nivel de profundidad actual en la recursión.
     * @return El nodo raíz del subárbol generado.
     */
    private Nodo generarArbol(int profundidadMaxima, String metodo, int nivelActual) {
        // Condición de parada: Profundidad máxima alcanzada
        if (nivelActual >= profundidadMaxima) {
            return new Nodo(Terminal.obtenerAleatorio()); // Creamos un terminal
        }

        // Aseguramos profundidad mínima de 2 (si la profundidad máxima lo permite)
        boolean forzarFuncion = (nivelActual < 2 && profundidadMaxima >= 2);

        boolean esHoja;
        if (forzarFuncion) {
            esHoja = false;
        } else {
            // Decidimos si crear una función o un terminal según el método
            double probTerminal = 0.4; // Probabilidad base de crear un terminal (ajustable)

            if (metodo.equalsIgnoreCase("Completa")) {
                // En Completa, solo somos hoja en la profundidad máxima exacta
                esHoja = (nivelActual >= profundidadMaxima); // Ya manejado arriba, redundante aquí
                 esHoja = false; // Si no estamos en profMax, somos función
            } else if (metodo.equalsIgnoreCase("Creciente")) {
                // En Creciente, podemos ser hoja antes de la profMax (después de prof min 2)
                esHoja = (random.nextDouble() < probTerminal);
            } else { // Ramped Half-and-Half (aproximado)
                if (random.nextBoolean()) { // Mitad de las veces: tipo Completa
                    esHoja = false; // Función hasta alcanzar profMax
                } else { // Mitad de las veces: tipo Creciente
                    esHoja = (random.nextDouble() < probTerminal);
                }
            }
             // Si llegamos a la profundidad máxima, DEBEMOS ser hoja
             if (nivelActual >= profundidadMaxima -1) { // penúltimo nivel, hijos serán hojas
                  if (metodo.equalsIgnoreCase("Completa")) esHoja = false; // Debe ser función
                  else if(metodo.equalsIgnoreCase("Creciente")) esHoja = random.nextDouble() < probTerminal;
                  else { // Ramped
                     if (random.nextBoolean()) esHoja = false;
                     else esHoja = random.nextDouble() < probTerminal;
                  }
             }
             if (nivelActual >= profundidadMaxima) { // Último nivel posible
                  esHoja = true;
             }

        }


        if (esHoja) {
            // Creamos un nodo terminal aleatorio
            return new Nodo(Terminal.obtenerAleatorio());
        } else {
            // Creamos un nodo de función aleatorio
            Funcion funcion = Funcion.obtenerAleatoria();
            int numHijos = (funcion == Funcion.PROG3) ? 3 : 2;
            Nodo hijo1 = generarArbol(profundidadMaxima, metodo, nivelActual + 1);
            Nodo hijo2 = generarArbol(profundidadMaxima, metodo, nivelActual + 1);
            Nodo hijo3 = (numHijos == 3) ? generarArbol(profundidadMaxima, metodo, nivelActual + 1) : null;

            if (numHijos == 3) {
                 return new Nodo(funcion, hijo1, hijo2, hijo3);
            } else {
                 return new Nodo(funcion, hijo1, hijo2);
            }
        }
    }

    // --- Métodos de Operaciones Genéticas (Mutación y Cruce) ---

    /**
     * Método que aplica el operador de mutación terminal.
     * Recorre el árbol y cambia ALGUNOS nodos terminales por otros aleatorios.
     * La decisión de SI este individuo muta se toma fuera (en PoblacionHormiga).
     * Este método implementa la lógica de QUÉ cambiar si la mutación ocurre.
     */
    public void mutacionTerminal() {
        if (this.raiz == null) return;
        // Llamamos a un helper recursivo
        mutacionTerminalRecursivo(this.raiz);
    }

    /** Helper recursivo para mutación terminal */
    private void mutacionTerminalRecursivo(Nodo nodo) {
        if (nodo == null) return;

        if (nodo.esTerminal()) {
            // Decidimos si ESTE nodo terminal específico muta
            // Podemos usar una probabilidad fija interna o una pasada de algún modo.
            // Usemos una probabilidad fija pequeña aquí para que no todos los terminales cambien.
            double probCambioNodo = 0.1; // Probabilidad de que un nodo específico mute
            if (random.nextDouble() < probCambioNodo) {
                nodo.terminal = Terminal.obtenerAleatorio();
            }
        } else {
            // Continuamos la búsqueda recursivamente en los hijos
            mutacionTerminalRecursivo(nodo.hijo1);
            mutacionTerminalRecursivo(nodo.hijo2);
            mutacionTerminalRecursivo(nodo.hijo3); // No importa si es null
        }
    }

    /**
     * Método que aplica el operador de mutación funcional.
     * Recorre el árbol y cambia ALGUNAS funciones por otras de la misma aridad.
     * La decisión de SI este individuo muta se toma fuera.
     */
    public void mutacionFuncional() {
         if (this.raiz == null) return;
         mutacionFuncionalRecursivo(this.raiz);
    }

    /** Helper recursivo para mutación funcional */
    private void mutacionFuncionalRecursivo(Nodo nodo) {
         if (nodo == null || nodo.esTerminal()) return;

         // Decidimos si ESTE nodo funcional específico muta
         double probCambioNodo = 0.1;
         if (random.nextDouble() < probCambioNodo) {
             int aridadActual = (nodo.funcion == Funcion.PROG3) ? 3 : 2;
             List<Funcion> posibles = new ArrayList<>();

             // Buscamos funciones alternativas con la misma aridad
             for (Funcion f : Funcion.values()) {
                 int aridadF = (f == Funcion.PROG3) ? 3 : 2;
                 if (aridadF == aridadActual && f != nodo.funcion) { // Excluimos la actual
                     posibles.add(f);
                 }
             }
             // Si encontramos alternativas, elegimos una al azar
             if (!posibles.isEmpty()) {
                 nodo.funcion = posibles.get(random.nextInt(posibles.size()));
             }
         }
         // Continuamos la búsqueda recursivamente en los hijos
         mutacionFuncionalRecursivo(nodo.hijo1);
         mutacionFuncionalRecursivo(nodo.hijo2);
         mutacionFuncionalRecursivo(nodo.hijo3);
    }


    /**
     * Método que aplica el operador de mutación de subárbol.
     * Elige un nodo aleatorio (excepto raíz quizás) y lo reemplaza por un subárbol nuevo.
     * La decisión de SI este individuo muta se toma fuera.
     */
    public void mutacionSubarbol() {
        if (this.raiz == null) return;

        List<Nodo> nodos = new ArrayList<>();
        getAllNodos(this.raiz, nodos); // Obtenemos todos los nodos
        if (nodos.size() <= 1) return; // No hacemos nada si solo está la raíz o está vacío

        // Elegimos un nodo aleatorio para reemplazar (excluimos la raíz para no cambiar todo siempre)
        Nodo nodoAReemplazar = nodos.get(random.nextInt(nodos.size() - 1) + 1); // Índice 1 en adelante

        // Determinamos una profundidad máxima para el nuevo subárbol
        int profundidadNodo = getNodoDepth(this.raiz, nodoAReemplazar, 0);
         // Evitamos prof -1 si el nodo no se encontrara (no debería pasar aquí)
         profundidadNodo = Math.max(0, profundidadNodo);
        int maxProfNuevo = Math.max(2, this.profundidadMaximaControlBloating - profundidadNodo);

        // Generamos un nuevo subárbol aleatorio
        Nodo nuevoSubarbol = generarArbol(maxProfNuevo, "Ramped", 0); // Usamos método configurable

        // Reemplazamos el contenido del nodo elegido por el del nuevo subárbol
        // (Esto es más seguro que asignar directamente a los hijos por si nodoAReemplazar fuera la raíz accidentalmente)
         if (nodoAReemplazar != null) { // Chequeo extra de seguridad
             nodoAReemplazar.terminal = nuevoSubarbol.terminal;
             nodoAReemplazar.funcion = nuevoSubarbol.funcion;
             nodoAReemplazar.hijo1 = nuevoSubarbol.hijo1;
             nodoAReemplazar.hijo2 = nuevoSubarbol.hijo2;
             nodoAReemplazar.hijo3 = nuevoSubarbol.hijo3;
         }


        // Controlamos bloating después de la mutación
        limitarProfundidad(this.raiz, 0, this.profundidadMaximaControlBloating);
    }


    /**
     * Método que aplica el operador de mutación Hoist.
     * Elige un nodo funcional aleatorio y lo reemplaza por uno de sus hijos (clonado).
     * La decisión de SI este individuo muta se toma fuera.
     */
    public void mutacionHoist() {
        if (this.raiz == null) return;

        List<Nodo> nodosFuncionales = new ArrayList<>();
        getAllNodosFuncionales(this.raiz, nodosFuncionales); // Obtenemos nodos internos
        if (nodosFuncionales.isEmpty()) return; // No hay nodos para hacer hoist

        // Elegimos un nodo funcional aleatorio
        Nodo nodoHoist = nodosFuncionales.get(random.nextInt(nodosFuncionales.size()));

        List<Nodo> hijos = new ArrayList<>();
        if (nodoHoist.hijo1 != null) hijos.add(nodoHoist.hijo1);
        if (nodoHoist.hijo2 != null) hijos.add(nodoHoist.hijo2);
        if (nodoHoist.hijo3 != null) hijos.add(nodoHoist.hijo3);

        if (!hijos.isEmpty()) {
            // Elegimos uno de los hijos al azar para que "suba"
            Nodo hijoElegido = hijos.get(random.nextInt(hijos.size())).clone(); // Clonamos el hijo

            // Reemplazamos el contenido del nodoHoist por el del hijoElegido clonado
            nodoHoist.terminal = hijoElegido.terminal;
            nodoHoist.funcion = hijoElegido.funcion;
            nodoHoist.hijo1 = hijoElegido.hijo1;
            nodoHoist.hijo2 = hijoElegido.hijo2;
            nodoHoist.hijo3 = hijoElegido.hijo3;

            // Controlamos bloating
             limitarProfundidad(this.raiz, 0, this.profundidadMaximaControlBloating);
        }
    }

    /**
     * Método que aplica el operador de cruce estándar de GP.
     * Intercambia subárboles seleccionados aleatoriamente entre este individuo y otro.
     * @param otro El otro individuo con el que cruzar.
     */
    public void cruzarCon(IndividuoHormiga otro) {
        if (this.raiz == null || otro.raiz == null) return; // No podemos cruzar si alguno no tiene árbol

        // 1. Obtenemos lista de todos los nodos para cada padre
        List<Nodo> nodosPadre1 = new ArrayList<>();
        getAllNodos(this.raiz, nodosPadre1);
        List<Nodo> nodosPadre2 = new ArrayList<>();
        getAllNodos(otro.raiz, nodosPadre2);

        if (nodosPadre1.isEmpty() || nodosPadre2.isEmpty()) return; // No podemos cruzar si algún árbol está vacío

        // 2. Seleccionamos un punto de cruce (nodo) aleatorio en cada padre
        Nodo puntoCruce1 = nodosPadre1.get(random.nextInt(nodosPadre1.size()));
        Nodo puntoCruce2 = nodosPadre2.get(random.nextInt(nodosPadre2.size()));

        // 3. Clonamos los subárboles seleccionados para evitar referencias compartidas
        Nodo subarbol1Clonado = puntoCruce1.clone();
        Nodo subarbol2Clonado = puntoCruce2.clone();

        // 4. Reemplazamos el contenido del punto de cruce 1 con el del subárbol 2 clonado
        puntoCruce1.terminal = subarbol2Clonado.terminal;
        puntoCruce1.funcion = subarbol2Clonado.funcion;
        puntoCruce1.hijo1 = subarbol2Clonado.hijo1;
        puntoCruce1.hijo2 = subarbol2Clonado.hijo2;
        puntoCruce1.hijo3 = subarbol2Clonado.hijo3;

        // 5. Reemplazamos el contenido del punto de cruce 2 con el del subárbol 1 clonado
        puntoCruce2.terminal = subarbol1Clonado.terminal;
        puntoCruce2.funcion = subarbol1Clonado.funcion;
        puntoCruce2.hijo1 = subarbol1Clonado.hijo1;
        puntoCruce2.hijo2 = subarbol1Clonado.hijo2;
        puntoCruce2.hijo3 = subarbol1Clonado.hijo3;

        // 6. Controlamos el bloating (limitamos profundidad) DESPUÉS del intercambio
        limitarProfundidad(this.raiz, 0, this.profundidadMaximaControlBloating);
        limitarProfundidad(otro.raiz, 0, otro.profundidadMaximaControlBloating); // Usar la profundidad del otro individuo
    }


    // --- Métodos Auxiliares y de Acceso ---

    /**
     * Método que obtiene la raíz del árbol de programa.
     * @return El nodo raíz.
     */
    public Nodo getRaiz() {
        return this.raiz;
    }

    /**
     * Método que asigna una nueva raíz al árbol (útil después de operaciones genéticas).
     * @param nuevaRaiz El nuevo nodo raíz.
     */
    public void setRaiz(Nodo nuevaRaiz) {
        this.raiz = nuevaRaiz;
    }

    /**
     * Método auxiliar recursivo para obtener todos los nodos de un subárbol.
     * @param nodo Nodo raíz del subárbol.
     * @param lista Lista donde se añadirán los nodos encontrados.
     */
    private void getAllNodos(Nodo nodo, List<Nodo> lista) {
        if (nodo == null) return;
        lista.add(nodo); // Añadimos el nodo actual
        // Llamamos recursivamente para los hijos
        getAllNodos(nodo.hijo1, lista);
        getAllNodos(nodo.hijo2, lista);
        getAllNodos(nodo.hijo3, lista);
    }

    /**
     * Método auxiliar recursivo para obtener solo los nodos funcionales (internos) de un subárbol.
     * @param nodo Nodo raíz del subárbol.
     * @param lista Lista donde se añadirán los nodos funcionales encontrados.
     */
    private void getAllNodosFuncionales(Nodo nodo, List<Nodo> lista) {
        if (nodo == null || nodo.esTerminal()) return; // Ignoramos nulls y terminales
        lista.add(nodo); // Añadimos el nodo funcional actual
        // Llamamos recursivamente para los hijos
        getAllNodosFuncionales(nodo.hijo1, lista);
        getAllNodosFuncionales(nodo.hijo2, lista);
        getAllNodosFuncionales(nodo.hijo3, lista);
    }

     /**
      * Método auxiliar para obtener la profundidad de un nodo específico dentro del árbol.
      * @param root Raíz del árbol completo.
      * @param target Nodo objetivo cuya profundidad buscamos.
      * @param currentDepth Profundidad actual en la recursión.
      * @return La profundidad del nodo objetivo, o -1 si no se encuentra.
      */
     private int getNodoDepth(Nodo root, Nodo target, int currentDepth) {
          if (root == null) return -1;
          if (root == target) return currentDepth;

          int depth = getNodoDepth(root.hijo1, target, currentDepth + 1);
          if (depth != -1) return depth;
          depth = getNodoDepth(root.hijo2, target, currentDepth + 1);
          if (depth != -1) return depth;
          depth = getNodoDepth(root.hijo3, target, currentDepth + 1);
          return depth;
     }


    /**
     * Método privado para controlar el bloating limitando la profundidad máxima del árbol.
     * Convierte los nodos que exceden la profundidad en terminales aleatorios.
     * @param nodo Nodo actual en la recursión.
     * @param nivelActual Profundidad actual del nodo.
     * @param profundidadMaxima Límite de profundidad permitido.
     */
    private void limitarProfundidad(Nodo nodo, int nivelActual, int profundidadMaxima) {
        if (nodo == null) {
            return;
        }
        // Si este nodo está en el último nivel permitido O MÁS ALLÁ
        if (nivelActual >= profundidadMaxima) {
             // Si es una función, la convertimos en terminal
            if (!nodo.esTerminal()) {
                nodo.funcion = null;
                nodo.terminal = Terminal.obtenerAleatorio();
                nodo.hijo1 = null; // Eliminamos referencias a hijos
                nodo.hijo2 = null;
                nodo.hijo3 = null;
            }
             // Si ya es terminal, no hacemos nada
        } else {
             // Si no hemos alcanzado el límite, continuamos recursivamente
             if (!nodo.esTerminal()){
                  limitarProfundidad(nodo.hijo1, nivelActual + 1, profundidadMaxima);
                  limitarProfundidad(nodo.hijo2, nivelActual + 1, profundidadMaxima);
                  limitarProfundidad(nodo.hijo3, nivelActual + 1, profundidadMaxima);
             }
        }
    }


    /**
     * Método que genera una representación en cadena del árbol del individuo.
     * @return String representando el árbol.
     */
    @Override
    public String toString() {
        if (raiz == null) return "ARBOL_NULO";
        return raiz.toString();
    }

    /**
     * Método que crea y devuelve una copia profunda (clon) de este individuo.
     * Clona la estructura del árbol, pero no el estado de la simulación (se reinicia al evaluar).
     * @return Un nuevo objeto IndividuoHormiga idéntico a este.
     */
    @Override
    public IndividuoHormiga clone() {
        // Creamos un nuevo individuo usando el constructor que establece la profundidad de bloating
        IndividuoHormiga nuevo = new IndividuoHormiga(this.profundidadMaximaControlBloating);
        // Clonamos la raíz del árbol si existe
        nuevo.raiz = (this.raiz != null) ? this.raiz.clone() : null;
        // El estado de la simulación (x, y, comida, etc.) NO se clona aquí,
        // ya que se reinicia con inicializarSimulacion() antes de cada evaluación.
        return nuevo;
    }

    // Métodos para obtener estado (útiles para visualización externa)
    /** Método que obtiene la coordenada X actual. */
    public int getX() { return x; }
    /** Método que obtiene la coordenada Y actual. */
    public int getY() { return y; }
    /** Método que obtiene la dirección actual. */
    public int getDireccion() { return direccion; }
    /** Método que obtiene el número de pasos ejecutados. */
    public int getPasos() { return pasos; }
    /** Método que obtiene la cantidad de comida consumida. */
    public int getComidaConsumida() { return comidaConsumida; }
    /** Método que obtiene el mapa de comida actual (puede haber sido modificada). */
    public boolean[][] getComida() { return comida; }

}

// ========================================================================
// --- Clases Auxiliares (Nodo, Funcion, Terminal) ---
// ========================================================================

/**
 * Clase que representa un nodo en el árbol de programa genético.
 * Puede ser un nodo de función (interno) o un nodo terminal (hoja).
 */
class Nodo {
    /** El terminal si este nodo es una hoja (null si es función). */
    Terminal terminal;
    /** La función si este nodo es interno (null si es terminal). */
    Funcion funcion;
    /** Referencia al primer hijo (si es función). */
    Nodo hijo1;
    /** Referencia al segundo hijo (si es función). */
    Nodo hijo2;
    /** Referencia al tercer hijo (si es PROG3). */
    Nodo hijo3;

    // --- Constructores ---

    /**
     * Constructor para crear un nodo hoja (terminal).
     * @param terminal El terminal que representa este nodo.
     */
    Nodo(Terminal terminal) {
        if (terminal == null) throw new IllegalArgumentException("El terminal no puede ser null para un nodo hoja.");
        this.terminal = terminal;
        this.funcion = null;
        this.hijo1 = null;
        this.hijo2 = null;
        this.hijo3 = null;
    }

    /**
     * Constructor para crear un nodo de función binaria (SICOMIDA, PROG2).
     * @param funcion La función binaria.
     * @param h1 El primer hijo.
     * @param h2 El segundo hijo.
     */
    Nodo(Funcion funcion, Nodo h1, Nodo h2) {
        if (funcion == null || funcion == Funcion.PROG3) {
            throw new IllegalArgumentException("Función inválida para constructor binario.");
        }
        if (h1 == null || h2 == null) {
            throw new IllegalArgumentException("Los hijos de una función binaria no pueden ser null.");
        }
        this.terminal = null;
        this.funcion = funcion;
        this.hijo1 = h1;
        this.hijo2 = h2;
        this.hijo3 = null;
    }

    /**
     * Constructor para crear un nodo de función ternaria (PROG3).
     * @param funcion La función PROG3.
     * @param h1 El primer hijo.
     * @param h2 El segundo hijo.
     * @param h3 El tercer hijo.
     */
    Nodo(Funcion funcion, Nodo h1, Nodo h2, Nodo h3) {
        if (funcion != Funcion.PROG3) {
            throw new IllegalArgumentException("Función inválida para constructor ternario.");
        }
         if (h1 == null || h2 == null || h3 == null) {
            throw new IllegalArgumentException("Los hijos de PROG3 no pueden ser null.");
        }
        this.terminal = null;
        this.funcion = funcion;
        this.hijo1 = h1;
        this.hijo2 = h2;
        this.hijo3 = h3;
    }

    /**
     * Método que comprueba si el nodo es terminal (una hoja).
     * @return true si es un nodo terminal, false si es un nodo de función.
     */
    boolean esTerminal() {
        // Un nodo es terminal si su función es null (y por tanto, terminal no es null)
        return this.funcion == null;
    }
    
    public int contarNodos() {
        if (esTerminal()) {
            return 1; // Un nodo hoja cuenta como 1
        } else {
            int count = 1; // Contamos el nodo actual (la función)
            if (hijo1 != null) count += hijo1.contarNodos();
            if (hijo2 != null) count += hijo2.contarNodos();
            if (hijo3 != null) count += hijo3.contarNodos();
            return count;
        }
    }

    /**
     * Método que genera una representación en cadena del subárbol a partir de este nodo.
     * @return String representando el subárbol.
     */
    @Override
    public String toString() {
        if (esTerminal()) {
            return terminal.toString();
        } else {
            String s = funcion + "(" + (hijo1 != null ? hijo1.toString() : "ERR:H1_NULL");
            if (hijo2 != null) s += ", " + hijo2.toString(); else s += ", ERR:H2_NULL";
            if (hijo3 != null) s += ", " + hijo3.toString(); // Solo se añade si existe (PROG3)
            s += ")";
            return s;
        }
    }

    /**
     * Método que crea y devuelve una copia profunda (clon) de este nodo y su subárbol.
     * @return Un nuevo objeto Nodo idéntico a este.
     */
    @Override
    public Nodo clone() {
        if (esTerminal()) {
            // Clonar un terminal es simplemente crear uno nuevo con el mismo valor
            return new Nodo(this.terminal);
        } else {
            // Clonar una función implica clonar recursivamente sus hijos
            Nodo clonHijo1 = (this.hijo1 != null) ? this.hijo1.clone() : null;
            Nodo clonHijo2 = (this.hijo2 != null) ? this.hijo2.clone() : null;
            Nodo clonHijo3 = (this.hijo3 != null) ? this.hijo3.clone() : null; // Clonamos aunque sea null

            if (this.funcion == Funcion.PROG3) {
                return new Nodo(this.funcion, clonHijo1, clonHijo2, clonHijo3);
            } else {
                return new Nodo(this.funcion, clonHijo1, clonHijo2);
            }
        }
    }
}

/**
 * Enumeración que define las funciones disponibles en el conjunto de funciones de GP.
 */
enum Funcion {
    SICOMIDA, PROG2, PROG3;
    /** Generador de números aleatorios para selección. */
    private static final Random random = new Random();
    /**
     * Función que devuelve una función aleatoria del conjunto disponible.
     * @return Una Funcion aleatoria.
     */
    static Funcion obtenerAleatoria() {
        return values()[random.nextInt(values().length)];
    }
}

/**
 * Enumeración que define las acciones terminales disponibles en el conjunto de terminales de GP.
 */
enum Terminal {
    AVANZA, DERECHA, IZQUIERDA;
    /** Generador de números aleatorios para selección. */
    private static final Random random = new Random();
    /**
     * Función que devuelve un terminal aleatorio del conjunto disponible.
     * @return Un Terminal aleatorio.
     */
    static Terminal obtenerAleatorio() {
        return values()[random.nextInt(values().length)];
    }
}
