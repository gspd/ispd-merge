package ispd.arquivo.xml;

import ispd.arquivo.xml.utils.ServiceCenterBuilder;
import ispd.arquivo.xml.utils.SwitchConnection;
import ispd.arquivo.xml.utils.UserPowerLimit;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class QueueNetworkBuilder {
    protected final HashMap<Integer, CentroServico> serviceCenters =
            new HashMap<>(0);
    private final HashMap<CentroServico, List<CS_Maquina>> clusterSlaves =
            new HashMap<>(0);
    private final List<CS_Processamento> masters = new ArrayList<>(0);
    private final List<CS_Maquina> machines = new ArrayList<>(0);
    protected final List<CS_Comunicacao> links = new ArrayList<>(0);
    protected final List<CS_Internet> internets = new ArrayList<>(0);
    protected final Map<String, Double> powerLimits;

    public QueueNetworkBuilder(final WrappedDocument doc) {
        this.powerLimits = doc.owners().collect(Collectors.toMap(
                WrappedElement::id, o -> 0.0,
                (prev, next) -> next, HashMap::new
        ));

        doc.machines().forEach(this::processMachineElement);
        doc.clusters().forEach(this::processClusterElement);
        doc.internets().forEach(this::processInternetElement);
        doc.links().forEach(this::processLinkElement);
        doc.masters().forEach(this::addSlavesToMachine);
    }

    private void processMachineElement(final WrappedElement e) {
        final var isMaster = e.hasMasterAttribute();

        final CS_Processamento machine;

        if (isMaster) {
            machine = ServiceCenterBuilder.aMaster(e);
            this.masters.add(machine);
        } else {
            machine = ServiceCenterBuilder.aMachine(e);
            this.machines.add((CS_Maquina) machine);
        }

        this.serviceCenters.put(e.globalIconId(), machine);

        this.increaseUserPower(
                machine.getProprietario(),
                machine.getPoderComputacional()
        );
    }

    private void processClusterElement(final WrappedElement e) {
        if (e.isMaster()) {
            final var cluster = ServiceCenterBuilder.aMasterWithNoLoad(e);

            this.masters.add(cluster);
            this.serviceCenters.put(e.globalIconId(), cluster);

            final int slaveCount = e.nodes();

            final double power =
                    cluster.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(cluster.getProprietario(), power);

            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);

            SwitchConnection.toCluster(theSwitch, cluster);

            for (int i = 0; i < slaveCount; i++) {
                final var machine =
                        ServiceCenterBuilder.aMachineWithId(e, i);
                SwitchConnection.toMachine(theSwitch, machine);

                machine.addMestre(cluster);
                cluster.addEscravo(machine);

                this.machines.add(machine);
            }
        } else {
            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);
            this.serviceCenters.put(e.globalIconId(), theSwitch);

            final double power = e.power() * e.nodes();

            this.increaseUserPower(e.owner(), power);

            final int slaveCount = e.nodes();

            final var slaves = new ArrayList<CS_Maquina>(slaveCount);

            for (int i = 0; i < slaveCount; i++) {
                final var machine =
                        ServiceCenterBuilder.aMachineWithId(e, i);
                SwitchConnection.toMachine(theSwitch, machine);
                slaves.add(machine);
            }

            this.machines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    protected void processInternetElement(final WrappedElement e) {
        final var net = ServiceCenterBuilder.anInternet(e);

        this.internets.add(net);
        this.serviceCenters.put(e.globalIconId(), net);
    }

    protected void processLinkElement(final WrappedElement e) {
        final var link = ServiceCenterBuilder.aLink(e);

        this.links.add(link);

        QueueNetworkBuilder.connectLinkAndVertices(link,
                this.getVertex(e.origination()),
                this.getVertex(e.destination())
        );
    }

    private void addSlavesToMachine(final WrappedElement e) {
        final var master =
                (CS_Mestre) this.serviceCenters.get(e.globalIconId());

        e.master().slaves()
                .map(WrappedElement::id)
                .map(Integer::parseInt)
                .map(this.serviceCenters::get)
                .forEach(sc -> this.addServiceCenterSlaves(sc, master));
    }

    private void increaseUserPower(final String user, final double increment) {
        final var oldValue = this.powerLimits.get(user);
        this.powerLimits.put(user, oldValue + increment);
    }

    private static void connectLinkAndVertices(
            final CS_Link link,
            final Vertice origination, final Vertice destination) {
        link.setConexoesEntrada((CentroServico) origination);
        link.setConexoesSaida((CentroServico) destination);
        origination.addConexoesSaida(link);
        destination.addConexoesEntrada(link);
    }

    private Vertice getVertex(final int e) {
        return (Vertice) this.serviceCenters.get(e);
    }

    private void addServiceCenterSlaves(
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
        final var helper = new UserPowerLimit(this.powerLimits);

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
}