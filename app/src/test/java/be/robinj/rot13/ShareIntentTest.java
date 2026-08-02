package be.robinj.rot13;

import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.assertEquals;

/**
 * Text shared into the app from elsewhere. This is the path that lets the app
 * be reached without copying, switching and pasting by hand, so it is worth
 * covering the awkward senders as well as the well-behaved one.
 */
@RunWith(RobolectricTestRunner.class)
public class ShareIntentTest
{
	private static Intent share (CharSequence text)
	{
		Intent intent = new Intent (Intent.ACTION_SEND);
		intent.setType ("text/plain");

		if (text != null)
			intent.putExtra (Intent.EXTRA_TEXT, text);

		return intent;
	}

	private static ActivityController<MainActivity> launchWith (Intent intent)
	{
		return Robolectric.buildActivity (MainActivity.class, intent).setup ();
	}

	@Test
	public void sharedTextPrefillsThePlaintextFieldAndIsEncoded ()
	{
		try (ActivityController<MainActivity> controller = launchWith (share ("Hello, World!")))
		{
			MainActivity activity = controller.get ();

			assertEquals ("Hello, World!",
					((TextView) activity.findViewById (R.id.txtPlain)).getText ().toString ());
			assertEquals ("Uryyb, Jbeyq!",
					((TextView) activity.findViewById (R.id.txtRot13)).getText ().toString ());
		}
	}

	@Test
	public void aSenderThatAttachesNoTextDoesNotCrash ()
	{
		// text/plain is declared but EXTRA_TEXT is absent entirely.
		try (ActivityController<MainActivity> controller = launchWith (share (null)))
		{
			MainActivity activity = controller.get ();

			assertEquals ("", ((TextView) activity.findViewById (R.id.txtPlain)).getText ().toString ());
			assertEquals ("", ((TextView) activity.findViewById (R.id.txtRot13)).getText ().toString ());
		}
	}

	@Test
	public void aNonTextShareIsIgnored ()
	{
		Intent intent = new Intent (Intent.ACTION_SEND);
		intent.setType ("image/png");
		intent.putExtra (Intent.EXTRA_TEXT, "Hello");

		try (ActivityController<MainActivity> controller = launchWith (intent))
		{
			assertEquals ("",
					((TextView) controller.get ().findViewById (R.id.txtPlain)).getText ().toString ());
		}
	}

	@Test
	public void aPlainLaunchIsUnaffected ()
	{
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity (MainActivity.class).setup ())
		{
			assertEquals ("",
					((TextView) controller.get ().findViewById (R.id.txtPlain)).getText ().toString ());
		}
	}

	@Test
	public void aSecondShareReplacesTheContent ()
	{
		try (ActivityController<MainActivity> controller = launchWith (share ("Hello")))
		{
			MainActivity activity = controller.get ();
			assertEquals ("Uryyb", ((TextView) activity.findViewById (R.id.txtRot13)).getText ().toString ());

			controller.newIntent (share ("Goodbye"));

			assertEquals ("Goodbye", ((TextView) activity.findViewById (R.id.txtPlain)).getText ().toString ());
			assertEquals ("Tbbqolr", ((TextView) activity.findViewById (R.id.txtRot13)).getText ().toString ());
		}
	}

	@Test
	public void sharedTextIsEncodedWithWhicheverModeIsActive ()
	{
		try (ActivityController<MainActivity> controller = launchWith (share ("abc")))
		{
			MainActivity activity = controller.get ();

			// Switch to Atbash, then share again: the new text must go through
			// the mode the user has chosen, not the default.
			TestChips.selectMode (activity, CipherMode.ATBASH);
			controller.newIntent (share ("abc"));

			assertEquals ("zyx", ((TextView) activity.findViewById (R.id.txtRot13)).getText ().toString ());
		}
	}

	@Test
	public void sharedTextSurvivesRecreation ()
	{
		// A share followed by a rotation must not re-read the intent and undo
		// whatever the user has typed since.
		try (ActivityController<MainActivity> controller = launchWith (share ("Hello")))
		{
			((EditText) controller.get ().findViewById (R.id.txtPlain)).setText ("Edited");

			controller.recreate ();

			assertEquals ("Edited",
					((TextView) controller.get ().findViewById (R.id.txtPlain)).getText ().toString ());
		}
	}
}
