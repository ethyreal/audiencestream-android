package com.tealium.audiencestream.visitor;

import com.tealium.audiencestream.AudienceStream;
import com.tealium.audiencestream.attribute.AttributeGroup;
import com.tealium.audiencestream.attribute.FlagAttribute;

import java.util.EventListener;

/**
 * Created by chadhartman on 1/26/15.
 */
final class FlagDiffNotifier extends AttributeDiffNotifier<FlagAttribute> implements Runnable {

    FlagDiffNotifier(AttributeGroup<FlagAttribute> oldAttributeGroup, AttributeGroup<FlagAttribute> newAttributeGroup) {
        super(oldAttributeGroup, newAttributeGroup);
    }

    @Override
    public void run() {
        AudienceStream.OnFlagUpdateListener flagUpdateListener;
        for (EventListener eventListener : AudienceStream.getEventListeners()) {
            if (eventListener instanceof AudienceStream.OnFlagUpdateListener) {
                flagUpdateListener = (AudienceStream.OnFlagUpdateListener) eventListener;

                for (FlagAttribute flag : this.getRemovedAttributes()) {
                    flagUpdateListener.onFlagUpdate(flag, null);
                }

                for (FlagAttribute flag : this.getModifiedAttributes()) {
                    flagUpdateListener.onFlagUpdate(
                            this.getOldAttributeGroup().get(flag.getId()),
                            flag);
                }

                for (FlagAttribute flag : this.getAddedAttributes()) {
                    flagUpdateListener.onFlagUpdate(null, flag);
                }
            }
        }
    }
}
