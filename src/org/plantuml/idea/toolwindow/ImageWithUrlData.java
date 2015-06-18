package org.plantuml.idea.toolwindow;

import org.plantuml.idea.util.UIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author koroandr
 *         18.06.15
 */
public class ImageWithUrlData {
    public ImageWithUrlData(byte[] imageData, byte[] svgData) throws IOException {
        this.parseImage(imageData);
        if (svgData == null) {
            this.urls = new UrlData[0];
        } else {
            this.parseUrls(svgData);
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public UrlData[] getUrls() {
        return urls;
    }

    public class UrlData {
        public UrlData(String url, Rectangle clickArea) {
            this.url = url;
            this.clickArea = clickArea;
        }

        public String getUrl() {
            return url;
        }

        public Rectangle getClickArea() {
            return clickArea;
        }

        private String url;
        private Rectangle clickArea;
    }

    private void parseImage(byte[] imageData) throws IOException{
            this.image = UIUtils.getBufferedImage(imageData);
    }

    private void parseUrls(byte [] svgData) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(svgData));

            String xpathExpression = "//a";

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            XPathExpression expression = xpath.compile(xpathExpression);

            NodeList svgPaths = (NodeList)expression.evaluate(document, XPathConstants.NODESET);

            UrlData[] urls = new UrlData[svgPaths.getLength()];
            for (int i = 0; i < svgPaths.getLength(); i++) {
                urls[i] = createUrl(svgPaths.item(i));
            }

            this.urls = urls;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private UrlData createUrl(Node linkNode) {
        NamedNodeMap rectNodeAttributes = linkNode.getFirstChild().getAttributes();
        Rectangle rect = new Rectangle(
                (int) Float.parseFloat(rectNodeAttributes.getNamedItem("x").getNodeValue()),
                (int) Float.parseFloat(rectNodeAttributes.getNamedItem("y").getNodeValue()),
                (int) Float.parseFloat(rectNodeAttributes.getNamedItem("width").getNodeValue()),
                (int) Float.parseFloat(rectNodeAttributes.getNamedItem("height").getNodeValue())
        );

       return new UrlData(linkNode.getAttributes().getNamedItem("xlink:href").getNodeValue(), rect);
    }

    private BufferedImage image;
    private UrlData[] urls;
}
