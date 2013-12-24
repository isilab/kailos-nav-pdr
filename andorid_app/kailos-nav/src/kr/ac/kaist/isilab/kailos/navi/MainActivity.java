package kr.ac.kaist.isilab.kailos.navi;

import java.util.Timer;
import java.util.TimerTask;

import android.support.v4.app.Fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import kr.ac.kaist.isilab.kailos.drawmenu.NsMenuAdapter;
import kr.ac.kaist.isilab.kailos.drawmenu.NsMenuItemModel;

public class MainActivity extends FragmentActivity {
	// Member variables...
	public static Context m_MainActivityContext;
	public static Resources m_Resources;
	
	private boolean m_bQuitting;
	private TimerTask m_TimerTask;
	private Timer m_Timer;
	
	// Service interface (AIDL)...
	private ServiceConnection m_KAILOSServiceConnection;  	
	private ServiceConnection m_KAILOSFusedLocationServiceConnection;
	
	// For drawer..
	private ListView m_DrawerList;
	private DrawerLayout m_Drawer;
	private CustomActionBarDrawerToggle m_DrawerToggle;

	// Constructor...
	public MainActivity() {
		m_bQuitting = false;
		m_Timer = null;
		m_TimerTask = null;
		
		// Prepare service binding connection...
		m_KAILOSServiceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder service) {
				KAILOSService.m_KAILOSServiceRPC = IKAILOSService.Stub.asInterface(service);
			}
			
			public void onServiceDisconnected(ComponentName name) {
				KAILOSService.m_KAILOSServiceRPC = null;
			}			
		};
		
		m_KAILOSFusedLocationServiceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder service) {
				KAILOSFusedLocationService.m_KAILOSFusedLocationServiceRPC = IKAILOSFusedLocationService.Stub.asInterface(service);
			}

			public void onServiceDisconnected(ComponentName name) {
				KAILOSFusedLocationService.m_KAILOSFusedLocationServiceRPC = null;
			}			
		};
	}

	// Member functions...   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
		
		m_MainActivityContext = this;
		m_Resources = getResources();
		
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		// Settings..
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				
		// Start and bind services...
		Intent intentFusedLocationService = new Intent(IKAILOSFusedLocationService.class.getName());
		
		startService(intentFusedLocationService);
		bindService(intentFusedLocationService, m_KAILOSFusedLocationServiceConnection, BIND_AUTO_CREATE);

		Intent intentBaseService = new Intent(IKAILOSService.class.getName());
		
		startService(intentBaseService);
		bindService(intentBaseService, m_KAILOSServiceConnection, BIND_AUTO_CREATE);
		
		
		// Getting References...
		m_Drawer = (DrawerLayout)findViewById(R.id.drawer_layout);		
		m_DrawerList = (ListView)findViewById(R.id.left_drawer);
		
		// Add menu items...
		addDrawerMenuItems();		

		// Create a custom toggle button...
		m_DrawerToggle = new CustomActionBarDrawerToggle(this, m_Drawer);
		m_Drawer.setDrawerListener(m_DrawerToggle);
				
		// Disable buggy IPv6...
		System.setProperty("http.keepAlive", "false");
		System.setProperty("java.net.preferIPv4Stack" , "true");

	

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		Fragment newFragment = new GoogleMapFragment();
		
		transaction.replace(R.id.replaceable_fragment, newFragment);
		transaction.commit();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);		
		// Sync the toggle state after onRestoreInstanceState has occurred.
		m_DrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		m_DrawerToggle.onConfigurationChanged(newConfig);
	}
	
	public void addDrawerMenuItems() {
		NsMenuAdapter m_Adapter = new NsMenuAdapter(this);
		
		String menuItems[] = getResources().getStringArray(R.array.ns_menu);
		String menuItemsIcon[] = getResources().getStringArray(R.array.ns_menu_icon);

		int res = 0;
		for(String item : menuItems) {
			int id_title = getResources().getIdentifier(item, "string", this.getPackageName());
			int id_icon = getResources().getIdentifier(menuItemsIcon[res], "drawable", this.getPackageName());

			if(id_title == 0 || id_icon == 0 )
				continue;
			
			NsMenuItemModel mItem = new NsMenuItemModel(id_title, id_icon);
			m_Adapter.addItem(mItem);
			res++;
		}
		
		if(m_DrawerList != null)
			m_DrawerList.setAdapter(m_Adapter);
		 
		m_DrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}
	
	@Override
    protected void onResume() {
		super.onResume();
    }
	
	@Override
    protected void onPause() {
		super.onPause();
	}

	@Override
    protected void onStop() {
		super.onStop();
	}

	@Override
    protected void onDestroy() {
		super.onDestroy();
		
		if( m_Timer != null ) 
			m_Timer.cancel();
		
		stopService(new Intent(this, KAILOSService.class));
		unbindService(m_KAILOSServiceConnection);
		
		stopService(new Intent(this, KAILOSFusedLocationService.class));
		unbindService(m_KAILOSFusedLocationServiceConnection);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
    }
	
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (m_DrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle your other action bar items...
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// On menu key, toggle drawer..
		if( keyCode == KeyEvent.KEYCODE_MENU ) {
			boolean drawerOpen = m_Drawer.isDrawerOpen(m_DrawerList);
			
			if( drawerOpen ) {
				m_Drawer.closeDrawers();
			} else {
				m_Drawer.openDrawer(m_DrawerList);
			}		
		} else if( keyCode == KeyEvent.KEYCODE_BACK ) {
			if( m_bQuitting ) {
				m_Timer.cancel();
				return super.onKeyDown(keyCode, event);
			} else {
				Toast.makeText(MainActivity.m_MainActivityContext, "Press back again to exit!", Toast.LENGTH_SHORT).show();
				
				m_bQuitting = true;				
				m_TimerTask = new TimerTask() {
		            @Override
		            public void run() {
		            	m_Timer.cancel();
		            	m_bQuitting = false;
		            }
		        };
		         
		        m_Timer = new Timer();
		        m_Timer.schedule(m_TimerTask, 2000);
			}
			return false;				
		}
		
		return super.onKeyDown(keyCode, event);		
	}
	
	private class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {
		public CustomActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout){
			super(activity, drawerLayout, R.drawable.ic_drawer, R.string.open_drawer, R.string.close_drawer);
		}

		@Override
		public void onDrawerClosed(View view) {
			invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
		}
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// Highlight the selected item, update the title, and close the drawer
			// update selected item and title, then close the drawer			
	        m_DrawerList.setItemChecked(position, true);
	        
	        Intent intent = null;
	        
	        // Handling...
	        switch( position ) {
		        case 0:  // Search POI...
		        	intent = new Intent(MainActivity.this, POISearchActivity.class);
		            startActivity(intent);
		            break;
		        case 1: // Settings...
		        	intent = new Intent(MainActivity.this, SettingsActivity.class);
		            startActivity(intent);		        	
		        	break;
		        default:
		        	Toast.makeText(MainActivity.this, "IN PROGRESS! :)", Toast.LENGTH_SHORT).show();
		        	break;	        	
	        }
	        
	        m_Drawer.closeDrawer(m_DrawerList);			
		}		
	}	
}
