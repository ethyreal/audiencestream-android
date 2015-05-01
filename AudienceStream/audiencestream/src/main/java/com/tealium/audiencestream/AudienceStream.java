package com.tealium.audiencestream;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

import com.tealium.audiencestream.attribute.AudienceAttribute;
import com.tealium.audiencestream.attribute.BadgeAttribute;
import com.tealium.audiencestream.attribute.DateAttribute;
import com.tealium.audiencestream.attribute.FlagAttribute;
import com.tealium.audiencestream.attribute.MetricAttribute;
import com.tealium.audiencestream.attribute.PropertyAttribute;
import com.tealium.audiencestream.visitor.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class for this AudienceStream library.
 * <p/>
 * {@link com.tealium.audiencestream.AudienceStream#enable(com.tealium.audiencestream.AudienceStream.Config)}
 * must be called before using any of the other methods.
 */
public final class AudienceStream {

    private static final Collection<EventListener> eventListeners;
    private static String visitorId;
    private static volatile boolean isEnabled;
    private static volatile Profile cachedProfile;

    static {
        if (Constant.DEBUG) {
            Log.i(Constant.DEBUG_TAG, "============================================");
        }

        isEnabled = false;
        eventListeners = Collections.newSetFromMap(new ConcurrentHashMap<EventListener, Boolean>());
    }

    private AudienceStream() {
    }

    /**
     * Required to enable the AudienceStream library.
     * <p/>
     * It has various setters for optional configuration options which return instances of itself
     * for method chaining.
     */
    public static final class Config {

        private final Context context;
        private final SharedPreferences sharedPreferences;
        private final String accountName;
        private final String profileName;
        private final String environmentName;
        private final MPS mps;
        private final ConnectivityManager connectivityManager;

        private String overrideProfile;
        private boolean isHttpsEnabled = true;

        /**
         * @param context         the current context reference
         * @param accountName     the account name provided by Tealium
         * @param profileName     the profile for this project
         * @param environmentName the development environment for this configuration, conventionally: "dev", "qa", or "prod".
         * @throws java.lang.IllegalArgumentException when any of the parameters are null or any of the strings are empty.
         */
        public Config(Context context, String accountName, String profileName, String environmentName) {

            if (context == null ||
                    Util.isEmptyOrNull(accountName) ||
                    Util.isEmptyOrNull(profileName) ||
                    Util.isEmptyOrNull(environmentName)) {
                throw new IllegalArgumentException("context, accountName, profileName, and environmentName are all required.");
            }

            this.context = context.getApplicationContext();
            this.accountName = accountName;
            this.profileName = profileName;
            this.environmentName = environmentName;
            this.mps = new MPS(context);
            this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            this.sharedPreferences = this.context.getSharedPreferences(Constant.SP.NAME, 0);

            try {
                cachedProfile = Profile.fromJSON(this.sharedPreferences.getString(Constant.SP.KEY_PROFILE, null));
            } catch (JSONException e) {
                // Corrupted
                this.sharedPreferences.edit().remove(Constant.SP.KEY_PROFILE).apply();
            }

            final String vidKey = String.format(Locale.ROOT, "%s.vid", this.accountName);

            if (!this.sharedPreferences.contains(vidKey)) {
                // no visitor id established
                this.sharedPreferences.edit()
                        .putString(vidKey,
                                UUID.randomUUID()
                                        .toString()
                                        .toUpperCase(Locale.ROOT)
                                        .replace("-", ""))
                        .apply();
            }

            AudienceStream.visitorId = this.sharedPreferences.getString(vidKey, null);
        }

        /**
         * Determine whether networking scheme is HTTP or HTTPS.
         *
         * @param isHttpsEnabled use HTTPS when true, HTTP otherwise.
         * @return this instance for method chaining.
         */
        public Config setHttpsEnabled(boolean isHttpsEnabled) {
            this.isHttpsEnabled = isHttpsEnabled;
            return this;
        }

        /**
         * Set the volume of which this library prints logs in LogCat.
         * <p/>
         * Available volumes are:
         * <ul>
         * <li>{@link Log#VERBOSE}</li>
         * <li>{@link Log#DEBUG}</li>
         * <li>{@link Log#INFO}</li>
         * <li>{@link Log#WARN}</li>
         * <li>{@link Log#ERROR}</li>
         * <li>{@link Log#ASSERT}</li>
         * </ul>
         * <p/>
         * Any value provided other than these will be interpreted as "SILENT".
         * <p/>
         * The default is {@link Log#WARN}.
         *
         * @param logLevel the desired log-level (2-7) for varying verbosity, any other value for SILENT.
         * @return this instance for method chaining.
         */
        public Config setLogLevel(int logLevel) {
            Logger.setLogLevel(logLevel);
            return this;
        }

        /**
         * Use the provided profile instead of "main" for data layer enrichment.
         */
        public Config setOverrideProfile(String profileName) {
            if (profileName != null && profileName.length() == 0) {
                this.overrideProfile = null;
            } else {
                this.overrideProfile = profileName;
            }

            return this;
        }

        boolean isHttpsEnabled() {
            return isHttpsEnabled;
        }

        SharedPreferences getSharedPreferences() {
            return this.sharedPreferences;
        }

        Context getContext() {
            return this.context;
        }

        String getAccountName() {
            return this.accountName;
        }

        String getProfileName() {
            return this.profileName;
        }

        String getOverrideProfile() {
            return overrideProfile;
        }

        String getEnvironmentName() {
            return environmentName;
        }

        ConnectivityManager getConnectivityManager() {
            return this.connectivityManager;
        }

        MPS getMPS() {
            return this.mps;
        }

        /**
         * @return a human-readable description of the current configuration.
         */
        @Override
        public String toString() {

            return "AudienceStream Configuration : {" + Constant.NEW_LINE +
                    Constant.TAB + "account_name : " + this.accountName + ',' + Constant.NEW_LINE +
                    Constant.TAB + "profile_name : " + this.profileName + ',' + Constant.NEW_LINE +
                    Constant.TAB + "environment_name : " + this.environmentName + ',' + Constant.NEW_LINE +
                    Constant.TAB + "enrichment_profile : " + (this.overrideProfile == null ? "main" : this.overrideProfile) + ',' + Constant.NEW_LINE +
                    Constant.TAB + "log_level : " + Logger.getLogLevel() + ',' + Constant.NEW_LINE +
                    Constant.TAB + "is_https_enabled : " + this.isHttpsEnabled + ',' + Constant.NEW_LINE +
                    Constant.TAB + "mobile_publish_settings : " + this.mps.toString(Constant.TAB) + Constant.NEW_LINE +
                    '}';
        }
    }

    /**
     * Add an {@link java.util.EventListener} to receive profile update events.
     * <p/>
     * The EventListeners that receive events are:
     * <p/>
     * <ul>
     * <li>{@link com.tealium.audiencestream.AudienceStream.OnAudienceUpdateListener}</li>
     * <li>{@link com.tealium.audiencestream.AudienceStream.OnBadgeUpdateListener}</li>
     * <li>{@link com.tealium.audiencestream.AudienceStream.OnFlagUpdateListener}</li>
     * <li>{@link com.tealium.audiencestream.AudienceStream.OnDateUpdateListener}</li>
     * <li>{@link com.tealium.audiencestream.AudienceStream.OnMetricUpdateListener}</li>
     * <li>{@link com.tealium.audiencestream.AudienceStream.OnPropertyUpdateListener}</li>
     * <li>{@link com.tealium.audiencestream.AudienceStream.OnProfileUpdatedListener}</li>
     * <p/>
     * </ul>
     */
    public static Collection<EventListener> getEventListeners() {
        return eventListeners;
    }

    /**
     * @return Whether the AudienceStream library is enabled ({@link AudienceStream#enable(Config)}
     * has been called).
     */
    public static boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Initialize the library with the desired configuration.
     *
     * @param config a Config instance. If null or the library is already initialized, a warning
     *               will be logged in LogCat and the call will have no effect.
     */
    public static void enable(Config config) {

        if (config == null) {
            Logger.e(new IllegalArgumentException("config must not be null."));
            return;
        }

        if (!isEnabled) {
            isEnabled = true;
            EventBus.initialize(config);
        } else {
            Logger.w("AudienceStream.enable(Config) was called when already initialized.");
        }
    }

    /**
     * Disable the initialized library. If the library was not initialized this call has no effect.
     */
    public static void disable() {
        isEnabled = false;
        EventBus.disable();
    }

    /**
     * Send an event dispatch to AudienceStream. This method calls
     * {@link AudienceStream#send(String, Map)} with the first argument as "link" along with the
     * given data.
     *
     * @param data the data tied to this event. The values provided by the map are copied into
     *             {@link java.lang.String}s, {@link org.json.JSONArray}s, and {@link org.json.JSONObject}s
     *             depending on their original type.
     */
    public static void sendEvent(Map<String, ?> data) {
        send("link", data);
    }

    /**
     * Send a view dispatch to AudienceStream. This method calls
     * {@link AudienceStream#send(String, Map)} with the first argument as "view" along with the
     *
     * @param data the data tied to this event. The values provided by the map are copied into
     *             {@link java.lang.String}s, {@link org.json.JSONArray}s, and {@link org.json.JSONObject}s
     *             depending on their original type.
     */
    public static void sendView(Map<String, ?> data) {
        send("view", data);
    }

    private static void send(String callType, Map<String, ?> data) {
        final JSONObject copy = data == null ? new JSONObject() : Util.JSON.mapToJSONObject(data);

        try {
            if (callType != null && !copy.has(Key.CALL_TYPE)) {
                copy.put(Key.CALL_TYPE, callType);
            }

            if (!copy.has(Key.PAGE_TYPE) && "view".equals(callType)) {
                copy.put(Key.PAGE_TYPE, "mobile_view");
            } else if (!copy.has(Key.EVENT_NAME) && "link".equals(callType)) {
                copy.put(Key.EVENT_NAME, "mobile_link");
            }
        } catch (JSONException e) {
            // Should never happen.
            Logger.e(e);
        }
        EventBus.submit(Events.createPopulateDispatchEvent(copy));
        EventBus.submit(Events.createDispatchReadyEvent(copy, false));

    }

    /**
     * Send a custom dispatch to AudienceStream.
     *
     * @param data the data tied to this event. The values provided by the map are copied into
     *             {@link java.lang.String}s, {@link org.json.JSONArray}s, and {@link org.json.JSONObject}s
     *             depending on their original type.
     * @see {@link com.tealium.audiencestream.AudienceStream#sendEvent(java.util.Map)}.
     * @see {@link com.tealium.audiencestream.AudienceStream#sendView(java.util.Map)}.
     */
    public static void send(Map<String, ?> data) {
        send(null, data);
    }

    /**
     * Join an AudienceStream Trace with the given trace id.
     *
     * @param traceId Given trace id, typically a 5-digit numerical string.
     */
    public static void joinTrace(String traceId) {
        EventBus.submit(Events.createTraceUpdateEvent(traceId));
    }

    /**
     * Leave an active AudienceStream Trace if one is running.
     */
    public static void leaveTrace() {
        EventBus.submit(Events.createTraceUpdateEvent(null));
    }

    /**
     * @return the last fetched profile, it may be null.
     */
    public static Profile getCachedProfile() {
        return cachedProfile;
    }

    /**
     * @return the visitor id tied to this installation.
     */
    public static String getVisitorId() {
        return visitorId;
    }

    /**
     * Implementers of this class can subscribe to Audience updates by adding the listener to
     * {@link AudienceStream#getEventListeners()}.
     */
    public interface OnAudienceUpdateListener extends EventListener {
        /**
         * Indicate that an Audience has changed for this visitor.
         *
         * @param oldAudience former audience this visitor was a member of. If null, the newAudience
         *                    was added.
         * @param newAudience current audience this visitor is a member of. If null, the oldAudience
         *                    was removed.
         */
        void onAudienceUpdate(AudienceAttribute oldAudience, AudienceAttribute newAudience);
    }

    /**
     * Implementers of this class can subscribe to Badge updates by adding the listener to
     * {@link AudienceStream#getEventListeners()}
     */
    public interface OnBadgeUpdateListener extends EventListener {
        /**
         * Indicate that a Badge has changed for this visitor.
         *
         * @param oldBadge former badge this visitor held. If null, the newBadge
         *                 was added.
         * @param newBadge current badge this visitor holds. If null, the oldBadge
         *                 was removed.
         */
        void onBadgeUpdate(BadgeAttribute oldBadge, BadgeAttribute newBadge);
    }

    /**
     * Implementers of this class can subscribe to Flag updates by adding the listener to
     * {@link AudienceStream#getEventListeners()}
     */
    public interface OnFlagUpdateListener extends EventListener {
        /**
         * Indicate that a Flag has changed for this visitor.
         *
         * @param oldFlag former flag this visitor held with the old value. If null, the newFlag
         *                was added.
         * @param newFlag current flag this visitor holds with the current value. If null, the
         *                oldFlag was removed.
         */
        void onFlagUpdate(FlagAttribute oldFlag, FlagAttribute newFlag);
    }

    /**
     * Implementers of this class can subscribe to Date updates by adding the listener to
     * {@link AudienceStream#getEventListeners()}
     */
    public interface OnDateUpdateListener extends EventListener {
        /**
         * Indicate that a Date has changed for this visitor.
         *
         * @param oldDate former Date this visitor held. If null, the newDate
         *                was added.
         * @param newDate current Date this visitor holds.
         */
        void onDateUpdate(DateAttribute oldDate, DateAttribute newDate);
    }

    /**
     * Implementers of this class can subscribe to Metric updates by adding the listener to
     * {@link AudienceStream#getEventListeners()}
     */
    public interface OnMetricUpdateListener extends EventListener {
        /**
         * Indicate that a Metric has changed for this visitor.
         *
         * @param oldMetric former metric this visitor held with the old value. If null, the
         *                  newMetric was added.
         * @param newMetric current metric this visitor holds with the latest value.
         */
        void onMetricUpdate(MetricAttribute oldMetric, MetricAttribute newMetric);
    }

    /**
     * Implementers of this class can subscribe to Property (AKA Trait) updates by adding the listener to
     * {@link AudienceStream#getEventListeners()}
     */
    public interface OnPropertyUpdateListener extends EventListener {
        /**
         * Indicate that a Property (AKA Trait) has changed for this visitor.
         *
         * @param oldProperty former trait this visitor held with the old value. If null, the
         *                    newProperty was added.
         * @param newProperty current flag this visitor holds with the current value. If null, the
         *                    newProperty was removed.
         */
        void onPropertyUpdate(PropertyAttribute oldProperty, PropertyAttribute newProperty);
    }

    /**
     * Implementers of this class can subscribe to Profile updates by adding the listener to
     * {@link AudienceStream#getEventListeners()}
     */
    public interface OnProfileUpdatedListener extends EventListener {
        /**
         * Indicate that a Profile has changed for this visitor.
         *
         * @param oldProfile the old profile for this visitor. May be null if a profile has not
         *                   been fetched.
         * @param newProfile up-to-date profile this visitor.
         */
        void onProfileUpdated(Profile oldProfile, Profile newProfile);
    }

    // Exposed for testing
    static void setVisitorId(String newVisitorId) {
        if (Constant.DEBUG) {
            visitorId = newVisitorId;
        }
    }

    // Exposed for testing
    static void setCachedProfile(Profile profile) {
        cachedProfile = profile;
    }
}
