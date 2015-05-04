package com.tealium.audiencestream.visitor;

import android.os.Handler;
import android.os.Looper;

import com.tealium.audiencestream.AudienceStream;

import java.util.EventListener;

public final class Processor {
    private Processor() {

    }

    /**
     * Diffs the two profiles and notifies the listeners residing in
     * {@link com.tealium.audiencestream.AudienceStream#getEventListeners()} of relevant changes.
     *
     * @param oldProfile profile instance or null
     * @param newProfile profile instance or null, NOTE: null will never be provided by this library.
     */
    public static void process(
            Profile oldProfile,
            Profile newProfile) {

        final boolean isOldProfileNull = oldProfile == null;
        final boolean isNewProfileNull = newProfile == null;

        if (isOldProfileNull && isNewProfileNull) {
            return;
        }

        if (!isOldProfileNull && oldProfile.equals(newProfile)) {
            return;
        }

        Handler uiHandler = new Handler(Looper.getMainLooper());

        uiHandler.post(createProfileNotifier(oldProfile, newProfile));

        uiHandler.post(new AudienceDiffNotifier(
                isOldProfileNull ? null : oldProfile.getAudiences(),
                isNewProfileNull ? null : newProfile.getAudiences()));

        uiHandler.post(new BadgeDiffNotifier(
                isOldProfileNull ? null : oldProfile.getBadges(),
                isNewProfileNull ? null : newProfile.getBadges()));

        uiHandler.post(new DateDiffNotifier(
                isOldProfileNull ? null : oldProfile.getDates(),
                isNewProfileNull ? null : newProfile.getDates()));

        uiHandler.post(new FlagDiffNotifier(
                isOldProfileNull ? null : oldProfile.getFlags(),
                isNewProfileNull ? null : newProfile.getFlags()));

        uiHandler.post(new MetricDiffNotifier(
                isOldProfileNull ? null : oldProfile.getMetrics(),
                isNewProfileNull ? null : newProfile.getMetrics()));

        uiHandler.post(new PropertyDiffNotifier(
                isOldProfileNull ? null : oldProfile.getProperties(),
                isNewProfileNull ? null : newProfile.getProperties()));
    }

    private static Runnable createProfileNotifier(
            final Profile oldProfile,
            final Profile newProfile) {

        return new Runnable() {
            @Override
            public void run() {
                for (EventListener eventListener : AudienceStream.getEventListeners()) {
                    if (eventListener instanceof AudienceStream.OnProfileUpdatedListener) {
                        ((AudienceStream.OnProfileUpdatedListener) eventListener)
                                .onProfileUpdated(oldProfile, newProfile);
                    }
                }
            }
        };
    }

}
