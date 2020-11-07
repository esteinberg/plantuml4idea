package org.plantuml.idea.rendering;

import org.junit.Test;
import org.plantuml.idea.plantuml.PlantUml;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PlantUmlPartialRendererTest {
    @Test
    public void renderError() throws Exception {
        RenderResult renderResult = new PlantUmlPartialRenderer().renderError(new RenderRequest("sourceFilePath", new File(""), "", PlantUml.ImageFormat.PNG, 0, 0, 0, false, RenderCommand.Reason.MANUAL_UPDATE), new PartialRenderingException());
        assertTrue(renderResult.hasError());
        assertNotNull(renderResult.getFirstDiagramBytes());
    }

}