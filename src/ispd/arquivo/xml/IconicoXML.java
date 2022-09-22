package ispd.arquivo.xml;

import ispd.gui.PickModelTypeDialog;
import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.Link;
import ispd.gui.iconico.grade.Machine;
import ispd.gui.iconico.grade.VirtualMachine;
import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaList;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaTrace;
import ispd.motor.carga.GerarCarga;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.motor.filas.servidores.implementacao.Vertice;
import ispd.motor.metricas.MetricasUsuarios;
import ispd.utils.ValidaValores;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Realiza manupulações com o arquivo xml do modelo icônico
 *
 * @author denison
 */
public class IconicoXML {
    private static final Element[] NO_CHILDREN = {};
    private static final Object[][] NO_ATTRS = {};
    private static final int DEFAULT_MODEL_TYPE = -1;
    private final Document doc =
            Objects.requireNonNull(ManipuladorXML.novoDocumento());
    private final Element system = this.doc.createElement("system");
    private Element load = null; // TODO: Don't like this

    public IconicoXML() {
        this(IconicoXML.DEFAULT_MODEL_TYPE);
    }

    public IconicoXML(final int modelType) {
        this.system.setAttribute("version",
                IconicoXML.getVersionForModelType(modelType));
        this.doc.appendChild(this.system);
    }

    /**
     * @throws IllegalArgumentException if modelType is not in -1, 0, 1 or 2
     */
    private static String getVersionForModelType(final int modelType) {
        return switch (modelType) {
            case PickModelTypeDialog.GRID -> "2.1";
            case PickModelTypeDialog.IAAS -> "2.2";
            case PickModelTypeDialog.PAAS -> "2.3";
            case IconicoXML.DEFAULT_MODEL_TYPE -> "1.2";
            default -> throw new IllegalArgumentException(
                    "Invalid model type " + modelType);
        };
    }

    /**
     * Este método sobrescreve ou cria arquivo xml do modelo iconico
     *
     * @param documento modelo iconico
     * @param arquivo   local que será salvo
     * @return indica se arquivo foi salvo corretamente
     */
    public static boolean escrever(final Document documento,
                                   final File arquivo) {
        return ManipuladorXML.escrever(documento, arquivo, "iSPD.dtd", false);
    }

    /**
     * Realiza a leitura de um arquivo xml contendo o modelo iconico
     * especificado pelo iSPD.dtd
     *
     * @param xmlFile endereço do arquivo xml
     * @return modelo iconico obtido do arquivo
     */
    public static Document ler(final File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        return ManipuladorXML.ler(xmlFile, "iSPD.dtd");
    }

    /**
     * Verifica se modelo está completo
     *
     * @throws IllegalArgumentException
     */
    public static void validarModelo(final Document doc) {
        if (IconicoXML.isElementEmpty(doc, "owner")) {
            throw new IllegalArgumentException("The model has no users.");
        }

        if (IconicoXML.isElementEmpty(doc, "machine") &&
            IconicoXML.isElementEmpty(doc, "cluster")) {
            throw new IllegalArgumentException("The model has no icons.");
        }

        if (IconicoXML.isElementEmpty(doc, "load")) {
            throw new IllegalArgumentException(
                    "One or more  workloads have not been configured.");
        }

        final boolean hasNoValidMaster =
                IconicoXML.docElementStream(doc, "machine")
                        .noneMatch(Utils::isValidMaster);

        if (hasNoValidMaster) {
            throw new IllegalArgumentException(
                    "One or more parameters have not been configured.");
        }
    }

    private static boolean isElementEmpty(
            final Document doc, final String tag) {
        return doc.getElementsByTagName(tag).getLength() == 0;
    }

    private static Stream<Element> docElementStream(
            final Document doc, final String tag) {
        return Utils.elementStreamOf(doc.getElementsByTagName(tag));
    }

    /**
     * Convert an iconic model into a queue network, usable in motor.
     *
     * @param model Object from xml with modeled computational grid
     * @return Simulable queue network, in accordance to given model
     */
    public static RedeDeFilas newRedeDeFilas(final Document model) {
        return new QueueNetworkBuilder(model).build();
    }

    public static RedeDeFilasCloud newRedeDeFilasCloud(final Document model) {
        return CloudQueueNetworkBuilder.build(model);
    }

    private static CS_MaquinaCloud cloudMachineFromElement(
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

    private static void connectMachineAndSwitch(
            final CS_Switch theSwitch, final CS_MaquinaCloud maq) {
        maq.addConexoesSaida(theSwitch);
        maq.addConexoesEntrada(theSwitch);
        theSwitch.addConexoesEntrada(maq);
        theSwitch.addConexoesSaida(maq);
    }

    /**
     * Obtem a configuração da carga de trabalho contida em um modelo iconico
     *
     * @param modelo contem conteudo recuperado de um arquivo xml
     * @return carga de trabalho contida no modelo
     */
    public static GerarCarga newGerarCarga(final Document modelo) {
        NodeList cargas = modelo.getElementsByTagName("load");
        GerarCarga cargasConfiguracao = null;
        //Realiza leitura da configuração de carga do modelo
        if (cargas.getLength() != 0) {
            final Element cargaAux = (Element) cargas.item(0);
            cargas = cargaAux.getElementsByTagName("random");
            if (cargas.getLength() != 0) {
                final Element carga = (Element) cargas.item(0);
                final int numeroTarefas = Integer.parseInt(carga.getAttribute(
                        "tasks"));
                final int timeOfArrival = Integer.parseInt(carga.getAttribute(
                        "time_arrival"));
                int minComputacao = 0;
                int maxComputacao = 0;
                int AverageComputacao = 0;
                double ProbabilityComputacao = 0;
                int minComunicacao = 0;
                int maxComunicacao = 0;
                int AverageComunicacao = 0;
                double ProbabilityComunicacao = 0;
                final NodeList size = carga.getElementsByTagName("size");
                for (int i = 0; i < size.getLength(); i++) {
                    final Element size1 = (Element) size.item(i);
                    if ("computing".equals(size1.getAttribute("type"))) {
                        minComputacao = Integer.parseInt(size1.getAttribute(
                                "minimum"));
                        maxComputacao = Integer.parseInt(size1.getAttribute(
                                "maximum"));
                        AverageComputacao =
                                Integer.parseInt(size1.getAttribute("average"));
                        ProbabilityComputacao =
                                Double.parseDouble(size1.getAttribute(
                                        "probability"));
                    } else if ("communication".equals(
                            size1.getAttribute("type"))) {
                        minComunicacao = Integer.parseInt(size1.getAttribute(
                                "minimum"));
                        maxComunicacao = Integer.parseInt(size1.getAttribute(
                                "maximum"));
                        AverageComunicacao =
                                Integer.parseInt(size1.getAttribute("average"));
                        ProbabilityComunicacao =
                                Double.parseDouble(size1.getAttribute(
                                        "probability"));
                    }
                }
                cargasConfiguracao = new CargaRandom(numeroTarefas,
                        minComputacao, maxComputacao, AverageComputacao,
                        ProbabilityComputacao, minComunicacao, maxComunicacao
                        , AverageComunicacao, ProbabilityComunicacao,
                        timeOfArrival);
            }
            cargas = cargaAux.getElementsByTagName("node");
            if (cargas.getLength() != 0) {
                final List<CargaForNode> tarefasDoNo =
                        new ArrayList<>();
                for (int i = 0; i < cargas.getLength(); i++) {
                    final Element carga = (Element) cargas.item(i);
                    final String aplicacao = carga.getAttribute("application");
                    final String proprietario = carga.getAttribute("owner");
                    final String escalonador = carga.getAttribute("id_master");
                    final int numeroTarefas =
                            Integer.parseInt(carga.getAttribute(
                                    "tasks"));
                    double minComputacao = 0;
                    double maxComputacao = 0;
                    double minComunicacao = 0;
                    double maxComunicacao = 0;
                    final NodeList size = carga.getElementsByTagName("size");
                    for (int j = 0; j < size.getLength(); j++) {
                        final Element size1 = (Element) size.item(j);
                        if ("computing".equals(size1.getAttribute("type"))) {
                            minComputacao =
                                    Double.parseDouble(size1.getAttribute(
                                            "minimum"));
                            maxComputacao =
                                    Double.parseDouble(size1.getAttribute(
                                            "maximum"));
                        } else if ("communication".equals(
                                size1.getAttribute("type"))) {
                            minComunicacao =
                                    Double.parseDouble(size1.getAttribute(
                                            "minimum"));
                            maxComunicacao =
                                    Double.parseDouble(size1.getAttribute(
                                            "maximum"));
                        }
                    }
                    final CargaForNode item = new CargaForNode(aplicacao,
                            proprietario, escalonador, numeroTarefas,
                            maxComputacao, minComputacao, maxComunicacao,
                            minComunicacao);
                    tarefasDoNo.add(item);
                }
                cargasConfiguracao = new CargaList(tarefasDoNo,
                        GerarCarga.FORNODE);
            }
            final NodeList trace = cargaAux.getElementsByTagName("trace");
            if (trace.getLength() != 0) {
                final Element carga = (Element) trace.item(0);
                final File filepath = new File(carga.getAttribute("file_path"));
                final int taskCount = Integer.parseInt(carga.getAttribute(
                        "tasks"));
                final String formato = carga.getAttribute("format");
                if (filepath.exists()) {
                    cargasConfiguracao = new CargaTrace(filepath, taskCount
                            , formato);
                }
            }
        }
        return cargasConfiguracao;
    }

    public static void newGrade(
            final Document doc,
            final Collection<? super Vertex> vertices,
            final Collection<? super Edge> edges) {
        final var icons = new HashMap<Integer, Object>(0);

        final var machines = doc.getElementsByTagName("machine");
        final var clusters = doc.getElementsByTagName("cluster");
        final var internet = doc.getElementsByTagName("internet");
        final var links = doc.getElementsByTagName("link");
        //Realiza leitura dos icones de cluster
        for (int i = 0; i < clusters.getLength(); i++) {
            final Element cluster = (Element) clusters.item(i);
            final Element pos =
                    IconicoXML.getFirstTagElement(cluster, "position");
            final int x = Integer.parseInt(pos.getAttribute("x"));
            final int y = Integer.parseInt(pos.getAttribute("y"));
            final Element id =
                    IconicoXML.getFirstTagElement(cluster, "icon_id");
            final int global = Integer.parseInt(id.getAttribute("global"));
            final int local = Integer.parseInt(id.getAttribute("local"));
            final Cluster clust = new Cluster(x, y, local, global,
                    Double.parseDouble(cluster.getAttribute("power")));
            clust.setSelected(false);
            vertices.add(clust);
            icons.put(global, clust);
            clust.getId().setName(cluster.getAttribute("id"));
            ValidaValores.addNomeIcone(clust.getId().getName());
            clust.setComputationalPower(Double.parseDouble(cluster.getAttribute("power")));
            IconicoXML.setGridItemCharacteristics(clust, cluster);
            clust.setSlaveCount(Integer.parseInt(cluster.getAttribute("nodes")));
            clust.setBandwidth(Double.parseDouble(cluster.getAttribute(
                    "bandwidth")));
            clust.setLatency(Double.parseDouble(cluster.getAttribute("latency"
            )));
            clust.setSchedulingAlgorithm(cluster.getAttribute("scheduler"));
            clust.setVmmAllocationPolicy(cluster.getAttribute("vm_alloc"));
            clust.setOwner(cluster.getAttribute("owner"));
            clust.setMaster(Boolean.parseBoolean(cluster.getAttribute("master"
            )));
        }
        //Realiza leitura dos icones de internet
        for (int i = 0; i < internet.getLength(); i++) {
            final Element inet = (Element) internet.item(i);
            final Element pos =
                    IconicoXML.getFirstTagElement(inet, "position");
            final int x = Integer.parseInt(pos.getAttribute("x"));
            final int y = Integer.parseInt(pos.getAttribute("y"));
            final Element id =
                    IconicoXML.getFirstTagElement(inet, "icon_id");
            final int global = Integer.parseInt(id.getAttribute("global"));
            final int local = Integer.parseInt(id.getAttribute("local"));
            final Internet net = new Internet(x, y, local, global);
            net.setSelected(false);
            vertices.add(net);
            icons.put(global, net);
            net.getId().setName(inet.getAttribute("id"));
            ValidaValores.addNomeIcone(net.getId().getName());
            net.setBandwidth(Double.parseDouble(inet.getAttribute("bandwidth")));
            net.setLoadFactor(Double.parseDouble(inet.getAttribute("load")));
            net.setLatency(Double.parseDouble(inet.getAttribute("latency")));
        }
        //Realiza leitura dos icones de máquina
        for (int i = 0; i < machines.getLength(); i++) {
            final Element maquina = (Element) machines.item(i);
            if (maquina.getElementsByTagName("master").getLength() <= 0) {
                final Machine maq = IconicoXML.createMachineMaybe(icons,
                        maquina);
                vertices.add(maq);
                IconicoXML.doSomethingUsefulAsWell(maquina, maq);
            } else {
                IconicoXML.createMachineMaybe(icons, maquina);
            }
        }
        //Realiza leitura dos mestres
        for (int i = 0; i < machines.getLength(); i++) {
            final Element maquina = (Element) machines.item(i);
            if (Utils.isValidMaster(maquina)) {
                final Element id =
                        IconicoXML.getFirstTagElement(maquina, "icon_id");
                final int global = Integer.parseInt(id.getAttribute("global"));
                final Machine maq = (Machine) icons.get(global);
                vertices.add(maq);
                IconicoXML.doSomethingUsefulAsWell(maquina, maq);
                final Element master = IconicoXML.getFirstTagElement(maquina,
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
                    IconicoXML.getFirstTagElement(link, "icon_id");
            final int global = Integer.parseInt(id.getAttribute("global"));
            final int local = Integer.parseInt(id.getAttribute("local"));
            final Element connect =
                    IconicoXML.getFirstTagElement(link, "connect");
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

    static Element getFirstTagElement(
            final @NotNull Element element, final String tag) {
        return (Element) element.getElementsByTagName(tag).item(0);
    }

    private static void setGridItemCharacteristics(
            final GridItem item, final Element elem) {
        if (IconicoXML.hasCharacteristics(elem))
            return;

        final var characteristics =
                IconicoXML.getFirstTagElement(elem, "characteristic");
        final var process =
                IconicoXML.getFirstTagElement(characteristics, "process");

        // TODO: Extract cost interface in both Cluster and Machine
        final var power = Double.parseDouble(process.getAttribute(
                "power"));
        final var cores = process.getAttribute(
                "number");
        final var memorySize = IconicoXML.getFirstTagElement(characteristics, "memory").getAttribute("size");
        final var diskSize = IconicoXML.getFirstTagElement(characteristics, "hard_disk").getAttribute("size");

        if (item instanceof Cluster cluster) {
            cluster.setComputationalPower(power);
            cluster.setCoreCount(Integer.valueOf(cores));

            cluster.setRam(Double.parseDouble(memorySize));
            cluster.setHardDisk(Double.parseDouble(diskSize));

            if (!IconicoXML.hasCostProperties(characteristics)) {
                return;
            }

            final Element cost =
                    IconicoXML.getFirstTagElement(characteristics, "cost");
            cluster.setCostPerProcessing(Double.parseDouble(cost.getAttribute(
                    "cost_proc")));
            cluster.setCostPerMemory(Double.parseDouble(cost.getAttribute(
                    "cost_mem")));
            cluster.setCostPerDisk(Double.parseDouble(cost.getAttribute(
                    "cost_disk")));
        } else if (item instanceof Machine machine) {


            machine.setComputationalPower(power);
            machine.setCoreCount(Integer.valueOf(cores));
            machine.setRam(Double.valueOf(memorySize));

            machine.setHardDisk(Double.valueOf(diskSize));

            if (!IconicoXML.hasCostProperties(characteristics)) {
                return;
            }

            final Element cost =
                    IconicoXML.getFirstTagElement(characteristics, "cost");
            machine.setCostPerProcessing(Double.valueOf(cost.getAttribute(
                    "cost_proc")));
            machine.setCostPerMemory(Double.valueOf(cost.getAttribute(
                    "cost_mem")));
            machine.setCostPerDisk(Double.valueOf(cost.getAttribute(
                    "cost_disk")));

        }
    }

    private static Machine createMachineMaybe(
            final Map<? super Integer, Object> icons, final Element machine) {
        final Element pos = IconicoXML.getFirstTagElement(machine, "position");
        final int x = Integer.parseInt(pos.getAttribute("x"));
        final int y = Integer.parseInt(pos.getAttribute("y"));
        final Element id =
                IconicoXML.getFirstTagElement(machine, "icon_id");
        final int global = Integer.parseInt(id.getAttribute("global"));
        final int local = Integer.parseInt(id.getAttribute("local"));
        final var maq = new Machine(x, y, local, global,
                Double.parseDouble(machine.getAttribute("energy")));
        maq.setSelected(false);
        icons.put(global, maq);
        return maq;
    }

    private static void doSomethingUsefulAsWell(
            final Element elem, final Machine machine) {
        final var newName = elem.getAttribute("id");
        machine.getId().setName(newName);
        ValidaValores.addNomeIcone(machine.getId().getName());

        machine.setComputationalPower(Double.parseDouble(elem.getAttribute(
                "power")));
        IconicoXML.setGridItemCharacteristics(machine, elem);
        machine.setLoadFactor(Double.parseDouble(elem.getAttribute(
                "load")));
        machine.setOwner(elem.getAttribute("owner"));
    }

    private static boolean hasCharacteristics(final Element elem) {
        return elem.getElementsByTagName("characteristic").getLength() <= 0;
    }

    private static boolean hasCostProperties(final Element characteristics) {
        return characteristics.getElementsByTagName("cost").getLength() > 0;
    }

    public static HashSet<String> newSetUsers(final Document doc) {
        return IconicoXML.docElementStream(doc, "owner")
                .map(IconicoXML::elementId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static String elementId(final Element e) {
        return e.getAttribute("id");
    }

    public static List<String> newListUsers(final Document doc) {
        return IconicoXML.docElementStream(doc, "owner")
                .map(o -> o.getAttribute("id"))
                .toList();
    }

    public static HashSet<VirtualMachine> newListVirtualMachines(final Document doc) {
        return IconicoXML.docElementStream(doc, "virtualMac")
                .map(IconicoXML::virtualMachineFromElement)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static VirtualMachine virtualMachineFromElement(final Element owner) {
        return new VirtualMachine(
                owner.getAttribute("id"),
                owner.getAttribute("owner"),
                owner.getAttribute("vmm"),
                Integer.parseInt(owner.getAttribute("power")),
                Double.parseDouble(owner.getAttribute("mem_alloc")),
                Double.parseDouble(owner.getAttribute("disk_alloc")),
                owner.getAttribute("op_system")
        );
    }

    public static Document[] clone(final File file, final int number)
            throws ParserConfigurationException, IOException, SAXException {

        final var builder = IconicoXML.getCloningBuilder();

        final var docs = new Document[number];

        for (int i = 0; i < number; i++) {
            // TODO: setEntityResolver outside loop?
            builder.setEntityResolver(new CloningEntityResolver());
            docs[i] = builder.parse(file);
        }

        return docs;
    }

    private static DocumentBuilder getCloningBuilder() throws ParserConfigurationException {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        return factory.newDocumentBuilder();
    }

    public static HashMap<String, Double> newListPerfil(final Document doc) {
        final var owners = doc.getElementsByTagName("owner");

        return IntStream.range(0, owners.getLength())
                .mapToObj(owners::item)
                .map(Element.class::cast)
                .collect(IconicoXML.mapOwnerToPowerLimit());
    }

    private static Collector<Element, ?, HashMap<String, Double>> mapOwnerToPowerLimit() {
        return Collectors.toMap(
                o -> o.getAttribute("id"),
                o -> Double.parseDouble(o.getAttribute("powerlimit")),
                (prev, next) -> next,
                HashMap::new
        );
    }

    public static int getIconGlobalId(final Element icon) {
        return IconicoXML.getIntValueAttribute((
                IconicoXML.getFirstTagElement(icon, "icon_id")
        ), "global");
    }

    public static int getIntValueAttribute(final Element elem,
                                           final String attr) {
        return Integer.parseInt(elem.getAttribute(attr));
    }

    public static CS_Mestre masterFromElement(final Element elem) {
        return new CS_Mestre(
                elem.getAttribute("id"),
                elem.getAttribute("owner"),
                Utils.getValueAttribute(elem, "power"),
                Utils.getValueAttribute(elem, "load"),
                IconicoXML.getScheduler(elem),
                Utils.getValueAttribute(elem, "energy")
        );
    }

    private static String getScheduler(final Element machine) {
        return IconicoXML.getFirstTagElement(machine, "master").getAttribute(
                "scheduler");
    }

    public static CS_Maquina machineFromElement(final Element elem) {
        return new CS_Maquina(
                elem.getAttribute("id"),
                elem.getAttribute("owner"),
                Utils.getValueAttribute(elem, "power"),
                1,
                Utils.getValueAttribute(elem, "load"),
                Utils.getValueAttribute(elem, "energy")
        );
    }

    public static CS_Switch switchFromElement(final Element elem) {
        return new CS_Switch(
                elem.getAttribute("id"),
                Utils.getValueAttribute(elem, "bandwidth"),
                0.0,
                Utils.getValueAttribute(elem, "latency")
        );
    }

    public static boolean isMaster(final Element cluster) {
        return Boolean.parseBoolean(cluster.getAttribute("master"));
    }

    public static CS_Mestre clusterFromElement(final Element elem) {
        return new CS_Mestre(
                elem.getAttribute("id"),
                elem.getAttribute("owner"),
                Utils.getValueAttribute(elem, "power"),
                0.0,
                elem.getAttribute("scheduler"),
                Utils.getValueAttribute(elem, "energy")
        );
    }

    public static void connectMachineAndSwitch(final CS_Maquina machine,
                                               final CS_Switch theSwitch) {
        machine.addConexoesSaida(theSwitch);
        machine.addConexoesEntrada(theSwitch);

        theSwitch.addConexoesEntrada(machine);
        theSwitch.addConexoesSaida(machine);
    }

    public static CS_Maquina machineFromElement(final Element cluster,
                                                final int id) {
        return new CS_Maquina(
                cluster.getAttribute("id"),
                cluster.getAttribute("owner"),
                Utils.getValueAttribute(cluster, "power"),
                1,
                0.0,
                id + 1,
                Utils.getValueAttribute(cluster, "energy")
        );
    }

    public static CS_Link linkFromElement(final Element elem) {
        return new CS_Link(
                elem.getAttribute("id"),
                Utils.getValueAttribute(elem, "bandwidth"),
                Utils.getValueAttribute(elem, "load"),
                Utils.getValueAttribute(elem, "latency")
        );
    }

    public static void connectClusterAndSwitch(final CS_Mestre cluster,
                                               final CS_Switch theSwitch) {
        cluster.addConexoesEntrada(theSwitch);
        cluster.addConexoesSaida(theSwitch);
        theSwitch.addConexoesEntrada(cluster);
        theSwitch.addConexoesSaida(cluster);
    }

    public void addUsers(final Collection<String> users,
                         final Map<String, Double> limits) {
        // TODO: Iterate over the HashMap instead?
        users.stream()
                .map(user -> this.anElement("owner",
                        "id", user,
                        "powerlimit", limits.get(user)
                ))
                .forEach(this.system::appendChild);
    }

    private Element anElement(
            final String name,
            final String k1, final Object v1,
            final String k2, final Object v2) {
        return this.anElement(name, new Object[][] {
                { k1, v1 },
                { k2, v2 },
        });
    }

    private Element anElement(
            final String name, final Object[][] attrs) {
        return this.anElement(name, attrs, IconicoXML.NO_CHILDREN);
    }

    private Element anElement(
            final String name, final Object[][] attrs,
            final Node[] children) {
        final var e = this.doc.createElement(name);

        for (final var attr : attrs) {
            final var key = attr[0];
            final var value = attr[1];
            e.setAttribute((String) key, value.toString());
        }

        Arrays.stream(children)
                .forEach(e::appendChild);

        return e;
    }

    public void addInternet(
            final int x, final int y,
            final int idLocal, final int idGlobal, final String name,
            final double bandwidth, final double internetLoad,
            final double latency) {
        this.system.appendChild(this.anElement(
                "internet", new Object[][] {
                        { "id", name },
                        { "bandwidth", bandwidth },
                        { "load", internetLoad },
                        { "latency", latency },
                }, new Element[] {
                        this.aPositionElement(x, y),
                        this.anIconIdElement(idGlobal, idLocal),
                }
        ));
    }

    public void addCluster(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Integer slaveCount,
            final Double power, final Integer coreCount,
            final Double memory, final Double disk,
            final Double bandwidth, final Double latency,
            final String scheduler,
            final String owner,
            final Boolean isMaster) {
        this.system.appendChild(this.anElement(
                "cluster", new Object[][] {
                        { "nodes", slaveCount },
                        { "power", power },
                        { "bandwidth", bandwidth },
                        { "latency", latency },
                        { "scheduler", scheduler },
                        { "owner", owner },
                        { "master", isMaster },
                        { "id", name },
                }, new Node[] {
                        this.aPositionElement(x, y),
                        this.anIconIdElement(globalId, localId),
                        this.newCharacteristic(
                                power, coreCount, memory, disk,
                                0.0, 0.0, 0.0
                        ),
                }
        ));
    }

    public void addClusterIaaS(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Integer slaveCount,
            final Double power, final Integer coreCount,
            final Double memory, final Double disk,
            final Double bandwidth, final Double latency,
            final String scheduler, final String vmAlloc,
            final Double processingCost, final Double memoryCost,
            final Double diskCost,
            final String owner, final Boolean isMaster) {
        this.system.appendChild(this.anElement(
                "cluster", new Object[][] {
                        { "id", name },
                        { "nodes", slaveCount },
                        { "power", power },
                        { "bandwidth", bandwidth },
                        { "latency", latency },
                        { "scheduler", scheduler },
                        { "vm_alloc", vmAlloc },
                        { "owner", owner },
                        { "master", isMaster },
                }, new Node[] {
                        this.aPositionElement(x, y),
                        this.anIconIdElement(globalId, localId),
                        this.newCharacteristic(
                                power, coreCount, memory, disk,
                                processingCost, memoryCost, diskCost
                        )
                }
        ));
    }

    private Element anIconIdElement(final int global, final int local) {
        return this.anElement("icon_id", "global", global, "local", local);
    }

    private Node newCharacteristic(final Double power, final Integer coreCount,
                                   final Double memory, final Double disk,
                                   final Double processingCost,
                                   final Double memoryCost,
                                   final Double diskCost) {
        return this.anElement(
                "characteristic", IconicoXML.NO_ATTRS, new Element[] {
                        this.anElement("process",
                                "power", power,
                                "number", coreCount),
                        this.anElement("memory", "size", memory),
                        this.anElement("hard_disk", "size", disk),
                        this.anElement("cost", new Object[][] {
                                { "cost_proc", processingCost },
                                { "cost_mem", memoryCost },
                                { "cost_disk", diskCost },
                        }),
                }
        );
    }

    public void addMachine(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Double power, final Double occupancy,
            final String scheduler, final String owner,
            final Integer coreCount, final Double memory, final Double disk,
            final boolean isMaster, final Collection<Integer> slaves,
            final Double energy) {
        this.addMachineInner(x, y, localId, globalId, name,
                power, occupancy, scheduler, owner, coreCount, memory, disk,
                null, null, null, isMaster, slaves,
                new Object[][] { { "energy", energy } }, IconicoXML.NO_ATTRS
        );
    }

    private Node newCharacteristic(final Double power, final Integer coreCount,
                                   final Double memory, final Double disk) {
        return this.anElement(
                "characteristic", IconicoXML.NO_ATTRS, new Element[] {
                        this.anElement("process",
                                "power", power,
                                "number", coreCount
                        ),
                        this.anElement("memory", "size", memory),
                        this.anElement("hard_disk", "size", disk),
                });
    }

    private Element anElement(
            final String name, final String key, final Object value) {
        return this.anElement(name, new Object[][] {
                { key, value },
        });
    }

    public void addMachine(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Double power, final Double occupancy,
            final String scheduler, final String owner,
            final Integer coreCount, final Double memory, final Double disk,
            final boolean isMaster, final Collection<Integer> slaves) {
        this.addMachineInner(x, y, localId, globalId, name,
                power, occupancy, scheduler, owner, coreCount, memory, disk,
                0.0, 0.0, 0.0, isMaster, slaves,
                IconicoXML.NO_ATTRS, IconicoXML.NO_ATTRS
        );
    }

    public void addMachineIaaS(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Double power, final Double occupancy,
            final String vmAlloc, final String scheduler, final String owner,
            final Integer coreCount, final Double memory, final Double disk,
            final Double costPerProcessing,
            final Double costPerMemory,
            final Double costPerDisk,
            final boolean isMaster, final Collection<Integer> slaves) {
        this.addMachineInner(x, y, localId, globalId, name,
                power, occupancy, scheduler, owner, coreCount, memory, disk,
                costPerProcessing, costPerMemory, costPerDisk, isMaster, slaves,
                IconicoXML.NO_ATTRS, new Object[][] { { "vm_alloc", vmAlloc }, }
        );
    }

    private void addMachineInner(
            final Integer x, final Integer y,
            final Integer localId, final Integer globalId, final String name,
            final Double power, final Double occupancy,
            final String scheduler, final String owner,
            final Integer coreCount, final Double memory, final Double disk,
            final Double costPerProcessing,
            final Double costPerMemory,
            final Double costPerDisk,
            final boolean isMaster, final Collection<Integer> slaves,
            final Object[][] extraAttrs, final Object[][] extraMasterAttrs) {
        final var attrs = Arrays.asList(new Object[][] {
                { "id", name },
                { "power", power },
                { "load", occupancy },
                { "owner", owner },
        });

        attrs.addAll(Arrays.asList(extraAttrs));

        final Node characteristic;

        // TODO: Generalize method newCharacteristic instead, this is ugly
        if (costPerProcessing != null) {
            characteristic = this.newCharacteristic(
                    power, coreCount, memory, disk,
                    costPerProcessing, costPerMemory, costPerDisk
            );
        } else {
            characteristic = this.newCharacteristic(
                    power, coreCount, memory, disk
            );
        }

        final var machine = this.anElement(
                "machine", attrs.toArray(Object[][]::new), new Node[] {
                        this.aPositionElement(x, y),
                        this.anIconIdElement(globalId, localId),
                        characteristic,
                }
        );

        if (isMaster) {
            machine.appendChild(this.aMasterElement(
                    scheduler, slaves, extraMasterAttrs
            ));
        }

        this.system.appendChild(machine);
    }

    private Element aMasterElement(final String scheduler,
                                   final Collection<Integer> slaves,
                                   final Object[][] extraAttrs) {
        final var attrs = Arrays.asList(new Object[][] {
                { "scheduler", scheduler },
        });

        attrs.addAll(Arrays.asList(extraAttrs));

        return this.anElement(
                "master", attrs.toArray(Object[][]::new),
                slaves.stream()
                        .map(this::aSlaveElement)
                        .toArray(Element[]::new)
        );
    }

    private Element aSlaveElement(final Integer id) {
        return this.anElement("slave", "id", id);
    }

    public void addLink(
            final int x0, final int y0,
            final int x1, final int y1,
            final int localId, final int globalId,
            final String name, final double bandwidth,
            final double linkLoad, final double latency,
            final int origination, final int destination) {
        // TODO: During refactoring steps were reordered. Need to test.
        this.system.appendChild(this.anElement(
                "link", new Object[][] {
                        { "id", name },
                        { "bandwidth", bandwidth },
                        { "load", linkLoad },
                        { "latency", latency },
                }, new Element[] {
                        this.anElement("connect",
                                "origination", origination,
                                "destination", destination),
                        this.aPositionElement(x0, y0),
                        this.aPositionElement(x1, y1),
                        this.anIconIdElement(globalId, localId),
                }
        ));
    }

    private Element aPositionElement(final int x, final int y) {
        return this.anElement("position", "x", x, "y", y);
    }

    public void addVirtualMachines(
            final String id, final String owner, final String vmm,
            final int power, final double memory, final double disk,
            final String os) {
        this.system.appendChild(this.anElement(
                "virtualMac", new Object[][] {
                        { "id", id },
                        { "owner", owner },
                        { "vmm", vmm },
                        { "power", power },
                        { "mem_alloc", memory },
                        { "disk_alloc", disk },
                        { "op_system", os },
                }
        ));
    }

    public void setLoadRandom(
            final Integer taskCount, final Integer arrivalTime,
            final Integer compMax, final Integer compAvg,
            final Integer compMin, final Double compProb,
            final Integer commMax, final Integer commAvg,
            final Integer commMin, final Double commProb) {
        this.addElementToLoad(this.anElement(
                "random", new Object[][] {
                        { "tasks", taskCount },
                        { "time_arrival", arrivalTime },
                }, new Element[] {
                        this.anElement("size", new Object[][] {
                                { "type", "computing" },
                                { "maximum", compMax },
                                { "average", compAvg },
                                { "minimum", compMin },
                                { "probability", compProb },
                        }),
                        this.anElement("size", new Object[][] {
                                { "type", "communication" },
                                { "maximum", commMax },
                                { "average", commAvg },
                                { "minimum", commMin },
                                { "probability", commProb },
                        }),
                }
        ));
    }

    private void addElementToLoad(final Node elem) {
        this.createLoadIfNull();
        this.load.appendChild(elem);
    }

    private void createLoadIfNull() {
        if (this.load == null) {
            this.load = this.doc.createElement("load");
            this.system.appendChild(this.load);
        }
    }

    public void addLoadNo(
            final String application,
            final String owner,
            final String masterId,
            final Integer taskCount,
            final Double maxComp, final Double minComp,
            final Double maxComm, final Double minComm) {
        this.addElementToLoad(this.anElement(
                "node", new Object[][] {
                        { "application", application },
                        { "owner", owner },
                        { "id_master", masterId },
                        { "tasks", taskCount },
                }, new Element[] {
                        this.anElement("size", new Object[][] {
                                { "type", "computing" },
                                { "maximum", maxComp },
                                { "minimum", minComp },
                        }),
                        this.anElement("size", new Object[][] {
                                { "type", "communication" },
                                { "maximum", maxComm },
                                { "minimum", minComm },
                        }),
                }
        ));
    }

    public void setLoadTrace(
            final String file, final String tasks, final String format) {
        this.addElementToLoad(this.anElement(
                "trace", new String[][] {
                        { "file_path", file },
                        { "tasks", tasks },
                        { "format", format },
                }
        ));
    }

    public Document getDescricao() {
        return this.doc;
    }

    static class CloudQueueNetworkBuilder {
        CloudQueueNetworkBuilder(final Document model) {

        }

        private static RedeDeFilasCloud build(final Document model) {
            final NodeList docmaquinas = model.getElementsByTagName("machine");
            final NodeList docclusters = model.getElementsByTagName("cluster");
            final NodeList docinternet = model.getElementsByTagName("internet");
            final NodeList doclinks = model.getElementsByTagName("link");
            final NodeList owners = model.getElementsByTagName("owner");
            //---v incluindo as máquinas virtuais
            final NodeList docVMs = model.getElementsByTagName("virtualMac");

            final HashMap<Integer, CentroServico> centroDeServicos =
                    new HashMap<>();
            final HashMap<CentroServico, List<CS_MaquinaCloud>> escravosCluster =
                    new HashMap<>();
            final List<CS_MaquinaCloud> maqs = new ArrayList<>();
            final List<CS_VirtualMac> vms = new ArrayList<>();
            final List<CS_Comunicacao> links = new ArrayList<>();
            final List<CS_Internet> nets = new ArrayList<>();
            final List<CS_Processamento> VMMs = new ArrayList<>();
            //cria lista de usuarios e o poder computacional cedido por cada um
            final HashMap<String, Double> usuarios = new HashMap<>();
            for (int i = 0; i < owners.getLength(); i++) {
                final Element owner = (Element) owners.item(i);
                usuarios.put(owner.getAttribute("id"), 0.0);
            }
            //cria maquinas, mestres, internets e mestres dos clusters
            //Realiza leitura dos icones de máquina
            for (int i = 0; i < docmaquinas.getLength(); i++) {
                final Element maquina = (Element) docmaquinas.item(i);
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
                    VMMs.add(mestre);
                    centroDeServicos.put(global, mestre);
                    //Contabiliza para o usuario poder computacional do mestre
                    usuarios.put(mestre.getProprietario(),
                            usuarios.get(mestre.getProprietario()) + mestre.getPoderComputacional());
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
                    maqs.add(maq);
                    centroDeServicos.put(global, maq);
                    usuarios.put(maq.getProprietario(),
                            usuarios.get(maq.getProprietario()) + maq.getPoderComputacional());
                }
            }
            //Realiza leitura dos icones de cluster
            for (int i = 0; i < docclusters.getLength(); i++) {
                final Element cluster = (Element) docclusters.item(i);
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
                    VMMs.add(clust);
                    centroDeServicos.put(global, clust);
                    //Contabiliza para o usuario poder computacional do mestre
                    final int numeroEscravos =
                            Integer.parseInt(cluster.getAttribute(
                                    "nodes"));
                    final double total =
                            clust.getPoderComputacional() + (clust.getPoderComputacional() * numeroEscravos);
                    usuarios.put(clust.getProprietario(),
                            total + usuarios.get(clust.getProprietario()));
                    final CS_Switch Switch = new CS_Switch(
                            (cluster.getAttribute("id") + "switch"),
                            Double.parseDouble(cluster.getAttribute(
                                    "bandwidth")),
                            0.0,
                            Double.parseDouble(cluster.getAttribute("latency")));
                    links.add(Switch);
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
                                IconicoXML.cloudMachineFromElement(cluster, j,
                                        custo,
                                        processamento, memoria, disco);

                        IconicoXML.connectMachineAndSwitch(Switch, maq);

                        maq.addMestre(clust);
                        clust.addEscravo(maq);
                        maqs.add(maq);
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
                    links.add(Switch);
                    centroDeServicos.put(global, Switch);
                    //Contabiliza para o usuario poder computacional do mestre
                    final double total =
                            Double.parseDouble(cluster.getAttribute(
                                    "power"))
                            * Integer.parseInt(cluster.getAttribute(
                                    "nodes"
                            ));
                    usuarios.put(cluster.getAttribute("owner"),
                            total + usuarios.get(cluster.getAttribute("owner")));
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
                                IconicoXML.cloudMachineFromElement(cluster, j,
                                        custo, processamento, memoria, disco);
                        IconicoXML.connectMachineAndSwitch(Switch, maq);
                        maqTemp.add(maq);
                        maqs.add(maq);
                    }
                    escravosCluster.put(Switch, maqTemp);
                }
            }

            //Realiza leitura dos icones de internet
            for (int i = 0; i < docinternet.getLength(); i++) {
                final Element inet = (Element) docinternet.item(i);
                final Element id =
                        IconicoXML.getFirstTagElement(inet, "icon_id");
                final int global = Integer.parseInt(id.getAttribute("global"));
                final CS_Internet net = new CS_Internet(
                        inet.getAttribute("id"),
                        Double.parseDouble(inet.getAttribute("bandwidth")),
                        Double.parseDouble(inet.getAttribute("load")),
                        Double.parseDouble(inet.getAttribute("latency")));
                nets.add(net);
                centroDeServicos.put(global, net);
            }
            //cria os links e realiza a conexão entre os recursos
            for (int i = 0; i < doclinks.getLength(); i++) {
                final Element link = (Element) doclinks.item(i);

                final CS_Link cslink = new CS_Link(
                        link.getAttribute("id"),
                        Double.parseDouble(link.getAttribute("bandwidth")),
                        Double.parseDouble(link.getAttribute("load")),
                        Double.parseDouble(link.getAttribute("latency")));
                links.add(cslink);

                //adiciona entrada e saida desta conexão
                final Element connect =
                        IconicoXML.getFirstTagElement(link, "connect");
                final Vertice origem =
                        (Vertice) centroDeServicos.get(Integer.parseInt(connect.getAttribute("origination")));
                final Vertice destino =
                        (Vertice) centroDeServicos.get(Integer.parseInt(connect.getAttribute("destination")));
                cslink.setConexoesSaida((CentroServico) destino);
                destino.addConexoesEntrada(cslink);
                cslink.setConexoesEntrada((CentroServico) origem);
                origem.addConexoesSaida(cslink);
            }
            //adiciona os escravos aos mestres
            for (int i = 0; i < docmaquinas.getLength(); i++) {
                final Element maquina = (Element) docmaquinas.item(i);
                final Element id =
                        IconicoXML.getFirstTagElement(maquina, "icon_id");
                final int global = Integer.parseInt(id.getAttribute("global"));
                if (Utils.isValidMaster(maquina)) {
                    final Element master =
                            IconicoXML.getFirstTagElement(maquina,
                                    "master");
                    final NodeList slaves = master.getElementsByTagName(
                            "slave");
                    final CS_VMM mestre = (CS_VMM) centroDeServicos.get(global);
                    for (int j = 0; j < slaves.getLength(); j++) {
                        final Element slave = (Element) slaves.item(j);
                        final CentroServico maq =
                                centroDeServicos.get(Integer.parseInt(slave.getAttribute("id")));
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
                                    escravosCluster.get(maq)) {
                                escr.addMestre(mestre);
                                mestre.addEscravo(escr);
                            }
                        }
                    }
                }
            }

            //Realiza leitura dos ícones de máquina virtual
            for (int i = 0; i < docVMs.getLength(); i++) {
                final Element virtualMac = (Element) docVMs.item(i);
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
                for (final CS_Processamento aux : VMMs) {
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
                vms.add(VM);
            }

            //verifica se há usuarios sem nenhum recurso
            final List<String> proprietarios = new ArrayList<>();
            final List<Double> poderComp = new ArrayList<>();
            for (final Map.Entry<String, Double> entry : usuarios.entrySet()) {
                proprietarios.add(entry.getKey());
                poderComp.add(entry.getValue());
            }
            //cria as métricas de usuarios para cada mestre
            for (final CS_Processamento mestre : VMMs) {
                final CS_VMM mst = (CS_VMM) mestre;
                final MetricasUsuarios mu = new MetricasUsuarios();
                mu.addAllUsuarios(proprietarios, poderComp);
                mst.getEscalonador().setMetricaUsuarios(mu);
            }
            final RedeDeFilasCloud rdf = new RedeDeFilasCloud(VMMs, maqs, vms,
                    links,
                    nets);
            //cria as métricas de usuarios globais da rede de filas
            final MetricasUsuarios mu = new MetricasUsuarios();
            mu.addAllUsuarios(proprietarios, poderComp);
            rdf.setUsuarios(proprietarios);
            return rdf;
        }
    }

    private static class CloningEntityResolver implements EntityResolver {
        // TODO: Resolve cyclic dependency
        private final InputSource substitute = new InputSource(
                IconicoXML.class.getResourceAsStream("iSPD.dtd"));

        public InputSource resolveEntity(final String publicId,
                                         final String systemId) {
            return this.substitute;
        }
    }

}