package org.plantuml.idea.rendering;

import org.junit.jupiter.api.Test;
import org.plantuml.idea.settings.PlantUmlSettings;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilityCheckTest {

	@Test
	void checkTransformer() {
		assertTrue(CompatibilityCheck.checkTransformer(PlantUmlSettings.getInstance()));
	}
}