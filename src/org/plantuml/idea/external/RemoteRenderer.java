package org.plantuml.idea.external;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.IdeaWideProxySelector;
import org.apache.commons.httpclient.HttpStatus;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.rendering.RenderingType;
import org.plantuml.idea.settings.PlantUmlSettings;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RemoteRenderer {
    private static final Logger LOG = Logger.getInstance(RemoteRenderer.class);
    private static final Logger BODY_LOG = Logger.getInstance("#org.plantuml.idea.external.RemoteRenderer.body");

    public static RenderResult render(RenderRequest renderRequest) {
        long start = System.currentTimeMillis();
        PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();
        String source = renderRequest.getSource();
        boolean displaySvg = plantUmlSettings.isDisplaySvg();
        try {

            String encoded = PlantUmlFacade.get().encode(source);
            String type = displaySvg ? "/svg/" : "/png/";
            String url = plantUmlSettings.getServerPrefix() + type + encoded;
            LOG.debug("url: ", url);

            HttpRequest build = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .timeout(Duration.of(30, ChronoUnit.SECONDS))
                    .build();

            HttpClient.Builder builder = HttpClient
                    .newBuilder()
                    .followRedirects(HttpClient.Redirect.NEVER); //it is slow, better if user fixes the url

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

            int statusCode = response.statusCode();
            HttpHeaders headers = response.headers();

            RuntimeException runtimeException = null;
            if (out.length == 0) {
                URI uri = response.uri();
                String statusText = HttpStatus.getStatusText(statusCode);
                runtimeException = new RuntimeException(statusCode + ": " + statusText + "; uri=" + uri + "\nresponseHeaders=" + headers + "\nResponse Body was empty, check the configured url or proxy, redirects are prohibited for performance reasons.");
            }
            byte[] bytes;
            byte[] svgBytes;
            if (displaySvg) {
                bytes = out;
                svgBytes = bytes;
            } else {
                bytes = out;
                svgBytes = null;
            }
            String description = statusCode >= 400 || runtimeException != null ? ImageItem.ERROR : "OK";

            RenderResult renderResult = new RenderResult(RenderingType.REMOTE, 1);
            renderResult.addRenderedImage(new ImageItem(renderRequest.getBaseDir(), displaySvg ? ImageFormat.SVG : ImageFormat.PNG, source, source, 0, description, bytes, svgBytes, RenderingType.REMOTE, null, null, runtimeException));
            return renderResult;
        } catch (Throwable e) {
            LOG.warn(e);
            RenderResult renderResult = new RenderResult(RenderingType.REMOTE, 1);
            renderResult.addRenderedImage(new ImageItem(renderRequest.getBaseDir(), displaySvg ? ImageFormat.SVG : ImageFormat.PNG, source, source, 0, ImageItem.ERROR, null, null, RenderingType.REMOTE, null, null, e));
            return renderResult;
        } finally {
            LOG.debug("render done in ", System.currentTimeMillis() - start, "ms");
        }
    }
}
