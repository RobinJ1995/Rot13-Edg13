package be.robinj.rot13;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The About screen: inflates the real layout, checks the version label is
 * populated from the package metadata and that the developer button fires a
 * browser intent.
 */
@RunWith(RobolectricTestRunner.class)
public class AboutRobolectricTest
{
	private static ActivityController<About> launch ()
	{
		return Robolectric.buildActivity (About.class).setup ();
	}

	@Test
	public void launchInflatesTheLayout ()
	{
		try (ActivityController<About> controller = launch ())
		{
			About activity = controller.get ();

			assertNotNull (activity.findViewById (R.id.toolbar));
			assertNotNull (activity.findViewById (R.id.imgLogo));
			assertNotNull (activity.findViewById (R.id.version));
			assertNotNull (activity.findViewById (R.id.dev));
		}
	}

	@Test
	public void versionLabelIsResolvedFromPackageInfo ()
	{
		try (ActivityController<About> controller = launch ())
		{
			TextView version = controller.get ().findViewById (R.id.version);
			String text = version.getText ().toString ();

			// The code prefixes the resolved version name with "v"; the "{...}"
			// form only appears when the package cannot be found.
			assertTrue ("version should start with 'v' but was: " + text, text.startsWith ("v"));
			assertFalse ("package metadata should resolve without error: " + text, text.startsWith ("{"));
		}
	}

	@Test
	public void developerButtonOpensTheDeveloperWebsite ()
	{
		try (ActivityController<About> controller = launch ())
		{
			About activity = controller.get ();

			View dev = activity.findViewById (R.id.dev);
			assertNotNull ("developer button should be present", dev);
			assertTrue ("the button should be wired up in onCreate", dev.performClick ());

			ShadowActivity shadow = Shadows.shadowOf (activity);
			Intent started = shadow.getNextStartedActivity ();
			assertNotNull ("clicking developer should start an activity", started);
			assertEquals (Intent.ACTION_VIEW, started.getAction ());
			assertEquals (Uri.parse ("http://www.robinj.be/"), started.getData ());
		}
	}

	@Test
	public void onCreateOptionsMenuReturnsTrue ()
	{
		try (ActivityController<About> controller = launch ())
		{
			About activity = controller.get ();
			assertTrue (activity.onCreateOptionsMenu (new RoboMenu (activity)));
		}
	}

	@Test
	public void theUpArrowFinishesTheScreen ()
	{
		try (ActivityController<About> controller = launch ())
		{
			About activity = controller.get ();

			assertTrue (activity.onSupportNavigateUp ());
			assertTrue ("navigating up should finish About", activity.isFinishing ());
		}
	}
}
