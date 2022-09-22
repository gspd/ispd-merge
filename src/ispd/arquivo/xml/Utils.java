package ispd.arquivo.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Utils {
    static Stream<Element> elementStreamOf(final NodeList list) {
        return IntStream.range(0, list.getLength())
                .mapToObj(list::item)
                .map(Element.class::cast);
    }

    public static void forEachDocElement(final Document doc, final String tag,
                                         final Consumer<? super Element> action) {
        forEachElement(
                doc.getElementsByTagName(tag),
                action
        );
    }

    public static void forEachElement(final NodeList list,
                                      final Consumer<? super Element> action) {
        elementStreamOf(list).forEach(action);
    }

    public static double getValueAttribute(final Element elem,
                                           final String attr) {
        return Double.parseDouble(elem.getAttribute(attr));
    }

    public static boolean isValidMaster(final Element m) {
        return m.getElementsByTagName("master").getLength() > 0;
    }
}
