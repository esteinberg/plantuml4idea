package org.plantuml.idea.plantuml;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class PlantUmlRendererTest {
    @Test
    public void render() throws Exception {
        PlantUmlResult render = PlantUmlRenderer.render(new RenderRequest(new File(""), "@startuml\n" +
                "xxx->yyy\n" +
                "@enduml", PlantUml.ImageFormat.PNG, 0, 100));
        Assert.assertFalse(render.isError());
        Assert.assertNull(render.getError());
    }

    @Test
    public void renderBrokenImage() throws Exception {
        PlantUmlResult render = PlantUmlRenderer.render(new RenderRequest(new File(""), "@startuml\n" +
                "xxx\n" +
                "@enduml", PlantUml.ImageFormat.PNG, 0, 0));
        Assert.assertTrue(render.isError());
        Assert.assertNull(render.getError());
    }

}