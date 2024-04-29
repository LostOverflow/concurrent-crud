package sportradar.demo.football.validator;

import org.springframework.util.StringUtils;
import sportradar.demo.football.ex.EmptyTeamNameException;
import sportradar.demo.football.ex.InvalidScoreException;
import sportradar.demo.football.ex.TeamNameOverflowException;

/**
 * This is kind of static validator which is taking care about parameters valid value.
 * But not check for unique commands playing runtime.
 * Such kind of checking happened in Scoreboard impl during creation, updating or deleting
 * -/-
 * Assuming this validator will cover business requirements of SportRadar
 * Some other validation rules could be applied with different MatchValidator impls
 */
public class SportRadarMatchValidator implements MatchValidator {

    public static final Integer MAX_TEAM_NAME = 256; // just my opinion rule, could be different

    @Override
    public void validateNewMatch(String homeTeam, String awayTeam) {
        validateTeamNames(homeTeam, awayTeam);
    }

    @Override
    public void validateUpdateMatch(String homeName, String awayName, int homeScore, int awayScore) {
        if (homeScore < 0) {
            throw new InvalidScoreException("HOME SCORE has negative value: " + homeScore);
        }
        if (awayScore < 0) {
            throw new InvalidScoreException("AWAY SCORE has negative value: " + awayScore);
        }
    }

    @Override
    public void validateDeleteMatch(String homeTeam, String awayTeam) {
        validateTeamNames(homeTeam, awayTeam);
    }

    private void validateTeamNames(String homeTeam, String awayTeam) {
        if (!StringUtils.hasLength(homeTeam)) {
            throw new EmptyTeamNameException("HOME team name is empty: [" + (homeTeam == null ? "null" : homeTeam));
        }
        if (!StringUtils.hasLength(awayTeam)) {
            throw new EmptyTeamNameException("AWAY team name is empty: [" + (awayTeam == null ? "null" : awayTeam));
        }
        if (homeTeam.length() > MAX_TEAM_NAME) {
            throw new TeamNameOverflowException("HOME team name overflow for input length: " + homeTeam.length());
        }
        if (awayTeam.length() > MAX_TEAM_NAME) {
            throw new TeamNameOverflowException("AWAY team name overflow for input length: " + awayTeam.length());
        }
    }

}
