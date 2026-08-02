package be.robinj.rot13;

import android.app.Activity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import static org.junit.Assert.assertNotNull;

/**
 * Test-side access to the mode chips, which MainActivity builds at runtime from
 * {@link CipherMode#values()} and so have no fixed ids to look up.
 */
final class TestChips
{
	private TestChips ()
	{
	}

	static Chip chipFor (Activity activity, CipherMode mode)
	{
		ChipGroup group = activity.findViewById (R.id.chipsMode);

		for (int i = 0; i < group.getChildCount (); i++)
		{
			Chip chip = (Chip) group.getChildAt (i);
			if (chip.getTag () == mode)
				return chip;
		}

		return null;
	}

	static void selectMode (Activity activity, CipherMode mode)
	{
		Chip chip = chipFor (activity, mode);
		assertNotNull ("no chip for " + mode, chip);

		chip.setChecked (true);
	}
}
