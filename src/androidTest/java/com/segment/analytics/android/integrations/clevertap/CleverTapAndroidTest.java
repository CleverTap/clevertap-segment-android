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

    public void testTrack() {
        TrackPayload trackPayload = new TrackPayloadBuilder().event("myEvent").build();
        integration.track(trackPayload);
        assertNotNull(clevertap.event.getDetails("myEvent"));
    }
}


