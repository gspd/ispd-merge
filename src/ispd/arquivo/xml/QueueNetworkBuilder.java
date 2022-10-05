package ispd.arquivo.xml;

import ispd.arquivo.xml.utils.Connection;
import ispd.arquivo.xml.utils.ServiceCenterBuilder;
import ispd.escalonador.Escalonador;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
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
    private final WrappedDocument doc;

    QueueNetworkBuilder(final Document doc) {
        this.doc = new WrappedDocument(doc);

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
        this.setOwnerPowerLimit(e);
    }

    private void processMachineElement(final Element elem) {
        final var e = new WrappedElement(elem);
        final var isMaster = e.hasMasterAttribute();

        final CS_Processamento machine;

        if (isMaster) {
            machine = ServiceCenterBuilder.aMaster(e);
        } else {
            machine = ServiceCenterBuilder.aMachine(e);
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
            final var cluster = ServiceCenterBuilder.aMasterWithNoLoad(e);

            this.masters.add(cluster);
            this.serviceCenters.put(id, cluster);

            final int slaveCount = new WrappedElement(elem).getInt("nodes");

            final double power =
                    cluster.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(cluster.getProprietario(), power);

            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);

            Connection.connectClusterAndSwitch(cluster, theSwitch);

            for (int i = 0; i < slaveCount; i++) {
                final var e1 = new WrappedElement(elem);
                final var machine =
                        ServiceCenterBuilder.aMachineWithId(e1, i);
                Connection.connectMachineAndSwitch(machine, theSwitch);

                machine.addMestre(cluster);
                cluster.addEscravo(machine);

                this.machines.add(machine);
            }

        } else {
            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);
            this.serviceCenters.put(id, theSwitch);

            final double power =
                    new WrappedElement(elem).getDouble("power")
                    * new WrappedElement(elem).getInt("nodes");

            this.increaseUserPower(elem.getAttribute("owner"), power);

            final int slaveCount = Integer.parseInt(
                    elem.getAttribute("nodes")
            );

            final var slaves = new ArrayList<CS_Maquina>(slaveCount);

            for (int i = 0; i < slaveCount; i++) {
                final var e1 = new WrappedElement(elem);
                final var machine =
                        ServiceCenterBuilder.aMachineWithId(e1, i);
                Connection.connectMachineAndSwitch(machine, theSwitch);
                slaves.add(machine);
            }

            this.machines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    private void processNetElement(final Element internet) {
        final var e = new WrappedElement(internet);
        final var net = ServiceCenterBuilder.anInternet(e);

        this.internets.add(net);
        this.serviceCenters.put(e.globalIconId(), net);
    }

    private void processLinkElement(final Element elem) {
        final var e = new WrappedElement(elem);
        final var link = ServiceCenterBuilder.aLink(e);

        this.links.add(link);

        Connection.connectLinkAndVertices(link,
                this.getElementVertex(elem, "origination"),
                this.getElementVertex(elem, "destination")
        );
    }

    private void addSlavesToMasters() {
        this.doc.wElementsWithTag("machine")
                .filter(WrappedElement::hasMasterAttribute)
                .forEach(this::addSlavesToMachine);
    }

    private void setOwnerPowerLimit(final WrappedElement user) {
        this.powerLimits.put(user.id(), 0.0);
    }

    private void increaseUserPower(final String user, final double increment) {
        final var oldValue = this.powerLimits.get(user);
        this.powerLimits.put(user, oldValue + increment);
    }

    private Vertice getElementVertex(final Element elem,
                                     final String vertexEnd) {
        final var e = new WrappedElement(elem);
        return (Vertice) this.serviceCenters.get(e.vertex(vertexEnd));
    }

    private void addSlavesToMachine(final WrappedElement e) {
        final var master =
                (CS_Mestre) this.serviceCenters.get(e.globalIconId());

        final var slaves = e.wMastersSlaves();

        slaves.map(WrappedElement::id)
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
        final var helper = new UserPowerLimitHelper(this.powerLimits);

        this.masters.stream()
                .map(CS_Mestre.class::cast)
                .map(CS_Mestre::getEscalonador)
                .forEach(helper::setSchedulerUserMetrics);

        final var queueNetwork = this.initQueueNetwork();
        queueNetwork.setUsuarios(helper.getOwners());
        return queueNetwork;
    }

    private RedeDeFilas initQueueNetwork() {
        return new RedeDeFilas(
                this.masters, this.machines,
                this.links, this.internets,
                this.powerLimits
        );
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
