package org.plantuml.idea.adapter;

import org.junit.Assert;
import org.junit.Test;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.plantuml.SourceExtractor;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.settings.PlantUmlSettings;

public class FacadeImplTest {

	@Test
	public void skinparams() {
		new FacadeImpl().getSkinParams();
	}

	@Test
	public void renderTest() {
		RenderRequest renderRequest = new RenderRequest("", SourceExtractor.TESTDOT, ImageFormat.SVG,
				0, new Zoom(1, PlantUmlSettings.getInstance()), null, false, RenderCommand.Reason.REFRESH, null);

		RenderResult result = PlantUmlFacade.get().render(renderRequest, null);

		Assert.assertEquals(1, result.getRendered());
		Assert.assertFalse(result.hasError());
	}
}