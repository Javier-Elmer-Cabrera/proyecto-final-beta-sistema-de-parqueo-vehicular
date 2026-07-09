/**
 * Representa un cajon (espacio) fisico de la playa de estacionamiento.
 *
 * Es el DATO que envuelve cada vertice del grafo (GrafoParqueo): las
 * conexiones con los espacios vecinos ya no se guardan aqui, sino en
 * las listas de adyacencia del grafo, siguiendo el esquema del curso
 * (el vertice conoce su dato; el grafo conoce las aristas).
 *
 * Aplica encapsulamiento: sus atributos son privados y su estado solo
 * cambia por los metodos asignarVehiculo() y liberar().
 */
public class EspacioParqueo {

    /** Numero del espacio (1, 2, 3...); es la identidad del vertice. */
    private int id;

    /** true si actualmente hay un vehiculo estacionado en el cajon. */
    private boolean ocupado;

    /** Vehiculo estacionado en el cajon, o null si esta libre. */
    private Vehiculo vehiculoAsignado;

    /**
     * Crea un espacio libre, sin vehiculo asignado.
     *
     * @param id numero identificador del espacio
     */
    public EspacioParqueo(int id) {
        this.id = id;
        this.ocupado = false;
        this.vehiculoAsignado = null;
    }

    /** @return numero identificador del espacio */
    public int getId() { return id; }

    /** @return true si el espacio esta ocupado por un vehiculo */
    public boolean isOcupado() { return ocupado; }

    /** @return vehiculo estacionado, o null si el espacio esta libre */
    public Vehiculo getVehiculoAsignado() { return vehiculoAsignado; }

    /**
     * Marca el cajon como ocupado y guarda la referencia al vehiculo
     * (al procesar una entrada).
     *
     * @param v vehiculo que se estaciona en este espacio
     */
    public void asignarVehiculo(Vehiculo v) {
        this.vehiculoAsignado = v;
        this.ocupado = true;
    }

    /**
     * Libera el cajon: queda sin vehiculo y disponible para el siguiente
     * (al procesar una salida).
     */
    public void liberar() {
        this.vehiculoAsignado = null;
        this.ocupado = false;
    }
}
