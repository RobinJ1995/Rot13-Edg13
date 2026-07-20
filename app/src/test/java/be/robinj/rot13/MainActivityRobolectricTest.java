package be.robinj.rot13;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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
 * Robolectric tests that drive the real MainActivity on the JVM: they inflate
 * the layout, run the fragment transaction and exercise the options-menu
 * navigation, covering behaviour the static rotate() tests never touch.
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityRobolectricTest {

	@Test
	public void launchInflatesLayoutAndAddsPlaceholderFragment() {
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity(MainActivity.class).setup()) {
			MainActivity activity = controller.get();
			assertNotNull(activity);

			activity.getSupportFragmentManager().executePendingTransactions();
			Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.container);
			assertNotNull("PlaceholderFragment should be added to the container", fragment);
			assertTrue(fragment instanceof MainActivity.PlaceholderFragment);
		}
	}

	@Test
	public void aboutMenuItemLaunchesAboutActivity() {
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity(MainActivity.class).setup()) {
			MainActivity activity = controller.get();

			MenuItem aboutItem = new RoboMenuItem(R.id.action_about);
			assertTrue(activity.onOptionsItemSelected(aboutItem));

			ShadowActivity shadow = Shadows.shadowOf(activity);
			Intent started = shadow.getNextStartedActivity();
			assertNotNull("selecting About should start an activity", started);
			assertEquals(About.class.getName(), started.getComponent().getClassName());
		}
	}

	@Test
	public void onCreateOptionsMenuInflatesTheAboutItem() {
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity(MainActivity.class).setup()) {
			MainActivity activity = controller.get();

			RoboMenu menu = new RoboMenu(activity);
			assertTrue(activity.onCreateOptionsMenu(menu));
			assertNotNull("About item should be inflated into the menu",
					menu.findItem(R.id.action_about));
		}
	}

	@Test
	public void unhandledMenuItemIsDelegatedToSuper() {
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity(MainActivity.class).setup()) {
			MainActivity activity = controller.get();

			// An id the activity does not recognise must fall through to the
			// AppCompat superclass, which reports it as unhandled.
			assertFalse(activity.onOptionsItemSelected(new RoboMenuItem(0)));
		}
	}

	@Test
	public void typingPlaintextLiveEncodesIntoTheRot13Field() {
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity(MainActivity.class).setup()) {
			MainActivity activity = controller.get();

			TextView txtPlain = activity.findViewById(R.id.txtPlain);
			TextView txtRot13 = activity.findViewById(R.id.txtRot13);

			assertTrue("plaintext field should take focus", txtPlain.requestFocus());
			txtPlain.setText("Hello, World!");

			assertEquals("Uryyb, Jbeyq!", txtRot13.getText().toString());
		}
	}

	@Test
	public void typingCiphertextLiveDecodesIntoThePlaintextField() {
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity(MainActivity.class).setup()) {
			MainActivity activity = controller.get();

			TextView txtPlain = activity.findViewById(R.id.txtPlain);
			TextView txtRot13 = activity.findViewById(R.id.txtRot13);

			assertTrue("ciphertext field should take focus", txtRot13.requestFocus());
			txtRot13.setText("Uryyb, Jbeyq!");

			assertEquals("Hello, World!", txtPlain.getText().toString());
		}
	}

	@Test
	public void editingAnUnfocusedFieldDoesNotOverwriteTheOther() {
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity(MainActivity.class).setup()) {
			MainActivity activity = controller.get();

			TextView txtPlain = activity.findViewById(R.id.txtPlain);
			TextView txtRot13 = activity.findViewById(R.id.txtRot13);

			// Give focus to the ciphertext field, then mutate the (unfocused)
			// plaintext field programmatically. The listener is gated on
			// hasFocus(), so the ciphertext must stay put.
			assertTrue(txtRot13.requestFocus());
			txtRot13.setText("Purpx");
			txtPlain.setText("ignored");

			assertEquals("Purpx", txtRot13.getText().toString());
		}
	}

	@Test
	public void editingClearsAnyErrorMarkersOnBothFields() {
		try (ActivityController<MainActivity> controller =
				 Robolectric.buildActivity(MainActivity.class).setup()) {
			MainActivity activity = controller.get();

			TextView txtPlain = activity.findViewById(R.id.txtPlain);
			TextView txtRot13 = activity.findViewById(R.id.txtRot13);

			txtPlain.setError("boom");
			txtRot13.setError("boom");

			assertTrue(txtPlain.requestFocus());
			txtPlain.setText("reset");

			// beforeTextChanged wipes the error state on both fields.
			assertEquals(null, txtPlain.getError());
			assertEquals(null, txtRot13.getError());
		}
	}
}
