package com.segment.analytics.android.integrations.clevertap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.exceptions.CleverTapMetaDataNotFoundException;
import com.clevertap.android.sdk.exceptions.CleverTapPermissionsNotSatisfied;
import com.segment.analytics.Analytics;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.Traits;
import com.segment.analytics.integrations.AliasPayload;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.internal.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CleverTapIntegration extends Integration<CleverTapAPI> {
    private final CleverTapAPI cl;
    private static final String CLEVERTAP_KEY = "CleverTap";
    private static final String ACCOUNT_ID_KEY = "clevertap_account_id";
    private static final String ACCOUNT_TOKEN_KEY = "clevertap_account_token";

    private static final Set<String> MALE_TOKENS = new HashSet<String>(Arrays.asList("M",
            "MALE"));
    private static final Set<String> FEMALE_TOKENS = new HashSet<String>(Arrays.asList("F",
            "FEMALE"));

    static final Map<String, String> MAP_KNOWN_PROFILE_FIELDS;

    static {
        Map<String, String> knownFieldsMap = new LinkedHashMap<>();
        knownFieldsMap.put("phone", "Phone");
        knownFieldsMap.put("name", "Name");
        knownFieldsMap.put("email", "Email");
        MAP_KNOWN_PROFILE_FIELDS = Collections.unmodifiableMap(knownFieldsMap);
    }

    public static final Factory FACTORY = new Factory() {
        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {
            final CleverTapAPI cl;
            Logger logger = analytics.logger(CLEVERTAP_KEY);
            try {
                String accountID = settings.getString(ACCOUNT_ID_KEY);
                String accountToken = settings.getString(ACCOUNT_TOKEN_KEY);
                if (Utils.isNullOrEmpty(accountID) || Utils.isNullOrEmpty(accountToken)) {
                    logger.info("CleverTap+Segment integration attempt to initialize without account id or account token.");
                    return null;
                }

                CleverTapAPI.changeCredentials(accountID, accountToken);
                cl = CleverTapAPI.getInstance(analytics.getApplication());
                logger.info("Configured CleverTap+Segment integration and initialized CleverTap.");
            } catch (CleverTapMetaDataNotFoundException | CleverTapPermissionsNotSatisfied e) {
                logger.error(e, "Cannot initialize the CleverTap SDK");
                return null;
            }

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
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        super.onActivityCreated(activity, savedInstanceState);

        if (cl == null) return;

        CleverTapAPI.setAppForeground(true);
        try {
            cl.event.pushNotificationEvent(activity.getIntent().getExtras());
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
            cl.activityResumed(activity);
        } catch (Throwable t) {
            // Ignore
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        super.onActivityPaused(activity);

        if (cl == null) return;

        try {
            cl.activityPaused(activity);
        } catch (Throwable t) {
            // Ignore
        }
    }

    @Override
    public void identify(IdentifyPayload identify) {
        super.identify(identify);

        if (identify == null) {
            return;
        }

        Traits traits = identify.traits();
        if (traits == null) {
            return;
        }

        try {
            ValueMap profile = new ValueMap(Utils.transform(traits, MAP_KNOWN_PROFILE_FIELDS));

            //Date birthday = traits.birthday();
            Date birthday = null;
            if (birthday != null) {
                profile.put("DOB", birthday);
            }

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
            cl.profile.push(profile);
        } catch (Throwable t) {
            mLogger.error(t, "CleverTap: Error pushing profile");
            cl.event.pushError(t.getMessage(), 512);
        }
    }

    @Override
    public void track(TrackPayload track) {
        super.track(track);

        if (track == null) {
            return;
        }

        String event = track.event();
        if (event == null) {
            return;
        }

        Properties properties = track.properties();

        try {
            cl.event.push(event, properties);
        } catch (Throwable t) {
            mLogger.error(t, "CleverTap: Error pushing event");
            cl.event.pushError(t.getMessage(), 512);
        }
    }

    @Override
    public void alias(AliasPayload alias) {
        super.alias(alias);

        if (alias == null || Utils.isNullOrEmpty(alias.userId())) {
            return;
        }

        try {
            HashMap<String, Object> profile = new HashMap<>();
            profile.put("Identity", alias.userId());
            cl.profile.push(profile);
        } catch (Throwable t) {
            mLogger.error(t, "CleverTap: Error pushing profile");
            cl.event.pushError(t.getMessage(), 512);
        }
    }

    @Override
    public CleverTapAPI getUnderlyingInstance() {
        return cl;
    }
}
