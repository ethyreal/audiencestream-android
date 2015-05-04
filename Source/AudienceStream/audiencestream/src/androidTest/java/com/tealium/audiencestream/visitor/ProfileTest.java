package com.tealium.audiencestream.visitor;

import com.tealium.audiencestream.attribute.AudienceAttribute;
import com.tealium.audiencestream.attribute.BadgeAttribute;
import com.tealium.audiencestream.attribute.DateAttribute;
import com.tealium.audiencestream.attribute.FlagAttribute;
import com.tealium.audiencestream.attribute.MetricAttribute;
import com.tealium.audiencestream.attribute.PropertyAttribute;
import com.tealium.audiencestream.testutil.TestUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

@SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
public class ProfileTest extends TestCase {

    public void testBuilder() throws Throwable {

        final long now = System.currentTimeMillis();

        Profile profile = new Profile.Builder()
                .setAudiences(TestUtil.createAttrCollection(new AudienceAttribute("a", "b")))
                .setBadges(TestUtil.createAttrCollection(new BadgeAttribute("a")))
                .setCreationDate(now)
                .setCurrentVisit(new CurrentVisit())
                .setDates(TestUtil.createAttrCollection(new DateAttribute("a", 0)))
                .setFlags(TestUtil.createAttrCollection(new FlagAttribute("a", false)))
                .setIsNewVisitor(false)
                .setMetrics(TestUtil.createAttrCollection(new MetricAttribute("a", 0)))
                .setProperties(TestUtil.createAttrCollection(new PropertyAttribute("a", null)))
                .build();

        Assert.assertEquals(now, profile.getCreationTimestamp());
        Assert.assertEquals(1, profile.getAudiences().size());
        Assert.assertEquals(1, profile.getBadges().size());
        Assert.assertEquals(1, profile.getDates().size());
        Assert.assertEquals(1, profile.getFlags().size());
        Assert.assertEquals(1, profile.getMetrics().size());
        Assert.assertEquals(1, profile.getProperties().size());
        Assert.assertFalse(profile.isNewVisitor());
    }

    public void testEquals() throws Throwable {
        final Profile oldProfile = new Profile.Builder().build();

        Assert.assertFalse(oldProfile.equals(null));
        Assert.assertFalse(oldProfile.equals("something"));
        Assert.assertTrue(oldProfile.equals(new Profile.Builder().build()));
        Assert.assertFalse(oldProfile.equals(createNewProfile()));
    }

    public void testToString() throws Throwable {
        final Profile oldProfile = new Profile.Builder().build();

        Assert.assertTrue(oldProfile.toString().equals(new Profile.Builder().build().toString()));
        Assert.assertFalse(oldProfile.toString().equals(createNewProfile().toString()));
    }

    public void testHashCode() throws Throwable {
        final Profile oldProfile = new Profile.Builder().build();
        final Profile newProfile = createNewProfile();

        Assert.assertEquals(oldProfile.hashCode(), new Profile.Builder().build().hashCode());
        Assert.assertTrue(oldProfile.hashCode() != newProfile.hashCode());
    }

    public Profile createNewProfile() {
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
}
