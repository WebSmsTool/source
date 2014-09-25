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

package at.tugraz.ist.akm.test.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.AboutActivity;
import at.tugraz.ist.akm.activities.MainActivity;
import at.tugraz.ist.akm.activities.preferences.PreferencesActivity;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

import com.robotium.solo.Solo;

public class ActionBarTest extends
        ActivityInstrumentationTestCase2<MainActivity>
{

    private LogClient mLog = null;
    private static final int WAIT_FOR_MONITOR_TIMEOUT = 5000;
    private static final int ACTIVITY_SHOW_TIMEOUT = 1000;


    public ActionBarTest()
    {
        super(MainActivity.class);
        TraceService.setSink(new ThrowingLogSink());
        mLog = new LogClient(ActionBarTest.class.getName());
    }


    public void testClickAndClosePreferencesActivty()
    {
        clickAndCloseActivtyStartingFromMainActivity(PreferencesActivity.class,
                R.id.actionbar_settings);
    }


    public void testClickAndCloseAboutActivty()
    {
        clickAndCloseActivtyStartingFromMainActivity(AboutActivity.class,
                R.id.actionbar_about);
    }


    private void clickAndCloseActivtyStartingFromMainActivity(
            Class<?> activity, int resourceId)
    {
        try
        {
            Instrumentation instrumentation = getInstrumentation();
            Solo clickedActivity = clickActivity(activity, resourceId);
            Thread.sleep(ACTIVITY_SHOW_TIMEOUT);
            clickedActivity.getCurrentActivity().finish();
            Solo mainSolo = new Solo(instrumentation, getActivity());
            mainSolo.assertCurrentActivity("wrong activity", MainActivity.class);
            Thread.sleep(ACTIVITY_SHOW_TIMEOUT);
        }
        catch (Exception e)
        {
            assertTrue(false);
        }
    }


    private Solo clickActivity(Class<?> activity, int resourceId)
    {
        Instrumentation instrumentation = getInstrumentation();

        Solo mainSolo = new Solo(instrumentation, getActivity());
        mainSolo.assertCurrentActivity("wrong activity", MainActivity.class);

        mainSolo.clickOnActionBarItem(resourceId);
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(
                activity.getName(), null, false);

        Activity activityToBeClicked = instrumentation
                .waitForMonitorWithTimeout(monitor, WAIT_FOR_MONITOR_TIMEOUT);
        assertNotNull("wrong activity", activityToBeClicked);
        Solo clickedSolo = new Solo(instrumentation, activityToBeClicked);
        clickedSolo.assertCurrentActivity("wrong activity", activity);
        return clickedSolo;
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        log(getName() + ".setUp()");
    }


    @Override
    protected void tearDown() throws Exception
    {
        Solo s = new Solo(getInstrumentation());
        s.finishOpenedActivities();
        log(getName() + ".tearDown()");
        super.tearDown();
    }


    protected void log(final String m)
    {
        mLog.info(m);
    }

}