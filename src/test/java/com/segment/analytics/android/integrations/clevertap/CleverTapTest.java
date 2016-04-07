package com.segment.analytics.android.integrations.clevertap;

import android.app.Activity;
import android.app.Application;
import android.app.usage.UsageEvents;
import android.os.Bundle;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.EventHandler;
import com.clevertap.android.sdk.ProfileHandler;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.test.AliasPayloadBuilder;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.ScreenPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;

import java.lang.Exception;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.json.JSONObject;

import static com.segment.analytics.Utils.createTraits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest({CleverTapAPI.class, EventHandler.class, ProfileHandler.class})

public class CleverTapTest {
    @Mock CleverTapAPI clevertap;
    //@Mock EventHandler clevertap.event;
    //@Mock ProfileHandler clevertap.profile;
    @Mock Application context;
    Logger logger;
    @Mock Analytics analytics;

    //CleverTapAPI clevertap;
    CleverTapIntegration integration;

    @Before
    public void setUp() {
        CleverTapAPI.changeCredentials("4W9-747-W54Z", "a4a-c04");
        initMocks(this);
        mockStatic(CleverTapAPI.class);
        logger = Logger.with(Analytics.LogLevel.DEBUG);
        when(analytics.logger("CleverTap")).thenReturn(logger);
        when(analytics.getApplication()).thenReturn(context);

        try {
            when(CleverTapAPI.getInstance(context)).thenReturn(clevertap);
        } catch (Exception e) {
          // ignore
        }

        integration = new CleverTapIntegration(clevertap, logger);
    }

    @Test
    public void factory() {
        ValueMap settings = new ValueMap().putValue("CleverTapAccountID", "4W9-747-W54Z").putValue("CleverTapAccountToken", "a4a-c04");
        CleverTapIntegration integration = (CleverTapIntegration) CleverTapIntegration.FACTORY.create(settings, analytics);
        /*
        verifyStatic();
        CleverTapAPI.changeCredentials("4W9-747-W54Z", "a4a-c04");
        verifyStatic();
        try {

            CleverTapAPI.getInstance(context);
        } catch (Exception e) {
            // ignore
        }
        */
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityCreate() {
        Activity activity = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        integration.onActivityCreated(activity, bundle);
        verifyStatic();
        CleverTapAPI.setAppForeground(true);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityStart() {
        Activity activity = mock(Activity.class);
        integration.onActivityStarted(activity);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityResume() {
        Activity activity = mock(Activity.class);
        integration.onActivityResumed(activity);
        verify(clevertap).activityResumed(activity);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityPause() {
        Activity activity = mock(Activity.class);
        integration.onActivityPaused(activity);
        verify(clevertap).activityPaused(activity);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityStop() {
        Activity activity = mock(Activity.class);
        integration.onActivityStopped(activity);
        verifyNoMoreCleverTapInteractions();
    }


    @Test
    public void activitySaveInstance() {
        Activity activity = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        integration.onActivitySaveInstanceState(activity, bundle);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityDestroy() {
        Activity activity = mock(Activity.class);
        integration.onActivityDestroyed(activity);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void alias() {
        integration.alias(new AliasPayloadBuilder().traits(createTraits("foo")).newId("bar").build());
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void identify() {
        Traits traits = new Traits();
        traits.putEmail("foo@foo.com");
        traits.putName("first last");
        traits.putPhone("5555551234");
        IdentifyPayload identifyPayload = new IdentifyPayloadBuilder().traits(traits).build();
        integration.identify(identifyPayload);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void event() {
        TrackPayload trackPayload = new TrackPayloadBuilder().event("myEvent").build();
        integration.track(trackPayload);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void screen() {
        integration.screen(new ScreenPayloadBuilder().name("foo").build());
        verifyNoMoreCleverTapInteractions();
    }

    private void verifyNoMoreCleverTapInteractions() {
        verifyNoMoreInteractions(CleverTapAPI.class);
        verifyNoMoreInteractions(clevertap);
    }
}