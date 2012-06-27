package org.plantuml.idea.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    public static PlantUmlResult render(String source) {
        return render(source, ImageFormat.PNG);
    }

    public static PlantUmlResult render(String source, ImageFormat format) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String desc = null;
        String error = null;
        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(source);
            // Write the image to "os"
            desc = reader.generateImage(os, new FileFormatOption(format.getFormat()));
        } catch (IOException e) {
            error = e.getMessage();
        }
        return new PlantUmlResult(os.toByteArray(), desc, error);
    }
}
