package be.robinj.rot13;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity
{
	private static final String STATE_MODE = "cipherMode";
	private static final String STATE_SHIFT = "cipherShift";
	private static final String STATE_PANEL_OPEN = "panelOpen";
	private static final String STATE_LAST_EDITED_PLAIN = "lastEditedPlain";

	private static final String MIME_TEXT = "text/plain";

	private EditText txtPlain;
	private EditText txtRot13;
	private TextInputLayout layoutPlain;
	private TextInputLayout layoutRot13;
	private MaterialButton btnModeToggle;
	private View panelMode;
	private ChipGroup chipsMode;
	private TextView lblShift;
	private Slider sliderShift;

	private CipherMode mode = CipherMode.ROT13;
	private int shift = CipherMode.DEFAULT_SHIFT;

	/**
	 * Which field the user last typed in. Determines which way a mode or shift
	 * change recomputes, so changing cipher does not silently swap over what
	 * the user considers their input.
	 */
	private boolean lastEditedPlain = true;

	/**
	 * Re-entrancy guard. Writing to one field triggers the other field's
	 * TextWatcher, which would write back and loop forever. The old code gated
	 * on hasFocus() instead, which cannot work once programmatic writes — a
	 * shared intent, a mode change — also have to propagate.
	 */
	private boolean isUpdating;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_main);

		setSupportActionBar ((MaterialToolbar) findViewById (R.id.toolbar));

		this.txtPlain = findViewById (R.id.txtPlain);
		this.txtRot13 = findViewById (R.id.txtRot13);
		this.layoutPlain = findViewById (R.id.layoutPlain);
		this.layoutRot13 = findViewById (R.id.layoutRot13);
		this.btnModeToggle = findViewById (R.id.btnModeToggle);
		this.panelMode = findViewById (R.id.panelMode);
		this.chipsMode = findViewById (R.id.chipsMode);
		this.lblShift = findViewById (R.id.lblShift);
		this.sliderShift = findViewById (R.id.sliderShift);

		// Restored before the framework hands the fields their text back, so the
		// watchers that fire during view restore already use the right cipher.
		if (savedInstanceState != null)
		{
			this.mode = CipherMode.valueOf (savedInstanceState.getString (STATE_MODE, CipherMode.ROT13.name ()));
			this.shift = savedInstanceState.getInt (STATE_SHIFT, CipherMode.DEFAULT_SHIFT);
			this.lastEditedPlain = savedInstanceState.getBoolean (STATE_LAST_EDITED_PLAIN, true);
			this.panelMode.setVisibility (savedInstanceState.getBoolean (STATE_PANEL_OPEN) ? View.VISIBLE : View.GONE);
		}

		this.buildModeChips ();
		this.wireTextFields ();
		this.wireCopyButtons ();

		this.btnModeToggle.setOnClickListener (view -> this.togglePanel ());

		this.sliderShift.setValue (this.shift);
		this.sliderShift.addOnChangeListener ((slider, value, fromUser) -> this.setShift ((int) value));

		this.syncModeViews ();

		// Only on a fresh start: after a config change the fields have already
		// been restored, and re-reading the intent would clobber the user's
		// edits with whatever was originally shared.
		if (savedInstanceState == null)
			this.handleSendIntent (getIntent ());
	}

	@Override
	protected void onNewIntent (Intent intent)
	{
		super.onNewIntent (intent);

		setIntent (intent);
		this.handleSendIntent (intent);
	}

	@Override
	protected void onSaveInstanceState (@NonNull Bundle outState)
	{
		super.onSaveInstanceState (outState);

		// The field contents save themselves via the views' own ids; only the
		// cipher settings need doing by hand.
		outState.putString (STATE_MODE, this.mode.name ());
		outState.putInt (STATE_SHIFT, this.shift);
		outState.putBoolean (STATE_PANEL_OPEN, this.panelMode.getVisibility () == View.VISIBLE);
		outState.putBoolean (STATE_LAST_EDITED_PLAIN, this.lastEditedPlain);
	}

	/**
	 * Accepts text shared from any other app: the shared text becomes the
	 * plaintext, and the normal pipeline encodes it.
	 */
	private void handleSendIntent (Intent intent)
	{
		if (intent == null || !Intent.ACTION_SEND.equals (intent.getAction ()))
			return;

		if (!MIME_TEXT.equals (intent.getType ()))
			return;

		// EXTRA_TEXT is a CharSequence and may be absent entirely; a sender that
		// declares text/plain without attaching any must not crash us.
		CharSequence shared = intent.getCharSequenceExtra (Intent.EXTRA_TEXT);
		if (shared == null)
			return;

		this.lastEditedPlain = true;
		this.txtPlain.setText (shared);
		this.txtPlain.setSelection (this.txtPlain.getText ().length ());
	}

	/**
	 * Builds one chip per cipher, so adding a mode to {@link CipherMode} is the
	 * only edit needed to surface it.
	 */
	private void buildModeChips ()
	{
		for (CipherMode candidate : CipherMode.values ())
		{
			Chip chip = (Chip) getLayoutInflater ().inflate (R.layout.item_mode_chip, this.chipsMode, false);
			chip.setId (View.generateViewId ());
			chip.setText (candidate.labelRes ());
			chip.setTag (candidate);
			chip.setChecked (candidate == this.mode);

			this.chipsMode.addView (chip);
		}

		this.chipsMode.setOnCheckedStateChangeListener ((group, checkedIds) ->
		{
			if (checkedIds.isEmpty ())
				return;

			Chip checked = group.findViewById (checkedIds.get (0));
			if (checked != null)
				this.setMode ((CipherMode) checked.getTag ());
		});
	}

	private void wireTextFields ()
	{
		this.txtPlain.addTextChangedListener (new Retranslator (true));
		this.txtRot13.addTextChangedListener (new Retranslator (false));
	}

	private void wireCopyButtons ()
	{
		this.layoutPlain.setEndIconOnClickListener (view -> this.copyToClipboard (this.txtPlain));
		this.layoutRot13.setEndIconOnClickListener (view -> this.copyToClipboard (this.txtRot13));

		// The fields stay editable, so a plain tap has to place the cursor;
		// long-press is the gesture that is free to mean "copy".
		this.txtPlain.setOnLongClickListener (view -> this.copyToClipboard (this.txtPlain));
		this.txtRot13.setOnLongClickListener (view -> this.copyToClipboard (this.txtRot13));
	}

	private boolean copyToClipboard (EditText field)
	{
		ClipboardManager clipboard = (ClipboardManager) getSystemService (Context.CLIPBOARD_SERVICE);
		if (clipboard == null)
			return false;

		clipboard.setPrimaryClip (ClipData.newPlainText (getString (R.string.clipboard_label), field.getText ().toString ()));

		// Android 13 and up show their own clipboard confirmation; ours would be
		// a second, redundant popup on top of it.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
			Snackbar.make (findViewById (android.R.id.content), R.string.copied, Snackbar.LENGTH_SHORT).show ();

		return true;
	}

	private void togglePanel ()
	{
		this.panelMode.setVisibility (this.panelMode.getVisibility () == View.VISIBLE ? View.GONE : View.VISIBLE);
	}

	private void setMode (CipherMode next)
	{
		if (next == null || next == this.mode)
			return;

		this.mode = next;
		this.syncModeViews ();
		this.retranslate ();
	}

	private void setShift (int next)
	{
		if (next == this.shift)
			return;

		this.shift = next;
		this.syncModeViews ();
		this.retranslate ();
	}

	/** Pushes the current mode and shift out to the controls that display them. */
	private void syncModeViews ()
	{
		this.btnModeToggle.setText (this.mode.labelRes ());
		this.lblShift.setText (getString (R.string.shift_value, this.shift));

		int shiftVisibility = this.mode.usesShift () ? View.VISIBLE : View.GONE;
		this.lblShift.setVisibility (shiftVisibility);
		this.sliderShift.setVisibility (shiftVisibility);
	}

	/** Recomputes the derived field from whichever one the user last typed in. */
	private void retranslate ()
	{
		this.isUpdating = true;
		try
		{
			if (this.lastEditedPlain)
				this.txtRot13.setText (this.mode.encode (this.txtPlain.getText ().toString (), this.shift));
			else
				this.txtPlain.setText (this.mode.decode (this.txtRot13.getText ().toString (), this.shift));
		}
		finally
		{
			this.isUpdating = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ().inflate (R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		int id = item.getItemId ();

		if (id == R.id.action_about)
		{
			Intent intent = new Intent (this, About.class);
			this.startActivity (intent);

			return true;
		}

		return super.onOptionsItemSelected (item);
	}

	/**
	 * Plain ROT13, kept as this class's long-standing entry point.
	 *
	 * @deprecated use {@link Ciphers#rot13(String)}, or
	 *             {@link CipherMode#ROT13}, directly.
	 */
	@Deprecated
	public static String rotate (String str)
	{
		return Ciphers.rot13 (str);
	}

	/**
	 * Mirrors one field into the other. Both directions share this class; the
	 * only difference is which way {@link CipherMode} is asked to translate.
	 */
	private final class Retranslator implements TextWatcher
	{
		private final boolean fromPlain;

		private Retranslator (boolean fromPlain)
		{
			this.fromPlain = fromPlain;
		}

		@Override
		public void beforeTextChanged (CharSequence s, int start, int count, int after)
		{
			if (MainActivity.this.isUpdating)
				return;

			MainActivity.this.txtPlain.setError (null);
			MainActivity.this.txtRot13.setError (null);
		}

		@Override
		public void onTextChanged (CharSequence s, int start, int before, int count)
		{
		}

		@Override
		public void afterTextChanged (Editable s)
		{
			// The write below retriggers the other field's watcher; without this
			// guard the two would bounce off each other indefinitely.
			if (MainActivity.this.isUpdating)
				return;

			MainActivity.this.isUpdating = true;
			try
			{
				MainActivity.this.lastEditedPlain = this.fromPlain;

				String input = s.toString ();
				if (this.fromPlain)
					MainActivity.this.txtRot13.setText (
							MainActivity.this.mode.encode (input, MainActivity.this.shift));
				else
					MainActivity.this.txtPlain.setText (
							MainActivity.this.mode.decode (input, MainActivity.this.shift));
			}
			finally
			{
				MainActivity.this.isUpdating = false;
			}
		}
	}
}
