/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.arquivo;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 *
 * @author Diogo Tavares
 */
public class CargaXML {
    
    public static Document ler(File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        Document documento = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        //Apresentar Erros encontrados no parse
        /*builder.setErrorHandler(new ErrorHandler() {
        public void warning(SAXParseException e) throws SAXException {
        show("Warning", e);
        throw (e);
        }
        public void error(SAXParseException e) throws SAXException {
        show("Error", e);
        throw (e);
        }
        public void fatalError(SAXParseException e) throws SAXException {
        show("Fatal Error", e);
        throw (e);
        }
        private void show(String type, SAXParseException e) {
        System.err.println(type + ": " + e.getMessage());
        System.err.println("Line " + e.getLineNumber() + " Column " + e.getColumnNumber());
        System.err.println("System ID: " + e.getSystemId());
        }
        });*/
        //Indicar local do arquivo .dtd
        builder.setEntityResolver(new EntityResolver() {

            InputSource substitute = new InputSource(CargaXML.class.getResourceAsStream("iSPDcarga.dtd"));

            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return substitute;
            }
        });
        documento = builder.parse(xmlFile);
        return documento;
    }

    
    
    
}
