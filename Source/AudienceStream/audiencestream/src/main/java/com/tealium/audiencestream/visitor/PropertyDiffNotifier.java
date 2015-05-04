package com.tealium.audiencestream.visitor;

import com.tealium.audiencestream.AudienceStream;
import com.tealium.audiencestream.attribute.AttributeGroup;
import com.tealium.audiencestream.attribute.PropertyAttribute;

import java.util.EventListener;

final class PropertyDiffNotifier extends AttributeDiffNotifier<PropertyAttribute> implements Runnable {

    PropertyDiffNotifier(AttributeGroup<PropertyAttribute> oldAttributeGroup, AttributeGroup<PropertyAttribute> newAttributeGroup) {
        super(oldAttributeGroup, newAttributeGroup);
    }

    @Override
    public void run() {
        AudienceStream.OnPropertyUpdateListener propertyUpdateListener;
        for (EventListener eventListener : AudienceStream.getEventListeners()) {
            if (eventListener instanceof AudienceStream.OnPropertyUpdateListener) {
                propertyUpdateListener = (AudienceStream.OnPropertyUpdateListener) eventListener;

                for (PropertyAttribute property : this.getRemovedAttributes()) {
                    propertyUpdateListener.onPropertyUpdate(property, null);
                }

                for (PropertyAttribute property : this.getModifiedAttributes()) {
                    propertyUpdateListener.onPropertyUpdate(
                            this.getOldAttributeGroup().get(property.getId()),
                            property);
                }

                for (PropertyAttribute property : this.getAddedAttributes()) {
                    propertyUpdateListener.onPropertyUpdate(null, property);
                }
            }
        }
    }
}
