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
import org.plantuml.idea.util.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoteRenderer {
    private static final Logger LOG = Logger.getInstance(RemoteRenderer.class);
    private static final Logger BODY_LOG = Logger.getInstance("#org.plantuml.idea.external.RemoteRenderer.body");
    public static final int MAX_PAGES = 100;

    public static RenderResult render(RenderRequest renderRequest) {
        long start = System.currentTimeMillis();
        PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();
        String source = renderRequest.getSource();
        ImageFormat format = renderRequest.getFormat();
        boolean displaySvg = format == ImageFormat.SVG;

        int pages = countPages(source);
        RenderResult renderResult = new RenderResult(RenderingType.REMOTE, pages);

        try {
            HttpClient client = getHttpClient(plantUmlSettings);
            String encoded = PlantUmlFacade.get().encode(source);
            String type = displaySvg ? "/svg/" : "/png/";

            ArrayList<Callable<ImageItem>> tasks = new ArrayList<>();
            for (int i = 0; i < pages; i++) {
                int finalI = i;
                tasks.add(() -> renderPage(renderRequest, plantUmlSettings, source, format, renderResult, client, encoded, type, finalI));
            }

            List<Future<ImageItem>> futures = ForkJoinPool.commonPool().invokeAll(tasks, 30, TimeUnit.SECONDS);
            for (Future<ImageItem> future : futures) {
                renderResult.addRenderedImage(future.get());
            }

            return renderResult;
        } catch (Throwable e) {
            LOG.warn(e);
            renderResult.addRenderedImage(new ImageItem(renderRequest.getBaseDir(), format, source, source, 0, ImageItem.ERROR, null, null, RenderingType.REMOTE, null, null, e));
            return renderResult;
        } finally {
            LOG.debug("render done in ", System.currentTimeMillis() - start, "ms");
        }
    }

    private static ImageItem renderPage(RenderRequest renderRequest, PlantUmlSettings plantUmlSettings, String source, ImageFormat format, RenderResult renderResult, HttpClient client, String encoded, String type, int i) {
        try {
            String page = i + "/";
            String url = plantUmlSettings.getServerPrefix() + type + page + encoded;
            LOG.debug("url: ", url);

            HttpRequest build = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .timeout(Duration.of(30, ChronoUnit.SECONDS))
                    .build();

            HttpResponse<byte[]> response = client.send(build, HttpResponse.BodyHandlers.ofByteArray());
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

            ImageFormat actualFormat = Utils.isPng(out) ? ImageFormat.PNG : format;

            byte[] bytes;
            byte[] svgBytes;
            if (actualFormat == ImageFormat.SVG) {
                bytes = out;
                svgBytes = bytes;
            } else {
                bytes = out;
                svgBytes = null;
            }
            String description = statusCode >= 400 || runtimeException != null ? ImageItem.ERROR : "OK";

            return new ImageItem(renderRequest.getBaseDir(), actualFormat, source, source, i, description, bytes, svgBytes, RenderingType.REMOTE, null, null, runtimeException);
        } catch (Throwable e) {
            LOG.warn(e);
            return new ImageItem(renderRequest.getBaseDir(), format, source, source, i, ImageItem.ERROR, null, null, RenderingType.REMOTE, null, null, e);
        }
    }

    private static HttpClient getHttpClient(PlantUmlSettings plantUmlSettings) {
        HttpClient.Builder builder = HttpClient
                .newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER); //it is slow, better if user fixes the url

        if (plantUmlSettings.isUseProxy()) {
            builder.proxy(new IdeaWideProxySelector(HttpConfigurable.getInstance()));
        } else {
            builder.proxy(HttpClient.Builder.NO_PROXY);
        }
        HttpClient client = builder.connectTimeout(Duration.of(5, ChronoUnit.SECONDS)).build();
        return client;
    }

    private static int countPages(String source) {
        int pages = 1;
        Pattern pattern = Pattern.compile("^[ \t]*newpage.*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find() && pages < MAX_PAGES) {
            pages++;
        }
        return pages;
    }
}
