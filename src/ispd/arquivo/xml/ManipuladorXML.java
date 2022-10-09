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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManipuladorXML {
    public static Document read(final File file, final String dtdPath)
            throws ParserConfigurationException, IOException, SAXException {

        final var factory = ManipuladorXML.makeFactory();
        return ManipuladorXML.makeDocBuilder(dtdPath, factory)
                .parse(file);
    }

    private static DocumentBuilderFactory makeFactory() {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        return factory;
    }

    private static DocumentBuilder makeDocBuilder(
            final String dtdPath, final DocumentBuilderFactory factory) throws ParserConfigurationException {
        final var builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new SubstituteEntityResolver(dtdPath));
        return builder;
    }

    /**
     * Este método sobrescreve ou cria arquivo xml
     */
    public static boolean write(
            final Document doc, final File outputFile,
            final String docTypeSystem, final Boolean omitXmlDecl) {
        try {
            final var source = new DOMSource(doc);
            final var result = new StreamResult(outputFile);

            ManipuladorXML.makeTransformer(docTypeSystem, omitXmlDecl)
                    .transform(source, result);

            return true;
        } catch (final TransformerException ex) {
            Logger.getLogger(ManipuladorXML.class.getName())
                    .log(Level.SEVERE, null, ex);

            return false;
        }
    }

    private static Transformer makeTransformer(
            final String docTypeSystem, final Boolean omitXmlDecl)
            throws TransformerConfigurationException {
        final var transformer = TransformerFactory.newInstance()
                .newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docTypeSystem);

        if (omitXmlDecl) {
            transformer.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION, "yes");
        }

        return transformer;
    }

    /**
     * Cria novo documento
     *
     * @return novo documento xml iconico
     */
    public static Document newDocument() {
        try {
            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .newDocument();
        } catch (final ParserConfigurationException ex) {
            Logger.getLogger(ManipuladorXML.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private static class SubstituteEntityResolver implements EntityResolver {
        private final InputSource substitute;

        private SubstituteEntityResolver(final String dtd) {
            this.substitute = new InputSource(
                    ManipuladorXML.class.getResourceAsStream("dtd/" + dtd));
        }

        @Override
        public InputSource resolveEntity(
                final String publicId, final String systemId) {
            return this.substitute;
        }
    }
}
