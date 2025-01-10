/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.mysql.cj.ServerPreparedQueryTestcaseGenerator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author jesan
 */
public class DatabaseView {

    private JFrame frame;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JFrame parentFrame;

    public DatabaseView(JFrame parentFrame) {
        // Guardamos la referencia al JFrame principal, pero no la cerramos
        this.parentFrame = parentFrame;
        parentFrame.setVisible(false); // Ocultar la ventana principal

        frame = new JFrame("Base de Datos");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear un panel con un fondo de imagen
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Dibujar la imagen de fondo
                ImageIcon imageIcon = new ImageIcon(getClass().getResource("/Resource/juntaA.jpeg"));
                Image img = imageIcon.getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setOpaque(false);  // Asegurarse de que el panel de fondo sea transparente

        // Cargar datos de la base de datos
        Object[][] data = fetchDataFromDatabase();
        String[] columnNames = {"id", "NIF", "Adjudicatario", "Objeto Generico", "Objeto", "Fecha Adjudicacion", "Importe", "Proveedores Consultados"};

        // Modelo de tabla con los datos y las columnas
        tableModel = new DefaultTableModel(data, columnNames);
        table = new JTable(tableModel);

        // Hacer la tabla transparente
        table.setOpaque(false);  // Hacer que la tabla sea transparente
        table.setBackground(new Color(0, 0, 0, 0)); // Fondo transparente para la tabla

        // Configurar el color del texto en la tabla a negro
        table.setForeground(Color.BLACK);

        // Configurar la fuente de las celdas
        table.setFont(new Font("Arial", Font.BOLD, 12));

        // Configurar el renderizador para todas las celdas para que tengan texto en negro
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setForeground(Color.BLACK); // Establecer el color del texto a negro
                return component;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false); // Hacer que el JScrollPane también sea transparente
        scrollPane.getViewport().setOpaque(false); // Asegurarse de que la vista de la tabla también sea transparente

        // Crear un panel para los botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20)); // Centrado y con espacio entre los botones
        buttonPanel.setOpaque(false); // Hacer el fondo del panel transparente

        // Botón para generar archivo XML
        JButton generateXMLButton = new JButton("Generar XML");
        generateXMLButton.addActionListener(e -> generateXMLFile());

        // Botón para borrar filas
        JButton deleteRowsButton = new JButton("Borrar Filas");
        deleteRowsButton.addActionListener(e -> deleteRows());

        // Botón para volver al menú anterior
        JButton backButton = new JButton("Volver al Menú Principal");
        backButton.addActionListener(e -> goBackToMainMenu());

        // Botón para salir
        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(e -> {
            frame.dispose();
            System.exit(0);
        });

        // Añadir los botones al panel
        buttonPanel.add(generateXMLButton);
        buttonPanel.add(deleteRowsButton);  
        buttonPanel.add(backButton);
        buttonPanel.add(exitButton);

        // Añadir componentes al panel de fondo
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);
        backgroundPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Añadir el panel de fondo a la ventana
        frame.setContentPane(backgroundPanel);

        frame.setSize(800, 600); // Tamaño de la ventana de base de datos
        frame.setLocationRelativeTo(null); // Centrar la ventana
        frame.setVisible(true); // Mostrar la ventana
    }

        //Metodo para comprobar la conexion
    private Object[][] fetchDataFromDatabase() {
    // Comprobar si hay conexión con la base de datos
    if (!DatabaseHandler.checkConnection()) {
        JOptionPane.showMessageDialog(frame, "No hay conexión con la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        return new Object[0][0]; // Retornar tabla vacía si no hay conexión
    }

    try {
        ResultSet resultSet = DatabaseHandler.fetchData();
        resultSet.last(); // Moverse al final para obtener el número de filas
        int rowCount = resultSet.getRow();
        System.out.println(rowCount + " Filas en la base de datos.");
        resultSet.beforeFirst(); // Regresar al principio

        if (rowCount == 0) {
            JOptionPane.showMessageDialog(frame, "No hay datos en la base de datos.");
        }

        Object[][] data = new Object[rowCount][8]; // Asumiendo 8 columnas
        int rowIndex = 0;

        while (resultSet.next()) {
            data[rowIndex][0] = resultSet.getString("id");
            data[rowIndex][1] = resultSet.getString("NIF");
            data[rowIndex][2] = resultSet.getString("Adjudicatario");
            data[rowIndex][3] = resultSet.getString("ObjetoGenerico");
            data[rowIndex][4] = resultSet.getString("Objeto");
            data[rowIndex][5] = resultSet.getString("FechaAdjudicacion");
            data[rowIndex][6] = resultSet.getString("Importe");
            data[rowIndex][7] = resultSet.getString("ProveedoresConsultados");
            rowIndex++;
        }

        return data;
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(frame, "Error al cargar los datos desde la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        return new Object[0][0]; // Retornar tabla vacía si ocurre un error
    }
}

    private void generateXMLFile() {
    try {
        // Cargar los datos de la base de datos
        ResultSet resultSet = DatabaseHandler.fetchData();
        
        // Crear JFileChooser para permitir la selección de directorio y archivo
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona la ubicación para guardar el archivo XML");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Solo se permite seleccionar un archivo
        fileChooser.setAcceptAllFileFilterUsed(false); // Deshabilita la opción "Todos los archivos"
        
        // Establecer filtro para solo mostrar archivos XML
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ficheros XML", "xml"));
        
        // Establecer el nombre predeterminado del archivo
        fileChooser.setSelectedFile(new File("ContratosJunta.xml")); 
        
        // Abrir el cuadro de diálogo para seleccionar archivo
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            // Si el usuario no ha agregado la extensión .xml, se añade automáticamente
            if (!filePath.endsWith(".xml")) {
                filePath += ".xml";
            }
            
            // Generar el archivo XML en la ubicación seleccionada
            filePath = XMLHandler.generateXMLExcludingField(filePath, resultSet, "TipoContrato");

            // Mostrar mensaje con la ruta donde se guardó el archivo
            String successMessage = "Fichero XML generado correctamente.\nRuta: " + filePath;
            JOptionPane.showMessageDialog(frame, successMessage);
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(frame, "Error al generar el Fichero XML.");
    }
}

    // Método para borrar todas las filas de la tabla
    private void deleteRows() {
        try {
            // Borrar los datos de la base de datos (si se desea borrar también en la base de datos)
            DatabaseHandler.deleteAllData();
            
            // Reinicia los id
            DatabaseHandler.resetAutoIncrement();

            // Borrar las filas de la tabla
            tableModel.setRowCount(0);
            
            JOptionPane.showMessageDialog(frame, "Todas las filas borradas y condador dde ID reiniciado.");
            }catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al borrar las filas.");
            }

            // Recargar los datos de la base de datos después de borrarlos
            Object[][] newData = fetchDataFromDatabase();
            for (Object[] row : newData) {
                tableModel.addRow(row);
            }
        }

    private void goBackToMainMenu() {
        // Cierra la ventana actual y vuelve al menú principal
        frame.dispose(); // Cerrar la ventana de base de datos

        // Mostrar la ventana principal y hacerla visible
        parentFrame.setVisible(true); // Mostrar la ventana principal
    }
}