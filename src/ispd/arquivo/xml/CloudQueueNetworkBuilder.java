package ispd.arquivo.xml;

import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.motor.filas.servidores.implementacao.Vertice;
import ispd.motor.metricas.MetricasUsuarios;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudQueueNetworkBuilder {
    private final NodeList docMachines;
    private final NodeList docClusters;
    private final NodeList docInternet;
    private final NodeList docLinks;
    private final NodeList docOwners;
    private final NodeList docVms;
    private final HashMap<Integer, CentroServico> serviceCenters =
            new HashMap<>(0);
    private final HashMap<CentroServico, List<CS_MaquinaCloud>> clusterSlaves =
            new HashMap<>(0);
    private final List<CS_MaquinaCloud> machines = new ArrayList<>(0);
    private final List<CS_VirtualMac> virtualMachines = new ArrayList<>(0);
    private final List<CS_Comunicacao> links = new ArrayList<>(0);
    private final List<CS_Internet> internets = new ArrayList<>(0);
    private final List<CS_Processamento> virtualMachineMasters =
            new ArrayList<>(0);
    private final HashMap<String, Double> users = new HashMap<>(0);

    private final List<String> owners = new ArrayList<>(0);
    private final List<Double> powers = new ArrayList<>(0);

    public CloudQueueNetworkBuilder(final Document model) {
        this.docMachines = model.getElementsByTagName("machine");
        this.docClusters = model.getElementsByTagName("cluster");
        this.docInternet = model.getElementsByTagName("internet");
        this.docLinks = model.getElementsByTagName("link");
        this.docOwners = model.getElementsByTagName("owner");
        this.docVms = model.getElementsByTagName("virtualMac");
    }

    public static CS_MaquinaCloud cloudMachineFromElement(
            final Element cluster, final int id,
            final Element cost, final Element processing,
            final Element memory, final Element disk) {
        return new CS_MaquinaCloud(
                "%s.%d".formatted(cluster.getAttribute("id"), id),
                cluster.getAttribute("owner"),
                Double.parseDouble(processing.getAttribute("power")),
                Integer.parseInt(processing.getAttribute("number")),
                Double.parseDouble(memory.getAttribute("size")),
                Double.parseDouble(disk.getAttribute("size")),
                Double.parseDouble(cost.getAttribute("cost_proc")),
                Double.parseDouble(cost.getAttribute("cost_mem")),
                Double.parseDouble(cost.getAttribute("cost_disk")),
                0.0,
                id + 1
        );
    }

    public static void connectMachineAndSwitch(
            final CS_Switch theSwitch, final CS_MaquinaCloud maq) {
        maq.addConexoesSaida(theSwitch);
        maq.addConexoesEntrada(theSwitch);
        theSwitch.addConexoesEntrada(maq);
        theSwitch.addConexoesSaida(maq);
    }

    public RedeDeFilasCloud build() {
        for (int i = 0; i < this.docOwners.getLength(); i++) {
            final Element owner = (Element) this.docOwners.item(i);
            this.users.put(owner.getAttribute("id"), 0.0);
        }
        //cria maquinas, mestres, internets e mestres dos clusters
        //Realiza leitura dos icones de máquina
        for (int i = 0; i < this.docMachines.getLength(); i++) {
            final Element maquina = (Element) this.docMachines.item(i);
            final Element id =
                    IconicoXML.getFirstTagElement(maquina, "icon_id");
            final int global = Integer.parseInt(id.getAttribute("global"));
            if (Utils.isValidMaster(maquina)) {
                final Element master =
                        IconicoXML.getFirstTagElement(maquina,
                                "master");
                final Element carac = IconicoXML.getFirstTagElement(maquina,
                        "characteristic");
                final Element proc =
                        IconicoXML.getFirstTagElement(carac, "process");
                final Element memoria = IconicoXML.getFirstTagElement(carac,
                        "memory");
                final Element disco = IconicoXML.getFirstTagElement(carac,
                        "hard_disk");
                final Element custo =
                        IconicoXML.getFirstTagElement(carac, "cost");
                //instancia o CS_VMM
                final CS_Processamento mestre = new CS_VMM(
                        maquina.getAttribute("id"),
                        maquina.getAttribute("owner"),
                        Double.parseDouble(proc.getAttribute("power")),
                        Double.parseDouble(memoria.getAttribute("size")),
                        Double.parseDouble(disco.getAttribute("size")),
                        Double.parseDouble(maquina.getAttribute("load")),
                        master.getAttribute("scheduler")/*Escalonador*/,
                        master.getAttribute("vm_alloc"));
                this.virtualMachineMasters.add(mestre);
                this.serviceCenters.put(global, mestre);
                //Contabiliza para o usuario poder computacional do mestre
                this.users.put(mestre.getProprietario(),
                        this.users.get(mestre.getProprietario()) + mestre.getPoderComputacional());
            } else {
                //acessa as características do máquina
                final Element caracteristica =
                        IconicoXML.getFirstTagElement(maquina,
                                "characteristic");
                final Element custo =
                        IconicoXML.getFirstTagElement(caracteristica,
                                "cost");
                final Element processamento =
                        IconicoXML.getFirstTagElement(caracteristica,
                                "process");
                final Element memoria =
                        IconicoXML.getFirstTagElement(caracteristica,
                                "memory");
                final Element disco =
                        IconicoXML.getFirstTagElement(caracteristica,
                                "hard_disk");
                //instancia um CS_MaquinaCloud
                final CS_MaquinaCloud maq = new CS_MaquinaCloud(
                        maquina.getAttribute("id"),
                        maquina.getAttribute("owner"),
                        Double.parseDouble(processamento.getAttribute(
                                "power")),
                        Integer.parseInt(processamento.getAttribute(
                                "number")),
                        Double.parseDouble(maquina.getAttribute("load")),
                        Double.parseDouble(memoria.getAttribute("size")),
                        Double.parseDouble(disco.getAttribute("size")),
                        Double.parseDouble(custo.getAttribute("cost_proc")),
                        Double.parseDouble(custo.getAttribute("cost_mem")),
                        Double.parseDouble(custo.getAttribute("cost_disk"))
                );
                this.machines.add(maq);
                this.serviceCenters.put(global, maq);
                this.users.put(maq.getProprietario(),
                        this.users.get(maq.getProprietario()) + maq.getPoderComputacional());
            }
        }
        //Realiza leitura dos icones de cluster
        for (int i = 0; i < this.docClusters.getLength(); i++) {
            final Element cluster = (Element) this.docClusters.item(i);
            final Element id =
                    IconicoXML.getFirstTagElement(cluster, "icon_id");
            final Element carac = IconicoXML.getFirstTagElement(cluster,
                    "characteristic");
            final Element proc =
                    IconicoXML.getFirstTagElement(carac, "process");
            final Element mem =
                    IconicoXML.getFirstTagElement(carac, "memory");
            final Element disc =
                    IconicoXML.getFirstTagElement(carac, "hard_disk");

            final int global = Integer.parseInt(id.getAttribute("global"));
            if (Boolean.parseBoolean(cluster.getAttribute("master"))) {
                final CS_VMM clust = new CS_VMM(
                        cluster.getAttribute("id"),
                        cluster.getAttribute("owner"),
                        Double.parseDouble(proc.getAttribute("power")),
                        Double.parseDouble(mem.getAttribute("size")),
                        Double.parseDouble(disc.getAttribute("size")),
                        0.0,
                        cluster.getAttribute("scheduler")/*Escalonador*/,
                        cluster.getAttribute("vm_alloc"));
                this.virtualMachineMasters.add(clust);
                this.serviceCenters.put(global, clust);
                //Contabiliza para o usuario poder computacional do mestre
                final int numeroEscravos =
                        Integer.parseInt(cluster.getAttribute(
                                "nodes"));
                final double total =
                        clust.getPoderComputacional() + (clust.getPoderComputacional() * numeroEscravos);
                this.users.put(clust.getProprietario(),
                        total + this.users.get(clust.getProprietario()));
                final CS_Switch Switch = new CS_Switch(
                        (cluster.getAttribute("id") + "switch"),
                        Double.parseDouble(cluster.getAttribute(
                                "bandwidth")),
                        0.0,
                        Double.parseDouble(cluster.getAttribute("latency")));
                this.links.add(Switch);
                clust.addConexoesEntrada(Switch);
                clust.addConexoesSaida(Switch);
                Switch.addConexoesEntrada(clust);
                Switch.addConexoesSaida(clust);
                for (int j = 0; j < numeroEscravos; j++) {
                    final Element caracteristica =
                            IconicoXML.getFirstTagElement(cluster,
                                    "characteristic");
                    final Element custo =
                            IconicoXML.getFirstTagElement(caracteristica,
                                    "cost");
                    final Element processamento =
                            IconicoXML.getFirstTagElement(caracteristica,
                                    "process");
                    final Element memoria =
                            IconicoXML.getFirstTagElement(caracteristica,
                                    "memory");
                    final Element disco =
                            IconicoXML.getFirstTagElement(caracteristica,
                                    "hard_disk");
                    final var maq =
                            cloudMachineFromElement(cluster, j,
                                    custo,
                                    processamento, memoria, disco);

                    connectMachineAndSwitch(Switch, maq);

                    maq.addMestre(clust);
                    clust.addEscravo(maq);
                    this.machines.add(maq);
                    //não adicionei referencia ao switch nem aos escrevos do
                    // cluster aos centros de serviços
                }
            } else {
                final CS_Switch Switch = new CS_Switch(
                        (cluster.getAttribute("id") + "switch"),
                        Double.parseDouble(cluster.getAttribute(
                                "bandwidth")),
                        0.0,
                        Double.parseDouble(cluster.getAttribute("latency")));
                this.links.add(Switch);
                this.serviceCenters.put(global, Switch);
                //Contabiliza para o usuario poder computacional do mestre
                final double total =
                        Double.parseDouble(cluster.getAttribute(
                                "power"))
                        * Integer.parseInt(cluster.getAttribute(
                                "nodes"
                        ));
                this.users.put(cluster.getAttribute("owner"),
                        total + this.users.get(cluster.getAttribute("owner")));
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
                            cloudMachineFromElement(cluster, j,
                                    custo, processamento, memoria, disco);
                    connectMachineAndSwitch(Switch, maq);
                    maqTemp.add(maq);
                    this.machines.add(maq);
                }
                this.clusterSlaves.put(Switch, maqTemp);
            }
        }

        //Realiza leitura dos icones de internet
        for (int i = 0; i < this.docInternet.getLength(); i++) {
            final Element inet = (Element) this.docInternet.item(i);
            final Element id =
                    IconicoXML.getFirstTagElement(inet, "icon_id");
            final int global = Integer.parseInt(id.getAttribute("global"));
            final CS_Internet net = new CS_Internet(
                    inet.getAttribute("id"),
                    Double.parseDouble(inet.getAttribute("bandwidth")),
                    Double.parseDouble(inet.getAttribute("load")),
                    Double.parseDouble(inet.getAttribute("latency")));
            this.internets.add(net);
            this.serviceCenters.put(global, net);
        }
        //cria os links e realiza a conexão entre os recursos
        for (int i = 0; i < this.docLinks.getLength(); i++) {
            final Element link = (Element) this.docLinks.item(i);

            final CS_Link cslink = new CS_Link(
                    link.getAttribute("id"),
                    Double.parseDouble(link.getAttribute("bandwidth")),
                    Double.parseDouble(link.getAttribute("load")),
                    Double.parseDouble(link.getAttribute("latency")));
            this.links.add(cslink);

            //adiciona entrada e saida desta conexão
            final Element connect =
                    IconicoXML.getFirstTagElement(link, "connect");
            final Vertice origem =
                    (Vertice) this.serviceCenters.get(Integer.parseInt(connect.getAttribute("origination")));
            final Vertice destino =
                    (Vertice) this.serviceCenters.get(Integer.parseInt(connect.getAttribute("destination")));
            cslink.setConexoesSaida((CentroServico) destino);
            destino.addConexoesEntrada(cslink);
            cslink.setConexoesEntrada((CentroServico) origem);
            origem.addConexoesSaida(cslink);
        }
        //adiciona os escravos aos mestres
        for (int i = 0; i < this.docMachines.getLength(); i++) {
            final Element maquina = (Element) this.docMachines.item(i);
            final Element id =
                    IconicoXML.getFirstTagElement(maquina, "icon_id");
            final int global = Integer.parseInt(id.getAttribute("global"));
            if (Utils.isValidMaster(maquina)) {
                final Element master =
                        IconicoXML.getFirstTagElement(maquina,
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
                            //trecho de debbuging
                            System.out.println(maqTemp.getId() + " " +
                                               "adicionou " +
                                               "como mestre: " + mestre.getId());
                            //fim dbg
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

        //Realiza leitura dos ícones de máquina virtual
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

        for (final Map.Entry<String, Double> entry : this.users.entrySet()) {
            this.owners.add(entry.getKey());
            this.powers.add(entry.getValue());
        }
        //cria as métricas de usuarios para cada mestre
        for (final CS_Processamento mestre : this.virtualMachineMasters) {
            final CS_VMM mst = (CS_VMM) mestre;
            final MetricasUsuarios mu = new MetricasUsuarios();
            mu.addAllUsuarios(this.owners, this.powers);
            mst.getEscalonador().setMetricaUsuarios(mu);
        }
        final RedeDeFilasCloud rdf =
                new RedeDeFilasCloud(this.virtualMachineMasters,
                        this.machines, this.virtualMachines,
                        this.links,
                        this.internets);
        //cria as métricas de usuarios globais da rede de filas
        final MetricasUsuarios mu = new MetricasUsuarios();
        mu.addAllUsuarios(this.owners, this.powers);
        rdf.setUsuarios(this.owners);
        return rdf;
    }
}
