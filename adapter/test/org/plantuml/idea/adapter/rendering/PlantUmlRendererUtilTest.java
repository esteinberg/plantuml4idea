package org.plantuml.idea.adapter.rendering;

import org.junit.Assert;
import org.junit.Test;
import org.plantuml.idea.adapter.FacadeImpl;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;

import java.io.File;

import static org.plantuml.idea.adapter.rendering.PlantUmlRendererUtil.NEW_PAGE_PATTERN;

public class PlantUmlRendererUtilTest {
    @Test
    public void render() throws Exception {
        RenderResult render = new FacadeImpl().render(new RenderRequest("sourceFilePath", new File(""), "@startuml\n" +
                "xxx->yyy\n" +
                "@enduml", PlantUml.ImageFormat.PNG, 0, 100, null, false, RenderCommand.Reason.REFRESH), null);
        Assert.assertNotNull(render);
        Assert.assertNotNull(render.getFirstDiagramBytes());
        Assert.assertNotNull(render.getImageItems().get(0));
    }

    @Test
    public void renderBrokenImage() throws Exception {
        RenderResult render = new FacadeImpl().render(new RenderRequest("sourceFilePath", new File(""), "@startuml\n" +
                "xxx\n" +
                "@enduml", PlantUml.ImageFormat.PNG, 0, 0, null, false, RenderCommand.Reason.REFRESH), null);
        Assert.assertNotNull(render);
        Assert.assertNotNull(render.getImageItems().get(0));
        Assert.assertTrue(render.hasError());
    }

    @Test
    public void splitNewPage() throws Exception {
        String[] strings = NEW_PAGE_PATTERN.split("@startuml\n" +
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