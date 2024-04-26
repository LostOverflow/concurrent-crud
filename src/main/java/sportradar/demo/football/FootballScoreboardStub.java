package sportradar.demo.football;

import sportradar.demo.football.dto.CurrentMatch;

import java.util.List;

// TODO remove this class on final check if need not at all
public class FootballScoreboardStub implements FootballScoreboard {
    @Override
    public void startNewMatch(String homeTeam, String awayTeam) {

    }

    @Override
    public void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore) {

    }

    @Override
    public void removeMatch(String homeTeam, String awayTeam) {

    }

    @Override
    public List<CurrentMatch> getSummary() {
        return List.of();
    }
}
