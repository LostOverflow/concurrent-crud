package sportradar.demo.football;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD Test plan:
 *
 * <p>
 * API OPERATION:
 * signature: startNewMatch(String homeTeam, String awayTeam)
 * desc: Starts a new match, assuming initial score 0(Home team) – 0(Away team) and adding it the scoreboard.
 * <p>
 *
 * <p>
 * TEST CASES:
 *
 * <p>
 * name     : Empty board. Invalid team names: input names are duplicated
 * desc     : Adding a new match when NO ONE other match started before, but passing two SAME NAMES for two teams
 * verify   : validation exception has thrown: DuplicatedTeamNamesException
 * <p>
 *
 * <p>
 * name: Empty board. Invalid team names: Home team name is too long
 * desc: Adding a new match when NO ONE other match started before, but passing home team name longer than max limited
 * verify: validation exception has thrown: TeamNameOverflowException
 * <p>
 *
 * <p>
 * the same case but vice versa with the Away team
 * <p>
 *
 * <p>
 * the same case but both team names are too long
 * <p>
 *
 * <p>
 * name: Non Empty Board. HOME team is already playing
 * desc: trying to add a new match when HOME team is already playing in other match
 * verify: validation exception has thrown: TeamAlreadyPlayingException
 * <p>
 *
 * <p>
 * name: Non Empty Board. AWAY team is already playing
 * desc: trying to add a new match when AWAY team is already playing in OTHER match
 * verify: validation exception has thrown: TeamAlreadyPlayingException
 * <p>
 *
 * <p>
 * name: Duplicated Start for the same match
 * desc: trying to add a new match when AWAY team is already playing in OTHER match
 * verify: validation exception has thrown: TeamAlreadyPlayingException
 * <p>
 *
 * <p>
 * TODO good to have test case:
 *  According to the task description we have to 'remember' the ordering of started matches.
 *  Concurrency challenge:
 *  Let's assume that:
 *      * matches could start and finish
 *      * matches could be extended for a different period of time
 *      * the same match (the same teams) could have such changes history log:
 *          started -> updated -> removed -> started (why not?)
 *  To make sure that getSummary operation is returning correct ordered matches
 *  we have to be able of making snapshots of match's ordered list in thread safe manner
 * </p>
 * <p>
 * API OPERATION:
 * <p>
 * signature: updateScore(int homeTeamScore, int awayTeamScore)
 * desc: Receives a pair of absolute scores: home team score and away team score
 * and updates it appropriately
 * <p>
 * TEST CASES:
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
 * Note:
 * I have spent some time thinking on how to validate Update scores. But some lack of football rules:
 * could be the scores updated more than +/-1 at one time? Could it be reset to 0/0 if any emergency happened?
 * Decided not to apply any validation for update yet
 * <p>
 * API OPERATION:
 * signature: finishMatch(String homeTeam, String awayTeam)
 * desc: Receives a pair of home and away teams and Finishes match currently in progress.
 * Removes match from the scoreboard.
 * <p>
 * TEST CASES:
 * <p>
 * Note: "removing from" operation could be implemented in both ways:
 * * throw an exception if search item was not there
 * * ignore all if search item was not there
 * If we want to keep idempotency during repeated operations - then should ignore
 * <p>
 * I prefer to throw an exception, that is more informative
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
 * TODO could add 'double removing the same match' test case
 * <p>
 * name: 'Remove From multi match board'
 * desc: Removes dedicated match from multi match board
 * make sure: board is empty (getMatches should return empty list)
 * invoke: start match A and verify it
 * invoke: start match B and verify it
 * invoke: remove match A, verify match B is only on the board
 * <p>
 * TODO good to have test case for UPDATE operation:
 *  Let's assume scoreboard could be updated from different places of the world.
 *  That means f.e. 'UpdateScore' requests during the trip from client to server
 *  could achieve different time delays.
 *  And That means 'the server', who is in charge of request's processing
 *  could receive them in wrong order.
 *   For example:
 *   Original score order happened at match: 0 - 0, 0 - 1, 1 - 1
 *   Score updates ordering at server  side: 0 - 1, 1 - 1, 0 - 0
 *   Final score state for that match become incorrect and could stay in it for a long time.
 * <p>
 *  API OPERATION:
 *  signature: List<CurrentMatch> getMatches(String homeTeam, String awayTeam)
 *  desc: Receives a pair of home and away teams and Finishes match currently in progress.
 *  Removes match from the scoreboard.
 * <p>
 *  TODO add setSummary taking in account ordering of matches being added
 * <p>
 */
public class FootballScoreboardApplicationTests {

    private static FootballScoreboard scoreboard;

    @BeforeAll
    static void contextLoads() {
        scoreboard = FootballScoreboardImpl.getInstance();
    }

    /*
     I decided do not clear the map (used as in-memory storage) BEFORE each test but only check if it's empty.
     If map is not empty then probably:
        * It was not cleaned AFTER the test
        * Some other parallel test is running and cache map is in usage.
     I prefer to raise an exception to notify developer that is something wrong
     */
    @BeforeEach
    void checkMatchesClear() {
        assert scoreboard.getSummary().isEmpty();
    }

    @AfterEach
    void clearInMemoryCache() {
        scoreboard.clearAllMatches();
        assert scoreboard.getSummary().isEmpty();
    }

    @Test
        /*
         * name      : Empty board. Valid team names
         * desc      : Adding a new match when NO ONE other match started before
         * make sure : board is empty (getMatches should return empty list)
         * invoke    : add new match
         * verify    : Dashboard should show only added single match with exact same team names and order
         *             (Home team is always left)
         *             Match's sequence number should be incremented
         */
    void testStartMatch_EmptyBoard_ValidNames() {
        var teamA = "teamA";
        var teamB = "teamB";
        scoreboard.startNewMatch(teamA, teamB);
        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(1, matchesSize, "Not a single match started!");
        var match = matches.get(0);
        assertEquals(teamA, match.getHomeTeamName());
        assertEquals(teamB, match.getAwayTeamName());
        assertTrue(match.getStartSequence() > 0, "Start sequence has to be incremented!");
    }

}
