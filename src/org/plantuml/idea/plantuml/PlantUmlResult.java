package org.plantuml.idea.plantuml;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlResult {
    private byte[] diagramBytes;
    private String description;
    private String error;
    private int pages;

    private int width;
    private int height;

    private SVGDocument document;

    public PlantUmlResult(byte[] diagramBytes, String description, String error, int pages) {
        this.diagramBytes = diagramBytes;
        this.description = description;
        this.error = error;
        this.pages = pages;

        generateSVGDocument();
    }

    public byte[] getDiagramBytes() {
        return diagramBytes;
    }

    public void setDiagramBytes(byte[] diagramBytes) {
        this.diagramBytes = diagramBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isError() {
        return (description == null || description.isEmpty());
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public SVGDocument getDocument() {
        return document;
    }

    @Override
    public String toString() {
        return "PlantUmlResult{" +
                "diagramBytes=" + Arrays.toString(diagramBytes) +
                ", description='" + description + '\'' +
                ", error='" + error + '\'' +
                '}';
    }

    private void generateSVGDocument() {
        try {
            String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(xmlParser);
            InputStream inputStream = new ByteArrayInputStream(diagramBytes);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf8");
            document = documentFactory.createSVGDocument("http://www.w3.org/2000/svg", inputStreamReader);
            UserAgentAdapter userAgent = new UserAgentAdapter();
            DocumentLoader documentLoader = new DocumentLoader(userAgent);
            BridgeContext bridgeContext = new BridgeContext(userAgent, documentLoader);
            bridgeContext.setDynamicState(BridgeContext.DYNAMIC);
            GVTBuilder builder = new GVTBuilder();
            GraphicsNode graphicsNode = builder.build(bridgeContext, document);

            Rectangle2D rect = graphicsNode.getGeometryBounds();

            width = (int) Math.round(rect.getWidth() * 1.1d);
            height = (int) Math.round(rect.getHeight() * 1.1d);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
