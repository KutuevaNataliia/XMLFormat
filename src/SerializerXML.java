import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class SerializerXML {
    private String dirName;
    private String xmlFilePath = "tryXML.xml";

    private Document document;

    public SerializerXML(String dirName) {
        this.dirName = dirName;
    }

    public void serialize() {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            File root = new File(dirName);
            if (!root.isDirectory()) {
                throw new RuntimeException("Неправильно задана директория");
            }
            Element rootElement = document.createElement(root.getName());
            document.appendChild(rootElement);
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    walkDirectory(file, rootElement);
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            e.printStackTrace();
        }
    }

    private void walkDirectory(File root, Element parentElement) throws IOException {
        if (root.isDirectory()) {
            Element element = document.createElement(root.getName());
            if (parentElement != null) {
                parentElement.appendChild(element);
            }
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    walkDirectory(file, element);
                }
            }
        } else {
            Element element = document.createElement("file");
            String filename = root.getName();
            element.setAttribute("filename", filename);
            String extension = filename.substring(filename.lastIndexOf(".") + 1);
            if ("bmp".equals(extension)) {
                BufferedImage bmp = ImageIO.read(root);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bmp, "bmp", baos);
                byte[] b = baos.toByteArray();
                String encodedImage = Base64.getEncoder().encodeToString(b);
                element.setAttribute("dt", "binary.base64");
                element.setTextContent(encodedImage);
            }
            if (parentElement != null) {
                parentElement.appendChild(element);
            }
        }
    }
}
