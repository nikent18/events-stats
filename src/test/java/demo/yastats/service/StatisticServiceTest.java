package demo.yastats.service;

import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import demo.yastats.exception.StatisticsEmptyDateTimeException;
import demo.yastats.model.Event;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class StatisticServiceTest {

    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;

    @Test
    public void putNewEventTest() {
        LocalDateTime dt = LocalDateTime.now();
        StatisticsService statisticsService = new StatisticsService(true);
        statisticsService.addEvent(dt);
        long minuteEvents = statisticsService.getLastMinuteEvents();
        long hourEvents = statisticsService.getLastHourEvents();
        long dayEvents = statisticsService.getLastDayEvents();
        Assert.assertTrue(minuteEvents == 1);
        Assert.assertTrue(hourEvents == 1);
        Assert.assertTrue(dayEvents == 1);
    }

    @Test(expected = StatisticsEmptyDateTimeException.class)
    public void putNullDateTimeEventTest() {
        StatisticsService statisticsService = new StatisticsService();
        statisticsService.addEvent(null);
    }

    @Test
    public void multithreadingAddEventsTest() throws ExecutionException, InterruptedException {
        StatisticsService statisticsService = new StatisticsService();
        Integer numberOfEventsForTask = 5000;
        Integer numberOfThreads = 3;
        Integer numberOfTasks = 3;
        Callable<Void> callableTask = () -> {
            IntStream.range(0, numberOfEventsForTask).forEach(i -> {
                statisticsService.addEvent(LocalDateTime.now());
            });
            return null;
        };
        List callableTasks = new ArrayList<>();
        IntStream.range(0, numberOfTasks).forEach(i -> callableTasks.add(callableTask));
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        executorService.invokeAll(callableTasks);
        executorService.shutdown();
        long eventsNumber = statisticsService.getLastDayEvents();
        Assert.assertTrue("Events number = " + eventsNumber, numberOfEventsForTask * numberOfTasks == eventsNumber);
    }

    @Test
    public void lastMinuteEventsTest() {
        StatisticsService statisticsService = new StatisticsService();
        Integer numberOfEvents = 100;
        statisticsService.addEvent(LocalDateTime.now().minusSeconds(59));
        statisticsService.addEvent(LocalDateTime.now().minusSeconds(60));
        Assert.assertTrue(statisticsService.getLastMinuteEvents() == 1);
        Assert.assertTrue(statisticsService.getLastHourEvents() == 2);
        Assert.assertTrue(statisticsService.getLastDayEvents() == 2);
    }

    @Test
    public void lastHourEventsTest() {
        StatisticsService statisticsService = new StatisticsService();
        Integer numberOfEvents = 100;
        statisticsService.addEvent(LocalDateTime.now().minusSeconds(3599));
        statisticsService.addEvent(LocalDateTime.now().minusSeconds(3600));
        Assert.assertTrue(statisticsService.getLastMinuteEvents() == 0);
        Assert.assertTrue(statisticsService.getLastHourEvents() == 1);
        Assert.assertTrue(statisticsService.getLastDayEvents() == 2);
    }

    @Test
    public void lastDayEventsTest() {
        StatisticsService statisticsService = new StatisticsService();
        Integer numberOfEvents = 100;
        statisticsService.addEvent(LocalDateTime.now().minusSeconds(86399));
        statisticsService.addEvent(LocalDateTime.now().minusSeconds(86400));
        Assert.assertTrue(statisticsService.getLastMinuteEvents() == 0);
        Assert.assertTrue(statisticsService.getLastHourEvents() == 0);
        Assert.assertTrue(statisticsService.getLastDayEvents() == 1);
    }

    @Test
    public void removalListenerNullEventOutput() {
        outContent.reset();
        EventRemovalListener eventRemovalListener = new EventRemovalListener();
        eventRemovalListener.onRemoval(RemovalNotification.create(1, null, RemovalCause.COLLECTED));
        Assert.assertTrue(outContent.toString(), !outContent.toString().contains("Event info"));
        Assert.assertTrue(outContent.toString(), !outContent.toString().contains("Date and time:"));
    }

    @Test
    public void removalListenerNotNullEventOutput() {
        outContent.reset();
        EventRemovalListener eventRemovalListener = new EventRemovalListener();
        eventRemovalListener.onRemoval(RemovalNotification.create(1, new Event(LocalDateTime.now()), RemovalCause.COLLECTED));
        Assert.assertTrue(outContent.toString(), outContent.toString().contains("Event info"));
        Assert.assertTrue(outContent.toString(), outContent.toString().contains("Date and time:"));
    }

    @Test
    public void eventToStringTest() {
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event(now);
        String expected = "Date and time: " + now.toString();
        Assert.assertEquals("To string method not correct", expected, event.toString());
    }

    @Test
    public void eventSetterTest() {
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event(now);
        event.setLocalDateTime(now.plusSeconds(1));
        Assert.assertEquals("Setter works incorrect", event.getLocalDateTime(), now.plusSeconds(1));
    }

    @Test
    public void maxSizeConstructorTest() {
        Integer maxSize = 50;
        StatisticsService statisticsService = new StatisticsService(Long.valueOf(maxSize));
        IntStream.range(0, maxSize*2).forEach(i -> statisticsService.addEvent(LocalDateTime.now()));
        Assert.assertTrue(statisticsService.getLastHourEvents() == maxSize);
    }

    @BeforeClass
    public static void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterClass
    public static void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

}
