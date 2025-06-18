package org.plantuml.idea.rendering;

import org.junit.Assert;
import org.junit.Test;
import org.plantuml.idea.settings.PlantUmlSettings;


public class CompatibilityCheckTest {

	@Test
	public void checkTransformer() {
		Assert.assertTrue(CompatibilityCheck.checkTransformer(PlantUmlSettings.getInstance()));
	}
}