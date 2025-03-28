package modelo;
import java.util.Arrays;
import java.util.Random;

// Clase que representa a un indivuduo de la funcion 1
public class IndividuoFuncion1 extends Individuo<Boolean> implements Cloneable {
	
	private Random rand = new Random();
	private static double precision = 0.001;

	// Constructor 
	public IndividuoFuncion1() {
		
		this.tamGenes = new int[2]; 
		this.min = new double[2]; 
		this.max = new double[2];
		this.min[0] =-3.000;
		this.min[1] = 4.100;
		this.max[0] = 12.100;
		this.max[1] = 5.800;

		// x1∈ [-3.0 , 12.1]
		// x2∈ [4.1 , 5.8]
		this.tamGenes[0] = this.tamGen(this.valorError, min[0], max[0]); 
		this.tamGenes[1] = this.tamGen(this.valorError, min[1], max[1]); 
		int tamTotal = tamGenes[0] + tamGenes[1];
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

	// Funcion que implementa la formula para obtener el valor real del cromosoma
	public double getValor() {
		double x1 = this.getFenotipo(0), x2 = this.getFenotipo(1);
		return (21.5 + x1 * Math.sin(4 * Math.PI * x1) + x2 * Math.sin(20 * Math.PI * x2));
	}
	
	// En esta funcion el fitness es simplemente el valor
	public double getFitness() { 
		return this.getValor();
	}

	// Método que cruza los bits del cromsoma uniformemente
	public void cruzarUniforme(IndividuoFuncion1 otro) {
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
	public void cruzarMonopunto(IndividuoFuncion1 padre2) {
		Random rand = new Random();
	    int puntoCorte = rand.nextInt(this.cromosoma.length); // Punto de corte aleatorio

	    for (int i = puntoCorte; i < this.cromosoma.length; i++) {
	        // Intercambiamos los bits después del punto de corte
	        Boolean temp = this.cromosoma[i];
	        this.cromosoma[i] = padre2.cromosoma[i];
	        padre2.cromosoma[i] = temp;
	    }
		
	}
	
	// Funcion que implementa la formula para obtener los valores reales de los genes del cromosoma
	public double[] getValores() {
		
		double[] valores = {this.getFenotipo(0), this.getFenotipo(1)};
		return valores;
		
	}
	
	@Override
	public String toString() {
		
		double[] valores = this.getValores();
		return "x1 = " + valores[0] + " y x2 = " + valores[1];
	}	
	

}
