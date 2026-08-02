package be.robinj.rot13;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Drives the real MainActivity on the JVM: inflates the layout, switches
 * ciphers, moves the shift slider and copies to the clipboard, covering the
 * wiring the pure {@link Ciphers} tests cannot reach.
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityRobolectricTest
{
	private static ActivityController<MainActivity> launch ()
	{
		return Robolectric.buildActivity (MainActivity.class).setup ();
	}

	private static Chip chipFor (MainActivity activity, CipherMode mode)
	{
		return TestChips.chipFor (activity, mode);
	}

	private static void selectMode (MainActivity activity, CipherMode mode)
	{
		TestChips.selectMode (activity, mode);
	}

	private static String clipboardText (MainActivity activity)
	{
		ClipboardManager clipboard =
				(ClipboardManager) activity.getSystemService (Context.CLIPBOARD_SERVICE);

		if (clipboard == null || clipboard.getPrimaryClip () == null)
			return null;

		return clipboard.getPrimaryClip ().getItemAt (0).getText ().toString ();
	}

	// ---- launch and navigation -----------------------------------------

	@Test
	public void launchInflatesBothFields ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();

			assertNotNull (activity.findViewById (R.id.txtPlain));
			assertNotNull (activity.findViewById (R.id.txtRot13));
			assertNotNull (activity.findViewById (R.id.toolbar));
		}
	}

	@Test
	public void aboutMenuItemLaunchesAboutActivity ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();

			assertTrue (activity.onOptionsItemSelected (new RoboMenuItem (R.id.action_about)));

			ShadowActivity shadow = Shadows.shadowOf (activity);
			Intent started = shadow.getNextStartedActivity ();
			assertNotNull ("selecting About should start an activity", started);
			assertEquals (About.class.getName (), started.getComponent ().getClassName ());
		}
	}

	@Test
	public void onCreateOptionsMenuInflatesTheAboutItem ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();

			RoboMenu menu = new RoboMenu (activity);
			assertTrue (activity.onCreateOptionsMenu (menu));
			assertNotNull (menu.findItem (R.id.action_about));
		}
	}

	@Test
	public void unhandledMenuItemIsDelegatedToSuper ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MenuItem unknown = new RoboMenuItem (0);
			assertFalse (controller.get ().onOptionsItemSelected (unknown));
		}
	}

	@Test
	public void rotateStillDelegatesToRot13 ()
	{
		// The long-standing public entry point, kept working after the logic
		// moved out into Ciphers.
		assertEquals ("Uryyb, Jbeyq!", MainActivity.rotate ("Hello, World!"));
	}

	// ---- live translation ----------------------------------------------

	@Test
	public void typingPlaintextLiveEncodesIntoTheCipherField ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();

			((EditText) activity.findViewById (R.id.txtPlain)).setText ("Hello, World!");

			assertEquals ("Uryyb, Jbeyq!",
					((TextView) activity.findViewById (R.id.txtRot13)).getText ().toString ());
		}
	}

	@Test
	public void typingCiphertextLiveDecodesIntoThePlaintextField ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();

			((EditText) activity.findViewById (R.id.txtRot13)).setText ("Uryyb, Jbeyq!");

			assertEquals ("Hello, World!",
					((TextView) activity.findViewById (R.id.txtPlain)).getText ().toString ());
		}
	}

	@Test
	public void programmaticWritesPropagateAndTerminate ()
	{
		// The old hasFocus() gate blocked programmatic writes entirely, which
		// would silently have broken shared text. The isUpdating guard replaces
		// it: a write propagates exactly once and does not bounce back. Were the
		// guard to regress, this test would hang or blow the stack rather than
		// fail an assertion.
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			EditText plain = activity.findViewById (R.id.txtPlain);
			EditText cipher = activity.findViewById (R.id.txtRot13);

			plain.setText ("abc");
			assertEquals ("nop", cipher.getText ().toString ());

			cipher.setText ("nop");
			assertEquals ("abc", plain.getText ().toString ());
		}
	}

	@Test
	public void editingClearsAnyErrorMarkersOnBothFields ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			EditText plain = activity.findViewById (R.id.txtPlain);
			EditText cipher = activity.findViewById (R.id.txtRot13);

			plain.setError ("boom");
			cipher.setError ("boom");

			plain.setText ("reset");

			assertEquals (null, plain.getError ());
			assertEquals (null, cipher.getError ());
		}
	}

	// ---- mode switching ------------------------------------------------

	@Test
	public void everyModeGetsAChip ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			ChipGroup group = activity.findViewById (R.id.chipsMode);

			assertEquals (CipherMode.values ().length, group.getChildCount ());

			for (CipherMode mode : CipherMode.values ())
				assertNotNull (mode.toString (), chipFor (activity, mode));
		}
	}

	@Test
	public void switchingModeRecomputesTheOutputImmediately ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			EditText plain = activity.findViewById (R.id.txtPlain);
			EditText cipher = activity.findViewById (R.id.txtRot13);

			plain.setText ("abc");
			assertEquals ("nop", cipher.getText ().toString ());

			selectMode (activity, CipherMode.ATBASH);
			assertEquals ("zyx", cipher.getText ().toString ());

			selectMode (activity, CipherMode.ROT47);
			assertEquals (Ciphers.rot47 ("abc"), cipher.getText ().toString ());
		}
	}

	@Test
	public void switchingModeRecomputesFromWhicheverFieldWasEditedLast ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			EditText plain = activity.findViewById (R.id.txtPlain);
			EditText cipher = activity.findViewById (R.id.txtRot13);

			// The last edit was on the cipher side, so a mode change must
			// recompute the plaintext and leave the user's own text alone.
			cipher.setText ("zyx");
			selectMode (activity, CipherMode.ATBASH);

			assertEquals ("zyx", cipher.getText ().toString ());
			assertEquals ("abc", plain.getText ().toString ());
		}
	}

	@Test
	public void modeToggleLabelTracksTheSelectedMode ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			MaterialButton toggle = activity.findViewById (R.id.btnModeToggle);

			assertEquals (activity.getString (R.string.mode_rot13), toggle.getText ().toString ());

			selectMode (activity, CipherMode.ATBASH);
			assertEquals (activity.getString (R.string.mode_atbash), toggle.getText ().toString ());
		}
	}

	@Test
	public void modePanelStartsCollapsedAndTogglesOpen ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			View panel = activity.findViewById (R.id.panelMode);

			assertEquals (View.GONE, panel.getVisibility ());

			activity.findViewById (R.id.btnModeToggle).performClick ();
			assertEquals (View.VISIBLE, panel.getVisibility ());

			activity.findViewById (R.id.btnModeToggle).performClick ();
			assertEquals (View.GONE, panel.getVisibility ());
		}
	}

	// ---- the shift slider ----------------------------------------------

	@Test
	public void shiftControlsAreHiddenUnlessCaesarIsSelected ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			View slider = activity.findViewById (R.id.sliderShift);
			View label = activity.findViewById (R.id.lblShift);

			assertEquals (View.GONE, slider.getVisibility ());
			assertEquals (View.GONE, label.getVisibility ());

			selectMode (activity, CipherMode.CAESAR);
			assertEquals (View.VISIBLE, slider.getVisibility ());
			assertEquals (View.VISIBLE, label.getVisibility ());

			selectMode (activity, CipherMode.ROT13);
			assertEquals (View.GONE, slider.getVisibility ());
		}
	}

	@Test
	public void movingTheShiftSliderRecomputes ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			EditText plain = activity.findViewById (R.id.txtPlain);
			EditText cipher = activity.findViewById (R.id.txtRot13);
			Slider slider = activity.findViewById (R.id.sliderShift);

			plain.setText ("abc");
			selectMode (activity, CipherMode.CAESAR);

			slider.setValue (7f);
			assertEquals ("hij", cipher.getText ().toString ());

			slider.setValue (1f);
			assertEquals ("bcd", cipher.getText ().toString ());
		}
	}

	@Test
	public void caesarDecodesInTheOppositeDirection ()
	{
		// The one non-self-inverse mode: editing the cipher side has to shift
		// backwards, not forwards.
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			EditText plain = activity.findViewById (R.id.txtPlain);
			EditText cipher = activity.findViewById (R.id.txtRot13);
			Slider slider = activity.findViewById (R.id.sliderShift);

			selectMode (activity, CipherMode.CAESAR);
			slider.setValue (7f);

			cipher.setText ("hij");
			assertEquals ("abc", plain.getText ().toString ());
		}
	}

	@Test
	public void shiftLabelShowsTheCurrentValue ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			TextView label = activity.findViewById (R.id.lblShift);

			selectMode (activity, CipherMode.CAESAR);
			((Slider) activity.findViewById (R.id.sliderShift)).setValue (9f);

			assertEquals (activity.getString (R.string.shift_value, 9), label.getText ().toString ());
		}
	}

	// ---- clipboard ------------------------------------------------------

	@Test
	public void longPressingAFieldCopiesItToTheClipboard ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			EditText plain = activity.findViewById (R.id.txtPlain);
			EditText cipher = activity.findViewById (R.id.txtRot13);

			plain.setText ("Hello");

			assertTrue (cipher.performLongClick ());
			assertEquals ("Uryyb", clipboardText (activity));

			assertTrue (plain.performLongClick ());
			assertEquals ("Hello", clipboardText (activity));
		}
	}

	@Test
	public void theCopyEndIconCopiesTheFieldItBelongsTo ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			((EditText) activity.findViewById (R.id.txtPlain)).setText ("Hello");

			View endIcon = activity.findViewById (R.id.layoutRot13)
					.findViewById (com.google.android.material.R.id.text_input_end_icon);
			assertNotNull ("the cipher field should have a copy end icon", endIcon);

			endIcon.performClick ();
			assertEquals ("Uryyb", clipboardText (activity));
		}
	}

	// ---- state retention ------------------------------------------------

	@Test
	public void modeShiftAndTextSurviveRecreation ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			MainActivity activity = controller.get ();
			((EditText) activity.findViewById (R.id.txtPlain)).setText ("abc");
			selectMode (activity, CipherMode.CAESAR);
			((Slider) activity.findViewById (R.id.sliderShift)).setValue (7f);
			activity.findViewById (R.id.btnModeToggle).performClick ();

			assertEquals ("hij", ((TextView) activity.findViewById (R.id.txtRot13)).getText ().toString ());

			controller.recreate ();
			MainActivity restored = controller.get ();

			assertEquals ("abc", ((TextView) restored.findViewById (R.id.txtPlain)).getText ().toString ());
			assertEquals ("hij", ((TextView) restored.findViewById (R.id.txtRot13)).getText ().toString ());
			assertEquals (restored.getString (R.string.mode_caesar),
					((MaterialButton) restored.findViewById (R.id.btnModeToggle)).getText ().toString ());
			assertEquals (7f, ((Slider) restored.findViewById (R.id.sliderShift)).getValue (), 0.001f);
			assertEquals (View.VISIBLE, restored.findViewById (R.id.panelMode).getVisibility ());
		}
	}

	@Test
	public void theCheckedChipSurvivesRecreation ()
	{
		try (ActivityController<MainActivity> controller = launch ())
		{
			selectMode (controller.get (), CipherMode.ATBASH);

			controller.recreate ();
			MainActivity restored = controller.get ();

			Chip atbash = chipFor (restored, CipherMode.ATBASH);
			assertNotNull (atbash);
			assertTrue ("the Atbash chip should still be checked", atbash.isChecked ());
		}
	}
}
