package ispd.arquivo.xml.modelBuilders;

import ispd.arquivo.xml.WrappedDocument;
import ispd.arquivo.xml.WrappedElement;
import ispd.arquivo.xml.utils.ServiceCenterBuilder;
import ispd.arquivo.xml.utils.SwitchConnection;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.motor.metricas.MetricasUsuarios;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudQueueNetworkBuilder extends QueueNetworkBuilder {
    private final NodeList docMachines;
    private final HashMap<CentroServico, List<CS_MaquinaCloud>> clusterSlaves =
            new HashMap<>(0);
    private final List<CS_MaquinaCloud> cloudMachines = new ArrayList<>(0);
    private final List<CS_VirtualMac> virtualMachines = new ArrayList<>(0);
    private final List<CS_Processamento> virtualMachineMasters =
            new ArrayList<>(0);

    public CloudQueueNetworkBuilder(final Document model) {
        super(new WrappedDocument(model));

        final var doc = new WrappedDocument(model);

        this.docMachines = model.getElementsByTagName("machine");

        doc.masters().forEach(this::processMachineElement);
        doc.clusters().forEach(this::processClusterElement);
        doc.internets().forEach(this::processInternetElement);
        doc.links().forEach(this::processLinkElement);

        //adiciona os escravos aos mestres
        this.processMasters();

        doc.virtualMachines().forEach(this::processVirtualMachineElement);
    }

    private void processClusterElement(final WrappedElement e) {
        if (e.isMaster()) {
            final var clust = ServiceCenterBuilder.aVmmNoLoad(e);

            this.virtualMachineMasters.add(clust);
            this.serviceCenters.put(e.globalIconId(), clust);

            final int slaveCount = e.nodes();

            final double power =
                    clust.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(clust.getProprietario(), power);

            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);

            SwitchConnection.toVirtualMachineMaster(theSwitch, clust);

            for (int j = 0; j < slaveCount; j++) {
                final var machine =
                        ServiceCenterBuilder.aCloudMachineWithId(e, j);
                SwitchConnection.toCloudMachine(theSwitch, machine);

                machine.addMestre(clust);
                clust.addEscravo(machine);

                this.cloudMachines.add(machine);
            }
        } else {
            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);
            this.serviceCenters.put(e.globalIconId(), theSwitch);

            this.increaseUserPower(e.owner(), e.power() * e.nodes());

            final int slaveCount = e.nodes();

            final List<CS_MaquinaCloud> slaves = new ArrayList<>(slaveCount);

            for (int j = 0; j < slaveCount; j++) {
                final var machine =
                        ServiceCenterBuilder.aCloudMachineWithId(e, j);
                SwitchConnection.toCloudMachine(theSwitch, machine);
                slaves.add(machine);
            }

            this.cloudMachines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    private void processMasters() {
        for (int i = 0; i < this.docMachines.getLength(); i++) {
            final Element maquina = (Element) this.docMachines.item(i);
            final Element id =
                    GridBuilder.getFirstTagElement(maquina, "icon_id");
            final int global = Integer.parseInt(id.getAttribute("global"));
            if (new WrappedElement(maquina).hasMasterAttribute()) {
                final Element master =
                        GridBuilder.getFirstTagElement(maquina,
                                "master");
                final NodeList slaves = master.getElementsByTagName(
                        "slave");
                final CS_VMM mestre = (CS_VMM) this.serviceCenters.get(global);
                for (int j = 0; j < slaves.getLength(); j++) {
                    final Element slave = (Element) slaves.item(j);
                    final CentroServico maq =
                            this.serviceCenters.get(Integer.parseInt(slave.getAttribute("id")));
                    if (maq instanceof CS_Processamento) {
                        mestre.addEscravo((CS_Processamento) maq);
                        if (maq instanceof CS_MaquinaCloud maqTemp) {
                            maqTemp.addMestre(mestre);
                        }
                    } else if (maq instanceof CS_Switch) {
                        for (final CS_MaquinaCloud escr :
                                this.clusterSlaves.get(maq)) {
                            escr.addMestre(mestre);
                            mestre.addEscravo(escr);
                        }
                    }
                }
            }
        }
    }

    private void processVirtualMachineElement(final WrappedElement e) {
        final var virtualMachine =
                ServiceCenterBuilder.aVirtualMachine(e);

        final var masterId = e.vmm();

        this.virtualMachineMasters.stream()
                .filter(cs -> cs.getId().equals(masterId))
                .map(CS_VMM.class::cast)
                .forEach(master -> CloudQueueNetworkBuilder
                        .connectVmAndMaster(virtualMachine, master));

        this.virtualMachines.add(virtualMachine);
    }

    private static void connectVmAndMaster(
            final CS_VirtualMac vm, final CS_VMM master) {
        vm.addVMM(master);
        master.addVM(vm);
    }

    @Override
    protected CS_Processamento makeAndAddMachine(final WrappedElement e) {
        final CS_Processamento machine;

        if (e.hasMasterAttribute()) {
            machine = ServiceCenterBuilder.aVirtualMachineMaster(e);
            this.virtualMachineMasters.add(machine);
        } else {
            machine = ServiceCenterBuilder.aCloudMachine(e);
            this.cloudMachines.add((CS_MaquinaCloud) machine);
        }

        return machine;
    }

    public RedeDeFilasCloud build() {
        final List<String> owners = new ArrayList<>(0);
        final List<Double> powers = new ArrayList<>(0);

        for (final Map.Entry<String, Double> entry :
                this.powerLimits.entrySet()) {
            owners.add(entry.getKey());
            powers.add(entry.getValue());
        }
        //cria as métricas de usuarios para cada mestre
        for (final CS_Processamento mestre : this.virtualMachineMasters) {
            final CS_VMM mst = (CS_VMM) mestre;
            final MetricasUsuarios mu = new MetricasUsuarios();
            mu.addAllUsuarios(owners, powers);
            mst.getEscalonador().setMetricaUsuarios(mu);
        }
        final RedeDeFilasCloud rdf =
                new RedeDeFilasCloud(this.virtualMachineMasters,
                        this.cloudMachines, this.virtualMachines,
                        this.links,
                        this.internets);
        //cria as métricas de usuarios globais da rede de filas
        final MetricasUsuarios mu = new MetricasUsuarios();
        mu.addAllUsuarios(owners, powers);
        rdf.setUsuarios(owners);
        return rdf;
    }
}
