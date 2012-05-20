package org.plantuml.idea.plantuml;

import net.sourceforge.plantuml.SourceStringReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Eugene Steinberg
 */

public class PlantUml {

    public static final String STARTUML = "@startuml";

    public static PlantUmlResult render(String source) {
        BufferedImage renderedImage = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = null;
        String desc = null;
        String error = null;
        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(source);
            // Write the image to "os"
            desc = reader.generateImage(os);
            // close all the flow.
            os.flush();
            os.close();
            is = new ByteArrayInputStream(os.toByteArray());
            renderedImage = ImageIO.read(new ByteArrayInputStream(os.toByteArray()));
            // close the input stream
            is.close();
        } catch (IOException e) {
            error = e.getMessage();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                // do nothing
            }
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                // do nothing
            }
        }
        return new PlantUmlResult(renderedImage, desc, error);
    }

    /**
     * Checks wherever content contains PlantUML source code
     * @param content input content
     * @return true if content is a PlantUML source
     */
    public static boolean containsPlantUmlSource(String content) {
        return content.contains(STARTUML);
    }
}
