
package it.unibo.cus.model.classes;

import java.util.List;

import it.unibo.cus.model.api.Task;

/**
 * The scheduler manages tasks and their execution timing.
 * It checks each task's guard and executes its tick when the guard is true.
a */
public class Schedular {

    private final List<Task> tasks;
    private boolean tick = true;
    private final Timer timer;

    public Schedular(List<Task> tasks) {
        this.tasks = tasks;

        this.timer = new Timer(
            "Schedular",
            100,
            () -> this.tick = true
        );

        this.timer.start();
    }

    public void schedular() {
        timer.update();
        if (tick) {
            timer.restart();
            tick = false;
            tasks.forEach(t -> {
                if (t.guard()) {
                    t.tick();
                }
            });
        }
    }
}
