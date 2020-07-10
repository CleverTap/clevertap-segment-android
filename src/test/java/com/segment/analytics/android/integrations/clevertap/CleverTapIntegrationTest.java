package com.segment.analytics.android.integrations.clevertap;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.clevertap.android.sdk.CleverTapAPI;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.AliasPayload;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.internal.Utils;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.segment.analytics.internal.Utils.isNullOrEmpty;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest({CleverTapAPI.class, Logger.class, Utils.class})

public class CleverTapIntegrationTest {
    @Mock
    public CleverTapAPI clevertap;
    @Mock
    public Application context;

    @Mock
    public Logger logger;
    @Mock
    public Analytics analytics;

    CleverTapIntegration integration;
    ValueMap mockSettings;

    @Before
    public void setUp() {
        mockStatic(CleverTapAPI.class);

        mockSettings = new ValueMap();
        mockSettings.putValue("clevertap_account_id", "acctID")
                .putValue("clevertap_account_token", "acctToken")
                .putValue("region", "reg");

        when(analytics.logger("CleverTap")).thenReturn(logger);
        when(analytics.getApplication()).thenReturn(context);

        try {
            when(CleverTapAPI.getDefaultInstance(context)).thenReturn(clevertap);
        } catch (Exception e) {
            // ignore
        }

        integration = (CleverTapIntegration) CleverTapIntegration.FACTORY.create(mockSettings, analytics);
    }

    @Test
    public void testFactoryCreateWhenAccountIdAndTokenNotNull() {

        CleverTapIntegration integrationExpected = new CleverTapIntegration(clevertap, logger);

        //verify method call during method execution flow
        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.changeCredentials(anyString(), anyString(), anyString());

        //verify actual and expected instance of CleverTapIntegration are same by fields
        assertThat(integration, samePropertyValuesAs(integrationExpected));

    }

    @Test
    public void testFactoryCreateWhenAccountIdOrTokenIsNull() {

        //test case 1 - when clevertap_account_id is null
        mockSettings.putValue("clevertap_account_id", null);
        mockSettings.putValue("clevertap_account_token", "acctToken");
        CleverTapIntegration integrationActual = (CleverTapIntegration) CleverTapIntegration.FACTORY.create(mockSettings, analytics);

        //verify that instance of CleverTapIntegration is null for test case 1
        assertNull(integrationActual);

        //test case 2 - when clevertap_account_token is null
        mockSettings.putValue("clevertap_account_id", "acctID");
        mockSettings.putValue("clevertap_account_token", null);
        CleverTapIntegration integrationActual1 = (CleverTapIntegration) CleverTapIntegration.FACTORY.create(mockSettings, analytics);

        //verify that instance of CleverTapIntegration is null for test case 2
        assertNull(integrationActual1);

        //test case 3 - when clevertap_account_token and clevertap_account_id is null
        mockSettings.putValue("clevertap_account_id", null);
        mockSettings.putValue("clevertap_account_token", null);
        CleverTapIntegration integrationActual2 = (CleverTapIntegration) CleverTapIntegration.FACTORY.create(mockSettings, analytics);

        //verify that instance of CleverTapIntegration is null for test case 3
        assertNull(integrationActual2);

    }

    @Test
    public void testFactoryKey() {
        assertEquals("CleverTap", CleverTapIntegration.FACTORY.key());
    }

    @Test
    public void testIdentifyWhenIdentifyPayloadIsNull() {

        IdentifyPayload mock = mock(IdentifyPayload.class);
        integration.identify(null);

        //verify that when payload is null then called method returns immediately without any interactions
        Mockito.verifyNoMoreInteractions(mock);

    }

    @Test
    public void testIdentifyWhenTraitsIsNull() {

        IdentifyPayload mock = mock(IdentifyPayload.class);

        integration.identify(mock);

        //verify that when traits is null then called method returns immediately without any interactions
        verify(mock).traits();
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test
    public void testIdentify() {

        Date date = new GregorianCalendar(1992, 1, 22).getTime();
        String userId = "1010";

        //test case 1 - without gender
        Traits traits = new Traits();
        traits.putPhone("1234567890");
        traits.putName("Piyush Kukadiya");
        traits.putEmail("piyush@clevertap.com");
        traits.putBirthday(date);
        traits.put("userId", userId);


        IdentifyPayload identifyPayload = new IdentifyPayload.Builder().traits(traits).userId(userId).build();
        integration.identify(identifyPayload);

        ValueMap expectedProfile = new ValueMap()
                .putValue("Phone", traits.phone())
                .putValue("Email", traits.email())
                .putValue("Name", traits.name())
                .putValue("DOB", Utils.toISO8601String(traits.birthday()))
                .putValue("userId", userId)
                .putValue("Identity", userId);

        //verify that onUserLogin() called on CleverTapAPI with expectedProfile
        verify(clevertap).onUserLogin(refEq(expectedProfile));

        //test case 2 - with gender male
        traits.putGender("male");

        IdentifyPayload identifyPayloadWithMale = new IdentifyPayload.Builder().traits(traits).userId(userId).build();
        integration.identify(identifyPayloadWithMale);

        expectedProfile.putValue("Gender", "M");// gender added by CleverTapIntegration
        expectedProfile.putValue("gender", "male");//gender added by segment

        verify(clevertap).onUserLogin(refEq(expectedProfile));

        //test case 3 - with gender female
        traits.putGender("f");

        IdentifyPayload identifyPayloadWithFemale = new IdentifyPayload.Builder().traits(traits).userId(userId).build();
        integration.identify(identifyPayloadWithFemale);

        expectedProfile.putValue("Gender", "F");// gender added by CleverTapIntegration
        expectedProfile.putValue("gender", "f");//gender added by segment

        verify(clevertap).onUserLogin(refEq(expectedProfile));
    }

    @Test
    public void testAliasWhenAliasPayloadIsNull() {

        AliasPayload mock = mock(AliasPayload.class);
        integration.alias(null);

        //verify that when payload is null then called method returns immediately without any interactions
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test
    public void testAlias() {

        String userId = "1010";

        AliasPayload aliasPayload = new AliasPayload.Builder().previousId("1234").userId(userId).build();
        integration.alias(aliasPayload);

        HashMap<String, Object> expectedProfile = new HashMap();
        expectedProfile.put("Identity", userId);

        //verify that pushProfile() called on CleverTapAPI with expectedProfile
        verify(clevertap).pushProfile(refEq(expectedProfile));

    }

    @Test
    public void testTrackWhenTrackPayloadIsNull() {

        TrackPayload mock = mock(TrackPayload.class);
        integration.track(null);

        //verify that when payload is null then called method returns immediately without any interactions
        Mockito.verifyNoMoreInteractions(mock);

    }

    @Test
    public void testTrackWhenTrackEventIsNull() {
        TrackPayload trackPayload = mock(TrackPayload.class);
        when(trackPayload.properties()).thenReturn(new Properties());
        try {
            integration.track(trackPayload);
        } catch (NullPointerException e) {
            //test case will fail if any null pointer is thrown
            fail("Should not have thrown null exception");
        }

        //verify that when event is null then called method returns immediately without any interactions with pushEvent()
        verify(clevertap, never()).pushEvent(anyString(), any(Properties.class));
    }

    @Test
    public void testTrack() {

        String userId = "1010";
        Properties properties = new Properties();
        properties.putOrderId("123456");

        //test case 1 - when event is "Order Completed"
        TrackPayload trackPayload = new TrackPayload.Builder().event("Order Completed")
                .userId(userId)
                .properties(properties)
                .build();
        integration.track(trackPayload);

        assertTrue(trackPayload.event().equals("Order Completed"));
        verify(clevertap, never()).pushEvent(anyString(), ArgumentMatchers.<String, Object>anyMap());

        //test case 2 - when event is not "Order Completed"
        TrackPayload trackPayload1 = new TrackPayload.Builder().event("Custom")
                .userId(userId)
                .properties(properties)
                .build();
        integration.track(trackPayload1);

        assertFalse(trackPayload1.event().equals("Order Completed"));
        verify(clevertap).pushEvent(anyString(), ArgumentMatchers.<String, Object>anyMap());
    }

    @Test
    public void testScreenWhenScreenPayloadIsNull() {

        ScreenPayload mock = mock(ScreenPayload.class);

        try {
            integration.screen(null);
        } catch (NullPointerException e) {
            fail("Should not have thrown null exception");
        }

        Mockito.verifyNoMoreInteractions(mock);

    }

    @Test
    public void testScreen() {

        String userId = "1010";
        Properties properties = new Properties();
        properties.putOrderId("123456");

        ScreenPayload screenPayload = new ScreenPayload.Builder()
                .name("s1")
                .userId(userId)
                .properties(properties)
                .build();

        integration.screen(screenPayload);

        verify(clevertap).recordScreen("s1");

    }

    @Test
    public void testHandleOrderCompleted() throws Exception {

        String userId = "1010";
        Properties properties = new Properties();
        properties.putOrderId("123456");
        properties.putTotal(1000);
        properties.putShipping(50);
        properties.putCoupon("Welcome Offer");
        properties.put("first time buy", true);

        Properties.Product p1 = new Properties.Product("123", "12", 100);
        p1.putName("batman");
        Properties.Product p2 = new Properties.Product("1234", "123", 150);
        p2.putName("hulk");

        properties.putProducts(p1, p2);


        TrackPayload trackPayload = new TrackPayload.Builder().event("Order Completed")
                .userId(userId)
                .properties(properties)
                .build();

        //test handleOrderCompleted private method
        Whitebox.invokeMethod(integration, "handleOrderCompleted", trackPayload);

        /**
         * verify pushChargedEvent called on clevertap with expected params
         */

        HashMap<String, Object> expectedDetails = new HashMap<>();
        ArrayList<HashMap<String, Object>> expectedItems = new ArrayList<>();

        expectedDetails.put("Amount", 1000d);
        expectedDetails.put("Charged ID", "123456");

        JSONObject propertiesJson = properties.toJsonObject();
        Iterator<?> keys = propertiesJson.keys();
        while (keys.hasNext()) {
            try {
                String key = (String) keys.next();
                if (key.equals("products")) continue;
                expectedDetails.put(key, propertiesJson.get(key));

            } catch (Throwable t) {
                // no-op
            }
        }

        List<Properties.Product> expectedProducts = properties.products();
        if (!isNullOrEmpty(expectedProducts)) {
            for (int i = 0; i < expectedProducts.size(); i++) {
                try {
                    Properties.Product expectedProduct = expectedProducts.get(i);
                    HashMap<String, Object> expectedItem = new HashMap<>();

                    if (expectedProduct.id() != null) {
                        expectedItem.put("id", expectedProduct.id());
                    }
                    if (expectedProduct.name() != null) {
                        expectedItem.put("name", expectedProduct.name());
                    }
                    if (expectedProduct.sku() != null) {
                        expectedItem.put("sku", expectedProduct.sku());
                    }
                    expectedItem.put("price", expectedProduct.price());

                    expectedItems.add(expectedItem);
                } catch (Throwable t) {
                }
            }
        }

        //verify that pushChargedEvent() called on CleverTapAPI with expectedDetails,expectedItems
        verify(clevertap).pushChargedEvent(expectedDetails, expectedItems);

    }

    @Test
    public void testOnActivityCreated() {
        Activity activity = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        Intent intent = mock(Intent.class);

        Bundle expectedIntentBundle = mock(Bundle.class);
        Uri expectedIntentUri = mock(Uri.class);

        when(activity.getIntent()).thenReturn(intent);
        when(intent.getExtras()).thenReturn(expectedIntentBundle);
        when(intent.getData()).thenReturn(expectedIntentUri);

        integration.onActivityCreated(activity, bundle);

        //verify that setAppForeground(true) called on CleverTapAPI
        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.setAppForeground(true);

        //verify that pushNotificationClickedEvent() called on CleverTapAPI with expectedIntentBundle
        verify(clevertap).pushNotificationClickedEvent(expectedIntentBundle);
        //verify that pushDeepLink() called on CleverTapAPI with expectedIntentUri
        verify(clevertap).pushDeepLink(expectedIntentUri);
    }

    @Test
    public void testOnActivityResumed() {
        Activity activity = mock(Activity.class);
        integration.onActivityResumed(activity);

        //verify that onActivityResumed() called on CleverTapAPI
        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.onActivityResumed(activity);
    }

    @Test
    public void testOnActivityPaused() {
        Activity activity = mock(Activity.class);
        integration.onActivityPaused(activity);

        //verify that onActivityPaused() called on CleverTapAPI
        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.onActivityPaused();
    }


}