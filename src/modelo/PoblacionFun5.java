package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PoblacionFun5 extends Poblacion<IndividuoFuncion5>{
	
	private int tamPoblacion;
	private AlgoritmoGenetico algoritmo;
	public static IndividuoFuncion5 mejorIndividuoAbsoluto;
	
	public PoblacionFun5(int tamPoblacion, AlgoritmoGenetico algoritmo) {
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
	public void iniciarGeneracionDimensionada(int dimension) {
		
		// Inciamos la poblacion
		for (int i = 0; i <tamPoblacion; i++) {
			
			this.add(new IndividuoFuncion5(dimension));
			
		}
		
	}
	
	public double getFitnessMedio() {
	    if (this.isEmpty()) return 0.0; // Evita divisiones por cero

	    double sumaFitness = 0.0;
	    for (IndividuoFuncion5 ind : this) {
	        sumaFitness += ind.getFitness();
	    }

	    return sumaFitness / this.size();
	}


	// METODOS DE SELECCION
	
	public PoblacionFun5 seleccionarSegun(String metodoSeleccion) {
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
	    switch (metodoCruce) {
	        case "Monopunto":
	            cruceMonopunto();
	            break;
	        case "Uniforme":
	            cruceUniforme();
	            break;
	        case "Aritmético":
	            cruceAritmetico();
	            break;
	        case "BLX-Alpha":
	            cruceBLXAlpha(0.5); // Puedes cambiar el valor de alpha si lo deseas
	            break;
	        case "Ninguno":
	            break;
	        default:
	            throw new IllegalArgumentException("Método de cruce no válido: " + metodoCruce);
	    }
	}

	public PoblacionFun5 seleccionRuleta() {
	    // 1. Encontrar el peor y mejor fitness
	    double minFitness = Double.MAX_VALUE;
	    double maxFitness = Double.MIN_VALUE;

	    for (IndividuoFuncion5 ind : this) {
	        double fit = ind.getFitness();
	        minFitness = Math.min(minFitness, fit);
	        maxFitness = Math.max(maxFitness, fit);
	    }

	    // 2. Ajustar fitness para evitar valores negativos y normalizar
	    double delta = 1e-6; // Pequeño desplazamiento para evitar valores 0
	    double ajuste = Math.abs(minFitness) + delta;  

	    // 3. Aplicar normalización de fitness (escala positiva)
	    double[] fitnessEscalado = new double[tamPoblacion];
	    double sumaFitnessEscalado = 0.0;

	    for (int i = 0; i < tamPoblacion; i++) {
	        fitnessEscalado[i] = this.get(i).getFitness() + ajuste; // Convertir a valores positivos
	        sumaFitnessEscalado += fitnessEscalado[i];
	    }

	    // 4. Crear lista de probabilidades acumuladas
	    double[] probabilidadesAcumuladas = new double[tamPoblacion];
	    double acumulado = 0.0;

	    for (int i = 0; i < tamPoblacion; i++) {
	        acumulado += fitnessEscalado[i] / sumaFitnessEscalado;
	        probabilidadesAcumuladas[i] = acumulado;
	    }

	    // 5. Crear nueva población evitando sesgo extremo
	    PoblacionFun5 nuevaGeneracion = new PoblacionFun5(tamPoblacion, algoritmo);
	    Random rand = new Random();

	    for (int i = 0; i < tamPoblacion; i++) {
	        double r = rand.nextDouble(); // Número aleatorio entre 0 y 1

	        for (int j = 0; j < tamPoblacion; j++) {
	            if (r <= probabilidadesAcumuladas[j]) {
	                nuevaGeneracion.add((IndividuoFuncion5) this.get(j).clone());
	                break; // Una vez seleccionado el individuo, salimos del bucle
	            }
	        }
	    }

	    return nuevaGeneracion;
	}


    public PoblacionFun5 seleccionTorneoDeterministico() {
        PoblacionFun5 nuevaGeneracion = new PoblacionFun5(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
        	IndividuoFuncion5 mejor = null;
            for (int j = 0; j < 3; j++) {
            	IndividuoFuncion5 candidato = this.get(rand.nextInt(tamPoblacion));
                if (mejor == null || candidato.getFitness() < mejor.getFitness()) {
                    mejor = candidato;
                }
            }
            nuevaGeneracion.add((IndividuoFuncion5) mejor.clone());
        }
        return nuevaGeneracion;
    }

    // Selección por Torneo Probabilístico para Minimización
    public PoblacionFun5 seleccionTorneoProbabilistico(double P) {
        PoblacionFun5 nuevaGeneracion = new PoblacionFun5(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
            IndividuoFuncion5 mejor = null, peor = null;
            for (int j = 0; j < 3; j++) {
                IndividuoFuncion5 candidato = this.get(rand.nextInt(tamPoblacion));
                if (mejor == null || candidato.getFitness() < mejor.getFitness()) { // Menor fitness es mejor
                    peor = mejor;
                    mejor = candidato;
                } else if (peor == null || candidato.getFitness() > peor.getFitness()) {
                    peor = candidato;
                }
            }
            if (rand.nextDouble() < P) {
			    nuevaGeneracion.add((IndividuoFuncion5) mejor.clone());
			} else {
			    nuevaGeneracion.add((IndividuoFuncion5) peor.clone());
			}
        }
        return nuevaGeneracion;
    }

    public PoblacionFun5 seleccionEstocasticaUniversal() {
        PoblacionFun5 nuevaGeneracion = new PoblacionFun5(tamPoblacion, algoritmo);
        Random rand = new Random();
        double r = rand.nextDouble() / tamPoblacion;
        double sumaFitness = this.stream().mapToDouble(IndividuoFuncion5::getFitness).sum();
        double paso = 1.0 / tamPoblacion;

        double acumulado = 0.0;
        int indice = 0;
        for (int i = 0; i < tamPoblacion; i++) {
            double umbral = r + i * paso;
            while (acumulado < umbral && indice < tamPoblacion) {
                acumulado += (1.0 / this.get(indice).getFitness()) / sumaFitness;
                indice++;
            }
            nuevaGeneracion.add((IndividuoFuncion5) this.get(indice - 1).clone());
        }
        return nuevaGeneracion;
    }

    // Selección por Truncamiento para Minimización
    public PoblacionFun5 seleccionTruncamiento() {
        PoblacionFun5 nuevaGeneracion = new PoblacionFun5(tamPoblacion, algoritmo);
        this.sort((a, b) -> Double.compare(a.getFitness(), b.getFitness())); // Ordenar por fitness ascendente (menor es mejor)
        int seleccionados = tamPoblacion / 2;

        for (int i = 0; i < seleccionados; i++) {
            nuevaGeneracion.add((IndividuoFuncion5) this.get(i).clone());
			nuevaGeneracion.add((IndividuoFuncion5) this.get(i).clone());
        }
        return nuevaGeneracion;
    }

    // Selección por Restos para Minimización
    public PoblacionFun5 seleccionRestos() {
        PoblacionFun5 nuevaGeneracion = new PoblacionFun5(tamPoblacion, algoritmo);
        double sumaFitness = this.stream().mapToDouble(IndividuoFuncion5::getFitness).sum();

        for (IndividuoFuncion5 ind : this) {
            int cantidad = (int) ((1.0 / ind.getFitness() / sumaFitness) * tamPoblacion); // Menor fitness = más probabilidades
            for (int j = 0; j < cantidad; j++) {
                nuevaGeneracion.add((IndividuoFuncion5) ind.clone());
            }
        }
        while (nuevaGeneracion.size() < tamPoblacion) {
            nuevaGeneracion.addAll(this.seleccionRuleta());
        }
        return nuevaGeneracion;
    }
	

    public void mutacion() {
        Random rand = new Random();

        for (IndividuoFuncion5 individuo : this) {
            Double[] cromosoma = individuo.getCromosoma();

            for (int i = 0; i < cromosoma.length; i++) {
                if (rand.nextDouble() < algoritmo.getProbMutacion()) {
                    // Aplicamos una perturbación aleatoria dentro del rango definido
                    cromosoma[i] += (rand.nextDouble() * 2 - 1);
                }
            }
        }
    }
	
	
	
	public void cruceMonopunto() {
	    Random rand = new Random();
	    PoblacionFun5 nuevaPoblacion = new PoblacionFun5(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	        IndividuoFuncion5 padre1 = this.get(i);
	        IndividuoFuncion5 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            // Se aplica el cruce directamente sobre los padres
	            padre1.cruzarMonopunto(padre2);
	        }

	        nuevaPoblacion.add((IndividuoFuncion5) padre1.clone());
			nuevaPoblacion.add((IndividuoFuncion5) padre2.clone());
	    }

	    // Si la población era impar, copiamos el último individuo sin cambios
	    if (this.size() % 2 != 0) {
	        nuevaPoblacion.add((IndividuoFuncion5) this.get(this.size() - 1).clone());
	    }

	    // Reemplazamos la población actual con la nueva
	    this.clear();
	    this.addAll(nuevaPoblacion);
	}

	
	public void cruceUniforme() {
	    Random rand = new Random();
	    PoblacionFun5 nuevaPoblacion = new PoblacionFun5(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	        IndividuoFuncion5 padre1 = this.get(i);
	        IndividuoFuncion5 padre2 = this.get(i + 1);

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            // Aplicamos cruce uniforme a los dos padres
	            padre1.cruzarUniforme(padre2);
	        }

	        nuevaPoblacion.add((IndividuoFuncion5) padre1.clone());
			nuevaPoblacion.add((IndividuoFuncion5) padre2.clone());
	    }

	    // Si la población era impar, copiar el último individuo sin cambios
	    if (this.size() % 2 != 0) {
	        nuevaPoblacion.add((IndividuoFuncion5) this.get(this.size() - 1).clone());
	    }

	    // Reemplazamos la población actual con la nueva
	    this.clear();
	    this.addAll(nuevaPoblacion);
	}
	
	public void cruceAritmetico() {
	    Random rand = new Random();
	    PoblacionFun5 nuevaPoblacion = new PoblacionFun5(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	        IndividuoFuncion5 padre1 = this.get(i);
	        IndividuoFuncion5 padre2 = this.get(i + 1);
	        IndividuoFuncion5 hijo1 = padre1.clone();
	        IndividuoFuncion5 hijo2 = padre2.clone();
	        
	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            Double[] cromPadre1 = padre1.getCromosoma();
	            Double[] cromPadre2 = padre2.getCromosoma();
	            Double[] cromHijo1 = new Double[cromPadre1.length];
	            Double[] cromHijo2 = new Double[cromPadre2.length];

	            for (int j = 0; j < cromPadre1.length; j++) {
	            	cromHijo1[j] = 0.5 * cromPadre1[j] + 0.5 * cromPadre2[j];
	            	cromHijo2[j] = 0.5 * cromPadre2[j] + 0.5 * cromPadre1[j];
	            }
	            
	            hijo1.setCromosoma(cromHijo1);
	            hijo2.setCromosoma(cromHijo2);

	            nuevaPoblacion.add(hijo1);
	            nuevaPoblacion.add(hijo2);
	        } else {
	            nuevaPoblacion.add((IndividuoFuncion5) padre1.clone());
	            nuevaPoblacion.add((IndividuoFuncion5) padre2.clone());
	        }
	    }

	    if (this.size() % 2 != 0) {
	        nuevaPoblacion.add((IndividuoFuncion5) this.get(this.size() - 1).clone());
	    }

	    this.clear();
	    this.addAll(nuevaPoblacion);
	}

	public void cruceBLXAlpha(double alpha) {
	    Random rand = new Random();
	    PoblacionFun5 nuevaPoblacion = new PoblacionFun5(this.tamPoblacion, algoritmo);

	    int limite = (this.size() % 2 == 0) ? this.size() : this.size() - 1;

	    for (int i = 0; i < limite; i += 2) {
	        IndividuoFuncion5 padre1 = this.get(i);
	        IndividuoFuncion5 padre2 = this.get(i + 1);
	        IndividuoFuncion5 hijo1 = padre1.clone();
	        IndividuoFuncion5 hijo2 = padre2.clone();

	        if (rand.nextDouble() < algoritmo.getProbCruce()) {
	            Double[] cromPadre1 = padre1.getCromosoma();
	            Double[] cromPadre2 = padre2.getCromosoma();
	            Double[] cromHijo1 = new Double[cromPadre1.length];
	            Double[] cromHijo2 = new Double[cromPadre2.length];

	            for (int j = 0; j < cromPadre1.length; j++) {
	                double min = Math.min(cromPadre1[j], cromPadre2[j]);
	                double max = Math.max(cromPadre1[j], cromPadre2[j]);
	                double d = max - min;

	                cromHijo1[j] = min - alpha * d + rand.nextDouble() * ((1 + 2 * alpha) * d);
	                cromHijo2[j] = min - alpha * d + rand.nextDouble() * ((1 + 2 * alpha) * d);
	            }

	            nuevaPoblacion.add(hijo1);
	            nuevaPoblacion.add(hijo2);
	        } else {
	            nuevaPoblacion.add((IndividuoFuncion5) padre1.clone());
	            nuevaPoblacion.add((IndividuoFuncion5) padre2.clone());
	        }
	    }

	    if (this.size() % 2 != 0) {
	        nuevaPoblacion.add((IndividuoFuncion5) this.get(this.size() - 1).clone());
	    }

	    this.clear();
	    this.addAll(nuevaPoblacion);
	}


	@Override
	public IndividuoFuncion5 getMejorIndividuo() {
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
			mejorIndividuoAbsoluto = (IndividuoFuncion5) mejorIndividuo.clone();
		}
		
	}
	
	@Override
	public void setAbsoluto(Individuo ind) {
		// TODO Auto-generated method stub
		mejorIndividuoAbsoluto = (IndividuoFuncion5) ind;
	}

	@Override
	public Individuo getAbsoluto() {
		// TODO Auto-generated method stub
		return mejorIndividuoAbsoluto;
	}
}
