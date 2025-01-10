/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.plaf.metal.MetalInternalFrameUI;

/**
 *
 * @author jesan
 */
public class Main {

    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private static JFrame mainFrame; 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createMainWindow);
    }

    public static void createMainWindow() {
        if (mainFrame == null) {
            mainFrame = new JFrame("Pantalla Principal");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
            mainFrame.setLocationRelativeTo(null);

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    ImageIcon imageIcon = new ImageIcon(getClass().getResource("/Resource/Fondo.png"));
                    Image img = imageIcon.getImage();
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                }
            };
            panel.setLayout(null);

            JButton btnLoadXML = new JButton("Cargar Fichero XML");
            btnLoadXML.setBounds(30, 100, 250, 50);
            styleButton(btnLoadXML);
            btnLoadXML.setBackground(Color.WHITE);
            btnLoadXML.addActionListener(e -> loadXMLFile());

            JButton btnMostrarbd = new JButton("Mostrar base datos");
            btnMostrarbd.setBounds(500, 100, 250, 50);
            styleButton(btnMostrarbd);
            btnMostrarbd.setBackground(Color.WHITE);
            btnMostrarbd.addActionListener(e -> showDatabaseView());

            panel.add(btnLoadXML);
            panel.add(btnMostrarbd);

            mainFrame.add(panel);
            mainFrame.setVisible(true);
        } else {
            mainFrame.setVisible(true); // Asegúrate de que la ventana principal está visible
        }
    }

    private static void styleButton(JButton button) {
        button.setFont(new Font("Verdana", Font.BOLD, 16));
        button.setForeground(Color.BLACK);
        button.setBackground(new Color(255, 255, 255, 200));
        button.setOpaque(true);
        button.setFocusPainted(false);
    }

    private static void loadXMLFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ficheros XML", "xml"));
        int result = fileChooser.showOpenDialog(mainFrame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Crear ventana de carga con barra de progreso
            JDialog loadingDialog = createLoadingDialog(mainFrame);

            // Ejecutar la carga del archivo en un hilo separado
            new Thread(() -> {
                try {
                    XMLHandler.readXMLAndInsertToDB(file.getAbsolutePath());
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose(); // Cerrar ventana de carga
                        JOptionPane.showMessageDialog(mainFrame, "Fichero XML cargado correctamente.");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose(); // Cerrar ventana de carga
                        JOptionPane.showMessageDialog(mainFrame, "Error al cargar el Fichero XML.");
                    });
                }
            }).start();

            loadingDialog.setVisible(true); // Mostrar ventana de carga
        }
    }

    private static JDialog createLoadingDialog(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Cargando...", true);
        dialog.setSize(450, 80);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());

        JLabel label = new JLabel("Por favor, espere mientras se carga el Fichero...", SwingConstants.CENTER);
        label.setFont(new Font("Verdana", Font.BOLD, 12));

        // Barra de progreso animada
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // Modo indeterminado (animado)
        progressBar.setString("Cargando...");
        progressBar.setStringPainted(true); // Mostrar texto en la barra
        
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new  BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        dialog.add(label, BorderLayout.NORTH);
        dialog.add(progressBar, BorderLayout.CENTER);

        return dialog;
    }

    private static void showDatabaseView() {
        mainFrame.setVisible(false); // Ocultar la ventana principal
        new DatabaseView(mainFrame); // Crear la ventana de base de datos y pasar la ventana principal
    }
}