package be.robinj.rot13;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Well-known properties and classic samples of the ROT13 cipher.
 *
 * These lock in the folklore that people actually rely on: ROT13 is its own
 * inverse, has no fixed points, is case-independent, and turns a handful of
 * famous words into other real words.
 */
public class Rot13EdgeCasesTest {

	@Test
	public void noLetterIsAFixedPoint() {
		// Because the shift (13) is exactly half of 26, ROT13 is a derangement
		// of the alphabet: no letter can ever encode to itself.
		for (char c = 'a'; c <= 'z'; c++) {
			assertNotEquals("lowercase '" + c + "' must not map to itself",
					String.valueOf(c), MainActivity.rotate(String.valueOf(c)));
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			assertNotEquals("uppercase '" + c + "' must not map to itself",
					String.valueOf(c), MainActivity.rotate(String.valueOf(c)));
		}
	}

	@Test
	public void encodeAndDecodeAreTheSameOperation() {
		// The defining property of ROT13: applying it a second time decodes,
		// so encode and decode are literally the same function.
		String plain = "Attack at dawn!";
		String cipher = MainActivity.rotate(plain);
		assertEquals(plain, MainActivity.rotate(cipher));
	}

	@Test
	public void fourApplicationsReturnToStart() {
		// ROT13 twice is the identity (ROT26), so any even number of
		// applications is a no-op.
		String plain = "Grumpy Wizards make toxic brew for the Evil Queen: 42";
		String once = MainActivity.rotate(plain);
		String twice = MainActivity.rotate(once);
		String thrice = MainActivity.rotate(twice);
		String fourTimes = MainActivity.rotate(thrice);
		assertEquals(plain, twice);
		assertEquals(plain, fourTimes);
	}

	@Test
	public void selfReferentialAppName() {
		// The classic party trick — and this project's own name: "Rot13"
		// enciphers to "Ebg13" (digits ride through untouched).
		assertEquals("Ebg13", MainActivity.rotate("Rot13"));
		assertEquals("EBG13", MainActivity.rotate("ROT13"));
	}

	@Test
	public void famousWordPairsThatBecomeOtherWords() {
		// A small gallery of ROT13 pairs where both halves are real words —
		// the reason the cipher is a running in-joke.
		assertEquals("irk <-> vex", "vex", MainActivity.rotate("irk"));
		assertEquals("vex <-> irk", "irk", MainActivity.rotate("vex"));
		assertEquals("cat <-> png", "png", MainActivity.rotate("cat"));
		assertEquals("why <-> jul", "jul", MainActivity.rotate("why"));
		assertEquals("gnat <-> tang", "tang", MainActivity.rotate("gnat"));
		// ...and it stays symmetric.
		assertEquals("png <-> cat", "cat", MainActivity.rotate("png"));
	}

	@Test
	public void caseIsHandledIndependently() {
		// Upper- and lower-case letters rotate within their own halves, so a
		// letter's case is always preserved.
		assertEquals("Uryyb", MainActivity.rotate("Hello"));
		assertEquals("uRYYB", MainActivity.rotate("hELLO"));
	}
}
