package modelo;
import java.util.Arrays;
import java.util.Random;

// Clase que representa a un indivuduo de la funcion 1
public class IndividuoFuncion5 extends Individuo<Double> implements Cloneable {
	
	private Random rand = new Random();
	private int dimensiones;
	private double alpha = 0.5;
	

	// Constructor 
	public IndividuoFuncion5(int dimensiones) {
        this.dimensiones = dimensiones;
        this.cromosoma = new Double[dimensiones];
        this.min = new double[dimensiones];
        this.max = new double[dimensiones];
        
        for (int i = 0; i < dimensiones; i++) {
            this.min[i] = 0;
            this.max[i] = Math.PI;
            this.cromosoma[i] = min[i] + (max[i] - min[i]) * rand.nextDouble();
        }
    }
	
	// Método para obtener el valor de la función Michalewicz
    public double getValor() {
        double m = 10;
        double sum = 0.0;
        
        for (int i = 0; i < dimensiones; i++) {
            double xi = this.cromosoma[i];
            double term1 = Math.sin(xi);
            double term2 = Math.pow(Math.sin((i + 1) * xi * xi / Math.PI), 2 * m);
            sum += term1 * term2;
        }
        
        return -sum;
    }
	
	// Fitness es el valor de la función
    @Override
    public double getFitness() {
        return this.getValor();
    }
    
    // Cruce Monopunto
    public void cruzarMonopunto(IndividuoFuncion5 otro) {
        int puntoCorte = rand.nextInt(dimensiones);
        for (int i = puntoCorte; i < dimensiones; i++) {
            double temp = this.cromosoma[i];
            this.cromosoma[i] = otro.cromosoma[i];
            otro.cromosoma[i] = temp;
        }
    }
	
	// Cruce Uniforme
    public void cruzarUniforme(IndividuoFuncion5 otro) {
        for (int i = 0; i < dimensiones; i++) {
            if (rand.nextBoolean()) {
                double temp = this.cromosoma[i];
                this.cromosoma[i] = otro.cromosoma[i];
                otro.cromosoma[i] = temp;
            }
        }
    }
    
    // Cruce Aritmético (promedio)
    public void cruzarAritmetico(IndividuoFuncion5 otro) {
        for (int i = 0; i < dimensiones; i++) {
            this.cromosoma[i] = (this.cromosoma[i] + otro.cromosoma[i]) / 2;
        }
    }
    
    // Cruce BLX-α
    public void cruzarBLX(IndividuoFuncion5 otro) {
        for (int i = 0; i < dimensiones; i++) {
            double cMin = Math.min(this.cromosoma[i], otro.cromosoma[i]);
            double cMax = Math.max(this.cromosoma[i], otro.cromosoma[i]);
            double range = cMax - cMin;
            this.cromosoma[i] = cMin - alpha * range + (range + 2 * alpha * range) * rand.nextDouble();
        }
    }
    
    // Mutación sobre valores reales
    public void mutacion(double probMutacion) {
        for (int i = 0; i < dimensiones; i++) {
            if (rand.nextDouble() < probMutacion) {
                this.cromosoma[i] = min[i] + (max[i] - min[i]) * rand.nextDouble();
            }
        }
    }
    
    @Override
    public String toString() {
        return Arrays.toString(cromosoma);
    }

}
