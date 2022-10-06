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

    public int getInt(final String attributeName) {
        return Integer.parseInt(this.getAttribute(attributeName));
    }

    public WrappedElement position() {
        return this.wFirstTagElement("position");
    }

    public WrappedElement connection() {
        return this.wFirstTagElement("connect");
    }

    public String getAttribute(final String s) {
        return this.element.getAttribute(s);
    }

    public WrappedElement wFirstTagElement(final String tagName) {
        return new WrappedElement(this.firstTagElement(tagName));
    }

    public Element firstTagElement(final String tagName) {
        return (Element) this.element.getElementsByTagName(tagName).item(0);
    }

    public int destination() {
        return this.vertex("destination");
    }

    public String vmm() {
        return this.getAttribute("vmm");
    }

    public WrappedElement master() {
        return this.wFirstTagElement("master");
    }

    public Stream<WrappedElement> slaves() {
        return this.elementsWithTag("slave");
    }

    private Stream<WrappedElement> elementsWithTag(final String tag) {
        return WrappedElement.nodeListToWrappedElementStream(
                this.element.getElementsByTagName(tag)
        );
    }

    private static Stream<WrappedElement> nodeListToWrappedElementStream(final NodeList nl) {
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
        return this.wFirstTagElement("icon_id");
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
        return this.element.getElementsByTagName("master").getLength() > 0;
    }

    public boolean hasCharacteristicAttribute() {
        return this.element.getElementsByTagName("characteristic").getLength() > 0;
    }

    public boolean hasCostAttribute() {
        return this.element.getElementsByTagName("cost").getLength() > 0;
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
        return this.wFirstTagElement("characteristic");
    }

    public WrappedElement processor() {
        return this.wFirstTagElement("process");
    }

    public WrappedElement memory() {
        return this.wFirstTagElement("memory");
    }

    public WrappedElement hardDisk() {
        return this.wFirstTagElement("hard_disk");
    }

    public WrappedElement costs() {
        return this.wFirstTagElement("cost");
    }
}
