package it.unibo.cus.model.classes.task;

import it.unibo.cus.model.api.Task;

import java.util.List;

import it.unibo.cus.model.api.HelperInputOutput;
import it.unibo.cus.model.classes.task.helperIO.IOhelperTMS;
import it.unibo.cus.model.classes.task.helperIO.IOhelperWCS;

public class InputOutput implements Task {
    @SuppressWarnings("null")
    private final List<HelperInputOutput> helpers = List.of(
        new IOhelperTMS(),
        // new IOhelperDBS(),
        new IOhelperWCS()
    );

    @Override
    public void tick() {
        helpers.forEach(helper -> {
            if (helper.guard()) {
                helper.read();
                helper.send();
            }
        });
    }

    @Override
    public boolean guard() {
        return true;
    }

}
