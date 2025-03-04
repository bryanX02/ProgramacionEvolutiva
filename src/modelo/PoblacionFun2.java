package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PoblacionFun2 extends Poblacion<IndividuoFuncion2>{
	
	private int tamPoblacion;
	private AlgoritmoGenetico algoritmo;
	
	public PoblacionFun2(int tamPoblacion, AlgoritmoGenetico algoritmo) {
		super();
		this.tamPoblacion = tamPoblacion;
		this.algoritmo = algoritmo;
	}
	
	public int getTamPoblacion() {
		return tamPoblacion;
	}

	public void setTamPoblacion(int tamPoblacion) {
		this.tamPoblacion = tamPoblacion;
	}

	// Funciones
	public void iniciarGeneracion() {
		
		// Inciamos la poblacion
		for (int i = 0; i <tamPoblacion; i++) {
			
			this.add(new IndividuoFuncion2());
			
		}
		
	}
	
	public double getFitnessMedio() {
	    if (this.isEmpty()) return 0.0; // Evita divisiones por cero

	    double sumaFitness = 0.0;
	    for (IndividuoFuncion2 ind : this) {
	        sumaFitness += ind.getFitness();
	    }

	    return sumaFitness / this.size();
	}


	// METODOS DE SELECCION
	
	public PoblacionFun2 seleccionarSegun(String metodoSeleccion) {
        switch (metodoSeleccion) {
            case "Ruleta":
                return seleccionRuleta();
            case "Torneo Probabilístico":
                return seleccionTorneoProbabilistico(0.5);
            case "Torneo Determinístico":
                return seleccionTorneoDeterministico();
            case "Estocástico Universal":
                return seleccionEstocasticaUniversal();
            case "Truncamiento":
                return seleccionTruncamiento();
            case "Restos":
                return seleccionRestos();
            default:
                throw new IllegalArgumentException("Método de selección no válido: " + metodoSeleccion);
        }
    }

    public void cruzarSegun(String metodoCruce) {
        if ("Monopunto".equals(metodoCruce)) {
            cruceMonopunto();
        } else if ("Uniforme".equals(metodoCruce)) {
            cruceUniforme();
        } else if (!"Ninguno".equals(metodoCruce)){
            throw new IllegalArgumentException("Método de cruce no válido: " + metodoCruce);
        }
    }
	
    public PoblacionFun2 seleccionRuleta() {
        // 1. Calcular la suma total de fitness (valores reales)
        double sumaFitness = 0.0;
        for (IndividuoFuncion2 ind : this) {
            sumaFitness += ind.getFitness();  // Acumulamos los fitness (valores reales)
        }

        // 2. Construir lista de probabilidades acumuladas
        double[] probabilidadesAcumuladas = new double[tamPoblacion];
        double acumulado = 0.0;

        // Usamos 1 / fitness para minimizar (invertir el fitness)
        for (int i = 0; i < tamPoblacion; i++) {
            acumulado += (1.0 / this.get(i).getFitness()); // Invertir el fitness para minimizar
            probabilidadesAcumuladas[i] = acumulado;
        }

        // Normalizamos las probabilidades acumuladas para que sumen 1
        for (int i = 0; i < tamPoblacion; i++) {
            probabilidadesAcumuladas[i] /= acumulado;  // Normalización
        }

        // 3. Crear una nueva población de tamaño correcto
        PoblacionFun2 nuevaGeneracion = new PoblacionFun2(tamPoblacion, algoritmo);
        Random rand = new Random();

        // 4. Garantizar que seleccionamos exactamente `tamPoblacion` individuos
        for (int i = 0; i < tamPoblacion; i++) {
            double r = rand.nextDouble(); // Número aleatorio entre 0 y 1

            // 5. Buscar el individuo correspondiente basándonos en la probabilidad acumulada
            for (int j = 0; j < tamPoblacion; j++) {
                if (r <= probabilidadesAcumuladas[j]) {
                    try {
                        nuevaGeneracion.add((IndividuoFuncion2) this.get(j).clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    break; // Salir del bucle una vez seleccionado el individuo
                }
            }
        }

        return nuevaGeneracion;
    }



    public PoblacionFun2 seleccionTorneoDeterministico() {
        PoblacionFun2 nuevaGeneracion = new PoblacionFun2(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
        	IndividuoFuncion2 mejor = null;
            for (int j = 0; j < 3; j++) {
            	IndividuoFuncion2 candidato = this.get(rand.nextInt(tamPoblacion));
                if (mejor == null || candidato.getFitness() < mejor.getFitness()) {
                    mejor = candidato;
                }
            }
            try {
                nuevaGeneracion.add((IndividuoFuncion2) mejor.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return nuevaGeneracion;
    }

    // Selección por Torneo Probabilístico para Minimización
    public PoblacionFun2 seleccionTorneoProbabilistico(double P) {
        PoblacionFun2 nuevaGeneracion = new PoblacionFun2(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
            IndividuoFuncion2 mejor = null, peor = null;
            for (int j = 0; j < 3; j++) {
                IndividuoFuncion2 candidato = this.get(rand.nextInt(tamPoblacion));
                if (mejor == null || candidato.getFitness() < mejor.getFitness()) { // Menor fitness es mejor
                    peor = mejor;
                    mejor = candidato;
                } else if (peor == null || candidato.getFitness() > peor.getFitness()) {
                    peor = candidato;
                }
            }
            try {
                if (rand.nextDouble() < P) {
                    nuevaGeneracion.add((IndividuoFuncion2) mejor.clone());
                } else {
                    nuevaGeneracion.add((IndividuoFuncion2) peor.clone());
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return nuevaGeneracion;
    }

    public PoblacionFun2 seleccionEstocasticaUniversal() {
        PoblacionFun2 nuevaGeneracion = new PoblacionFun2(tamPoblacion, algoritmo);
        Random rand = new Random();
        double r = rand.nextDouble() / tamPoblacion;
        double sumaFitness = this.stream().mapToDouble(IndividuoFuncion2::getFitness).sum();
        double paso = 1.0 / tamPoblacion;

        double acumulado = 0.0;
        int indice = 0;
        for (int i = 0; i < tamPoblacion; i++) {
            double umbral = r + i * paso;
            while (acumulado < umbral && indice < tamPoblacion) {
                acumulado += (1.0 / this.get(indice).getFitness()) / sumaFitness;
                indice++;
            }
            try {
                nuevaGeneracion.add((IndividuoFuncion2) this.get(indice - 1).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return nuevaGeneracion;
    }

    // Selección por Truncamiento para Minimización
    public PoblacionFun2 seleccionTruncamiento() {
        PoblacionFun2 nuevaGeneracion = new PoblacionFun2(tamPoblacion, algoritmo);
        this.sort((a, b) -> Double.compare(a.getFitness(), b.getFitness())); // Ordenar por fitness ascendente (menor es mejor)
        int seleccionados = tamPoblacion / 2;

        for (int i = 0; i < seleccionados; i++) {
            try {
                nuevaGeneracion.add((IndividuoFuncion2) this.get(i).clone());
                nuevaGeneracion.add((IndividuoFuncion2) this.get(i).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return nuevaGeneracion;
    }

    // Selección por Restos para Minimización
    public PoblacionFun2 seleccionRestos() {
        PoblacionFun2 nuevaGeneracion = new PoblacionFun2(tamPoblacion, algoritmo);
        double sumaFitness = this.stream().mapToDouble(IndividuoFuncion2::getFitness).sum();

        for (IndividuoFuncion2 ind : this) {
            int cantidad = (int) ((1.0 / ind.getFitness() / sumaFitness) * tamPoblacion); // Menor fitness = más probabilidades
            for (int j = 0; j < cantidad; j++) {
                try {
                    nuevaGeneracion.add((IndividuoFuncion2) ind.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        while (nuevaGeneracion.size() < tamPoblacion) {
            nuevaGeneracion.addAll(this.seleccionRuleta());
        }
        return nuevaGeneracion;
    }
	

	public void mutacion() {
	    Random rand = new Random();

	    // Recorremos cada individuo en la población
	    for (IndividuoFuncion2 individuo : this) {
	        Boolean[] cromosoma = individuo.getCromosoma(); // Obtener el cromosoma actual

	        // Recorremos cada bit del cromosoma
	        for (int i = 0; i < cromosoma.length; i++) {
	            if (rand.nextDouble() < algoritmo.getProbMutacion()) { // Verificamos si el bit muta
	                cromosoma[i] = !cromosoma[i]; // Se invierte el bit (mutación)
	            }
	        }
	    }
	}
	
	
	
	public void cruceMonopunto() {
	    Random rand = new Random();
	    PoblacionFun2 nuevaPoblacion = new PoblacionFun2(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	    	IndividuoFuncion2 padre1 = this.get(i);
	    	IndividuoFuncion2 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            // Se aplica el cruce directamente sobre los padres
	            padre1.cruzarMonopunto(padre2);
	        }

	        // Añadir los individuos a la nueva población (se hayan cruzado o no)
	        try {
	            nuevaPoblacion.add((IndividuoFuncion2) padre1.clone());
	            nuevaPoblacion.add((IndividuoFuncion2) padre2.clone());
	        } catch (CloneNotSupportedException e) {
	            e.printStackTrace();
	        }
	    }

	    // Si la población era impar, copiamos el último individuo sin cambios
	    if (this.size() % 2 != 0) {
	        try {
	            nuevaPoblacion.add((IndividuoFuncion2) this.get(this.size() - 1).clone());
	        } catch (CloneNotSupportedException e) {
	            e.printStackTrace();
	        }
	    }

	    // Reemplazamos la población actual con la nueva
	    this.clear();
	    this.addAll(nuevaPoblacion);
	}

	
	public void cruceUniforme() {
	    Random rand = new Random();
	    PoblacionFun2 nuevaPoblacion = new PoblacionFun2(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	    	IndividuoFuncion2 padre1 = this.get(i);
	        IndividuoFuncion2 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            // Aplicamos cruce uniforme a los dos padres
	            padre1.cruzarUniforme(padre2);
	        }

	        // Añadir los individuos (cruzados o no) a la nueva población
	        try {
	            nuevaPoblacion.add((IndividuoFuncion2) padre1.clone());
	            nuevaPoblacion.add((IndividuoFuncion2) padre2.clone());
	        } catch (CloneNotSupportedException e) {
	            e.printStackTrace();
	        }
	    }

	    // Si la población era impar, copiar el último individuo sin cambios
	    if (this.size() % 2 != 0) {
	        try {
	            nuevaPoblacion.add((IndividuoFuncion2) this.get(this.size() - 1).clone());
	        } catch (CloneNotSupportedException e) {
	            e.printStackTrace();
	        }
	    }

	    // Reemplazamos la población actual con la nueva
	    this.clear();
	    this.addAll(nuevaPoblacion);
	}

	public IndividuoFuncion2 getMejorIndividuo() {
        return this.stream().min((a, b) -> Double.compare(a.getFitness(), b.getFitness())).orElse(null);
    }

	@Override
	public Poblacion seleccionRuleta1() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	/* Cuando pensaba que el cruce era entre genes
	public void cruce() {
	    Random rand = new Random();
	    ArrayList<IndividuoFuncion1> nuevaPoblacion = new ArrayList<>();

	    for (int i = 0; i < this.size(); i += 2) {
	        if (i + 1 >= this.size()) {
	            nuevaPoblacion.add(this.get(i)); // Si hay un individuo sin pareja, lo añadimos sin cruzar
	            break;
	        }

	        IndividuoFuncion1 padre1 = this.get(i);
	        IndividuoFuncion1 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) { // Se decide si se cruzan
	            // Obtener los genes de cada padre
	            Boolean[][] genes1 = padre1.getGenes();
	            Boolean[][] genes2 = padre2.getGenes();

	            // Crear hijos con genes cruzados
	            Boolean[] nuevoGen1A = new Boolean[padre1.getTamGenes()[0]];
	            Boolean[] nuevoGen1B = new Boolean[padre1.getTamGenes()[1]];
	            Boolean[] nuevoGen2A = new Boolean[padre2.getTamGenes()[0]];
	            Boolean[] nuevoGen2B = new Boolean[padre2.getTamGenes()[1]];

	            // Punto de cruce aleatorio para cada gen
	            int puntoCruce1 = rand.nextInt(padre1.getTamGenes()[0]);
	            int puntoCruce2 = rand.nextInt(padre1.getTamGenes()[1]);

	            // Cruce del primer gen
	            for (int j = 0; j < padre1.getTamGenes()[0]; j++) {
	                if (j < puntoCruce1) {
	                    nuevoGen1A[j] = genes1[0][j];
	                    nuevoGen2A[j] = genes2[0][j];
	                } else {
	                    nuevoGen1A[j] = genes2[0][j];
	                    nuevoGen2A[j] = genes1[0][j];
	                }
	            }

	            // Cruce del segundo gen
	            for (int j = 0; j < padre1.getTamGenes()[1]; j++) {
	                if (j < puntoCruce2) {
	                    nuevoGen1B[j] = genes1[1][j];
	                    nuevoGen2B[j] = genes2[1][j];
	                } else {
	                    nuevoGen1B[j] = genes2[1][j];
	                    nuevoGen2B[j] = genes1[1][j];
	                }
	            }

	            // Crear nuevos individuos hijos y asignarles los genes cruzados
	            IndividuoFuncion1 hijo1 = new IndividuoFuncion1();
	            IndividuoFuncion1 hijo2 = new IndividuoFuncion1();

	            hijo1.setGenes(nuevoGen1A, nuevoGen1B);
	            hijo2.setGenes(nuevoGen2A, nuevoGen2B);

	            nuevaPoblacion.add(hijo1);
	            nuevaPoblacion.add(hijo2);
	        } else {
	            // Si no hay cruce, se mantienen los mismos individuos
	            nuevaPoblacion.add(padre1);
	            nuevaPoblacion.add(padre2);
	        }
	    }

	    this.clear();
	    this.addAll(nuevaPoblacion);
	}*/




	
}
