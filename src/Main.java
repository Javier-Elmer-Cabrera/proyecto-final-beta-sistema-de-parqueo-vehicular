import javax.swing.SwingUtilities;

/**
 * Punto de entrada del sistema PARK-X.
 *
 * Crea el controlador (SistemaParqueo) con sus tres estructuras de
 * datos manuales y lanza la interfaz grafica en el hilo de eventos
 * de Swing (EDT), como exige la especificacion de Swing.
 */
public class Main {
    public static void main(String[] args) {
        // Inicializamos el parqueo con 5 espacios para probar fácilmente topes y colas
        SistemaParqueo sistema = new SistemaParqueo(5);

        // Lanzar la interfaz gráfica en el hilo de eventos de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            SistemaParqueoGUI gui = new SistemaParqueoGUI(sistema);
            gui.setVisible(true);
        });
    }
}
