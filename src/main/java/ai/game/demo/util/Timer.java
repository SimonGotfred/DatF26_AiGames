package ai.game.demo.util;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.Temporal;

import java.util.List;

public class Timer implements TemporalAmount, Comparable<TemporalAmount>
{
    public static Timer from(LocalDateTime start){return new Timer(start);}

    public Timer(){start=LocalDateTime.now();}
    public Timer(LocalDateTime start){this.start=start;}
    public Timer(LocalDate start){this(start.atStartOfDay());}
    public Timer(LocalTime start){this(start.atDate(LocalDate.now()));}

    private LocalDateTime start,stop;
    private Duration duration = Duration.ZERO;

    public LocalDateTime start(){return start=LocalDateTime.now();}
    public Duration       peek(){return Duration.between(start,LocalDateTime.now());}
    public Duration       stop()
    {
        stop = LocalDateTime.now();
        return duration=Duration.between(start,stop);
    }

    public LocalDateTime started(){return    start;}
    public LocalDateTime stopped(){return     stop;}
    public Duration     duration(){return duration;}

    @Override public long get(TemporalUnit unit) {return duration.get(unit);}
    @Override public List<TemporalUnit>    getUnits()         {return duration.getUnits();}
    @Override public Temporal addTo       (Temporal temporal) {return temporal.plus(duration);}
    @Override public Temporal subtractFrom(Temporal temporal) {return temporal.minus(duration);}
    @Override public int compareTo(@NonNull TemporalAmount other) {return duration.compareTo(Duration.from(other));}
}
