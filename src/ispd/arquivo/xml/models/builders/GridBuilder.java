package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.WrappedDocument;
import ispd.arquivo.xml.WrappedElement;
import ispd.arquivo.xml.models.IconicModel;
import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Internet;
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

    public IconicModel build() {
        final Document doc = this.doc.document();

        final var machines = doc.getElementsByTagName("machine");
        final var clusters = doc.getElementsByTagName("cluster");
        final var internet = doc.getElementsByTagName("internet");
        //Realiza leitura dos icones de cluster
        this.processCluster(clusters);
        //Realiza leitura dos icones de internet
        this.processInternet(internet);
        //Realiza leitura dos icones de máquina
        this.processMachine(machines);
        //Realiza leitura dos mestres
        this.processMaster(machines);

        this.doc.links().forEach(this::processLinkElement);

        return new IconicModel(this.vertices, this.edges);
    }

    private void processLinkElement(final WrappedElement e) {
        final var lk = this.linkFromElement(e);

        this.edges.add(lk);
    }

    private Link linkFromElement(final WrappedElement e) {
        final var origination =
                this.getVertex(e.origination());
        final var destination =
                this.getVertex(e.destination());

        final var link = new Link(
                origination, destination,
                e.iconId().local(), e.globalIconId()
        );

        link.setSelected(false);

        ((GridItem) origination).getOutboundConnections().add(link);
        ((GridItem) destination).getInboundConnections().add(link);

        link.getId().setName(e.id());
        link.setBandwidth(e.bandwidth());
        link.setLoadFactor(e.load());
        link.setLatency(e.latency());

        return link;
    }

    private Vertex getVertex(final int e) {
        return (Vertex) this.icons.get(e);
    }

    private void processMaster(final NodeList machines) {
        for (int i = 0; i < machines.getLength(); i++) {
            final Element maquina = (Element) machines.item(i);
            if (new WrappedElement(maquina).hasMasterAttribute()) {
                final Element id =
                        GridBuilder.getFirstTagElement(maquina, "icon_id");
                final int global = Integer.parseInt(id.getAttribute("global"));
                final Machine maq = (Machine) this.icons.get(global);
                this.vertices.add(maq);
                final var e = new WrappedElement(maquina);
                GridBuilder.machineFromElement(maq, e);
                final Element master = GridBuilder.getFirstTagElement(maquina,
                        "master");
                maq.setSchedulingAlgorithm(master.getAttribute("scheduler"));
                maq.setVmmAllocationPolicy(master.getAttribute("vm_alloc"));
                maq.setMaster(true);
                final NodeList slaves = master.getElementsByTagName("slave");
                final List<GridItem> escravos =
                        new ArrayList<>(slaves.getLength());
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
        }
    }

    private void processMachine(final NodeList machines) {
        for (int i = 0; i < machines.getLength(); i++) {
            final Element maquina = (Element) machines.item(i);
            final var m = new WrappedElement(maquina);

            if (maquina.getElementsByTagName("master").getLength() <= 0) {


                final Machine maq1 = GridBuilder.machineIconFromElement(m);

                this.icons.put(maq1.getId().getGlobalId(), maq1);
                this.vertices.add(maq1);
                GridBuilder.machineFromElement(maq1, m);
            } else {
                final Machine maq = GridBuilder.machineIconFromElement(m);

                this.icons.put(maq.getId().getGlobalId(), maq);
            }
        }
    }

    private void processInternet(final NodeList internet) {
        for (int i = 0; i < internet.getLength(); i++) {
            final Element inet = (Element) internet.item(i);
            final var wInet = new WrappedElement(inet);

            final Internet net = GridBuilder.netIconFromElement(wInet);

            this.vertices.add(net);
            this.icons.put(net.getId().getGlobalId(), net);
            net.getId().setName(wInet.id());

            net.setBandwidth(wInet.bandwidth());
            net.setLoadFactor(wInet.load());
            net.setLatency(wInet.latency());
        }
    }

    private void processCluster(final NodeList clusters) {
        for (int i = 0; i < clusters.getLength(); i++) {
            final var c = new WrappedElement((Element) clusters.item(i));

            final var info = IconInfo.fromElement(c);

            final var cluster = new Cluster(
                    info.x(), info.y(),
                    info.localId(), info.globalId(),
                    c.power() // TODO: Supposed to be .energy()?
            );

            cluster.setSelected(false);
            this.vertices.add(cluster);

            this.icons.put(cluster.getId().getGlobalId(), cluster);
            cluster.getId().setName(c.id());
            cluster.setComputationalPower(c.power());
            GridBuilder.setGridItemCharacteristics(cluster, c);
            cluster.setSlaveCount(c.nodes());
            cluster.setBandwidth(c.bandwidth());
            cluster.setLatency(c.latency());
            cluster.setSchedulingAlgorithm(c.scheduler());
            cluster.setVmmAllocationPolicy(c.vmAlloc());
            cluster.setOwner(c.owner());
            cluster.setMaster(c.isMaster());
        }
    }

    private static void setGridItemCharacteristics(
            final GridItem item, final WrappedElement e) {
        if (!e.hasCharacteristicAttribute()) {
            return;
        }

        final var characteristic = e.characteristics();

        if (item instanceof Cluster cluster) {
            cluster.setComputationalPower(characteristic.processor().power());
            cluster.setCoreCount(characteristic.processor().number());
            cluster.setRam(characteristic.memory().size());
            cluster.setHardDisk(characteristic.hardDisk().size());

            if (!characteristic.hasCostAttribute()) {
                return;
            }

            final var co = characteristic.costs();

            cluster.setCostPerProcessing(co.costProcessing());
            cluster.setCostPerMemory(co.costMemory());
            cluster.setCostPerDisk(co.costDisk());

        } else if (item instanceof Machine machine) {
            machine.setComputationalPower(characteristic.processor().power());
            machine.setCoreCount(characteristic.processor().number());
            machine.setRam(characteristic.memory().size());
            machine.setHardDisk(characteristic.hardDisk().size());

            if (!characteristic.hasCostAttribute()) {
                return;
            }

            final var co = characteristic.costs();

            machine.setCostPerProcessing(co.costProcessing());
            machine.setCostPerMemory(co.costMemory());
            machine.setCostPerDisk(co.costDisk());
        }
    }

    private static Internet netIconFromElement(final WrappedElement e) {
        final var info = IconInfo.fromElement(e);

        return new Internet(
                info.x(), info.y(),
                info.localId(), info.globalId()
        );
    }

    private static Machine machineIconFromElement(final WrappedElement m) {
        final var info = IconInfo.fromElement(m);

        return new Machine(
                info.x(), info.y(),
                info.localId(), info.globalId(),
                m.energy()
        );
    }

    private static void machineFromElement(
            final Machine machine, final WrappedElement e) {
        final var newName = e.id();
        machine.getId().setName(newName);

        GridBuilder.setMachinePropertiesFromElement(machine, e);
    }

    static Element getFirstTagElement(
            final Element element, final String tag) {
        // TODO: Inline this method
        return new WrappedElement(element).firstTagElement(tag);
    }

    private static void setMachinePropertiesFromElement(
            final Machine machine, final WrappedElement e) {
        machine.setComputationalPower(e.power());
        GridBuilder.setGridItemCharacteristics(machine, e);
        machine.setLoadFactor(e.load());
        machine.setOwner(e.owner());
    }

    private record IconInfo(int x, int y, int globalId, int localId) {
        private static IconInfo fromElement(final WrappedElement e) {
            final var position = e.position();
            final var iconId = e.iconId();

            return new IconInfo(
                    position.x(), position.y(),
                    iconId.global(), iconId.local()
            );
        }
    }
}
