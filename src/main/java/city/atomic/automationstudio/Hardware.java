package city.atomic.automationstudio;

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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Hardware {
    private final File hardwareFile;
    private final Document xml;

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
        } catch (ParserConfigurationException | IOException | SAXException e) {
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
        } catch (IOException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public String getParameter(String moduleName, String id) {
        try {
            String path = moduleName != null
                    ? "Module[@Name='" + moduleName + "']/Parameter[@ID='" + id + "']"
                    : "Module/Parameter[@ID='" + id + "']";
            Element root = xml.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate(path, root, XPathConstants.NODESET);
            if (nodes.getLength() == 1) {
                return nodes.item(0).getAttributes().getNamedItem("Value").getNodeValue();
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    public String getParameter(String id) {
        return getParameter(null, id);
    }

    public void setParameter(String moduleName, String id, String value) {
        try {
            String path = moduleName != null
                    ? "Module[@Name='" + moduleName + "']/Parameter[@ID='" + id + "']"
                    : "Module/Parameter[@ID='" + id + "']";
            Element root = xml.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate(path, root, XPathConstants.NODESET);
            if (nodes.getLength() == 1) {
                nodes.item(0).getAttributes().getNamedItem("Value").setNodeValue(value);
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public void setParameter(String id, String value) {
        setParameter(null, id, value);
    }

    public String getConfigVersion() {
        return getParameter("ConfigVersion");
    }

    public void setConfigVersion(String version) {
        setParameter("ConfigVersion", version);
    }

    public int getWebServerPort() {
        try {
            String portStr = getParameter("WebServerPort");
            return Integer.parseInt(portStr);
        } catch (Exception e) {
            // Default port is 80
            return 80;
        }
    }

    public void setWebServerPort(int port) {
        setParameter("WebServerPort", Integer.toString(port));
    }
}
