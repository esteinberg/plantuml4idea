package org.plantuml.idea.external;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.IdeaWideProxySelector;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.Nullable;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoteRenderer {
    public static final Pattern NEWPAGE_PATTERN = Pattern.compile("^[ \t]*newpage.*$", Pattern.MULTILINE);
    private static final Logger LOG = Logger.getInstance(RemoteRenderer.class);
    private static final Logger BODY_LOG = Logger.getInstance("#org.plantuml.idea.external.RemoteRenderer.body");
    public static final int MAX_PAGES = 100;
    private static final ExecutorService executor = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), ConcurrencyUtil.newNamedThreadFactory("PlantUML integration plugin - RemoteRenderer", true, Thread.NORM_PRIORITY));

    public static RenderResult render(RenderRequest renderRequest) {
        long start = System.currentTimeMillis();
        PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();
        String source = renderRequest.getSource();
        ImageFormat format = renderRequest.getFormat();
        boolean displaySvg = format == ImageFormat.SVG;

        int pages = countPages(source);

        if (plantUmlSettings.isRemoteRenderingSinglePage()) {
            pages = 1;
        }

        int requestedPage = renderRequest.getPage();
        if (requestedPage >= pages) {
            requestedPage = -1;
        }
        RenderResult renderResult = new RenderResult(RenderingType.REMOTE, pages);

        try {
            HttpClient client = getHttpClient(plantUmlSettings);
            String encoded = PlantUmlFacade.get().encode(source);
            String type = displaySvg ? "/svg/" : "/png/";


            ArrayList<Callable<ImageItem>> tasks = new ArrayList<>();
            for (int i = 0; i < pages; i++) {
                int finalI = i;
                if (requestedPage != -1 && requestedPage != i) {
                    ImageItem imageItem = new ImageItem(renderRequest.getBaseDir(), renderRequest.getFormat(), source, source, i, RenderResult.TITLE_ONLY, null, null, RenderingType.REMOTE, null, null, null);
                    tasks.add(() -> imageItem);
                } else {
                    tasks.add(() -> renderPage(renderRequest, plantUmlSettings, source, format, client, encoded, type, finalI));
                }
            }

            List<Future<ImageItem>> futures = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
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

    private static ImageItem renderPage(RenderRequest renderRequest, PlantUmlSettings plantUmlSettings, String source, ImageFormat format, HttpClient client, String encoded, String type, int i) {
        try {
            String page;
            if (plantUmlSettings.isRemoteRenderingSinglePage()) {
                page = "";
            } else {
                page = i + "/";
            }

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
                BODY_LOG.debug("body: ", new String(out, StandardCharsets.UTF_8));
            }

            RuntimeException runtimeException = checkErrors(plantUmlSettings, response, out);

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
            String description = response.statusCode() > 299 || runtimeException != null ? ImageItem.ERROR : "OK";

            return new ImageItem(renderRequest.getBaseDir(), actualFormat, source, source, i, description, bytes, svgBytes, RenderingType.REMOTE, null, null, runtimeException);
        } catch (Throwable e) {
            LOG.warn(e);
            return new ImageItem(renderRequest.getBaseDir(), format, source, source, i, ImageItem.ERROR, null, null, RenderingType.REMOTE, null, null, e);
        }
    }

    @Nullable
    private static RuntimeException checkErrors(PlantUmlSettings plantUmlSettings, HttpResponse<byte[]> response, byte[] out) {
        int statusCode = response.statusCode();
        HttpHeaders headers = response.headers();

        RuntimeException runtimeException = null;
        if (statusCode < 200 || statusCode > 299) {
            URI uri = response.uri();
            String statusText = HttpStatus.getStatusText(statusCode);

            String s = "";
            if (!plantUmlSettings.isRemoteRenderingSinglePage()) {
                s = "Try to enable 'Single page' rendering, some providers do not support multiple pages.\n\n";
            }
            runtimeException = new RuntimeException(
                    +statusCode + ": " + statusText + "\n\n" + new String(out, StandardCharsets.UTF_8) + "\n\n" + s + "uri=" + uri + headers(headers));
        }

        if (out.length == 0) {
            String s = "";
            if (!plantUmlSettings.isRemoteRenderingSinglePage()) {
                s = "Try to enable 'Single page' rendering, some providers do not support multiple pages.\n\n";
            }
            URI uri = response.uri();
            String statusText = HttpStatus.getStatusText(statusCode);
            runtimeException = new RuntimeException(statusCode + ": " + statusText + "\n\nuri=" + uri + headers(headers) + "Response Body was empty, check the configured url or proxy, redirects are prohibited for performance reasons." + "\n\n" + s + "\n\n");
        }
        return runtimeException;
    }

    private static String headers(HttpHeaders headers) {
        StringBuilder sb = new StringBuilder("\n\nresponseHeaders=");
        Map<String, List<String>> map = headers.map();
        sb.append("{");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            sb.append("\n").append(entry);
        }
        sb.append("}\n\n");
        return sb.toString();
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
        Matcher matcher = NEWPAGE_PATTERN.matcher(source);
        while (matcher.find() && pages < MAX_PAGES) {
            pages++;
        }
        return pages;
    }
}
