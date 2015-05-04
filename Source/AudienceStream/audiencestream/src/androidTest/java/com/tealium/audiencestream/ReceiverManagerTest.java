package com.tealium.audiencestream;


import android.app.Application;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

public class ReceiverManagerTest extends ApplicationTestCase<Application> {
    public ReceiverManagerTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.createApplication();
    }

    public void testRegister() throws Throwable {

        ReceiverManager rm = new ReceiverManager(createConfig());

        rm.register();

        Assert.assertTrue(rm.isRegistered());

        try {
            rm.register();
            Assert.fail();
        } catch (IllegalStateException ignored) {

        }
    }

    public void testDisable() throws Throwable {

        ReceiverManager rm = new ReceiverManager(createConfig());

        rm.register();
        rm.onDisable();

        Assert.assertFalse(rm.isRegistered());

        try {
            rm.onDisable();
            Assert.fail();
        } catch (IllegalStateException ignored) {

        }
    }

    private AudienceStream.Config createConfig() {
        return new AudienceStream.Config(
                this.getApplication(),
                "tealiummobile",
                "demo",
                "dev");
    }
}
