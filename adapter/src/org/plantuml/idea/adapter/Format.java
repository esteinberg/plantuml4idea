package org.plantuml.idea.adapter;

import net.sourceforge.plantuml.FileFormat;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.RenderRequest;

import java.util.Arrays;

public enum Format {
    PNG(PlantUml.ImageFormat.PNG) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.PNG;
        }
    },
    SVG(PlantUml.ImageFormat.SVG) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.SVG;
        }
    },
    UTXT(PlantUml.ImageFormat.UTXT) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.UTXT;
        }
    },
    ATXT(PlantUml.ImageFormat.ATXT) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.ATXT;
        }
    },
    EPS(PlantUml.ImageFormat.EPS) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.EPS;
        }
    },
    TEX(PlantUml.ImageFormat.TEX) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.LATEX;
        }
    },
    TIKZ(PlantUml.ImageFormat.TIKZ) {
        @Override
        public FileFormat getFormat() {
            return FileFormat.LATEX_NO_PREAMBLE;
        }
    };

    PlantUml.ImageFormat format;

    Format(PlantUml.ImageFormat format) {
        this.format = format;
    }

    public abstract FileFormat getFormat();


    public static FileFormat from(RenderRequest renderRequest) {
        return from(renderRequest.getFormat());
    }

    public static FileFormat from(PlantUml.ImageFormat format) {
        return Arrays.stream(values()).filter(exImageFormat -> exImageFormat.format == format).findFirst().orElseThrow(RuntimeException::new).getFormat();
    }

}
