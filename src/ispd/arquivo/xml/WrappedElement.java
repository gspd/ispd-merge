package ispd.arquivo.xml;

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

    public int vertex(final String vertexEnd) {
        return this.wFirstTagElement("connect").getInt(vertexEnd);
    }

    public int getInt(final String attributeName) {
        return Integer.parseInt(this.getAttribute(attributeName));
    }

    public WrappedElement wFirstTagElement(final String tagName) {
        return new WrappedElement(this.firstTagElement(tagName));
    }

    private String getAttribute(final String s) {
        return this.element.getAttribute(s);
    }

    public Element firstTagElement(final String tagName) {
        return (Element) this.element.getElementsByTagName(tagName).item(0);
    }

    public String vmm() {
        return this.getAttribute("vmm");
    }

    public Stream<WrappedElement> wMastersSlaves() {
        final var ms = this.mastersSlaves();
        return IntStream.range(0, ms.getLength())
                .mapToObj(ms::item)
                .map(Element.class::cast)
                .map(WrappedElement::new);
    }

    public NodeList mastersSlaves() {
        return this.firstTagElement("master").getElementsByTagName("slave");
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
        return this.wFirstTagElement("icon_id").getInt("global");
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

    public double diskAlloc() {
        return this.getDouble("disk_alloc");
    }

    public String opSystem() {
        return this.getAttribute("op_system");
    }

    public String mastersScheduler() {
        return this.wFirstTagElement("master").scheduler();
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

    public Element getElement() {
        // TODO: Check if this method is removable
        return this.element;
    }
}
