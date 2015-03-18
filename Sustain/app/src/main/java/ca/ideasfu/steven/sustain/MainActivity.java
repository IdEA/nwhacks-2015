package ca.ideasfu.steven.sustain;

import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import de.keyboardsurfer.android.widget.crouton.Crouton;


public class MainActivity extends ActionBarActivity {
	public static final int DRAWER_DISPENSER_ID = 10;
	public static final String DRAWER_DISPENSER_TAG = "TAG1";
	public static final int DRAWER_USER_ID = 20;
	public static final String DRAWER_USER_TAG = "TAG2";

	private Drawer.Result leftDrawer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hideSystemUI(getWindow().getDecorView());
		setContentView(R.layout.activity_main);
		keepScreenOn();
		switchToIdleFragment(getSupportFragmentManager().beginTransaction());

		leftDrawer = new Drawer()
				.withActivity(this)
				.withDrawerWidthDp(255)
				.withDrawerGravity(Gravity.LEFT)
				.addDrawerItems(
						new PrimaryDrawerItem().withIdentifier(DRAWER_DISPENSER_ID).withName("Dispenser"),
						new DividerDrawerItem(),
						new SecondaryDrawerItem().withIdentifier(DRAWER_USER_ID).withName("User")
				)
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
						FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
						switch (drawerItem.getIdentifier()) {
							case DRAWER_DISPENSER_ID: {
								switchToIdleFragment(fragTrans);
								break;
							}
							case DRAWER_USER_ID: {
								switchToUserFragment(fragTrans);
								break;
							}
						}
					}
				})
				.build();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		Crouton.cancelAllCroutons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		menu.findItem(R.id.action_about).setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_info_circle).color(Color.WHITE).actionBarSize());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_about) {
			new Libs.Builder()
					.withFields(R.string.class.getFields())
					.withAboutIconShown(true)
					.withAboutVersionShown(true)
					.withAboutDescription("The sustainability app")
					.withActivityTitle("About!")
							//.withActivityTheme(R.style.MaterialDrawerTheme_ActionBar)
					.withLibraries("rxJava", "rxAndroid")
					.start(this);
			return true;
		}
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onWindowFocusChanged(boolean f) {
		if(f) {
			hideSystemUI(getWindow().getDecorView());
		}
	}

	public void switchToPullingFragment() {
		PullingFragment fragment = new PullingFragment();
		getSupportFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.fragment_container, fragment)
				.commit();
	}
	public void switchToIdleFragment() {
		IdleFragment fragment = new IdleFragment();
		getSupportFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.fragment_container, fragment)
				.commit();
	}

	public void switchToIdleFragment(FragmentTransaction fragTrans) {
		final IdleFragment fragment = new IdleFragment();
		fragTrans
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.fragment_container, fragment, DRAWER_DISPENSER_TAG)
				.commit();
	}

	public void switchToUserFragment(FragmentTransaction fragTrans) {
		final PullingFragment fragment = new PullingFragment();
		fragTrans.replace(R.id.fragment_container, fragment, DRAWER_USER_TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();
	}

	// This snippet hides the system bars.
	private void hideSystemUI(View view) {
		// Set the IMMERSIVE flag.
		// Set the content to appear under the system bars so that the content
		// doesn't resize when the system bars hide and show.
		view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
						| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
						| View.SYSTEM_UI_FLAG_IMMERSIVE);
	}
	private void showSystemUI(View view) {
		view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}
	private void keepScreenOn() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	public interface FragmentInterface {
		void frag();
	}
}
