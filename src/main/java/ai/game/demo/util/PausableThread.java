package ai.game.demo.util;

public abstract class PausableThread extends Thread
{
    private volatile boolean stopped,paused,pause,run=true;
    protected abstract void loop();

    @Override public final void run()
    {
        while(mayRun())loop();
        paused=stopped=true;
    }

    protected final boolean mayRun()
    {
        paused=true;
        while(pause&&run) Thread.onSpinWait();
        paused = !run;
        return run;
    }

    public final boolean running (){return !stopped&&!paused;}
    public final boolean paused  (){return paused;}
    public final boolean pausing (){return pause&&run;}
    public final boolean stopped (){return stopped;}
    public final boolean stopping(){return !run;}

    public final void Stop   (){pause=run=false;} // ! why 'stop' final if deprecated !
    public final void pause  (){pause=run;}
    public final void unpause(){pause=false;}
}
