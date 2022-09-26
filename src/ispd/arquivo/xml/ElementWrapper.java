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
    public String getAttribute(String s) {
        return null;
    }

    @Override
    public void setAttribute(String s, String s1) throws DOMException {

    }

    @Override
    public void removeAttribute(String s) throws DOMException {

    }

    @Override
    public Attr getAttributeNode(String s) {
        return null;
    }

    @Override
    public Attr setAttributeNode(Attr attr) throws DOMException {
        return null;
    }

    @Override
    public Attr removeAttributeNode(Attr attr) throws DOMException {
        return null;
    }

    @Override
    public NodeList getElementsByTagName(String s) {
        return null;
    }

    @Override
    public String getAttributeNS(String s, String s1) throws DOMException {
        return null;
    }

    @Override
    public void setAttributeNS(String s, String s1, String s2) throws DOMException {

    }

    @Override
    public void removeAttributeNS(String s, String s1) throws DOMException {

    }

    @Override
    public Attr getAttributeNodeNS(String s, String s1) throws DOMException {
        return null;
    }

    @Override
    public Attr setAttributeNodeNS(Attr attr) throws DOMException {
        return null;
    }

    @Override
    public NodeList getElementsByTagNameNS(String s, String s1) throws DOMException {
        return null;
    }

    @Override
    public boolean hasAttribute(String s) {
        return false;
    }

    @Override
    public boolean hasAttributeNS(String s, String s1) throws DOMException {
        return false;
    }

    @Override
    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    @Override
    public void setIdAttribute(String s, boolean b) throws DOMException {

    }

    @Override
    public void setIdAttributeNS(String s, String s1, boolean b) throws DOMException {

    }

    @Override
    public void setIdAttributeNode(Attr attr, boolean b) throws DOMException {

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
    public void setNodeValue(String s) throws DOMException {

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
    public Node insertBefore(Node node, Node node1) throws DOMException {
        return null;
    }

    @Override
    public Node replaceChild(Node node, Node node1) throws DOMException {
        return null;
    }

    @Override
    public Node removeChild(Node node) throws DOMException {
        return null;
    }

    @Override
    public Node appendChild(Node node) throws DOMException {
        return null;
    }

    @Override
    public boolean hasChildNodes() {
        return false;
    }

    @Override
    public Node cloneNode(boolean b) {
        return null;
    }

    @Override
    public void normalize() {

    }

    @Override
    public boolean isSupported(String s, String s1) {
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
    public void setPrefix(String s) throws DOMException {

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
    public short compareDocumentPosition(Node node) throws DOMException {
        return 0;
    }

    @Override
    public String getTextContent() throws DOMException {
        return null;
    }

    @Override
    public void setTextContent(String s) throws DOMException {

    }

    @Override
    public boolean isSameNode(Node node) {
        return false;
    }

    @Override
    public String lookupPrefix(String s) {
        return null;
    }

    @Override
    public boolean isDefaultNamespace(String s) {
        return false;
    }

    @Override
    public String lookupNamespaceURI(String s) {
        return null;
    }

    @Override
    public boolean isEqualNode(Node node) {
        return false;
    }

    @Override
    public Object getFeature(String s, String s1) {
        return null;
    }

    @Override
    public Object setUserData(String s, Object o,
                              UserDataHandler userDataHandler) {
        return null;
    }

    @Override
    public Object getUserData(String s) {
        return null;
    }
}
