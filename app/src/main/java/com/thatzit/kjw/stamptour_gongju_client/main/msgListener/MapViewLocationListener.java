package com.thatzit.kjw.stamptour_gongju_client.main.msgListener;

import com.thatzit.kjw.stamptour_gongju_client.push.service.event.LocationEvent;

/**
 * Created by kjw on 16. 9. 15..
 */
public interface MapViewLocationListener {
    public void OnReceivedLocation(LocationEvent locationEvent);
}
