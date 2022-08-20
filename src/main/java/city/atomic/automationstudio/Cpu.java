package city.atomic.automationstudio;

import org.apache.commons.lang.NotImplementedException;
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

public class Cpu {
    private String cpuType;
    private File cpuConfigFile;
    private Document xml;

    private Cpu(String cpuType, File cpuConfigFile, Document xml) {
        this.cpuType = cpuType;
        this.cpuConfigFile = cpuConfigFile;
        this.xml = xml;
    }

    public static Cpu load(String cpuType, File cpuConfigFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(cpuConfigFile);
            return new Cpu(cpuType, cpuConfigFile, document);
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
            FileWriter writer = new FileWriter(cpuConfigFile);
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
