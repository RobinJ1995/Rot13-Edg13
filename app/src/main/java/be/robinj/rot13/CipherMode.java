package be.robinj.rot13;

/**
 * The ciphers the app offers, each knowing how to encode and decode itself.
 *
 * <p>Every mode except {@link #CAESAR} is its own inverse, so encoding and
 * decoding are the same operation. Caesar is the exception: it decodes by
 * shifting the other way. Keeping that asymmetry inside the enum is what lets
 * both text fields stay editable in every mode.
 */
public enum CipherMode
{
	ROT13 (R.string.mode_rot13, false),
	CAESAR (R.string.mode_caesar, true),
	ROT47 (R.string.mode_rot47, false),
	ROT5 (R.string.mode_rot5, false),
	ROT18 (R.string.mode_rot18, false),
	ATBASH (R.string.mode_atbash, false);

	/** Where the shift slider starts when Caesar is first selected. */
	public static final int DEFAULT_SHIFT = 3;

	public static final int MIN_SHIFT = 0;
	public static final int MAX_SHIFT = 25;

	private final int labelRes;
	private final boolean usesShift;

	CipherMode (int labelRes, boolean usesShift)
	{
		this.labelRes = labelRes;
		this.usesShift = usesShift;
	}

	/** Plaintext to cipher text. */
	public String encode (String str, int shift)
	{
		switch (this)
		{
			case ROT13:
				return Ciphers.rot13 (str);
			case CAESAR:
				return Ciphers.caesar (str, shift);
			case ROT47:
				return Ciphers.rot47 (str);
			case ROT5:
				return Ciphers.rot5 (str);
			case ROT18:
				return Ciphers.rot18 (str);
			case ATBASH:
				return Ciphers.atbash (str);
			default:
				throw new IllegalStateException ("Unhandled cipher mode: " + this);
		}
	}

	/**
	 * Cipher text back to plaintext. Identical to {@link #encode} for every
	 * self-inverse mode; Caesar shifts in the opposite direction.
	 */
	public String decode (String str, int shift)
	{
		if (this == CAESAR)
			return Ciphers.caesar (str, -shift);

		return this.encode (str, shift);
	}

	/** Whether the shift slider is meaningful for this mode. */
	public boolean usesShift ()
	{
		return this.usesShift;
	}

	/**
	 * Whether encoding twice returns the original. True for everything but
	 * Caesar, where it only holds for shifts of 0 and 13.
	 */
	public boolean isSelfInverse (int shift)
	{
		if (this != CAESAR)
			return true;

		int n = Math.floorMod (shift, 26);
		return n == 0 || n == Ciphers.ROT13_SHIFT;
	}

	public int labelRes ()
	{
		return this.labelRes;
	}
}
