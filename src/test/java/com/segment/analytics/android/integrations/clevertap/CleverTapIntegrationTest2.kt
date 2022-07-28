package com.segment.analytics.android.integrations.clevertap

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.clevertap.android.sdk.CleverTapAPI
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import com.segment.analytics.Traits
import com.segment.analytics.ValueMap
import com.segment.analytics.integrations.*
import com.segment.analytics.internal.Utils
import org.hamcrest.beans.SamePropertyValuesAs
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.*


@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class CleverTapIntegrationTest2 {
    private lateinit var context: Context
    private lateinit var analytics: Analytics

    private lateinit var valueMap: ValueMap
    private lateinit var ctIntegration: CleverTapIntegration

    private lateinit var clevertap:CleverTapAPI
    private lateinit var logger: Logger

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>().also { it ->
            val shadowContext = Shadows.shadowOf(it)
            shadowContext.grantPermissions(Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        analytics = Analytics.Builder(context,"writeKey").build() //Mockito.mock(Analytics::class.java)

        valueMap = ValueMap().also {
            it.putValue("clevertap_account_id", "acctID")
            it.putValue("clevertap_account_token", "acctToken")
            it.putValue("region", "reg")
        }
        logger = Logger("tag",Analytics.LogLevel.VERBOSE) //Mockito.mock(Logger::class.java)

        ctIntegration = CleverTapIntegration.FACTORY.create(valueMap, analytics) as CleverTapIntegration


        clevertap =CleverTapAPI.getDefaultInstance(context)!!

        //Mockito.`when`(CleverTapAPI.getDefaultInstance(context)).thenReturn(clevertap)
        //Mockito.`when`(analytics.logger("CleverTap")).thenReturn(logger)
       // Mockito.`when`(analytics.application).thenReturn(context as Application)


        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }


    @Test
    fun testFactoryKey() {
        Assert.assertEquals("CleverTap", CleverTapIntegration.FACTORY.key())
    }

    @Test
    fun testFactoryKey2() {
        Assert.assertEquals("CleverTap", CleverTapIntegration.FACTORY.key())
    }



    @Test
    fun testFactoryCreateWhenAccountIdOrTokenIsNull() {

        //test case 1 - when clevertap_account_id is null
        valueMap.putValue("clevertap_account_id", null)
        valueMap.putValue("clevertap_account_token", "acctToken")
        val integrationActual = CleverTapIntegration.FACTORY.create(valueMap, analytics) as? CleverTapIntegration

        //verify that instance of CleverTapIntegration is null for test case 1
        Assert.assertNull(integrationActual)

        //test case 2 - when clevertap_account_token is null
        valueMap.putValue("clevertap_account_id", "acctID")
        valueMap.putValue("clevertap_account_token", null)
        val integrationActual1 = CleverTapIntegration.FACTORY.create(valueMap, analytics) as? CleverTapIntegration

        //verify that instance of CleverTapIntegration is null for test case 2
        Assert.assertNull(integrationActual1)

        //test case 3 - when clevertap_account_token and clevertap_account_id is null
        valueMap.putValue("clevertap_account_id", null)
        valueMap.putValue("clevertap_account_token", null)
        val integrationActual2 = CleverTapIntegration.FACTORY.create(valueMap, analytics) as? CleverTapIntegration

        //verify that instance of CleverTapIntegration is null for test case 3
        Assert.assertNull(integrationActual2)
    }


    @Test
    fun testFactoryCreateWhenAccountIdAndTokenNotNull() {
        val integrationExpected = CleverTapIntegration(clevertap, logger)

        //verify method call during method execution flow
        //PowerMockito.verifyStatic(CleverTapAPI::class.java)
        //CleverTapAPI.changeCredentials(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())

        //verify actual and expected instance of CleverTapIntegration are same by fields
        Assert.assertThat(ctIntegration, SamePropertyValuesAs.samePropertyValuesAs(integrationExpected))
    }


    @Test
    fun testIdentifyWhenIdentifyPayloadIsNull() {
        val mock = Mockito.mock(IdentifyPayload::class.java)
        ctIntegration.identify(null)

        //verify that when payload is null then called method returns immediately without any interactions
        Mockito.verifyNoMoreInteractions(mock)
    }

    @Test
    fun testIdentifyWhenTraitsIsNull() {
        val mock = Mockito.mock(IdentifyPayload::class.java)
        ctIntegration.identify(mock)

        //verify that when traits is null then called method returns immediately without any interactions
        Mockito.verify(mock).traits()
        Mockito.verifyNoMoreInteractions(mock)
    }

    @Test
    fun testIdentify() {
        val date = GregorianCalendar(1992, 1, 22).time
        val userId = "1010"

        //test case 1 - without gender
        val traits = Traits()
        traits.putPhone("1234567890")
        traits.putName("Piyush Kukadiya")
        traits.putEmail("piyush@clevertap.com")
        traits.putBirthday(date)
        traits["userId"] = userId
        val identifyPayload = IdentifyPayload.Builder().traits(traits).userId(userId).build()
        ctIntegration.identify(identifyPayload)
        val expectedProfile = ValueMap()
            .putValue("Phone", traits.phone())
            .putValue("Email", traits.email())
            .putValue("Name", traits.name())
            .putValue("DOB", Utils.toISO8601String(traits.birthday()))
            .putValue("userId", userId)
            .putValue("Identity", userId)

        //verify that onUserLogin() called on CleverTapAPI with expectedProfile
        Mockito.verify<CleverTapAPI>(clevertap).onUserLogin(ArgumentMatchers.refEq(expectedProfile))

        //test case 2 - with gender male
        traits.putGender("male")
        val identifyPayloadWithMale = IdentifyPayload.Builder().traits(traits).userId(userId).build()
        ctIntegration.identify(identifyPayloadWithMale)
        expectedProfile.putValue("Gender", "M") // gender added by CleverTapIntegration
        expectedProfile.putValue("gender", "male") //gender added by segment
        Mockito.verify<CleverTapAPI>(clevertap).onUserLogin(ArgumentMatchers.refEq(expectedProfile))

        //test case 3 - with gender female
        traits.putGender("f")
        val identifyPayloadWithFemale = IdentifyPayload.Builder().traits(traits).userId(userId).build()
        ctIntegration.identify(identifyPayloadWithFemale)
        expectedProfile.putValue("Gender", "F") // gender added by CleverTapIntegration
        expectedProfile.putValue("gender", "f") //gender added by segment
        Mockito.verify<CleverTapAPI>(clevertap).onUserLogin(ArgumentMatchers.refEq(expectedProfile))
    }

    @Test
    fun testAliasWhenAliasPayloadIsNull() {
        val mock = Mockito.mock(AliasPayload::class.java)
        ctIntegration.alias(null)

        //verify that when payload is null then called method returns immediately without any interactions
        Mockito.verifyNoMoreInteractions(mock)
    }

    @Test
    fun testAlias() {
        val userId = "1010"
        val aliasPayload = AliasPayload.Builder().previousId("1234").userId(userId).build()
        ctIntegration.alias(aliasPayload)
        val expectedProfile: HashMap<String?, Any?> = HashMap<String?, Any?>()
        expectedProfile["Identity"] = userId

        //verify that pushProfile() called on CleverTapAPI with expectedProfile
        Mockito.verify<CleverTapAPI>(clevertap).pushProfile(ArgumentMatchers.refEq(expectedProfile))
    }

    @Test
    fun testTrackWhenTrackPayloadIsNull() {
        val mock = Mockito.mock(TrackPayload::class.java)
        ctIntegration.track(null)

        //verify that when payload is null then called method returns immediately without any interactions
        Mockito.verifyNoMoreInteractions(mock)
    }

    @Test
    fun testTrackWhenTrackEventIsNull() {
        val trackPayload = Mockito.mock(TrackPayload::class.java)
        Mockito.`when`(trackPayload.properties()).thenReturn(Properties())
        try {
            ctIntegration.track(trackPayload)
        }
        catch (e: NullPointerException) {
            //test case will fail if any null pointer is thrown
            Assert.fail("Should not have thrown null exception")
        }

        //verify that when event is null then called method returns immediately without any interactions with pushEvent()
        Mockito.verify<CleverTapAPI>(clevertap, Mockito.never()).pushEvent(ArgumentMatchers.anyString(), ArgumentMatchers.any(Properties::class.java))
    }

    @Test
    fun testTrack() {
        val userId = "1010"
        val properties = Properties()
        properties.putOrderId("123456")

        //test case 1 - when event is "Order Completed"
        val trackPayload = TrackPayload.Builder().event("Order Completed")
            .userId(userId)
            .properties(properties)
            .build()
        ctIntegration.track(trackPayload)
        Assert.assertTrue(trackPayload.event() == "Order Completed")
        Mockito.verify<CleverTapAPI>(clevertap, Mockito.never()).pushEvent(ArgumentMatchers.anyString(), ArgumentMatchers.anyMap())

        //test case 2 - when event is not "Order Completed"
        val trackPayload1 = TrackPayload.Builder().event("Custom")
            .userId(userId)
            .properties(properties)
            .build()
        ctIntegration.track(trackPayload1)
        Assert.assertFalse(trackPayload1.event() == "Order Completed")
        Mockito.verify<CleverTapAPI>(clevertap).pushEvent(ArgumentMatchers.anyString(), ArgumentMatchers.anyMap())
    }

    @Test
    fun testScreenWhenScreenPayloadIsNull() {
        val mock = Mockito.mock(ScreenPayload::class.java)
        try {
            ctIntegration.screen(null)
        }
        catch (e: NullPointerException) {
            Assert.fail("Should not have thrown null exception")
        }
        Mockito.verifyNoMoreInteractions(mock)
    }

    @Test
    fun testScreen() {
        val userId = "1010"
        val properties = Properties()
        properties.putOrderId("123456")
        val screenPayload = ScreenPayload.Builder()
            .name("s1")
            .userId(userId)
            .properties(properties)
            .build()
        ctIntegration.screen(screenPayload)
        Mockito.verify<CleverTapAPI>(clevertap).recordScreen("s1")
    }

    @Test
    @Throws(Exception::class)
    fun testHandleOrderCompleted() {
        val userId = "1010"
        val properties = Properties()
        properties.putOrderId("123456")
        properties.putTotal(1000.0)
        properties.putShipping(50.0)
        properties.putCoupon("Welcome Offer")
        properties["first time buy"] = true
        val p1 = Properties.Product("123", "12", 100.0)
        p1.putName("batman")
        val p2 = Properties.Product("1234", "123", 150.0)
        p2.putName("hulk")
        properties.putProducts(p1, p2)
        val trackPayload = TrackPayload.Builder().event("Order Completed")
            .userId(userId)
            .properties(properties)
            .build()

        //test handleOrderCompleted private method
        //Whitebox.invokeMethod<Any>(ctIntegration, "handleOrderCompleted", trackPayload)
        /**
         * verify pushChargedEvent called on clevertap with expected params
         */
        val expectedDetails = HashMap<String, Any>()
        val expectedItems = ArrayList<HashMap<String, Any>>()
        expectedDetails["Amount"] = 1000.0
        expectedDetails["Charged ID"] = "123456"
        val propertiesJson = properties.toJsonObject()
        val keys: Iterator<*> = propertiesJson.keys()
        while (keys.hasNext()) {
            try {
                val key = keys.next() as String
                if (key == "products") continue
                expectedDetails[key] = propertiesJson[key]
            }
            catch (t: Throwable) {
                // no-op
            }
        }
        val expectedProducts = properties.products()
        if (!Utils.isNullOrEmpty(expectedProducts)) {
            for (i in expectedProducts.indices) {
                try {
                    val expectedProduct = expectedProducts[i]
                    val expectedItem = HashMap<String, Any>()
                    if (expectedProduct!!.id() != null) {
                        expectedItem["id"] = expectedProduct.id()
                    }
                    if (expectedProduct.name() != null) {
                        expectedItem["name"] = expectedProduct.name()
                    }
                    if (expectedProduct.sku() != null) {
                        expectedItem["sku"] = expectedProduct.sku()
                    }
                    expectedItem["price"] = expectedProduct.price()
                    expectedItems.add(expectedItem)
                }
                catch (t: Throwable) {
                }
            }
        }

        //verify that pushChargedEvent() called on CleverTapAPI with expectedDetails,expectedItems
        Mockito.verify<CleverTapAPI>(clevertap).pushChargedEvent(expectedDetails, expectedItems)
    }

    @Test
    fun testOnActivityCreated() {
        val activity = Mockito.mock(Activity::class.java)
        val bundle = Mockito.mock(Bundle::class.java)
        val intent = Mockito.mock(Intent::class.java)
        val expectedIntentBundle = Mockito.mock(Bundle::class.java)
        val expectedIntentUri = Mockito.mock(Uri::class.java)
        Mockito.`when`(activity.intent).thenReturn(intent)
        Mockito.`when`(intent.extras).thenReturn(expectedIntentBundle)
        Mockito.`when`(intent.data).thenReturn(expectedIntentUri)
        ctIntegration.onActivityCreated(activity, bundle)

        //verify that setAppForeground(true) called on CleverTapAPI
        //Mockito.verifyStatic(CleverTapAPI::class.java)
        //CleverTapAPI.setAppForeground(true)

        //verify that pushNotificationClickedEvent() called on CleverTapAPI with expectedIntentBundle
        Mockito.verify<CleverTapAPI>(clevertap).pushNotificationClickedEvent(expectedIntentBundle)
        //verify that pushDeepLink() called on CleverTapAPI with expectedIntentUri
        Mockito.verify<CleverTapAPI>(clevertap).pushDeepLink(expectedIntentUri)
    }

    @Test
    fun testOnActivityResumed() {
        val activity = Mockito.mock(Activity::class.java)
        ctIntegration.onActivityResumed(activity)

        //verify that onActivityResumed() called on CleverTapAPI
        //PowerMockito.verifyStatic(CleverTapAPI::class.java)
        //CleverTapAPI.onActivityResumed(activity)
    }

    @Test
    fun testOnActivityPaused() {
        val activity = Mockito.mock(Activity::class.java)
        ctIntegration.onActivityPaused(activity)

        //verify that onActivityPaused() called on CleverTapAPI
        //PowerMockito.verifyStatic(CleverTapAPI::class.java)
        //CleverTapAPI.onActivityPaused()
    }


}

