package org.plantuml.idea.rendering;

import org.apache.commons.lang.builder.ToStringBuilder;
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
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.plantuml.idea.intentions.ReverseArrowIntention.logger;

public class ImageItem {

    private final int page;
    private final String description;
    private final byte[] imageBytes;
    private final RenderingType renderingType;
    private final BufferedImage image;
    private final UrlData[] urls;

    private final String pageSource;
    private String documentSource;

    public ImageItem(File baseDir, String documentSource, String pageSource, int page, String description, byte[] imageBytes, byte[] svgBytes, RenderingType renderingType) {
        this.pageSource = pageSource;
        this.documentSource = documentSource;
        this.page = page;
        this.description = description;
        this.imageBytes = imageBytes;
        this.renderingType = renderingType;
        try {
            this.image = UIUtils.getBufferedImage(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.urls = this.parseUrls(svgBytes, baseDir);
    }

    public ImageItem(int page, ImageItem description) {
        this.page = page;
        this.description = description.description;
        this.pageSource = description.pageSource;
        this.documentSource = description.documentSource;
        this.imageBytes = description.imageBytes;
        this.image = description.image;
        this.urls = description.urls;
        this.renderingType = description.renderingType;
    }

    public RenderingType getRenderingType() {
        return renderingType;
    }

    public void setDocumentSource(String documentSource) {
        this.documentSource = documentSource;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getDocumentSource() {
        return documentSource;
    }

    public String getDescription() {
        return description;
    }

    public int getPage() {
        return page;
    }

    public byte[] getImageBytes() {
        return imageBytes;
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
        if (svgData == null || svgData.length == 0 || baseDir == null) {
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
                .append("page", page)
                .append("pageSourceAttached", pageSource != null)
                .append("description", description)
                .append("diagramBytesLength", imageBytes == null ? "null" : imageBytes.length)
                .toString();
    }

}
