package be.robinj.rot13;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Properties of the cipher primitives, asserted over whole input ranges rather
 * than a handful of examples. Pure JVM — no Robolectric, no Android.
 */
public class CiphersTest
{
	private static final String SAMPLE =
			"The Quick Brown Fox Jumps Over The Lazy Dog! 0123456789 ~`@[`{";

	private static String printableAscii ()
	{
		StringBuilder sb = new StringBuilder ();
		for (char c = 32; c < 127; c++)
			sb.append (c);

		return sb.toString ();
	}

	// ---- caesar --------------------------------------------------------

	@Test
	public void caesarRoundTripsForEveryShift ()
	{
		for (int shift = 0; shift < 26; shift++)
			assertEquals ("shift " + shift,
					SAMPLE, Ciphers.caesar (Ciphers.caesar (SAMPLE, shift), -shift));
	}

	@Test
	public void caesarOfThirteenIsRot13 ()
	{
		assertEquals ("Uryyb, Jbeyq!", Ciphers.caesar ("Hello, World!", 13));
	}

	@Test
	public void caesarShiftIsNormalisedIntoRange ()
	{
		// -13, 13 and 39 are the same rotation; 26 and 0 are both the identity.
		assertEquals (Ciphers.caesar (SAMPLE, 13), Ciphers.caesar (SAMPLE, -13));
		assertEquals (Ciphers.caesar (SAMPLE, 13), Ciphers.caesar (SAMPLE, 39));
		assertEquals (SAMPLE, Ciphers.caesar (SAMPLE, 26));
		assertEquals (SAMPLE, Ciphers.caesar (SAMPLE, -26));
	}

	@Test
	public void caesarShiftsEveryLetterByTheGivenAmount ()
	{
		for (int shift = 0; shift < 26; shift++)
		{
			for (int i = 0; i < 26; i++)
			{
				char lower = (char) ('a' + i);
				char upper = (char) ('A' + i);

				assertEquals ("shift " + shift + " of '" + lower + "'",
						String.valueOf ((char) ('a' + (i + shift) % 26)),
						Ciphers.caesar (String.valueOf (lower), shift));
				assertEquals ("shift " + shift + " of '" + upper + "'",
						String.valueOf ((char) ('A' + (i + shift) % 26)),
						Ciphers.caesar (String.valueOf (upper), shift));
			}
		}
	}

	@Test
	public void caesarLeavesEverythingThatIsNotAnAsciiLetter ()
	{
		// The characters immediately outside each letter range are the ones an
		// off-by-one would catch: '@'(64), '['(91), '`'(96), '{'(123).
		assertEquals ("@[`{ 0123456789 !?", Ciphers.caesar ("@[`{ 0123456789 !?", 7));
	}

	@Test
	public void caesarZeroIsTheIdentity ()
	{
		assertEquals (SAMPLE, Ciphers.caesar (SAMPLE, 0));
	}

	@Test
	public void caesarPreservesWhitespaceAndLength ()
	{
		String input = "a b\tc\nd";
		assertEquals ("h i\tj\nk", Ciphers.caesar (input, 7));
		assertEquals (input.length (), Ciphers.caesar (input, 7).length ());
	}

	// ---- rot47 ---------------------------------------------------------

	@Test
	public void rot47IsSelfInverseAcrossPrintableAscii ()
	{
		String original = printableAscii ();
		String once = Ciphers.rot47 (original);

		assertEquals (original, Ciphers.rot47 (once));
		assertNotEquals (original, once);
	}

	@Test
	public void rot47CoversTheBoundariesOfItsRange ()
	{
		// '!' is 33, the bottom of the range; '~' is 126, the top.
		assertEquals ("P", Ciphers.rot47 ("!"));
		assertEquals ("O", Ciphers.rot47 ("~"));
	}

	@Test
	public void rot47LeavesTheSpaceAlone ()
	{
		// 32 sits just below the range by definition, which is what keeps word
		// boundaries visible in the output.
		assertEquals ("? ?", Ciphers.rot47 ("n n"));
		assertEquals ("   ", Ciphers.rot47 ("   "));
	}

	@Test
	public void rot47CoversDigitsAndPunctuationUnlikeRot13 ()
	{
		assertNotEquals ("2024", Ciphers.rot47 ("2024"));
		assertNotEquals ("!?", Ciphers.rot47 ("!?"));
	}

	// ---- rot5 ----------------------------------------------------------

	@Test
	public void rot5RotatesDigitsAndNothingElse ()
	{
		assertEquals ("5678901234", Ciphers.rot5 ("0123456789"));
		assertEquals ("abcXYZ", Ciphers.rot5 ("abcXYZ"));
	}

	@Test
	public void rot5IsSelfInverse ()
	{
		assertEquals (SAMPLE, Ciphers.rot5 (Ciphers.rot5 (SAMPLE)));
	}

	// ---- rot18 ---------------------------------------------------------

	@Test
	public void rot18RotatesLettersAndDigitsTogether ()
	{
		assertEquals ("nop678", Ciphers.rot18 ("abc123"));
	}

	@Test
	public void rot18IsSelfInverse ()
	{
		assertEquals (SAMPLE, Ciphers.rot18 (Ciphers.rot18 (SAMPLE)));
	}

	@Test
	public void rot18ClosesTheDigitGapRot13Leaves ()
	{
		assertEquals ("2024", Ciphers.caesar ("2024", 13));
		assertEquals ("7579", Ciphers.rot18 ("2024"));
	}

	// ---- atbash --------------------------------------------------------

	@Test
	public void atbashMirrorsTheAlphabet ()
	{
		assertEquals ("zyxw", Ciphers.atbash ("abcd"));
		assertEquals ("ZYXW", Ciphers.atbash ("ABCD"));
	}

	@Test
	public void atbashIsSelfInverse ()
	{
		assertEquals (SAMPLE, Ciphers.atbash (Ciphers.atbash (SAMPLE)));
	}

	@Test
	public void atbashMapsEveryLetterToItsMirror ()
	{
		for (int i = 0; i < 26; i++)
		{
			assertEquals (String.valueOf ((char) ('z' - i)),
					Ciphers.atbash (String.valueOf ((char) ('a' + i))));
			assertEquals (String.valueOf ((char) ('Z' - i)),
					Ciphers.atbash (String.valueOf ((char) ('A' + i))));
		}
	}

	// ---- shared contracts ----------------------------------------------

	@Test
	public void everyCipherPassesNonAsciiThroughUnchanged ()
	{
		// This is a documented contract, not an accident of the implementation:
		// accented Latin, Cyrillic, CJK and emoji all fall outside every range.
		String exotic = "é ñ Привет 你好 🙂";

		assertEquals (exotic, Ciphers.caesar (exotic, 7));
		assertEquals (exotic, Ciphers.rot47 (exotic));
		assertEquals (exotic, Ciphers.rot5 (exotic));
		assertEquals (exotic, Ciphers.rot18 (exotic));
		assertEquals (exotic, Ciphers.atbash (exotic));
	}

	@Test
	public void everyCipherHandlesTheEmptyString ()
	{
		assertEquals ("", Ciphers.caesar ("", 7));
		assertEquals ("", Ciphers.rot47 (""));
		assertEquals ("", Ciphers.rot5 (""));
		assertEquals ("", Ciphers.rot18 (""));
		assertEquals ("", Ciphers.atbash (""));
	}

	@Test
	public void everyCipherPreservesLength ()
	{
		String input = printableAscii () + "é 你好";

		assertEquals (input.length (), Ciphers.caesar (input, 7).length ());
		assertEquals (input.length (), Ciphers.rot47 (input).length ());
		assertEquals (input.length (), Ciphers.rot5 (input).length ());
		assertEquals (input.length (), Ciphers.rot18 (input).length ());
		assertEquals (input.length (), Ciphers.atbash (input).length ());
	}
}
