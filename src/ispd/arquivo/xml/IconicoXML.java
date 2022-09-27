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
import ispd.utils.ValidaValores;
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
     * @throws IllegalArgumentException if the model is incomplete
     */
    public static void validarModelo(final Document doc) {
        final var document = new WrappedDocument(doc);

        if (document.hasEmptyTag("owner")) {
            throw new IllegalArgumentException("The model has no users.");
        }

        if (document.hasEmptyTag("machine") &&
            document.hasEmptyTag("cluster")) {
            throw new IllegalArgumentException("The model has no icons.");
        }

        if (document.hasEmptyTag("load")) {
            throw new IllegalArgumentException(
                    "One or more  workloads have not been configured.");
        }

        final boolean hasNoValidMaster =
                document.wElementsWithTag("machine")
                        .noneMatch(WrappedElement::hasMasterAttribute);

        if (hasNoValidMaster) {
            throw new IllegalArgumentException(
                    "One or more parameters have not been configured.");
        }
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
        return new CloudQueueNetworkBuilder(model).build();
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
            if (new WrappedElement(maquina).hasMasterAttribute()) {
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
            final Element element, final String tag) {
        // TODO: Inline this method
        return new WrappedElement(element).firstTagElement(tag);
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
        final var memorySize = IconicoXML.getFirstTagElement(characteristics,
                "memory").getAttribute("size");
        final var diskSize = IconicoXML.getFirstTagElement(characteristics,
                "hard_disk").getAttribute("size");

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
        return new WrappedDocument(doc).elementsWithTag("owner")
                .map(WrappedElement::new)
                .map(WrappedElement::id)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static List<String> newListUsers(final Document doc) {
        return new WrappedDocument(doc).elementsWithTag("owner")
                .map(WrappedElement::new)
                .map(WrappedElement::id)
                .toList();
    }

    public static HashSet<VirtualMachine> newListVirtualMachines(final Document doc) {
        return new WrappedDocument(doc).elementsWithTag("virtualMac")
                .map(WrappedElement::new)
                .map(IconicoXML::virtualMachineFromElement)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static VirtualMachine virtualMachineFromElement(final WrappedElement e) {
        return new VirtualMachine(e.id(), e.owner(), e.vmm(), e.powerAsInt(),
                e.memAlloc(), e.diskAlloc(), e.opSystem());
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
            final Boolean isMaster,
            final Double energy) {
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
                        { "energy", energy },
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
        // Note: Arrays.asList returns an abstract list, which throws on .add()
        final var attrList = Arrays.stream(new Object[][] {
                { "id", name },
                { "power", power },
                { "load", occupancy },
                { "owner", owner },
        }).collect(Collectors.toList());

        attrList.addAll(Arrays.asList(extraAttrs));

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
                "machine", attrList.toArray(Object[][]::new), new Node[] {
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
        final var attrList = Arrays.asList(new Object[][] {
                { "scheduler", scheduler },
        });

        attrList.addAll(Arrays.asList(extraAttrs));

        return this.anElement(
                "master", attrList.toArray(Object[][]::new),
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