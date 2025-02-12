package practica1;

public class AlgoritmoGenetico {

	private int tamPoblacion; 
	private IndividuoFuncion1[] poblacion; 
	private double[] fitness; 
	private int maxGeneraciones; 
	private double probCruce; 
	private double probMutacion; 
	private int tamTorneo;
	private Individuo elMejor; 
	private int pos_mejor;


	public AlgoritmoGenetico(int tamPoblacion, int maxGeneraciones, double probCruce, double probMutacion,
			int tamTorneo) {
		super();
		this.tamPoblacion = tamPoblacion;
		this.maxGeneraciones = maxGeneraciones;
		this.probCruce = probCruce;
		this.probMutacion = probMutacion;
		this.tamTorneo = tamTorneo;
	}


	public IndividuoFuncion1[] obtenerNuevaGeneracion() {
		
		// Inciamos la poblacion
		poblacion = new IndividuoFuncion1[this.tamPoblacion];
		
		for (int i = 0; i < this.tamPoblacion; i++) {
			
			poblacion[i] = new IndividuoFuncion1();
			
		}
		
		// Evaluamos la poblacion
		// En este caso no habría nada que analizar
		
		// Crearemos generaciones
		return poblacion;
		
		
		
	}
	
	 
	
}
