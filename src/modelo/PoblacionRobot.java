package modelo;

import java.util.Random;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Collections;

public class PoblacionRobot extends Poblacion<IndividuoRobot> {

    private int tamPoblacion;
    private AlgoritmoGenetico algoritmo;
    private static IndividuoRobot mejorIndividuoAbsoluto;

    public PoblacionRobot(int tamPoblacion, AlgoritmoGenetico algoritmo) {
        this.tamPoblacion = tamPoblacion;
        this.algoritmo = algoritmo;
    }

    public int getTamPoblacion() {
        return tamPoblacion;
    }

    public void setTamPoblacion(int tamPoblacion) {
        this.tamPoblacion = tamPoblacion;
    }

    @Override
    public void iniciarGeneracion() {
        for (int i = 0; i < tamPoblacion; i++) {
            this.add(new IndividuoRobot());
        }
    }

    @Override
    public double getFitnessMedio() {
        return this.isEmpty() ? 0.0 : this.stream().mapToDouble(IndividuoRobot::getFitness).average().orElse(0.0);
    }

    @Override
    public double getExtremo() {
        return Double.MAX_VALUE; // Optimizado para minimizar (antes era MIN_VALUE)
    }

    @Override
    public PoblacionRobot seleccionarSegun(String metodoSeleccion) {
        return switch (metodoSeleccion) {
            case "Ruleta" -> seleccionRuleta();
            case "Torneo Probabilístico" -> seleccionTorneoProbabilistico(0.5); // Probabilidad de 50%
            case "Torneo Determinístico" -> seleccionTorneoDeterministico();
            case "Estocástico Universal" -> seleccionEstocasticaUniversal();
            case "Truncamiento" -> seleccionTruncamiento(0.5); // Porcentaje de truncamiento (50%)
            case "Restos" -> seleccionRestos();
            case "Ranking" -> seleccionRanking();
            default -> throw new IllegalArgumentException("Método de selección no válido: " + metodoSeleccion);
        };
    }
    
    @Override
    public void cruzarSegun(String metodoCruce) {
        switch (metodoCruce) {
            case "Monopunto" -> cruceMonopunto();
            case "Uniforme" -> cruceUniforme();
            case "PMX" -> crucePMX();
            case "OX" -> cruceOX();
            case "OXPP" -> cruceOXPP();
            case "CX" -> cruceCX();
            case "CO" -> cruceCO();
            case "ERX" -> cruceERX();
            case "Propio" -> crucePropio(); // Cruce de invención propia
            case "Ninguno" -> {} // No se aplica cruce
            default -> throw new IllegalArgumentException("Método de cruce no válido: " + metodoCruce);
        }
    }
    
    public void mutarSegun(String metodoMutacion) {
        switch (metodoMutacion) {
            case "Inserción" -> mutacionInsercion();
            case "Intercambio" -> mutacionIntercambio();
            case "Inversión" -> mutacionInversion();
            case "Heurística" -> mutacionHeuristica();
            case "Propia" -> mutacionPropia();
            case "Ninguno" -> {} // No se aplica mutación
            default -> throw new IllegalArgumentException("Método de mutación no válido: " + metodoMutacion);
        }
    }

    private PoblacionRobot seleccionRuleta() {
        double[] fitnessInvertido = this.stream()
                .mapToDouble(ind -> 1.0 / (1.0 + ind.getFitness()))
                .toArray();
        double sumaFitness = 0.0;
        for (double fit : fitnessInvertido) sumaFitness += fit;

        PoblacionRobot nuevaGeneracion = new PoblacionRobot(tamPoblacion, algoritmo);
        Random rand = new Random();

        for (int i = 0; i < tamPoblacion; i++) {
            double r = rand.nextDouble() * sumaFitness, acumulado = 0.0;
            for (int j = 0; j < this.size(); j++) {
                acumulado += fitnessInvertido[j];
                if (acumulado >= r) {
                    nuevaGeneracion.add((IndividuoRobot) this.get(j).clone());
                    break;
                }
            }
        }
        return nuevaGeneracion;
    }

    private PoblacionRobot seleccionTorneoDeterministico() {
        return seleccionTorneo(3, 1.0);
    }

    private PoblacionRobot seleccionTorneoProbabilistico(double P) {
        return seleccionTorneo(3, P);
    }

    private PoblacionRobot seleccionTorneo(int tamTorneo, double probMejor) {
        PoblacionRobot nuevaGeneracion = new PoblacionRobot(tamPoblacion, algoritmo);

        for (int i = 0; i < tamPoblacion; i++) {
            IndividuoRobot mejor = null, peor = null;

            // Seleccionar al azar tamTorneo individuos
            for (int j = 0; j < tamTorneo; j++) {
                IndividuoRobot candidato = this.get(algoritmo.rand.nextInt(tamPoblacion));

                if (mejor == null || candidato.getFitness() < mejor.getFitness()) {
                    peor = mejor;
                    mejor = candidato;
                } else if (peor == null || candidato.getFitness() > peor.getFitness()) {
                    peor = candidato;
                }
            }

            // Asegurar que siempre se seleccionan individuos válidos
            if (peor == null) peor = mejor;

            // Selección con probabilidad
            nuevaGeneracion.add(algoritmo.rand.nextDouble() < probMejor ? (IndividuoRobot) mejor.clone() : (IndividuoRobot) peor.clone());
        }
        return nuevaGeneracion;
    }
    
    private PoblacionRobot seleccionEstocasticaUniversal() {
        double[] fitnessInvertido = this.stream()
                .mapToDouble(ind -> 1.0 / (1.0 + ind.getFitness()))
                .toArray();
        double sumaFitness = Arrays.stream(fitnessInvertido).sum();

        PoblacionRobot nuevaGeneracion = new PoblacionRobot(tamPoblacion, algoritmo);
        
        double distancia = sumaFitness / tamPoblacion;
        double inicio = algoritmo.rand.nextDouble() * distancia;

        for (int i = 0; i < tamPoblacion; i++) {
            double punto = inicio + i * distancia;
            double acumulado = 0.0;
            for (int j = 0; j < this.size(); j++) {
                acumulado += fitnessInvertido[j];
                if (acumulado >= punto) {
                    nuevaGeneracion.add((IndividuoRobot) this.get(j).clone());
                    break;
                }
            }
        }
        return nuevaGeneracion;
    }
    
    private PoblacionRobot seleccionTruncamiento(double porcentaje) {
        this.sort(Comparator.comparingDouble(IndividuoRobot::getFitness));
        int limite = (int) (tamPoblacion * porcentaje);
        PoblacionRobot nuevaGeneracion = new PoblacionRobot(tamPoblacion, algoritmo);

        for (int i = 0; i < tamPoblacion; i++) {
            nuevaGeneracion.add((IndividuoRobot) this.get(i % limite).clone());
        }
        return nuevaGeneracion;
    }
    
    private PoblacionRobot seleccionRestos() {
        double[] fitnessInvertido = this.stream()
                .mapToDouble(ind -> 1.0 / (1.0 + ind.getFitness()))
                .toArray();
        double sumaFitness = Arrays.stream(fitnessInvertido).sum();

        PoblacionRobot nuevaGeneracion = new PoblacionRobot(tamPoblacion, algoritmo);
        for (int i = 0; i < this.size(); i++) {
            int veces = (int) (fitnessInvertido[i] / sumaFitness * tamPoblacion);
            for (int j = 0; j < veces; j++) {
                nuevaGeneracion.add((IndividuoRobot) this.get(i).clone());
            }
        }

        // Completar la población con selección por ruleta
        while (nuevaGeneracion.size() < tamPoblacion) {
            nuevaGeneracion.add((IndividuoRobot) seleccionRuleta().get(0).clone());
        }
        return nuevaGeneracion;
    }
    
    private PoblacionRobot seleccionRanking() {
        this.sort(Comparator.comparingDouble(IndividuoRobot::getFitness));
        double[] probabilidades = new double[tamPoblacion];
        double sumaProbabilidades = 0.0;

        // Asignar probabilidades basadas en el ranking
        for (int i = 0; i < tamPoblacion; i++) {
            probabilidades[i] = (tamPoblacion - i) / (double) tamPoblacion;
            sumaProbabilidades += probabilidades[i];
        }

        PoblacionRobot nuevaGeneracion = new PoblacionRobot(tamPoblacion, algoritmo);

        for (int i = 0; i < tamPoblacion; i++) {
            double r = algoritmo.rand.nextDouble() * sumaProbabilidades;
            double acumulado = 0.0;
            for (int j = 0; j < tamPoblacion; j++) {
                acumulado += probabilidades[j];
                if (acumulado >= r) {
                    nuevaGeneracion.add((IndividuoRobot) this.get(j).clone());
                    break;
                }
            }
        }
        return nuevaGeneracion;
    }
    
    public void cruceMonopunto() {
        realizarCruce((p1, p2) -> p1.cruzarMonopunto(p2));
    }

    public void cruceUniforme() {
        realizarCruce((p1, p2) -> p1.cruzarUniforme(p2));
    }

    public void crucePMX() {
        realizarCruce((p1, p2) -> p1.cruzarPMX(p2));
    }

    public void cruceOX() {
        realizarCruce((p1, p2) -> p1.cruzarOX(p2));
    }

    public void cruceOXPP() {
        realizarCruce((p1, p2) -> p1.cruzarOXPP(p2));
    }

    public void cruceCX() {
        realizarCruce((p1, p2) -> p1.cruzarCX(p2));
    }

    public void cruceCO() {
        realizarCruce((p1, p2) -> p1.cruzarCO(p2));
    }

    public void cruceERX() {
        realizarCruce((p1, p2) -> p1.cruzarERX(p2));
    }
    
    public void crucePropio() {
        realizarCruce((p1, p2) -> p1.cruzarPropio(p2));
    }
    
    public void mutacionInsercion() {
        for (IndividuoRobot ind : this) {
            if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                ind.mutacionInsercion();
            }
        }
    }

    public void mutacionIntercambio() {
        for (IndividuoRobot ind : this) {
            if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                ind.mutacionIntercambio();
            }
        }
    }
    
    public void mutacionInversion() {
        for (IndividuoRobot ind : this) {
            if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                ind.mutacionInversion();
            }
        }
    }
    
    public void mutacionHeuristica() {
        for (IndividuoRobot ind : this) {
            if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                ind.mutacionHeuristica();
            }
        }
    }

    public void mutacionPropia() {
        for (IndividuoRobot ind : this) {
            if (algoritmo.rand.nextDouble() < algoritmo.getProbMutacion()) {
                ind.mutacionPropia();
            }
        }
    }
    
    private void realizarCruce(CruceOperator operador) {
        
        int limite = this.size() - (this.size() % 2); // Asegura pares de individuos

        for (int i = 0; i < limite; i += 2) {
            IndividuoRobot p1 = this.get(i), p2 = this.get(i + 1);
            if (algoritmo.rand.nextDouble() < algoritmo.getProbCruce()) {
                operador.aplicar(p1, p2);
            }
            this.set(i, (IndividuoRobot) p1.clone());
            this.set(i + 1, (IndividuoRobot) p2.clone());
        }
    }

    @FunctionalInterface
    private interface CruceOperator {
        void aplicar(IndividuoRobot p1, IndividuoRobot p2);
    }

    @Override
    public IndividuoRobot getMejorIndividuo() {
        return Collections.min(this, Comparator.comparingDouble(IndividuoRobot::getFitness));
    }

    @Override
    public void actualizarAbsoluto(Individuo mejorIndividuo) {
        if (mejorIndividuoAbsoluto == null || mejorIndividuo.getFitness() < mejorIndividuoAbsoluto.getFitness()) {
            mejorIndividuoAbsoluto = (IndividuoRobot) mejorIndividuo.clone();
        }
    }

    @Override
    public void setAbsoluto(Individuo ind) {
        mejorIndividuoAbsoluto = (IndividuoRobot) ind;
    }

    @Override
    public Individuo getAbsoluto() {
        return mejorIndividuoAbsoluto;
    }
}
