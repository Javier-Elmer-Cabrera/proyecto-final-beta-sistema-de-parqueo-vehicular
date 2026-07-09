[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/5GYFwgZR)

# Sistema de Parqueo Vehicular

Proyecto final del curso **Estructuras de Datos y Algoritmos**. Sistema de gestión para una playa de estacionamiento que controla entradas, salidas, tarifas y disponibilidad de espacios en tiempo real, con interfaz gráfica en Java Swing.

## Integrantes

* Alva Baltazar, Christian Enrique
* Condori Medina, Edgar Adrian
* Sanchez Uribe, Pedro
* Calderon Rojas, Adolfo Pool
* Ascona Francisco, Angel
---

🎥 **Presentación del proyecto:** [Ver en Canva](https://canva.link/i192k8s7bk6ku5q)

---

## Descripción

PARK-X administra el ciclo completo de un vehículo en la playa de estacionamiento:

1. **Registrar entrada** — valida la placa, encola el vehículo y le asigna el primer espacio libre; si el parqueo está lleno, espera en cola.
2. **Registrar salida** — localiza el vehículo por su placa en tiempo O(1), calcula la tarifa por tiempo de permanencia y emite el ticket de cobro; el espacio liberado se asigna automáticamente al primero de la cola.
3. **Consultar disponibilidad** — mapa visual de la playa actualizado cada segundo (verde = libre, rojo = ocupado con placa y tarifa en vivo).
4. **Reporte de ingresos** — ganancias acumuladas, tasa de ocupación y vehículos en espera.

Todos los datos se manejan **en memoria** (sin base de datos ni archivos), según los requerimientos del proyecto.

## Estructuras de datos (implementación propia)

Las tres estructuras exigidas están **implementadas manualmente con nodos enlazados (autorreferencias)**, sin usar el framework de colecciones de Java (`ArrayList`, `LinkedList`, `HashMap`, etc.):

| Estructura | Archivo | Implementación | Uso en el sistema |
|---|---|---|---|
| **Cola (FIFO)** | `src/ColaVehiculos.java` | Nodos enlazados con punteros frente/atrás | Fila de vehículos esperando entrar; el primero en llegar es el primero en estacionar |
| **Tabla Hash** | `src/TablaHashParqueo.java` | Función hash polinomial propia + colisiones por encadenamiento | Búsqueda O(1) de vehículos por placa para cobros y salidas |
| **Grafo no dirigido** | `src/GrafoParqueo.java` | Lista de adyacencia con nodos enlazados + recorrido BFS | Modela la distribución física de los espacios y sus conexiones (pasillo) |

## Interfaz gráfica (Java Swing)

- Menú lateral con navegación entre Dashboard, Vista de Tabla y Reportes.
- Formularios con **validación en vivo** de la placa (expresión regular + duplicados).
- Visualización de vehículos en **JTable** con buscador por placa.
- **Diálogos de confirmación** antes de cobrar una salida.
- Actualización en tiempo real (Timer de 1 s) y **procesamiento en hilos** para no bloquear la interfaz.
- Tema oscuro profesional con componentes personalizados.

## Requisitos y ejecución

- **JDK 21** o superior
- IDE Java (NetBeans / IntelliJ IDEA) con **Ant** (incluye `build.xml`)

**Desde el IDE:** abrir el proyecto y ejecutar la clase `Main`.

**Desde terminal:**

```bash
cd src
javac *.java
java Main
```

El sistema inicia con 5 espacios de parqueo (configurable en `Main.java`) y una tarifa de S/. 0.50 por minuto simulado (1 segundo real = 1 minuto, para facilitar la demostración).

## Estructura del proyecto

```
├── src/
│   ├── Main.java               # Punto de entrada (lanza la GUI en el EDT)
│   ├── SistemaParqueo.java     # Controlador: orquesta las 3 estructuras
│   ├── ColaVehiculos.java      # COLA FIFO manual (nodos enlazados)
│   ├── TablaHashParqueo.java   # TABLA HASH manual (encadenamiento)
│   ├── GrafoParqueo.java       # GRAFO manual (lista de adyacencia + BFS)
│   ├── EspacioParqueo.java     # Entidad: cajón de estacionamiento (vértice)
│   ├── Vehiculo.java           # Entidad: vehículo (dato de las estructuras)
│   └── SistemaParqueoGUI.java  # Interfaz gráfica Swing
├── INFORME.md                  # Informe técnico del proyecto
├── build.xml                   # Build de Ant (NetBeans)
└── README.md
```
