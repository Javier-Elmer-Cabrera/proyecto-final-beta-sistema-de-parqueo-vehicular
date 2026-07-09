/**
 * Nodo de la tabla hash (autorreferencia).
 *
 * Guarda un par clave-valor (placa - espacio asignado) y la referencia
 * al siguiente nodo de la misma cubeta. Ese enlace "siguiente" es el que
 * resuelve las colisiones por encadenamiento: si dos placas caen en la
 * misma cubeta, quedan enlazadas en cadena.
 */
class NodoHash {
    String placa;
    EspacioParqueo espacio;
    NodoHash siguiente;

    NodoHash(String placa, EspacioParqueo espacio) {
        this.placa = placa;
        this.espacio = espacio;
        this.siguiente = null;
    }
}

/**
 * TABLA HASH implementada manualmente con encadenamiento de nodos.
 *
 * Asocia la placa del vehiculo (clave) con el espacio de parqueo donde
 * esta estacionado (valor), logrando busqueda en tiempo O(1) promedio.
 *
 * NO utiliza HashMap ni ninguna clase del framework de colecciones:
 * - La funcion hash se calcula a mano (metodo polinomial base 31).
 * - Las colisiones se resuelven con listas enlazadas (encadenamiento).
 * - El arreglo de cubetas es la unica estructura de soporte, ya que
 *   una tabla hash requiere un arreglo por definicion (el indice
 *   calculado por la funcion hash direcciona una posicion fisica).
 */
public class TablaHashParqueo {

    /** Numero de cubetas. Se usa un numero primo para dispersar mejor. */
    private static final int CAPACIDAD = 53;

    /** Arreglo de cubetas; cada posicion es el inicio de una cadena de nodos. */
    private NodoHash[] cubetas;

    /** Cantidad de pares placa-espacio almacenados. */
    private int cantidad;

    /** Crea la tabla con todas las cubetas vacias (null). */
    public TablaHashParqueo() {
        this.cubetas = new NodoHash[CAPACIDAD];
        this.cantidad = 0;
    }

    /**
     * FUNCION HASH escrita a mano (metodo polinomial, base 31).
     *
     * Recorre los caracteres de la placa acumulando
     * hash = hash * 31 + caracter, y reduce el resultado al rango
     * [0, CAPACIDAD-1] con el operador modulo. El ajuste final corrige
     * los valores negativos que puede producir el desbordamiento de int.
     *
     * La misma placa siempre produce el mismo indice: por eso se puede
     * guardar y recuperar sin recorrer toda la tabla.
     *
     * @param placa clave a dispersar
     * @return indice de cubeta entre 0 y CAPACIDAD-1
     */
    private int calcularIndice(String placa) {
        int hash = 0;
        for (int i = 0; i < placa.length(); i++) {
            hash = hash * 31 + placa.charAt(i);
        }
        int indice = hash % CAPACIDAD;
        if (indice < 0) {
            indice += CAPACIDAD;
        }
        return indice;
    }

    /**
     * Indica si la tabla no tiene ningun vehiculo registrado.
     *
     * @return true si no hay pares almacenados
     */
    public boolean estaVacia() {
        return cantidad == 0;
    }

    /**
     * Cantidad de vehiculos registrados (equivale a espacios ocupados,
     * porque cada placa insertada corresponde a un espacio asignado).
     *
     * @return numero de pares placa-espacio en la tabla
     */
    public int tamano() {
        return cantidad;
    }

    /**
     * Inserta el par placa-espacio en la tabla.
     *
     * Calcula la cubeta con la funcion hash y agrega el nodo al inicio
     * de la cadena de esa cubeta. Si otra placa ya ocupaba la cubeta
     * (colision), ambas conviven enlazadas en la cadena.
     *
     * @param placa   clave: placa del vehiculo estacionado
     * @param espacio valor: espacio de parqueo que se le asigno
     * @throws IllegalArgumentException si la placa ya esta registrada
     */
    public void insertar(String placa, EspacioParqueo espacio)
            throws IllegalArgumentException {
        if (contiene(placa)) {
            throw new IllegalArgumentException(
                "La placa " + placa + " ya esta registrada en la tabla");
        }
        int indice = calcularIndice(placa);
        NodoHash nuevo = new NodoHash(placa, espacio);
        // Insercion al inicio de la cadena de la cubeta (O(1))
        nuevo.siguiente = cubetas[indice];
        cubetas[indice] = nuevo;
        cantidad++;
    }

    /**
     * Busca el espacio asignado a una placa.
     *
     * La funcion hash lleva directo a la cubeta correcta (O(1)); solo se
     * recorre la cadena de esa cubeta por si hubo colisiones.
     *
     * @param placa placa a buscar
     * @return el espacio donde esta estacionado, o null si no existe
     */
    public EspacioParqueo buscar(String placa) {
        int indice = calcularIndice(placa);
        NodoHash actual = cubetas[indice];
        while (actual != null) {
            if (actual.placa.equalsIgnoreCase(placa)) {
                return actual.espacio;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Verifica si una placa esta registrada en la tabla.
     *
     * @param placa placa a verificar
     * @return true si la placa existe
     */
    public boolean contiene(String placa) {
        return buscar(placa) != null;
    }

    /**
     * Elimina el par placa-espacio de la tabla (cuando el vehiculo sale).
     *
     * Se localiza la cubeta con la funcion hash y se desengancha el nodo
     * de la cadena re-enlazando el anterior con el siguiente.
     *
     * @param placa placa del vehiculo que se retira
     * @throws IllegalArgumentException si la placa no esta registrada
     */
    public void eliminar(String placa) throws IllegalArgumentException {
        int indice = calcularIndice(placa);
        NodoHash actual = cubetas[indice];
        NodoHash anterior = null;
        while (actual != null) {
            if (actual.placa.equalsIgnoreCase(placa)) {
                if (anterior == null) {
                    // El nodo era el primero de la cadena
                    cubetas[indice] = actual.siguiente;
                } else {
                    // Se "puentea" el nodo eliminado
                    anterior.siguiente = actual.siguiente;
                }
                cantidad--;
                return;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        throw new IllegalArgumentException(
            "La placa " + placa + " no esta registrada en la tabla");
    }

    /**
     * Representacion en texto del contenido de la tabla, cubeta por
     * cubeta. Util para demostrar la dispersion y las colisiones.
     *
     * @return listado de cubetas ocupadas con sus cadenas
     */
    public String mostrar() {
        if (estaVacia()) {
            return "(tabla vacia)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPACIDAD; i++) {
            if (cubetas[i] != null) {
                sb.append("Cubeta ").append(i).append(": ");
                NodoHash actual = cubetas[i];
                while (actual != null) {
                    sb.append(actual.placa)
                      .append(" -> Espacio #")
                      .append(actual.espacio.getId());
                    if (actual.siguiente != null) {
                        sb.append(" | ");
                    }
                    actual = actual.siguiente;
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
