package modelo;

public class Individuo <T> implements Cloneable {

	T[] cromosoma;
	int[] tamGenes;
	
	protected double[] min;
	protected double[] max;
	protected double valorError;

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

	

}

