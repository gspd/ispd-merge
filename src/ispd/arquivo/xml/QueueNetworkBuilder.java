package ispd.arquivo.xml;

import ispd.escalonador.Escalonador;
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
    private final Map<String, Double> powerLimits = new HashMap<>(0);
    private final HashMap<Integer, CentroServico> serviceCenters =
            new HashMap<>(0);
    private final HashMap<CentroServico, List<CS_Maquina>> clusterSlaves =
            new HashMap<>(0);
    private final List<CS_Processamento> masters = new ArrayList<>(0);
    private final List<CS_Maquina> machines = new ArrayList<>(0);
    private final List<CS_Comunicacao> links = new ArrayList<>(0);
    private final List<CS_Internet> internets = new ArrayList<>(0);
    private final DocumentWrapper doc;

    QueueNetworkBuilder(final Document doc) {
        this.doc = new DocumentWrapper(doc);

        final Map<String, Consumer<Element>> processingSteps = Map.of(
                "owner", this::setOwnerPowerLimit,
                "machine", this::processMachineElement,
                "cluster", this::processClusterElement,
                "internet", this::processNetElement,
                "link", this::processLinkElement
        );

        processingSteps.forEach(this.doc::forEachElementWithTag);

        this.addSlavesToMasters();
    }

    private void setOwnerPowerLimit(final Element user) {
        final var e = new WrappedElement(user);
        final var id = e.id();
        this.powerLimits.put(id, 0.0);
    }

    private void processMachineElement(final Element elem) {
        final var e = new WrappedElement(elem);
        final var isMaster = e.hasMasterAttribute();

        final CS_Processamento machine;

        if (isMaster) {
            machine = QueueNetworkBuilder.masterFromElement(e);
        } else {
            machine = QueueNetworkBuilder.machineFromElement(e);
        }

        this.serviceCenters.put(e.globalIconId(), machine);

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
        final var e = new WrappedElement(elem);
        final int id = e.globalIconId();

        if (e.isMaster()) {
            final var cluster = QueueNetworkBuilder.masterFromElementNoLoad(e);

            this.masters.add(cluster);
            this.serviceCenters.put(id, cluster);

            final int slaveCount = IconicoXML.getIntValueAttribute(
                    elem, "nodes"
            );

            final double power =
                    cluster.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(cluster.getProprietario(), power);

            final var theSwitch = QueueNetworkBuilder.switchFromElement(e);

            this.links.add(theSwitch);

            QueueNetworkBuilder.connectClusterAndSwitch(cluster, theSwitch);

            for (int i = 0; i < slaveCount; i++) {
                final var e1 = new WrappedElement(elem);
                final var machine =
                        QueueNetworkBuilder.machineFromElement(i, e1);
                QueueNetworkBuilder.connectMachineAndSwitch(machine, theSwitch);

                machine.addMestre(cluster);
                cluster.addEscravo(machine);

                this.machines.add(machine);
            }

        } else {
            final var theSwitch = QueueNetworkBuilder.switchFromElement(e);

            this.links.add(theSwitch);
            this.serviceCenters.put(id, theSwitch);

            final double power =
                    new WrappedElement(elem).getDouble("power")
                    * IconicoXML.getIntValueAttribute(elem, "nodes");

            this.increaseUserPower(elem.getAttribute("owner"), power);

            final int slaveCount = Integer.parseInt(
                    elem.getAttribute("nodes")
            );

            final var slaves = new ArrayList<CS_Maquina>(slaveCount);

            for (int i = 0; i < slaveCount; i++) {
                final var e1 = new WrappedElement(elem);
                final var machine =
                        QueueNetworkBuilder.machineFromElement(i, e1);
                QueueNetworkBuilder.connectMachineAndSwitch(machine, theSwitch);
                slaves.add(machine);
            }

            this.machines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    private void processNetElement(final Element internet) {
        final var e = new WrappedElement(internet);
        final var net = QueueNetworkBuilder.internetFromElement(e);

        this.internets.add(net);
        this.serviceCenters.put(e.globalIconId(), net);
    }

    private void processLinkElement(final Element elem) {
        final var e = new WrappedElement(elem);
        final var link = QueueNetworkBuilder.linkFromElement(e);

        this.links.add(link);

        QueueNetworkBuilder.connectLinkAndVertices(link,
                this.getElementVertex(elem, "origination"),
                this.getElementVertex(elem, "destination")
        );
    }

    private void addSlavesToMasters() {
        this.doc.elementsWithTag("machine")
                .map(WrappedElement::new)
                .filter(WrappedElement::hasMasterAttribute)
                .map(WrappedElement::getElement)
                .forEach(this::addSlavesToMachine);
    }

    private static CS_Mestre masterFromElement(final WrappedElement e) {
        return new CS_Mestre(e.id(), e.owner(), e.power(), e.load(),
                e.mastersScheduler(), e.energy());
    }

    private static CS_Maquina machineFromElement(final WrappedElement e) {
        return new CS_Maquina(e.id(), e.owner(), e.power(), 1,
                e.load(), e.energy());
    }

    private void increaseUserPower(final String user, final double value) {
        final var oldValue = this.powerLimits.get(user);
        this.powerLimits.put(user, oldValue + value);
    }

    private static CS_Mestre masterFromElementNoLoad(final WrappedElement e) {
        return new CS_Mestre(e.id(), e.owner(), e.power(), 0.0,
                e.scheduler(), e.energy());
    }

    private static CS_Switch switchFromElement(final WrappedElement e) {
        return new CS_Switch(e.id(), e.bandwidth(), 0.0, e.latency());
    }

    private static void connectClusterAndSwitch(
            final CS_Mestre cluster, final CS_Switch theSwitch) {
        cluster.addConexoesEntrada(theSwitch);
        cluster.addConexoesSaida(theSwitch);
        QueueNetworkBuilder.connectSwitchAndServiceCenter(theSwitch, cluster);
    }

    private static CS_Maquina machineFromElement(
            final int id, final WrappedElement e) {
        return new CS_Maquina(e.id(), e.owner(), e.power(), 1, 0.0,
                id + 1, e.energy());
    }

    private static void connectMachineAndSwitch(
            final CS_Maquina machine, final CS_Switch theSwitch) {
        machine.addConexoesSaida(theSwitch);
        machine.addConexoesEntrada(theSwitch);
        QueueNetworkBuilder.connectSwitchAndServiceCenter(theSwitch, machine);
    }

    private static CS_Internet internetFromElement(final WrappedElement e) {
        return new CS_Internet(e.id(), e.bandwidth(), e.load(), e.latency());
    }

    private static CS_Link linkFromElement(final WrappedElement e) {
        return new CS_Link(e.id(), e.bandwidth(), e.load(), e.latency());
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
        final var e = new WrappedElement(machine);
        final var master =
                (CS_Mestre) this.serviceCenters.get(e.globalIconId());

        final var slaves = e.mastersSlaves();

        IntStream.range(0, slaves.getLength())
                .mapToObj(slaves::item)
                .map(Element.class::cast)
                .map(IconicoXML::elementId)
                .map(Integer::parseInt)
                .map(this.serviceCenters::get)
                .forEach(sc -> this.processServiceCenter(sc, master));
    }

    private static void connectSwitchAndServiceCenter(
            final CS_Switch theSwitch, final CentroServico serviceCenter) {
        theSwitch.addConexoesEntrada(serviceCenter);
        theSwitch.addConexoesSaida(serviceCenter);
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
        final var helper = new UserPowerLimitHelper(this.powerLimits);

        this.masters.stream()
                .map(CS_Mestre.class::cast)
                .map(CS_Mestre::getEscalonador)
                .forEach(helper::setSchedulerUserMetrics);

        final var queueNetwork = new RedeDeFilas(
                this.masters, this.machines, this.links, this.internets);

        queueNetwork.setUsuarios(helper.getOwners());
        return queueNetwork;
    }

    static class UserPowerLimitHelper {
        private final List<String> owners;
        private final List<Double> limits;

        private UserPowerLimitHelper(final Map<String, Double> powerLimits) {
            this.owners = new ArrayList<>(powerLimits.keySet());
            this.limits = new ArrayList<>(powerLimits.values());
        }

        private void setSchedulerUserMetrics(final Escalonador scheduler) {
            scheduler.setMetricaUsuarios(this.makeUserMetrics());
        }

        private MetricasUsuarios makeUserMetrics() {
            final var metrics = new MetricasUsuarios();
            metrics.addAllUsuarios(this.owners, this.limits);
            return metrics;
        }

        public List<String> getOwners() {
            return this.owners;
        }
    }
}
