package ispd.arquivo.xml;

import org.w3c.dom.Element;

public class Utils {

    public static double getValueAttribute(final Element elem,
                                           final String attr) {
        return Double.parseDouble(elem.getAttribute(attr));
    }

    public static boolean isValidMaster(final Element m) {
        return m.getElementsByTagName("master").getLength() > 0;
    }
}
