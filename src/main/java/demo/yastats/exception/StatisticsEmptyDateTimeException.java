package demo.yastats.exception;

public class StatisticsEmptyDateTimeException extends RuntimeException {

    public StatisticsEmptyDateTimeException() {
        super();
    }

    public StatisticsEmptyDateTimeException(String s) {
        super(s);
    }
    public StatisticsEmptyDateTimeException(String s, Throwable throwable) {
        super(s, throwable);
    }
    public StatisticsEmptyDateTimeException(Throwable throwable) {
        super(throwable);
    }

}
