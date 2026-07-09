import java.time.LocalDateTime;

/**
 * Representa un vehiculo que ingresa a la playa de estacionamiento.
 *
 * Es el DATO que viaja por las estructuras manuales: se encola en
 * ColaVehiculos al llegar, se asocia a un EspacioParqueo y su placa
 * es la clave de la TablaHashParqueo.
 *
 * Aplica encapsulamiento: atributos privados sin setters (un vehiculo
 * no cambia de placa ni de hora de entrada una vez registrado).
 */
public class Vehiculo {

    /** Placa unica del vehiculo: es la clave de la tabla hash. */
    private String placa;

    /** Momento exacto del registro; base del calculo de la tarifa. */
    private LocalDateTime horaEntrada;

    /**
     * Crea la entidad con su placa y captura automaticamente la hora
     * exacta de entrada, con la que luego se calcula la permanencia.
     *
     * @param placa placa del vehiculo que ingresa
     */
    public Vehiculo(String placa) {
        this.placa = placa;
        this.horaEntrada = LocalDateTime.now();
    }

    /** @return placa unica del vehiculo */
    public String getPlaca() { return placa; }

    /** @return hora exacta en que el vehiculo fue registrado */
    public LocalDateTime getHoraEntrada() { return horaEntrada; }
}
