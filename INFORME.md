# INFORME TÉCNICO Y FUNCIONAL

## Sistema de Parqueo Vehicular — PARK-X

| | |
|---|---|
| **Curso** | Estructuras de Datos y Algoritmos |
| **Lenguaje** | Java (JDK 21) |
| **Interfaz** | Java Swing |
| **Persistencia** | Ninguna — todos los datos en memoria (requisito del proyecto) |
| **Presentación** | [Ver en Canva](https://canva.link/i192k8s7bk6ku5q) |
| **Fecha** | Julio 2026 |

**Integrantes:** Alva Baltazar, Christian Enrique · Condori Medina, Edgar Adrian · Sanchez Uribe, Pedro · Calderon Rojas, Adolfo Pool · Ascona Francisco, Angel

---

## 1. Introducción y descripción del sistema

El presente informe detalla el diseño, la arquitectura y el funcionamiento del **Sistema de Parqueo Vehicular PARK-X**, desarrollado en Java para gestionar las operaciones cotidianas de una playa de estacionamiento comercial: control de entradas y salidas de vehículos, cálculo automático de tarifas por tiempo de permanencia, monitoreo de la ocupación de espacios en tiempo real y consolidación de estadísticas de ingresos.

La característica central del proyecto es que las tres estructuras de datos dinámicas exigidas — **Cola, Tabla Hash y Grafo** — están **implementadas manualmente con nodos enlazados (autorreferencias)**, escritas paso a paso en código propio, **sin utilizar el framework de colecciones de Java** (`ArrayList`, `LinkedList`, `HashMap`, `Queue`, etc.).

### Objetivos del proyecto

- **Optimización operativa:** controlar el acceso y salida de vehículos mediante una cola dinámica de atención por orden de llegada.
- **Eficiencia en la búsqueda:** garantizar tiempos de respuesta O(1) en la localización de vehículos por placa mediante direccionamiento hash con función de dispersión propia.
- **Mapeo de la playa:** representar la distribución física de los espacios con un grafo no dirigido de lista de adyacencia, con soporte de recorridos clásicos (BFS).
- **Control financiero:** acumular los ingresos por cobros y mostrar estadísticas de uso en tiempo real.

---

## 2. Consideraciones técnicas

| Tecnología / Requisito | Detalle de implementación | Propósito |
|---|---|---|
| Java (JDK 21) | Lenguaje del proyecto; API `java.time` para el cálculo de permanencia | Base del desarrollo |
| Estructuras dinámicas | **Implementación propia con autorreferencias** (nodos enlazados); prohibido `java.util` colecciones | Núcleo académico del proyecto |
| Ant (`build.xml`) | Proyecto compatible con NetBeans | Ciclo de build y portabilidad |
| Java Swing | Interfaz gráfica con Dashboard, JTable, diálogos y Timer | Interacción del operario |
| Sin persistencia | Todos los datos viven en el heap de la JVM | Requisito explícito del proyecto |

---

## 3. Implementación de las estructuras de datos dinámicas

Las tres estructuras siguen el mismo esquema: una clase nodo con autorreferencia (`siguiente`) y una clase estructura que administra los enlaces a mano, lanzando excepciones (`IllegalStateException`, `IllegalArgumentException`) ante operaciones inválidas.

### 3.1 Cola FIFO — `ColaVehiculos.java`

Estructura lineal **First-In, First-Out**: los vehículos se forman al final de la fila (`atras`) y se atienden por el frente (`frente`), como una fila real de autos.

```java
class NodoColaVehiculo {
    Vehiculo dato;
    NodoColaVehiculo siguiente;   // autorreferencia al siguiente de la fila
}
```

Operaciones principales (ambas **O(1)** gracias a los punteros a ambos extremos):

```java
public void encolar(Vehiculo vehiculo) {
    NodoColaVehiculo nuevo = new NodoColaVehiculo(vehiculo);
    if (frente == null) {          // cola vacia: unico nodo
        frente = nuevo;
        atras = nuevo;
    } else {                       // se engancha al final
        atras.siguiente = nuevo;
        atras = nuevo;
    }
    cantidad++;
}

public Vehiculo desencolar() throws IllegalStateException {
    if (estaVacia()) {
        throw new IllegalStateException("Cola vacia: no hay vehiculos que atender");
    }
    Vehiculo dato = frente.dato;
    frente = frente.siguiente;     // el frente avanza al siguiente nodo
    if (frente == null) atras = null;
    cantidad--;
    return dato;
}
```

**Uso en el sistema:** cuando el parqueo está lleno, los vehículos esperan encolados; al liberarse un espacio, `desencolar()` garantiza que lo ocupe el que llegó primero.

### 3.2 Tabla Hash — `TablaHashParqueo.java`

Estructura asociativa que empareja la **placa (clave)** con el **espacio asignado (valor)**. La función hash está escrita a mano (método polinomial base 31, módulo 53 — número primo para mejorar la dispersión):

```java
private int calcularIndice(String placa) {
    int hash = 0;
    for (int i = 0; i < placa.length(); i++) {
        hash = hash * 31 + placa.charAt(i);   // acumulacion polinomial
    }
    int indice = hash % CAPACIDAD;            // reduccion al rango de cubetas
    if (indice < 0) indice += CAPACIDAD;      // correccion de desbordamiento
    return indice;
}
```

Las **colisiones se resuelven por encadenamiento**: si dos placas caen en la misma cubeta, sus nodos quedan enlazados en cadena:

```java
public void insertar(String placa, EspacioParqueo espacio) {
    int indice = calcularIndice(placa);
    NodoHash nuevo = new NodoHash(placa, espacio);
    nuevo.siguiente = cubetas[indice];   // insercion al inicio de la cadena
    cubetas[indice] = nuevo;
    cantidad++;
}
```

El arreglo de cubetas es la única estructura de soporte, ya que una tabla hash **requiere un arreglo por definición**: el índice calculado direcciona una posición física directa, y de ahí proviene la búsqueda **O(1) promedio**.

**Uso en el sistema:** al registrar la salida, `buscar(placa)` recupera el espacio del vehículo al instante, sin recorrer el parqueo.

### 3.3 Grafo no dirigido — `GrafoParqueo.java`

Modela la distribución física de la playa: cada **vértice** es un espacio (`EspacioParqueo`) y cada **arista** una conexión del pasillo. Se implementa con **lista de adyacencia de nodos enlazados**:

```java
class NodoVertice {
    EspacioParqueo espacio;         // dato del vertice
    NodoVertice siguiente;          // siguiente vertice de la lista principal
    NodoAdyacente listaAdyacencia;  // cadena de vecinos (aristas)
}

class NodoAdyacente {
    int idDestino;                  // espacio con el que conecta
    NodoAdyacente siguiente;        // siguiente vecino de la cadena
}
```

Las aristas son **no dirigidas**: la conexión se registra en la lista de adyacencia de ambos vértices, porque en la playa se circula en los dos sentidos. Al inicializar, los espacios se conectan consecutivamente (1—2—3—4—5) simulando el pasillo de circulación.

El grafo incluye además un **recorrido en anchura (BFS)** implementado con cola y lista de visitados también manuales, que demuestra el soporte de algoritmos clásicos de grafos: partiendo de un espacio, visita primero sus vecinos directos y se expande por niveles a través de las listas de adyacencia.

**Uso en el sistema:** `buscarPrimerLibre()` recorre los vértices en orden físico para asignar el espacio al vehículo que entra; la GUI dibuja los vértices como tarjetas y las aristas como líneas del pasillo.

---

## 4. Diseño de Programación Orientada a Objetos

- **Encapsulamiento:** todos los atributos son `private` y el estado solo cambia por métodos públicos. `Vehiculo` no tiene setters (un vehículo no cambia de placa ni de hora de entrada); `EspacioParqueo` solo muta por `asignarVehiculo()` y `liberar()`; la GUI no recibe las estructuras completas sino que consulta por métodos (`estaEstacionado()`, `contarEnCola()`...).
- **Modularidad y alta cohesión:** cada clase tiene una única responsabilidad — `ColaVehiculos`, `TablaHashParqueo` y `GrafoParqueo` implementan las estructuras; `Vehiculo` y `EspacioParqueo` modelan las entidades; `SistemaParqueo` orquesta la lógica de negocio; `SistemaParqueoGUI` presenta; `Main` arranca.
- **Herencia y polimorfismo (en la GUI):** los componentes visuales extienden Swing — `SistemaParqueoGUI extends JFrame`, `RoundedPanel extends JPanel`, `ModernButton extends JButton`, `EspacioCard extends RoundedPanel` (herencia de dos niveles) — y sobrescriben `paintComponent()` para el dibujo personalizado.
- **Manejo de excepciones:** las estructuras lanzan `IllegalStateException` (cola/grafo vacío) e `IllegalArgumentException` (duplicados, vértices inexistentes), siguiendo el estilo del material del curso; la lógica de negocio previene los estados inválidos validando antes de operar, y la GUI captura `InterruptedException` en el hilo de fondo.

---

## 5. Guía funcional

### 5.1 Registrar entrada de vehículo

1. La GUI valida el formato de la placa en vivo (expresión regular) y la operación corre en un **hilo de fondo** para no bloquear la interfaz.
2. `registrarEntrada()` valida duplicados: primero en la **tabla hash** (¿ya está estacionado?, O(1)) y luego en la **cola** (¿ya está esperando?).
3. Se crea el `Vehiculo` (captura su hora de entrada) y se **encola** al final de la fila.
4. `procesarEntrada()` busca en el **grafo** el primer espacio libre: si existe, **desencola** al primero de la fila, lo estaciona e **inserta** placa → espacio en la tabla hash; si no, el vehículo permanece en cola.

### 5.2 Registrar salida y cálculo de tarifa

1. La GUI muestra un **diálogo de confirmación** con el ticket proyectado (placa, espacio, tiempo, monto).
2. `registrarSalida()` localiza el espacio con la **tabla hash** en O(1).
3. Calcula la tarifa: tiempo transcurrido desde la hora de entrada × S/. 0.50 por minuto (cada segundo real se simula como 1 minuto para agilizar la demostración).
4. Acumula el cobro en `ingresosTotales`, **libera el vértice** del grafo y **elimina** la placa de la tabla hash.
5. Emite el ticket y llama a `procesarEntrada()`: el espacio liberado se ofrece automáticamente al primero de la **cola (FIFO)**.

### 5.3 Consultar disponibilidad en tiempo real

Recorre los vértices del grafo reportando cada espacio como `[LIBRE]` u `[OCUPADO por placa]`. En la GUI, el **mapa de distribución** pinta cada vértice como tarjeta (verde/rojo con tarifa en vivo) y dibuja las aristas del pasillo; un `Timer` de Swing lo refresca **cada segundo**.

### 5.4 Reporte de ingresos

Consolida tres indicadores leyendo directamente las estructuras: ingresos totales (acumulador alimentado solo por los cobros de salida), vehículos estacionados (**tamaño de la tabla hash**) y vehículos en espera (**tamaño de la cola**). La GUI añade la tasa de ocupación con barra de progreso.

---

## 6. Interfaz gráfica y concurrencia

- **Navegación** por menú lateral: Dashboard/Mapa, Vista de Tabla y Reporte de Ingresos (CardLayout).
- **Validación en vivo** del formulario con `DocumentListener` y botón habilitado solo con placa válida.
- **JTable** con modelo propio (`AbstractTableModel`) y filtro de búsqueda por placa.
- **Diálogos** (`JOptionPane`) para confirmar el cobro antes de liberar el espacio.
- **Threading:** la entrada se procesa en un hilo de fondo (`new Thread`) y las actualizaciones visuales vuelven al **Event Dispatch Thread** con `SwingUtilities.invokeLater()`; un `Timer` de Swing actualiza tarifas e indicadores cada 1000 ms sin bloquear.

---

## 7. Pruebas realizadas

Se verificó el sistema con una batería de pruebas de consola y ejecución de la GUI:

| Prueba | Resultado |
|---|---|
| Construcción del grafo (5 vértices, aristas bidireccionales del pasillo) | ✓ Cada vértice lista sus vecinos correctos |
| Recorrido BFS desde el espacio 1 | ✓ Orden `1 2 3 4 5` |
| Llenado del parqueo (5 entradas consecutivas) | ✓ Asignación en orden físico 1..5 |
| Placa duplicada | ✓ Rechazada por la tabla hash |
| Parqueo lleno (6.° y 7.° vehículo) | ✓ Quedan en cola en orden de llegada |
| Dispersión de la tabla hash | ✓ Placas distribuidas en cubetas distintas |
| Salida con parqueo lleno | ✓ El espacio lo toma el **primero** de la cola (FIFO) |
| Salida de placa inexistente | ✓ Rechazada con mensaje de error |
| Reporte de ingresos | ✓ Acumulado igual a la suma de tickets |
| GUI con Timer en ejecución | ✓ Sin excepciones |

---

## 8. Conclusiones

- El sistema es **completamente operativo** y compila bajo JDK 21 sin errores.
- Las tres estructuras de datos dinámicas están **implementadas manualmente con autorreferencias**, sin colecciones de Java, cumpliendo el requerimiento central del curso: la cola con punteros frente/atrás, la tabla hash con función de dispersión propia y encadenamiento, y el grafo con lista de adyacencia y recorrido BFS.
- Las cuatro funcionalidades (entrada, salida con cobro, disponibilidad y reporte) están resueltas e integradas: la salida de un vehículo dispara automáticamente la atención de la cola, demostrando la interacción de las tres estructuras.
- El diseño respeta los principios de POO (encapsulamiento, modularidad, herencia en los componentes visuales) y el código está documentado con Javadoc.

### Recomendaciones de mejora

1. **Herencia en el modelo de dominio:** clase abstracta `Vehiculo` con subclases (`Automovil`, `Motocicleta`, `Camion`) y tarifas polimórficas por tipo.
2. **Histórico de cobros:** registrar cada salida (placa, fecha, monto) en una lista enlazada propia para reportes diarios/mensuales por rango de fechas.
3. **Asignación por cercanía:** agregar pesos a las aristas del grafo y un vértice "Entrada" para asignar el espacio libre más cercano usando BFS/Dijkstra, aprovechando las listas de adyacencia ya implementadas.
