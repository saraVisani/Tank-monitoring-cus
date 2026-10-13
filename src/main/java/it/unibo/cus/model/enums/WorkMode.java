package it.unibo.cus.model.enums;

/**
 * Enum Workmode: this enum is for the Global work state of the system
 */
public enum WorkMode {
    MANUAL,
    AUTOMATIC;

    @Override
    public String toString() {
        return switch (this) {
            case MANUAL -> "Manual";
            case AUTOMATIC -> "Automatic";
        };
    }
}