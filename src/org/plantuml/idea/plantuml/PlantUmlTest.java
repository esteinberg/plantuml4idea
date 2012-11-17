package org.plantuml.idea.plantuml;

import junit.framework.TestCase;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlTest extends TestCase {
    String validSource1 = "@startuml some code @enduml";
    String validSource2 = "@startuml\nsecond code\n@enduml";
    String validSource3 = "@startuml\nthird code\n@enduml";
    String intermediateText1 = "\nsome intermediate\n paragraph text\n";
    String intermediateText2 = "another \n intermediate \n text";
    String sourceWithoutEndUml = "@startuml\ncode without enduml";

    public void testExtractSingleSource() throws Exception {
        assertEquals(validSource1, PlantUml.extractSource(validSource1, 0));
        assertEquals(validSource1, PlantUml.extractSource(validSource1, validSource1.length() / 2));
        assertEquals(validSource1, PlantUml.extractSource(validSource1, validSource1.length()));
        assertEquals(sourceWithoutEndUml, PlantUml.extractSource(sourceWithoutEndUml, sourceWithoutEndUml.length() / 2));
        assertEquals(sourceWithoutEndUml, PlantUml.extractSource(sourceWithoutEndUml, sourceWithoutEndUml.length()));
    }

    public void testExtractCompoundSource() {
        String compoundSource1 = validSource1 + intermediateText1 + validSource2;
        assertEquals(validSource1, PlantUml.extractSource(compoundSource1, validSource1.length() / 2));
        assertEquals(validSource2, PlantUml.extractSource(compoundSource1, compoundSource1.length() - validSource2.length() / 2));
        assertEquals("", PlantUml.extractSource(compoundSource1, validSource1.length() + intermediateText1.length() / 2));

        String compoundSource2 = validSource1 + intermediateText1 + sourceWithoutEndUml;
        assertEquals(sourceWithoutEndUml,
                PlantUml.extractSource(compoundSource2, compoundSource2.length() - sourceWithoutEndUml.length() / 2));
        String compoundSource3 = sourceWithoutEndUml + intermediateText1 + validSource1;
        assertEquals(compoundSource3,
                PlantUml.extractSource(compoundSource3 + intermediateText2, compoundSource3.length() - validSource1.length() / 2));
    }

    public void testExtractDotSource() {

        String dotSource = "@startdot\n dotcode\n @enddot";
        assertEquals(dotSource, PlantUml.extractSource(intermediateText1 + dotSource + intermediateText2,
                intermediateText1.length() + dotSource.length() / 2));
    }

    public void testExtractSourceWithComments() {
        String source ="/**\n" +
                "* @startuml\n" +
                "* Bob -> Alice: hello\n" +
                "*/";
        String expected = "@startuml\n" +
                "Bob -> Alice: hello\n" +
                "*/";
        assertEquals(expected,PlantUml.extractSource(source, source.length() / 2));

        source ="/*\n" +
                " * @startuml\n" +
                " * (*) -> Init\n" +
                " * @enduml\n" +
                " */";
        expected = "@startuml\n" +
                "(*) -> Init\n" +
                "@enduml";
        assertEquals(expected,PlantUml.extractSource(source, source.length() / 2));
    }
}
