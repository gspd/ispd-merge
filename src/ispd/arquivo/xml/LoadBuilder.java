package ispd.arquivo.xml;

import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaList;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaTrace;
import ispd.motor.carga.GerarCarga;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoadBuilder {
    static GerarCarga buildLoad(final Document modelo) {
        final var d = new WrappedDocument(modelo);

        final NodeList cargas = modelo.getElementsByTagName("load");

        if (d.hasNoLoads()) {
            return null;
        }


        final Element cargaAux = (Element) cargas.item(0);
        GerarCarga result = null;
        final NodeList random = cargaAux.getElementsByTagName("random");
        if (random.getLength() != 0) {
            final Element carga = (Element) random.item(0);
            final var e = new WrappedElement(carga);
            result = LoadBuilder.randomLoadFromElement(e);
        }

        GerarCarga cargasConfiguracao;
        cargasConfiguracao =
                result;
        cargasConfiguracao = LoadBuilder.getLoadByNode(cargasConfiguracao
                , cargaAux);
        cargasConfiguracao =
                LoadBuilder.getLoadByTrace(cargasConfiguracao, cargaAux);

        return cargasConfiguracao;
    }

    private static CargaRandom randomLoadFromElement(final WrappedElement e) {
        final var computation = e.sizes()
                .filter(WrappedElement::isComputingType)
                .findFirst()
                .map(SizeInfo::fromElement)
                .orElseGet(SizeInfo::new);

        final var communication = e.sizes()
                .filter(WrappedElement::isCommunicationType)
                .findFirst()
                .map(SizeInfo::fromElement)
                .orElseGet(SizeInfo::new);

        return new CargaRandom(
                e.tasks(),
                (int) computation.minimum(),
                (int) computation.maximum(),
                (int) computation.average(),
                computation.probability(),
                (int) communication.minimum(),
                (int) communication.maximum(),
                (int) communication.average(), communication.probability(),
                e.arrivalTime()
        );
    }

    private static GerarCarga getLoadByNode(final GerarCarga cargasConfiguracao,
                                            final Element cargaAux) {

        final var c = new WrappedElement(cargaAux);

        final NodeList node = cargaAux.getElementsByTagName("node");

        if (node.getLength() == 0) {
            return cargasConfiguracao;
        }

        final List<CargaForNode> tarefasDoNo =
                new ArrayList<CargaForNode>();
        for (int i = 0; i < node.getLength(); i++) {
            final Element carga = (Element) node.item(i);
            final var e = new WrappedElement(carga);
            final String aplicacao = carga.getAttribute("application");
            final String proprietario = e.owner();
            final String escalonador = carga.getAttribute("id_master");
            final int numeroTarefas =
                    Integer.parseInt(carga.getAttribute(
                            "tasks"));
            double minComputacao = 0;
            double maxComputacao = 0;
            double minComunicacao = 0;
            double maxComunicacao = 0;
            final NodeList size = carga.getElementsByTagName("size");
            for (int j = 0; j < size.getLength(); j++) {
                final Element size1 = (Element) size.item(j);
                if ("computing".equals(size1.getAttribute("type"))) {
                    minComputacao =
                            Double.parseDouble(size1.getAttribute(
                                    "minimum"));
                    maxComputacao =
                            Double.parseDouble(size1.getAttribute(
                                    "maximum"));
                } else if ("communication".equals(
                        size1.getAttribute("type"))) {
                    minComunicacao =
                            Double.parseDouble(size1.getAttribute(
                                    "minimum"));
                    maxComunicacao =
                            Double.parseDouble(size1.getAttribute(
                                    "maximum"));
                }
            }
            final CargaForNode item = new CargaForNode(aplicacao,
                    proprietario, escalonador, numeroTarefas,
                    maxComputacao, minComputacao, maxComunicacao,
                    minComunicacao);
            tarefasDoNo.add(item);
        }

        return new CargaList(tarefasDoNo, GerarCarga.FORNODE);
    }

    private static GerarCarga getLoadByTrace(GerarCarga cargasConfiguracao,
                                             final Element cargaAux) {
        final NodeList trace = cargaAux.getElementsByTagName("trace");
        if (trace.getLength() != 0) {
            final Element carga = (Element) trace.item(0);
            final File filepath = new File(carga.getAttribute("file_path"));
            final int taskCount = Integer.parseInt(carga.getAttribute(
                    "tasks"));
            final String formato = carga.getAttribute("format");
            if (filepath.exists()) {
                cargasConfiguracao = new CargaTrace(filepath, taskCount
                        , formato);
            }
        }
        return cargasConfiguracao;
    }

    private record SizeInfo(
            double minimum, double maximum,
            double average, double probability) {
        private SizeInfo() {
            this(0, 0, 0, 0);
        }
        private SizeInfo(final double minimum, final double maximum) {
            this(minimum, maximum, 0, 0);
        }

        private static SizeInfo fromElement(final WrappedElement e) {
            return new SizeInfo(
                    e.minimum(),
                    e.maximum(),
                    e.average(),
                    e.probability()
            );
        }
    }
}