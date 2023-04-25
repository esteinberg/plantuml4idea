package org.plantuml.idea.plantuml;

import java.util.Arrays;

public enum ImageFormat {

    PNG,
    SVG,
    UTXT,
    ATXT,
    EPS,
    TEX,
    TIKZ,
    ;

    public static ImageFormat from(String defaultExportFileFormat) {
        return Arrays.stream(ImageFormat.values()).filter(f -> f.name().equals(defaultExportFileFormat)).findFirst().orElse(PNG);
    }

}
