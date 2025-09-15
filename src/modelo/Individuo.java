package modelo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Individuo <T> implements Cloneable {

	protected T[] cromosoma;
	protected int[] tamGenes;
	protected double valorError;
	protected int dimensiones;

	protected static final int GRID_SIZE = 15;
	protected static final int NUM_HABITACIONES = 20;
	protected static final int[] BASE = {7, 7};
	protected static final Map<Integer, int[]> HABITACIONES = new HashMap<>();
	protected static final Set<int[]> OBSTACULOS = new HashSet<>();
	
	protected static final int[][] DIRECCIONES = {
    	    {-1, 0}, {1, 0}, {0, -1}, {0, 1} // Solo arriba, abajo, izquierda y derecha
    };
    
    static {
        // Definir las coordenadas fijas de las habitaciones
        int[][] habitacionesFijas = {
            {2,2}, {2,12}, {12,2}, {12,12}, {2,7}, {7,2}, {7,12}, {12,7}, {0,7}, {7,0},
            {14,7}, {7,14}, {0,0}, {0,14}, {14,0}, {14,14}, {4,4}, {4,12}, {10,4}, {10,12}
        };
        for (int i = 0; i < NUM_HABITACIONES; i++) {
            HABITACIONES.put(i + 1, habitacionesFijas[i]);
        }
        
        // Definir los obstáculos
        int[][] obstaculosFijos = {
            {5,5}, {5,6}, {5,7}, {5,8}, {5,9},
            {8,10}, {9,10}, {10,10}, {11,10}, {12,10},
            {10,3}, {11,4},
            {10,6}, {11,6}, {12,6}, {13,6},
            {8,1}, {8,2}, {8,3}, {8,4},
            {0,13}, {1,13},
            {3,8}, {3,9}, {3,10}, {3,11}
        };
        for (int[] obst : obstaculosFijos) {
            OBSTACULOS.add(obst);
        }
    }
	
	
	// Funcion que permite clonar un objeto
    @Override
    public Individuo<T> clone() {
        try {
            // La llamada a super.clone() hace una copia superficial de los campos.
            @SuppressWarnings("unchecked")
            Individuo<T> copia = (Individuo<T>) super.clone();

            // Ahora hacemos una copia profunda de los arrays para que no se compartan.
            if (this.cromosoma != null) {
                copia.cromosoma = this.cromosoma.clone();
            }
            if (this.tamGenes != null) {
                copia.tamGenes = this.tamGenes.clone();
            }

            return copia;
        } catch (CloneNotSupportedException e) {
            // Esta excepción no debería ocurrir si la clase implementa Cloneable.
            throw new RuntimeException("Error al clonar el individuo. ¿Implementa Cloneable?", e);
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

