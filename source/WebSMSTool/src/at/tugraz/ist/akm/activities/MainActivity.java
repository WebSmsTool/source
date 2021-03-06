/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.os.Messenger;
import android.preference.PreferenceManager;
// there are serious issues when using support.v7 if project is referencing to 
// the support.v7 android library project. we'll sick with v4 vor a long while
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.exceptional.UncaughtExceptionLogger;
import at.tugraz.ist.akm.secureRandom.PRNGFixes;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity
{
    private static final String LAST_ACTIVE_NAVIGATION_DRAWER_BUNDLE_KEY = "at.tugraz.ist.akm.LAST_ACTIVE_NAVIGATION_DRAWER_ITEM_KEY";

    private int mCurrentNavigationDrawerEntry = 0;
    private LogClient mLog = new LogClient(this);
    final String mServiceName = WebSMSToolService.class.getName();
    private String[] mDrawerEntryTitles = null;
    private String[] mDrawerIcons = null;
    private String[] mDrawerFragments = null;
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private String mDefaultAppPackage = "at.tugraz.ist.akm";
    private String mDefaultSystemPackage = "android";
    private Fragment mCurrentFragment = null;


    public MainActivity()
    {
        mLog.debug("constructing " + getClass().getSimpleName());
        PRNGFixes.apply();
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        mLog.debug("brought activity to front");
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        mLog.debug("user returned to activity");
    }


    @Override
    protected void onPause()
    {
        mLog.debug("activity goes to background");
        super.onStop();
    }


    @Override
    protected void onStop()
    {
        mLog.debug("activity no longer visible");
        super.onStop();
    }


    @Override
    protected void onDestroy()
    {
        mLog.debug("activity goes to Hades");
        invalidateDrawerList();
        mLog = null;
        super.onDestroy();
    }


    private SimpleAdapter newItemDrawerAdapter()
    {
        mDrawerEntryTitles = getResources().getStringArray(
                R.array.drawer_string_array);
        mDrawerIcons = getResources().getStringArray(R.array.drawer_icon_array);

        List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < mDrawerFragments.length; i++)
        {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("icon",
                    Integer.toString(getDrawableIdentifier(mDrawerIcons[i])));
            map.put("title", mDrawerEntryTitles[i]);
            data.add(map);
        }

        String[] fromMapping = { "icon", "title" };
        int[] toMapping = { R.id.drawer_item_icon, R.id.drawer_item_text };

        return new SimpleAdapter(getBaseContext(), data,
                R.layout.navigation_drawer_list_entry, fromMapping, toMapping);
    }


    private int getDrawableIdentifier(String drawable)
    {
        int id = getResources().getIdentifier(drawable, "drawable",
                mDefaultAppPackage);

        if (id == 0)
        {
            id = getResources().getIdentifier(drawable, "drawable",
                    mDefaultSystemPackage);
        }
        return id;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mLog.debug(MainActivity.class.getSimpleName() + " on create");

        fixAndroidBugIssued6641();

        if (null != savedInstanceState)
        {
            restoreCurrentNavigationDrawerEntry(savedInstanceState);
        }

        setContentView(R.layout.navigation_drawer_list);
        mDrawerFragments = getResources().getStringArray(
                R.array.drawer_fragment_array);
        mDrawerEntryTitles = getResources().getStringArray(
                R.array.drawer_string_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer_left_drawer);

        mDrawerList.setAdapter(newItemDrawerAdapter());

        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    final int pos, long id)
            {
                mCurrentNavigationDrawerEntry = pos;
                mDrawerLayout
                        .setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                            @Override
                            public void onDrawerClosed(View drawerView)
                            {
                                super.onDrawerClosed(drawerView);

                                MainActivity.this
                                        .fragmentTransaction(mDrawerFragments[pos]);
                            }
                        });
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        setUpDrawerToggle();
        fragmentTransaction(mDrawerFragments[mCurrentNavigationDrawerEntry]);
        mDrawerList.setItemChecked(mCurrentNavigationDrawerEntry, true);
    }


    private void invalidateDrawerList()
    {
        mDrawerEntryTitles = null;
        mDrawerFragments = null;
        mDrawerLayout.setDrawerListener(null);
        mDrawerLayout = null;
        mDrawerIcons = null;
        mDrawerList.setAdapter(null);
        mDrawerList.setOnItemClickListener(null);
        mDrawerList = null;
        mDrawerToggle = null;
    }


    private void restoreCurrentNavigationDrawerEntry(Bundle savedInstanceState)
    {
        mCurrentNavigationDrawerEntry = savedInstanceState
                .getInt(LAST_ACTIVE_NAVIGATION_DRAWER_BUNDLE_KEY);
    }


    private void storeCurrentNavigationDrawerEntry(Bundle aBundle)
    {
        aBundle.putInt(LAST_ACTIVE_NAVIGATION_DRAWER_BUNDLE_KEY,
                mCurrentNavigationDrawerEntry);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        storeCurrentNavigationDrawerEntry(outState);
        super.onSaveInstanceState(outState);
    }


    private void fragmentTransaction(String fragmentTag)
    {
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        mCurrentFragment = Fragment.instantiate(MainActivity.this, fragmentTag);
        transaction.replace(R.id.navigation_drawer_content_frame,
                mCurrentFragment, fragmentTag);
        transaction.commit();
    }


    @Override
    public void onBackPressed()
    {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
        {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (null == getFragmentManager().findFragmentByTag(
                mDrawerFragments[0]))
        {
            fragmentTransaction(mDrawerFragments[0]);
        } else
        {
            finish();
        }
    }


    private void setUpDrawerToggle()
    {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_navigation_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                invalidateOptionsMenu();
            }


            @Override
            public void onDrawerOpened(View drawerView)
            {
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run()
            {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
        {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else
        {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return false;
    }


    @Override
    public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        mLog.warning("on trim memory");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (Debug.isDebuggerConnected())
        {
            UncaughtExceptionLogger exLogger = new UncaughtExceptionLogger(mLog);
            exLogger.register();
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.default_actionbar, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }


    private void fixAndroidBugIssued6641()
    {
        // These lines are working around an android bug:
        // https://code.google.com/p/android/issues/detail?id=6641
        PreferenceManager.setDefaultValues(this, R.xml.preferences_list, false);
        setDefaultBooleanPreferenceValue(R.string.preferences_access_restriction_key);
        setDefaultBooleanPreferenceValue(R.string.preferences_protocol_checkbox_key);
    }


    private void setDefaultBooleanPreferenceValue(int resourceId)
    {
        String sharedPreferenceKey = getApplicationContext().getString(
                resourceId);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        prefs.edit()
                .putBoolean(sharedPreferenceKey,
                        prefs.getBoolean(sharedPreferenceKey, false)).commit();
    }


    public Messenger getStartServiceFragmentMessenger()
    {
        if (mCurrentFragment != null
                && mCurrentFragment instanceof StartServiceFragment)
        {
            return ((StartServiceFragment) mCurrentFragment).getMessenger();
        }
        return null;
    }

}
