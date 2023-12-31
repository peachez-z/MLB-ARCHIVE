package com.example.ssafy301.simulation.service;

import com.example.ssafy301.common.api.exception.NotFoundException;
import com.example.ssafy301.common.api.status.FailCode;
import com.example.ssafy301.hitting.dto.HittingReqDto;
import com.example.ssafy301.hitting.dto.HittingRespDto;
import com.example.ssafy301.hitting.service.HittingService;
import com.example.ssafy301.match.domain.Match;
import com.example.ssafy301.match.domain.MatchDetail;
import com.example.ssafy301.match.dto.MatchDetailDto;
import com.example.ssafy301.match.dto.MatchDto;
import com.example.ssafy301.match.dto.QMatchDto;
import com.example.ssafy301.match.repository.MatchDetailRepository;
import com.example.ssafy301.match.repository.MatchRepository;
import com.example.ssafy301.match.service.MatchService;
import com.example.ssafy301.pitching.dto.PitchingReqDto;
import com.example.ssafy301.pitching.dto.PitchingRespDto;
import com.example.ssafy301.pitching.service.PitchingService;
import com.example.ssafy301.player.domain.Player;
import com.example.ssafy301.player.domain.Position;
import com.example.ssafy301.player.repository.PlayerRepository;
import com.example.ssafy301.player.service.PlayerService;
import com.example.ssafy301.seasonRoster.domain.SeasonRoster;
import com.example.ssafy301.seasonRoster.dto.SeasonRosterDto;
import com.example.ssafy301.seasonRoster.dto.SeasonRosterReqDto;
import com.example.ssafy301.seasonRoster.service.SeasonRosterService;
import com.example.ssafy301.simulation.dto.SimulationMembersDto;
import com.example.ssafy301.simulation.dto.SimulationMembersJsonDto;
import com.example.ssafy301.simulation.dto.SimulationMembersJsonDto.Players;
import com.example.ssafy301.simulation.dto.SimulationResponseDto;
import com.example.ssafy301.teamLike.dto.TeamLikeDto;
import com.example.ssafy301.user.service.UserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ssafy301.simulation.dto.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.ssafy301.match.domain.QMatch.*;
import static com.example.ssafy301.seasonRoster.domain.QSeasonRoster.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final MatchService matchService;
    private final SeasonRosterService seasonRosterService;
    private final UserService userService;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final HittingService hittingService;
    private final PitchingService pitchingService;
    private final JPAQueryFactory queryFactory;
    private final MatchRepository matchRepository;
    private final MatchDetailRepository matchDetailRepository;
    private final ObjectMapper objectMapper;
    // 시뮬레이션 추천
    // 내가 좋아하는 팀이 참여한 경기 중
    // 큰 점수 차로 진 경기를 보여주자
    public List<MatchDto> getRecommendedSimulations(String refreshToken) {

        List<TeamLikeDto> likeTeams = userService.getLikedTeams(refreshToken);
        TeamLikeDto likeTeam = null;

        if(likeTeams != null && !likeTeams.isEmpty()) {
            likeTeam = likeTeams.get(0);
        }

        int currentYear = LocalDate.now().getYear();

        List<MatchDto> result;

        // 좋아하는 팀이 없다면
        // 작년부터 올해까지의 전체 경기 목록에서
        // 점수 차가 큰 Top5를 가져오자
        if(likeTeam == null) {
            // 우선 작년부터 올해까지의 경기 리스트를 모두 가져오자
            List<MatchDto> allMatches = queryFactory
                    .select(new QMatchDto(
                            match
                    ))
                    .from(match)
                    .where(
                            matchDateIn(currentYear - 1, currentYear))
                    .fetch();

            // 전체 경기 중에
            // 점수 차가 큰 Top5 경기를 가져옴
            Collections.sort(allMatches, new Comparator<MatchDto>() {
                @Override
                public int compare(MatchDto match1, MatchDto match2) {
                    // 홈팀과 어웨이팀의 점수차가 클수록 앞에 위치
                    int scoreDifference1 = Math.abs(match1.getHomeScore() - match1.getAwayScore());
                    int scoreDifference2 = Math.abs(match2.getHomeScore() - match2.getAwayScore());

                    return Integer.compare(scoreDifference2, scoreDifference1);
                }
            });

            result = allMatches.subList(0, Math.min(5, allMatches.size()));
        }
        // 좋아하는 팀이 있다면
        // 작년부터 올해까지의 경기 중
        // 내 팀이 참여한 경기만을 골라
        // 점수 차가 크게 진 Top5를 가져오자
        else {
            // 우선 내가 좋아하는 팀이 참가한 경기 리스트를 모두 가져오자
            List<MatchDto> myTeamMatches = queryFactory
                    .select(new QMatchDto(
                            match
                    ))
                    .from(match)
                    .where(
                            matchTeam(likeTeam.getTeamId()),
                            matchDateIn(currentYear - 1, currentYear))
                    .fetch();

            // 경기가 없다면 예외 발생
            if(myTeamMatches == null || myTeamMatches.size() == 0) {
                throw new NotFoundException(FailCode.NO_MATCH);
            }

//            for (MatchDto myTeamMatch : myTeamMatches) {
//                log.debug(myTeamMatch.getHomeName() + " : " + myTeamMatch.getAwayName() + " = " + myTeamMatch.getHomeScore() + " : " + myTeamMatch.getAwayScore());
//            }

            // 그리고 내가 응원하는 팀이 진 경기를 가져오자
            List<MatchDto> loseMatches = new ArrayList<>();
            Long myTeamId = likeTeam.getTeamId();
            for (MatchDto myTeamMatch : myTeamMatches) {
                // 경기에서 내가 좋아하는 팀이 홈팀인 경우, 홈팀의 점수가 더 낮을 때 loseMatches에 넣어줌
                if((myTeamMatch.getHomeId().equals(myTeamId)) && (myTeamMatch.getHomeScore() < myTeamMatch.getAwayScore())) {
                    loseMatches.add(myTeamMatch);
                }
                // 경기에서 내가 좋아하는 팀이 어웨이팀인 경우, 어웨이팀의 점수가 더 낮을 때 loseMatches에 넣어줌
                else if ((myTeamMatch.getAwayId().equals(myTeamId)) && (myTeamMatch.getAwayScore() < myTeamMatch.getHomeScore())) {
                    loseMatches.add(myTeamMatch);
                }
            }


            // 작년부터 지금까지 진 경기가 없다면 예외 발생
            if(loseMatches.isEmpty()) {
                throw new NotFoundException(FailCode.NO_LOSE_MATCH);
            }

            // 진 경기 중에 더 큰 차이로 진 Top5 경기를 반환해줌
            Collections.sort(loseMatches, new Comparator<MatchDto>() {
                @Override
                public int compare(MatchDto match1, MatchDto match2) {
                    // 홈팀과 어웨이팀의 점수차가 클수록 앞에 위치
                    int scoreDifference1 = Math.abs(match1.getHomeScore() - match1.getAwayScore());
                    int scoreDifference2 = Math.abs(match2.getHomeScore() - match2.getAwayScore());

                    return Integer.compare(scoreDifference2, scoreDifference1);
                }
            });

            result = loseMatches.subList(0, Math.min(5, loseMatches.size()));
        }

        return result;
    }

    private BooleanExpression matchDateIn(int startYear, int endYear) {
        return match.matchDate.year().between(startYear, endYear);
    }

    private BooleanExpression matchTeam(Long teamId) {
        return match.homeId.eq(teamId).or(match.awayId.eq(teamId));
    }


    // 시뮬레이션 하고 싶은 경기를 선택하면
    // 해당 경기에 참여한 두 팀의 선수 목록을 보내줌
    public SimulationMembersDto getSimulationTeams(Long matchId) {
        MatchDetailDto match = matchService.getMatchById(matchId);
        List<SeasonRosterDto> homeMembers = seasonRosterService.getSeasonRosterList(new SeasonRosterReqDto(match.getHomeId(), match.getMatchDate().getYear()));
        List<SeasonRosterDto> awayMembers = seasonRosterService.getSeasonRosterList(new SeasonRosterReqDto(match.getAwayId(), match.getMatchDate().getYear()));
        return new SimulationMembersDto(homeMembers, awayMembers);
    }

    public SimulationMembersJsonDto getSimulationMembersByMatchId(Long matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new NotFoundException(FailCode.NO_MATCH));;
        MatchDetail matchDetail = matchDetailRepository.findById(match.getMatchDetailId()).orElseThrow(() -> new NotFoundException(FailCode.NO_MATCH));;
        log.debug("DTO Result: " + matchDetail.getMatchId()+" zzzz ",matchDetail.getId());
        if (matchDetail == null) {
            throw new NotFoundException(FailCode.NO_MATCH);
        }

        try {
            log.debug("왜 안돼~~~~~~~~~~~~~~~~~~~~~~~~~~");
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            SimulationMembersJsonDto dto = objectMapper.readValue(matchDetail.getBoxscore(), SimulationMembersJsonDto.class);
            log.debug("DTO Result: " + dto);
            return dto;
        } catch (IOException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }

    public SimulationResponseDto getFilteredMembers(SimulationMembersJsonDto dto) {
        Map<String, Players> awayPlayers = dto.getBoxscore().getTeams().getAway().getPlayers();
        Map<String, Players> homePlayers = dto.getBoxscore().getTeams().getHome().getPlayers();

        List<Players> filteredAwayBatters = dto.getBoxscore().getTeams().getAway().getBattingOrder().stream()
                .map(pid -> awayPlayers.get("ID" + pid))
                .collect(Collectors.toList());

        List<Players> filteredAwayPitchers = dto.getBoxscore().getTeams().getAway().getPitchers().stream()
                .map(pid -> awayPlayers.get("ID" + pid))
                .collect(Collectors.toList());

        List<Players> filteredHomeBatters = dto.getBoxscore().getTeams().getHome().getBattingOrder().stream()
                .map(pid -> homePlayers.get("ID" + pid))
                .collect(Collectors.toList());

        List<Players> filteredHomePitchers = dto.getBoxscore().getTeams().getHome().getPitchers().stream()
                .map(pid -> homePlayers.get("ID" + pid))
                .collect(Collectors.toList());

        return new SimulationResponseDto(filteredHomeBatters, filteredHomePitchers, filteredAwayBatters, filteredAwayPitchers);
    }

    public PlayerSearchRespDto getReplacementList(SimulationPlayerSearchDto searchDto) {

        // 우선 검색어가 이름에 포함된 선수가 존재하는지 확인
        List<Player> allPlayers = playerService.searchSimulationPlayerByName(searchDto.getContent());

        // 팀id랑 season, 그리고 바꾸려는 선수의 포지션으로
        // seasonRoster 목록을 가져오자
        // 교체되려는 선수의 포지션을 알아오자
        Player currentPlayer = playerRepository.findById(searchDto.getPlayerId()).orElseThrow(() -> new NotFoundException(FailCode.NO_REPLACED_PLAYER));
        Position mainPosition = currentPlayer.getMainPosition();
        List<Player> players = allPlayers.stream().filter(player ->
                        (player.getMainPosition() == Position.TWO_WAY_PLAYER || player.getMainPosition() == mainPosition))
                .collect(Collectors.toList());

        List<SeasonRoster> seasonRosters = queryFactory
                .select(seasonRoster)
                .from(seasonRoster)
                .where(
                        teamIdEq(searchDto.getTeamId()),
                        seasonEq(searchDto.getSeason())
                )
                .fetch();

        for (SeasonRoster seasonRoster : seasonRosters) {
            System.out.println("seasonRoster.getPlayer().getName() = " + seasonRoster.getPlayer().getName());
        }

        // 입력된 시즌에 해당 팀의 SeasonRoster가 존재하지 않으면 예외 발생
        if(seasonRosters == null || seasonRosters.isEmpty()) {
            throw new NotFoundException(FailCode.NO_SEASONROSTER);
        }

        // seasonRoster의 id와 같고
        // mainPosition과 포지션이 같은 선수만 남겨주자
        List<SeasonRoster> matchedPlayers = seasonRosters.stream()
                .filter(seasonRoster -> players.stream()
                        .anyMatch(player -> player.getId().equals(seasonRoster.getPlayer().getId())))
                .collect(Collectors.toList());

        // seasonRosters와 players에 겹치는 선수가 없다면 예외 발생
        if(matchedPlayers.isEmpty()) {
            throw new NotFoundException(FailCode.NO_RESULTS);
        }

        // 이제 matchedPlayers에서
        // MainPosiotion이 Pitcher면 PitcherSearchRespDto로
        // PITCHER 이외의 포지션이면 OtherPositionSearchRespDto로
        // 변환해서 PlayerSearchRespDto의 List에 포지션 별로 나눠서 담아주자
        PlayerSearchRespDto result = new PlayerSearchRespDto();

        for(int idx = 0; idx < matchedPlayers.size(); idx++) {
            SeasonRoster matchedPlayer = matchedPlayers.get(idx);
            if(matchedPlayer.getPlayer().getMainPosition() == Position.TWO_WAY_PLAYER) {
                PitchingRespDto pitching = pitchingService.getPitchingStat2(new PitchingReqDto(matchedPlayer.getPlayer().getId(), searchDto.getSeason()));
                HittingRespDto hitting = hittingService.getHittingStat2(new HittingReqDto(matchedPlayer.getPlayer().getId(), searchDto.getSeason()));

                if(pitching == null || hitting == null) {
                    continue;
                }

                TwoWayRespDto twoWay = new TwoWayRespDto(matchedPlayer.getPlayer(), pitching, hitting);
                result.addTwoWay(twoWay);
            }
            else if(matchedPlayer.getPlayer().getMainPosition() == Position.PITCHER) {
                PitchingRespDto pitching = pitchingService.getPitchingStat2(new PitchingReqDto(matchedPlayer.getPlayer().getId(), searchDto.getSeason()));

                if(pitching == null) {
                    continue;
                }

                PitcherSearchRespDto pitcher = new PitcherSearchRespDto(matchedPlayer.getPlayer(), pitching);
                result.addPitcher(pitcher);
            }
            else {
                HittingRespDto hitting = hittingService.getHittingStat2(new HittingReqDto(matchedPlayer.getPlayer().getId(), searchDto.getSeason()));

                if(hitting == null) {
                    continue;
                }

                OtherPositionSearchRespDto other = new OtherPositionSearchRespDto(matchedPlayer.getPlayer(), hitting);
                result.addOthers(other);
            }
        }

        return result;
    }

    private BooleanExpression seasonEq(int inputSeason) {
        return seasonRoster.season.eq(inputSeason);
    }

    private BooleanExpression teamIdEq(Long teamId) {
        return teamId != null ? seasonRoster.team.id.eq(teamId) : null;
    }
}
