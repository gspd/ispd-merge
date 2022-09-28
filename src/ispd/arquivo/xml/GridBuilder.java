package ispd.arquivo.xml;

import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.Link;
import ispd.gui.iconico.grade.Machine;
import ispd.utils.ValidaValores;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

class GridBuilder {

    public static void buildGrid(Document doc,
                                 Collection<? super Vertex> vertices,
                                 Collection<? super Edge> edges) {
        final var icons = new HashMap<Integer, Object>(0);

        final var machines = doc.getElementsByTagName("machine");
        final var clusters = doc.getElementsByTagName("cluster");
        final var internet = doc.getElementsByTagName("internet");
        final var links = doc.getElementsByTagName("link");
        //Realiza leitura dos icones de cluster
        for (int i = 0; i < clusters.getLength(); i++) {
            final var c = new WrappedElement((Element) clusters.item(i));

            final var info = IconInfo.fromElement(c);

            final var cluster = new Cluster(
                    info.x(), info.y(),
                    info.localId(), info.globalId(),
                    c.power()
            );

            cluster.setSelected(false);
            vertices.add(cluster);

            icons.put(cluster.getId().getGlobalId(), cluster);
            cluster.getId().setName(c.id());
            ValidaValores.addNomeIcone(cluster.getId().getName());
            cluster.setComputationalPower(c.power());
            setGridItemCharacteristics(cluster, c);
            cluster.setSlaveCount(c.nodes());
            cluster.setBandwidth(c.bandwidth());
            cluster.setLatency(c.latency());
            cluster.setSchedulingAlgorithm(c.scheduler());
            cluster.setVmmAllocationPolicy(c.vmAlloc());
            cluster.setOwner(c.owner());
            cluster.setMaster(c.isMaster());
        }
        //Realiza leitura dos icones de internet
        for (int i = 0; i < internet.getLength(); i++) {
            final Element inet = (Element) internet.item(i);
            final var wInet = new WrappedElement(inet);

            final Internet net = netIconFromElement(wInet);

            vertices.add(net);
            icons.put(net.getId().getGlobalId(), net);
            net.getId().setName(wInet.id());

            ValidaValores.addNomeIcone(net.getId().getName());

            net.setBandwidth(wInet.bandwidth());
            net.setLoadFactor(wInet.load());
            net.setLatency(wInet.latency());
        }
        //Realiza leitura dos icones de máquina
        for (int i = 0; i < machines.getLength(); i++) {
            final Element maquina = (Element) machines.item(i);
            final var m = new WrappedElement(maquina);

            if (maquina.getElementsByTagName("master").getLength() <= 0) {


                final Machine maq1 = machineIconFromElement(m);

                icons.put(maq1.getId().getGlobalId(), maq1);
                vertices.add(maq1);
                machineFromElement(maq1, m);
            } else {
                final Machine maq = machineIconFromElement(m);

                icons.put(maq.getId().getGlobalId(), maq);
            }
        }
        //Realiza leitura dos mestres
        for (int i = 0; i < machines.getLength(); i++) {
            final Element maquina = (Element) machines.item(i);
            if (new WrappedElement(maquina).hasMasterAttribute()) {
                final Element id =
                        getFirstTagElement(maquina, "icon_id");
                final int global = Integer.parseInt(id.getAttribute("global"));
                final Machine maq = (Machine) icons.get(global);
                vertices.add(maq);
                final var e = new WrappedElement(maquina);
                machineFromElement(maq, e);
                final Element master = getFirstTagElement(maquina,
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
                            (GridItem) icons.get(Integer.parseInt(slave.getAttribute("id")));
                    if (escravo != null) {
                        escravos.add(escravo);
                    }
                }
                maq.setSlaves(escravos);
            }
        }
        //Realiza leitura dos icones de rede
        for (int i = 0; i < links.getLength(); i++) {
            final Element link = (Element) links.item(i);
            final Element id =
                    getFirstTagElement(link, "icon_id");
            final int global = Integer.parseInt(id.getAttribute("global"));
            final int local = Integer.parseInt(id.getAttribute("local"));
            final Element connect =
                    getFirstTagElement(link, "connect");
            final Vertex origem =
                    (Vertex) icons.get(Integer.parseInt(connect.getAttribute(
                            "origination")));
            final Vertex destino =
                    (Vertex) icons.get(Integer.parseInt(connect.getAttribute(
                            "destination")));
            final Link lk = new Link(origem, destino, local, global);
            lk.setSelected(false);
            ((GridItem) origem).getOutboundConnections().add(lk);
            ((GridItem) destino).getInboundConnections().add(lk);
            edges.add(lk);
            lk.getId().setName(link.getAttribute("id"));
            ValidaValores.addNomeIcone(lk.getId().getName());
            lk.setBandwidth(Double.parseDouble(link.getAttribute("bandwidth")));
            lk.setLoadFactor(Double.parseDouble(link.getAttribute("load")));
            lk.setLatency(Double.parseDouble(link.getAttribute("latency")));
        }
    }

    private static Internet netIconFromElement(final WrappedElement e) {
        final var info = IconInfo.fromElement(e);
        final Internet net = new Internet(
                info.x(), info.y(),
                info.localId(), info.globalId()
        );

        net.setSelected(false);
        return net;
    }

    static Element getFirstTagElement(
            final Element element, final String tag) {
        // TODO: Inline this method
        return new WrappedElement(element).firstTagElement(tag);
    }

    private static void setGridItemCharacteristics(
            final GridItem item, final WrappedElement e) {
        if (!e.hasCharacteristicAttribute()) {
            return;
        }

        final var characteristic = e.wFirstTagElement("characteristic");

        final var process = characteristic.wFirstTagElement("process");

        final var memorySize =
                characteristic.wFirstTagElement("memory").size();
        final var diskSize =
                characteristic.wFirstTagElement("hard_disk").size();

        if (item instanceof Cluster cluster) {
            cluster.setComputationalPower(process.power());
            cluster.setCoreCount(process.number());
            cluster.setRam(memorySize);
            cluster.setHardDisk(diskSize);

            if (!characteristic.hasCostAttribute()) {
                return;
            }

            final var co = characteristic.wFirstTagElement("cost");

            cluster.setCostPerProcessing(co.costProcessing());
            cluster.setCostPerMemory(co.costMemory());
            cluster.setCostPerDisk(co.costDisk());

        } else if (item instanceof Machine machine) {
            machine.setComputationalPower(process.power());
            machine.setCoreCount(process.number());
            machine.setRam(memorySize);
            machine.setHardDisk(diskSize);

            if (!characteristic.hasCostAttribute()) {
                return;
            }

            final var co = characteristic.wFirstTagElement("cost");

            machine.setCostPerProcessing(co.costProcessing());
            machine.setCostPerMemory(co.costMemory());
            machine.setCostPerDisk(co.costDisk());
        }
    }

    private static Machine machineIconFromElement(final WrappedElement m) {
        final var info = IconInfo.fromElement(m);

        final var maq = new Machine(
                info.x(), info.y(),
                info.localId(), info.globalId(),
                m.energy()
        );

        maq.setSelected(false);
        return maq;
    }

    private static void machineFromElement(
            final Machine machine, final WrappedElement e) {
        final var newName = e.id();
        machine.getId().setName(newName);
        ValidaValores.addNomeIcone(newName);

        setMachinePropertiesFromElement(machine, e);
    }

    private static void setMachinePropertiesFromElement(
            final Machine machine, final WrappedElement e) {
        machine.setComputationalPower(e.power());
        setGridItemCharacteristics(machine, e);
        machine.setLoadFactor(e.load());
        machine.setOwner(e.owner());
    }

    private record IconInfo(int x, int y, int globalId, int localId) {
        public static IconInfo fromElement(final WrappedElement e) {
            final var position = e.wFirstTagElement("position");
            final var iconId = e.wFirstTagElement("icon_id");

            return new IconInfo(
                    position.x(), position.y(),
                    iconId.global(), iconId.local()
            );
        }
    }
}
