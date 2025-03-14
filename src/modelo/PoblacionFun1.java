package modelo;

import java.util.Random;

public class PoblacionFun1 extends Poblacion<IndividuoFuncion1>{
	
	private int tamPoblacion;
	private AlgoritmoGenetico algoritmo;
	public static IndividuoFuncion1 mejorIndividuoAbsoluto;
	
	public PoblacionFun1(int tamPoblacion, AlgoritmoGenetico algoritmo) {
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
	@Override
	public void iniciarGeneracion() {
		// Inciamos la poblacion
		for (int i = 0; i <tamPoblacion; i++) {
			
			this.add(new IndividuoFuncion1());
			
		}
	}
	
	@Override
	public double getFitnessMedio() {
	    if (this.isEmpty()) return 0.0; // Evita divisiones por cero

	    double sumaFitness = 0.0;
	    for (IndividuoFuncion1 ind : this) {
	        sumaFitness += ind.getFitness();
	    }

	    return sumaFitness / this.size();
	}
	
	
	@Override
	public double getExtremo() {
		// TODO Auto-generated method stub
		return Double.MIN_VALUE;
	}
	
	
	// METODOS DE SELECCION


	@Override
	public PoblacionFun1 seleccionarSegun(String metodoSeleccion) {
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

	@Override
    public void cruzarSegun(String metodoCruce) {
        if ("Monopunto".equals(metodoCruce)) {
            cruceMonopunto();
        } else if ("Uniforme".equals(metodoCruce)) {
            cruceUniforme();
        } else if (!"Ninguno".equals(metodoCruce)){
            throw new IllegalArgumentException("Método de cruce no válido: " + metodoCruce);
        }
    }
	
	// Implementación de la selección por ruleta
	public PoblacionFun1 seleccionRuleta() {
	    
	    // 1. Calcular la suma total de fitness
	    double sumaFitness = 0.0;
	    for (IndividuoFuncion1 ind : this) {
	        sumaFitness += ind.getFitness();
	    }

	    // 2. Construir lista de probabilidades acumuladas
	    double[] probabilidadesAcumuladas = new double[tamPoblacion];
	    double acumulado = 0.0;

	    for (int i = 0; i < tamPoblacion; i++) {
	        acumulado += this.get(i).getFitness() / sumaFitness;
	        probabilidadesAcumuladas[i] = acumulado;
	    }

	    // 3. Crear una nueva población de tamaño correcto
	    PoblacionFun1 nuevaGeneracion = new PoblacionFun1(tamPoblacion, algoritmo);
	    Random rand = new Random();

	    for (int i = 0; i < tamPoblacion; i++) { // Garantizar 100 individuos
	        double r = rand.nextDouble(); // Número aleatorio entre 0 y 1

	        // 4. Buscar el individuo correspondiente
	        for (int j = 0; j < tamPoblacion; j++) {
	            if (r <= probabilidadesAcumuladas[j]) {
	                nuevaGeneracion.add((IndividuoFuncion1) this.get(j).clone());
	                break; // Salir del bucle una vez seleccionado el individuo
	            }
	        }
	    }

	    return nuevaGeneracion;
	}

    public PoblacionFun1 seleccionTorneoDeterministico() {
        PoblacionFun1 nuevaGeneracion = new PoblacionFun1(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
            IndividuoFuncion1 mejor = null;
            for (int j = 0; j < 3; j++) {
                IndividuoFuncion1 candidato = this.get(rand.nextInt(tamPoblacion));
                if (mejor == null || candidato.getFitness() > mejor.getFitness()) {
                    mejor = candidato;
                }
            }
            nuevaGeneracion.add((IndividuoFuncion1) mejor.clone());
        }
        return nuevaGeneracion;
    }

    public PoblacionFun1 seleccionTorneoProbabilistico(double P) {
        PoblacionFun1 nuevaGeneracion = new PoblacionFun1(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
            IndividuoFuncion1 mejor = null, peor = null;
            for (int j = 0; j < 3; j++) {
                IndividuoFuncion1 candidato = this.get(rand.nextInt(tamPoblacion));
                if (mejor == null || candidato.getFitness() > mejor.getFitness()) {
                    peor = mejor;
                    mejor = candidato;
                } else if (peor == null || candidato.getFitness() < peor.getFitness()) {
                    peor = candidato;
                }
            }
            if (rand.nextDouble() < P) {
			    nuevaGeneracion.add((IndividuoFuncion1) mejor.clone());
			} else {
			    nuevaGeneracion.add((IndividuoFuncion1) peor.clone());
			}
        }
        return nuevaGeneracion;
    }

    public PoblacionFun1 seleccionEstocasticaUniversal() {
        PoblacionFun1 nuevaGeneracion = new PoblacionFun1(tamPoblacion, algoritmo);
        Random rand = new Random();
        double r = rand.nextDouble() / tamPoblacion;
        double sumaFitness = this.stream().mapToDouble(IndividuoFuncion1::getFitness).sum();
        double paso = 1.0 / tamPoblacion;

        double acumulado = 0.0;
        int indice = 0;
        for (int i = 0; i < tamPoblacion; i++) {
            double umbral = r + i * paso;
            while (acumulado < umbral && indice < tamPoblacion) {
                acumulado += this.get(indice).getFitness() / sumaFitness;
                indice++;
            }
            nuevaGeneracion.add((IndividuoFuncion1) this.get(indice - 1).clone());
        }
        return nuevaGeneracion;
    }

    public PoblacionFun1 seleccionTruncamiento() {
        PoblacionFun1 nuevaGeneracion = new PoblacionFun1(tamPoblacion, algoritmo);
        this.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        int seleccionados = tamPoblacion / 2;

        for (int i = 0; i < seleccionados; i++) {
            nuevaGeneracion.add((IndividuoFuncion1) this.get(i).clone());
			nuevaGeneracion.add((IndividuoFuncion1) this.get(i).clone());
        }
        return nuevaGeneracion;
    }

    public PoblacionFun1 seleccionRestos() {
        PoblacionFun1 nuevaGeneracion = new PoblacionFun1(tamPoblacion, algoritmo);
        double sumaFitness = this.stream().mapToDouble(IndividuoFuncion1::getFitness).sum();

        for (IndividuoFuncion1 ind : this) {
            int cantidad = (int) ((ind.getFitness() / sumaFitness) * tamPoblacion);
            for (int j = 0; j < cantidad; j++) {
                nuevaGeneracion.add((IndividuoFuncion1) ind.clone());
            }
        }
        while (nuevaGeneracion.size() < tamPoblacion) {
            nuevaGeneracion.addAll(this.seleccionRuleta());
        }
        return nuevaGeneracion;
    }
	
    @Override
	public void mutacion() {
	    Random rand = new Random();

	    // Recorremos cada individuo en la población
	    for (IndividuoFuncion1 individuo : this) {
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
	    PoblacionFun1 nuevaPoblacion = new PoblacionFun1(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	        IndividuoFuncion1 padre1 = this.get(i);
	        IndividuoFuncion1 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            // Se aplica el cruce directamente sobre los padres
	            padre1.cruzarMonopunto(padre2);
	        }

	        nuevaPoblacion.add((IndividuoFuncion1) padre1.clone());
			nuevaPoblacion.add((IndividuoFuncion1) padre2.clone());
	    }

	    // Si la población era impar, copiamos el último individuo sin cambios
	    if (this.size() % 2 != 0) {
	        nuevaPoblacion.add((IndividuoFuncion1) this.get(this.size() - 1).clone());
	    }

	    // Reemplazamos la población actual con la nueva
	    this.clear();
	    this.addAll(nuevaPoblacion);
	}

	
	public void cruceUniforme() {
	    Random rand = new Random();
	    PoblacionFun1 nuevaPoblacion = new PoblacionFun1(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	        IndividuoFuncion1 padre1 = this.get(i);
	        IndividuoFuncion1 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            // Aplicamos cruce uniforme a los dos padres
	            padre1.cruzarUniforme(padre2);
	        }

	        nuevaPoblacion.add((IndividuoFuncion1) padre1.clone());
			nuevaPoblacion.add((IndividuoFuncion1) padre2.clone());
	    }

	    // Si la población era impar, copiar el último individuo sin cambios
	    if (this.size() % 2 != 0) {
	        nuevaPoblacion.add((IndividuoFuncion1) this.get(this.size() - 1).clone());
	    }

	    // Reemplazamos la población actual con la nueva
	    this.clear();
	    this.addAll(nuevaPoblacion);
	}

	@Override
	public IndividuoFuncion1 getMejorIndividuo() {
        return this.stream().max((a, b) -> Double.compare(a.getFitness(), b.getFitness())).orElse(null);
    }

	@Override
	public void actualizarAbsoluto(Individuo mejorIndividuo) {

		if(mejorIndividuo.getFitness() > mejorIndividuoAbsoluto.getFitness()) {
			mejorIndividuoAbsoluto = (IndividuoFuncion1) mejorIndividuo.clone();
		}
		
	}

	@Override
	public void setAbsoluto(Individuo ind) {
		// TODO Auto-generated method stub
		mejorIndividuoAbsoluto = (IndividuoFuncion1) ind;
	}

	@Override
	public Individuo getAbsoluto() {
		// TODO Auto-generated method stub
		return mejorIndividuoAbsoluto;
	}
	
	
	
}
