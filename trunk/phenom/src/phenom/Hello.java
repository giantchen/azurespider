package phenom;

import org.junit.*;
import static org.junit.Assert.assertEquals;

public class Hello {
	@BeforeClass
	public static void setup() {
		System.out.println("setup.");
	}

	@AfterClass
	public static void tearDown() {
		System.out.println("tear down.");
	}

	@Test
	public void hello() {
		System.out.println("Hello world.");
	}

	@Test
	public void world() {
		System.out.println("Hello world2.");
		assertEquals(true, true);
	}
}
