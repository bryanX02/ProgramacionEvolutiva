package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import modelo.*; // Asegúrate que importa tus clases IndividuoHormiga, PoblacionHormiga, etc.
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

/**
 * Clase que representa la ventana principal de la interfaz gráfica para el
 * problema de la Hormiga con Algoritmos Genéticos.
 */
public class JFramePoblacionHormiga extends JFrame {

    // --- Componentes de Configuración ---
    private JTextField txtPoblacion, txtGeneraciones, txtCruce, txtMutacion, txtElitismo;
    private JTextField txtProfundidadInicial, txtProfundidadBloating;
    private JTextField txtPasosMejor;
    private JComboBox<String> cmbSeleccion, cmbInicializacion, cmbMutacion; // cmbCruce eliminado (solo hay 1)
    private JButton btnEjecutar;
    private JTextArea txtAreaResultadoArbol; // Para mostrar el mejor árbol
    private JTextField txtFitnessFinal;
    private JTextField txtPasosFinales;

    // --- Componentes de Visualización ---
    private XYSeries mejorSeries;
    private XYSeries mediaSeries;
    private XYSeries mejorAbsolutoSeries;
    private ChartPanel chartPanel; // Panel para la gráfica JFreeChart
    private PanelMapa panelMapa;   // Panel personalizado para dibujar el mapa

    // --- Lógica del Algoritmo ---
    private AlgoritmoGenetico algoritmo; // Configuración del AG

    /**
     * Constructor principal de la ventana JFrame.
     */
    public JFramePoblacionHormiga() {
        setTitle("Algoritmo Genético - Hormiga Santa Fe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(50, 50, 1200, 750); // Ajustamos tamaño ventana
        setLayout(new BorderLayout(5, 5)); // Usamos BorderLayout

        // --- Creamos los paneles principales ---
        JPanel panelConfiguracion = crearPanelConfiguracion();
        JPanel panelVisualizacion = crearPanelVisualizacion();

        // --- Añadimos paneles a la ventana ---
        add(panelConfiguracion, BorderLayout.WEST);
        add(panelVisualizacion, BorderLayout.CENTER);

        // --- Acción del Botón ---
        btnEjecutar.addActionListener(e -> ejecutarAlgoritmoEnSegundoPlano());

        pack(); // Ajusta el tamaño de la ventana a los componentes
        setLocationRelativeTo(null); // Centra la ventana
    }

    /**
     * Método que crea y configura el panel lateral de parámetros.
     * @return El JPanel de configuración.
     */
    private JPanel crearPanelConfiguracion() {
        JPanel panel = new JPanel();
        // Usamos GridBagLayout para mejor control
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5); // Espaciado

        // --- Parámetros Población/Generaciones ---
        panel.add(new JLabel("Tamaño Población:"), gbc);
        gbc.gridx++;
        txtPoblacion = new JTextField("100", 5); // Tamaño reducido para pruebas rápidas
        panel.add(txtPoblacion, gbc);
        gbc.gridx = 0; gbc.gridy++;

        panel.add(new JLabel("Nº Generaciones:"), gbc);
        gbc.gridx++;
        txtGeneraciones = new JTextField("400", 5); // Generaciones reducidas
        panel.add(txtGeneraciones, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // --- Parámetros Probabilidades ---
        panel.add(new JLabel("Prob. Cruce (%):"), gbc);
        gbc.gridx++;
        txtCruce = new JTextField("70", 5);
        panel.add(txtCruce, gbc);
        gbc.gridx = 0; gbc.gridy++;

        panel.add(new JLabel("Prob. Mutación (%):"), gbc);
        gbc.gridx++;
        txtMutacion = new JTextField("70", 5); // Probabilidad más baja suele ser mejor
        panel.add(txtMutacion, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // --- Parámetros Árbol ---
        panel.add(new JLabel("Profundidad Inicial:"), gbc);
        gbc.gridx++;
        txtProfundidadInicial = new JTextField("3", 5);
        panel.add(txtProfundidadInicial, gbc);
        gbc.gridx = 0; gbc.gridy++;

        panel.add(new JLabel("Método Inicialización:"), gbc);
        gbc.gridx++;
        cmbInicializacion = new JComboBox<>(new String[]{"Ramped", "Creciente", "Completa"});
        panel.add(cmbInicializacion, gbc);
        gbc.gridx = 0; gbc.gridy++;

        panel.add(new JLabel("Control de Bloating (profundidad):"), gbc);
        gbc.gridx++;
        txtProfundidadBloating = new JTextField("20", 5);
        panel.add(txtProfundidadBloating, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // --- Parámetros Selección ---
        panel.add(new JLabel("Método Selección:"), gbc);
        gbc.gridx++;
        cmbSeleccion = new JComboBox<>(new String[]{
                "Torneo Determinístico", "Torneo Probabilístico", "Ruleta",
                "Estocástico Universal", "Truncamiento", "Restos", "Ranking"
        });
        cmbSeleccion.setSelectedItem("Ruleta"); // Valor por defecto
        panel.add(cmbSeleccion, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // --- Parámetros Mutación ---
        panel.add(new JLabel("Método Mutación:"), gbc);
        gbc.gridx++;
        cmbMutacion = new JComboBox<>(new String[]{
                "Subárbol", "Terminal", "Funcional", "Hoist" // Ordenados por impacto (aprox)
        });
        cmbMutacion.setSelectedItem("Hoist");
        panel.add(cmbMutacion, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // --- Elitismo ---
        panel.add(new JLabel("Elitismo (%):"), gbc);
        gbc.gridx++;
        txtElitismo = new JTextField("5", 5); // Un pequeño elitismo suele ayudar
        panel.add(txtElitismo, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // --- Botón Ejecutar ---
        gbc.gridwidth = 2; // Ocupa dos columnas
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        btnEjecutar = new JButton("Ejecutar Algoritmo");
        panel.add(btnEjecutar, gbc);
        gbc.gridy++;

        // --- Área para mostrar el mejor árbol ---
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Mejor Árbol Encontrado:"), gbc);
        gbc.gridy++;
        txtAreaResultadoArbol = new JTextArea(5, 25); // 5 filas, 25 columnas aprox
        txtAreaResultadoArbol.setLineWrap(true);
        txtAreaResultadoArbol.setWrapStyleWord(true);
        txtAreaResultadoArbol.setEditable(false);
        JScrollPane scrollArbol = new JScrollPane(txtAreaResultadoArbol);
        panel.add(scrollArbol, gbc);
        gbc.gridy++;

       
       

        JPanel panelResultados = new JPanel(new GridLayout(2, 2, 5, 2)); // 2 filas, 2 columnas
        panelResultados.setBorder(BorderFactory.createTitledBorder("Resultado Final"));

        panelResultados.add(new JLabel("Fitness Final:"));
        txtFitnessFinal = new JTextField();
        txtFitnessFinal.setEditable(false);
        txtFitnessFinal.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelResultados.add(txtFitnessFinal);

        panelResultados.add(new JLabel("Pasos Usados:"));
        txtPasosFinales = new JTextField();
        txtPasosFinales.setEditable(false);
        txtPasosFinales.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelResultados.add(txtPasosFinales);

        panel.add(panelResultados, gbc);
        
        // Añadimos un "pegamento" vertical para empujar todo hacia arriba
        gbc.gridwidth = 2; // Ocupar ancho completo para el pegamento
        gbc.weighty = 1.0; // Permitir que este componente se expanda verticalmente
        gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), gbc);

        gbc.gridy++; // Bajamos una fila
        gbc.weighty = 0; // Quitamos el peso vertical para que no se expanda
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        panel.setBorder(BorderFactory.createTitledBorder("Configuración"));
        return panel;
    }

     /**
     * Método que crea el panel que contendrá la gráfica y el mapa.
     * @return El JPanel de visualización.
     */
     private JPanel crearPanelVisualizacion() {
         JPanel panel = new JPanel(new BorderLayout(5, 5));

         // --- Gráfica de Fitness ---
         mejorSeries = new XYSeries("Mejor Fitness (Gen)");
         mediaSeries = new XYSeries("Fitness Medio (Gen)");
         mejorAbsolutoSeries = new XYSeries("Mejor Absoluto");
         XYSeriesCollection dataset = new XYSeriesCollection();
         dataset.addSeries(mejorSeries);
         dataset.addSeries(mediaSeries);
         dataset.addSeries(mejorAbsolutoSeries);

         JFreeChart chart = ChartFactory.createXYLineChart(
                 "Evolución del Fitness", "Generación", "Fitness (Comida)", dataset,
                 PlotOrientation.VERTICAL, true, true, false);

         // Personalizar apariencia del gráfico (opcional)
         XYPlot plot = chart.getXYPlot();
         plot.setBackgroundPaint(Color.LIGHT_GRAY);
         plot.setDomainGridlinePaint(Color.WHITE);
         plot.setRangeGridlinePaint(Color.WHITE);
         XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
         renderer.setSeriesPaint(0, Color.BLUE); // Mejor Gen
         renderer.setSeriesPaint(1, Color.ORANGE); // Medio Gen
         renderer.setSeriesPaint(2, Color.RED);  // Mejor Absoluto
         renderer.setSeriesStroke(0, new BasicStroke(2.0f));
         renderer.setSeriesStroke(1, new BasicStroke(1.5f));
         renderer.setSeriesStroke(2, new BasicStroke(2.5f));


         chartPanel = new ChartPanel(chart);
         chartPanel.setPreferredSize(new Dimension(600, 350)); // Tamaño preferido

         // --- Panel del Mapa ---
         panelMapa = new PanelMapa(); // Nuestro panel personalizado
         panelMapa.setPreferredSize(new Dimension(400, 400)); // Ajustar tamaño según necesidad

         // --- Contenedor para Mapa y Resultado del Árbol ---
         //JPanel panelInferior = new JPanel(new BorderLayout());
         //panelInferior.add(panelMapa, BorderLayout.CENTER);
         //panelInferior.add(new JScrollPane(txtAreaResultadoArbol), BorderLayout.SOUTH);

         panel.add(chartPanel, BorderLayout.CENTER); // Gráfica arriba
         panel.add(panelMapa, BorderLayout.SOUTH); // Mapa abajo

         panel.setBorder(BorderFactory.createTitledBorder("Visualización"));
         return panel;
     }

    /**
     * Método que inicia la ejecución del algoritmo genético en un hilo separado.
     */
    private void ejecutarAlgoritmoEnSegundoPlano() {
        // Validar entradas antes de empezar
        try {
            int tamPoblacion = Integer.parseInt(txtPoblacion.getText());
            int maxGeneraciones = Integer.parseInt(txtGeneraciones.getText());
            double probCruce = Double.parseDouble(txtCruce.getText()) / 100.0;
            double probMutacion = Double.parseDouble(txtMutacion.getText()) / 100.0;
            double elitismo = Double.parseDouble(txtElitismo.getText()) / 100.0;
            int profInicial = Integer.parseInt(txtProfundidadInicial.getText());
            int profBloating = Integer.parseInt(txtProfundidadBloating.getText());

            if (tamPoblacion <= 0 || maxGeneraciones <= 0 || probCruce < 0 || probCruce > 1 ||
                probMutacion < 0 || probMutacion > 1 || elitismo < 0 || elitismo >= 1 || // Elitismo debe ser < 1
                profInicial < 2 || profBloating < profInicial) {
                throw new NumberFormatException("Parámetros inválidos.");
            }

            // Configurar AlgoritmoGenetico (usamos la instancia de la clase)
            algoritmo = new AlgoritmoGenetico(); // Crear nueva instancia para cada ejecución
            algoritmo.setProbCruce(probCruce);
            algoritmo.setProbMutacion(probMutacion);
            // Pasamos parámetros específicos a través del constructor o setters si los tuvieras
            algoritmo.setProfundidadInicial(profInicial);
            algoritmo.setMetodoInicializacion((String) cmbInicializacion.getSelectedItem());
            algoritmo.setProfMaxBloating(profBloating);
            // Otros parámetros como tamaño de torneo, etc., podrían necesitar setters

            // Limpiar estado anterior
            mejorSeries.clear();
            mediaSeries.clear();
            mejorAbsolutoSeries.clear();
            panelMapa.limpiar();
            txtAreaResultadoArbol.setText("");
            txtFitnessFinal.setText("");
            txtPasosFinales.setText("");
            btnEjecutar.setEnabled(false);
            btnEjecutar.setText("Ejecutando...");


            // Crear e iniciar el SwingWorker
            WorkerAlgoritmo worker = new WorkerAlgoritmo(
                tamPoblacion,
                maxGeneraciones,
                elitismo,
                (String) cmbSeleccion.getSelectedItem(),
                (String) cmbMutacion.getSelectedItem(), // Pasar método de mutación seleccionado
                algoritmo // Pasar el objeto algoritmo configurado
            );
            worker.execute();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error en los parámetros de entrada. Asegúrate de que son números válidos.\n" + ex.getMessage(),
                    "Error de Entrada", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método para actualizar la interfaz una vez que el SwingWorker ha terminado.
     * @param mejorIndividuo El mejor individuo encontrado por el algoritmo.
     */
    private void finalizarEjecucion(IndividuoHormiga mejorIndividuo) {
        btnEjecutar.setText("Ejecutar Algoritmo");
        btnEjecutar.setEnabled(true);

        if (mejorIndividuo != null) {
            double fitnessFinal = mejorIndividuo.evaluar(); // Re-evaluar por si acaso
            int pasosFinales = mejorIndividuo.getPasos();
            
            System.out.println("===== Ejecución Finalizada =====");
            System.out.println("Mejor Fitness Absoluto: " + fitnessFinal);
            System.out.println("Pasos Utilizados: " + pasosFinales);
            System.out.println("Árbol: " + mejorIndividuo.toString());
            System.out.println("================================");

            // Mostramos el árbol en el JTextArea
            txtAreaResultadoArbol.setText(mejorIndividuo.toString());
            txtFitnessFinal.setText(String.format("%.0f / 89", fitnessFinal));
            txtPasosFinales.setText(String.format("%d / 400", pasosFinales));
            
            // Dibujamos el mapa del mejor individuo
            mostrarMapa(mejorIndividuo);

        

        } else {
            System.out.println("Ejecución finalizada, pero no se encontró un mejor individuo válido.");
            txtPasosMejor.setText("N/A"); // Indicar que no aplica
            JOptionPane.showMessageDialog(this,
                "Ejecución completada, pero no se generó un resultado válido.",
                "Resultado", JOptionPane.WARNING_MESSAGE);
       }
    }


    /**
     * Método que actualiza el panel del mapa para mostrar la ruta del mejor individuo.
     * @param mejor El mejor individuo encontrado.
     */
    private void mostrarMapa(IndividuoHormiga mejor) {
         if (mejor == null) {
              panelMapa.limpiar();
              return;
         }
        boolean[][] comidaInicial = IndividuoHormiga.generarMapaComidaInicial();
        boolean[][] ruta = mejor.obtenerRutaRecorrida(); // Obtenemos la ruta
        panelMapa.setDatos(comidaInicial, ruta); // Pasamos los datos al panel para que se dibuje
    }

    // --- Clase Interna SwingWorker ---
    /**
     * Clase interna que ejecuta el algoritmo genético en un hilo de fondo
     * para no bloquear la interfaz gráfica (EDT).
     */
    private class WorkerAlgoritmo extends SwingWorker<IndividuoHormiga, Object[]> {
        private final int tamPoblacion;
        private final int maxGeneraciones;
        private final double elitismoPorc;
        private final String metodoSeleccion;
        private final String metodoMutacion; // Método de mutación específico a usar
        private final AlgoritmoGenetico agConfig; // Configuración del AG

        WorkerAlgoritmo(int tamPoblacion, int maxGeneraciones, double elitismoPorc,
                        String metodoSeleccion, String metodoMutacion, AlgoritmoGenetico agConfig) {
            this.tamPoblacion = tamPoblacion;
            this.maxGeneraciones = maxGeneraciones;
            this.elitismoPorc = elitismoPorc;
            this.metodoSeleccion = metodoSeleccion;
            this.metodoMutacion = metodoMutacion;
            this.agConfig = agConfig;
        }

        @Override
        protected IndividuoHormiga doInBackground() throws Exception {
            // --- Inicialización ---
            PoblacionHormiga poblacion = new PoblacionHormiga(tamPoblacion, agConfig);
            poblacion.iniciarGeneracion(); // Crea individuos iniciales
         // Se evalúa toda la población de una vez, cacheando los resultados y aplicando parquedad.
            poblacion.evaluarPoblacionConParquedad();
            
            // Obtenemos los datos de la generación 0 usando los métodos que acceden a la caché.
            IndividuoHormiga mejorGeneracion0 = poblacion.getMejorIndividuo();
            poblacion.actualizarAbsoluto(mejorGeneracion0);
            IndividuoHormiga mejorAbsoluto = poblacion.getAbsoluto();
            
            double mejorFitness0 = (mejorGeneracion0 != null) ? mejorGeneracion0.getComidaConsumida() : 0;
            double mejorAbsFitness0 = (mejorAbsoluto != null) ? mejorAbsoluto.getComidaConsumida() : 0;
            double fitnessMedio0 = poblacion.getFitnessMedio();

            // Publicamos los resultados iniciales para la gráfica
            publish(new Object[]{0, mejorFitness0, mejorAbsFitness0, fitnessMedio0});

            int generacionesSinMejora = 0;
            double fitnessAnterior = 0;
            
            // --- Bucle Evolutivo ---
            for (int i = 1; i < maxGeneraciones && !isCancelled(); i++) {
                // 1. Elitismo: Guardar los mejores N individuos de la población actual.
                // La población actual ya tiene su fitness cacheado de la iteración anterior.
                int numElite = (int) (tamPoblacion * elitismoPorc);
                List<IndividuoHormiga> elite = new ArrayList<>();
                if (numElite > 0 && poblacion.size() > 0) {
                    // --- CAMBIO: Usamos la caché de fitness bruto para ordenar y elegir la élite ---
                    poblacion.sort(Comparator.comparingDouble(poblacion.getFitnessBrutoCache()::get).reversed());
                    for (int k = 0; k < numElite && k < poblacion.size(); ++k) {
                        elite.add(poblacion.get(k).clone());
                    }
                }

                // 2. Selección: Crear una nueva población usando el fitness ajustado por parquedad.
                // El método 'seleccionarSegun' ya está modificado para usar la caché de fitness ajustado.
                PoblacionHormiga seleccionados = poblacion.seleccionarSegun(metodoSeleccion);

                // 3. Cruce
                seleccionados.cruzarPoblacion();

                // 4. Mutación
                seleccionados.mutarPoblacionEspecifica(metodoMutacion);

                // 5. Elitismo (Reemplazo): Reemplazar a los peores con la élite guardada
                if (numElite > 0 && !elite.isEmpty() && seleccionados.size() > 0) {
                    // --- CAMBIO: Ordenamos la nueva población por fitness bruto para encontrar a los peores ---
                    // (Nota: se podría ordenar por fitness ajustado también, es una decisión de diseño)
                    seleccionados.sort(Comparator.comparingDouble(IndividuoHormiga::evaluar)); // Evaluar aquí es menos eficiente pero asegura orden correcto post-mutación
                    for (int k = 0; k < numElite && k < elite.size() && k < seleccionados.size(); ++k) {
                        seleccionados.set(k, elite.get(k));
                    }
                }

                // --- CAMBIO CRÍTICO: EVALUACIÓN DE LA NUEVA POBLACIÓN ---
                // 6. Se evalúa la nueva población (hijos+mutados) y se cachean los resultados.
                //    Esta llamada es crucial para que la siguiente selección, elitismo y
                //    estadísticas funcionen correctamente y de forma eficiente.
                seleccionados.evaluarPoblacionConParquedad();

                // 7. Seguimiento y publicación de resultados
                IndividuoHormiga mejorGeneracion = seleccionados.getMejorIndividuo();
                double fitnessMedio = seleccionados.getFitnessMedio();
                seleccionados.actualizarAbsoluto(mejorGeneracion);
                mejorAbsoluto = seleccionados.getAbsoluto();

                if (mejorGeneracion != null && mejorAbsoluto != null) {
                    // Publicamos los datos usando la caché de fitness bruto
                    publish(new Object[]{i, seleccionados.getFitnessBrutoCache().get(mejorGeneracion), mejorAbsoluto.evaluar(), fitnessMedio});
                }

                // 8. Preparar para la siguiente generación
                poblacion = seleccionados;

                // 9. Condición de parada temprana
                if (mejorAbsoluto != null && mejorAbsoluto.getComidaConsumida() >= 89.0) {
                    System.out.println("¡Solución óptima encontrada en generación " + i + "!");
                    break;
                }
            }

            return poblacion.getAbsoluto(); // Devolvemos el mejor absoluto encontrado
        }

        @Override
        protected void process(List<Object[]> chunks) {
            // Este método se ejecuta en el EDT para actualizar la gráfica
            for (Object[] data : chunks) {
                try { // Añadimos try-catch por si acaso data no tiene los elementos esperados
                    int gen = (Integer) data[0];

                    // Hacemos cast a Number y obtenemos el valor double
                    double bestFit = ((Number) data[1]).doubleValue();
                    double absBestFit = ((Number) data[2]).doubleValue();

                    double avgFit = (Double) data[3]; // El promedio ya era Double

                    mejorSeries.addOrUpdate(gen, bestFit);
                    mejorAbsolutoSeries.addOrUpdate(gen, absBestFit);
                    mediaSeries.addOrUpdate(gen, avgFit);

                } catch (Exception e) {
                    // Manejar posible error si el array 'data' no tiene el formato esperado
                    System.err.println("Error procesando datos para la gráfica: " + e.getMessage());
                    e.printStackTrace(); // Imprimir stack trace para depuración
                }
            }
        }

        @Override
        protected void done() {
            // Este método se ejecuta en el EDT cuando doInBackground termina
            try {
                IndividuoHormiga finalBest = get(); // Obtenemos el resultado
                finalizarEjecucion(finalBest); // Llamamos al método que actualiza la GUI final
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(JFramePoblacionHormiga.this,
                        "Error durante la ejecución en segundo plano: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                finalizarEjecucion(null); // Asegura que la GUI se desbloquee
            }
        }
    }


     // --- Clase Interna PanelMapa ---
     /**
      * Panel personalizado para dibujar el mapa de la hormiga (tablero 32x32).
      */
     private class PanelMapa extends JPanel {
         private boolean[][] comidaInicial;
         private boolean[][] ruta;
         private final int CELL_SIZE = 12; // Tamaño de cada celda en píxeles
         private final int GRID_SIZE = 32;

         PanelMapa() {
             setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE + 1, GRID_SIZE * CELL_SIZE + 1));
             setBackground(Color.WHITE);
             setBorder(BorderFactory.createLineBorder(Color.BLACK));
         }

         /** Actualiza los datos a dibujar */
         public void setDatos(boolean[][] comida, boolean[][] ruta) {
             this.comidaInicial = comida;
             this.ruta = ruta;
             repaint(); // Solicita redibujar el panel con los nuevos datos
         }

         /** Limpia el mapa */
         public void limpiar() {
              this.comidaInicial = null;
              this.ruta = null;
              repaint();
         }

         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             Graphics2D g2d = (Graphics2D) g;

             if (comidaInicial == null) return; // No dibujar si no hay datos

             // Dibujar celdas y contenido
             for (int y = 0; y < GRID_SIZE; y++) {
                 for (int x = 0; x < GRID_SIZE; x++) {
                     int cellX = x * CELL_SIZE;
                     int cellY = y * CELL_SIZE;

                     // ¿Hay comida inicial aquí?
                     if (comidaInicial[y][x]) {
                         g2d.setColor(Color.GREEN.darker()); // Color para comida
                         g2d.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);
                     }

                     // ¿La hormiga pasó por aquí?
                     if (ruta != null && ruta[y][x]) {
                         g2d.setColor(new Color(0, 0, 150, 150)); // Color para la ruta (azul semitransparente)
                         // Dibujar un círculo o un punto en el centro
                         int centerX = cellX + CELL_SIZE / 2;
                         int centerY = cellY + CELL_SIZE / 2;
                         g2d.fillOval(centerX - CELL_SIZE / 4, centerY - CELL_SIZE / 4, CELL_SIZE / 2, CELL_SIZE / 2);
                     }

                      // Marcar inicio (0,0)
                      if (x==0 && y==0) {
                           g2d.setColor(Color.RED);
                           g2d.drawRect(cellX, cellY, CELL_SIZE-1, CELL_SIZE-1); // Recuadro rojo
                      }
                 }
             }

             // Dibujar rejilla (opcional, puede hacer más lento el dibujo)
             g2d.setColor(Color.LIGHT_GRAY);
             for (int i = 0; i <= GRID_SIZE; i++) {
                 g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE); // Verticales
                 g2d.drawLine(0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE, i * CELL_SIZE); // Horizontales
             }
         }
     }


}