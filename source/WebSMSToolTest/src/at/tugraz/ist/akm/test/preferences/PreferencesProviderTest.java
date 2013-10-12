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

package at.tugraz.ist.akm.test.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import at.tugraz.ist.akm.preferences.OnSharedPreferenceChangeListenerValidator;
import at.tugraz.ist.akm.preferences.PreferencesProvider;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

public class PreferencesProviderTest extends AndroidTestCase implements
        OnSharedPreferenceChangeListener
{
    private SharedPreferences mSharedPreferences = null;
    private PreferencesProvider mConfig = null;
    private LogClient mLog = new LogClient(this);
    private int mExpectedOnValueChangedCallbacks = 0;
    private OnSharedPreferenceChangeListenerValidator mPreferenceValidator = null;


    public PreferencesProviderTest()
    {
        // super("at.tugraz.ist.akm", PreferencesActivity.class);
        super();
        TraceService.setSink(new ThrowingLogSink());
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        mConfig = new PreferencesProvider(getContext());
        mPreferenceValidator = new OnSharedPreferenceChangeListenerValidator(
                getContext().getResources());
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mSharedPreferences
                .registerOnSharedPreferenceChangeListener(mPreferenceValidator);

    }


    @Override
    protected void tearDown() throws Exception
    {
        mSharedPreferences
                .unregisterOnSharedPreferenceChangeListener(mPreferenceValidator);
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }


    public void testGetSetting_userName()
    {
        String outUserName = "testUserName";
        mConfig.setUserName(outUserName);

        String inUserName = mConfig.getUserName();
        assertEquals(outUserName, inUserName);
    }


    public void testGetSetting_userPassword()
    {
        String outPassword = "testPassword";
        mConfig.setPassword(outPassword);
        String inPassword = mConfig.getPassWord();
        assertEquals(outPassword, inPassword);
    }


    public void testGetSetting_serverPort()
    {
        String outPort = "8080";
        mConfig.setPort(outPort);

        String inPort = mConfig.getPort();
        assertEquals(outPort, inPort);
    }


    public void testGetSetting_serverProtocol()
    {
        String outProtocol = "http";
        mConfig.setProtocol(outProtocol);
        String inProtocol = mConfig.getProtocol();
        assertEquals(outProtocol, inProtocol);

        outProtocol = "https";
        mConfig.setProtocol(outProtocol);
        inProtocol = mConfig.getProtocol();
        assertEquals(outProtocol, inProtocol);
    }


    public void testGetSetting_serverProtocolWithErroneousValue()
    {
        String expectedInProtocol = "https";
        mExpectedOnValueChangedCallbacks = 2;
        mConfig.setProtocol("asdf");
        waitForOutstandingSharedPreferencesCallbacks();
        String inProtocol = mConfig.getProtocol();
        assertEquals(expectedInProtocol, inProtocol);
    }


    public void testUpdateSetting_noException()
    {
        mConfig.setUserName("franz");
        mConfig.setPassword("sepp");
        mConfig.setKeyStorePassword("oachkatzerlschwoaf");
        mConfig.setProtocol("https");
        mConfig.setPort("8888");
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key)
    {
        if (mExpectedOnValueChangedCallbacks > 0)
        {
            mLog.debug("got outstanding callback, decrementing from ["
                    + mExpectedOnValueChangedCallbacks + "] to ["
                    + (mExpectedOnValueChangedCallbacks - 1) + "]");
            mExpectedOnValueChangedCallbacks--;
        }
    }


    private void waitForOutstandingSharedPreferencesCallbacks()
    {
        mLog.debug("waiting for callback/s being finished ["
                + mExpectedOnValueChangedCallbacks + "]");
        while (mExpectedOnValueChangedCallbacks > 0)
            ;
    }

}
