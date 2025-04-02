package modelo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RutaCache {
    private final Map<String, List<int[]>> cache;

    public RutaCache() {
        this.cache = new HashMap<>();
    }

    public List<int[]> obtenerRuta(int[] inicio, int[] destino) {
        String clave = generarClave(inicio, destino);
        return cache.get(clave);
    }

    public void guardarRuta(int[] inicio, int[] destino, List<int[]> ruta) {
        String clave = generarClave(inicio, destino);
        cache.put(clave, ruta);
    }

    private String generarClave(int[] inicio, int[] destino) {
        return inicio[0] + "," + inicio[1] + "->" + destino[0] + "," + destino[1];
    }
}
