package com.tealium.audiencestream.visitor;

import com.tealium.audiencestream.AudienceStream;
import com.tealium.audiencestream.attribute.AttributeGroup;
import com.tealium.audiencestream.attribute.DateAttribute;

import java.util.EventListener;

final class DateDiffNotifier extends AttributeDiffNotifier<DateAttribute> implements Runnable {

    DateDiffNotifier(AttributeGroup<DateAttribute> oldAttributeGroup, AttributeGroup<DateAttribute> newAttributeGroup) {
        super(oldAttributeGroup, newAttributeGroup);
    }

    @Override
    public void run() {
        AudienceStream.OnDateUpdateListener dateUpdateListener;
        for (EventListener eventListener : AudienceStream.getEventListeners()) {
            if (eventListener instanceof AudienceStream.OnDateUpdateListener) {
                dateUpdateListener = (AudienceStream.OnDateUpdateListener) eventListener;

                for (DateAttribute date : this.getRemovedAttributes()) {
                    dateUpdateListener.onDateUpdate(date, null);
                }

                for (DateAttribute date : this.getModifiedAttributes()) {
                    dateUpdateListener.onDateUpdate(
                            this.getOldAttributeGroup().get(date.getId()),
                            date);
                }

                for (DateAttribute date : this.getAddedAttributes()) {
                    dateUpdateListener.onDateUpdate(null, date);
                }
            }
        }
    }
}
