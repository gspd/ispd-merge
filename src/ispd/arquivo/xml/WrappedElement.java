package ispd.arquivo.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This is a planned class to add convenience methods to the Element class.
 * The idea is to function as a wrapper, outsourcing most method calls to the
 * inner object.
 */
public class WrappedElement {
    private final Element element;

    public WrappedElement(final Element element) {
        this.element = element;
    }

    NodeList mastersSlaves() {
        return this.firstTagElement("master").getElementsByTagName("slave");
    }

    public Element firstTagElement(final String tagName) {
        return (Element) this.element.getElementsByTagName(tagName).item(0);
    }

    public String id() {
        return this.getAttribute("id");
    }

    public String getAttribute(final String s) {
        return this.element.getAttribute(s);
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
        final var idElement = new WrappedElement(this.firstTagElement(
                "icon_id"));
        return idElement.getInt("global");
    }

    public int getInt(final String attributeName) {
        return Integer.parseInt(this.getAttribute(attributeName));
    }

    public String owner() {
        return this.getAttribute("owner");
    }

    public double power() {
        return this.getDouble("power");
    }

    public String mastersScheduler() {
        final var master = new WrappedElement(this.firstTagElement("master"));
        return master.scheduler();
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
