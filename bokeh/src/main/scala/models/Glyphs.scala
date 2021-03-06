package io.continuum.bokeh

@model abstract class Glyph extends PlotObject with Vectorization {
    object visible extends Field[Boolean](true)
}

@model class AnnularWedge extends Glyph with FillProps with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object inner_radius extends Spatial[Double] with NonNegative
    object outer_radius extends Spatial[Double] with NonNegative
    object start_angle extends Angular[Double]
    object end_angle extends Angular[Double]
    object direction extends Field[Direction]
}

@model class Annulus extends Glyph with FillProps with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object inner_radius extends Spatial[Double] with NonNegative
    object outer_radius extends Spatial[Double] with NonNegative
}

@model class Arc extends Glyph with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object radius extends Spatial[Double] with NonNegative
    object start_angle extends Angular[Double]
    object end_angle extends Angular[Double]
    object direction extends Field[Direction]
}

@model class Bezier extends Glyph with LineProps {
    object x0 extends Spatial[Double]
    object y0 extends Spatial[Double]
    object x1 extends Spatial[Double]
    object y1 extends Spatial[Double]
    object cx0 extends Spatial[Double]
    object cy0 extends Spatial[Double]
    object cx1 extends Spatial[Double]
    object cy1 extends Spatial[Double]
}

@model class ImageRGBA extends Glyph {
    object image extends Vectorized[Array[Double]]
    object rows extends Vectorized[Int]
    object cols extends Vectorized[Int]
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object dw extends Spatial[Double] with NonNegative
    object dh extends Spatial[Double] with NonNegative
    object dilate extends Field[Boolean]

    def image[T[_]: MatrixLike](value: T[Double]): SelfType = {
        val (image, rows, cols) = implicitly[MatrixLike[T]].data(value)

        this.image := image
        this.rows  := rows
        this.cols  := cols

        this
    }
}

@model class Image extends ImageRGBA {
    object color_mapper extends Field[ColorMapper]
}

@model class ImageURL extends Glyph {
    object url extends Vectorized[String]
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object w extends Spatial[Double] with NonNegative
    object h extends Spatial[Double] with NonNegative
    object angle extends Angular[Double]
    object dilate extends Field[Boolean]
    object anchor extends Field[Anchor]
}

@model class Line extends Glyph with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
}

@model class MultiLine extends Glyph with LineProps {
    object xs extends Spatial[List[Double]]
    object ys extends Spatial[List[Double]]
}

@model class Oval extends Glyph with FillProps with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object width extends Spatial[Double] with NonNegative
    object height extends Spatial[Double] with NonNegative
    object angle extends Angular[Double]
}

@model class Patch extends Glyph with FillProps with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
}

@model class Patches extends Glyph with LineProps with FillProps {
    object xs extends Spatial[List[Double]]
    object ys extends Spatial[List[Double]]
}

@model class Quad extends Glyph with FillProps with LineProps {
    object left extends Spatial[Double]
    object right extends Spatial[Double]
    object bottom extends Spatial[Double]
    object top extends Spatial[Double]
}

@model class Quadratic extends Glyph with LineProps {
    object x0 extends Spatial[Double]
    object y0 extends Spatial[Double]
    object x1 extends Spatial[Double]
    object y1 extends Spatial[Double]
    object cx extends Spatial[Double]
    object cy extends Spatial[Double]
}

@model class Ray extends Glyph with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object angle extends Angular[Double]
    object length extends Spatial[Double](SpatialUnits.Screen) with NonNegative
}

@model class Rect extends Glyph with FillProps with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object width extends Spatial[Double] with NonNegative
    object height extends Spatial[Double] with NonNegative
    object angle extends Angular[Double]
    object dilate extends Field[Boolean]
}

@model class Segment extends Glyph with LineProps {
    object x0 extends Spatial[Double]
    object y0 extends Spatial[Double]
    object x1 extends Spatial[Double]
    object y1 extends Spatial[Double]
}

@model class Text extends Glyph with TextProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object text extends Vectorized[String]
    object angle extends Angular[Double](0)
    object x_offset extends Spatial[Double](0, SpatialUnits.Screen)
    object y_offset extends Spatial[Double](0, SpatialUnits.Screen)
}

@model class Wedge extends Glyph with FillProps with LineProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object radius extends Spatial[Double] with NonNegative
    object start_angle extends Angular[Double]
    object end_angle extends Angular[Double]
    object direction extends Field[Direction]
}

@model class Gear extends Glyph with LineProps with FillProps {
    object x extends Spatial[Double]
    object y extends Spatial[Double]
    object angle extends Angular[Double]
    object module extends Spatial[Double] with NonNegative
    object teeth extends Vectorized[Int] // TODO: with NonNegative
    object pressure_angle extends Angular[Double](20, AngularUnits.Deg)
    object shaft_size extends Spatial[Double](0.3) with NonNegative
    object internal extends Vectorized[Boolean](false)
}
