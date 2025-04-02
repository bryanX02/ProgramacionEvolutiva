package modelo;

import java.util.*;

public class IndividuoRobot extends Individuo<Integer> {

    private final RutaCache rutaCache = new RutaCache(); // Caché de rutas

    // CONSTRUCTOR Y CONFIGURACIÓN INICIAL
    public IndividuoRobot() {
        this.tamGenes = new int[]{NUM_HABITACIONES};
        List<Integer> habitaciones = new ArrayList<>(HABITACIONES.keySet());
        Collections.shuffle(habitaciones);
        this.cromosoma = habitaciones.toArray(new Integer[0]);
    }

    // SETTERS Y GETTERS
    public void setCromosoma(List<Integer> cromosomaLista) {
        this.cromosoma = cromosomaLista.toArray(new Integer[0]);
    }

    // FUNCIONES DE RUTA Y FITNESS
    public List<int[]> getRuta() {
        List<int[]> ruta = new ArrayList<>();
        ruta.add(BASE);
        int[] actual = BASE;

        for (int habitacion : cromosoma) {
            int[] destino = HABITACIONES.get(habitacion);

            // Se verifica si la ruta ya está en el caché
            List<int[]> tramo = rutaCache.obtenerRuta(actual, destino);
            if (tramo == null) {
                tramo = aEstrella(actual, destino);
                if (tramo == null || tramo.size() <= 1) return null; // Evitar rutas vacías o inválidas
                rutaCache.guardarRuta(actual, destino, tramo); // Guardar en caché
            }

            if (tramo.size() > 1) { // Asegurar que hay más de un punto
                ruta.addAll(tramo.subList(1, tramo.size()));
            }
            actual = destino;
        }

        List<int[]> regreso = rutaCache.obtenerRuta(actual, BASE);
        if (regreso == null) {
            regreso = aEstrella(actual, BASE);
            if (regreso == null || regreso.size() <= 1) return null; // Evitar rutas vacías o inválidas
            rutaCache.guardarRuta(actual, BASE, regreso); // Guardar en caché
        }

        if (regreso.size() > 1) { // Asegurar que hay más de un punto
            ruta.addAll(regreso.subList(1, regreso.size()));
        }

        return limpiarRuta(ruta);
    }

    private List<int[]> limpiarRuta(List<int[]> ruta) {
        List<int[]> rutaLimpia = new ArrayList<>();
        int[] anterior = null;
        for (int[] punto : ruta) {
            if (anterior == null || !Arrays.equals(punto, anterior)) {
                rutaLimpia.add(punto);
            }
            anterior = punto;
        }
        return rutaLimpia;
    }

    public double getFitness() {
        int distanciaTotal = 0;
        int[] actual = BASE;

        for (int habitacion : cromosoma) {
            int[] destino = HABITACIONES.get(habitacion);

            // Verificar si la ruta ya está en el caché
            List<int[]> tramo = rutaCache.obtenerRuta(actual, destino);
            if (tramo == null) {
                tramo = aEstrella(actual, destino);
                if (tramo == null || tramo.size() <= 1) {
                    return 1000; // Penalización alta si no hay ruta válida
                }
                rutaCache.guardarRuta(actual, destino, tramo); // Guardar en caché
            }

            distanciaTotal += tramo.size();
            actual = destino;
        }

        List<int[]> regreso = rutaCache.obtenerRuta(actual, BASE);
        if (regreso == null) {
            regreso = aEstrella(actual, BASE);
            if (regreso == null || regreso.size() <= 1) {
                return 1000; // Penalización alta si no hay ruta válida
            }
            rutaCache.guardarRuta(actual, BASE, regreso); // Guardar en caché
        }
        distanciaTotal += regreso.size();

        return distanciaTotal;
    }


    private List<int[]> aEstrella(int[] inicio, int[] destino) {
        PriorityQueue<Nodo> frontera = new PriorityQueue<>(Comparator.comparingDouble(n -> n.prioridad));
        Set<Integer> visitados = new HashSet<>();
        Map<Integer, int[]> padre = new HashMap<>();
        Map<Integer, Integer> costos = new HashMap<>();

        int inicioKey = clave(inicio);
        int destinoKey = clave(destino);
        frontera.add(new Nodo(inicio, 0, heuristica(inicio, destino)));
        costos.put(inicioKey, 0);
        padre.put(inicioKey, null);

        while (!frontera.isEmpty()) {
            Nodo actual = frontera.poll();
            int actualKey = clave(actual.pos);
            if (actualKey == destinoKey) return reconstruirCamino(padre, destinoKey);
            if (!visitados.add(actualKey)) continue;

            for (int[] dir : DIRECCIONES) {
                int[] vecino = {actual.pos[0] + dir[0], actual.pos[1] + dir[1]};
                int vecinoKey = clave(vecino);
                if (!esValido(vecino) || visitados.contains(vecinoKey)) continue;
                int nuevoCosto = costos.get(actualKey) + 1;
                if (!costos.containsKey(vecinoKey) || nuevoCosto < costos.get(vecinoKey)) {
                    costos.put(vecinoKey, nuevoCosto);
                    frontera.add(new Nodo(vecino, nuevoCosto, nuevoCosto + heuristica(vecino, destino)));
                    padre.put(vecinoKey, actual.pos);
                }
            }
        }
        return null;
    }

    private List<int[]> reconstruirCamino(Map<Integer, int[]> padre, int destinoKey) {
        List<int[]> camino = new ArrayList<>();
        int[] paso = padre.get(destinoKey);
        while (paso != null) {
            camino.add(paso);
            paso = padre.get(clave(paso));
        }
        Collections.reverse(camino);
        return camino;
    }

    private boolean esValido(int[] pos) {
        if (pos[0] < 0 || pos[0] >= GRID_SIZE || pos[1] < 0 || pos[1] >= GRID_SIZE) {
            return false;
        }
        for (int[] obstaculo : OBSTACULOS) {
            if (Arrays.equals(pos, obstaculo)) {
                return false;
            }
        }
        return true;
    }

    private int clave(int[] pos) {
        return pos[0] * GRID_SIZE + pos[1];
    }

    private double heuristica(int[] a, int[] b) {
        return Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2));
    }

    // CLASE INTERNA PARA NODOS DEL ALGORITMO A*
    private static class Nodo {
        int[] pos;
        int costo;
        double prioridad;

        Nodo(int[] pos, int costo, double prioridad) {
            this.pos = pos;
            this.costo = costo;
            this.prioridad = prioridad;
        }
    }
    
    public String[][] generarMapa() {
        String[][] mapa = new String[GRID_SIZE][GRID_SIZE];

        // Inicializar mapa vacío con espacios formateados
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                mapa[i][j] = "   ";  // Tres espacios para uniformidad
            }
        }

        // Colocar obstáculos
        for (int[] obstaculo : OBSTACULOS) {
            mapa[obstaculo[0]][obstaculo[1]] = "■  ";
        }

        // Obtener la ruta y dibujar con '*'
        List<int[]> ruta = getRuta();
        for (int i = 1; i < ruta.size(); i++) {
            int[] origen = ruta.get(i - 1);
            int[] destino = ruta.get(i);
            int x = origen[0], y = origen[1];

            while (x != destino[0] || y != destino[1]) {
                if (x != destino[0]) {
                    x += (x < destino[0]) ? 1 : -1;
                } else if (y != destino[1]) {
                    y += (y < destino[1]) ? 1 : -1;
                }

                if (mapa[x][y].trim().isEmpty()) {  // No sobrescribir habitaciones u obstáculos
                    mapa[x][y] = "*  ";
                }
            }
        }

        // Colocar habitaciones numeradas con formato fijo
        for (Map.Entry<Integer, int[]> entry : HABITACIONES.entrySet()) {
            int num = entry.getKey();
            int[] coord = entry.getValue();
            mapa[coord[0]][coord[1]] = String.format("%02d ", num);  // Asegurar dos dígitos
        }

        // Marcar la base con 'B'
        mapa[BASE[0]][BASE[1]] = " B ";

        return mapa;
    }
    
    public void cruzarMonopunto(IndividuoRobot otro) {
        Random rand = new Random();
        int puntoCorte = rand.nextInt(NUM_HABITACIONES); // Índice válido

        Set<Integer> usados = new HashSet<>(Arrays.asList(Arrays.copyOfRange(this.cromosoma, 0, puntoCorte)));
        Integer[] nuevoCromosoma = new Integer[NUM_HABITACIONES];
        System.arraycopy(this.cromosoma, 0, nuevoCromosoma, 0, puntoCorte);

        int index = puntoCorte;
        for (int gene : otro.cromosoma) {
            if (!usados.contains(gene)) {
                nuevoCromosoma[index++] = gene;
            }
        }

        this.cromosoma = nuevoCromosoma;
    }

    public void cruzarUniforme(IndividuoRobot otro) {
        Random rand = new Random();
        Integer[] hijo1 = new Integer[NUM_HABITACIONES];
        Integer[] hijo2 = new Integer[NUM_HABITACIONES];
        Set<Integer> usadosHijo1 = new HashSet<>();
        Set<Integer> usadosHijo2 = new HashSet<>();

        // Cruce uniforme
        for (int i = 0; i < NUM_HABITACIONES; i++) {
            if (rand.nextBoolean()) {
                // Si el gen no ha sido usado en hijo1, lo asignamos
                if (!usadosHijo1.contains(this.cromosoma[i])) {
                    hijo1[i] = this.cromosoma[i];
                    usadosHijo1.add(hijo1[i]);
                }
                // Si el gen no ha sido usado en hijo2, lo asignamos
                if (!usadosHijo2.contains(otro.cromosoma[i])) {
                    hijo2[i] = otro.cromosoma[i];
                    usadosHijo2.add(hijo2[i]);
                }
            } else {
                // Si el gen no ha sido usado en hijo1, lo asignamos
                if (!usadosHijo1.contains(otro.cromosoma[i])) {
                    hijo1[i] = otro.cromosoma[i];
                    usadosHijo1.add(hijo1[i]);
                }
                // Si el gen no ha sido usado en hijo2, lo asignamos
                if (!usadosHijo2.contains(this.cromosoma[i])) {
                    hijo2[i] = this.cromosoma[i];
                    usadosHijo2.add(hijo2[i]);
                }
            }
        }

        // Completar genes faltantes sin repeticiones
        completarGenesFaltantes(hijo1, usadosHijo1);
        completarGenesFaltantes(hijo2, usadosHijo2);

        this.cromosoma = hijo1;
        otro.cromosoma = hijo2;
    }

    private void completarGenesFaltantes(Integer[] hijo, Set<Integer> usados) {
        List<Integer> faltantes = new ArrayList<>();
        for (int i = 1; i <= NUM_HABITACIONES; i++) {
            if (!usados.contains(i)) {
                faltantes.add(i);
            }
        }

        Collections.shuffle(faltantes); // Mezclar para evitar sesgos
        int index = 0;
        for (int i = 0; i < hijo.length; i++) {
            if (hijo[i] == null) {
                hijo[i] = faltantes.get(index++);
            }
        }
    }
    
    public void cruzarPMX(IndividuoRobot otro) {
        Random rand = new Random();
        int punto1 = rand.nextInt(NUM_HABITACIONES);
        int punto2 = rand.nextInt(NUM_HABITACIONES - punto1) + punto1;

        Integer[] hijo1 = new Integer[NUM_HABITACIONES];
        Integer[] hijo2 = new Integer[NUM_HABITACIONES];

        // Inicializar los hijos con valores nulos
        Arrays.fill(hijo1, null);
        Arrays.fill(hijo2, null);

        // Paso 1: Copiar el segmento entre los puntos de corte
        for (int i = punto1; i <= punto2; i++) {
            hijo1[i] = otro.cromosoma[i]; // Hijo 1 recibe el segmento del padre 2
            hijo2[i] = this.cromosoma[i]; // Hijo 2 recibe el segmento del padre 1
        }

        // Paso 2: Mapear los valores restantes
        for (int i = 0; i < NUM_HABITACIONES; i++) {
            if (i < punto1 || i > punto2) {
                // Para hijo1
                if (!contiene(hijo1, this.cromosoma[i])) {
                    hijo1[i] = this.cromosoma[i];
                } else {
                    hijo1[i] = mapearGen(this.cromosoma, otro.cromosoma, this.cromosoma[i], punto1, punto2);
                }

                // Para hijo2
                if (!contiene(hijo2, otro.cromosoma[i])) {
                    hijo2[i] = otro.cromosoma[i];
                } else {
                    hijo2[i] = mapearGen(otro.cromosoma, this.cromosoma, otro.cromosoma[i], punto1, punto2);
                }
            }
        }

        // Asignar los hijos a los padres
        this.cromosoma = hijo1;
        otro.cromosoma = hijo2;
    }

    private Integer mapearGen(Integer[] padre1, Integer[] padre2, int gen, int punto1, int punto2) {
        // Buscar el gen en el segmento copiado del padre2
        for (int i = punto1; i <= punto2; i++) {
            if (padre2[i] == gen) {
                return padre1[i]; // Devolver el gen correspondiente en el padre1
            }
        }
        return gen; // Si no se encuentra, devolver el gen original
    }

    private boolean contiene(Integer[] cromosoma, int gen) {
        for (Integer valor : cromosoma) {
            if (valor != null && valor == gen) {
                return true;
            }
        }
        return false;
    }
    
    public void cruzarOX(IndividuoRobot otro) {
        Random rand = new Random();
        int punto1 = rand.nextInt(NUM_HABITACIONES);
        int punto2 = rand.nextInt(NUM_HABITACIONES - punto1) + punto1;

        Integer[] hijo1 = new Integer[NUM_HABITACIONES];
        Integer[] hijo2 = new Integer[NUM_HABITACIONES];

        // Copiar el segmento entre los puntos de corte
        for (int i = punto1; i <= punto2; i++) {
            hijo1[i] = this.cromosoma[i];
            hijo2[i] = otro.cromosoma[i];
        }

        // Completar el resto de los genes
        completarOX(hijo1, otro.cromosoma, punto2);
        completarOX(hijo2, this.cromosoma, punto2);

        this.cromosoma = hijo1;
        otro.cromosoma = hijo2;
    }

    private void completarOX(Integer[] hijo, Integer[] padre, int punto2) {
        int index = (punto2 + 1) % NUM_HABITACIONES;
        for (int i = 0; i < NUM_HABITACIONES; i++) {
            if (!Arrays.asList(hijo).contains(padre[i])) {
                hijo[index] = padre[i];
                index = (index + 1) % NUM_HABITACIONES;
            }
        }
    }
    
    public void cruzarOXPP(IndividuoRobot otro) {
        Random rand = new Random();
        int punto1 = rand.nextInt(NUM_HABITACIONES);
        int punto2 = rand.nextInt(NUM_HABITACIONES - punto1) + punto1;

        Integer[] hijo1 = new Integer[NUM_HABITACIONES];
        Integer[] hijo2 = new Integer[NUM_HABITACIONES];

        // Copiar el segmento entre los puntos de corte
        for (int i = punto1; i <= punto2; i++) {
            hijo1[i] = this.cromosoma[i];
            hijo2[i] = otro.cromosoma[i];
        }

        // Completar el resto de los genes preservando la posición
        completarOXPP(hijo1, otro.cromosoma, punto1, punto2);
        completarOXPP(hijo2, this.cromosoma, punto1, punto2);

        this.cromosoma = hijo1;
        otro.cromosoma = hijo2;
    }

    private void completarOXPP(Integer[] hijo, Integer[] padre, int punto1, int punto2) {
        int indexHijo = 0;
        int indexPadre = 0;

        while (indexHijo < NUM_HABITACIONES) {
            if (indexHijo == punto1) {
                indexHijo = punto2 + 1; // Saltar el segmento copiado
            }

            if (indexHijo >= NUM_HABITACIONES) break;

            // Buscar el siguiente gen en el padre que no esté en el hijo
            while (indexPadre < NUM_HABITACIONES && Arrays.asList(hijo).contains(padre[indexPadre])) {
                indexPadre++;
            }

            if (indexPadre < NUM_HABITACIONES) {
                hijo[indexHijo] = padre[indexPadre];
                indexHijo++;
                indexPadre++;
            }
        }
    }
    
    public void cruzarCX(IndividuoRobot otro) {
        Integer[] hijo1 = new Integer[NUM_HABITACIONES];
        Integer[] hijo2 = new Integer[NUM_HABITACIONES];
        boolean[] visitados = new boolean[NUM_HABITACIONES];

        int index = 0;
        while (index < NUM_HABITACIONES) {
            if (!visitados[index]) {
                int cicloInicio = index;
                int actual = index;

                do {
                    hijo1[actual] = this.cromosoma[actual];
                    hijo2[actual] = otro.cromosoma[actual];
                    visitados[actual] = true;
                    actual = Arrays.asList(this.cromosoma).indexOf(otro.cromosoma[actual]);
                } while (actual != cicloInicio);
            }
            index++;
        }

        this.cromosoma = hijo1;
        otro.cromosoma = hijo2;
    }
    
    public void cruzarCO(IndividuoRobot otro) {
        Integer[] hijo1 = new Integer[NUM_HABITACIONES];
        Integer[] hijo2 = new Integer[NUM_HABITACIONES];

        // Convertir cromosomas a codificación ordinal
        List<Integer> ordinal1 = convertirAOrdinal(this.cromosoma);
        List<Integer> ordinal2 = convertirAOrdinal(otro.cromosoma);

        // Aplicar cruce uniforme en la codificación ordinal
        Random rand = new Random();
        for (int i = 0; i < NUM_HABITACIONES; i++) {
            if (rand.nextBoolean()) {
                hijo1[i] = ordinal1.get(i);
                hijo2[i] = ordinal2.get(i);
            } else {
                hijo1[i] = ordinal2.get(i);
                hijo2[i] = ordinal1.get(i);
            }
        }

        // Convertir de vuelta a cromosomas
        this.cromosoma = convertirDeOrdinal(hijo1);
        otro.cromosoma = convertirDeOrdinal(hijo2);
    }

    private List<Integer> convertirAOrdinal(Integer[] cromosoma) {
        List<Integer> ordinal = new ArrayList<>();
        List<Integer> lista = new ArrayList<>(Arrays.asList(cromosoma));

        for (int i = 0; i < NUM_HABITACIONES; i++) {
            ordinal.add(lista.indexOf(i + 1));
            lista.remove((Integer) (i + 1));
        }

        return ordinal;
    }

    private Integer[] convertirDeOrdinal(Integer[] ordinal) {
        List<Integer> cromosoma = new ArrayList<>();
        List<Integer> lista = new ArrayList<>();
        for (int i = 1; i <= NUM_HABITACIONES; i++) {
            lista.add(i);
        }

        for (int i = 0; i < NUM_HABITACIONES; i++) {
            cromosoma.add(lista.get(ordinal[i]));
            lista.remove((int) ordinal[i]);
        }

        return cromosoma.toArray(new Integer[0]);
    }
    
    public void cruzarERX(IndividuoRobot otro) {
        // Crear una lista de vecinos para cada habitación
        Map<Integer, Set<Integer>> vecinos = new HashMap<>();
        for (int i = 1; i <= NUM_HABITACIONES; i++) {
            vecinos.put(i, new HashSet<>());
        }

        // Agregar vecinos de ambos padres
        agregarVecinos(this.cromosoma, vecinos);
        agregarVecinos(otro.cromosoma, vecinos);

        // Construir el hijo
        Integer[] hijo = new Integer[NUM_HABITACIONES];
        int actual = this.cromosoma[0]; // Empezar con el primer gen del primer padre

        for (int i = 0; i < NUM_HABITACIONES; i++) {
            hijo[i] = actual;

            // Eliminar el gen actual de los vecinos
            for (Set<Integer> set : vecinos.values()) {
                set.remove(actual);
            }

            // Seleccionar el siguiente gen basado en los vecinos
            if (!vecinos.get(actual).isEmpty()) {
                actual = vecinos.get(actual).iterator().next();
            } else {
                // Si no hay vecinos, seleccionar un gen aleatorio no usado
                for (int j = 1; j <= NUM_HABITACIONES; j++) {
                    if (!Arrays.asList(hijo).contains(j)) {
                        actual = j;
                        break;
                    }
                }
            }
        }

        this.cromosoma = hijo;
    }
    
    public void cruzarPropio(IndividuoRobot otro) {
        Random rand = new Random();
        Integer[] hijo1 = new Integer[NUM_HABITACIONES];
        Integer[] hijo2 = new Integer[NUM_HABITACIONES];
        Set<Integer> usadosHijo1 = new HashSet<>();
        Set<Integer> usadosHijo2 = new HashSet<>();

        // Seleccionar un segmento aleatorio del primer padre
        int punto1 = rand.nextInt(NUM_HABITACIONES);
        int punto2 = rand.nextInt(NUM_HABITACIONES - punto1) + punto1;

        // Copiar el segmento al hijo1
        for (int i = punto1; i <= punto2; i++) {
            hijo1[i] = this.cromosoma[i];
            usadosHijo1.add(hijo1[i]);
        }

        // Copiar el segmento al hijo2
        for (int i = punto1; i <= punto2; i++) {
            hijo2[i] = otro.cromosoma[i];
            usadosHijo2.add(hijo2[i]);
        }

        // Completar los genes faltantes en hijo1
        completarGenesFaltantes(hijo1, usadosHijo1);
        // Completar los genes faltantes en hijo2
        completarGenesFaltantes(hijo2, usadosHijo2);

        this.cromosoma = hijo1;
        otro.cromosoma = hijo2;
    }
    
    
    // FUNCIONES DE MUTACIÓN
    public void mutacionInsercion() {
        Random rand = new Random();
        int pos1 = rand.nextInt(NUM_HABITACIONES);
        int pos2 = rand.nextInt(NUM_HABITACIONES);

        int gen = this.cromosoma[pos1];
        List<Integer> lista = new ArrayList<>(Arrays.asList(this.cromosoma));
        lista.remove(pos1);
        lista.add(pos2, gen);

        this.cromosoma = lista.toArray(new Integer[0]);
    }
    
    public void mutacionIntercambio() {
        Random rand = new Random();
        int pos1 = rand.nextInt(NUM_HABITACIONES);
        int pos2 = rand.nextInt(NUM_HABITACIONES);

        int temp = this.cromosoma[pos1];
        this.cromosoma[pos1] = this.cromosoma[pos2];
        this.cromosoma[pos2] = temp;
    }
    
    
    
    public void mutacionHeuristica() {
        Random rand = new Random();
        int pos1 = rand.nextInt(NUM_HABITACIONES);
        int pos2 = rand.nextInt(NUM_HABITACIONES);

        // Intercambiar genes si mejora el fitness
        double fitnessActual = this.getFitness();
        int temp = this.cromosoma[pos1];
        this.cromosoma[pos1] = this.cromosoma[pos2];
        this.cromosoma[pos2] = temp;

        double fitnessNuevo = this.getFitness();
        if (fitnessNuevo > fitnessActual) {
            // Si no mejora, revertir el intercambio
            this.cromosoma[pos2] = this.cromosoma[pos1];
            this.cromosoma[pos1] = temp;
        }
    }
    
    public void mutacionPropia() {
        Random rand = new Random();
        int punto1 = rand.nextInt(NUM_HABITACIONES);
        int punto2 = rand.nextInt(NUM_HABITACIONES - punto1) + punto1;

        // Invertir el segmento entre punto1 y punto2
        List<Integer> sublista = Arrays.asList(this.cromosoma).subList(punto1, punto2 + 1);
        Collections.reverse(sublista);
    }
    
    // FUNCIONES AUXILIARES
    private void agregarVecinos(Integer[] cromosoma, Map<Integer, Set<Integer>> vecinos) {
        for (int i = 0; i < NUM_HABITACIONES; i++) {
            int anterior = (i == 0) ? cromosoma[NUM_HABITACIONES - 1] : cromosoma[i - 1];
            int siguiente = (i == NUM_HABITACIONES - 1) ? cromosoma[0] : cromosoma[i + 1];

            vecinos.get(cromosoma[i]).add(anterior);
            vecinos.get(cromosoma[i]).add(siguiente);
        }
    }
    
    public void mutacionInversion() {
        Random rand = new Random();
        int punto1 = rand.nextInt(NUM_HABITACIONES);
        int punto2 = rand.nextInt(NUM_HABITACIONES - punto1) + punto1;

        List<Integer> sublista = Arrays.asList(this.cromosoma).subList(punto1, punto2 + 1);
        Collections.reverse(sublista);
    }
    
}
