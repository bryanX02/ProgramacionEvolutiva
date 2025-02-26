package modelo;
import java.util.Arrays;
import java.util.Random;

// Clase que representa a un indivuduo de la funcion 1
public class IndividuoFuncion4 extends Individuo<Boolean> implements Cloneable {
	
	private Random rand = new Random();
	private static double precision = 0.001;

	// Constructor 
	public IndividuoFuncion4(int dimensiones) {
		
		this.tamGenes = new int[dimensiones]; 
		this.min = new double[dimensiones]; 
		this.max = new double[dimensiones];
		
		int tamTotal = 0;
		for (int i = 0; i<dimensiones; i++) {
			this.min[i] = 0;
			this.max[i] = Math.PI;
			this.tamGenes[i] = this.tamGen(this.valorError, min[i], max[i]);
			tamTotal += tamGenes[i];
		}
		
		this.cromosoma = new Boolean[tamTotal];
		for(int i = 0; i < tamTotal; i++) this.cromosoma[i] = this.rand.nextBoolean();

	}
	
	// Getters y setters  
    public Boolean[] getCromosoma() {
        return this.cromosoma;
    }

    public void setCromosoma(Boolean[] cromosoma) {
        this.cromosoma = cromosoma;
    }

	// Funcion que permite clonar un objeto
	protected IndividuoFuncion4 clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (IndividuoFuncion4) super.clone();
	}

	// ESTO NO FUNCIONARIA
	// Función para obtener los genes separados
    public Boolean[][] getGenes() {
        Boolean[] gen1 = Arrays.copyOfRange(this.cromosoma, 0, this.tamGenes[0]);
        Boolean[] gen2 = Arrays.copyOfRange(this.cromosoma, this.tamGenes[0], this.cromosoma.length);
        return new Boolean[][] {gen1, gen2};
    }
    
	// Método para establecer nuevos genes en el cromosoma
    public void setGenes(Boolean[] gen1, Boolean[] gen2) {
        System.arraycopy(gen1, 0, this.cromosoma, 0, gen1.length);
        System.arraycopy(gen2, 0, this.cromosoma, gen1.length, gen2.length);
    }

    // Funcion que implementa la formula para obtener el tamaño del gen
	public int tamGen(double valorError, double min, double max) {
		 return (int) (Math.log10(((max - min) / precision) + 1) / Math.log10(2));
	}

	// Función que implementa la evaluación de la función Michalewicz
	public double getValor() {
	    int dimensiones = this.tamGenes.length; // Número de dimensiones del problema
	    double m = 10; // Parámetro m dado en la ecuación
	    double sum = 0.0;

	    // Calculamos la suma en la ecuación
	    for (int i = 0; i < dimensiones; i++) {
	        double xi = this.getFenotipo(i); // Obtener el valor real del gen i
	        double term1 = Math.sin(xi);
	        double term2 = Math.pow(Math.sin((i + 1) * xi * xi / Math.PI), 2 * m);
	        sum += term1 * term2;
	    }

	    return -sum; // Se devuelve el negativo de la suma
	}


	// Funcion que convierte binario a decimal
	private int bin2dec(Boolean[] gen) {
		
		int decimal = 0;
	    int length = gen.length;

	    // Iteramos sobre el array booleano desde el último elemento hasta el primero
	    for (int i = 0; i < length; i++) {
	        if (gen[length - 1 - i]) {
	            // Si el valor en la posición es true, añadimos 2^i a la suma decimal
	            decimal += Math.pow(2, i);
	        }
	    }
	    
	    return decimal;
		
	}
	
	// Con esta funcion obtenemos el valor real, usando la formula
	private double getFenotipo(int i) {

		// Calcular el factor de escala
	    double aux = (this.max[i] - this.min[i]) / (Math.pow(2, this.tamGenes[i]) - 1);
	    
	    // Determinar el inicio y fin del segmento del cromosoma a convertir
	    int inicio = (i == 0) ? 0 : this.tamGenes[i - 1];
	    int fin = inicio + this.tamGenes[i];

	    // Extraemos la parte correspondiente del cromosoma
	    Boolean[] subCromosoma = Arrays.copyOfRange(this.cromosoma, inicio, fin);

	    double xV = this.min[i] + bin2dec(subCromosoma) * aux;
		
		return xV;
	}
	
	// En esta funcion el fitness es simplemente el valor
	public double getFitness() { 
		return this.getValor();
	}

	// Método que cruza los bits del cromsoma uniformemente
	public void cruzarUniforme(IndividuoFuncion4 otro) {
	    Random rand = new Random();

	    for (int i = 0; i < this.cromosoma.length; i++) {
	        if (rand.nextBoolean()) { // 50% de probabilidad de intercambiar genes
	            Boolean temp = this.cromosoma[i];
	            this.cromosoma[i] = otro.cromosoma[i];
	            otro.cromosoma[i] = temp;
	        }
	    }
	}

	// Método que cruza los bits del cromsoma en un punto de corte
	public void cruzarMonopunto(IndividuoFuncion4 padre2) {
		Random rand = new Random();
	    int puntoCorte = rand.nextInt(this.cromosoma.length); // Punto de corte aleatorio

	    for (int i = puntoCorte; i < this.cromosoma.length; i++) {
	        // Intercambiamos los bits después del punto de corte
	        Boolean temp = this.cromosoma[i];
	        this.cromosoma[i] = padre2.cromosoma[i];
	        padre2.cromosoma[i] = temp;
	    }
		
	}
	
	@Override
	public String toString() {
		
		return Arrays.toString(cromosoma);
	}	

}
