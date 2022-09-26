package ispd.arquivo.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;


/**
 * This is a planned class to add convenience methods to the Element class.
 * The idea is to function as a wrapper, outsourcing most method calls to the
 * inner object.
 */
public class ElementWrapper implements Element {
    private final Element element;

    public ElementWrapper(final Element element) {
        this.element = element;
    }

    @Override
    public String getTagName() {
        return this.element.getTagName();
    }

    @Override
    public String getAttribute(final String s) {
        return null;
    }

    @Override
    public void setAttribute(final String s, final String s1) throws DOMException {

    }

    @Override
    public void removeAttribute(final String s) throws DOMException {

    }

    @Override
    public Attr getAttributeNode(final String s) {
        return null;
    }

    @Override
    public Attr setAttributeNode(final Attr attr) throws DOMException {
        return null;
    }

    @Override
    public Attr removeAttributeNode(final Attr attr) throws DOMException {
        return null;
    }

    @Override
    public NodeList getElementsByTagName(final String s) {
        return null;
    }

    @Override
    public String getAttributeNS(final String s, final String s1) throws DOMException {
        return null;
    }

    @Override
    public void setAttributeNS(final String s, final String s1,
                               final String s2) throws DOMException {

    }

    @Override
    public void removeAttributeNS(final String s, final String s1) throws DOMException {

    }

    @Override
    public Attr getAttributeNodeNS(final String s, final String s1) throws DOMException {
        return null;
    }

    @Override
    public Attr setAttributeNodeNS(final Attr attr) throws DOMException {
        return null;
    }

    @Override
    public NodeList getElementsByTagNameNS(final String s, final String s1) throws DOMException {
        return null;
    }

    @Override
    public boolean hasAttribute(final String s) {
        return false;
    }

    @Override
    public boolean hasAttributeNS(final String s, final String s1) throws DOMException {
        return false;
    }

    @Override
    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    @Override
    public void setIdAttribute(final String s, final boolean b) throws DOMException {

    }

    @Override
    public void setIdAttributeNS(final String s, final String s1,
                                 final boolean b) throws DOMException {

    }

    @Override
    public void setIdAttributeNode(final Attr attr, final boolean b) throws DOMException {

    }

    @Override
    public String getNodeName() {
        return null;
    }

    @Override
    public String getNodeValue() throws DOMException {
        return null;
    }

    @Override
    public void setNodeValue(final String s) throws DOMException {

    }

    @Override
    public short getNodeType() {
        return 0;
    }

    @Override
    public Node getParentNode() {
        return null;
    }

    @Override
    public NodeList getChildNodes() {
        return null;
    }

    @Override
    public Node getFirstChild() {
        return null;
    }

    @Override
    public Node getLastChild() {
        return null;
    }

    @Override
    public Node getPreviousSibling() {
        return null;
    }

    @Override
    public Node getNextSibling() {
        return null;
    }

    @Override
    public NamedNodeMap getAttributes() {
        return null;
    }

    @Override
    public Document getOwnerDocument() {
        return null;
    }

    @Override
    public Node insertBefore(final Node node, final Node node1) throws DOMException {
        return null;
    }

    @Override
    public Node replaceChild(final Node node, final Node node1) throws DOMException {
        return null;
    }

    @Override
    public Node removeChild(final Node node) throws DOMException {
        return null;
    }

    @Override
    public Node appendChild(final Node node) throws DOMException {
        return null;
    }

    @Override
    public boolean hasChildNodes() {
        return false;
    }

    @Override
    public Node cloneNode(final boolean b) {
        return null;
    }

    @Override
    public void normalize() {

    }

    @Override
    public boolean isSupported(final String s, final String s1) {
        return false;
    }

    @Override
    public String getNamespaceURI() {
        return null;
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public void setPrefix(final String s) throws DOMException {

    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public boolean hasAttributes() {
        return false;
    }

    @Override
    public String getBaseURI() {
        return null;
    }

    @Override
    public short compareDocumentPosition(final Node node) throws DOMException {
        return 0;
    }

    @Override
    public String getTextContent() throws DOMException {
        return null;
    }

    @Override
    public void setTextContent(final String s) throws DOMException {

    }

    @Override
    public boolean isSameNode(final Node node) {
        return false;
    }

    @Override
    public String lookupPrefix(final String s) {
        return null;
    }

    @Override
    public boolean isDefaultNamespace(final String s) {
        return false;
    }

    @Override
    public String lookupNamespaceURI(final String s) {
        return null;
    }

    @Override
    public boolean isEqualNode(final Node node) {
        return false;
    }

    @Override
    public Object getFeature(final String s, final String s1) {
        return null;
    }

    @Override
    public Object setUserData(final String s, final Object o,
                              final UserDataHandler userDataHandler) {
        return null;
    }

    @Override
    public Object getUserData(final String s) {
        return null;
    }
}
