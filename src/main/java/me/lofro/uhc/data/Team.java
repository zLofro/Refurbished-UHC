package me.lofro.uhc.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    private @Getter @Setter String name;

    private final @Getter List<UUID> members;

    public Team(final String name, List<UUID> members) {
        this.name = name;
        this.members = members;
    }

    public Team(final String name) {
        this.name = name;
        this.members = new ArrayList<>();
    }

    public Team(final List<UUID> members) {
        this.name = null;
        this.members = members;
    }

}