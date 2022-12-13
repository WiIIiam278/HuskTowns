package net.william278.husktowns.town;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Role {

    private int weight;
    private String id;
    private String name;
    private List<Privilege> privileges;

    private Role(int weight, @NotNull String id, @NotNull String name, @NotNull List<Privilege> privileges) {
        this.weight = weight;
        this.id = id;
        this.name = name;
    }

    public static Role of(int weight, @NotNull String id, @NotNull String name, @NotNull List<Privilege> privileges) {
        return new Role(weight, id, name, privileges);
    }

    @SuppressWarnings("unused")
    private Role() {
    }

    public int getWeight() {
        return weight;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
