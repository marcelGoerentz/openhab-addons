package org.openhab.binding.easyfamily.internal.dto.data;

import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_MARKERS;
import static org.openhab.binding.easyfamily.internal.EasyFamilyBindingConstants.CHANNEL_NET_MARKERS;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class MarkerBit extends Marker {

    MarkerBit(int number, int value, int netIndex) {
        super(CHANNEL_MARKERS, number, value, netIndex);
        if (netIndex > 0) {
            this.type = CHANNEL_NET_MARKERS;
        }
    }
}
