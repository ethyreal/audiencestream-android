package com.tealium.audiencestream.visitor;

import com.tealium.audiencestream.AudienceStream;
import com.tealium.audiencestream.attribute.AttributeGroup;
import com.tealium.audiencestream.attribute.MetricAttribute;

import java.util.EventListener;

final class MetricDiffNotifier extends AttributeDiffNotifier<MetricAttribute> implements Runnable {

    MetricDiffNotifier(AttributeGroup<MetricAttribute> oldAttributeGroup, AttributeGroup<MetricAttribute> newAttributeGroup) {
        super(oldAttributeGroup, newAttributeGroup);
    }

    @Override
    public void run() {
        AudienceStream.OnMetricUpdateListener metricUpmetricListener;
        for (EventListener eventListener : AudienceStream.getEventListeners()) {
            if (eventListener instanceof AudienceStream.OnMetricUpdateListener) {
                metricUpmetricListener = (AudienceStream.OnMetricUpdateListener) eventListener;

                for (MetricAttribute metric : this.getRemovedAttributes()) {
                    metricUpmetricListener.onMetricUpdate(metric, null);
                }

                for (MetricAttribute metric : this.getModifiedAttributes()) {
                    metricUpmetricListener.onMetricUpdate(
                            this.getOldAttributeGroup().get(metric.getId()),
                            metric);
                }

                for (MetricAttribute metric : this.getAddedAttributes()) {
                    metricUpmetricListener.onMetricUpdate(null, metric);
                }
            }
        }
    }
}
