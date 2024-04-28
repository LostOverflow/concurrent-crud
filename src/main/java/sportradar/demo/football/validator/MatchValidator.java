package sportradar.demo.football.validator;

/**
 * Implementing Strategy pattern for Matches being added, updated, deleted Validating
 */
public interface MatchValidator {
    void validateNewMatch(String homeName, String awayName);

    void validateUpdateMatch(String homeName, String awayName, int homeScore, int awayScore);

    void validateDeleteMatch(String homeName, String awayName);
}
