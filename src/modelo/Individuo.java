package modelo;


public class Individuo <T> implements Cloneable {

	protected T[] cromosoma;
	protected int[] tamGenes;
	protected double valorError;
	protected int dimensiones;

	protected static final int[][] DIRECCIONES = {
    	    {-1, 0}, {1, 0}, {0, -1}, {0, 1} // Solo arriba, abajo, izquierda y derecha
    };
    
    public static int[][] SANTA_FE_TRAIL_COORDS = {
        {0,0}, {0,1}, {0,2}, {0,3},
        {1,3},
        {2,3}, {2,25}, {2,26}, {2,27},
        {3,3}, {3,24}, {3,29},
        {4,3}, {4,24}, {4,29}, 
        {5,3}, {5,4}, {5,5}, {5,6}, {5,8}, {5,9}, {5,10}, {5,11}, {5,12}, {5,21}, {5,22},
        {6,12}, {6,29},
        {7,12},
        {8,12}, {8,20},
        {9,12}, {9,20}, {9,29},
        {10,12}, {10,20},
        {11,20},
        {12,12}, {12,29},
        {13,12},
        {14,12}, {14,20}, {14,26}, {14,27}, {14,28},
        {15,12}, {15,20}, {15,23},
        {16,17},
        {17,16},
        {18,12}, {18,16}, {18,24},
        {19,12}, {19,16}, {19,27},
        {20,12},
        {21,12}, {21,16},
        {22,12}, {22,26},
        {23,12}, {23,23},
        {24,3}, {24,4}, {24,7}, {24,8}, {24,9}, {24,10}, {24,11}, {24,16},
        {25,1}, {25,16},
        {26,1}, {26,16},
        {27,1}, {27,8}, {27,9}, {27,10}, {27,11},{27,12}, {27,13},{27,14},
        {28,1}, {28,7},
        {29,7},
        {30,2}, {30,3}, {30,4}, {30,5}
    };
   
	
	
	// Funcion que permite clonar un objeto
	@Override
	public Individuo<T> clone() {
	    try {
	        Individuo<T> copia = (Individuo<T>) super.clone(); // Clonación superficial

	        // Clonación profunda de los arrays
	        copia.cromosoma = this.cromosoma.clone();
	        copia.tamGenes = this.tamGenes.clone();

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
	protected double getFenotipo() {
		return 0;

	}
	

}

