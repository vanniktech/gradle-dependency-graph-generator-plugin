package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.dot.Shape.BOX
import com.vanniktech.dependency.graph.generator.dot.Shape.POLYGON
import com.vanniktech.dependency.graph.generator.dot.Shape.ELLIPSE
import com.vanniktech.dependency.graph.generator.dot.Shape.OVAL
import com.vanniktech.dependency.graph.generator.dot.Shape.CIRCLE
import com.vanniktech.dependency.graph.generator.dot.Shape.POINT
import com.vanniktech.dependency.graph.generator.dot.Shape.EGG
import com.vanniktech.dependency.graph.generator.dot.Shape.TRIANGLE
import com.vanniktech.dependency.graph.generator.dot.Shape.PLAINTEXT
import com.vanniktech.dependency.graph.generator.dot.Shape.PLAIN
import com.vanniktech.dependency.graph.generator.dot.Shape.DIAMOND
import com.vanniktech.dependency.graph.generator.dot.Shape.TRAPEZIUM
import com.vanniktech.dependency.graph.generator.dot.Shape.PARALLELOGRAM
import com.vanniktech.dependency.graph.generator.dot.Shape.HOUSE
import com.vanniktech.dependency.graph.generator.dot.Shape.PENTAGON
import com.vanniktech.dependency.graph.generator.dot.Shape.HEXAGON
import com.vanniktech.dependency.graph.generator.dot.Shape.SEPTAGON
import com.vanniktech.dependency.graph.generator.dot.Shape.OCTAGON
import com.vanniktech.dependency.graph.generator.dot.Shape.DOUBLECIRCLE
import com.vanniktech.dependency.graph.generator.dot.Shape.DOUBLEOCTAGON
import com.vanniktech.dependency.graph.generator.dot.Shape.TRIPLEOCTAGON
import com.vanniktech.dependency.graph.generator.dot.Shape.INVTRIANGLE
import com.vanniktech.dependency.graph.generator.dot.Shape.INVTRAPEZIUM
import com.vanniktech.dependency.graph.generator.dot.Shape.INVHOUSE
import com.vanniktech.dependency.graph.generator.dot.Shape.MDIAMOND
import com.vanniktech.dependency.graph.generator.dot.Shape.MSQUARE
import com.vanniktech.dependency.graph.generator.dot.Shape.MCIRCLE
import com.vanniktech.dependency.graph.generator.dot.Shape.RECT
import com.vanniktech.dependency.graph.generator.dot.Shape.RECTANGLE
import com.vanniktech.dependency.graph.generator.dot.Shape.SQUARE
import com.vanniktech.dependency.graph.generator.dot.Shape.STAR
import com.vanniktech.dependency.graph.generator.dot.Shape.NONE
import com.vanniktech.dependency.graph.generator.dot.Shape.UNDERLINE
import com.vanniktech.dependency.graph.generator.dot.Shape.CYLINDER
import com.vanniktech.dependency.graph.generator.dot.Shape.NOTE
import com.vanniktech.dependency.graph.generator.dot.Shape.TAB
import com.vanniktech.dependency.graph.generator.dot.Shape.FOLDER
import com.vanniktech.dependency.graph.generator.dot.Shape.BOX3D
import com.vanniktech.dependency.graph.generator.dot.Shape.COMPONENT
import com.vanniktech.dependency.graph.generator.dot.Shape.PROMOTER
import com.vanniktech.dependency.graph.generator.dot.Shape.CDS
import com.vanniktech.dependency.graph.generator.dot.Shape.TERMINATOR
import com.vanniktech.dependency.graph.generator.dot.Shape.UTR
import com.vanniktech.dependency.graph.generator.dot.Shape.PRIMERSITE
import com.vanniktech.dependency.graph.generator.dot.Shape.RESTRICTIONSITE
import com.vanniktech.dependency.graph.generator.dot.Shape.FIVEPOVERHANG
import com.vanniktech.dependency.graph.generator.dot.Shape.THREEPOVERHANG
import com.vanniktech.dependency.graph.generator.dot.Shape.NOVERHANG
import com.vanniktech.dependency.graph.generator.dot.Shape.ASSEMBLY
import com.vanniktech.dependency.graph.generator.dot.Shape.SIGNATURE
import com.vanniktech.dependency.graph.generator.dot.Shape.INSULATOR
import com.vanniktech.dependency.graph.generator.dot.Shape.RIBOSITE
import com.vanniktech.dependency.graph.generator.dot.Shape.RNASTAB
import com.vanniktech.dependency.graph.generator.dot.Shape.PROTEASESITE
import com.vanniktech.dependency.graph.generator.dot.Shape.PROTEINSTAB
import com.vanniktech.dependency.graph.generator.dot.Shape.RPROMOTER
import com.vanniktech.dependency.graph.generator.dot.Shape.RARROW
import com.vanniktech.dependency.graph.generator.dot.Shape.LARROW
import com.vanniktech.dependency.graph.generator.dot.Shape.LPROMOTER
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class ShapeTest {
  @Test fun values() {
    assertThat(Shape.values()).containsExactly(BOX, POLYGON, ELLIPSE, OVAL, CIRCLE, POINT, EGG, TRIANGLE, PLAINTEXT, PLAIN, DIAMOND, TRAPEZIUM, PARALLELOGRAM, HOUSE, PENTAGON, HEXAGON, SEPTAGON, OCTAGON, DOUBLECIRCLE, DOUBLEOCTAGON, TRIPLEOCTAGON, INVTRIANGLE, INVTRAPEZIUM, INVHOUSE, MDIAMOND, MSQUARE, MCIRCLE, RECT, RECTANGLE, SQUARE, STAR, NONE, UNDERLINE, CYLINDER, NOTE, TAB, FOLDER, BOX3D, COMPONENT, PROMOTER, CDS, TERMINATOR, UTR, PRIMERSITE, RESTRICTIONSITE, FIVEPOVERHANG, THREEPOVERHANG, NOVERHANG, ASSEMBLY, SIGNATURE, INSULATOR, RIBOSITE, RNASTAB, PROTEASESITE, PROTEINSTAB, RPROMOTER, RARROW, LARROW, LPROMOTER)
  }

  @Test @Suppress("Detekt.LongMethod") fun string() {
    assertThat(BOX).hasToString("box")
    assertThat(POLYGON).hasToString("polygon")
    assertThat(ELLIPSE).hasToString("ellipse")
    assertThat(OVAL).hasToString("oval")
    assertThat(CIRCLE).hasToString("circle")
    assertThat(POINT).hasToString("point")
    assertThat(EGG).hasToString("egg")
    assertThat(TRIANGLE).hasToString("triangle")
    assertThat(PLAINTEXT).hasToString("plaintext")
    assertThat(PLAIN).hasToString("plain")
    assertThat(DIAMOND).hasToString("diamond")
    assertThat(TRAPEZIUM).hasToString("trapezium")
    assertThat(PARALLELOGRAM).hasToString("parallelogram")
    assertThat(HOUSE).hasToString("house")
    assertThat(PENTAGON).hasToString("pentagon")
    assertThat(HEXAGON).hasToString("hexagon")
    assertThat(SEPTAGON).hasToString("septagon")
    assertThat(OCTAGON).hasToString("octagon")
    assertThat(DOUBLECIRCLE).hasToString("doublecircle")
    assertThat(DOUBLEOCTAGON).hasToString("doubleoctagon")
    assertThat(TRIPLEOCTAGON).hasToString("tripleoctagon")
    assertThat(INVTRIANGLE).hasToString("invtriangle")
    assertThat(INVTRAPEZIUM).hasToString("invtrapezium")
    assertThat(INVHOUSE).hasToString("invhouse")
    assertThat(MDIAMOND).hasToString("Mdiamond")
    assertThat(MSQUARE).hasToString("Msquare")
    assertThat(MCIRCLE).hasToString("Mcircle")
    assertThat(RECT).hasToString("rect")
    assertThat(RECTANGLE).hasToString("rectangle")
    assertThat(SQUARE).hasToString("square")
    assertThat(STAR).hasToString("star")
    assertThat(NONE).hasToString("none")
    assertThat(UNDERLINE).hasToString("underline")
    assertThat(CYLINDER).hasToString("cylinder")
    assertThat(NOTE).hasToString("note")
    assertThat(TAB).hasToString("tab")
    assertThat(FOLDER).hasToString("folder")
    assertThat(BOX3D).hasToString("box3d")
    assertThat(COMPONENT).hasToString("component")
    assertThat(PROMOTER).hasToString("promoter")
    assertThat(CDS).hasToString("cds")
    assertThat(TERMINATOR).hasToString("terminator")
    assertThat(UTR).hasToString("utr")
    assertThat(PRIMERSITE).hasToString("primersite")
    assertThat(RESTRICTIONSITE).hasToString("restrictionsite")
    assertThat(FIVEPOVERHANG).hasToString("fivepoverhang")
    assertThat(THREEPOVERHANG).hasToString("threepoverhang")
    assertThat(NOVERHANG).hasToString("noverhang")
    assertThat(ASSEMBLY).hasToString("assembly")
    assertThat(SIGNATURE).hasToString("signature")
    assertThat(INSULATOR).hasToString("insulator")
    assertThat(RIBOSITE).hasToString("ribosite")
    assertThat(RNASTAB).hasToString("rnastab")
    assertThat(PROTEASESITE).hasToString("proteasesite")
    assertThat(PROTEINSTAB).hasToString("proteinstab")
    assertThat(RPROMOTER).hasToString("rpromoter")
    assertThat(RARROW).hasToString("rarrow")
    assertThat(LARROW).hasToString("larrow")
    assertThat(LPROMOTER).hasToString("lpromoter")
  }
}
