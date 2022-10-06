package ispd.arquivo.xml;

import ispd.arquivo.xml.modelBuilders.CloudQueueNetworkBuilder;
import ispd.arquivo.xml.modelBuilders.GridBuilder;
import ispd.arquivo.xml.modelBuilders.QueueNetworkBuilder;
import ispd.gui.PickModelTypeDialog;
import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.VirtualMachine;
import ispd.motor.carga.GerarCarga;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
                    "One or more workloads have not been configured.");
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
        return new QueueNetworkBuilder(new WrappedDocument(model)).build();
    }

    public static RedeDeFilasCloud newRedeDeFilasCloud(final Document model) {
        return (RedeDeFilasCloud) new CloudQueueNetworkBuilder(
                new WrappedDocument(model)).build();
    }

    /**
     * Obtem a configuração da carga de trabalho contida em um modelo iconico
     *
     * @param modelo contem conteudo recuperado de um arquivo xml
     * @return carga de trabalho contida no modelo
     */
    public static GerarCarga newGerarCarga(final Document modelo) {
        return LoadBuilder.buildLoad(modelo);
    }

    public static void newGrade(
            final Document doc,
            final Collection<? super Vertex> vertices,
            final Collection<? super Edge> edges) {
        final var model = new GridBuilder(doc).buildGrid();
        vertices.addAll(model.vertices());
        edges.addAll(model.edges());
    }

    public static HashSet<String> newSetUsers(final Document doc) {
        return new WrappedDocument(doc).wElementsWithTag("owner")
                .map(WrappedElement::id)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static List<String> newListUsers(final Document doc) {
        return new WrappedDocument(doc).wElementsWithTag("owner")
                .map(WrappedElement::id)
                .toList();
    }

    public static HashSet<VirtualMachine> newListVirtualMachines(final Document doc) {
        return new WrappedDocument(doc).wElementsWithTag("virtualMac")
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
            // TODO: can EntityResolver be set outside loop?
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
        return new WrappedDocument(doc).wElementsWithTag("owner")
                .collect(Collectors.toMap(
                        WrappedElement::id,
                        WrappedElement::powerLimit,
                        (prev, next) -> next,
                        HashMap::new
                ));
    }

    public void addUsers(final Collection<String> users,
                         final Map<String, Double> limits) {
        // TODO: Could I iterate over the map instead? Why need arg 'users'?
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

        public InputSource resolveEntity(
                final String publicId, final String systemId) {
            return this.substitute;
        }
    }

}