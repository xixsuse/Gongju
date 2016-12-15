package com.thatzit.kjw.stamptour_gongju_client.main.msgListener;

import com.thatzit.kjw.stamptour_gongju_client.push.service.event.GpsStateEvent;

/**
 * Created by kjw on 16. 9. 15..
 */
public interface MapViewGpsStateListener {
    public void OnReceivedParentStateEvent(GpsStateEvent event);
}
