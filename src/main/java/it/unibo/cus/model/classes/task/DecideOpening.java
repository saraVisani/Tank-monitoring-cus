package it.unibo.cus.model.classes.task;

import it.unibo.cus.model.api.Task;
import it.unibo.cus.model.classes.State;
import it.unibo.cus.model.classes.Timer;
import it.unibo.cus.model.enums.ProblemsState;
import it.unibo.cus.model.enums.WorkMode;

public class DecideOpening implements Task {
    private final float l1 = (float) 15.5, l2 = (float) 20.5;
    private final long t1 = 10000;
    private boolean open = false;
    private final Timer timer = new Timer("measureTimer", t1, () -> open = true);

    @Override
    public void tick() {
        timer.update();
        if(State.waterLevel > l2){
            open = true;
            State.valveOpenPercentage = 100;
        } else {
            if(State.waterLevel > l1){
                if(open){
                    State.valveOpenPercentage = 50;
                } else {
                    if(!timer.isRunning()){
                        timer.start();
                    }
                }
            } else {
                State.valveOpenPercentage = 0;
                open = false;
                timer.stop();
                timer.reset();
            }
        }
    }

    @Override
    public boolean guard() {
        if(timer.isRunning() && State.workMode != WorkMode.AUTOMATIC){
            timer.stop();
            timer.reset();
            open = false;
        }
        return State.workMode == WorkMode.AUTOMATIC && State.problemsState != ProblemsState.UNCONNECTED && State.problemsState != ProblemsState.UNCON_NOT_AVAIL;
    }

}
