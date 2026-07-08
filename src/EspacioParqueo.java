import java.util.ArrayList;
import java.util.List;

public class EspacioParqueo {
    private int id;
    private boolean ocupado;
    private Vehiculo vehiculoAsignado;
    private List<EspacioParqueo> vecinos; // Conexiones del grafo para el mapa de la playa

    public EspacioParqueo(int id) {
        this.id = id;
        this.ocupado = false;
        this.vehiculoAsignado = null;
        this.vecinos = new ArrayList<>();
    }

    public int getId() { return id; }
    public boolean isOcupado() { return ocupado; }
    public List<EspacioParqueo> getVecinos() { return vecinos; }
    public Vehiculo getVehiculoAsignado() { return vehiculoAsignado; }

    public void asignarVehiculo(Vehiculo v) {
        this.vehiculoAsignado = v;
        this.ocupado = true;
    }

    public void liberar() {
        this.vehiculoAsignado = null;
        this.ocupado = false;
    }

    public void conectarCon(EspacioParqueo otro) {
        if (!vecinos.contains(otro)) {
            vecinos.add(otro);
            otro.vecinos.add(this); // Grafo no dirigido
        }
    }
}