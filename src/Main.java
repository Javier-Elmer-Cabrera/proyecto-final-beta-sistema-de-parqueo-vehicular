import javax.swing.SwingUtilities;

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