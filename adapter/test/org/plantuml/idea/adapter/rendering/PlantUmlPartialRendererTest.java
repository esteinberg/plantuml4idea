package org.plantuml.idea.adapter.rendering;

import org.junit.Test;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.rendering.PartialRenderingException;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.settings.PlantUmlSettings;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PlantUmlPartialRendererTest {
    @Test
    public void renderError() throws Exception {
        RenderResult renderResult = new PlantUmlPartialRenderer().renderError(new RenderRequest("sourceFilePath", "", ImageFormat.PNG, 0, new Zoom(0, new PlantUmlSettings()), 0, false, RenderCommand.Reason.MANUAL_UPDATE, null), new PartialRenderingException());
        assertTrue(renderResult.hasError());
        assertNotNull(renderResult.getFirstDiagramBytes());
    }

}