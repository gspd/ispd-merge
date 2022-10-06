package ispd.arquivo.xml;

import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaList;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaTrace;
import ispd.motor.carga.GerarCarga;
import org.w3c.dom.Document;

import java.io.File;

public class LoadBuilder {
    static GerarCarga build(final Document doc) {
        final var load =
                new WrappedDocument(doc).loads().findFirst();

        if (load.isEmpty()) {
            return null;
        }

        final var c = load.get();

        final var randomLoad = c.randomLoads()
                .findFirst()
                .map(LoadBuilder::randomLoadFromElement)
                .orElse(null);

        if (randomLoad != null) {
            return randomLoad;
        }

        final var nodeLoad = LoadBuilder.perNodeLoadFromElement(c);

        if (nodeLoad != null)
            return nodeLoad;

        final var traceLoad = c.traceLoads()
                .findFirst()
                .map(LoadBuilder::traceLoadFromElement);

        if (traceLoad.isEmpty()){
            return null;
        }

        return traceLoad.get();
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
                (int) computation.minimum(), (int) computation.maximum(),
                (int) computation.average(), computation.probability(),
                (int) communication.minimum(), (int) communication.maximum(),
                (int) communication.average(), communication.probability(),
                e.arrivalTime()
        );
    }

    private static CargaList perNodeLoadFromElement(final WrappedElement e) {
        final var nodeLoads = e.nodeLoads()
                .map(LoadBuilder::nodeLoadFromElement)
                .toList();

        if (nodeLoads.isEmpty()) {
            return null;
        }

        return new CargaList(nodeLoads, GerarCarga.FORNODE);
    }

    private static CargaTrace traceLoadFromElement(final WrappedElement e) {
        final var file = new File(e.filePath());

        if (file.exists()) {
            return new CargaTrace(file, e.tasks(), e.format());
        }

        return null;
    }

    private static CargaForNode nodeLoadFromElement(final WrappedElement e) {
        final var computation = e.sizes()
                .filter(WrappedElement::isComputingType)
                .findFirst()
                .map(SizeInfo::rangeFromElement)
                .orElseGet(SizeInfo::new);

        final var communication = e.sizes()
                .filter(WrappedElement::isCommunicationType)
                .findFirst()
                .map(SizeInfo::rangeFromElement)
                .orElseGet(SizeInfo::new);

        return new CargaForNode(e.application(),
                e.owner(), e.masterId(), e.tasks(),
                computation.maximum(), computation.minimum(),
                communication.maximum(), communication.minimum()
        );
    }

    private record SizeInfo(
            double minimum, double maximum,
            double average, double probability) {
        private SizeInfo() {
            this(0, 0, 0, 0);
        }

        private static SizeInfo fromElement(final WrappedElement e) {
            return new SizeInfo(
                    e.minimum(), e.maximum(),
                    e.average(), e.probability()
            );
        }

        private static SizeInfo rangeFromElement(final WrappedElement e) {
            return new SizeInfo(e.minimum(), e.maximum(), 0, 0);
        }
    }
}