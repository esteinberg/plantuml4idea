package org.plantuml.idea.preview.image.links;

import org.junit.Test;
import org.plantuml.idea.preview.image.links.LinkNavigator.Coordinates;

import static org.junit.Assert.assertEquals;

public class LinkNavigatorTest {

	@Test
	public void getCoordinates() {
		assertEquals(new Coordinates("https://github.com", null, null), LinkNavigator.getCoordinates("https://github.com"));
		assertEquals(new Coordinates("src/Main.java", null, 9), LinkNavigator.getCoordinates("src/Main.java:10 qq"));
		assertEquals(new Coordinates("C:/src/Main.java", null, 9), LinkNavigator.getCoordinates("C:/src/Main.java:10 qq"));
		assertEquals(new Coordinates("src/Main.java", "foo", null), LinkNavigator.getCoordinates("src/Main.java#foo qq"));

		assertEquals(new Coordinates("src/Main.java:qq", null, null), LinkNavigator.getCoordinates("src/Main.java:qq qq"));
		assertEquals(new Coordinates("src/Main.java:", null, 0), LinkNavigator.getCoordinates("src/Main.java::1"));
		assertEquals(new Coordinates("src/Main.java::", null, null), LinkNavigator.getCoordinates("src/Main.java::"));
		assertEquals(new Coordinates(":", null, null), LinkNavigator.getCoordinates(":"));
		assertEquals(new Coordinates("", null, null), LinkNavigator.getCoordinates(""));
	}
}