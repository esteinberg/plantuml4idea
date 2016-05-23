package org.plantuml.idea.plantuml;

import org.junit.Assert;
import org.junit.Test;
import org.plantuml.idea.rendering.PlantUmlRenderer;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;

import java.io.File;

public class PlantUmlRendererTest {
    @Test
    public void render() throws Exception {
        RenderResult render = PlantUmlRenderer.render(new RenderRequest(new File(""), "@startuml\n" +
                "xxx->yyy\n" +
                "@enduml", PlantUml.ImageFormat.PNG, 0, 100, null, false), null);
        Assert.assertNotNull(render);
        Assert.assertNotNull(render.getFirstDiagramBytes());
        Assert.assertNotNull(render.getImageItems().get(0));
    }

    @Test
    public void renderBrokenImage() throws Exception {
        RenderResult render = PlantUmlRenderer.render(new RenderRequest(new File(""), "@startuml\n" +
                "xxx\n" +
                "@enduml", PlantUml.ImageFormat.PNG, 0, 0, null, false), null);
        Assert.assertNotNull(render);
        Assert.assertNotNull(render.getImageItems().get(0));
    }

    @Test
    public void splitNewPage() throws Exception {
        String[] strings = PlantUmlRenderer.splitNewPage("@startuml\n" +
                "xx1\n" +
                " newpage \n" +
                "xx3\n" +
                "    @newpage    \n" +
                "xx4\n" +
                "@newpage\n" +
                "xx5\n" +
                "@enduml");

        Assert.assertArrayEquals(
                new String[]{
                        "@startuml\n" +
                                "xx1"
                        ,
                        "\nxx3"
                        ,
                        "\nxx4"
                        ,
                        "\nxx5\n@enduml"},
                strings);
    }

}