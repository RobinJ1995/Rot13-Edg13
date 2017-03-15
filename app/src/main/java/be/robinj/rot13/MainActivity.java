package be.robinj.rot13;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId ();

	    if (id == R.id.action_about)
	    {
		    Intent intent = new Intent (this, About.class);
		    this.startActivity (intent);

		    return true;
	    }

        return super.onOptionsItemSelected(item);
    }

	public static String rotate (String str)
	{
		StringBuilder rotated = new StringBuilder ();
		for (int i = 0; i < str.length (); i++)
		{
			char c = str.charAt (i);
			if (c >= 'a' && c <= 'm')
				c += 13;
			else if (c >= 'A' && c <= 'M')
				c += 13;
			else if (c >= 'n' && c <= 'z')
				c -= 13;
			else if (c >= 'N' && c <= 'Z')
				c -= 13;

			rotated.append (c);
		}

		return rotated.toString ();
	}

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

	        final TextView txtPlain = (TextView) rootView.findViewById (R.id.txtPlain);
	        final TextView txtRot13 = (TextView) rootView.findViewById (R.id.txtRot13);

	        txtPlain.addTextChangedListener
	        (
			        new TextWatcher()
		        {
			        @Override
			        public void beforeTextChanged (CharSequence charSequence, int i, int i2, int i3)
			        {
				        txtPlain.setError (null);
				        txtRot13.setError (null);
			        }

			        @Override
			        public void onTextChanged (CharSequence charSequence, int i, int i2, int i3)
			        {
				        if (txtPlain.hasFocus ())
				        {
					        String encoded = MainActivity.rotate(charSequence.toString());
					        txtRot13.setText(encoded);
				        }
			        }

			        @Override
			        public void afterTextChanged (Editable editable)
			        {
			        }
		        }
	        );

	        txtRot13.addTextChangedListener
	        (
		        new TextWatcher ()
		        {
			        @Override
			        public void beforeTextChanged (CharSequence charSequence, int i, int i2, int i3)
			        {
				        txtPlain.setError (null);
				        txtRot13.setError (null);
			        }

			        @Override
			        public void onTextChanged (CharSequence charSequence, int i, int i2, int i3)
			        {
				        if (txtRot13.hasFocus ())
				        {
					        String decoded = MainActivity.rotate(charSequence.toString());
					        txtPlain.setText(decoded);
				        }
			        }

			        @Override
			        public void afterTextChanged (Editable editable)
			        {
			        }
		        }
	        );

	        return rootView;
        }
    }

}
