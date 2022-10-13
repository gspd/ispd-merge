package ispd.arquivo.xml.utils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class to add convenience methods to manipulate XML Element objects.
 * It functions as a wrapper, outsourcing method calls to the inner object.
 */
public class WrappedElement {
    private final Element element;

    /**
     * @return id of origination vertex
     */
    public int origination() {
        return this.vertex("origination");
    }

    private int vertex(final String vertexEnd) {
        return this.connection().getInt(vertexEnd);
    }

    private int getInt(final String attributeName) {
        return Integer.parseInt(this.getAttribute(attributeName));
    }

    /**
     * @return connection element
     */
    private WrappedElement connection() {
        return this.firstTagElement("connect");
    }

    private String getAttribute(final String s) {
        return this.element.getAttribute(s);
    }

    /**
     * @return first element with given tag name
     */
    private WrappedElement firstTagElement(final String tagName) {
        return new WrappedElement((Element) this.getElementsByTagName(tagName).item(0));
    }

    private NodeList getElementsByTagName(final String tag) {
        return this.element.getElementsByTagName(tag);
    }

    /**
     * @return id of destination vertex
     */
    public int destination() {
        return this.vertex("destination");
    }

    public WrappedElement position() {
        return this.firstTagElement("position");
    }

    public String vmm() {
        return this.getAttribute("vmm");
    }

    /**
     * @return inner master element
     */
    public WrappedElement master() {
        return this.firstTagElement("master");
    }

    public Stream<WrappedElement> slaves() {
        return this.elementsWithTag("slave");
    }

    private Stream<WrappedElement> elementsWithTag(final String tag) {
        return WrappedElement.nodeListToWrappedElementStream(
                this.getElementsByTagName(tag)
        );
    }

    /**
     * @return convert given {@link NodeList} into a {@link Stream} of {@code
     * WrappedElement}s
     */
    /* package-private */
    static Stream<WrappedElement> nodeListToWrappedElementStream(final NodeList nl) {
        return IntStream.range(0, nl.getLength())
                .mapToObj(nl::item)
                .map(Element.class::cast)
                .map(WrappedElement::new);
    }

    public WrappedElement(final Element element) {
        this.element = element;
    }

    public String id() {
        return this.getAttribute("id");
    }

    public double bandwidth() {
        return this.getDouble("bandwidth");
    }

    private double getDouble(final String attributeName) {
        return Double.parseDouble(this.getAttribute(attributeName));
    }

    public double latency() {
        return this.getDouble("latency");
    }

    /**
     * @return inner "icon id" element's "global" attribute value
     */
    public int globalIconId() {
        return this.iconId().global();
    }

    public int global() {
        return this.getInt("global");
    }

    /**
     * @return inner element with tag "icon_id"
     */
    public WrappedElement iconId() {
        return this.firstTagElement("icon_id");
    }

    public String owner() {
        return this.getAttribute("owner");
    }

    public double power() {
        return this.getDouble("power");
    }

    /**
     * @return "mem_alloc" attribute
     */
    public double memAlloc() {
        return this.getDouble("mem_alloc");
    }

    public int nodes() {
        return this.getInt("nodes");
    }

    /**
     * @return "vm_alloc" attribute
     */
    public String vmAlloc() {
        return this.getAttribute("vm_alloc");
    }

    /**
     * @return "disk_alloc" attribute
     */
    public double diskAlloc() {
        return this.getDouble("disk_alloc");
    }

    /**
     * @return "op_system" attribute
     */
    public String opSystem() {
        return this.getAttribute("op_system");
    }

    public String scheduler() {
        return this.getAttribute("scheduler");
    }

    public double energy() {
        return this.getDouble("energy");
    }

    /**
     * @return whether the attribute "master" in this element is true
     */
    public boolean isMaster() {
        return this.getBoolean("master");
    }

    private boolean getBoolean(final String attr) {
        return Boolean.parseBoolean(this.getAttribute(attr));
    }

    public double load() {
        return this.getDouble("load");
    }

    /**
     * @return whether this element has a inner "master" element
     */
    public boolean hasMasterAttribute() {
        return this.getElementsByTagName("master").getLength() > 0;
    }

    /**
     * @return whether this element has a inner "characteristic" element
     */
    public boolean hasCharacteristicAttribute() {
        return this.getElementsByTagName("characteristic").getLength() > 0;
    }

    /**
     * @return whether this element has a inner "cost" element
     */
    public boolean hasCostAttribute() {
        return this.getElementsByTagName("cost").getLength() > 0;
    }

    /**
     * @return "cost_proc" attribute
     */
    public double costProcessing() {
        return this.getDouble("cost_proc");
    }

    /**
     * @return "cost_mem" attribute
     */
    public double costMemory() {
        return this.getDouble("cost_mem");
    }

    public int x() {
        return this.getInt("x");
    }

    public int y() {
        return this.getInt("y");
    }

    public int local() {
        return this.getInt("local");
    }

    public double powerLimit() {
        return this.getDouble("powerlimit");
    }

    public double costDisk() {
        return this.getDouble("cost_disk");
    }

    public double size() {
        return this.getDouble("size");
    }

    /**
     * @return "number" attribute, usually representing the number of cores
     */
    public int number() {
        return this.getInt("number");
    }

    /**
     * @return inner element with tag "characteristic", containing processing
     * and storage information
     */
    public WrappedElement characteristics() {
        return this.firstTagElement("characteristic");
    }

    /**
     * @return inner element with tag "process", representing the processor
     */
    public WrappedElement processor() {
        return this.firstTagElement("process");
    }

    public WrappedElement memory() {
        return this.firstTagElement("memory");
    }

    /**
     * @return inner element with tag "hard_disk"
     */
    public WrappedElement hardDisk() {
        return this.firstTagElement("hard_disk");
    }

    /**
     * @return inner "cost" element
     */
    public WrappedElement costs() {
        return this.firstTagElement("cost");
    }

    public int tasks() {
        return this.getInt("tasks");
    }

    /**
     * @return "time_arrival" attribute
     */
    public int arrivalTime() {
        return this.getInt("time_arrival");
    }

    /**
     * @return {@link Stream} of all "size" inner elements
     */
    public Stream<WrappedElement> sizes() {
        return this.elementsWithTag("size");
    }

    /**
     * @return whether this element's "type" attribute contains the value
     * "computing"
     */
    public boolean isComputingType() {
        return "computing".equals(this.type());
    }

    private String type() {
        return this.getAttribute("type");
    }

    /**
     * @return whether this element's "type" attribute contains the value
     * "communication"
     */
    public boolean isCommunicationType() {
        return "communication".equals(this.type());
    }

    public double minimum() {
        return this.getDouble("minimum");
    }

    public double maximum() {
        return this.getDouble("maximum");
    }

    public double average() {
        return this.getDouble("average");
    }

    public double probability() {
        return this.getDouble("probability");
    }

    public String application() {
        return this.getAttribute("application");
    }

    /**
     * @return "id_master" attribute
     */
    public String masterId() {
        return this.getAttribute("id_master");
    }

    /**
     * @return "file_path" attribute
     */
    public String filePath() {
        return this.getAttribute("file_path");
    }

    public String format() {
        return this.getAttribute("format");
    }

    /**
     * @return {@link Stream} of inner elements with tag "random"
     */
    public Stream<WrappedElement> randomLoads() {
        return this.elementsWithTag("random");
    }

    /**
     * @return {@link Stream} of inner elements with tag "node"
     */
    public Stream<WrappedElement> nodeLoads() {
        return this.elementsWithTag("node");
    }

    /**
     * @return {@link Stream} of inner elements with tag "trace"
     */
    public Stream<WrappedElement> traceLoads() {
        return this.elementsWithTag("trace");
    }

    /**
     * @return "simulation_mode" attribute
     */
    public String simulationMode() {
        return this.getAttribute("simulation_mode");
    }

    /**
     * @return "number_threads" attribute
     */
    public int numberOfThreads() {
        return this.getInt("number_threads");
    }

    /**
     * @return "number_simulations" attribute
     */
    public int numberOfSimulations() {
        return this.getInt("number_simulations");
    }

    /**
     * @return inner element with tag "chart_create"
     */
    public WrappedElement chartCreate() {
        return this.firstTagElement("chart_create");
    }

    /**
     * @return whether the attribute "processing" is true
     */
    public boolean shouldChartProcessing() {
        return this.getBoolean("processing");
    }

    /**
     * @return whether the attribute "communication" is true
     */
    public boolean shouldChartCommunication() {
        return this.getBoolean("communication");
    }

    /**
     * @return whether the attribute "user_time" is true
     */
    public boolean shouldChartUserTime() {
        return this.getBoolean("user_time");
    }

    /**
     * @return whether the attribute "machine_time" is true
     */
    public boolean shouldChartMachineTime() {
        return this.getBoolean("machine_time");
    }

    /**
     * @return whether the attribute "task_time" is true
     */
    public boolean shouldChartTaskTime() {
        return this.getBoolean("task_time");
    }

    /**
     * @return inner element with tag "model_open"
     */
    public WrappedElement modelOpen() {
        return this.firstTagElement("model_open");
    }

    /**
     * @return "last_file" attribute
     */
    public String lastFile() {
        return this.getAttribute("last_file");
    }
}
