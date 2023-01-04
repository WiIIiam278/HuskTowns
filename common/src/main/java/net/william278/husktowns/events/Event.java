package net.william278.husktowns.events;

public interface Event {

    void setCancelled(boolean cancelled);

    boolean isCancelled();

}
