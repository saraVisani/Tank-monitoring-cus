package it.unibo.cus.model.classes;

import it.unibo.cus.model.api.Task;
import it.unibo.cus.model.api.HelperInputOutput;
import it.unibo.cus.model.classes.helperIO.IOhelperTMS;
import it.unibo.cus.model.enums.ProblemsState;

public class InputOutput implements Task {
    private final HelperInputOutput helperTMS = new IOhelperTMS();
    //private final HelperInputOutput helperDBS = new IOhelperDBS();
    //private final HelperInputOutput helperWCS = new IOhelperWCS();

    @Override
    public void tick() {
        if(State.problemsState != ProblemsState.UNCON_NOT_AVAIL){
            if(State.problemsState != ProblemsState.NOT_AVAILABLE){
                helperTMS.read();
                helperTMS.send();
            }
            if(State.problemsState != ProblemsState.UNCONNECTED){
                //helperDBS.read();
                //helperDBS.send();
            }
        }
        //helperWCS.read();
        //helperWCS.send();
    }

    @Override
    public boolean guard() {
        return true;
    }

}
