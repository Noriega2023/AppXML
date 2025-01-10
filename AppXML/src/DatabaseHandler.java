/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 *
 * @author jesan
 */

/**
 * Clase para manejar la conexión y operaciones con la base de datos de contratos.
 */
public class DatabaseHandler    {
    private static final String URL = "jdbc:mysql://localhost:3306/contratos_junta_andalucia";
    private static final String USER = "root";
    private static final String PASSWORD = "Cordoba100";

    // Conexión a la base de datos
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método para convertir y validar la fecha y hora
    public static String formatFechaAdjudicacion(String fecha) {
        try {
            if (fecha == null || fecha.trim().isEmpty()) {
                return null;  // Si la fecha está vacía, retornamos null
            }

            // Intentamos convertir la fecha con fecha y hora en formato yyyy-MM-dd'T'HH:mm:ss.SSS
            if (fecha.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")) {
                System.out.println("Fecha en formato correcto (yyyy-MM-dd'T'HH:mm:ss.SSS): " + fecha);
                return fecha.replace("T", " ").substring(0, 19);  // Convertimos el formato T a un espacio y eliminamos los milisegundos
            } 
            // Intentamos convertir la fecha en formato yyyy-MM-dd
            else if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                System.out.println("Fecha en formato yyyy-MM-dd, añadiendo hora predeterminada.");
                // Agregamos la hora predeterminada (00:00:00) si solo es fecha
                return fecha + " 00:00:00"; 
            } 
            // Intentamos convertir la fecha de formato dd/MM/yyyy HH:mm:ss a yyyy-MM-dd HH:mm:ss
            else if (fecha.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
                System.out.println("Fecha en formato dd/MM/yyyy HH:mm:ss, convirtiendo.");
                SimpleDateFormat sdfIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdfOut.format(sdfIn.parse(fecha));  // Convertimos a yyyy-MM-dd HH:mm:ss
            } 
            // Intentamos convertir la fecha de formato dd/MM/yyyy a yyyy-MM-dd 00:00:00
            else if (fecha.matches("\\d{2}/\\d{2}/\\d{4}")) {
                System.out.println("Fecha en formato dd/MM/yyyy, convirtiendo a yyyy-MM-dd 00:00:00.");
                SimpleDateFormat sdfIn = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdfOut.format(sdfIn.parse(fecha)) + " 00:00:00";  // Agregamos la hora predeterminada
            } else {
                System.out.println("Formato de fecha no reconocido.");
                return null;  // Si el formato no es válido, retornamos null
            }
        } catch (ParseException e) {
            System.out.println("Error de ParseException: " + e.getMessage());
            e.printStackTrace();
            return null;  // Si ocurre un error en la conversión, retornamos null
        }
    }

    // Método para validar que el campo es un número válido (importe)
    public static Double validateImporte(String importe) {
        try {
            return importe != null && !importe.isEmpty() ? Double.valueOf(importe) : null;
        } catch (NumberFormatException e) {
            return null;  // Si no es un número válido, retornamos null
        }
    }

    // Insertar datos en la base de datos
    public static void insertData(String nif, String adjudicatario, String objetoGenerico,
                                  String objeto, String fechaAdjudicacion, String importe,
                                  String proveedores, String tipoContrato) throws SQLException {
        String query = "INSERT INTO contratos (NIF, Adjudicatario, ObjetoGenerico, Objeto, FechaAdjudicacion, Importe, ProveedoresConsultados, TipoContrato) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            // Validamos los campos antes de insertarlos
            stmt.setString(1, (nif != null && !nif.trim().isEmpty()) ? nif : null);
            stmt.setString(2, (adjudicatario != null && !adjudicatario.trim().isEmpty()) ? adjudicatario : null);
            stmt.setString(3, (objetoGenerico != null && !objetoGenerico.trim().isEmpty()) ? objetoGenerico : null);
            stmt.setString(4, (objeto != null && !objeto.trim().isEmpty()) ? objeto : null);

            // Convertimos la fecha a formato adecuado
            String fechaFormateada = formatFechaAdjudicacion(fechaAdjudicacion);
            System.out.println("Fecha formateada antes de insertar: " + fechaFormateada);  // Verificamos la fecha antes de insertar
            stmt.setString(5, fechaFormateada);  // Inserta la fecha formateada

            // Convertimos el importe a Double
            stmt.setObject(6, validateImporte(importe), Types.DOUBLE); 
            
            stmt.setString(7, (proveedores != null && !proveedores.trim().isEmpty()) ? proveedores : null);
            stmt.setString(8, (tipoContrato != null && !tipoContrato.trim().isEmpty()) ? tipoContrato : null);

            stmt.executeUpdate();
        }
    }

    // Obtener todos los datos de la base de datos (cambiamos el ResultSet a un tipo adecuado)
    public static ResultSet fetchData() throws SQLException {
        String query = "SELECT * FROM contratos";
        Connection conn = connect();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return stmt.executeQuery(query);
    }

    // Método para eliminar un registro de la base de datos utilizando el NIF
    public static void deleteData(String nif) throws SQLException {
        String query = "DELETE FROM contratos WHERE NIF = ?";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nif);
            stmt.executeUpdate();
        }
    }

    // Obtener el número total de registros en la base de datos
    public static int getTotalRows() throws SQLException {
        String query = "SELECT COUNT(*) FROM contratos";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt(1); // Obtener el conteo de filas
            }
        }
        return 0;
    }

    // Método para borrar todos los datos de la tabla
public static void deleteAllData() throws SQLException {
    String query = "DELETE FROM contratos";

    try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.executeUpdate();

        }
    }

    static void resetAutoIncrement() throws SQLDataException, SQLException {
        String sql = "ALTER TABLE contratos AUTO_INCREMENT = 1";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.executeUpdate();
        }
    }

    static boolean checkConnection() {
        try (Connection conn = connect()){
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexión a la base de datos exitosa.");
                return true;
            }
        }catch (SQLException e){
            System.out.println("Error de conexión: " + e.getMessage());
        }
        System.out.println("No hay conexión con la base de datos.");
        return false;
    }
}