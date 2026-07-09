/**
 * Nodo que representa un vecino (arista) en la lista de adyacencia.
 *
 * Guarda el id del espacio destino y la referencia al siguiente vecino
 * del mismo vertice (autorreferencia).
 */
class NodoAdyacente {
    int idDestino;
    NodoAdyacente siguiente;

    NodoAdyacente(int idDestino) {
        this.idDestino = idDestino;
        this.siguiente = null;
    }
}

/**
 * Nodo que representa un vertice del grafo.
 *
 * Cada vertice envuelve un espacio de parqueo, enlaza con el siguiente
 * vertice de la lista principal y arranca su propia cadena de vecinos
 * (lista de adyacencia).
 */
class NodoVertice {
    EspacioParqueo espacio;
    NodoVertice siguiente;
    NodoAdyacente listaAdyacencia;

    NodoVertice(EspacioParqueo espacio) {
        this.espacio = espacio;
        this.siguiente = null;
        this.listaAdyacencia = null;
    }
}

/**
 * Nodo auxiliar de enteros usado por el recorrido BFS
 * (para la cola de pendientes y la lista de visitados).
 */
class NodoId {
    int id;
    NodoId siguiente;

    NodoId(int id) {
        this.id = id;
        this.siguiente = null;
    }
}

/**
 * GRAFO NO DIRIGIDO implementado manualmente con lista de adyacencia.
 *
 * Modela la distribucion fisica de la playa de estacionamiento:
 * - Cada VERTICE es un espacio de parqueo (EspacioParqueo).
 * - Cada ARISTA es una conexion fisica entre dos espacios (el pasillo).
 *
 * NO utiliza ArrayList, LinkedList ni ninguna coleccion de Java:
 * los vertices forman una lista enlazada principal y cada vertice
 * mantiene su propia cadena de nodos adyacentes (autorreferencias),
 * siguiendo el mismo esquema del material del curso.
 */
public class GrafoParqueo {

    /** Primer vertice de la lista enlazada principal. */
    private NodoVertice primerVertice;

    /** Ultimo vertice: permite insertar al final y conservar el orden 1..N. */
    private NodoVertice ultimoVertice;

    /** Cantidad de vertices (espacios) del grafo. */
    private int cantidadVertices;

    /** Crea un grafo vacio, sin vertices ni aristas. */
    public GrafoParqueo() {
        this.primerVertice = null;
        this.ultimoVertice = null;
        this.cantidadVertices = 0;
    }

    /**
     * Indica si el grafo no tiene vertices.
     *
     * @return true si la lista principal esta vacia
     */
    public boolean estaVacio() {
        return primerVertice == null;
    }

    /**
     * Cantidad de espacios (vertices) registrados en el grafo.
     *
     * @return numero de vertices
     */
    public int obtenerCantidadVertices() {
        return cantidadVertices;
    }

    /**
     * Busca un vertice recorriendo la lista principal nodo por nodo.
     *
     * @param id identificador del espacio buscado
     * @return el nodo vertice, o null si no existe
     */
    private NodoVertice buscarVertice(int id) {
        NodoVertice actual = primerVertice;
        while (actual != null) {
            if (actual.espacio.getId() == id) {
                return actual;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Verifica si existe un espacio con el id indicado.
     *
     * @param id identificador a verificar
     * @return true si el vertice existe
     */
    public boolean existeVertice(int id) {
        return buscarVertice(id) != null;
    }

    /**
     * Devuelve el espacio de parqueo asociado a un id.
     *
     * @param id identificador del espacio
     * @return el espacio, o null si no existe en el grafo
     */
    public EspacioParqueo buscarEspacio(int id) {
        NodoVertice v = buscarVertice(id);
        if (v == null) {
            return null;
        }
        return v.espacio;
    }

    /**
     * Agrega un espacio como nuevo vertice del grafo.
     *
     * Se inserta al FINAL de la lista principal para conservar el orden
     * fisico de los espacios (1, 2, 3...), de modo que las busquedas
     * secuenciales asignen primero los espacios de menor numero.
     *
     * @param espacio espacio de parqueo a incorporar como vertice
     * @throws IllegalArgumentException si ya existe un vertice con ese id
     */
    public void agregarEspacio(EspacioParqueo espacio)
            throws IllegalArgumentException {
        if (existeVertice(espacio.getId())) {
            throw new IllegalArgumentException(
                "El espacio #" + espacio.getId() + " ya existe en el grafo");
        }
        NodoVertice nuevo = new NodoVertice(espacio);
        if (primerVertice == null) {
            primerVertice = nuevo;
        } else {
            ultimoVertice.siguiente = nuevo;
        }
        ultimoVertice = nuevo;
        cantidadVertices++;
    }

    /**
     * Agrega una arista NO DIRIGIDA entre dos espacios: la conexion se
     * registra en la lista de adyacencia de ambos vertices, porque en la
     * playa se puede circular entre espacios en los dos sentidos.
     *
     * @param idOrigen  id del primer espacio
     * @param idDestino id del segundo espacio
     * @throws IllegalStateException    si el grafo esta vacio
     * @throws IllegalArgumentException si algun vertice no existe
     *                                  o la arista ya existe
     */
    public void conectar(int idOrigen, int idDestino)
            throws IllegalStateException, IllegalArgumentException {
        if (estaVacio()) {
            throw new IllegalStateException("Grafo vacio");
        }
        NodoVertice vOrigen = buscarVertice(idOrigen);
        NodoVertice vDestino = buscarVertice(idDestino);
        if (vOrigen == null || vDestino == null) {
            throw new IllegalArgumentException(
                "Espacio origen o destino no existe en el grafo");
        }
        if (sonAdyacentes(idOrigen, idDestino)) {
            throw new IllegalArgumentException(
                "La conexion entre #" + idOrigen + " y #" + idDestino + " ya existe");
        }
        // Insertar destino en la cadena de adyacencia del origen
        NodoAdyacente nuevoDestino = new NodoAdyacente(idDestino);
        nuevoDestino.siguiente = vOrigen.listaAdyacencia;
        vOrigen.listaAdyacencia = nuevoDestino;
        // Insertar origen en la cadena del destino (grafo no dirigido)
        NodoAdyacente nuevoOrigen = new NodoAdyacente(idOrigen);
        nuevoOrigen.siguiente = vDestino.listaAdyacencia;
        vDestino.listaAdyacencia = nuevoOrigen;
    }

    /**
     * Verifica si dos espacios estan conectados directamente,
     * recorriendo la cadena de adyacencia del origen.
     *
     * @param idOrigen  id del primer espacio
     * @param idDestino id del segundo espacio
     * @return true si existe la arista
     * @throws IllegalArgumentException si el origen no existe
     */
    public boolean sonAdyacentes(int idOrigen, int idDestino)
            throws IllegalArgumentException {
        NodoVertice vOrigen = buscarVertice(idOrigen);
        if (vOrigen == null) {
            throw new IllegalArgumentException(
                "El espacio #" + idOrigen + " no existe en el grafo");
        }
        NodoAdyacente actual = vOrigen.listaAdyacencia;
        while (actual != null) {
            if (actual.idDestino == idDestino) {
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    /**
     * Busca el primer espacio LIBRE recorriendo los vertices en su
     * orden fisico (1, 2, 3...). Es la operacion que usa el sistema
     * para asignar un espacio al vehiculo que entra.
     *
     * @return el primer espacio desocupado, o null si todo esta lleno
     */
    public EspacioParqueo buscarPrimerLibre() {
        NodoVertice actual = primerVertice;
        while (actual != null) {
            if (!actual.espacio.isOcupado()) {
                return actual.espacio;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Recorrido en anchura (BFS) desde un espacio, usando una cola y una
     * lista de visitados construidas a mano con nodos enlazados (NodoId).
     *
     * Demuestra que el grafo soporta algoritmos clasicos de recorrido:
     * visita primero los vecinos directos, luego los vecinos de estos,
     * expandiendose por niveles a traves de las listas de adyacencia.
     *
     * @param idInicio id del espacio desde donde parte el recorrido
     * @return los ids visitados en orden BFS, separados por espacios
     * @throws IllegalArgumentException si el espacio inicial no existe
     */
    public String recorridoBFS(int idInicio) throws IllegalArgumentException {
        if (buscarVertice(idInicio) == null) {
            throw new IllegalArgumentException(
                "El espacio #" + idInicio + " no existe en el grafo");
        }
        StringBuilder resultado = new StringBuilder();

        // Cola manual de pendientes (frente/atras) hecha con NodoId
        NodoId colaFrente = null;
        NodoId colaAtras = null;
        // Lista manual de visitados hecha con NodoId
        NodoId visitados = null;

        // Encolar el inicio y marcarlo visitado
        colaFrente = new NodoId(idInicio);
        colaAtras = colaFrente;
        visitados = new NodoId(idInicio);

        while (colaFrente != null) {
            // Desencolar el siguiente espacio a visitar
            int idActual = colaFrente.id;
            colaFrente = colaFrente.siguiente;
            if (colaFrente == null) {
                colaAtras = null;
            }
            resultado.append(idActual).append(" ");

            // Recorrer los vecinos del espacio actual
            NodoVertice v = buscarVertice(idActual);
            NodoAdyacente ady = v.listaAdyacencia;
            while (ady != null) {
                // Verificar si el vecino ya fue visitado
                boolean yaVisitado = false;
                NodoId nv = visitados;
                while (nv != null) {
                    if (nv.id == ady.idDestino) {
                        yaVisitado = true;
                        break;
                    }
                    nv = nv.siguiente;
                }
                if (!yaVisitado) {
                    // Marcar visitado (insercion al inicio de la lista)
                    NodoId marca = new NodoId(ady.idDestino);
                    marca.siguiente = visitados;
                    visitados = marca;
                    // Encolar al final de la cola de pendientes
                    NodoId pendiente = new NodoId(ady.idDestino);
                    if (colaFrente == null) {
                        colaFrente = pendiente;
                    } else {
                        colaAtras.siguiente = pendiente;
                    }
                    colaAtras = pendiente;
                }
                ady = ady.siguiente;
            }
        }
        return resultado.toString().trim();
    }

    /**
     * Representacion en texto del grafo: cada vertice con su estado
     * y su cadena de adyacencia. Util para la sustentacion.
     *
     * @return listado de vertices y conexiones, o aviso de grafo vacio
     */
    public String mostrar() {
        if (estaVacio()) {
            return "Grafo vacio";
        }
        StringBuilder sb = new StringBuilder();
        NodoVertice actual = primerVertice;
        while (actual != null) {
            sb.append("Espacio #").append(actual.espacio.getId());
            sb.append(actual.espacio.isOcupado() ? " [OCUPADO]" : " [LIBRE]");
            sb.append(" -> vecinos: ");
            NodoAdyacente ady = actual.listaAdyacencia;
            if (ady == null) {
                sb.append("(ninguno)");
            }
            while (ady != null) {
                sb.append("#").append(ady.idDestino);
                if (ady.siguiente != null) {
                    sb.append(", ");
                }
                ady = ady.siguiente;
            }
            sb.append("\n");
            actual = actual.siguiente;
        }
        return sb.toString();
    }
}
