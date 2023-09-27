package com.example.ssafy301.match.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchDetail {

    @Id
    @Column(name = "match_detail_id")
    private Long id;

    @Column(name = "match_id")
    private Long matchId;
    @Column(columnDefinition = "LONGTEXT")
    private String linescore;
    @Column(columnDefinition = "LONGTEXT")
    private String boxscore;
}
