package org.plantuml.idea.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eugene Steinberg
 */
public class PlantUml {
    public static final String TESTDOT = "@startuml\ntestdot\n@enduml";
    public static final String UMLSTART = "@startuml";
    public static final String UMLEND = "@enduml";

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
        } catch (Exception e) {
            error = e.getMessage();
        }
        return new PlantUmlResult(os.toByteArray(), desc, error);
    }

    private static final String separator = System.getProperty("line.separator");

    /**
     * Extracts plantUML diagram sourcePattern code from the given string starting from given offset
     * <ul>
     * <li>Relies on having @startuml and @enduml tags (puml tags) wrapping plantuml sourcePattern code</li>
     * <li>If offset happens to be inside puml tags, returns corresponding sourcePattern code.</li>
     * <li>If offset happens to be outside puml tags, returns empty string </li>
     * </ul>
     *
     * @param text   sourcePattern code containing multiple plantuml sources
     * @param offset offset in the text from which plantuml sourcePattern is extracted
     * @return extracted plantUml code, including @startuml and @enduml tags or empty string if
     *         no valid sourcePattern code was found
     */
    private static Pattern sourcePattern = Pattern.compile("(?:(@startuml(?s).*?@enduml)(?s).*?)+");

    public static String extractSource(String text, int offset) {
        if (!text.contains("@startuml")) return "";

        Matcher matcher = sourcePattern.matcher(text);

        while (matcher.find()) {
            String group = matcher.group();
            if (matcher.start() <= offset && offset <= matcher.end())
                return group;
        }
        return "";
    }

}
