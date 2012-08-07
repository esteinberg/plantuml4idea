package org.plantuml.idea.plantuml;

import junit.framework.TestCase;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlTest extends TestCase {
    String source1 = "@startuml some code @enduml";
    String source2 = "@startuml\nsecond code\n@enduml";
    String source3 = "@startuml\nthird code\n@enduml";
    String compoundSource = source1 + "\n" + "some intermediate stuff " + "\n" + source2 + "some other intermediate"
            + source3;

    public void testExtractSingleSource() throws Exception {
        assertEquals(source1, PlantUml.extractSource(source1, 0));
        assertEquals(source1, PlantUml.extractSource(source1, source1.length() / 2));
        assertEquals(source1,PlantUml.extractSource(source1, source1.length()));
    }

    public void testExtractCompoundSource() {
        assertEquals(source1, PlantUml.extractSource(compoundSource, source1.length() / 2));
        assertEquals(source2, PlantUml.extractSource(compoundSource, compoundSource.length() - source3.length() - 30));
        assertEquals("",PlantUml.extractSource(compoundSource, source1.length() + 5));

    }

}
