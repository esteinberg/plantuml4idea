package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.image.ImageContainerSvg;
import org.plantuml.idea.preview.image.svg.MyImageEditorImpl;
import org.plantuml.idea.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;

import static org.plantuml.idea.intentions.ReverseArrowIntention.logger;

public class ImageItem {
    public static final String ERROR = "(Error)";
    private static final Logger LOG = Logger.getInstance(ImageItem.class);

    private final int page;
    @Nullable
    private final String description;
    @NotNull
    private final RenderingType renderingType;
    @NotNull
    private List<LinkData> links;
    @Nullable
    private final String title;
    @Nullable
    private final String customFileName;
    @NotNull
    private final ImageFormat format;
    @Nullable
    private final String pageSource;
    @NotNull
    private final String documentSource;
    private final byte[] imageBytes;
    private Throwable exception;

    private final Map<PlantUmlPreviewPanel, ImageItemComponent> componentMap = new HashMap<>();
    private BufferedImage bufferedImage;

    public ImageItem(@Nullable File baseDir,
                     @NotNull ImageFormat format,
                     @NotNull String documentSource,
                     @Nullable String pageSource,
                     int page,
                     @Nullable String description,
                     @Nullable byte[] imageBytes,
                     @Nullable byte[] svgBytes,
                     @NotNull RenderingType renderingType,
                     @Nullable String title,
                     @Nullable String customFileName,
                     @Nullable Throwable exception) {
        this.format = format;
        this.pageSource = pageSource;
        this.documentSource = documentSource;
        this.page = page;
        this.description = description;
        this.renderingType = renderingType;
        this.title = title;
        this.customFileName = customFileName;
        this.imageBytes = imageBytes;
        this.exception = exception;

        this.links = this.parseLinks(svgBytes, baseDir);
    }

    public ImageItem(int page, ImageItem item, @NotNull ImageFormat format) {
        this.page = page;
        this.description = item.description;
        this.pageSource = item.pageSource;
        this.documentSource = item.documentSource;
        this.componentMap.putAll(item.componentMap);
        this.links = item.links;
        this.imageBytes = item.imageBytes;
        this.renderingType = item.renderingType;
        this.title = item.title;
        this.customFileName = item.customFileName;
        this.format = format;
        this.exception = item.exception;
    }

    @NotNull
    public ImageFormat getFormat() {
        return format;
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
    public BufferedImage getImage(PlantUmlPreviewPanel previewPanel, Project project, RenderRequest renderRequest, RenderResult renderResult) {
        ImageItemComponent imageItemComponent = getImageItemComponent(previewPanel);
        if (imageItemComponent.image == null && hasImageBytes()) {
            imageItemComponent = initImage(project, renderRequest, renderResult, previewPanel);
        }
        if (imageItemComponent == null) {
            return null;
        }
        return imageItemComponent.getImage();
    }

    @NotNull
    public String getDocumentSource() {
        return documentSource;
    }

    @Nullable
    public String getCustomFileName() {
        return customFileName;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public int getPage() {
        return page;
    }

    public boolean hasImageBytes() {
        return imageBytes != null && imageBytes.length > 0;
    }

    public Throwable getException() {
        return exception;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    @Nullable
    public String getPageSource() {
        return pageSource;
    }

    @NotNull
    public List<LinkData> getLinks() {
        return links;
    }

    public boolean hasError() {
        String description = getDescription();
        if (description == null || description.isEmpty() || ImageItem.ERROR.equals(description)) {
            return true;
        }
        return false;
    }

    ImageItemComponent initImage(Project project, RenderRequest renderRequest, RenderResult renderResult, PlantUmlPreviewPanel previewPanel) {
        ImageItemComponent imageItemComponent = getImageItemComponent(previewPanel);

        if (imageItemComponent.isNull() && hasImageBytes()) {
            long start = System.currentTimeMillis();
            if (format == ImageFormat.PNG) {
                initPng(imageItemComponent);
            } else if (format == ImageFormat.SVG) {
                imageItemComponent.editor = ImageContainerSvg.initEditor(previewPanel, this, project, renderRequest, renderResult);
            }
            LOG.debug("initImage done in ", System.currentTimeMillis() - start, "ms");
        }
        return imageItemComponent;
    }

    /**
     * it seems that BufferedImage can be shared in multiple components
     */
    private synchronized void initPng(ImageItemComponent imageItemComponent) {
        try {
            if (bufferedImage == null) {
                bufferedImage = Utils.getBufferedImage(getImageBytes());
            }
            imageItemComponent.image = bufferedImage;
            if (imageItemComponent.image == null) {
                LOG.error("image not generated, imageBytes.length :" + getImageBytes().length);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MyImageEditorImpl getEditor(PlantUmlPreviewPanel previewPanel, final Project project, final RenderRequest renderRequest, final RenderResult renderResult) {
        ImageItemComponent imageItemComponent = getImageItemComponent(previewPanel);
        if (imageItemComponent.editor == null) {
            imageItemComponent.editor = ImageContainerSvg.initEditor(previewPanel, this, project, renderRequest, renderResult);
        }
        return imageItemComponent.editor;
    }

    private synchronized ImageItemComponent getImageItemComponent(PlantUmlPreviewPanel previewPanel) {
        ImageItemComponent imageItemComponent = componentMap.get(previewPanel);
        if (imageItemComponent == null) {
            imageItemComponent = new ImageItemComponent();
            if (previewPanel != null) {
                componentMap.put(previewPanel, imageItemComponent);
            }
        }
        return imageItemComponent;
    }

    /**
     * items are shared between RenderCacheItem
     */
    @Deprecated
    public void dispose() {
//        if (editor != null) {
//            Disposer.dispose(editor);
//        }
    }

    public class LinkData {
        private final String text;
        private final Rectangle clickArea;
        private final boolean link;

        public LinkData(String text, Rectangle clickArea, boolean link) {
            this.text = text;
            this.clickArea = clickArea;
            this.link = link;
        }

        public String getText() {
            return text;
        }

        public boolean isLink() {
            return link;
        }

        public Rectangle getClickArea() {
            return clickArea;
        }

        @Override
        public String toString() {
            return "LinkData{" +
                    "text='" + text + '\'' +
                    ", link=" + link +
                    ", clickArea=" + clickArea +
                    '}';
        }
    }

    private List<LinkData> parseLinks(byte[] svgData, File baseDir) {
        if (svgData == null || svgData.length == 0) {
            return Collections.emptyList();
        }
        try {
            long start = System.currentTimeMillis();
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

            ArrayList<LinkData> linkData = new ArrayList<>();
            linkData.addAll(parseHyperLinks(document));
            linkData.addAll(parseText(document));

            LOG.debug("parseLinks done in ", System.currentTimeMillis() - start, "ms");
            return linkData;
        } catch (Throwable e) {
            logger.warn(e);
            return Collections.emptyList();
        }
    }

    @NotNull
    private List<LinkData> parseHyperLinks(Document document) throws XPathExpressionException, URISyntaxException {
        String xpathExpression = "//a";

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);

        NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        List<LinkData> urls = new ArrayList<LinkData>();
        for (int i = 0; i < svgPaths.getLength(); i++) {
            urls.addAll(createLink(svgPaths.item(i)));
        }
        return urls;
    }

    private Collection<? extends LinkData> parseText(Document document) throws XPathExpressionException {
        String xpathExpression = "//text";

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);

        NodeList nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        List<LinkData> urls = new ArrayList<LinkData>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getParentNode() != null && node.getParentNode().getNodeName().equals("a")) {
                continue;
            }
            String textContent = node.getTextContent();
            if (StringUtils.isEmpty(textContent)) {
                continue;
            }
            LinkData e = textNodeToUrlData(textContent, node, false);
            if (e != null) {
                urls.add(e);
            }
        }
        return urls;
    }

    /**
     * <a href="ddd" target="_top" title="ddd" xlink:actuate="onRequest" xlink:href="ddd" xlink:show="new"
     * xlink:title="ddd" xlink:type="simple">
     * <text fill="#0000FF" font-family="sans-serif" font-size="13" lengthAdjust="spacingAndGlyphs"
     * text-decoration="underline" textLength="21" x="1235.5" y="3587.8857">ddd
     * </text>
     * </a>
     */
    private List<LinkData> createLink(Node linkNode) throws URISyntaxException {
        List<LinkData> urls = new ArrayList<LinkData>();

        String nodeValue = linkNode.getAttributes().getNamedItem("xlink:href").getNodeValue();

        for (int i = 0; i < linkNode.getChildNodes().getLength(); i++) {
            Node child = linkNode.getChildNodes().item(i);
            if (child.getNodeName().equals("text")) {
                LinkData e = textNodeToUrlData(nodeValue, child, true);
                if (e != null) {
                    urls.add(e);
                }
            }
        }

        return urls;
    }

    private LinkData textNodeToUrlData(String text, Node child, boolean link) {
        NamedNodeMap nodeAttributes = child.getAttributes();
        int x = (int) Float.parseFloat(nodeAttributes.getNamedItem("x").getNodeValue());
        int y = (int) Float.parseFloat(nodeAttributes.getNamedItem("y").getNodeValue());
        Node textLength1 = nodeAttributes.getNamedItem("textLength");
        if (textLength1 == null) {
            return null;
        }
        int textLength = (int) Float.parseFloat(textLength1.getNodeValue());
        int height = (int) Float.parseFloat(nodeAttributes.getNamedItem("font-size").getNodeValue());

        Rectangle rect = new Rectangle(
                x,
                y - height,
                textLength,
                height
        );

        return new LinkData(text, rect, link);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("format", format)
                .append("filename", customFileName)
                .append("page", page)
                .append("description", description)
                .append("title", title)
                .append("imageBytes.length", imageBytes != null ? imageBytes.length : null)
                .toString();
    }

}
