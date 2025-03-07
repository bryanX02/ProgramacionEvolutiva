package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PoblacionFun3 extends Poblacion<IndividuoFuncion3>{
	
	private int tamPoblacion;
	private AlgoritmoGenetico algoritmo;
	public static IndividuoFuncion3 mejorIndividuoAbsoluto;
	
	public PoblacionFun3(int tamPoblacion, AlgoritmoGenetico algoritmo) {
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
			
			this.add(new IndividuoFuncion3());
			
		}
		
	}
	
	public double getFitnessMedio() {
	    if (this.isEmpty()) return 0.0; // Evita divisiones por cero

	    double sumaFitness = 0.0;
	    for (IndividuoFuncion3 ind : this) {
	        sumaFitness += ind.getFitness();
	    }

	    return sumaFitness / this.size();
	}


	// METODOS DE SELECCION
	
	public PoblacionFun3 seleccionarSegun(String metodoSeleccion) {
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
	
    public PoblacionFun3 seleccionRuleta() {
        // 1. Calcular la suma total de fitness (valores reales)
        double sumaFitness = 0.0;
        for (IndividuoFuncion3 ind : this) {
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
        PoblacionFun3 nuevaGeneracion = new PoblacionFun3(tamPoblacion, algoritmo);
        Random rand = new Random();

        // 4. Garantizar que seleccionamos exactamente `tamPoblacion` individuos
        for (int i = 0; i < tamPoblacion; i++) {
            double r = rand.nextDouble(); // Número aleatorio entre 0 y 1

            // 5. Buscar el individuo correspondiente basándonos en la probabilidad acumulada
            for (int j = 0; j < tamPoblacion; j++) {
                if (r <= probabilidadesAcumuladas[j]) {
                    try {
                        nuevaGeneracion.add((IndividuoFuncion3) this.get(j).clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    break; // Salir del bucle una vez seleccionado el individuo
                }
            }
        }

        return nuevaGeneracion;
    }



    public PoblacionFun3 seleccionTorneoDeterministico() {
        PoblacionFun3 nuevaGeneracion = new PoblacionFun3(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
        	IndividuoFuncion3 mejor = null;
            for (int j = 0; j < 3; j++) {
            	IndividuoFuncion3 candidato = this.get(rand.nextInt(tamPoblacion));
                if (mejor == null || candidato.getFitness() < mejor.getFitness()) {
                    mejor = candidato;
                }
            }
            try {
                nuevaGeneracion.add((IndividuoFuncion3) mejor.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return nuevaGeneracion;
    }

    // Selección por Torneo Probabilístico para Minimización
    public PoblacionFun3 seleccionTorneoProbabilistico(double P) {
        PoblacionFun3 nuevaGeneracion = new PoblacionFun3(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
            IndividuoFuncion3 mejor = null, peor = null;
            for (int j = 0; j < 3; j++) {
                IndividuoFuncion3 candidato = this.get(rand.nextInt(tamPoblacion));
                if (mejor == null || candidato.getFitness() < mejor.getFitness()) { // Menor fitness es mejor
                    peor = mejor;
                    mejor = candidato;
                } else if (peor == null || candidato.getFitness() > peor.getFitness()) {
                    peor = candidato;
                }
            }
            try {
                if (rand.nextDouble() < P) {
                    nuevaGeneracion.add((IndividuoFuncion3) mejor.clone());
                } else {
                    nuevaGeneracion.add((IndividuoFuncion3) peor.clone());
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return nuevaGeneracion;
    }

    public PoblacionFun3 seleccionEstocasticaUniversal() {
        PoblacionFun3 nuevaGeneracion = new PoblacionFun3(tamPoblacion, algoritmo);
        Random rand = new Random();
        double r = rand.nextDouble() / tamPoblacion;
        double sumaFitness = this.stream().mapToDouble(IndividuoFuncion3::getFitness).sum();
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
                nuevaGeneracion.add((IndividuoFuncion3) this.get(indice - 1).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return nuevaGeneracion;
    }

    // Selección por Truncamiento para Minimización
    public PoblacionFun3 seleccionTruncamiento() {
        PoblacionFun3 nuevaGeneracion = new PoblacionFun3(tamPoblacion, algoritmo);
        this.sort((a, b) -> Double.compare(a.getFitness(), b.getFitness())); // Ordenar por fitness ascendente (menor es mejor)
        int seleccionados = tamPoblacion / 2;

        for (int i = 0; i < seleccionados; i++) {
            try {
                nuevaGeneracion.add((IndividuoFuncion3) this.get(i).clone());
                nuevaGeneracion.add((IndividuoFuncion3) this.get(i).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return nuevaGeneracion;
    }

    // Selección por Restos para Minimización
    public PoblacionFun3 seleccionRestos() {
        PoblacionFun3 nuevaGeneracion = new PoblacionFun3(tamPoblacion, algoritmo);
        double sumaFitness = this.stream().mapToDouble(IndividuoFuncion3::getFitness).sum();

        for (IndividuoFuncion3 ind : this) {
            int cantidad = (int) ((1.0 / ind.getFitness() / sumaFitness) * tamPoblacion); // Menor fitness = más probabilidades
            for (int j = 0; j < cantidad; j++) {
                try {
                    nuevaGeneracion.add((IndividuoFuncion3) ind.clone());
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
	    for (IndividuoFuncion3 individuo : this) {
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
	    PoblacionFun3 nuevaPoblacion = new PoblacionFun3(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	        IndividuoFuncion3 padre1 = this.get(i);
	        IndividuoFuncion3 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            // Se aplica el cruce directamente sobre los padres
	            padre1.cruzarMonopunto(padre2);
	        }

	        // Añadir los individuos a la nueva población (se hayan cruzado o no)
	        try {
	            nuevaPoblacion.add((IndividuoFuncion3) padre1.clone());
	            nuevaPoblacion.add((IndividuoFuncion3) padre2.clone());
	        } catch (CloneNotSupportedException e) {
	            e.printStackTrace();
	        }
	    }

	    // Si la población era impar, copiamos el último individuo sin cambios
	    if (this.size() % 2 != 0) {
	        try {
	            nuevaPoblacion.add((IndividuoFuncion3) this.get(this.size() - 1).clone());
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
	    PoblacionFun3 nuevaPoblacion = new PoblacionFun3(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	        IndividuoFuncion3 padre1 = this.get(i);
	        IndividuoFuncion3 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            // Aplicamos cruce uniforme a los dos padres
	            padre1.cruzarUniforme(padre2);
	        }

	        // Añadir los individuos (cruzados o no) a la nueva población
	        try {
	            nuevaPoblacion.add((IndividuoFuncion3) padre1.clone());
	            nuevaPoblacion.add((IndividuoFuncion3) padre2.clone());
	        } catch (CloneNotSupportedException e) {
	            e.printStackTrace();
	        }
	    }

	    // Si la población era impar, copiar el último individuo sin cambios
	    if (this.size() % 2 != 0) {
	        try {
	            nuevaPoblacion.add((IndividuoFuncion3) this.get(this.size() - 1).clone());
	        } catch (CloneNotSupportedException e) {
	            e.printStackTrace();
	        }
	    }

	    // Reemplazamos la población actual con la nueva
	    this.clear();
	    this.addAll(nuevaPoblacion);
	}

	@Override
	public IndividuoFuncion3 getMejorIndividuo() {
        return this.stream().min((a, b) -> Double.compare(a.getFitness(), b.getFitness())).orElse(null);
    }
	
	@Override
	public double getExtremo() {
		// TODO Auto-generated method stub
		return Double.MAX_VALUE;
	}

	@Override
	public void actualizarAbsoluto(Individuo mejorIndividuo) {

		if(mejorIndividuo.getFitness() < mejorIndividuoAbsoluto.getFitness()) {
			mejorIndividuoAbsoluto = (IndividuoFuncion3) mejorIndividuo;
		}
		
	}
	
	@Override
	public void setAbsoluto(Individuo ind) {
		// TODO Auto-generated method stub
		mejorIndividuoAbsoluto = (IndividuoFuncion3) ind;
	}

	@Override
	public Individuo getAbsoluto() {
		// TODO Auto-generated method stub
		return mejorIndividuoAbsoluto;
	}
	
}
