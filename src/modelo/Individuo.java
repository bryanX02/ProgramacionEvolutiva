package modelo;

import java.util.Arrays;

public class Individuo <T> implements Cloneable {

	protected T[] cromosoma;
	protected int[] tamGenes;
	protected double[] min;
	protected double[] max;
	protected double valorError;
	protected int dimensiones;

	
	
	// Funcion que permite clonar un objeto
	@Override
	public Individuo<T> clone() {
	    try {
	        Individuo<T> copia = (Individuo<T>) super.clone(); // Clonación superficial

	        // Clonación profunda de los arrays
	        copia.cromosoma = this.cromosoma.clone();
	        copia.tamGenes = this.tamGenes.clone();
	        copia.min = this.min.clone();
	        copia.max = this.max.clone();

	        return copia;
	    } catch (CloneNotSupportedException e) {
	        throw new RuntimeException("Error al clonar el individuo", e);
	    }
	}

	
	public T[] getCromosoma() {
		return cromosoma;
	}
	public void setCromosoma(T[] cromosoma) {
		this.cromosoma = cromosoma;
	}
	public int[] getTamGenes() {
		return tamGenes;
	}
	public void setTamGenes(int[] tamGenes) {
		this.tamGenes = tamGenes;
	}
	
	public double getFitness() {
		return 0.0;
	}

	// Con esta funcion obtenemos el valor real, usando la formula
	protected double getFenotipo(int i) {

		// Calcular el factor de escala
	    double aux = (this.max[i] - this.min[i]) / (Math.pow(2, this.tamGenes[i]) - 1);
	    
	    // Determinar el inicio y fin del segmento del cromosoma a convertir
	    int inicio = 0;
	    for (int j = 0; j < i; j++) {
	        inicio += this.tamGenes[j];
	    }
	    int fin = inicio + this.tamGenes[i];

	    // Extraemos la parte correspondiente del cromosoma
	    Boolean[] subCromosoma = (Boolean[]) Arrays.copyOfRange(this.cromosoma, inicio, fin);

	    double xV = this.min[i] + bin2dec(subCromosoma) * aux;
		
		return xV;
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

}

