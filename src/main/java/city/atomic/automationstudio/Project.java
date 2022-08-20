package city.atomic.automationstudio;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private final File projectFile;
    private final Path projectRoot;

    private Project(File projectFile) {
        this.projectFile = projectFile;
        this.projectRoot = Paths.get(projectFile.getParent());
    }

    public static Project load(String projectFilePath) {
        return new Project(new File(projectFilePath));
    }

    public List<Config> getConfigs() {
        List<Config> configs = new ArrayList<Config>();

        try {
            File physicalPkg = new File(projectRoot.toString(), "Physical/Physical.pkg");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document =  builder.parse(physicalPkg);
            Element root = document.getDocumentElement();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("Objects/Object[@Type='Configuration']", root, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                String name = n.getTextContent();
                File configPkgFile = new File(projectRoot.toString(), "Physical/" + name + "/Config.pkg");
                Config config = Config.load(name, configPkgFile);
                configs.add(config);
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        return configs;
    }

    public Config findConfig(String name) {
        List<Config> configs = getConfigs();
        for (Config c : configs) {
            if (c.getName().equals(name)) {
                return c;
            }
        }

        return null;
    }
}
