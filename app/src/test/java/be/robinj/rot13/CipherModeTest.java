package be.robinj.rot13;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * The contract every mode has to satisfy, checked across all of them at once so
 * a mode added later cannot quietly skip it.
 */
public class CipherModeTest
{
	private static final String SAMPLE = "Attack at dawn, 0700 hours!";

	@Test
	public void decodeUndoesEncodeForEveryModeAndShift ()
	{
		for (CipherMode mode : CipherMode.values ())
		{
			for (int shift = CipherMode.MIN_SHIFT; shift <= CipherMode.MAX_SHIFT; shift++)
			{
				assertEquals (mode + " at shift " + shift,
						SAMPLE, mode.decode (mode.encode (SAMPLE, shift), shift));
			}
		}
	}

	@Test
	public void onlyCaesarUsesTheShift ()
	{
		for (CipherMode mode : CipherMode.values ())
			assertEquals (mode.toString (), mode == CipherMode.CAESAR, mode.usesShift ());
	}

	@Test
	public void shiftIsIgnoredByEveryModeThatDoesNotUseIt ()
	{
		for (CipherMode mode : CipherMode.values ())
		{
			if (mode.usesShift ())
				continue;

			assertEquals (mode.toString (),
					mode.encode (SAMPLE, 0), mode.encode (SAMPLE, 19));
		}
	}

	@Test
	public void encodeAndDecodeAgreeForEverySelfInverseMode ()
	{
		for (CipherMode mode : CipherMode.values ())
		{
			if (mode == CipherMode.CAESAR)
				continue;

			assertEquals (mode.toString (), mode.encode (SAMPLE, 0), mode.decode (SAMPLE, 0));
			assertTrue (mode.toString (), mode.isSelfInverse (0));
		}
	}

	@Test
	public void caesarIsSelfInverseOnlyAtZeroAndThirteen ()
	{
		for (int shift = CipherMode.MIN_SHIFT; shift <= CipherMode.MAX_SHIFT; shift++)
		{
			boolean expected = shift == 0 || shift == 13;
			assertEquals ("shift " + shift, expected, CipherMode.CAESAR.isSelfInverse (shift));
		}
	}

	@Test
	public void caesarDecodesInTheOppositeDirection ()
	{
		// The one asymmetric path in the app, and the easiest thing to get
		// backwards: encoding by 7 must be undone by decoding by 7, not by -7.
		assertEquals ("hij", CipherMode.CAESAR.encode ("abc", 7));
		assertEquals ("abc", CipherMode.CAESAR.decode ("hij", 7));
		assertNotEquals (CipherMode.CAESAR.encode ("abc", 7), CipherMode.CAESAR.decode ("abc", 7));
	}

	@Test
	public void rot13ModeIgnoresTheShiftAndAlwaysRotatesByThirteen ()
	{
		assertEquals ("Uryyb", CipherMode.ROT13.encode ("Hello", 0));
		assertEquals ("Uryyb", CipherMode.ROT13.encode ("Hello", 7));
		assertFalse (CipherMode.ROT13.usesShift ());
	}

	@Test
	public void everyModeHasALabel ()
	{
		for (CipherMode mode : CipherMode.values ())
			assertNotEquals (mode.toString (), 0, mode.labelRes ());
	}

	@Test
	public void everyModeHandlesTheEmptyString ()
	{
		for (CipherMode mode : CipherMode.values ())
		{
			assertEquals (mode.toString (), "", mode.encode ("", 7));
			assertEquals (mode.toString (), "", mode.decode ("", 7));
		}
	}
}
