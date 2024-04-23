package sportradar.demo.football;

import sportradar.demo.football.dto.CurrentMatch;

import java.util.List;

public class FootballDashboardStub implements FootballDashboard {
    @Override
    public void startNewMatch(String homeTeam, String awayTeam) {

    }

    @Override
    public void updateScore(int homeTeamScore, int awayTeamScore) {

    }

    @Override
    public void removeMatch(String homeTeam, String awayTeam) {

    }

    @Override
    public List<CurrentMatch> getSummary() {
        return List.of();
    }
}
