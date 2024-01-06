package org.plantuml.idea.preview.image.links;

import org.junit.jupiter.api.Test;
import org.plantuml.idea.preview.image.links.LinkNavigator.Coordinates;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkNavigatorTest {

    @Test
    void getCoordinates() {
        assertEquals(new Coordinates("https://github.com", null, null), LinkNavigator.getCoordinates("https://github.com"));
        assertEquals(new Coordinates("src/Main.java", null, 9), LinkNavigator.getCoordinates("src/Main.java:10 qq"));
        assertEquals(new Coordinates("src/Main.java", "foo", null), LinkNavigator.getCoordinates("src/Main.java#foo qq"));

        assertEquals(new Coordinates("src/Main.java:qq", null, null), LinkNavigator.getCoordinates("src/Main.java:qq qq"));
    }
}