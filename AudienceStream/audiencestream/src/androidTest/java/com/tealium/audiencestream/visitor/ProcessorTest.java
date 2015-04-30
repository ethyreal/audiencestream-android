package com.tealium.audiencestream.visitor;

import com.tealium.audiencestream.AudienceStream;
import com.tealium.audiencestream.attribute.AudienceAttribute;
import com.tealium.audiencestream.attribute.BadgeAttribute;
import com.tealium.audiencestream.attribute.DateAttribute;
import com.tealium.audiencestream.attribute.FlagAttribute;
import com.tealium.audiencestream.attribute.MetricAttribute;
import com.tealium.audiencestream.attribute.PropertyAttribute;
import com.tealium.audiencestream.testutil.TestUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ProcessorTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AudienceStream.getEventListeners().clear();
    }

    public void testProccess() throws Throwable {
        final long wait = 500;

        UpdateReceiver receiver = new UpdateReceiver().reset();
        AudienceStream.getEventListeners().add(receiver);

        final Profile oldProfile = createOldProfile();
        final Profile newProfile = createNewProfile();

        Processor.process(null, null);
        Thread.sleep(1000); // Let operation run.
        Assert.assertFalse(receiver.isUpdated());

        Processor.process(oldProfile, oldProfile);
        Thread.sleep(wait); // Let operation run.
        Assert.assertFalse(receiver.isUpdated());

        Processor.process(oldProfile, null);
        Thread.sleep(wait); // Let operation run.
        Assert.assertFalse(receiver.isAudienceUpdated);
        Assert.assertFalse(receiver.isBadgeUpdated);
        Assert.assertFalse(receiver.isDateUpdated);
        Assert.assertFalse(receiver.isFlagUpdated);
        Assert.assertFalse(receiver.isMetricUpdated);
        Assert.assertFalse(receiver.isPropertyUpdated);
        Assert.assertTrue(receiver.isProfileUpdated);

        receiver.reset();

        Processor.process(null, newProfile);
        Thread.sleep(wait); // Let operation run.
        Assert.assertTrue(receiver.isAudienceUpdated);
        Assert.assertTrue(receiver.isBadgeUpdated);
        Assert.assertTrue(receiver.isDateUpdated);
        Assert.assertTrue(receiver.isFlagUpdated);
        Assert.assertTrue(receiver.isMetricUpdated);
        Assert.assertTrue(receiver.isPropertyUpdated);
        Assert.assertTrue(receiver.isProfileUpdated);

        receiver.reset();

        Processor.process(oldProfile, newProfile);
        Thread.sleep(wait); // Let operation run.
        Assert.assertTrue(receiver.isAudienceUpdated);
        Assert.assertTrue(receiver.isBadgeUpdated);
        Assert.assertTrue(receiver.isDateUpdated);
        Assert.assertTrue(receiver.isFlagUpdated);
        Assert.assertTrue(receiver.isMetricUpdated);
        Assert.assertTrue(receiver.isPropertyUpdated);
        Assert.assertTrue(receiver.isProfileUpdated);
    }

    private Profile createOldProfile() {
        return new Profile.Builder()
                .setCreationDate(System.currentTimeMillis())
                .setIsNewVisitor(true)
                .build();
    }

    private Profile createNewProfile() {
        return new Profile.Builder()
                .setAudiences(TestUtil.createAttrCollection(new AudienceAttribute("a", "b")))
                .setBadges(TestUtil.createAttrCollection(new BadgeAttribute("a")))
                .setCreationDate(System.currentTimeMillis())
                .setCurrentVisit(new CurrentVisit())
                .setDates(TestUtil.createAttrCollection(new DateAttribute("a", 0)))
                .setFlags(TestUtil.createAttrCollection(new FlagAttribute("a", false)))
                .setIsNewVisitor(false)
                .setMetrics(TestUtil.createAttrCollection(new MetricAttribute("a", 0)))
                .setProperties(TestUtil.createAttrCollection(new PropertyAttribute("a", null)))
                .build();
    }

    private static class UpdateReceiver implements
            AudienceStream.OnAudienceUpdateListener,
            AudienceStream.OnBadgeUpdateListener,
            AudienceStream.OnDateUpdateListener,
            AudienceStream.OnFlagUpdateListener,
            AudienceStream.OnMetricUpdateListener,
            AudienceStream.OnPropertyUpdateListener,
            AudienceStream.OnProfileUpdatedListener {

        volatile boolean isAudienceUpdated;
        volatile boolean isBadgeUpdated;
        volatile boolean isDateUpdated;
        volatile boolean isFlagUpdated;
        volatile boolean isMetricUpdated;
        volatile boolean isProfileUpdated;
        volatile boolean isPropertyUpdated;

        @Override
        public void onAudienceUpdate(AudienceAttribute oldAudience, AudienceAttribute newAudience) {
            this.isAudienceUpdated = true;
        }

        @Override
        public void onBadgeUpdate(BadgeAttribute oldBadge, BadgeAttribute newBadge) {
            this.isBadgeUpdated = true;
        }

        @Override
        public void onDateUpdate(DateAttribute oldDate, DateAttribute newDate) {
            this.isDateUpdated = true;
        }

        @Override
        public void onFlagUpdate(FlagAttribute oldFlag, FlagAttribute newFlag) {
            this.isFlagUpdated = true;
        }

        @Override
        public void onMetricUpdate(MetricAttribute oldMetric, MetricAttribute newMetric) {
            this.isMetricUpdated = true;
        }

        @Override
        public void onProfileUpdated(Profile oldProfile, Profile newProfile) {
            this.isProfileUpdated = true;
        }

        @Override
        public void onPropertyUpdate(PropertyAttribute oldProperty, PropertyAttribute newProperty) {
            this.isPropertyUpdated = true;
        }

        boolean isUpdated() {
            return this.isAudienceUpdated ||
                    this.isBadgeUpdated ||
                    this.isDateUpdated ||
                    this.isFlagUpdated ||
                    this.isMetricUpdated ||
                    this.isProfileUpdated ||
                    this.isPropertyUpdated;
        }

        UpdateReceiver reset() {
            this.isAudienceUpdated = false;
            this.isBadgeUpdated = false;
            this.isDateUpdated = false;
            this.isFlagUpdated = false;
            this.isMetricUpdated = false;
            this.isProfileUpdated = false;
            this.isPropertyUpdated = false;
            return this;
        }
    }
}
