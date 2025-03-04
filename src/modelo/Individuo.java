package modelo;

public class Individuo <T> {

	T[] cromosoma;
	int[] tamGenes;
	
	protected double[] min;
	protected double[] max;
	protected double valorError;

	
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

