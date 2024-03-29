package org.plantuml.idea.preview.image.svg.batik

class Dimension2DDouble(var width: Double, var height: Double) {
    fun setSize(size: Dimension2DDouble) {
        width = size.width
        height = size.height
    }

    fun setSize(width: Double, height: Double) {
        this.width = width
        this.height = height
    }
}
