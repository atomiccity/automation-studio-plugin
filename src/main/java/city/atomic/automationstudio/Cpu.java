package city.atomic.automationstudio;

import hudson.FilePath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Cpu {
    private final String cpuType;
    private final FilePath cpuConfigFile;
    private final Document xml;

    private Cpu(String cpuType, FilePath cpuConfigFile, Document xml) {
        this.cpuType = cpuType;
        this.cpuConfigFile = cpuConfigFile;
        this.xml = xml;
    }

    public static Cpu load(String cpuType, FilePath cpuConfigFile) {
        try (InputStream in = cpuConfigFile.read()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(in);
            return new Cpu(cpuType, cpuConfigFile, document);
        } catch (ParserConfigurationException | InterruptedException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try (OutputStream out = cpuConfigFile.write()) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xml);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (IOException | TransformerException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAdditionalBuildOptions() {
        try {
            Element root = xml.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("Configuration/Build", root, XPathConstants.NODESET);
            if (nodes.getLength() == 1) {
                return nodes.item(0).getAttributes().getNamedItem("AdditionalBuildOptions").getNodeValue();
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    public void setAdditionalBuildOptions(String options) {
        try {
            Element root = xml.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("Configuration/Build", root, XPathConstants.NODESET);
            if (nodes.getLength() == 1) {
                nodes.item(0).getAttributes().getNamedItem("AdditionalBuildOptions").setNodeValue(options);
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAnsicAdditionalBuildOptions() {
        try {
            Element root = xml.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("Configuration/Build", root, XPathConstants.NODESET);
            if (nodes.getLength() == 1) {
                return nodes.item(0).getAttributes().getNamedItem("AnsicAdditionalBuildOptions").getNodeValue();
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    public void setAnsicAdditionalBuildOptions(String options) {
        try {
            Element root = xml.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("Configuration/Build", root, XPathConstants.NODESET);
            if (nodes.getLength() == 1) {
                nodes.item(0).getAttributes().getNamedItem("AnsicAdditionalBuildOptions").setNodeValue(options);
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
