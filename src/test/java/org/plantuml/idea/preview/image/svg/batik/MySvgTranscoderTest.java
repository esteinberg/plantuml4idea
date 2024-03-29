package org.plantuml.idea.preview.image.svg.batik;

import io.sf.carte.echosvg.transcoder.TranscoderException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MySvgTranscoderTest {
	@Test
	public void test() throws IOException, TranscoderException {
		renderSvg("src/test/resources/testData/test2.svg");
		renderSvg("src/test/resources/testData/test3.svg");

	}

	private static void renderSvg(String pathname) throws IOException, TranscoderException {
		Document svgDocument = MySvgDocumentFactoryKt.createSvgDocument(null, FileUtils.readFileToByteArray(new File(pathname)));
		//it shows what is in png document - unZOOMED values, not limited by px limit
		Dimension2DDouble outSize = new Dimension2DDouble(0.0D, 0.0D);
		BufferedImage image = MySvgTranscoder.createImage((float) 1.1, svgDocument, outSize);
	}
}
