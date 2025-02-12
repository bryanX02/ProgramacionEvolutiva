package practica1;

public abstract class Individuo <T> {

	T[] cromosoma;
	int[] tamGenes;
	
	protected double[] min;
	protected double[] max;
	protected double valorError;
	
	public abstract T fitness();
	public abstract int tamGen(double valorError, double min, double max);
	
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

	

}

