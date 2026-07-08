import java.time.LocalDateTime;

public class Vehiculo {
    private String placa;
    private LocalDateTime horaEntrada;

    public Vehiculo(String placa) {
        this.placa = placa;
        this.horaEntrada = LocalDateTime.now();
    }

    public String getPlaca() { return placa; }
    public LocalDateTime getHoraEntrada() { return horaEntrada; }
}