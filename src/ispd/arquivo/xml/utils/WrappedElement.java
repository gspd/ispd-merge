package ispd.arquivo.xml.utils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * This is a class to add convenience methods to manipulate XML Element objects.
 * It functions as a wrapper, outsourcing most method calls to the inner object.
 */
public class WrappedElement {
    private final Element element;

    public int origination() {
        return this.vertex("origination");
    }

    public int vertex(final String vertexEnd) {
        return this.connection().getInt(vertexEnd);
    }

    private int getInt(final String attributeName) {
        return Integer.parseInt(this.getAttribute(attributeName));
    }

    private WrappedElement connection() {
        return this.firstTagElement("connect");
    }

    private String getAttribute(final String s) {
        return this.element.getAttribute(s);
    }

    private WrappedElement firstTagElement(final String tagName) {
        return new WrappedElement((Element) this.getElementsByTagName(tagName).item(0));
    }

    private NodeList getElementsByTagName(final String tag) {
        return this.element.getElementsByTagName(tag);
    }

    public WrappedElement position() {
        return this.firstTagElement("position");
    }

    public int destination() {
        return this.vertex("destination");
    }

    public String vmm() {
        return this.getAttribute("vmm");
    }

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

    /* package-private */ static Stream<WrappedElement> nodeListToWrappedElementStream(final NodeList nl) {
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

    public double getDouble(final String attributeName) {
        return Double.parseDouble(this.getAttribute(attributeName));
    }

    public double latency() {
        return this.getDouble("latency");
    }

    public int globalIconId() {
        return this.iconId().global();
    }

    public int global() {
        return this.getInt("global");
    }

    public WrappedElement iconId() {
        return this.firstTagElement("icon_id");
    }

    public String owner() {
        return this.getAttribute("owner");
    }

    public double power() {
        return this.getDouble("power");
    }

    public int powerAsInt() {
        // TODO: Is this function necessary?
        return this.getInt("power");
    }

    public double memAlloc() {
        return this.getDouble("mem_alloc");
    }

    public int nodes() {
        return this.getInt("nodes");
    }

    public String vmAlloc() {
        return this.getAttribute("vm_alloc");
    }

    public double diskAlloc() {
        return this.getDouble("disk_alloc");
    }

    public String opSystem() {
        return this.getAttribute("op_system");
    }

    public String scheduler() {
        return this.getAttribute("scheduler");
    }

    public double energy() {
        return this.getDouble("energy");
    }

    public boolean isMaster() {
        return Boolean.parseBoolean(this.getAttribute("master"));
    }

    public double load() {
        return this.getDouble("load");
    }

    public boolean hasMasterAttribute() {
        return this.getElementsByTagName("master").getLength() > 0;
    }

    public boolean hasCharacteristicAttribute() {
        return this.getElementsByTagName("characteristic").getLength() > 0;
    }

    public boolean hasCostAttribute() {
        return this.getElementsByTagName("cost").getLength() > 0;
    }

    public double costProcessing() {
        return this.getDouble("cost_proc");
    }

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

    public int number() {
        return this.getInt("number");
    }

    public WrappedElement characteristics() {
        return this.firstTagElement("characteristic");
    }

    public WrappedElement processor() {
        return this.firstTagElement("process");
    }

    public WrappedElement memory() {
        return this.firstTagElement("memory");
    }

    public WrappedElement hardDisk() {
        return this.firstTagElement("hard_disk");
    }

    public WrappedElement costs() {
        return this.firstTagElement("cost");
    }

    public int tasks() {
        return this.getInt("tasks");
    }

    public int arrivalTime() {
        return this.getInt("time_arrival");
    }

    public Stream<WrappedElement> sizes() {
        return this.elementsWithTag("size");
    }

    private String type() {
        return this.getAttribute("type");
    }

    public boolean isComputingType() {
        return "computing".equals(this.type());
    }

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

    private boolean hasEmptyTag(final String tag) {
        return this.element.getElementsByTagName(tag).getLength() == 0;
    }

    public boolean hasRandomLoads() {
        return this.hasEmptyTag("random");
    }

    public Stream<WrappedElement> randomLoad() {
        return this.elementsWithTag("random");
    }

    public Stream<WrappedElement> nodeLoads () {
        return this.elementsWithTag("node");
    }
}
