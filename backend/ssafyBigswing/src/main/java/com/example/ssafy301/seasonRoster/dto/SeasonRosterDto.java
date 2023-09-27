package com.example.ssafy301.seasonRoster.dto;

import com.example.ssafy301.seasonRoster.domain.SeasonRoster;
import lombok.Data;

@Data
public class SeasonRosterDto {

    private Long id;
    private Long playerId;
    private String playerName;
    private String Image;
    private int season;

    public SeasonRosterDto(SeasonRoster seasonRoster) {
        this.id = seasonRoster.getId();
        this.playerId = seasonRoster.getPlayer().getId();
        this.playerName = seasonRoster.getPlayer().getKorName();
        this.Image = seasonRoster.getPlayer().getImage();
        this.season = seasonRoster.getSeason();
    }
}
