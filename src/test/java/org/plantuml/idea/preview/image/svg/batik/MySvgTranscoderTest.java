package org.plantuml.idea.preview.image.svg.batik;

import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.ImageLoader;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MySvgTranscoderTest {
	@Test
	public void sanitize() throws IOException, TranscoderException {
		renderSvg("src/test/resources/testData/test2.svg");
		renderSvg("src/test/resources/testData/test3.svg");

	}

	private static void renderSvg(String pathname) throws IOException, TranscoderException {
		String text = FileUtils.readFileToString(new File(pathname), CharsetToolkit.UTF8);
		ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
		InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
		Document svgDocument = MySvgDocumentFactoryKt.createSvgDocument(null, reader);
		//it shows what is in png document - unZOOMED values, not limited by px limit
		ImageLoader.Dimension2DDouble outSize = new ImageLoader.Dimension2DDouble(0.0D, 0.0D);
		BufferedImage image = MySvgTranscoder.createImage((float) 1.1, svgDocument, outSize);
	}
}
