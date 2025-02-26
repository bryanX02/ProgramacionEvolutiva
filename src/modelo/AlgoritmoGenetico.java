package modelo;

public class AlgoritmoGenetico {

	private int tamPoblacion;
	private double[] fitness; 
	private int maxGeneraciones; 
	private double probCruce; 
	private double probMutacion; 
	private int tamTorneo;
	private Individuo elMejor; 
	private int pos_mejor;
	private double elitismo;


	public AlgoritmoGenetico(int tamPoblacion, int maxGeneraciones, double probCruce, double probMutacion, double elitismo, int tamTorneo) {
		super();
		this.tamPoblacion = tamPoblacion;
		this.maxGeneraciones = maxGeneraciones;
		this.probCruce = probCruce;
		this.probMutacion = probMutacion;
		this.tamTorneo = tamTorneo;
		this.elitismo = elitismo;
	}


	public int getMaxGeneraciones() {
		return maxGeneraciones;
	}


	public void setMaxGeneraciones(int maxGeneraciones) {
		this.maxGeneraciones = maxGeneraciones;
	}


	public double getProbCruce() {
		return probCruce;
	}


	public void setProbCruce(double probCruce) {
		this.probCruce = probCruce;
	}


	public double getProbMutacion() {
		return probMutacion;
	}


	public void setProbMutacion(double probMutacion) {
		this.probMutacion = probMutacion;
	}


	public double[] getFitness() {
		return fitness;
	}


	public void setFitness(double[] fitness) {
		this.fitness = fitness;
	}


	public int getTamTorneo() {
		return tamTorneo;
	}


	public void setTamTorneo(int tamTorneo) {
		this.tamTorneo = tamTorneo;
	}


	public Individuo getElMejor() {
		return elMejor;
	}


	public void setElMejor(Individuo elMejor) {
		this.elMejor = elMejor;
	}


	public int getPos_mejor() {
		return pos_mejor;
	}


	public void setPos_mejor(int pos_mejor) {
		this.pos_mejor = pos_mejor;
	}

	
	
}
