package modelo;

import java.util.Random;

public class AlgoritmoGenetico {
    public Random rand = new Random(); // Generador aleatorio
    private double probMutacion = 0.1;
    private double probCruce = 0.6;
    private int profundidadInicial = 6; // Profundidad para generar árboles iniciales
    private String metodoInicializacion = "Ramped"; // "Completa", "Creciente", "Ramped"
    private int profMaxBloating = 10; // Profundidad máxima post-operaciones
    private int tamTorneo = 3; // Tamaño para selección por torneo
    private double probTorneoProb = 0.75; // Probabilidad de elegir al mejor en torneo probabilístico
    private double porcTruncamiento = 0.5; // Porcentaje para selección por truncamiento

    // Getters (necesarios para PoblacionHormiga)
    public double getProbMutacion() { return probMutacion; }
    public double getProbCruce() { return probCruce; }
    public int getProfundidadInicial() { return profundidadInicial; }
    public String getMetodoInicializacion() { return metodoInicializacion; }
    public int getProfMaxBloating() { return profMaxBloating; }
    public int getTamTorneo() { return tamTorneo; }
    public double getProbTorneoProb() { return probTorneoProb; }
    public double getPorcTruncamiento() { return porcTruncamiento; }

    // Setters (para configurar desde fuera)
    public void setProbMutacion(double probMutacion) { this.probMutacion = probMutacion; }
    public void setProbCruce(double probCruce) { this.probCruce = probCruce; }
    public void setProfundidadInicial(int profundidadInicial) { this.profundidadInicial = profundidadInicial; }
    public void setMetodoInicializacion(String metodoInicializacion) { this.metodoInicializacion = metodoInicializacion; }
    public void setProfMaxBloating(int profMaxBloating) { this.profMaxBloating = profMaxBloating; }
    public void setTamTorneo(int tamTorneo) { this.tamTorneo = tamTorneo; }
    public void setProbTorneoProb(double probTorneoProb) { this.probTorneoProb = probTorneoProb; }
    public void setPorcTruncamiento(double porcTruncamiento) { this.porcTruncamiento = porcTruncamiento; }
}
