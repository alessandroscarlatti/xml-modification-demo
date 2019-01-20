package com.scarlatti;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/**
 * @author Alessandro Scarlatti
 * @since Friday, 1/18/2019
 */
public class XmlModificationTest {
    @Test
    public void testXmlModification() throws Exception {
        String xml = readString(getClass(), "original.xml");
        Document doc = readDocumentFromString(xml);

        NodeList nodeList = getNodesByXpath(doc, "/penguin/friend");
        assertEquals(3, nodeList.getLength());

        String string = getStringByXpath(doc, "/penguin/friend[3]");
        assertEquals("qwer", string);

        Node friend1 = getNodeByXPath(doc, "/penguin/friend[1]");
        assertEquals("asdf", friend1.getTextContent());

        Node penguin = getNodeByXPath(doc, "/penguin");
        Node comment = doc.createComment("Removed friend 1");
        penguin.replaceChild(comment, friend1);

        Node scousin = getNodeByXPath(doc, "/penguin/scousin");
        Node commentScousin = doc.createComment(toXmlString(scousin));
        penguin.replaceChild(commentScousin, scousin);

        Node friend2 = getNodeByXPath(doc, "/penguin/friend[2]");
        penguin.removeChild(friend2);

        System.out.println(toXmlString(doc));
    }

    public static class XmlNodeVisitor {
        public void visitTextNode() {
        }

        public void visitParentNode() {
        }
    }

    public static String getStringByXpath(Document document, String xpath) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            return (String) xPath.compile(xpath).evaluate(document, XPathConstants.STRING);
        } catch (Exception e) {
            throw new RuntimeException("Error compiling XPath expression", e);
        }
    }

    public static NodeList getNodesByXpath(Document document, String xpath) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            return (NodeList) xPath.compile(xpath).evaluate(document, XPathConstants.NODESET);
        } catch (Exception e) {
            throw new RuntimeException("Error compiling XPath expression", e);
        }
    }

    public static Node getNodeByXPath(Document document, String xpath) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            return (Node) xPath.compile(xpath).evaluate(document, XPathConstants.NODE);
        } catch (Exception e) {
            throw new RuntimeException("Error compiling XPath expression", e);
        }
    }

    public static Document readDocumentFromString(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder b = f.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            return b.parse(is);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing document from string.", e);
        }
    }

    public static String readString(Class<?> clazz, String resource) {
        return new Scanner(clazz.getResourceAsStream(resource)).useDelimiter("\\Z").next();
    }

    public static String toXmlString(Node node) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            Document document = f.newDocumentBuilder().newDocument();
            Node clone = node.cloneNode(true);
            document.adoptNode(clone);
            document.appendChild(clone);
            return toXmlString(document);
        } catch (Exception e) {
            throw new RuntimeException("Error creating xml string", e);
        }
    }

    public static String toXmlString(Document document) {
        try {
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.METHOD, "xml");
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource domSource = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            StreamResult sr = new StreamResult(stringWriter);
            tf.transform(domSource, sr);

            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error converting XML document to string.", e);
        }
    }
}
