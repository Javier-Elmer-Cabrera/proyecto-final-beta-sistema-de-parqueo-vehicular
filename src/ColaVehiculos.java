/**
 * Nodo de la cola de vehiculos (autorreferencia).
 *
 * Cada nodo guarda un vehiculo y la referencia al siguiente nodo
 * de la fila. Cuando "siguiente" es null, el nodo es el ultimo.
 */
class NodoColaVehiculo {
    Vehiculo dato;
    NodoColaVehiculo siguiente;

    NodoColaVehiculo(Vehiculo dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}

/**
 * COLA (Queue) implementada manualmente con nodos enlazados.
 *
 * Estructura FIFO (First-In, First-Out): los vehiculos se encolan por
 * el extremo "atras" y se desencolan por el extremo "frente", igual que
 * una fila real de autos esperando entrar al parqueo.
 *
 * NO utiliza ninguna clase del framework de colecciones de Java
 * (LinkedList, ArrayDeque, etc.): los enlaces entre nodos se manejan
 * a mano con autorreferencias.
 *
 * Complejidad: encolar O(1), desencolar O(1), busqueda de placa O(n).
 */
public class ColaVehiculos {

    /** Primer nodo de la fila: el proximo vehiculo en ser atendido. */
    private NodoColaVehiculo frente;

    /** Ultimo nodo de la fila: donde se forma el vehiculo que llega. */
    private NodoColaVehiculo atras;

    /** Contador de nodos para consultar el tamano en O(1). */
    private int cantidad;

    /** Crea una cola vacia (sin nodos). */
    public ColaVehiculos() {
        this.frente = null;
        this.atras = null;
        this.cantidad = 0;
    }

    /**
     * Indica si la cola no tiene vehiculos.
     *
     * @return true si el frente es null (no hay nodos)
     */
    public boolean estaVacia() {
        return frente == null;
    }

    /**
     * Cantidad de vehiculos actualmente formados en la cola.
     *
     * @return numero de nodos de la cola
     */
    public int tamano() {
        return cantidad;
    }

    /**
     * Encola un vehiculo al final de la fila (operacion FIFO de insercion).
     *
     * Si la cola esta vacia, el nuevo nodo es a la vez frente y atras.
     * Si no, se engancha al nodo "atras" y pasa a ser el nuevo ultimo.
     *
     * @param vehiculo vehiculo que se forma en la fila de entrada
     */
    public void encolar(Vehiculo vehiculo) {
        NodoColaVehiculo nuevo = new NodoColaVehiculo(vehiculo);
        if (frente == null) {
            // Cola vacia: el nuevo nodo es el unico de la fila
            frente = nuevo;
            atras = nuevo;
        } else {
            // Se engancha al final y se actualiza la referencia "atras"
            atras.siguiente = nuevo;
            atras = nuevo;
        }
        cantidad++;
    }

    /**
     * Desencola el vehiculo del frente (operacion FIFO de extraccion):
     * el primero que llego es el primero en salir de la fila.
     *
     * @return el vehiculo que estaba al frente de la cola
     * @throws IllegalStateException si la cola esta vacia
     */
    public Vehiculo desencolar() throws IllegalStateException {
        if (estaVacia()) {
            throw new IllegalStateException("Cola vacia: no hay vehiculos que atender");
        }
        Vehiculo dato = frente.dato;
        // El frente avanza al siguiente nodo; el anterior queda sin referencias
        frente = frente.siguiente;
        if (frente == null) {
            // Si ya no quedan nodos, "atras" tambien debe anularse
            atras = null;
        }
        cantidad--;
        return dato;
    }

    /**
     * Consulta (sin extraer) el vehiculo del frente de la fila.
     *
     * @return vehiculo al frente, o null si la cola esta vacia
     */
    public Vehiculo verFrente() {
        if (estaVacia()) {
            return null;
        }
        return frente.dato;
    }

    /**
     * Verifica si una placa ya se encuentra esperando en la cola,
     * recorriendo los nodos uno por uno desde el frente.
     *
     * @param placa placa a buscar (comparacion sin distinguir mayusculas)
     * @return true si algun vehiculo de la fila tiene esa placa
     */
    public boolean contienePlaca(String placa) {
        NodoColaVehiculo actual = frente;
        while (actual != null) {
            if (actual.dato.getPlaca().equalsIgnoreCase(placa)) {
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    /**
     * Representacion en texto de la fila, del frente hacia atras.
     * Util para depuracion y para la sustentacion.
     *
     * @return placas en orden de llegada, o aviso de cola vacia
     */
    public String mostrar() {
        if (estaVacia()) {
            return "(cola vacia)";
        }
        StringBuilder sb = new StringBuilder();
        NodoColaVehiculo actual = frente;
        while (actual != null) {
            sb.append(actual.dato.getPlaca());
            if (actual.siguiente != null) {
                sb.append(" <- ");
            }
            actual = actual.siguiente;
        }
        return sb.toString();
    }
}
