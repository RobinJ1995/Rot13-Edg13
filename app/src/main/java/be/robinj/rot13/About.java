package be.robinj.rot13;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class About extends AppCompatActivity
{
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_about);

		setSupportActionBar ((MaterialToolbar) findViewById (R.id.toolbar));

		ActionBar actionBar = getSupportActionBar ();
		if (actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled (true);

		findViewById (R.id.dev).setOnClickListener (this::btnDev_clicked);

		TextView version = findViewById (R.id.version);
		String v;

		try
		{
			PackageInfo pkgInfo = getPackageManager ().getPackageInfo (getPackageName (), 0);
			v = "v" + pkgInfo.versionName;
		}
		catch (PackageManager.NameNotFoundException e)
		{
			v = "{" + e.toString () + "}";
		}

		version.setText (v);
	}

	public void btnDev_clicked (View view)
	{
		Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse ("http://www.robinj.be/"));
		this.startActivity (intent);
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ().inflate (R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onSupportNavigateUp ()
	{
		// The toolbar's up arrow, which the manifest's parentActivityName used
		// to drive on its own before the ActionBar became ours to set up.
		finish ();
		return true;
	}
}
