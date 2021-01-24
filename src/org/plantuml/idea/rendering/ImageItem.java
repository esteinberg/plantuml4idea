package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.scale.ScaleContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.intellij.images.ui.ImageComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.toolwindow.image.ImageContainerSvg;
import org.plantuml.idea.toolwindow.image.svg.MyImageEditorImpl;
import org.plantuml.idea.toolwindow.image.svg.MyImageEditorUI;
import org.plantuml.idea.util.UIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.plantuml.idea.intentions.ReverseArrowIntention.logger;

public class ImageItem {
    private static final Logger LOG = Logger.getInstance(ImageItem.class);

    private final int page;
    @Nullable
    private final String description;
    @NotNull
    private final RenderingType renderingType;
    @Nullable
    private BufferedImage image;
    @NotNull
    private final List<LinkData> links;
    @Nullable
    private final String title;
    /**
     * could have been in RenderResult, since it is currently the same for all pages
     */
    @Nullable
    private final String filename;
    @NotNull
    private final PlantUml.ImageFormat format;
    @Nullable
    private final String pageSource;
    @NotNull
    private final String documentSource;
    private byte[] imageBytes;
    private MyImageEditorImpl editor;

    public ImageItem(@Nullable File baseDir,
                     @NotNull PlantUml.ImageFormat format,
                     @NotNull String documentSource,
                     @Nullable String pageSource,
                     int page,
                     @Nullable String description,
                     @Nullable byte[] imageBytes,
                     @Nullable byte[] svgBytes,
                     @NotNull RenderingType renderingType,
                     @Nullable String title,
                     @Nullable String filename) {
        this.format = format;
        this.pageSource = pageSource;
        this.documentSource = documentSource;
        this.page = page;
        this.description = description;
        this.renderingType = renderingType;
        this.title = title;
        this.filename = filename;
        this.imageBytes = imageBytes;

        this.links = this.parseLinks(svgBytes, baseDir);
    }

    public ImageItem(int page, ImageItem item, @NotNull PlantUml.ImageFormat format) {
        this.page = page;
        this.description = item.description;
        this.pageSource = item.pageSource;
        this.documentSource = item.documentSource;
        this.image = item.image;
        this.links = item.links;
        this.imageBytes = item.imageBytes;
        this.renderingType = item.renderingType;
        this.title = item.title;
        this.filename = item.filename;
        this.format = format;
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
    public BufferedImage getImage(Project project, RenderRequest renderRequest, RenderResult renderResult) {
        if (image == null && imageBytes != null) {
            initImage(project, renderRequest, renderResult);
        }
        return image;
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

    public boolean hasImageBytes() {
        return imageBytes != null;
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
        if (description == null || description.isEmpty() || "(Error)".equals(description)) {
            return true;
        }
        return false;
    }

    void initImage(Project project, RenderRequest renderRequest, RenderResult renderResult) {
        long start = System.currentTimeMillis();
        if (getImageBytes() != null) {
            if (format == PlantUml.ImageFormat.PNG) {
                try {
                    this.image = UIUtils.getBufferedImage(getImageBytes());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (format == PlantUml.ImageFormat.SVG) {  //could be done parallelly
                MyImageEditorImpl init = getEditor(project, renderRequest, renderResult);
            }
        }
        LOG.debug("initImage done in ", System.currentTimeMillis() - start, "ms");
    }

    public MyImageEditorImpl getEditor(final Project project, final RenderRequest renderRequest, final RenderResult renderResult) {
        if (editor != null) {
            return editor;
        }
        long start = System.currentTimeMillis();

        LightVirtualFile virtualFile = new LightVirtualFile("svg image.svg", new String(getImageBytes(), StandardCharsets.UTF_8));
        this.editor = new MyImageEditorImpl(project, virtualFile, true, renderRequest.getZoom());
        ImageComponent imageComponent = this.editor.getComponent().getImageComponent();
        JComponent contentComponent = this.editor.getContentComponent();

        this.editor.setTransparencyChessboardVisible(PlantUmlSettings.getInstance().isShowChessboard());

        if (hasError()) {
            imageComponent.setTransparencyChessboardWhiteColor(Color.BLACK);
            imageComponent.setTransparencyChessboardBlankColor(Color.BLACK);
        }

        contentComponent.addPropertyChangeListener(MyImageEditorUI.ZOOM_FACTOR_PROP, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                Double scale = (Double) propertyChangeEvent.getNewValue();
                ImageContainerSvg.updateLinks(contentComponent, ScaleContext.create(editor.getComponent()), scale);
            }
        });

        contentComponent.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(Component comp, int x, int y) {
                ImageContainerSvg.ACTION_POPUP_MENU.getComponent().show(comp, x, y);
            }
        });

        ImageContainerSvg.initLinks(project, this, renderRequest, renderResult, contentComponent, ScaleContext.create(this.editor.getComponent()), renderRequest.getZoom().getDoubleScaledZoom());
        LOG.debug("init getEditor done in ", System.currentTimeMillis() - start, "ms");

        return this.editor;
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
        if (svgData == null || svgData.length == 0 || baseDir == null) {
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
        } catch (Exception e) {
            logger.debug(e);
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
            urls.add(textNodeToUrlData(textContent, node, false));
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
                urls.add(textNodeToUrlData(nodeValue, child, true));
            }
        }

        return urls;
    }

    @NotNull
    private LinkData textNodeToUrlData(String text, Node child, boolean link) {
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

        return new LinkData(text, rect, link);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("page", page)
                .append("description", description)
                .append("title", title)
                .append("hasImageBytes", hasImageBytes())
                .toString();
    }

}
