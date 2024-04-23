package sportradar.demo.football;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * TDD Test plan:
 * <p>
 * API:
 * signature: startNewMatch(String homeTeam, String awayTeam)
 * desc: Starts a new match, assuming initial score 0(Home team) – 0(Away team) and adding it the scoreboard.
 * <p>
 * Test cases:
 * name: 'Empty board. Valid team names'
 * desc: Adding a new match when NO ONE other match started before
 * make sure: board is empty (getMatches should return empty list)
 * invoke: add new match
 * verify: only single invocation was performed on dashboard for method: Start a new match
 * dashboard should show only added single match with exact same team names and order (Home team is always left)
 * <p>
 * name: 'Empty board. Invalid team names: input names are duplicated'
 * desc: Adding a new match when NO ONE other match started before, but passing two SAME NAMES for two teams
 * verify: validation exception has thrown: DuplicatedTeamNamesException
 * <p>
 * name: 'Empty board. Invalid team names: Home team name is too long'
 * desc: Adding a new match when NO ONE other match started before, but passing home team name longer than max limited
 * verify: validation exception has thrown: TeamNameOverflowException
 * <p>
 * the same case but vice versa with the Away team
 * <p>
 * the same case but both team names are too long
 * <p>
 * name: Non Empty Board. Home team is already playing
 * desc: trying to add a new match when HOME team is already playing in other match
 * verify: validation exception has thrown: TeamAlreadyPlayingException
 * <p>
 * name: Non Empty Board. Away team is already playing
 * desc: trying to add a new match when AWAY team is already playing in OTHER match
 * verify: validation exception has thrown: TeamAlreadyPlayingException
 * <p>
 * name: Duplicated Start for the same match
 * desc: trying to add a new match when AWAY team is already playing in OTHER match
 * verify: validation exception has thrown: TeamAlreadyPlayingException
 * <p>
 * API:
 * signature: updateScore(int homeTeamScore, int awayTeamScore)
 * desc: Receives a pair of absolute scores: home team score and away team score
 * and updates it appropriately
 * <p>
 * Test cases:
 * name: 'Update Empty board'
 * desc: Trying to update team[s] which are NOT playing now
 * verify: MatchNotStartedException
 * <p>
 * name: 'Update Single Match Board'
 * desc: Update scores on a Dashboard with single match on it
 * make sure: board is empty (getMatches should return empty list)
 * invoke: start new match
 * verify: single match on dashboard, scores are 0-0
 * invoke: update board with the new scores
 * verify: single match on dashboard, new scores are equal to new ones
 * <p>
 * name: 'Update Multi Match Board'
 * desc: Update scores on a Dashboard when multiple matches on it
 * make sure: board is empty (getMatches should return empty list)
 * invoke: start new match A and verify it
 * invoke: start new match B and verify it
 * invoke: update match A
 * verify: match A updated with the new score
 * invoke: update match B
 * verify: match B updated with the new score
 * <p>
 *     Note:
 * I have spent some time thinking on how to validate Update scores. But some lack of football rules:
 * could be the scores updated more than +/-1 at one time? Could it be reset to 0/0 if any emergency happened?
 * Decided not to apply any validation for update yet
 * <p>
 *  API:
 *  signature: finishMatch(String homeTeam, String awayTeam)
 *  desc: Receives a pair of home and away teams and Finishes match currently in progress.
 *  Removes match from the scoreboard.
 * <p>
 * Test cases:
 *
 *  Note: "removing from" operation could be implemented in both ways:
 *      * throw an exception if search item was not there
 *      * ignore all if search item was not there
 *      If we want to keep idempotency during repeated operations - then should ignore
 *
 *     I prefer to throw an exception, that is more informative
 * <p>
 * name: 'Remove last match'
 * desc: Removes last match from dashboard
 * make sure: board is empty (getMatches should return empty list)
 * invoke: start any new match and verify it
 * invoke: finishMatch which was created before
 * verify: board is empty (getMatches should return empty list)
 * <p>
 * name: 'Remove From empty board'
 * desc: Tries to remove match from empty board
 * make sure: exception has throws: MatchNotStartedException(String homeTeam, String awayTeam)
 * <p>
 * TODO could add double removing the same match test case
 * <p>
 * name: 'Remove From multi match board'
 * desc: Removes dedicated match from multi match board
 * make sure: board is empty (getMatches should return empty list)
 * invoke: start match A and verify it
 * invoke: start match B and verify it
 * invoke: remove match A, verify match B is only on the board
 * <p>
 *
 * <p>
 *  API:
 *  signature: List<CurrentMatch> getMatches(String homeTeam, String awayTeam)
 *  desc: Receives a pair of home and away teams and Finishes match currently in progress.
 *  Removes match from the scoreboard.
 * <p>
 *
 */
@SpringBootTest
class FootballScoreboardApplicationTests {

    @Test
    void contextLoads() {
    }

}
