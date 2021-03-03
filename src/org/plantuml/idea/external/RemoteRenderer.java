package org.plantuml.idea.external;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.IdeaWideProxySelector;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.rendering.RenderingType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RemoteRenderer {
    private static final Logger LOG = Logger.getInstance(RemoteRenderer.class);
    private static final Logger BODY_LOG = Logger.getInstance("#org.plantuml.idea.external.RemoteRenderer.body");

    public static RenderResult render(RenderRequest renderRequest) {
        long start = System.currentTimeMillis();
        try {
            PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();

            String source = renderRequest.getSource();
            String encoded = PlantUmlFacade.get().encode(source);

            boolean displaySvg = plantUmlSettings.isDisplaySvg();
            String type = displaySvg ? "svg/" : "png/";
            String url = plantUmlSettings.getServerUrl() + type + encoded;
            LOG.debug("url: ", url);

            HttpRequest build = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .timeout(Duration.of(30, ChronoUnit.SECONDS))
                    .build();

            HttpClient.Builder builder = HttpClient.newBuilder();
            if (plantUmlSettings.isUseProxy()) {
                builder.proxy(new IdeaWideProxySelector(HttpConfigurable.getInstance()));
            } else {
                builder.proxy(HttpClient.Builder.NO_PROXY);
            }
            HttpClient client = builder.connectTimeout(Duration.of(5, ChronoUnit.SECONDS)).build();

            HttpResponse.BodyHandler<byte[]> bodyHandler = HttpResponse.BodyHandlers.ofByteArray();
            HttpResponse<byte[]> response = client.send(build, bodyHandler);
            byte[] out = response.body();
            LOG.debug("", response);
            if (BODY_LOG.isDebugEnabled()) {
                BODY_LOG.debug("body: ", new String(out));
            }

            RenderResult renderResult = new RenderResult(RenderingType.REMOTE, 1);
            byte[] bytes;
            byte[] svgBytes;
            if (displaySvg) {
                bytes = out;
                svgBytes = bytes;
            } else {
                bytes = out;
                svgBytes = null;
            }
            String description = response.statusCode() >= 400 ? "(Error)" : "OK";
            renderResult.addRenderedImage(new ImageItem(renderRequest.getBaseDir(), displaySvg ? ImageFormat.SVG : ImageFormat.PNG, source, source, 0, description, bytes, svgBytes, RenderingType.REMOTE, null, null));

            return renderResult;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            LOG.debug("render done in ", System.currentTimeMillis() - start, "ms");
        }
    }
}
