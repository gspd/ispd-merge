package ispd.arquivo.xml.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public record WrappedDocument(Document document) {

    public Stream<Element> elementsWithTag(final String tag) {
        final var list = this.document.getElementsByTagName(tag);
        return IntStream.range(0, list.getLength())
                .mapToObj(list::item)
                .map(Element.class::cast);
    }

    public Stream<WrappedElement> wElementsWithTag(final String tag){
        return this.elementsWithTag(tag).map(WrappedElement::new);
    }

    public boolean hasEmptyTag(final String tag) {
        return this.document.getElementsByTagName(tag).getLength() == 0;
    }

    public Stream<WrappedElement> owners() {
        return this.wElementsWithTag("owner");
    }

    public Stream<WrappedElement> machines() {
        return this.wElementsWithTag("machine");
    }

    public Stream<WrappedElement> masters() {
        return this.machines()
                .filter(WrappedElement::hasMasterAttribute);
    }

    public Stream<WrappedElement> clusters() {
        return this.wElementsWithTag("cluster");
    }

    public Stream<WrappedElement> internets() {
        return this.wElementsWithTag("internet");
    }

    public Stream<WrappedElement> links() {
        return this.wElementsWithTag("link");
    }

    public Stream<WrappedElement> virtualMachines() {
        return this.wElementsWithTag("virtualMac");
    }
}