import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SistemaParqueo {
    // 1. COLA: Entrada y salida de vehiculos
    private Queue<Vehiculo> colaEntradaSalida;
    
    // 2. TABLA HASH: Busqueda rapida por placa
    private Map<String, EspacioParqueo> mapaPlacas;
    
    // 3. GRAFO: Distribucion de espacios
    private List<EspacioParqueo> grafoEspacios;

    private double ingresosTotales;
    private final double TARIFA_POR_MINUTO = 0.50; // Tarifa de ejemplo

    public SistemaParqueo(int cantidadEspacios) {
        this.colaEntradaSalida = new LinkedList<>();
        this.mapaPlacas = new HashMap<>();
        this.grafoEspacios = new ArrayList<>();
        this.ingresosTotales = 0.0;
        inicializarGrafoPlaya(cantidadEspacios);
    }

    // Inicializa los espacios y los conecta como un grafo (por ejemplo, pasillos lineales)
    private void inicializarGrafoPlaya(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            grafoEspacios.add(new EspacioParqueo(i));
        }
        // Conectar nodos consecutivos simulando una ruta de distribucion
        for (int i = 0; i < cantidad - 1; i++) {
            grafoEspacios.get(i).conectarCon(grafoEspacios.get(i + 1));
        }
    }

    // [FUNCIONALIDAD 1]: Registrar entrada de vehiculo
    public String registrarEntrada(String placa) {
        // Validar si el vehiculo ya esta estacionado en la playa (Tabla Hash)
        if (mapaPlacas.containsKey(placa)) {
            String err = "Error: El vehiculo con placa " + placa + " ya se encuentra estacionado.";
            System.out.println(err);
            return err;
        }

        // Validar si el vehiculo ya esta esperando en la cola (Cola)
        for (Vehiculo v : colaEntradaSalida) {
            if (v.getPlaca().equalsIgnoreCase(placa)) {
                String err = "Error: El vehiculo con placa " + placa + " ya se encuentra en la cola de espera.";
                System.out.println(err);
                return err;
            }
        }

        Vehiculo nuevoVehiculo = new Vehiculo(placa);
        colaEntradaSalida.add(nuevoVehiculo);
        String msg = "Vehiculo " + placa + " agregado a la cola de entrada.";
        System.out.println(msg);

        // Procesar la cola inmediatamente para asignarle un espacio libre
        String extra = procesarEntrada();
        if (extra != null) {
            msg += "\n" + extra;
        }
        return msg;
    }

    private String procesarEntrada() {
        if (colaEntradaSalida.isEmpty()) return null;

        // Buscar el primer espacio disponible en el grafo
        EspacioParqueo espacioLibre = null;
        for (EspacioParqueo espacio : grafoEspacios) {
            if (!espacio.isOcupado()) {
                espacioLibre = espacio;
                break;
            }
        }

        if (espacioLibre != null) {
            Vehiculo vehiculo = colaEntradaSalida.poll(); // Desencola
            espacioLibre.asignarVehiculo(vehiculo);
            mapaPlacas.put(vehiculo.getPlaca(), espacioLibre); // Registrar en la Tabla Hash
            String msg = "Vehiculo " + vehiculo.getPlaca() + " estacionado en el Espacio #" + espacioLibre.getId();
            System.out.println(msg);
            return msg;
        } else {
            String msg = "No hay espacios disponibles en este momento. El vehiculo permanece en cola.";
            System.out.println(msg);
            return msg;
        }
    }

    // [FUNCIONALIDAD 2]: Registrar salida de vehiculo
    public String registrarSalida(String placa) {
        // Busqueda rapida usando la TABLA HASH
        if (!mapaPlacas.containsKey(placa)) {
            String err = "Error: El vehiculo con placa " + placa + " no se encuentra en el parqueo.";
            System.out.println(err);
            return err;
        }

        EspacioParqueo espacio = mapaPlacas.get(placa);
        Vehiculo vehiculo = espacio.getVehiculoAsignado();

        // Calcular Tarifa basandose en el tiempo transcurrido
        LocalDateTime horaSalida = LocalDateTime.now();
        // Simulamos minutos en lugar de horas para efectos de prueba rapidos
        long minutos = Duration.between(vehiculo.getHoraEntrada(), horaSalida).toSeconds() + 1; // +1 seg minimo para cobrar algo
        double tarifaCalculada = minutos * TARIFA_POR_MINUTO;
        
        ingresosTotales += tarifaCalculada;

        // Liberar estructuras
        espacio.liberar();
        mapaPlacas.remove(placa);

        StringBuilder sb = new StringBuilder();
        sb.append("--- TICKET DE SALIDA ---\n");
        sb.append("Placa: ").append(placa).append("\n");
        sb.append("Tiempo de permanencia simulado: ").append(minutos).append(" minutos.\n");
        sb.append("Tarifa a pagar: S/. ").append(String.format("%.2f", tarifaCalculada)).append("\n");
        sb.append("------------------------");
        String ticket = sb.toString();

        System.out.println("\n" + ticket);

        // Intentar meter a alguien de la cola si hay un espacio libre
        procesarEntrada();
        
        return ticket;
    }

    // [FUNCIONALIDAD 3]: Consultar disponibilidad en tiempo real
    public void consultarDisponibilidad() {
        System.out.println("\n--- DISPONIBILIDAD EN TIEMPO REAL ---");
        for (EspacioParqueo espacio : grafoEspacios) {
            String estado = espacio.isOcupado() ? "[OCUPADO por " + espacio.getVehiculoAsignado().getPlaca() + "]" : "[LIBRE]";
            System.out.println("Espacio #" + espacio.getId() + ": " + estado);
        }
    }

    // [FUNCIONALIDAD 4]: Generar reporte de ingresos
    public void generarReporteIngresos() {
        System.out.println("\n--- REPORTE DE INGRESOS ---");
        System.out.println("Ingresos totales acumulados: S/. " + String.format("%.2f", ingresosTotales));
        System.out.println("Vehiculos actualmente estacionados: " + mapaPlacas.size());
        System.out.println("Vehiculos esperando en cola: " + colaEntradaSalida.size());
    }

    // --- GETTERS Y METODOS AUXILIARES PARA LA INTERFAZ GRAFICA ---
    public Queue<Vehiculo> getColaEntradaSalida() {
        return colaEntradaSalida;
    }

    public Map<String, EspacioParqueo> getMapaPlacas() {
        return mapaPlacas;
    }

    public List<EspacioParqueo> getGrafoEspacios() {
        return grafoEspacios;
    }

    public double getIngresosTotales() {
        return ingresosTotales;
    }

    public double getTarifaPorMinuto() {
        return TARIFA_POR_MINUTO;
    }

    public double calcularTarifaActual(Vehiculo v) {
        long segundos = Duration.between(v.getHoraEntrada(), LocalDateTime.now()).toSeconds();
        long minutosSimulados = segundos + 1; // +1 seg minimo como en registrarSalida
        return minutosSimulados * TARIFA_POR_MINUTO;
    }
}