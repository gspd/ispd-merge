package ispd.arquivo.xml;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author denison
 */
public class ManipuladorXML {

    public static Document ler(final File xmlFile, final String dtd) throws ParserConfigurationException, IOException, SAXException {
        Document documento = null;
        final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final String dtdT = dtd;
        //Indicar local do arquivo .dtd
        builder.setEntityResolver(new EntityResolver() {
            final InputSource substitute =
                    new InputSource(ManipuladorXML.class.getResourceAsStream(
                            "dtd/" + dtdT));

            @Override
            public InputSource resolveEntity(final String publicId,
                                             final String systemId) throws SAXException, IOException {
                return this.substitute;
            }
        });
        documento = builder.parse(xmlFile);
        return documento;
    }

    /**
     * Este método sobrescreve ou cria arquivo xml
     */
    public static boolean escrever(final Document documento,
                                   final File arquivo,
                                   final String doc_system,
                                   final Boolean omitTagXML) {
        try {
            final TransformerFactory tf = TransformerFactory.newInstance();
            //tf.setAttribute("indent-number", new Integer(4));
            final Transformer transformer = tf.newTransformer();
            final DOMSource source = new DOMSource(documento);
            final StreamResult result = new StreamResult(arquivo);
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache" +
                                          ".org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                    doc_system);
            if (omitTagXML) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION
                        , "yes");
            }
            transformer.transform(source, result);
        } catch (final TransformerConfigurationException ex) {
            Logger.getLogger(ManipuladorXML.class.getName()).log(Level.SEVERE
                    , null, ex);
            return false;
        } catch (final TransformerException ex) {
            Logger.getLogger(ManipuladorXML.class.getName()).log(Level.SEVERE
                    , null, ex);
            return false;
        }
        return true;
    }

    /**
     * Cria novo documento
     *
     * @return novo documento xml iconico
     */
    public static Document novoDocumento() {
        try {
            final DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (final ParserConfigurationException ex) {
            Logger.getLogger(ManipuladorXML.class.getName()).log(Level.SEVERE
                    , null, ex);
            return null;
        }
    }

    public static Document[] clone(final Document doc, final int number) throws TransformerException {
        final Document[] documento = new Document[number];
        final TransformerFactory tfactory = TransformerFactory.newInstance();
        final Transformer tx = tfactory.newTransformer();
        final DOMSource source = new DOMSource(doc);
        final DOMResult result = new DOMResult();
        tx.transform(source, result);
        for (int i = 0; i < number; i++) {
            documento[i] = (Document) result.getNode();
        }
        return documento;
    }

    public static Document[] clone(final File file, final int number) throws ParserConfigurationException, IOException, SAXException {
        final Document[] documento = new Document[number];
        final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        //Indicar local do arquivo .dtd
        builder.setEntityResolver(new EntityResolver() {
            final InputSource substitute =
                    new InputSource(IconicoXML.class.getResourceAsStream(
                            "iSPD.dtd"));

            @Override
            public InputSource resolveEntity(final String publicId,
                                             final String systemId) throws SAXException, IOException {
                return this.substitute;
            }
        });
        for (int i = 0; i < number; i++) {
            documento[i] = builder.parse(file);
        }
        //inputStream.close();
        return documento;
    }

}
