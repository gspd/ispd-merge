package ispd.arquivo.xml.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Objects;
import java.util.stream.Stream;

public class WrappedDocument {
    public final Document document; // TODO: Encapsulate field

    public WrappedDocument(final Document doc) {
        Objects.requireNonNull(doc);
        this.document = doc;
    }

    public Element createElement(final String name) {
        return this.document.createElement(name);
    }

    public void appendChild(final Node e) {
        this.document.appendChild(e);
    }

    public boolean hasNoOwners() {
        return this.hasEmptyTag("owner");
    }

    private boolean hasEmptyTag(final String tag) {
        return this.document.getElementsByTagName(tag).getLength() == 0;
    }

    public boolean hasNoMachines() {
        return this.hasEmptyTag("machine");
    }

    public boolean hasNoMasters() {
        return this.machines()
                .noneMatch(WrappedElement::hasMasterAttribute);
    }

    public Stream<WrappedElement> machines() {
        return this.elementsWithTag("machine");
    }

    private Stream<WrappedElement> elementsWithTag(final String tag) {
        return WrappedElement.nodeListToWrappedElementStream(
                this.document.getElementsByTagName(tag)
        );
    }

    public boolean hasNoClusters() {
        return this.hasEmptyTag("cluster");
    }

    public boolean hasNoLoads() {
        return this.hasEmptyTag("load");
    }

    public Stream<WrappedElement> owners() {
        return this.elementsWithTag("owner");
    }

    public Stream<WrappedElement> masters() {
        return this.machines()
                .filter(WrappedElement::hasMasterAttribute);
    }

    public Stream<WrappedElement> clusters() {
        return this.elementsWithTag("cluster");
    }

    public Stream<WrappedElement> internets() {
        return this.elementsWithTag("internet");
    }

    public Stream<WrappedElement> links() {
        return this.elementsWithTag("link");
    }

    public Stream<WrappedElement> virtualMachines() {
        return this.elementsWithTag("virtualMac");
    }

    public Stream<WrappedElement> loads() {
        return this.elementsWithTag("load");
    }
}