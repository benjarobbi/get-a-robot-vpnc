package org.codeandroid.wmealing.vpnc_frontend;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.codeandroid.wmealing.vpnc_frontend.MyActivityTest \
 * org.codeandroid.wmealing.vpnc_frontend.tests/android.test.InstrumentationTestRunner
 */
public class MyActivityTest extends ActivityInstrumentationTestCase<MyActivity> {

    public MyActivityTest() {
        super("org.codeandroid.wmealing.vpnc_frontend", MyActivity.class);
    }

}
