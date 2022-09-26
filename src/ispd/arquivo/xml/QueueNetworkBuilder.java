package ispd.arquivo.xml;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.Vertice;
import ispd.motor.metricas.MetricasUsuarios;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

class QueueNetworkBuilder {
    private final Map<String, Double> users = new HashMap<>(0);
    // TODO: Investigate why profiles is query-less
    private final Map<String, Double> profiles = new HashMap<>(0);
    private final HashMap<Integer, CentroServico> serviceCenters =
            new HashMap<>(0);
    private final HashMap<CentroServico, List<CS_Maquina>> clusterSlaves =
            new HashMap<>(0);
    private final List<CS_Processamento> masters = new ArrayList<>(0);
    private final List<CS_Maquina> machines = new ArrayList<>(0);
    private final List<CS_Comunicacao> links = new ArrayList<>(0);
    private final List<CS_Internet> internets = new ArrayList<>(0);
    private final List<String> owners = new ArrayList<>(0);
    private final List<Double> powers = new ArrayList<>(0);
    private final DocumentWrapper doc;

    QueueNetworkBuilder(final Document doc) {
        this.doc = new DocumentWrapper(doc);

        final Map<String, Consumer<Element>> processingSteps = Map.of(
                "owner", this::setUserPowerLimit,
                "machine", this::processMachineElement,
                "cluster", this::processClusterElement,
                "internet", this::processNetElement,
                "link", this::processLinkElement
        );

        processingSteps.forEach(this.doc::forEachElementWithTag);

        this.addSlavesToMasters();
    }

    private void setUserPowerLimit(final Element user) {
        final var id = user.getAttribute("id");
        this.users.put(id, 0.0);
        this.profiles.put(id,
                Utils.getValueAttribute(user, "powerlimit")
        );
    }

    private void processMachineElement(final Element elem) {
        final var isMaster = Utils.isValidMaster(elem);

        final CS_Processamento machine;

        if (isMaster) {
            machine = IconicoXML.masterFromElement(elem);
        } else {
            machine = IconicoXML.machineFromElement(elem);
        }

        this.serviceCenters.put(
                IconicoXML.getIconGlobalId(elem),
                machine
        );

        if (isMaster) {
            this.masters.add(machine);
        } else {
            this.machines.add((CS_Maquina) machine);
        }

        this.increaseUserPower(
                machine.getProprietario(),
                machine.getPoderComputacional()
        );
    }

    private void processClusterElement(final Element elem) {
        final int id = IconicoXML.getIconGlobalId(elem);

        if (IconicoXML.isMaster(elem)) {
            final var cluster = IconicoXML.clusterFromElement(elem);

            this.masters.add(cluster);
            this.serviceCenters.put(id, cluster);

            final int slaveCount = IconicoXML.getIntValueAttribute(
                    elem, "nodes"
            );

            final double power =
                    cluster.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(cluster.getProprietario(), power);

            final var theSwitch = IconicoXML.switchFromElement(elem);

            this.links.add(theSwitch);

            IconicoXML.connectClusterAndSwitch(cluster, theSwitch);

            for (int i = 0; i < slaveCount; i++) {
                final var machine = IconicoXML.machineFromElement(elem, i);
                IconicoXML.connectMachineAndSwitch(machine, theSwitch);

                machine.addMestre(cluster);
                cluster.addEscravo(machine);

                this.machines.add(machine);
            }

        } else {
            final var theSwitch = IconicoXML.switchFromElement(elem);

            this.links.add(theSwitch);
            this.serviceCenters.put(id, theSwitch);

            final double power =
                    Utils.getValueAttribute(elem, "power")
                    * IconicoXML.getIntValueAttribute(elem, "nodes");

            this.increaseUserPower(elem.getAttribute("owner"), power);

            final int slaveCount = Integer.parseInt(
                    elem.getAttribute("nodes")
            );

            final var slaves = new ArrayList<CS_Maquina>(slaveCount);

            for (int i = 0; i < slaveCount; i++) {
                final var machine = IconicoXML.machineFromElement(elem, i);
                IconicoXML.connectMachineAndSwitch(machine, theSwitch);
                slaves.add(machine);
            }

            this.machines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    private void processNetElement(final Element inet) {
        final var net = QueueNetworkBuilder.internetFromElement(inet);

        this.internets.add(net);
        this.serviceCenters.put(
                IconicoXML.getIconGlobalId(inet),
                net
        );
    }

    private void processLinkElement(final Element elem) {
        final var link = IconicoXML.linkFromElement(elem);

        this.links.add(link);

        QueueNetworkBuilder.connectLinkAndVertices(link,
                this.getElementVertex(elem, "origination"),
                this.getElementVertex(elem, "destination")
        );
    }

    private void addSlavesToMasters() {
        this.doc.elementsWithTag("machine")
                .filter(Utils::isValidMaster)
                .forEach(this::addSlavesToMachine);
    }

    private void increaseUserPower(final String user, final double value) {
        final var oldValue = this.users.get(user);
        this.users.put(user, oldValue + value);
    }

    private static CS_Internet internetFromElement(final Element elem) {
        return new CS_Internet(
                elem.getAttribute("id"),
                Utils.getValueAttribute(elem, "bandwidth"),
                Utils.getValueAttribute(elem, "load"),
                Utils.getValueAttribute(elem, "latency")
        );
    }

    private static void connectLinkAndVertices(final CS_Link link,
                                               final Vertice origination,
                                               final Vertice destination) {
        link.setConexoesEntrada((CentroServico) origination);
        link.setConexoesSaida((CentroServico) destination);
        origination.addConexoesSaida(link);
        destination.addConexoesEntrada(link);
    }

    private Vertice getElementVertex(final Element elem,
                                     final String vertexEnd) {
        return (Vertice) this.serviceCenters.get(IconicoXML.getIntValueAttribute(IconicoXML.getFirstTagElement(elem, "connect"), vertexEnd));
    }

    private void addSlavesToMachine(final Element machine) {
        final var master = (CS_Mestre) this.serviceCenters.get(
                IconicoXML.getIconGlobalId(machine)
        );

        final var slaves = IconicoXML
                .getFirstTagElement(machine, "master")
                .getElementsByTagName("slave");

        IntStream.range(0, slaves.getLength())
                .mapToObj(slaves::item)
                .map(Element.class::cast)
                .map(IconicoXML::elementId)
                .map(Integer::parseInt)
                .map(this.serviceCenters::get)
                .forEach(sc -> this.processServiceCenter(sc, master));
    }

    private void processServiceCenter(
            final CentroServico serviceCenter, final CS_Mestre master) {
        if (serviceCenter instanceof CS_Processamento proc) {
            master.addEscravo(proc);
            if (serviceCenter instanceof CS_Maquina machine) {
                machine.addMestre(master);
            }
        } else if (serviceCenter instanceof CS_Switch) {
            for (final var slave : this.clusterSlaves.get(serviceCenter)) {
                slave.addMestre(master);
                master.addEscravo(slave);
            }
        }
    }

    public RedeDeFilas build() {
        this.users.forEach((user, power) -> {
            this.owners.add(user);
            this.powers.add(power);
        });

        this.masters.stream()
                .map(CS_Mestre.class::cast)
                .forEach(this::setUserMetrics);

        final var queueNetwork = new RedeDeFilas(
                this.masters, this.machines, this.links, this.internets);

        this.makeUserMetrics();

        queueNetwork.setUsuarios(this.owners);
        return queueNetwork;
    }

    private void setUserMetrics(final CS_Mestre master) {
        // TODO: Why create a new one every time?
        master.getEscalonador().setMetricaUsuarios(this.makeUserMetrics());
    }

    private MetricasUsuarios makeUserMetrics() {
        final var metrics = new MetricasUsuarios();
        metrics.addAllUsuarios(this.owners, this.powers);
        return metrics;
    }
}
