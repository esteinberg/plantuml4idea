package org.plantuml.idea.rendering;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    @Nullable
    private final String description;
    @NotNull
    private final RenderingType renderingType;
    @Nullable
    private BufferedImage image;
    @NotNull
    private final UrlData[] urls;
    @Nullable
    private final String title;
    /**
     * could have been in RenderResult, since it is currently the same for all pages
     */
    @Nullable
    private final String filename;
    @Nullable
    private final String pageSource;
    @NotNull
    private final String documentSource;
    private byte[] imageBytes;

    public ImageItem(@Nullable File baseDir,
                     @NotNull String documentSource,
                     @Nullable String pageSource,
                     int page,
                     @Nullable String description,
                     @Nullable byte[] imageBytes,
                     @Nullable byte[] svgBytes,
                     @NotNull RenderingType renderingType,
                     @Nullable String title,
                     @Nullable String filename) {
        this.pageSource = pageSource;
        this.documentSource = documentSource;
        this.page = page;
        this.description = description;
        this.renderingType = renderingType;
        this.title = title;
        this.filename = filename;
        this.imageBytes = imageBytes;
        this.urls = this.parseUrls(svgBytes, baseDir);
    }

    public ImageItem(int page, ImageItem item) {
        this.page = page;
        this.description = item.description;
        this.pageSource = item.pageSource;
        this.documentSource = item.documentSource;
        this.image = item.image;
        this.urls = item.urls;
        this.imageBytes = item.imageBytes;
        this.renderingType = item.renderingType;
        this.title = item.title;
        this.filename = item.filename;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @NotNull
    public RenderingType getRenderingType() {
        return renderingType;
    }


    @Nullable
    public BufferedImage getImage() {
        return image;
    }

    public void setImage(@Nullable BufferedImage image) {
        this.image = image;
    }

    @NotNull
    public String getDocumentSource() {
        return documentSource;
    }

    @Nullable
    public String getFilename() {
        return filename;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public int getPage() {
        return page;
    }

    public boolean hasImage() {
        return image != null;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    @Nullable
    public String getPageSource() {
        return pageSource;
    }

    @NotNull
    public UrlData[] getUrls() {
        return urls;
    }

    protected boolean hasError() {
        String description = getDescription();
        if (description == null || description.isEmpty() || "(Error)".equals(description)) {
            return true;
        }
        return false;
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

    /**
     * <a href="ddd" target="_top" title="ddd" xlink:actuate="onRequest" xlink:href="ddd" xlink:show="new"
     * xlink:title="ddd" xlink:type="simple">
     * <text fill="#0000FF" font-family="sans-serif" font-size="13" lengthAdjust="spacingAndGlyphs"
     * text-decoration="underline" textLength="21" x="1235.5" y="3587.8857">ddd
     * </text>
     * </a>
     */
    private List<UrlData> createUrl(Node linkNode, File baseDir) throws URISyntaxException {
        List<UrlData> urls = new ArrayList<UrlData>();

        URI url = this.computeUri(linkNode.getAttributes().getNamedItem("xlink:href").getNodeValue(), baseDir);

        for (int i = 0; i < linkNode.getChildNodes().getLength(); i++) {
            Node child = linkNode.getChildNodes().item(i);
            if (child.getNodeName().equals("text")) {
                NamedNodeMap nodeAttributes = child.getAttributes();
                int x = (int) Float.parseFloat(nodeAttributes.getNamedItem("x").getNodeValue());
                int y = (int) Float.parseFloat(nodeAttributes.getNamedItem("y").getNodeValue());
                int textLength = (int) Float.parseFloat(nodeAttributes.getNamedItem("textLength").getNodeValue());
                int height = (int) Float.parseFloat(nodeAttributes.getNamedItem("font-size").getNodeValue());

                Rectangle rect = new Rectangle(
                        x,
                        y - height,
                        textLength,
                        height
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
                .append("description", description)
                .append("title", title)
                .append("hasImage", hasImage())
                .toString();
    }

}
