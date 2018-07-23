package demo.yastats.service;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import demo.yastats.model.Event;

/**
 * Listener of removal event. Events may be removed for several reasons, eg: outOfMemory, Time end
 */
class EventRemovalListener implements RemovalListener<Integer, Event> {

    /**
     * Prints information about removal reason.
     * @param removalNotification
     */
    @Override
    public void onRemoval(RemovalNotification<Integer, Event> removalNotification) {
        StringBuffer sb = new StringBuffer();
        sb
                .append("WARN Event: Id=")
                .append(removalNotification.getKey())
                .append(" Was deleted with reason: ")
                .append(removalNotification.getCause());
        if (removalNotification.getValue() != null) {
            sb
                    .append(" Event info: ")
                    .append(removalNotification.getValue());
        }

        System.out.println(sb.toString());
    }
}
