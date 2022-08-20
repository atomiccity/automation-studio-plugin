package city.atomic.automationstudio;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

public class Config {
    private final String name;
    private final File configPkgFile;

    private Config(String name, File configPkgFile) {
        this.name = name;
        this.configPkgFile = configPkgFile;
    }

    public static Config load(String name, File configPkgFile) {
        return new Config(name, configPkgFile);
    }

    public String getName() {
        return name;
    }

    public Cpu getCpu() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document =  builder.parse(configPkgFile);
            Element root = document.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("Objects/Object[@Type='Cpu']", root, XPathConstants.NODESET);

            if (nodes.getLength() == 1) {
                String cpuType = nodes.item(0).getTextContent();
                return Cpu.load(cpuType, new File(configPkgFile.getParent(), cpuType + "/Cpu.pkg"));
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public Hardware getHardware() {
        return Hardware.load(new File(configPkgFile.getParent(), "Hardware.hw"));
    }
}
