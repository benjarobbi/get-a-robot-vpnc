package org.codeandroid.vpnc_frontend;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.codeandroid.vpnc_frontend.VPNCTest \
 * org.codeandroid.vpnc_frontend.tests/android.test.InstrumentationTestRunner
 */
public class VPNCTest extends ActivityInstrumentationTestCase<VPNC> {

    public VPNCTest() {
        super("org.codeandroid.vpnc_frontend", VPNC.class);
    }

}
