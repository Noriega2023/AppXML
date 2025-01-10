
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.ResultSet;

/**
 *
 * @author jesan
 */
public class XMLHandler {

    // Leer el archivo XML e insertar datos en la base de datos
    public static void readXMLAndInsertToDB(String xmlFile) throws Exception {
        File file = new File(xmlFile);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);

        // Obtener todas las filas (Row)
        NodeList rows = doc.getElementsByTagName("Row");

        // Procesar filas (ignoramos la primera fila porque es el encabezado)
        for (int i = 1; i < rows.getLength(); i++) {
            Node row = rows.item(i);
            if (row.getNodeType() == Node.ELEMENT_NODE) {
                Element rowElement = (Element) row;
                NodeList cells = rowElement.getElementsByTagName("Cell");

                // Leer valores de las celdas
                String nif = getCellValue(cells, 0);
                String adjudicatario = getCellValue(cells, 1);
                String objetoGenerico = getCellValue(cells, 2);
                String objeto = getCellValue(cells, 3);
                String fechaAdjudicacion = getCellValue(cells, 4);
                String importe = getCellValue(cells, 5);
                String proveedores = getCellValue(cells, 6);
                String tipoContrato = getCellValue(cells, 7);

                // Insertar datos en la base de datos
                DatabaseHandler.insertData(nif, adjudicatario, objetoGenerico, objeto,
                                           fechaAdjudicacion, importe, proveedores, tipoContrato);
            }
        }
    }

    // Generar un nuevo archivo XML excluyendo un campo específico (como TipoContrato)
    public static String generateXMLExcludingField(String outputFile, ResultSet resultSet, String excludeField)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Crear la raíz del documento
        Element root = doc.createElement("Contratos");
        doc.appendChild(root);

        // Recorrer los datos obtenidos de la base de datos
        while (resultSet.next()) {
            Element contrato = doc.createElement("Contrato");
            root.appendChild(contrato);

            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = resultSet.getMetaData().getColumnName(i);

                // Excluir el campo 'TipoContrato' al generar el XML
                if (!columnName.equalsIgnoreCase(excludeField)) {
                    Element field = doc.createElement(columnName);
                    field.appendChild(doc.createTextNode(resultSet.getString(i)));
                    contrato.appendChild(field);
                }
            }
        }

        // Escribir el documento XML en un archivo
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(outputFile));

        // Este archivo se guardará en el directorio donde se ejecute el programa.
        transformer.transform(source, result);

        // Devolver la ruta del archivo generado
        return new File(outputFile).getAbsolutePath();
    }

    // Método auxiliar para obtener el valor de una celda por índice
    private static String getCellValue(NodeList cells, int index) {
        if (index < cells.getLength()) {
            Element cell = (Element) cells.item(index);
            NodeList data = cell.getElementsByTagName("Data");
            if (data.getLength() > 0) {
                return data.item(0).getTextContent();
            }
        }
        return null;
    }
}