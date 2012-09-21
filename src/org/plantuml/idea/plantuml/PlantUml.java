package org.plantuml.idea.plantuml;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eugene Steinberg
 */
public class PlantUml {
    public static final String TESTDOT = "@startuml\ntestdot\n@enduml";
    public static final String UMLSTART = "@start";

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

    /**
     * Renders given source code into diagram
     *
     * @param source plantUml source code
     * @param format desired image format
     * @return rendering result
     */
    public static PlantUmlResult render(String source, ImageFormat format) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String desc = null;
        String error = null;
        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(source);
            // Write the image to "os"
            desc = reader.generateImage(os, new FileFormatOption(format.getFormat()));
        } catch (Throwable e) {
            error = e.getMessage();
        }
        return new PlantUmlResult(os.toByteArray(), desc, error);
    }

    /**
     * Renders source code and saves diagram images to files according to provided naming scheme
     * and image format.
     *
     * @param source         source code to be rendered
     * @param format         image format
     * @param fileName       fileName to use with first file
     * @param fileNameFormat file naming scheme for further files
     * @throws IOException in case of rendering or saving fails
     */
    public static void renderAndSave(String source, ImageFormat format, String fileName, String fileNameFormat)
            throws IOException {
        SourceStringReader reader = new SourceStringReader(source);
        List<BlockUml> blocks = reader.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            String fName = i == 0 ? fileName : String.format(fileNameFormat, i);
            FileOutputStream outputStream = new FileOutputStream(fName);
            reader.generateImage(outputStream, i, new FileFormatOption(format.getFormat()));
            outputStream.close();
        }
    }

    private static Pattern sourcePattern =
            Pattern.compile("(?:(@start(?:uml|dot|jcckit|ditaa|salt)(?s).*?(?:@end(?:uml|dot|jcckit|ditaa|salt)|$))(?s).*?)+");

    /**
     * Extracts plantUML diagram source code from the given string starting from given offset
     * <ul>
     * <li>Relies on having @startuml and @enduml tags (puml tags) wrapping plantuml sourcePattern code</li>
     * <li>If offset happens to be inside puml tags, returns corresponding sourcePattern code.</li>
     * <li>If offset happens to be outside puml tags, returns empty string </li>
     * </ul>
     *
     * @param text   source code containing multiple plantuml sources
     * @param offset offset in the text from which plantuml sourcePattern is extracted
     * @return extracted plantUml code, including @startuml and @enduml tags or empty string if
     *         no valid sourcePattern code was found
     */
    public static String extractSource(String text, int offset) {
        if (!text.contains(UMLSTART)) return "";

        Matcher matcher = sourcePattern.matcher(text);

        while (matcher.find()) {
            String group = matcher.group();
            if (matcher.start() <= offset && offset <= matcher.end())
                return group;
        }
        return "";
    }

}
