package practica1;
import java.util.Arrays;
import java.util.Random;

public class IndividuoFuncion1 extends Individuo<Boolean> {
	
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

	
	@Override
	public Boolean fitness() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int tamGen(double valorError, double min, double max) {
		 return (int) (Math.log10(((max - min) / precision) + 1) / Math.log10(2));
	}

	public double getValor() {
		double x1 = this.getFenotipo(0), x2 = this.getFenotipo(1);
		System.out.println("X1: " + String.valueOf(x1) + ", X2: " + String.valueOf(x2));
		return (21.5 + x1 * Math.sin(4 * Math.PI * x1) + x2 * Math.sin(20 * Math.PI * x2));
	}

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

	    // Extraer la parte correspondiente del cromosoma
	    Boolean[] subCromosoma = Arrays.copyOfRange(this.cromosoma, inicio, fin);

	    double xV = this.min[i] + bin2dec(subCromosoma) * aux;
		
		return xV;
	}
	
	public double getFitness() { 
		return this.getValor();
	}
	
	
	

}
