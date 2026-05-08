package ai.game.demo.util;

public abstract class PausableThread extends Thread
{
    private volatile boolean stopped,paused,pause,run;
    protected abstract void loop();

    @Override public final void run()
    {
        run=true;
        while(mayRun())loop();
        paused=stopped=true;
    }

    protected final boolean mayRun()
    {
        paused=true;
        while(pause&&run) Thread.onSpinWait();
        paused=!run;
        return run;
    }

    public final boolean running (){return !stopped&&!paused;}
    public final boolean paused  (){return paused;}
    public final boolean pausing (){return pause&&run;}
    public final boolean stopped (){return stopped;}
    public final boolean stopping(){return !run;}

    public final void Stop   (){pause=run=false;} // ! "why capital S?" - because superclass 'stop()' is both 'deprecated' *and* 'final' blocking the use of that signature
    public final void pause  (){pause=run;}
    public final void unpause(){pause=false;}
    public final void awaitStop (){while(!stopped)Thread.onSpinWait();}
    public final void awaitPause(){while(!paused)Thread.onSpinWait();}
}
