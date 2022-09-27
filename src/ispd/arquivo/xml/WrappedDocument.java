package ispd.arquivo.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record WrappedDocument(Document document) {
    public void forEachElementWithTag(
            final String tag, final Consumer<? super Element> action) {
        this.elementsWithTag(tag).forEach(action);
    }

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
}