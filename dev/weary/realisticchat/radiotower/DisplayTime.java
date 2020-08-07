package dev.weary.realisticchat.radiotower;

public class DisplayTime {
    public static DisplayTime NONE = new DisplayTime();

    private boolean isDisabled = false;
    private int hours;
    private int minutes;
    private int seconds;

    private DisplayTime() {
        this.isDisabled = true;
    }

    public DisplayTime(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        if (this.isDisabled) {
            return "--:--:--";
        }

        return String.format("%02d:%02d:%02d", this.hours, this.minutes, this.seconds);
    }
}
