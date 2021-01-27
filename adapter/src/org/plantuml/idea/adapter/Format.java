package org.plantuml.idea.adapter;

import net.sourceforge.plantuml.FileFormat;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.rendering.RenderRequest;

import java.util.Arrays;

public enum Format {
    PNG(ImageFormat.PNG) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.PNG;
        }
    },
    SVG(ImageFormat.SVG) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.SVG;
        }
    },
    UTXT(ImageFormat.UTXT) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.UTXT;
        }
    },
    ATXT(ImageFormat.ATXT) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.ATXT;
        }
    },
    EPS(ImageFormat.EPS) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.EPS;
        }
    },
    TEX(ImageFormat.TEX) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.LATEX;
        }
    },
    TIKZ(ImageFormat.TIKZ) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.LATEX_NO_PREAMBLE;
        }
    };

    ImageFormat format;

    Format(ImageFormat format) {
        this.format = format;
    }

    public abstract FileFormat getFormat();


    public static FileFormat from(RenderRequest renderRequest) {
        return from(renderRequest.getFormat());
    }

    public static FileFormat from(ImageFormat format) {
        return Arrays.stream(values()).filter(exImageFormat -> exImageFormat.format == format).findFirst().orElseThrow(RuntimeException::new).getFormat();
    }

}
