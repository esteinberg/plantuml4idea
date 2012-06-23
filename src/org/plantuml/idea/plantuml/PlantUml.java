package org.plantuml.idea.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Eugene Steinberg
 */
public class PlantUml {

    public enum ImageFormat {
        PNG {
            @Override
            FileFormat getFormat() {
                return FileFormat.PNG;
            }
        },
        SVG {
            @Override
            FileFormat getFormat() {
                return FileFormat.SVG;
            }
        },
        EPS {
            @Override
            FileFormat getFormat() {
                return FileFormat.EPS;
            }
        };
        abstract FileFormat getFormat();
    }

    public static final String STARTUML = "@startuml";

    public static PlantUmlResult render (String source) {
        return render (source, ImageFormat.PNG);
    }
    public static PlantUmlResult render(String source, ImageFormat format) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = null;
        String desc = null;
        String error = null;
        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(source);
            // Write the image to "os"

            desc = reader.generateImage(os, new FileFormatOption(format.getFormat()));
            // close all the flow.
            os.flush();

        } catch (IOException e) {
            error = e.getMessage();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                // do nothing
            }
        }
        return new PlantUmlResult(os.toByteArray(), desc, error);
    }

    /**
     * Checks wherever content contains PlantUML source code
     *
     * @param content input content
     * @return true if content is a PlantUML source
     */
    public static boolean containsPlantUmlSource(String content) {
        return content.contains(STARTUML);
    }
}
