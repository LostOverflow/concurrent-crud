package sportradar.demo.football.validator;

import org.springframework.util.StringUtils;
import sportradar.demo.football.ex.EmptyTeamNameException;
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
    public void validateNewMatch(String homeTeamName, String awayTeamName) {
        if (!StringUtils.hasLength(homeTeamName)) {
            throw new EmptyTeamNameException("HOME team name is empty: [" + (homeTeamName == null ? "null" : homeTeamName));
        }
        if (!StringUtils.hasLength(awayTeamName)) {
            throw new EmptyTeamNameException("AWAY team name is empty: [" + (awayTeamName == null ? "null" : awayTeamName));
        }
        if (homeTeamName.length() > MAX_TEAM_NAME) {
            throw new TeamNameOverflowException("HOME team name overflow for input length: " + homeTeamName.length());
        }
        if (awayTeamName.length() > MAX_TEAM_NAME) {
            throw new TeamNameOverflowException("AWAY team name overflow for input length: " + awayTeamName.length());
        }
    }

    @Override
    public void validateUpdateMatch(String homeName, String awayName, int homeScore, int awayScore) {
        // TODO add validation
    }

    @Override
    public void validateDeleteMatch(String homeName, String awayName) {
        // TODO add validation
    }
}
