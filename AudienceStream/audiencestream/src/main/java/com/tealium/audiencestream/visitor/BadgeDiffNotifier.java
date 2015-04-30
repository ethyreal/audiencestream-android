package com.tealium.audiencestream.visitor;


import com.tealium.audiencestream.AudienceStream;
import com.tealium.audiencestream.attribute.AttributeGroup;
import com.tealium.audiencestream.attribute.BadgeAttribute;

import java.util.EventListener;

final class BadgeDiffNotifier extends AttributeDiffNotifier<BadgeAttribute> implements Runnable {

    BadgeDiffNotifier(AttributeGroup<BadgeAttribute> oldAttributeGroup, AttributeGroup<BadgeAttribute> newAttributeGroup) {
        super(oldAttributeGroup, newAttributeGroup);
    }

    @Override
    public void run() {

        AudienceStream.OnBadgeUpdateListener badgeUpdateListener;

        for(EventListener eventListener : AudienceStream.getEventListeners()) {

            if (eventListener instanceof AudienceStream.OnBadgeUpdateListener) {
                badgeUpdateListener = (AudienceStream.OnBadgeUpdateListener) eventListener;

                for (BadgeAttribute badge : this.getRemovedAttributes()) {
                    badgeUpdateListener.onBadgeUpdate(badge, null);
                }

                for (BadgeAttribute badge : this.getModifiedAttributes()) {
                    badgeUpdateListener.onBadgeUpdate(
                            this.getOldAttributeGroup().get(badge.getId()),
                            badge);
                }

                for (BadgeAttribute badge : this.getAddedAttributes()) {
                    badgeUpdateListener.onBadgeUpdate(null, badge);
                }
            }
        }
    }
}
