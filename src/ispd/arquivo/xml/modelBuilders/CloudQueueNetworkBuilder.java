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
    private final NodeList docClusters;
    private final NodeList docVms;
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
        this.docClusters = model.getElementsByTagName("cluster");
        this.docVms = model.getElementsByTagName("virtualMac");

        //cria maquinas, mestres, internets e mestres dos clusters
        //Realiza leitura dos icones de máquina
        doc.masters().forEach(this::processMachineElement);

        //Realiza leitura dos icones de cluster
        this.processClusters();

        doc.internets().forEach(this::processInternetElement);
        doc.links().forEach(this::processLinkElement);


        //adiciona os escravos aos mestres
        this.processMasters();

        //Realiza leitura dos ícones de máquina virtual
        this.processVirtualMachines();
    }

    private void processClusters() {
        for (int i = 0; i < this.docClusters.getLength(); i++) {
            final Element cluster = (Element) this.docClusters.item(i);
            this.processClusterElement(cluster);
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

    private void processVirtualMachines() {
        for (int i = 0; i < this.docVms.getLength(); i++) {
            final Element virtualMac = (Element) this.docVms.item(i);
            final CS_VirtualMac VM =
                    new CS_VirtualMac(virtualMac.getAttribute("id"),
                            virtualMac.getAttribute("owner"),
                            Integer.parseInt(virtualMac.getAttribute(
                                    "power")),
                            Double.parseDouble(virtualMac.getAttribute(
                                    "mem_alloc")),
                            Double.parseDouble(virtualMac.getAttribute(
                                    "disk_alloc")),
                            virtualMac.getAttribute("op_system"));
            //adicionando VMM responsável pela VM
            for (final CS_Processamento aux : this.virtualMachineMasters) {
                if (virtualMac.getAttribute("vmm").equals(aux.getId())) {
                    //atentar ao fato de que a solução falha se o nome do
                    // vmm
                    // for alterado e não atualizado na tabela das vms
                    //To do: corrigir problema futuramente
                    VM.addVMM((CS_VMM) aux);
                    //adicionando VM para o VMM

                    final CS_VMM vmm = (CS_VMM) aux;
                    vmm.addVM(VM);

                }

            }
            this.virtualMachines.add(VM);
        }
    }

    private void processClusterElement(final Element cluster) {
        final var e = new WrappedElement(cluster);

        if (e.isMaster()) {
            final var clust = ServiceCenterBuilder.aVmmNoLoad(e);

            this.virtualMachineMasters.add(clust);
            this.serviceCenters.put(e.globalIconId(), clust);

            final int slaveCount = e.nodes();

            final double power =
                    clust.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(clust.getProprietario(), power);

            final var theSwitch = new CS_Switch(
                    (e.id() + "switch"),
                    e.bandwidth(),
                    0.0,
                    e.latency()
            );

            this.links.add(theSwitch);

            clust.addConexoesEntrada(theSwitch);
            clust.addConexoesSaida(theSwitch);
            theSwitch.addConexoesEntrada(clust);
            theSwitch.addConexoesSaida(clust);

            for (int j = 0; j < slaveCount; j++) {
                final var machine =
                        ServiceCenterBuilder.aCloudMachineWithId(e, j);
                SwitchConnection.toCloudMachine(machine, theSwitch);

                machine.addMestre(clust);
                clust.addEscravo(machine);

                this.cloudMachines.add(machine);
            }
        } else {
            final CS_Switch Switch = new CS_Switch(
                    (cluster.getAttribute("id") + "switch"),
                    Double.parseDouble(cluster.getAttribute(
                            "bandwidth")),
                    0.0,
                    Double.parseDouble(cluster.getAttribute("latency")));
            this.links.add(Switch);
            this.serviceCenters.put(e.globalIconId(), Switch);
            //Contabiliza para o usuario poder computacional do mestre
            final double total =
                    Double.parseDouble(cluster.getAttribute(
                            "power"))
                    * Integer.parseInt(cluster.getAttribute(
                            "nodes"
                    ));
            this.powerLimits.put(cluster.getAttribute("owner"),
                    total + this.powerLimits.get(cluster.getAttribute(
                            "owner")));
            final List<CS_MaquinaCloud> maqTemp =
                    new ArrayList<>();
            final int numeroEscravos =
                    Integer.parseInt(cluster.getAttribute(
                            "nodes"));
            for (int j = 0; j < numeroEscravos; j++) {
                final Element caracteristica =
                        (Element) cluster.getElementsByTagName(
                                "characteristic");
                final Element custo =
                        (Element) caracteristica.getElementsByTagName(
                                "cost");
                final Element processamento =
                        (Element) caracteristica.getElementsByTagName(
                                "process");
                final Element memoria =
                        (Element) caracteristica.getElementsByTagName(
                                "memory");
                final Element disco =
                        (Element) caracteristica.getElementsByTagName(
                                "hard_disk");
                final var maq =
                        new CS_MaquinaCloud(
                                "%s.%d".formatted(cluster.getAttribute(
                                        "id"), j),
                                cluster.getAttribute("owner"),
                                Double.parseDouble(processamento.getAttribute("power")),
                                Integer.parseInt(processamento.getAttribute(
                                        "number")),
                                Double.parseDouble(memoria.getAttribute(
                                        "size")),
                                Double.parseDouble(disco.getAttribute(
                                        "size")),
                                Double.parseDouble(custo.getAttribute(
                                        "cost_proc")),
                                Double.parseDouble(custo.getAttribute(
                                        "cost_mem")),
                                Double.parseDouble(custo.getAttribute(
                                        "cost_disk")),
                                0.0,
                                j + 1
                        );
                SwitchConnection.toCloudMachine(maq, Switch);
                maqTemp.add(maq);
                this.cloudMachines.add(maq);
            }
            this.clusterSlaves.put(Switch, maqTemp);
        }
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
