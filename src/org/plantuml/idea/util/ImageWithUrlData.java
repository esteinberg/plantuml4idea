package org.plantuml.idea.util;

import com.intellij.openapi.diagnostic.Logger;
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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author koroandr
 *         18.06.15
 */
public class ImageWithUrlData {
    Logger logger = Logger.getInstance(ImageWithUrlData.class);

    public ImageWithUrlData(byte[] imageData, byte[] svgData, File baseDir) throws IOException {
        this.parseImage(imageData);
        this.parseUrls(svgData, baseDir);
    }

    public BufferedImage getImage() {
        return image;
    }

    public UrlData[] getUrls() {
        return urls;
    }

    public class UrlData {
        public UrlData(URI uri, Rectangle clickArea) {
            this.uri = uri;
            this.clickArea = clickArea;
        }

        public URI getUri() {
            return uri;
        }

        public Rectangle getClickArea() {
            return clickArea;
        }

        private URI uri;
        private Rectangle clickArea;
    }

    private void parseImage(byte[] imageData) throws IOException{
            this.image = UIUtils.getBufferedImage(imageData);
    }

    private void parseUrls(byte [] svgData, File baseDir) {
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
                urls[i] = createUrl(svgPaths.item(i), baseDir);
            }

            this.urls = urls;

        } catch (Exception e) {
            logger.warn(e);
            this.urls = new UrlData[0];
        }
    }

    private UrlData createUrl(Node linkNode, File baseDir) throws URISyntaxException {
        NamedNodeMap rectNodeAttributes = linkNode.getFirstChild().getAttributes();
        Rectangle rect = new Rectangle(
                (int) Float.parseFloat(rectNodeAttributes.getNamedItem("x").getNodeValue()),
                (int) Float.parseFloat(rectNodeAttributes.getNamedItem("y").getNodeValue()),
                (int) Float.parseFloat(rectNodeAttributes.getNamedItem("width").getNodeValue()),
                (int) Float.parseFloat(rectNodeAttributes.getNamedItem("height").getNodeValue())
        );

       return new UrlData(this.computeUri(linkNode.getAttributes().getNamedItem("xlink:href").getNodeValue(), baseDir), rect);
    }

    /**
     * If uri is a relative path, then assuming that full uri is file:/{path_to_diagram_file}/{uri}
     * @param url absolute or relative url
     * @return absolute uri
     */
    private URI computeUri(String url, File baseDir) throws URISyntaxException {
        URI uri = new URI(url);
        if (!uri.isAbsolute()) {
            //Concatenating baseDir and relative URI
            uri = new File(baseDir, url).toURI();
        }
        return uri;
    }

    private BufferedImage image;
    private UrlData[] urls;
}
