package me.lofro.uhc.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GameData {

    private @Setter int time;
    private final List<Team> teams;
    private @Setter boolean isInGame;
    private @Setter boolean inScatter;

    public GameData(int time, List<Team> teams, boolean isInGame, boolean inScatter) {
        this.time = time;
        this.teams = teams;
        this.isInGame = isInGame;
        this.inScatter = inScatter;
    }

    public GameData() {
        this.time = 0;
        this.isInGame = false;
        this.teams = new ArrayList<>();
        this.inScatter = false;
    }

}
