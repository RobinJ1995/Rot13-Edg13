package be.robinj.rot13;

/**
 * The cipher primitives, kept free of any Android dependency so they can be
 * unit tested on a plain JVM.
 *
 * <p>Every cipher here operates on ASCII only. Characters outside the range a
 * cipher covers — accented Latin, Cyrillic, CJK, emoji — are passed through
 * unchanged. That is a deliberate contract rather than an accident of the
 * implementation, and the test suite asserts it.
 */
public final class Ciphers
{
	/** The shift that turns {@code caesar} into the classic ROT13. */
	public static final int ROT13_SHIFT = 13;

	private Ciphers ()
	{
	}

	/**
	 * Shifts every ASCII letter along the alphabet, wrapping at the ends and
	 * preserving case. Digits, punctuation and non-ASCII are untouched.
	 *
	 * @param shift any integer; it is reduced modulo 26, so negative and
	 *              out-of-range values are normalised rather than rejected.
	 *              This is what makes decoding {@code caesar(s, -n)}.
	 */
	public static String caesar (String str, int shift)
	{
		int n = Math.floorMod (shift, 26);
		if (n == 0)
			return str;

		StringBuilder rotated = new StringBuilder (str.length ());
		for (int i = 0; i < str.length (); i++)
		{
			char c = str.charAt (i);

			if (c >= 'a' && c <= 'z')
				c = (char) ('a' + (c - 'a' + n) % 26);
			else if (c >= 'A' && c <= 'Z')
				c = (char) ('A' + (c - 'A' + n) % 26);

			rotated.append (c);
		}

		return rotated.toString ();
	}

	/** The app's namesake: {@link #caesar} at a shift of 13, so its own inverse. */
	public static String rot13 (String str)
	{
		return caesar (str, ROT13_SHIFT);
	}

	/**
	 * Rotates the 94 printable ASCII characters '!' (33) through '~' (126) by
	 * 47 — half the range, so it is its own inverse. The space (32) is outside
	 * the range by definition, which keeps word boundaries readable.
	 */
	public static String rot47 (String str)
	{
		StringBuilder rotated = new StringBuilder (str.length ());
		for (int i = 0; i < str.length (); i++)
		{
			char c = str.charAt (i);

			if (c >= '!' && c <= '~')
				c = (char) ('!' + (c - '!' + 47) % 94);

			rotated.append (c);
		}

		return rotated.toString ();
	}

	/**
	 * Rotates digits by 5, wrapping 0-9. Its own inverse. Letters are left
	 * alone, which is what makes it composable with ROT13.
	 */
	public static String rot5 (String str)
	{
		StringBuilder rotated = new StringBuilder (str.length ());
		for (int i = 0; i < str.length (); i++)
		{
			char c = str.charAt (i);

			if (c >= '0' && c <= '9')
				c = (char) ('0' + (c - '0' + 5) % 10);

			rotated.append (c);
		}

		return rotated.toString ();
	}

	/**
	 * ROT13 on letters and ROT5 on digits at the same time — the combination
	 * that ROT13 alone misses, which silently leaves strings like "2024" intact.
	 */
	public static String rot18 (String str)
	{
		return rot5 (rot13 (str));
	}

	/**
	 * Mirrors the alphabet: a&lt;-&gt;z, b&lt;-&gt;y, and so on, preserving
	 * case. Its own inverse.
	 */
	public static String atbash (String str)
	{
		StringBuilder rotated = new StringBuilder (str.length ());
		for (int i = 0; i < str.length (); i++)
		{
			char c = str.charAt (i);

			if (c >= 'a' && c <= 'z')
				c = (char) ('a' + 'z' - c);
			else if (c >= 'A' && c <= 'Z')
				c = (char) ('A' + 'Z' - c);

			rotated.append (c);
		}

		return rotated.toString ();
	}
}
