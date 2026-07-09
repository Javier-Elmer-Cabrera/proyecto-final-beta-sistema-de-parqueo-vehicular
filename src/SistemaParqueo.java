import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Controlador principal del sistema de parqueo.
 *
 * Orquesta las TRES estructuras de datos dinamicas implementadas
 * manualmente (sin el framework de colecciones de Java):
 *
 *  1. ColaVehiculos  - COLA FIFO con nodos enlazados: fila de entrada.
 *  2. TablaHashParqueo - TABLA HASH con encadenamiento: placa -> espacio.
 *  3. GrafoParqueo   - GRAFO con lista de adyacencia: mapa de la playa.
 *
 * Implementa las cuatro funcionalidades del proyecto: registrar entrada,
 * registrar salida con calculo de tarifa, consultar disponibilidad en
 * tiempo real y generar el reporte de ingresos.
 */
public class SistemaParqueo {

    /** 1. COLA manual: vehiculos esperando entrar (FIFO). */
    private ColaVehiculos colaEntrada;

    /** 2. TABLA HASH manual: busqueda O(1) de placa -> espacio asignado. */
    private TablaHashParqueo tablaPlacas;

    /** 3. GRAFO manual: distribucion fisica de los espacios de la playa. */
    private GrafoParqueo grafoPlaya;

    /** Acumulador financiero: crece con cada cobro (Funcionalidad 4). */
    private double ingresosTotales;

    /** Tarifa cobrada por cada minuto (simulado) de permanencia. */
    private final double TARIFA_POR_MINUTO = 0.50; // Tarifa de ejemplo

    /**
     * Crea el sistema e inicializa las tres estructuras vacias, luego
     * construye el grafo de la playa con la cantidad de espacios indicada.
     *
     * @param cantidadEspacios numero de cajones de la playa
     */
    public SistemaParqueo(int cantidadEspacios) {
        this.colaEntrada = new ColaVehiculos();
        this.tablaPlacas = new TablaHashParqueo();
        this.grafoPlaya = new GrafoParqueo();
        this.ingresosTotales = 0.0;
        inicializarGrafoPlaya(cantidadEspacios);
    }

    /**
     * Construye el grafo de la playa: crea un vertice por cada espacio y
     * conecta los consecutivos con aristas no dirigidas, simulando el
     * pasillo lineal de circulacion (1-2-3-...-N).
     *
     * @param cantidad numero de espacios a crear
     */
    private void inicializarGrafoPlaya(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            grafoPlaya.agregarEspacio(new EspacioParqueo(i));
        }
        // Conectar vertices consecutivos simulando la ruta de distribucion
        for (int i = 1; i < cantidad; i++) {
            grafoPlaya.conectar(i, i + 1);
        }
    }

    /**
     * [FUNCIONALIDAD 1] Registra la entrada de un vehiculo a la playa.
     *
     * Flujo: valida duplicados (Tabla Hash y Cola), encola el vehiculo
     * (FIFO) y luego intenta asignarle un espacio libre del grafo
     * llamando a procesarEntrada().
     *
     * @param placa placa unica del vehiculo que desea ingresar
     * @return mensaje descriptivo del resultado (exito, en cola o error)
     */
    public String registrarEntrada(String placa) {
        // VALIDACION 1 (TABLA HASH): busqueda O(1) por placa.
        // Si la placa ya tiene un espacio asignado, el vehiculo ya esta adentro.
        if (tablaPlacas.contiene(placa)) {
            String err = "Error: El vehiculo con placa " + placa + " ya se encuentra estacionado.";
            System.out.println(err);
            return err;
        }

        // VALIDACION 2 (COLA): recorrido de los nodos para evitar que la
        // misma placa espere dos veces en la fila de entrada.
        if (colaEntrada.contienePlaca(placa)) {
            String err = "Error: El vehiculo con placa " + placa + " ya se encuentra en la cola de espera.";
            System.out.println(err);
            return err;
        }

        // El constructor de Vehiculo captura la hora de entrada (para la tarifa).
        Vehiculo nuevoVehiculo = new Vehiculo(placa);

        // COLA (FIFO): el vehiculo se forma al final de la fila.
        colaEntrada.encolar(nuevoVehiculo);
        String msg = "Vehiculo " + placa + " agregado a la cola de entrada.";
        System.out.println(msg);

        // Intentar estacionarlo de inmediato si hay espacio disponible.
        String extra = procesarEntrada();
        if (extra != null) {
            msg += "\n" + extra;
        }
        return msg;
    }

    /**
     * Atiende al primer vehiculo de la cola (FIFO) asignandole el primer
     * espacio libre encontrado en el GRAFO de la playa.
     *
     * Aqui interactuan las 3 estructuras manuales:
     *  - GRAFO: buscarPrimerLibre() recorre los vertices buscando uno libre.
     *  - COLA:  desencolar() saca al vehiculo que llego primero.
     *  - HASH:  insertar() registra placa -> espacio para busqueda O(1) al salir.
     *
     * @return mensaje con el espacio asignado, o aviso de que no hay espacios
     */
    private String procesarEntrada() {
        if (colaEntrada.estaVacia()) return null; // nadie espera

        // GRAFO: busqueda secuencial del primer vertice (espacio) desocupado.
        EspacioParqueo espacioLibre = grafoPlaya.buscarPrimerLibre();

        if (espacioLibre != null) {
            // COLA: sale el primero en llegar (principio FIFO).
            // Metodo: desencolar() va al frente de la fila de espera, saca al
            // primer vehiculo que estaba aguardando y lo devuelve.
            Vehiculo vehiculo = colaEntrada.desencolar();

            // Se ocupa el vertice del grafo con el vehiculo.
            espacioLibre.asignarVehiculo(vehiculo);

            // TABLA HASH: registro placa -> espacio (clave -> valor).
            tablaPlacas.insertar(vehiculo.getPlaca(), espacioLibre);
            String msg = "Vehiculo " + vehiculo.getPlaca() + " estacionado en el Espacio #" + espacioLibre.getId();
            System.out.println(msg);
            return msg;
        } else {
            // Parqueo lleno: el vehiculo permanece formado en la cola.
            String msg = "No hay espacios disponibles en este momento. El vehiculo permanece en cola.";
            System.out.println(msg);
            return msg;
        }
    }

    /**
     * [FUNCIONALIDAD 2] Registra la salida de un vehiculo y calcula su tarifa.
     *
     * Flujo: localiza el vehiculo en tiempo O(1) usando la TABLA HASH,
     * calcula la tarifa segun el tiempo de permanencia, libera el vertice
     * del GRAFO, elimina la placa del hash y finalmente llama a
     * procesarEntrada() para que el primer vehiculo de la COLA (FIFO)
     * ocupe el espacio recien liberado.
     *
     * @param placa placa del vehiculo que desea retirarse
     * @return ticket de salida con tiempo y tarifa, o mensaje de error
     */
    public String registrarSalida(String placa) {
        // TABLA HASH: busqueda O(1) por placa, sin recorrer los espacios.
        // Si la placa no esta registrada, el vehiculo no esta en el parqueo.
        if (!tablaPlacas.contiene(placa)) {
            String err = "Error: El vehiculo con placa " + placa + " no se encuentra en el parqueo.";
            System.out.println(err);
            return err;
        }

        // TABLA HASH: buscar() recupera directamente el espacio (valor) de la placa (clave).
        EspacioParqueo espacio = tablaPlacas.buscar(placa);
        Vehiculo vehiculo = espacio.getVehiculoAsignado();

        // TARIFA: hora de salida - hora de entrada (capturada por el constructor de Vehiculo).
        LocalDateTime horaSalida = LocalDateTime.now();
        // Para demostraciones rapidas, cada segundo real se simula como 1 minuto.
        long minutos = Duration.between(vehiculo.getHoraEntrada(), horaSalida).toSeconds() + 1; // +1 seg minimo para cobrar algo
        double tarifaCalculada = minutos * TARIFA_POR_MINUTO;

        // Acumular el cobro en los ingresos totales (Funcionalidad 4: reportes).
        ingresosTotales += tarifaCalculada;

        // GRAFO: el vertice EspacioParqueo se marca como libre y suelta al vehiculo.
        espacio.liberar();
        // TABLA HASH: se elimina la placa; el vehiculo ya no esta "adentro".
        tablaPlacas.eliminar(placa);

        // TICKET: se arma el comprobante de salida con tiempo y monto a pagar.
        StringBuilder sb = new StringBuilder();
        sb.append("--- TICKET DE SALIDA ---\n");
        sb.append("Placa: ").append(placa).append("\n");
        sb.append("Tiempo de permanencia simulado: ").append(minutos).append(" minutos.\n");
        sb.append("Tarifa a pagar: S/. ").append(String.format("%.2f", tarifaCalculada)).append("\n");
        sb.append("------------------------");
        String ticket = sb.toString();

        System.out.println("\n" + ticket);

        // COLA (FIFO): el espacio liberado se ofrece automaticamente al
        // primer vehiculo que espera en la cola de entrada.
        procesarEntrada();

        return ticket;
    }

    /**
     * [FUNCIONALIDAD 3] Consulta la disponibilidad de espacios en tiempo real.
     *
     * Recorre el GRAFO de la playa vertice por vertice (por id, en orden
     * fisico 1..N) y reporta el estado de cada cajon: [LIBRE] u
     * [OCUPADO por placa].
     *
     * La GUI usa esta misma fuente de datos (getEspacio / getCantidadEspacios)
     * para pintar el mapa de la playa cada segundo mediante un Timer de Swing.
     */
    public void consultarDisponibilidad() {
        System.out.println("\n--- DISPONIBILIDAD EN TIEMPO REAL ---");

        // GRAFO: recorrido de todos los vertices EspacioParqueo de la playa.
        for (int id = 1; id <= grafoPlaya.obtenerCantidadVertices(); id++) {
            EspacioParqueo espacio = grafoPlaya.buscarEspacio(id);
            // Cada vertice conoce su propio estado (encapsulamiento).
            String estado = espacio.isOcupado() ? "[OCUPADO por " + espacio.getVehiculoAsignado().getPlaca() + "]" : "[LIBRE]";
            System.out.println("Espacio #" + espacio.getId() + ": " + estado);
        }
    }

    /**
     * [FUNCIONALIDAD 4] Genera el reporte de ingresos y estadisticas de uso.
     *
     * Consolida tres indicadores leyendo directamente las estructuras:
     *  - Ingresos totales: acumulador (ingresosTotales) que crece con cada
     *    cobro realizado en registrarSalida().
     *  - Vehiculos estacionados: tamano de la TABLA HASH (tablaPlacas),
     *    ya que cada placa registrada equivale a un espacio ocupado.
     *  - Vehiculos en espera: tamano de la COLA (colaEntrada).
     */
    public void generarReporteIngresos() {
        System.out.println("\n--- REPORTE DE INGRESOS ---");
        // Acumulador financiero: sumatoria de todas las tarifas cobradas.
        System.out.println("Ingresos totales acumulados: S/. " + String.format("%.2f", ingresosTotales));
        // TABLA HASH: su tamano indica cuantos autos estan adentro.
        System.out.println("Vehiculos actualmente estacionados: " + tablaPlacas.tamano());
        // COLA: cuantos autos siguen esperando un espacio libre.
        System.out.println("Vehiculos esperando en cola: " + colaEntrada.tamano());
    }

    // --- METODOS DE CONSULTA PARA LA INTERFAZ GRAFICA ---
    // La GUI no recibe las estructuras completas: consulta por estos metodos
    // (encapsulamiento), y cada consulta delega en la estructura manual.

    /** @return cantidad total de espacios (vertices del grafo) */
    public int getCantidadEspacios() {
        return grafoPlaya.obtenerCantidadVertices();
    }

    /**
     * Devuelve un espacio del grafo por su numero.
     *
     * @param id numero del espacio (1..N)
     * @return el espacio, o null si no existe
     */
    public EspacioParqueo getEspacio(int id) {
        return grafoPlaya.buscarEspacio(id);
    }

    /**
     * Verifica en la TABLA HASH si una placa esta estacionada.
     *
     * @param placa placa a verificar
     * @return true si el vehiculo esta dentro del parqueo
     */
    public boolean estaEstacionado(String placa) {
        return tablaPlacas.contiene(placa);
    }

    /**
     * Verifica en la COLA si una placa esta esperando entrar.
     *
     * @param placa placa a verificar
     * @return true si el vehiculo esta formado en la fila
     */
    public boolean estaEnCola(String placa) {
        return colaEntrada.contienePlaca(placa);
    }

    /**
     * Busca en la TABLA HASH el espacio asignado a una placa.
     *
     * @param placa placa del vehiculo estacionado
     * @return el espacio donde esta, o null si no esta estacionado
     */
    public EspacioParqueo buscarEspacioPorPlaca(String placa) {
        return tablaPlacas.buscar(placa);
    }

    /** @return numero de vehiculos estacionados (tamano de la tabla hash) */
    public int contarEstacionados() {
        return tablaPlacas.tamano();
    }

    /** @return numero de vehiculos esperando (tamano de la cola) */
    public int contarEnCola() {
        return colaEntrada.tamano();
    }

    /** @return sumatoria de todas las tarifas cobradas hasta el momento */
    public double getIngresosTotales() {
        return ingresosTotales;
    }

    /** @return tarifa por minuto (simulado) configurada en el sistema */
    public double getTarifaPorMinuto() {
        return TARIFA_POR_MINUTO;
    }

    /**
     * Calcula la tarifa acumulada de un vehiculo que sigue estacionado
     * (la GUI la muestra subiendo en tiempo real).
     *
     * @param v vehiculo estacionado
     * @return tarifa proyectada hasta este instante
     */
    public double calcularTarifaActual(Vehiculo v) {
        long segundos = Duration.between(v.getHoraEntrada(), LocalDateTime.now()).toSeconds();
        long minutosSimulados = segundos + 1; // +1 seg minimo como en registrarSalida
        return minutosSimulados * TARIFA_POR_MINUTO;
    }

    // --- METODOS DE DEMOSTRACION DE LAS ESTRUCTURAS (SUSTENTACION) ---

    /** @return representacion en texto del grafo (vertices y adyacencias) */
    public String mostrarGrafo() {
        return grafoPlaya.mostrar();
    }

    /** @return representacion en texto de la cola (placas en orden FIFO) */
    public String mostrarCola() {
        return colaEntrada.mostrar();
    }

    /** @return representacion en texto de la tabla hash (cubetas y cadenas) */
    public String mostrarTablaHash() {
        return tablaPlacas.mostrar();
    }

    /**
     * Recorrido BFS del grafo de la playa desde un espacio inicial.
     *
     * @param idInicio numero del espacio de partida
     * @return ids visitados en orden de anchura
     */
    public String recorridoBFS(int idInicio) {
        return grafoPlaya.recorridoBFS(idInicio);
    }
}
