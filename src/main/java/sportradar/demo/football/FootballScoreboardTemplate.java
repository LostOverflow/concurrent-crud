package sportradar.demo.football;

import sportradar.demo.football.validator.MatchValidator;

public abstract class FootballScoreboardTemplate implements FootballScoreboard {
    private final MatchValidator matchValidator;

    public FootballScoreboardTemplate(MatchValidator matchValidator) {
        this.matchValidator = matchValidator;
    }

    @Override
    // Deprecated to Override startNewMatch without validation!
    public final void startNewMatch(String homeTeam, String awayTeam) {
        matchValidator.validateNewMatch(homeTeam, awayTeam);
        doStartNewMatch(homeTeam, awayTeam);
    }

    public abstract void doStartNewMatch(String homeTeam, String awayTeam);

    @Override
    public final void updateMatchScore(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore) {
        matchValidator.validateUpdateMatch(homeTeam, awayTeam, homeTeamScore, awayTeamScore);
        doUpdateMatchScore(homeTeam, awayTeam, homeTeamScore, awayTeamScore);
    }

    public abstract void doUpdateMatchScore(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore);

    @Override
    public final void removeMatch(String homeTeam, String awayTeam) {
        matchValidator.validateDeleteMatch(homeTeam, awayTeam);
        doRemoveMatch(homeTeam, awayTeam);
    }

    public abstract void doRemoveMatch(String homeTeam, String awayTeam);

}
