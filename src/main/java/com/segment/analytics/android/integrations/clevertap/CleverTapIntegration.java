package com.segment.analytics.android.integrations.clevertap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.clevertap.android.sdk.CleverTapAPI;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Properties.Product;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.AliasPayload;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.internal.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.segment.analytics.internal.Utils.isNullOrEmpty;

public class CleverTapIntegration extends Integration<CleverTapAPI> {
    private final CleverTapAPI cl;
    private static final String CLEVERTAP_KEY = "CleverTap";
    private static final String ACCOUNT_ID_KEY = "clevertap_account_id";
    private static final String ACCOUNT_TOKEN_KEY = "clevertap_account_token";
    private static final String ACCOUNT_REGION_KEY = "region";

    private static final Set<String> MALE_TOKENS = new HashSet<>(Arrays.asList("M",
            "MALE"));
    private static final Set<String> FEMALE_TOKENS = new HashSet<>(Arrays.asList("F",
            "FEMALE"));

    private static final Map<String, String> MAP_KNOWN_PROFILE_FIELDS;

    static {
        Map<String, String> knownFieldsMap = new LinkedHashMap<>();
        knownFieldsMap.put("phone", "Phone");
        knownFieldsMap.put("name", "Name");
        knownFieldsMap.put("email", "Email");
        knownFieldsMap.put("birthday", "DOB");
        MAP_KNOWN_PROFILE_FIELDS = Collections.unmodifiableMap(knownFieldsMap);
    }

    public static final Factory FACTORY = new Factory() {
        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {
            final CleverTapAPI cl;
            Logger logger = analytics.logger(CLEVERTAP_KEY);
            String accountID = settings.getString(ACCOUNT_ID_KEY);
            String accountToken = settings.getString(ACCOUNT_TOKEN_KEY);
            String region = settings.getString(ACCOUNT_REGION_KEY);
            if (region != null) {
                region = region.replace(".", "");
            }

            if (Utils.isNullOrEmpty(accountID) || Utils.isNullOrEmpty(accountToken)) {
                logger.info("CleverTap+Segment integration attempt to initialize without account id or account token.");
                return null;
            }

            CleverTapAPI.changeCredentials(accountID, accountToken, region);
            cl = CleverTapAPI.getDefaultInstance(analytics.getApplication());
            logger.info("Configured CleverTap+Segment integration and initialized CleverTap.");

            return new CleverTapIntegration(cl, logger);
        }

        @Override
        public String key() {
            return CLEVERTAP_KEY;
        }

    };

    private final Logger mLogger;

    public CleverTapIntegration(CleverTapAPI instance, Logger logger) {
        this.cl = instance;
        this.mLogger = logger;
        if (this.cl != null)
            this.cl.setLibrary("Segment-Android");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        super.onActivityCreated(activity, savedInstanceState);

        if (cl == null) return;

        CleverTapAPI.setAppForeground(true);
        try {
            cl.pushNotificationClickedEvent(activity.getIntent().getExtras());
        } catch (Throwable t) {
            // Ignore
        }

        try {
            Intent intent = activity.getIntent();
            Uri data = intent.getData();
            cl.pushDeepLink(data);
        } catch (Throwable t) {
            // Ignore
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        super.onActivityResumed(activity);

        if (cl == null) return;

        try {
            CleverTapAPI.onActivityResumed(activity);
        } catch (Throwable t) {
            // Ignore
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        super.onActivityPaused(activity);

        if (cl == null) return;

        try {
            CleverTapAPI.onActivityPaused();
        } catch (Throwable t) {
            // Ignore
        }
    }

    @Override
    public void identify(IdentifyPayload identify) {
        super.identify(identify);

        if(cl == null)
        {
            mLogger.debug("CleverTap Instance is null.");
            return;
        }

        if (identify == null) {
            return;
        }

        Traits traits = identify.traits();
        if (traits == null) {
            return;
        }

        try {
            ValueMap profile = new ValueMap(Utils.transform(traits, MAP_KNOWN_PROFILE_FIELDS));

            String userId = traits.userId();
            if (!Utils.isNullOrEmpty(userId)) {
                profile.put("Identity", userId);
            }

            String gender = traits.gender();
            if (!Utils.isNullOrEmpty(gender)) {
                if (MALE_TOKENS.contains(gender.toUpperCase())) {
                    profile.put("Gender", "M");
                } else if (FEMALE_TOKENS.contains(gender.toUpperCase())) {
                    profile.put("Gender", "F");
                }
            }
            cl.onUserLogin(profile);
        } catch (Throwable t) {
            mLogger.error(t, "CleverTap: Error pushing profile");
            cl.pushError(t.getMessage(), 512);
        }
    }

    @Override
    public void track(TrackPayload track) {
        super.track(track);

        if(cl == null)
        {
            mLogger.debug("CleverTap Instance is null.");
            return;
        }

        if (track == null) {
            return;
        }

        String event = track.event();
        if (event == null) {
            return;
        }

        if (event.equals("Order Completed")) {
            handleOrderCompleted(track);
            return;
        }

        Properties properties = track.properties();

        try {
            cl.pushEvent(event, properties);
        } catch (Throwable t) {
            mLogger.error(t, "CleverTap: Error pushing event");
            cl.pushError(t.getMessage(), 512);
        }
    }

    @Override
    public void alias(AliasPayload alias) {
        super.alias(alias);

        if(cl == null)
        {
            mLogger.debug("CleverTap Instance is null.");
            return;
        }

        if (alias == null || Utils.isNullOrEmpty(alias.userId())) {
            return;
        }

        try {
            HashMap<String, Object> profile = new HashMap<>();
            profile.put("Identity", alias.userId());
            cl.pushProfile(profile);
        } catch (Throwable t) {
            mLogger.error(t, "CleverTap: Error pushing profile");
            cl.pushError(t.getMessage(), 512);
        }
    }

    @Override
    public CleverTapAPI getUnderlyingInstance() {
        return cl;
    }

    private void handleOrderCompleted(TrackPayload track) {

        if(cl == null)
        {
            mLogger.debug("CleverTap Instance is null.");
            return;
        }

        if (!track.event().equals("Order Completed")) return;

        Properties properties = track.properties();

        HashMap<String, Object> details = new HashMap<>();
        ArrayList<HashMap<String, Object>> items = new ArrayList<>();

        details.put("Amount", properties.total());

        final String orderId = properties.orderId();
        if (orderId != null) {
            details.put("Charged ID", properties.orderId());
        }

        JSONObject propertiesJson = properties.toJsonObject();
        Iterator<?> keys = propertiesJson.keys();
        while (keys.hasNext()) {
            try {
                String key = (String) keys.next();
                if (key.equals("products")) continue;
                details.put(key, propertiesJson.get(key));

            } catch (Throwable t) {
                // no-op
            }
        }

        List<Product> products = properties.products();
        if (!isNullOrEmpty(products)) {
            for (int i = 0; i < products.size(); i++) {
                try {
                    Product product = products.get(i);
                    HashMap<String, Object> item = new HashMap<>();

                    if (product.id() != null) {
                        item.put("id", product.id());
                    }
                    if (product.name() != null) {
                        item.put("name", product.name());
                    }
                    if (product.sku() != null) {
                        item.put("sku", product.sku());
                    }
                    item.put("price", product.price());

                    items.add(item);
                } catch (Throwable t) {
                    mLogger.error(t, "CleverTap: Error handling Order Completed product");
                    cl.pushError("Error handling Order Completed product: " + t.getMessage(), 512);
                }
            }
        }

        try {
            cl.pushChargedEvent(details, items);
        } catch (Throwable t) {
            mLogger.error(t, "CleverTap: Error handling Order Completed");
            cl.pushError("Error handling Order Completed: " + t.getMessage(), 512);
        }
    }

    @Override
    public void screen(ScreenPayload screen) {
        super.screen(screen);

        if(cl == null)
        {
            mLogger.debug("CleverTap Instance is null.");
            return;
        }

        if (screen.name() == null)
            return;

        cl.recordScreen(screen.name());
    }

}
