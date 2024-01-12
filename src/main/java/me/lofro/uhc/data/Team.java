package me.lofro.uhc.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    private @Getter @Setter String name;

    private final @Getter List<UUID> members;
    private @Setter @Getter int timesJoined;

    public Team(final String name, List<UUID> members, int timesJoined) {
        this.name = name;
        this.members = members;
        this.timesJoined = timesJoined;
    }

    public Team(final String name, List<UUID> members) {
        this.name = name;
        this.members = members;
        this.timesJoined = 0;
    }

    public Team(final String name) {
        this.name = name;
        this.members = new ArrayList<>();
        this.timesJoined = 0;
    }

    public Team(final List<UUID> members) {
        this.name = null;
        this.members = members;
        this.timesJoined = 0;
    }

}
