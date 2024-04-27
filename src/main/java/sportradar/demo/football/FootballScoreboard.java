package sportradar.demo.football;

import sportradar.demo.football.dto.CurrentMatch;

import java.util.List;

// TODO take care of java docs!
public interface FootballScoreboard {
    void startNewMatch(String homeTeam, String awayTeam);

    void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore);

    void removeMatch(String homeTeam, String awayTeam);

    List<CurrentMatch> getSummary();

    void clearAllMatches();
}
