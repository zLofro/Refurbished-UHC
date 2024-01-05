package me.lofro.uhc.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GameData {

    private @Setter int time;
    private final List<Team> teams;

    public GameData(int time, List<Team> teams) {
        this.time = time;
        this.teams = teams;
    }

    public GameData() {
        this.time = 0;
        this.teams = new ArrayList<>();
    }

}
