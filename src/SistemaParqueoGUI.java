import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Map;

public class SistemaParqueoGUI extends JFrame {

    private final SistemaParqueo sistema;

    // Paleta de Colores Profesionales (Estilo Slate Dark)
    private static final Color COLOR_BG = new Color(15, 23, 42);          // Slate 900
    private static final Color COLOR_PANEL_BG = new Color(30, 41, 59);    // Slate 800
    private static final Color COLOR_CARD_BG = new Color(51, 65, 85);     // Slate 700
    private static final Color COLOR_SIDEBAR_BG = new Color(9, 15, 29);   // Slate 950
    private static final Color COLOR_ACCENT = new Color(37, 99, 235);     // Blue 600
    private static final Color COLOR_ACCENT_HOVER = new Color(29, 78, 216);// Blue 700
    private static final Color COLOR_TEXT_PRIMARY = new Color(248, 250, 252); // Slate 50
    private static final Color COLOR_TEXT_SECONDARY = new Color(148, 163, 184); // Slate 400
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);   // Emerald 500
    private static final Color COLOR_DANGER = new Color(239, 68, 68);     // Red 500
    private static final Color COLOR_WARNING = new Color(245, 158, 11);   // Amber 500
    private static final Color COLOR_BORDER = new Color(71, 85, 105);     // Slate 600

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // Componentes de navegación y layout
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarButton btnNavDashboard;
    private SidebarButton btnNavTabla;
    private SidebarButton btnNavReporte;

    // Componentes de la interfaz
    private JTextField txtPlaca;
    private JLabel lblValidation;
    private ModernButton btnRegistrar;
    private JPanel panelMapaGrid;
    private JLabel lblTotalDisponibles;
    private JLabel lblTotalCola;
    private JLabel lblTotalIngresos;
    
    // Historial amigable
    private JPanel panelHistorial;

    // Tarjetas de espacio (para no recrear componentes y evitar parpadeos)
    private final List<EspacioCard> cardsEspacios = new ArrayList<>();

    // Tabla
    private JTable tableVehiculos;
    private VehiculosTableModel tableModel;
    private JTextField txtBuscarPlaca;

    // Reporte
    private JLabel lblReporteGanancias;
    private JLabel lblReporteOcupados;
    private JLabel lblReporteCola;
    private JProgressBar progressOcupacion;

    public SistemaParqueoGUI(SistemaParqueo sistema) {
        this.sistema = sistema;

        // Configuración básica del JFrame
        setTitle("PARK-X // Sistema de Gestión de Parqueo Vehicular");
        setSize(1150, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // Construir la barra lateral (Sidebar)
        JPanel sidebar = crearSidebar();
        add(sidebar, BorderLayout.WEST);

        // Construir el contenedor principal (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_BG);

        // Añadir paneles al CardLayout
        contentPanel.add(crearPanelDashboard(), "DASHBOARD");
        contentPanel.add(crearPanelTabla(), "TABLA");
        contentPanel.add(crearPanelReportes(), "REPORTES");

        add(contentPanel, BorderLayout.CENTER);

        // Inicializar las tarjetas del mapa una sola vez
        inicializarMapaGrid();

        // Hilo de actualización automática en tiempo real (cada 1 segundo)
        Timer timer = new Timer(1000, e -> actualizarTiempoReal());
        timer.start();

        // Mostrar por defecto la sección de Dashboard
        navegarA("DASHBOARD", btnNavDashboard);
    }

    // --- CONSTRUCCIÓN DE LA BARRA LATERAL (SIDEBAR) ---
    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(COLOR_SIDEBAR_BG);
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));

        // Cabecera del Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 25));
        logoPanel.setOpaque(false);
        JLabel lblLogo = new JLabel("PARK-X");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogo.setForeground(COLOR_TEXT_PRIMARY);
        JLabel lblSubLogo = new JLabel("PARKING SYSTEM");
        lblSubLogo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSubLogo.setForeground(COLOR_ACCENT);

        JPanel logoTextContainer = new JPanel();
        logoTextContainer.setLayout(new BoxLayout(logoTextContainer, BoxLayout.Y_AXIS));
        logoTextContainer.setOpaque(false);
        logoTextContainer.add(lblLogo);
        logoTextContainer.add(lblSubLogo);
        logoPanel.add(logoTextContainer);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Lista de botones de navegación
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(new EmptyBorder(30, 10, 10, 10));

        btnNavDashboard = new SidebarButton("📊  Dashboard / Mapa");
        btnNavDashboard.addActionListener(e -> navegarA("DASHBOARD", btnNavDashboard));

        btnNavTabla = new SidebarButton("📋  Vista de Tabla");
        btnNavTabla.addActionListener(e -> navegarA("TABLA", btnNavTabla));

        btnNavReporte = new SidebarButton("💰  Reporte de Ingresos");
        btnNavReporte.addActionListener(e -> navegarA("REPORTES", btnNavReporte));

        navPanel.add(btnNavDashboard);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(btnNavTabla);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(btnNavReporte);

        sidebar.add(navPanel, BorderLayout.CENTER);

        // Pie de página del sidebar
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        footerPanel.setOpaque(false);
        JLabel lblFooter = new JLabel("v1.1.0 // Java Swing");
        lblFooter.setFont(FONT_SMALL);
        lblFooter.setForeground(COLOR_TEXT_SECONDARY);
        footerPanel.add(lblFooter);

        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    // --- CONSTRUCCIÓN DEL PANEL 1: DASHBOARD ---
    private JPanel crearPanelDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(15, 15));
        dashboard.setBackground(COLOR_BG);
        dashboard.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabecera superior con tarjetas de resumen rápido
        JPanel topSummaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        topSummaryPanel.setOpaque(false);
        topSummaryPanel.setPreferredSize(new Dimension(0, 90));

        lblTotalDisponibles = new JLabel("0 / 0", SwingConstants.CENTER);
        lblTotalCola = new JLabel("0", SwingConstants.CENTER);
        lblTotalIngresos = new JLabel("S/. 0.00", SwingConstants.CENTER);

        topSummaryPanel.add(crearCardResumen("ESPACIOS DISPONIBLES", lblTotalDisponibles, COLOR_SUCCESS));
        topSummaryPanel.add(crearCardResumen("VEHÍCULOS EN COLA", lblTotalCola, COLOR_WARNING));
        topSummaryPanel.add(crearCardResumen("INGRESOS ACUMULADOS", lblTotalIngresos, COLOR_ACCENT));

        dashboard.add(topSummaryPanel, BorderLayout.NORTH);

        // Cuerpo central: Dividido en Formulario (Izquierda) y Mapa del Parqueo (Derecha)
        JPanel bodyPanel = new JPanel(new BorderLayout(20, 0));
        bodyPanel.setOpaque(false);

        // 1. PANEL IZQUIERDO: Formularios, cola e historial amigable
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(320, 0));

        // 1.1 Tarjeta de Formulario para Registrar Entrada
        RoundedPanel formPanel = new RoundedPanel(15, COLOR_PANEL_BG);
        formPanel.setLayout(new BorderLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setMaximumSize(new Dimension(320, 210));
        formPanel.setPreferredSize(new Dimension(320, 210));

        JLabel lblFormTitle = new JLabel("Registrar Ingreso");
        lblFormTitle.setFont(FONT_SUBTITLE);
        lblFormTitle.setForeground(COLOR_TEXT_PRIMARY);
        formPanel.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridLayout(3, 1, 5, 5));
        formFields.setOpaque(false);
        formFields.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lblInput = new JLabel("Placa del Vehículo:");
        lblInput.setFont(FONT_BODY);
        lblInput.setForeground(COLOR_TEXT_SECONDARY);

        txtPlaca = new JTextField();
        txtPlaca.setBackground(COLOR_CARD_BG);
        txtPlaca.setForeground(COLOR_TEXT_PRIMARY);
        txtPlaca.setCaretColor(COLOR_TEXT_PRIMARY);
        txtPlaca.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtPlaca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Cambiar borde dinámicamente en foco
        txtPlaca.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPlaca.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_ACCENT, 2),
                    BorderFactory.createEmptyBorder(5, 11, 5, 11)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPlaca.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
        });

        lblValidation = new JLabel("Ingrese una placa válida");
        lblValidation.setFont(FONT_SMALL);
        lblValidation.setForeground(COLOR_TEXT_SECONDARY);

        formFields.add(lblInput);
        formFields.add(txtPlaca);
        formFields.add(lblValidation);
        formPanel.add(formFields, BorderLayout.CENTER);

        btnRegistrar = new ModernButton("Registrar Entrada", COLOR_ACCENT, COLOR_ACCENT_HOVER);
        btnRegistrar.setEnabled(false);
        btnRegistrar.addActionListener(e -> ejecutarEntradaEnHilo());
        formPanel.add(btnRegistrar, BorderLayout.SOUTH);

        // Escucha cambios en el campo de texto para validación dinámica
        txtPlaca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validarPlaca(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validarPlaca(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validarPlaca(); }
        });

        leftPanel.add(formPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 1.2 Tarjeta de Consola de Historial Amigable (Listado de Log Cards)
        RoundedPanel logPanel = new RoundedPanel(15, COLOR_PANEL_BG);
        logPanel.setLayout(new BorderLayout());
        logPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblLogTitle = new JLabel("Historial de Operaciones");
        lblLogTitle.setFont(FONT_SUBTITLE);
        lblLogTitle.setForeground(COLOR_TEXT_PRIMARY);
        logPanel.add(lblLogTitle, BorderLayout.NORTH);

        // Panel interno que almacena las tarjetas de historial
        panelHistorial = new JPanel();
        panelHistorial.setLayout(new BoxLayout(panelHistorial, BoxLayout.Y_AXIS));
        panelHistorial.setOpaque(false);

        // Envoltura para forzar el anclaje arriba de los elementos
        JPanel panelHistorialWrapper = new JPanel(new BorderLayout());
        panelHistorialWrapper.setOpaque(false);
        panelHistorialWrapper.add(panelHistorial, BorderLayout.NORTH);

        JScrollPane logScroll = new JScrollPane(panelHistorialWrapper);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        logScroll.setOpaque(false);
        logScroll.getViewport().setOpaque(false);
        logScroll.getVerticalScrollBar().setUnitIncrement(12);
        logPanel.add(logScroll, BorderLayout.CENTER);

        leftPanel.add(logPanel);

        bodyPanel.add(leftPanel, BorderLayout.WEST);

        // 2. PANEL DERECHO: Distribución del Mapa en Grafo
        RoundedPanel rightMapPanel = new RoundedPanel(15, COLOR_PANEL_BG);
        rightMapPanel.setLayout(new BorderLayout());
        rightMapPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel mapHeader = new JPanel(new BorderLayout());
        mapHeader.setOpaque(false);
        JLabel lblMapTitle = new JLabel("Mapa de Distribución (Grafo de Playa)");
        lblMapTitle.setFont(FONT_SUBTITLE);
        lblMapTitle.setForeground(COLOR_TEXT_PRIMARY);
        JLabel lblMapLegend = new JLabel("● Verde = Libre  |  ● Rojo/Auto = Ocupado  (Haz clic en un espacio)");
        lblMapLegend.setFont(FONT_SMALL);
        lblMapLegend.setForeground(COLOR_TEXT_SECONDARY);

        mapHeader.add(lblMapTitle, BorderLayout.NORTH);
        mapHeader.add(lblMapLegend, BorderLayout.SOUTH);
        rightMapPanel.add(mapHeader, BorderLayout.NORTH);

        // El mapa físico de espacios
        panelMapaGrid = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Dibujar conexiones del grafo (pasillos lineales)
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(71, 85, 105, 120)); // Gris slate suave translúcido
                g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Dibujar las líneas que unen los centros de los espacios secuenciales
                int n = panelMapaGrid.getComponentCount();
                for (int i = 0; i < n - 1; i++) {
                    Component c1 = panelMapaGrid.getComponent(i);
                    Component c2 = panelMapaGrid.getComponent(i + 1);
                    int x1 = c1.getX() + c1.getWidth() / 2;
                    int y1 = c1.getY() + c1.getHeight() / 2;
                    int x2 = c2.getX() + c2.getWidth() / 2;
                    int y2 = c2.getY() + c2.getHeight() / 2;
                    g2.drawLine(x1, y1, x2, y2);
                }
            }
        };
        panelMapaGrid.setOpaque(false);
        panelMapaGrid.setLayout(new FlowLayout(FlowLayout.CENTER, 35, 60)); // Layout para que fluyan los nodos
        panelMapaGrid.setBorder(new EmptyBorder(30, 10, 10, 10));

        rightMapPanel.add(panelMapaGrid, BorderLayout.CENTER);
        bodyPanel.add(rightMapPanel, BorderLayout.CENTER);

        dashboard.add(bodyPanel, BorderLayout.CENTER);

        return dashboard;
    }

    // --- CONSTRUCCIÓN DEL PANEL 2: TABLA DE DATOS ---
    private JPanel crearPanelTabla() {
        JPanel panelTabla = new JPanel(new BorderLayout(15, 15));
        panelTabla.setBackground(COLOR_BG);
        panelTabla.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Cabecera superior de la Tabla (Buscador y Título)
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Control de Vehículos Estacionados");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Barra de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar Placa:");
        lblBuscar.setFont(FONT_BODY);
        lblBuscar.setForeground(COLOR_TEXT_SECONDARY);

        txtBuscarPlaca = new JTextField();
        txtBuscarPlaca.setPreferredSize(new Dimension(180, 32));
        txtBuscarPlaca.setBackground(COLOR_PANEL_BG);
        txtBuscarPlaca.setForeground(COLOR_TEXT_PRIMARY);
        txtBuscarPlaca.setCaretColor(COLOR_TEXT_PRIMARY);
        txtBuscarPlaca.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        txtBuscarPlaca.setFont(FONT_BODY);

        // Filtrar tabla al escribir
        txtBuscarPlaca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrarTabla(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrarTabla(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrarTabla(); }
        });

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscarPlaca);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        panelTabla.add(headerPanel, BorderLayout.NORTH);

        // Estilización de la Tabla JTable
        tableModel = new VehiculosTableModel();
        tableVehiculos = new JTable(tableModel);
        tableVehiculos.setBackground(COLOR_PANEL_BG);
        tableVehiculos.setForeground(COLOR_TEXT_PRIMARY);
        tableVehiculos.setFont(FONT_BODY);
        tableVehiculos.setRowHeight(40);
        tableVehiculos.setSelectionBackground(COLOR_ACCENT);
        tableVehiculos.setSelectionForeground(COLOR_TEXT_PRIMARY);
        tableVehiculos.setGridColor(COLOR_BORDER);
        tableVehiculos.setShowVerticalLines(false);

        // Estilizar Cabecera de la Tabla
        JTableHeader tableHeader = tableVehiculos.getTableHeader();
        tableHeader.setBackground(COLOR_SIDEBAR_BG);
        tableHeader.setForeground(COLOR_TEXT_PRIMARY);
        tableHeader.setFont(FONT_BOLD);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setPreferredSize(new Dimension(0, 45));

        // Renderizado personalizado de celdas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableVehiculos.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scrollTable = new JScrollPane(tableVehiculos);
        scrollTable.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollTable.getViewport().setBackground(COLOR_BG);

        panelTabla.add(scrollTable, BorderLayout.CENTER);

        // Botones de acción en tabla
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bottomActions.setOpaque(false);

        ModernButton btnSalidaTabla = new ModernButton("Registrar Salida del Seleccionado", COLOR_DANGER, new Color(220, 38, 38));
        btnSalidaTabla.addActionListener(e -> {
            int selectedRow = tableVehiculos.getSelectedRow();
            if (selectedRow >= 0) {
                String placa = (String) tableModel.getValueAt(selectedRow, 1);
                ejecutarSalidaConConfirmacion(placa);
            } else {
                mostrarAlertaDialogo("Selección Requerida", "Por favor, seleccione un vehículo de la tabla.", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomActions.add(btnSalidaTabla);

        panelTabla.add(bottomActions, BorderLayout.SOUTH);

        return panelTabla;
    }

    // --- CONSTRUCCIÓN DEL PANEL 3: REPORTES ---
    private JPanel crearPanelReportes() {
        JPanel panelReportes = new JPanel(new BorderLayout(20, 20));
        panelReportes.setBackground(COLOR_BG);
        panelReportes.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblTitle = new JLabel("Estadísticas e Ingresos");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        panelReportes.add(lblTitle, BorderLayout.NORTH);

        // Panel central con indicadores financieros y de uso
        JPanel contentGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        contentGrid.setOpaque(false);

        // Indicador 1: Ganancias Totales
        RoundedPanel panelGanancias = new RoundedPanel(15, COLOR_PANEL_BG);
        panelGanancias.setLayout(new BorderLayout());
        panelGanancias.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel lblGTitle = new JLabel("GANANCIAS ACUMULADAS");
        lblGTitle.setFont(FONT_SMALL);
        lblGTitle.setForeground(COLOR_TEXT_SECONDARY);
        lblReporteGanancias = new JLabel("S/. 0.00");
        lblReporteGanancias.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblReporteGanancias.setForeground(COLOR_SUCCESS);
        panelGanancias.add(lblGTitle, BorderLayout.NORTH);
        panelGanancias.add(lblReporteGanancias, BorderLayout.CENTER);

        // Indicador 2: Tasa de Ocupación actual
        RoundedPanel panelOcupacion = new RoundedPanel(15, COLOR_PANEL_BG);
        panelOcupacion.setLayout(new BorderLayout());
        panelOcupacion.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel lblOTitle = new JLabel("TASA DE OCUPACIÓN");
        lblOTitle.setFont(FONT_SMALL);
        lblOTitle.setForeground(COLOR_TEXT_SECONDARY);

        lblReporteOcupados = new JLabel("0 / 0 espacios ocupados");
        lblReporteOcupados.setFont(FONT_SUBTITLE);
        lblReporteOcupados.setForeground(COLOR_TEXT_PRIMARY);

        progressOcupacion = new JProgressBar(0, 100);
        progressOcupacion.setStringPainted(true);
        progressOcupacion.setFont(FONT_BOLD);
        progressOcupacion.setForeground(COLOR_ACCENT);
        progressOcupacion.setBackground(COLOR_BG);
        progressOcupacion.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        progressOcupacion.setPreferredSize(new Dimension(0, 25));

        JPanel oContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        oContainer.setOpaque(false);
        oContainer.add(lblReporteOcupados);
        oContainer.add(progressOcupacion);

        panelOcupacion.add(lblOTitle, BorderLayout.NORTH);
        panelOcupacion.add(oContainer, BorderLayout.CENTER);

        // Indicador 3: Vehículos en Cola de espera
        RoundedPanel panelCola = new RoundedPanel(15, COLOR_PANEL_BG);
        panelCola.setLayout(new BorderLayout());
        panelCola.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel lblCTitle = new JLabel("COLA DE ESPERA ENTRADA/SALIDA");
        lblCTitle.setFont(FONT_SMALL);
        lblCTitle.setForeground(COLOR_TEXT_SECONDARY);
        lblReporteCola = new JLabel("0 vehículos en cola");
        lblReporteCola.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblReporteCola.setForeground(COLOR_WARNING);
        panelCola.add(lblCTitle, BorderLayout.NORTH);
        panelCola.add(lblReporteCola, BorderLayout.CENTER);

        // Indicador 4: Parámetros del sistema
        RoundedPanel panelInfo = new RoundedPanel(15, COLOR_PANEL_BG);
        panelInfo.setLayout(new BorderLayout());
        panelInfo.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel lblITitle = new JLabel("CONFIGURACIÓN Y TARIFAS");
        lblITitle.setFont(FONT_SMALL);
        lblITitle.setForeground(COLOR_TEXT_SECONDARY);

        JTextArea txtTarifaInfo = new JTextArea(
                "Tarifa por minuto: S/. " + String.format("%.2f", sistema.getTarifaPorMinuto()) + "\n" +
                "Monitoreo en tiempo real: Activo (1s)\n" +
                "Estructuras de datos utilizadas:\n" +
                "  - Grafo (Distribución de espacios y rutas)\n" +
                "  - Tabla Hash (Búsqueda veloz de placa)\n" +
                "  - Cola de prioridad/simple (Vehículos esperando)"
        );
        txtTarifaInfo.setFont(FONT_BODY);
        txtTarifaInfo.setForeground(COLOR_TEXT_PRIMARY);
        txtTarifaInfo.setOpaque(false);
        txtTarifaInfo.setEditable(false);
        txtTarifaInfo.setBorder(null);

        panelInfo.add(lblITitle, BorderLayout.NORTH);
        panelInfo.add(txtTarifaInfo, BorderLayout.CENTER);

        contentGrid.add(panelGanancias);
        contentGrid.add(panelOcupacion);
        contentGrid.add(panelCola);
        contentGrid.add(panelInfo);

        panelReportes.add(contentGrid, BorderLayout.CENTER);

        return panelReportes;
    }

    // --- LOGICA DE INICIALIZACION DE TARJETAS DE ESPACIOS (SOLO UNA VEZ) ---
    private void inicializarMapaGrid() {
        panelMapaGrid.removeAll();
        cardsEspacios.clear();

        List<EspacioParqueo> espacios = sistema.getGrafoEspacios();
        for (EspacioParqueo espacio : espacios) {
            EspacioCard card = new EspacioCard(espacio);
            cardsEspacios.add(card);
            panelMapaGrid.add(card);
        }

        panelMapaGrid.revalidate();
        panelMapaGrid.repaint();
    }

    // --- LÓGICA DE VALIDACIÓN DEL FORMULARIO DE PLACA ---
    private void validarPlaca() {
        String texto = txtPlaca.getText().trim();
        if (texto.isEmpty()) {
            lblValidation.setText("La placa no puede estar vacía");
            lblValidation.setForeground(COLOR_TEXT_SECONDARY);
            btnRegistrar.setEnabled(false);
            return;
        }

        // Validación de formato: De 3 a 8 caracteres alfanuméricos o guión
        if (!texto.matches("^[A-Za-zA-Z0-9-]{3,8}$")) {
            lblValidation.setText("Formato inválido (Ej: ABC-123 o ABC123)");
            lblValidation.setForeground(COLOR_DANGER);
            btnRegistrar.setEnabled(false);
            return;
        }

        // Validación de existencia en el parqueo
        String placaUpper = texto.toUpperCase();
        if (sistema.getMapaPlacas().containsKey(placaUpper)) {
            lblValidation.setText("¡El vehículo ya está estacionado!");
            lblValidation.setForeground(COLOR_DANGER);
            btnRegistrar.setEnabled(false);
            return;
        }

        // Validación de existencia en la cola de espera
        for (Vehiculo v : sistema.getColaEntradaSalida()) {
            if (v.getPlaca().equalsIgnoreCase(placaUpper)) {
                lblValidation.setText("¡El vehículo ya está en cola!");
                lblValidation.setForeground(COLOR_WARNING);
                btnRegistrar.setEnabled(false);
                return;
            }
        }

        lblValidation.setText("✓ Formato válido y disponible");
        lblValidation.setForeground(COLOR_SUCCESS);
        btnRegistrar.setEnabled(true);
    }

    // --- ACCIÓN: REGISTRAR ENTRADA (USANDO HILOS / THREADING) ---
    private void ejecutarEntradaEnHilo() {
        String placa = txtPlaca.getText().trim().toUpperCase();
        if (placa.isEmpty()) return;

        // Deshabilitar temporalmente la entrada para simular un proceso en segundo plano
        btnRegistrar.setEnabled(false);
        txtPlaca.setEnabled(false);
        lblValidation.setText("Procesando entrada...");
        lblValidation.setForeground(COLOR_WARNING);

        // Hilo de fondo (Threading) para no bloquear la GUI durante el proceso de simulación
        new Thread(() -> {
            try {
                // Simula latencia del hardware/red (sensor de barrera abriéndose, 1 segundo)
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}

            // Ejecutar la actualización en el Event Dispatch Thread (EDT) de Swing
            SwingUtilities.invokeLater(() -> {
                String resultMessage = sistema.registrarEntrada(placa);
                txtPlaca.setText("");
                txtPlaca.setEnabled(true);
                lblValidation.setText("Ingrese una placa válida");
                lblValidation.setForeground(COLOR_TEXT_SECONDARY);

                logActividad("ENTRADA", resultMessage);
                actualizarTodo();
                validarPlaca();
            });
        }).start();
    }

    // --- ACCIÓN: REGISTRAR SALIDA CON CONFIRMACIÓN ---
    private void ejecutarSalidaConConfirmacion(String placa) {
        if (placa == null || placa.isEmpty()) return;

        EspacioParqueo espacio = sistema.getMapaPlacas().get(placa);
        if (espacio == null) {
            mostrarAlertaDialogo("Error", "El vehículo con placa " + placa + " no se encuentra en el parqueo.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehiculo vehiculo = espacio.getVehiculoAsignado();
        double tarifa = sistema.calcularTarifaActual(vehiculo);
        
        // Calcular tiempo de permanencia hasta el momento
        long segundos = Duration.between(vehiculo.getHoraEntrada(), LocalDateTime.now()).toSeconds();
        long minutosSimulados = segundos + 1;

        // Diálogo elegante de confirmación de Ticket
        String mensaje = String.format(
                "¿Desea confirmar el cobro y la salida del vehículo?\n\n" +
                "Placa: %s\n" +
                "Espacio: #%d\n" +
                "Tiempo simulado: %d minutos\n" +
                "Tarifa a pagar: S/. %.2f\n\n" +
                "¿Liberar espacio ahora?",
                placa, espacio.getId(), minutosSimulados, tarifa
        );

        int confirm = JOptionPane.showConfirmDialog(
                this, 
                mensaje, 
                "TICKET DE SALIDA - CONFIRMACIÓN", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Registrar salida y capturar el ticket impreso
            String ticketOutput = sistema.registrarSalida(placa);
            logActividad("SALIDA", ticketOutput);
            
            // Diálogo de cobro exitoso
            JOptionPane.showMessageDialog(
                    this,
                    "Cobro procesado correctamente. Espacio #" + espacio.getId() + " liberado.",
                    "PAGO EXITOSO",
                    JOptionPane.INFORMATION_MESSAGE
            );

            actualizarTodo();
        }
    }

    // --- ACTUALIZACIÓN DE INTERFAZ GENERAL ---
    private void actualizarTodo() {
        actualizarIndicadores();
        actualizarMapaGrid();
        tableModel.fireTableDataChanged();
        filtrarTabla();
    }

    private void actualizarIndicadores() {
        int totalEspacios = sistema.getGrafoEspacios().size();
        int ocupados = sistema.getMapaPlacas().size();
        int disponibles = totalEspacios - ocupados;
        int cola = sistema.getColaEntradaSalida().size();
        double ingresos = sistema.getIngresosTotales();

        // Actualizar Dashboard
        lblTotalDisponibles.setText(disponibles + " / " + totalEspacios);
        lblTotalCola.setText(String.valueOf(cola));
        lblTotalIngresos.setText(String.format("S/. %.2f", ingresos));

        // Actualizar Reportes
        lblReporteGanancias.setText(String.format("S/. %.2f", ingresos));
        lblReporteOcupados.setText(ocupados + " / " + totalEspacios + " espacios ocupados");
        lblReporteCola.setText(cola + " vehículos en cola de espera");

        int percent = (totalEspacios > 0) ? (ocupados * 100 / totalEspacios) : 0;
        progressOcupacion.setValue(percent);
        progressOcupacion.setString(percent + "%");
    }

    private void actualizarMapaGrid() {
        // En lugar de remover y recrear, solo mandamos a actualizar el estado a cada tarjeta.
        // Esto previene parpadeos y mantiene los listeners y hover funcionando sin trabas.
        for (EspacioCard card : cardsEspacios) {
            card.actualizarEstado();
        }
    }

    // Actualiza los datos que cambian segundo a segundo sin bloquear
    private void actualizarTiempoReal() {
        // Actualiza el mapa para que cambien las tarifas en tiempo real
        actualizarTodo();
    }

    // --- BÚSQUEDA Y FILTRADO EN TABLA ---
    private void filtrarTabla() {
        String filterText = txtBuscarPlaca != null ? txtBuscarPlaca.getText().trim().toUpperCase() : "";
        tableModel.setFiltroPlaca(filterText);
    }

    // --- BITÁCORA EN PANTALLA AMIGABLE ---
    private void logActividad(String tipo, String mensaje) {
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        LogCard card = new LogCard(tipo, mensaje, hora);
        
        // Agregar espacio vertical si ya hay elementos
        if (panelHistorial.getComponentCount() > 0) {
            panelHistorial.add(Box.createRigidArea(new Dimension(0, 8)), 0);
        }
        
        panelHistorial.add(card, 0); // Insertar nueva tarjeta en la parte superior
        panelHistorial.revalidate();
        panelHistorial.repaint();
    }

    // --- ALERTA POPUP ELEGANT ---
    private void mostrarAlertaDialogo(String titulo, String msg, int tipo) {
        JOptionPane.showMessageDialog(this, msg, titulo, tipo);
    }

    // --- CONTROL DE NAVEGACIÓN ENTRE SECCIONES ---
    private void navegarA(String targetCard, SidebarButton activeBtn) {
        // Cambiar panel visual
        cardLayout.show(contentPanel, targetCard);

        // Deseleccionar botones anteriores
        btnNavDashboard.setSelectedState(false);
        btnNavTabla.setSelectedState(false);
        btnNavReporte.setSelectedState(false);

        // Activar el correspondiente
        activeBtn.setSelectedState(true);
        
        // Actualizar datos del panel que entra
        actualizarTodo();
    }

    // --- TARJETA DE RESUMEN (CABECERA) ---
    private JPanel crearCardResumen(String title, JLabel valueLabel, Color accentColor) {
        RoundedPanel card = new RoundedPanel(15, COLOR_PANEL_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_SMALL);
        lblTitle.setForeground(COLOR_TEXT_SECONDARY);
        card.add(lblTitle, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================================
    //                    COMPONENTES SWING PERSONALIZADOS
    // =========================================================================

    // Panel con esquinas redondeadas y color personalizable
    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bg) {
            this.cornerRadius = radius;
            this.backgroundColor = bg;
            setOpaque(false);
        }

        public void setBackgroundColor(Color bg) {
            this.backgroundColor = bg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }

    // Tarjeta del mapa representativa con dibujos vectoriales de autos y slots vacíos
    private class EspacioCard extends RoundedPanel {
        private final EspacioParqueo espacio;
        private boolean isHovered = false;
        private final JLabel lblInfo;

        public EspacioCard(EspacioParqueo espacio) {
            super(12, COLOR_CARD_BG);
            this.espacio = espacio;
            setPreferredSize(new Dimension(135, 140));
            setLayout(new BorderLayout(5, 5));
            setBorder(new EmptyBorder(10, 8, 10, 8));

            // Encabezado superior
            JLabel lblId = new JLabel("ESPACIO #" + espacio.getId());
            lblId.setFont(FONT_BOLD);
            lblId.setForeground(COLOR_TEXT_PRIMARY);
            lblId.setHorizontalAlignment(SwingConstants.CENTER);
            add(lblId, BorderLayout.NORTH);

            // Información inferior (Placa o estado)
            lblInfo = new JLabel();
            lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
            add(lblInfo, BorderLayout.SOUTH);

            // Listener de Mouse
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (espacio.isOcupado()) {
                        ejecutarSalidaConConfirmacion(espacio.getVehiculoAsignado().getPlaca());
                    } else {
                        txtPlaca.setText("");
                        txtPlaca.requestFocus();
                        txtPlaca.setBorder(BorderFactory.createLineBorder(COLOR_SUCCESS, 2));
                        lblValidation.setText("Ingrese placa para asignar al Espacio #" + espacio.getId());
                        lblValidation.setForeground(COLOR_SUCCESS);
                    }
                }
            });

            actualizarEstado();
        }

        public void actualizarEstado() {
            if (espacio.isOcupado()) {
                double tarifa = sistema.calcularTarifaActual(espacio.getVehiculoAsignado());
                lblInfo.setText(espacio.getVehiculoAsignado().getPlaca() + " (" + String.format("S/.%.1f", tarifa) + ")");
                lblInfo.setForeground(COLOR_TEXT_PRIMARY);
            } else {
                lblInfo.setText("DISPONIBLE");
                lblInfo.setForeground(COLOR_SUCCESS);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Cambiar color de fondo base dinámicamente antes de pintar
            if (espacio.isOcupado()) {
                setBackgroundColor(new Color(30, 41, 59)); // Slate 800
            } else {
                setBackgroundColor(new Color(15, 23, 42)); // Slate 900
            }

            super.paintComponent(g);
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dibujar detalles en medio del panel
            if (espacio.isOcupado()) {
                dibujarAuto(g2);
            } else {
                dibujarSlotVacio(g2);
            }

            // Pintar contorno en base a hover y ocupación
            if (isHovered) {
                g2.setColor(COLOR_ACCENT);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            } else {
                g2.setColor(espacio.isOcupado() ? COLOR_DANGER : COLOR_SUCCESS);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            }

            g2.dispose();
        }

        private void dibujarAuto(Graphics2D g2) {
            int cx = getWidth() / 2;
            int cy = getHeight() / 2 + 3;

            // Cuerpo del automóvil
            g2.setColor(COLOR_DANGER);
            g2.fillRoundRect(cx - 24, cy - 8, 48, 16, 6, 6);

            // Cabina superior
            g2.setColor(COLOR_TEXT_PRIMARY);
            g2.fillRoundRect(cx - 14, cy - 15, 28, 9, 4, 4);

            // Ventanas
            g2.setColor(COLOR_BG);
            g2.fillRect(cx - 10, cy - 13, 9, 6);
            g2.fillRect(cx + 1, cy - 13, 9, 6);

            // Ruedas
            g2.setColor(Color.BLACK);
            g2.fillOval(cx - 16, cy + 6, 8, 8);
            g2.fillOval(cx + 8, cy + 6, 8, 8);

            // Faros delanteros amarillos (apuntando a la derecha)
            g2.setColor(COLOR_WARNING);
            g2.fillRect(cx + 21, cy - 5, 3, 3);
            g2.fillRect(cx + 21, cy + 2, 3, 3);
        }

        private void dibujarSlotVacio(Graphics2D g2) {
            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;
            int cy = h / 2 + 3;

            // Líneas de parqueo translúcidas
            g2.setColor(new Color(148, 163, 184, 80));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
            g2.drawLine(cx - 28, cy - 22, cx - 28, cy + 22);
            g2.drawLine(cx + 28, cy - 22, cx + 28, cy + 22);

            // Dibujar P de Parking
            g2.setColor(new Color(16, 185, 129, 60));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            FontMetrics fm = g2.getFontMetrics();
            int px = cx - fm.stringWidth("P") / 2;
            int py = cy + fm.getAscent() / 2 - 2;
            g2.drawString("P", px, py);
        }
    }

    // Tarjeta del historial elegante y amigable
    private static class LogCard extends RoundedPanel {
        public LogCard(String tipo, String mensaje, String hora) {
            super(10, COLOR_PANEL_BG);
            setLayout(new BorderLayout(8, 4));
            
            // Determinar color de acento según tipo
            Color accentColor = COLOR_SUCCESS;
            String iconPrefix = "📥";
            if (tipo.equalsIgnoreCase("SALIDA")) {
                accentColor = COLOR_DANGER;
                iconPrefix = "📤";
            }

            // Borde izquierdo grueso con el color del tipo
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            // Dimensiones para evitar que se desborde en el BoxLayout
            setMaximumSize(new Dimension(300, 70));
            setPreferredSize(new Dimension(280, 65));

            // Título: [Icono] Entrada / Salida
            JLabel lblTipo = new JLabel(iconPrefix + " " + tipo);
            lblTipo.setFont(FONT_BOLD);
            lblTipo.setForeground(accentColor);

            // Hora a la derecha
            JLabel lblHora = new JLabel(hora);
            lblHora.setFont(FONT_SMALL);
            lblHora.setForeground(COLOR_TEXT_SECONDARY);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(lblTipo, BorderLayout.WEST);
            header.add(lblHora, BorderLayout.EAST);

            // Formatear mensaje a algo muy amigable y corto
            String mensajeCorto = mensaje;
            if (tipo.equalsIgnoreCase("SALIDA")) {
                String placa = "";
                String tarifa = "";
                for (String line : mensaje.split("\n")) {
                    if (line.startsWith("Placa:")) placa = line.replace("Placa:", "").trim();
                    if (line.startsWith("Tarifa a pagar:")) tarifa = line.replace("Tarifa a pagar:", "").trim();
                }
                mensajeCorto = "Vehículo " + placa + " retirado. Cobro: " + tarifa;
            } else {
                if (mensaje.contains("estacionado")) {
                    String[] parts = mensaje.split("\n");
                    mensajeCorto = parts[parts.length - 1]; // tomar el mensaje directo de asignacion
                }
            }

            JLabel lblMsg = new JLabel(mensajeCorto);
            lblMsg.setFont(FONT_SMALL);
            lblMsg.setForeground(COLOR_TEXT_PRIMARY);

            add(header, BorderLayout.NORTH);
            add(lblMsg, BorderLayout.CENTER);
        }
    }

    // Botón moderno con bordes redondeados y estados de Hover
    private static class ModernButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;
        private boolean isHovered = false;

        public ModernButton(String text, Color base, Color hover) {
            super(text);
            this.baseColor = base;
            this.hoverColor = hover;
            setFont(FONT_BOLD);
            setForeground(COLOR_TEXT_PRIMARY);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isEnabled()) {
                g2.setColor(COLOR_BORDER);
            } else if (isHovered) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(baseColor);
            }

            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            g2.dispose();

            super.paintComponent(g);
        }
    }

    // Botón de barra lateral estilizado con indicador vertical activo
    private static class SidebarButton extends JButton {
        private boolean isSelected = false;
        private boolean isHovered = false;

        public SidebarButton(String text) {
            super(text);
            setFont(FONT_BOLD);
            setForeground(COLOR_TEXT_SECONDARY);
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(200, 48));
            setMaximumSize(new Dimension(200, 48));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 20, 0, 0));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setSelectedState(boolean selected) {
            this.isSelected = selected;
            if (selected) {
                setForeground(COLOR_TEXT_PRIMARY);
            } else {
                setForeground(COLOR_TEXT_SECONDARY);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(COLOR_PANEL_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));

                // Indicador azul a la izquierda
                g2.setColor(COLOR_ACCENT);
                g2.fillRect(0, 8, 5, getHeight() - 16);
            } else if (isHovered) {
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================================
    //                          MODELO DE DATOS DE LA TABLA
    // =========================================================================
    private class VehiculosTableModel extends AbstractTableModel {
        private final String[] columnNames = {"# Espacio", "Placa de Vehículo", "Fecha/Hora Entrada", "Tiempo Transcurrido", "Tarifa Acumulada"};
        private final List<Object[]> dataList = new ArrayList<>();
        private String filtro = "";

        public void setFiltroPlaca(String filtro) {
            this.filtro = filtro.trim().toUpperCase();
            actualizarLista();
        }

        private void actualizarLista() {
            dataList.clear();
            List<EspacioParqueo> espacios = sistema.getGrafoEspacios();

            for (EspacioParqueo esp : espacios) {
                if (esp.isOcupado()) {
                    Vehiculo v = esp.getVehiculoAsignado();
                    String placa = v.getPlaca();

                    // Aplicar filtro si existe
                    if (!filtro.isEmpty() && !placa.contains(filtro)) {
                        continue;
                    }

                    // Calcular tiempos
                    LocalDateTime horaEntrada = v.getHoraEntrada();
                    long segundos = Duration.between(horaEntrada, LocalDateTime.now()).toSeconds();
                    long minutosSimulados = segundos + 1;
                    double tarifa = minutosSimulados * sistema.getTarifaPorMinuto();

                    String horaFormato = horaEntrada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                    String tiempoTexto = minutosSimulados + " minutos (simulados)";
                    String tarifaTexto = String.format("S/. %.2f", tarifa);

                    dataList.add(new Object[]{
                            "Espacio #" + esp.getId(),
                            placa,
                            horaFormato,
                            tiempoTexto,
                            tarifaTexto
                    });
                }
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            actualizarLista();
            return dataList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row < 0 || row >= dataList.size()) return null;
            return dataList.get(row)[col];
        }
    }
}
