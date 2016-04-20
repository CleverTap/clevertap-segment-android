package com.segment.analytics.android.integrations.clevertap;

import android.content.Context;
import android.test.AndroidTestCase;

import com.clevertap.android.sdk.CleverTapAPI;

import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.AliasPayload;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.test.AliasPayloadBuilder;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import static com.segment.analytics.Utils.createTraits;

import java.lang.Exception;


public class CleverTapAndroidTest extends AndroidTestCase {

    Context context;
    CleverTapIntegration integration;
    CleverTapAPI clevertap;

    public void setUp() throws Exception {
        super.setUp();

        context = getContext();
        assertNotNull(context);

        Analytics analytics = new Analytics.Builder(context, "foo" + System.currentTimeMillis()).build();
        ValueMap settings = new ValueMap().putValue("clevertap_account_id", "foo").putValue("clevertap_account_token", "bar");
        integration = (CleverTapIntegration) CleverTapIntegration.FACTORY.create(settings, analytics);
        assertNotNull(integration);

        try {
            clevertap = CleverTapAPI.getInstance(context);
            clevertap.enablePersonalization();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(clevertap);
    }

    public void testIdentify() {
        String testUserId = "testUser" + System.currentTimeMillis();
        Traits traits = createTraits(testUserId);
        String name = "FooName";
        traits.putName(name);
        traits.putGender("female");
        traits.putPhone("5555551234");
        traits.putEmail("foo@foo.com");
        IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();

        integration.identify(identifyPayload);
        assertEquals(testUserId, clevertap.profile.getProperty("Identity"));
        assertEquals(name, clevertap.profile.getProperty("Name"));
        assertEquals("F", clevertap.profile.getProperty("Gender"));
        assertEquals("5555551234", clevertap.profile.getProperty("Phone"));
        assertEquals("foo@foo.com", clevertap.profile.getProperty("Email"));
    }

    public void testAlias() {
        String userId = "654321abc";
        AliasPayload aliasPayload = new AliasPayloadBuilder().newId(userId).build();
        integration.alias(aliasPayload);
        assertEquals(userId, clevertap.profile.getProperty("Identity"));
    }

    public void testTrack() {
        TrackPayload trackPayload = new TrackPayloadBuilder().event("myEvent").build();
        integration.track(trackPayload);
        assertNotNull(clevertap.event.getDetails("myEvent"));
    }
}


