package sportradar.demo.football;

import sportradar.demo.football.dto.CurrentMatch;

import java.util.List;

// TODO take care of java docs!
public interface FootballScoreboard {
    void startNewMatch(String homeTeam, String awayTeam);

    void updateMatchScore(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore);

    void removeMatch(String homeTeam, String awayTeam);

    List<CurrentMatch> getSummary();

    // for test purples
    void clearAllMatches();

    // TODO implement print() method if required
}
