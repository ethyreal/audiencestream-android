package com.tealium.audiencestream.visitor;

import com.tealium.audiencestream.AudienceStream;
import com.tealium.audiencestream.attribute.AttributeGroup;
import com.tealium.audiencestream.attribute.AudienceAttribute;

import java.util.EventListener;

final class AudienceDiffNotifier extends AttributeDiffNotifier<AudienceAttribute> {

    AudienceDiffNotifier(
            AttributeGroup<AudienceAttribute> oldAttributeGroup,
            AttributeGroup<AudienceAttribute> newAttributeGroup) {
        super(oldAttributeGroup, newAttributeGroup);
    }

    /**
     * Notifies the listeners added by
     * that implement {@link com.tealium.audiencestream.AudienceStream.OnAudienceUpdateListener}.
     */
    @Override
    public void run() {
        AudienceStream.OnAudienceUpdateListener audienceUpdateListener;
        for (EventListener eventListener : AudienceStream.getEventListeners()) {
            if (eventListener instanceof AudienceStream.OnAudienceUpdateListener) {
                audienceUpdateListener = (AudienceStream.OnAudienceUpdateListener) eventListener;

                for (AudienceAttribute audience : this.getRemovedAttributes()) {
                    audienceUpdateListener.onAudienceUpdate(audience, null);
                }

                for (AudienceAttribute audience : this.getModifiedAttributes()) {
                    // Won't be any modified if oldAttributeGroup was null.
                    audienceUpdateListener.onAudienceUpdate(
                            this.getOldAttributeGroup().get(audience.getId()),
                            audience);
                }

                for (AudienceAttribute audience : this.getAddedAttributes()) {
                    audienceUpdateListener.onAudienceUpdate(null, audience);
                }
            }
        }
    }
}
