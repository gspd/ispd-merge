package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.models.IconicModel;
import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Link;
import ispd.gui.iconico.grade.Machine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridBuilder {
    private final Collection<Vertex> vertices = new ArrayList<>(0);
    private final Collection<Edge> edges = new ArrayList<>(0);
    private final WrappedDocument doc;
    private final Map<Integer, Object> icons = new HashMap<>(0);

    public GridBuilder(final WrappedDocument doc) {
        this.doc = doc;
    }

    private static void connectLinkAndVertices(
            final GridItem link,
            final GridItem origination, final GridItem destination) {
        origination.getOutboundConnections().add(link);
        destination.getInboundConnections().add(link);
    }

    static Element getFirstTagElement(
            final Element element, final String tag) {
        // TODO: Inline this method
        return new WrappedElement(element).firstTagElement(tag);
    }

    public IconicModel build() {
        final Document doc = this.doc.document();

        final var machines = doc.getElementsByTagName("machine");

        this.doc.clusters().forEach(this::processClusterElement);
        this.doc.internets().forEach(this::processInternetElement);

        //Realiza leitura dos icones de máquina
        this.processMachine(machines);
        //Realiza leitura dos mestres
        this.processMaster(machines);

        this.doc.links().forEach(this::processLinkElement);

        return new IconicModel(this.vertices, this.edges);
    }

    private void processLinkElement(final WrappedElement e) {
        final var lk = this.connectedLinkFromElement(e);

        this.edges.add(lk);
    }

    private Link connectedLinkFromElement(final WrappedElement e) {
        final var origination = this.getVertex(e.origination());
        final var destination = this.getVertex(e.destination());

        final var link = IconBuilder.aLink(e, origination, destination);

        GridBuilder.connectLinkAndVertices(
                link, (GridItem) origination, (GridItem) destination);

        return link;
    }

    private Vertex getVertex(final int e) {
        return (Vertex) this.icons.get(e);
    }

    private void processMaster(final NodeList machines) {
        for (int i = 0; i < machines.getLength(); i++) {
            final Element maquina = (Element) machines.item(i);
            if (new WrappedElement(maquina).hasMasterAttribute()) {
                processMaster(maquina);
            }
        }
    }

    private void processMaster(Element maquina) {

        final var e = new WrappedElement(maquina);

        final Machine maq = (Machine) this.icons.get(e.globalIconId());
        this.vertices.add(maq);

        maq.getId().setName(e.id());

        maq.setComputationalPower(e.power());
        IconBuilder.setProcessingCenterCharacteristics(maq, e);
        maq.setLoadFactor(e.load());
        maq.setOwner(e.owner());

        final Element master = GridBuilder.getFirstTagElement(maquina,
                "master");
        maq.setSchedulingAlgorithm(e.master().scheduler());
        maq.setVmmAllocationPolicy(e.master().vmAlloc());
        maq.setMaster(true);

        final NodeList slaves = master.getElementsByTagName("slave");
        final List<GridItem> escravos =                new ArrayList<>(slaves.getLength());
        for (int j = 0; j < slaves.getLength(); j++) {
            final Element slave = (Element) slaves.item(j);
            final GridItem escravo =
                    (GridItem) this.icons.get(Integer.parseInt(slave.getAttribute("id")));
            if (escravo != null) {
                escravos.add(escravo);
            }
        }
        maq.setSlaves(escravos);
    }

    private void processMachine(final NodeList machines) {
        for (int i = 0; i < machines.getLength(); i++) {
            final Element maquina = (Element) machines.item(i);
            final var m = new WrappedElement(maquina);

            if (!m.hasMasterAttribute()) {
                final var info = IconBuilder.IconInfo.fromElement(m);

                final Machine maq1 = new Machine(
                        info.x(), info.y(),
                        info.localId(), info.globalId(),
                        m.energy()
                );

                this.icons.put(maq1.getId().getGlobalId(), maq1);
                this.vertices.add(maq1);
                final var newName = m.id();
                maq1.getId().setName(newName);

                maq1.setComputationalPower(m.power());
                IconBuilder.setProcessingCenterCharacteristics(maq1, m);
                maq1.setLoadFactor(m.load());
                maq1.setOwner(m.owner());
            } else {
                final var info = IconBuilder.IconInfo.fromElement(m);

                final Machine maq = new Machine(
                        info.x(), info.y(),
                        info.localId(), info.globalId(),
                        m.energy()
                );

                this.icons.put(m.globalIconId(), maq);
            }
        }
    }

    private void processInternetElement(WrappedElement e) {
        final var net = IconBuilder.anInternet(e);

        this.vertices.add(net);
        this.icons.put(e.globalIconId(), net);
    }

    private void processClusterElement(WrappedElement e) {
        final Cluster cluster = IconBuilder.aCluster(e);

        this.vertices.add(cluster);
        this.icons.put(e.globalIconId(), cluster);
    }

}
