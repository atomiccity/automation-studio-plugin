package city.atomic.automationstudio;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Hardware {
    private File hardwareFile;
    private Document xml;

    private Hardware(File hardwareFile, Document xml) {
        this.hardwareFile = hardwareFile;
        this.xml = xml;
    }

    public static Hardware load(File hardwareFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(hardwareFile);
            return new Hardware(hardwareFile, document);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xml);
            FileWriter writer = new FileWriter(hardwareFile);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public String getConfigVersion() {
        try {
            Element root = xml.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("Module/Parameter[@ID='ConfigVersion']", root, XPathConstants.NODESET);
            if (nodes.getLength() == 1) {
                return nodes.item(0).getAttributes().getNamedItem("Value").getNodeValue();
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    public void setConfigVersion(String version) {
        try {
            Element root = xml.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("Module/Parameter[@ID='ConfigVersion']", root, XPathConstants.NODESET);
            if (nodes.getLength() == 1) {
                nodes.item(0).getAttributes().getNamedItem("Value").setNodeValue(version);
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
