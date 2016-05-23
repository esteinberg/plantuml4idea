package org.plantuml.idea.util;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author koroandr
 *         18.06.15
 */
public class ImageWithUrlData {
    static Logger logger = Logger.getInstance(ImageWithUrlData.class);

    private final BufferedImage image;
    private final String documentSource;
    private String pageSource;
    private final String description;
    private final UrlData[] urls;

    public ImageWithUrlData(String documentSource, String pageSource, String description, @NotNull byte[] imageData, byte[] svgData, File baseDir) throws IOException {
        this.documentSource = documentSource;
        this.pageSource = pageSource;
        this.description = description;
        this.image = UIUtils.getBufferedImage(imageData);
        this.urls = this.parseUrls(svgData, baseDir);
    }

//    public ImageWithUrlData(ImageWithUrlData input, String newSource) {
//        this.documentSource = newSource;
//        this.pageSource = pageSource;
//        this.image = input.image;
//        this.description = input.description;
//        UrlData[] urls = input.getUrls();
//        if (urls != null) {
//            this.urls = new UrlData[urls.length];
//            for (int i = 0; i < urls.length; i++) {
//                this.urls[i] = urls[i];
//            }
//        } else {
//            this.urls = new UrlData[0];
//        } 
//    }
//
//    public static ImageWithUrlData deepCloneWithNewSource(ImageWithUrlData imageWithUrlData, String newSource) {
//        if (imageWithUrlData != null) {
//            return new ImageWithUrlData(imageWithUrlData, newSource);
//        }
//        return null;
//    }

    public void setPageSource(String pageSource) {
        this.pageSource = pageSource;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getDocumentSource() {
        return documentSource;
    }

    public String getPageSource() {
        return pageSource;
    }

    public UrlData[] getUrls() {
        return urls;
    }

    public class UrlData {
        private final URI uri;
        private final Rectangle clickArea;
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

    }

    private UrlData[] parseUrls(byte[] svgData, File baseDir) {
        if (svgData == null || svgData.length == 0) {
            return new UrlData[0];
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //http://stackoverflow.com/a/155874/685796
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(svgData));

            String xpathExpression = "//a";

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            XPathExpression expression = xpath.compile(xpathExpression);

            NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

            List<UrlData> urls = new ArrayList<UrlData>();
            for (int i = 0; i < svgPaths.getLength(); i++) {
                urls.addAll(createUrl(svgPaths.item(i), baseDir));
            }

            return urls.toArray(new UrlData[urls.size()]);

        } catch (Exception e) {
            logger.debug(e);
            return new UrlData[0];
        }
    }

    private List<UrlData> createUrl(Node linkNode, File baseDir) throws URISyntaxException {
        List<UrlData> urls = new ArrayList<UrlData>();

        URI url = this.computeUri(linkNode.getAttributes().getNamedItem("xlink:href").getNodeValue(), baseDir);

        for (int i = 0; i < linkNode.getChildNodes().getLength(); i++) {
            Node child = linkNode.getChildNodes().item(i);
            if (child.getNodeName().equals("rect")) {
                NamedNodeMap nodeAttributes = child.getAttributes();
                Rectangle rect = new Rectangle(
                        (int) Float.parseFloat(nodeAttributes.getNamedItem("x").getNodeValue()),
                        (int) Float.parseFloat(nodeAttributes.getNamedItem("y").getNodeValue()),
                        (int) Float.parseFloat(nodeAttributes.getNamedItem("width").getNodeValue()),
                        (int) Float.parseFloat(nodeAttributes.getNamedItem("height").getNodeValue())
                );

                urls.add(new UrlData(url, rect));
            }
        }

        return urls;
    }

    /**
     * If uri is a relative path, then assuming that full uri is file:/{path_to_diagram_file}/{uri}
     *
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("description", description)
                .append("hasImage", image != null)
                .toString();
    }
}
