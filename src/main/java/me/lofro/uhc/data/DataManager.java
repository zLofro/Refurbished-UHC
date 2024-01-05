package me.lofro.uhc.data;

import lombok.Getter;
import me.lofro.uhc.UHC;
import me.lofro.uhc.api.data.JsonConfig;

public class DataManager {

  private final @Getter JsonConfig gameDataJson;

  public DataManager(final UHC uhc) throws Exception {
    this.gameDataJson = JsonConfig.cfg("gameData.json", uhc);
  }

  public void save() {
    UHC.getInstance().getGameManager().save(gameDataJson);
  }

  public void restore() {
    UHC.getInstance().getGameManager().restore(gameDataJson);
  }

}
