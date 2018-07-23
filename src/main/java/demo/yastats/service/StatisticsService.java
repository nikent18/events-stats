package demo.yastats.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import demo.yastats.exception.StatisticsEmptyDateTimeException;
import demo.yastats.model.Event;
import demo.yastats.utils.Messages;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to collect and display events statistics. We have an opportunity to create different instances
 * of this class with different cache settings to collect information about different event types in one application
 */
public class StatisticsService {

    private Long lastMinute = 60L;
    private Long lastHour = 3600L;
    private Long lastDay = 86400L;
    private Integer eventHoursTTL = 24;

    private AtomicInteger cacheKey = new AtomicInteger(Integer.MIN_VALUE);

    /**
     * Thread safe cache, stores Events
     */
    private Cache<Integer, Event> eventsCache;

    /**
     * Default cache settings, no elements or memory limit
     */
    public StatisticsService() {
        eventsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(eventHoursTTL, TimeUnit.HOURS)
                .removalListener(new EventRemovalListener())
                .build();
    }

    /**
     * Events will be removed from cache after 24 hours or when maxSize exhausted
     * @param maxSize Maximum elements in cache
     */
    public StatisticsService(Long maxSize) {
        eventsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(eventHoursTTL, TimeUnit.HOURS)
                .removalListener(new EventRemovalListener())
                .maximumSize(maxSize)
                .build();
    }

    /**
     * Events will be removed from cache after 24 hours or when out of memory near
     * In case of memory shortage some values will be removed
     * @param softValues
     */
    public StatisticsService(Boolean softValues) {
        CacheBuilder cb = CacheBuilder.newBuilder()
                    .expireAfterWrite(eventHoursTTL, TimeUnit.HOURS)
                    .removalListener(new EventRemovalListener());
        if (softValues) {
            cb.softValues();
        }
        eventsCache = cb.build();
    }

    /**
     * Add event in cache.
     * @param localDateTime
     * @throws StatisticsEmptyDateTimeException
     */
    public void addEvent(LocalDateTime localDateTime) throws StatisticsEmptyDateTimeException {
        if (localDateTime != null) {
            eventsCache.put(cacheKey.getAndIncrement(), new Event(localDateTime));
        } else {
            throw new StatisticsEmptyDateTimeException(Messages.EMPTY_DATE_TIME);
        }
    }

    public long getLastMinuteEvents() {
        return getLastSecondsEvents(lastMinute);
    }

    public long getLastHourEvents() {
        return getLastSecondsEvents(lastHour);
    }

    public long getLastDayEvents() {
        return getLastSecondsEvents(lastDay);
    }

    /**
     * Count number of Events between now and desired number of seconds.
     * In case of memory shortage with softValues constructor could return incorrect count, because some values could be removed
     * @param seconds Number of seconds before current moment
     * @return number of events
     */
    private long getLastSecondsEvents(long seconds) {
        LocalDateTime requestDT = LocalDateTime.now().minusSeconds(seconds);
        return eventsCache.asMap().entrySet().parallelStream()
                .filter(event ->
                        event.getValue().getLocalDateTime() != null &&
                        requestDT.isBefore(event.getValue().getLocalDateTime()))
                .count();
    }
}
