package sportradar.demo.football;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sportradar.demo.football.ex.MatchNotStartedException;
import sportradar.demo.football.ex.TeamAlreadyPlayingException;
import sportradar.demo.football.ex.TeamNameOverflowException;

import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static sportradar.demo.football.validator.SportRadarMatchValidator.MAX_TEAM_NAME;

/**
 * TDD Test plan:
 *
 *
 * <p>
 * API OPERATION:
 * signature: List<CurrentMatch> getMatches(String homeTeam, String awayTeam)
 * desc: Receives a pair of home and away teams and Finishes match currently in progress.
 * Removes match from the scoreboard.
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
        assertEquals(teamA, match.getHomeTeam());
        assertEquals(teamB, match.getAwayTeam());
        assertTrue(match.getStartSequence() > 0, "Start sequence has to be incremented!");
    }

    /*
     * <p>
     * name     : Empty board. Invalid team names: input names are duplicated
     * desc     : Adding a new match when NO ONE other match started before, but passing two SAME NAMES for two teams
     * verify   : validation exception has thrown: DuplicatedTeamNamesException
     *            scoreboard state should NOT be changed!
     * <p>
     */
    @Test
    void testStartMatch_EmptyBoard_NamesDuplicated() {
        var teamA = "teamA";
        assertThrows(TeamAlreadyPlayingException.class, () -> scoreboard.startNewMatch(teamA, teamA));
        assertTrue(
                scoreboard.getSummary().isEmpty(),
                "Scoreboard state has changed during duplicated teams being added"
        );
    }

    /*
     * <p>
     * name  : Empty board. Invalid team names: Home team name is too long
     * desc  : Adding a new match when NO ONE other match started before, but passing home team name longer than max limited
     * verify: validation exception has thrown: TeamNameOverflowException
     *         scoreboard state should NOT be changed!
     * <p>
     */
    @Test
    void testStartMatch_EmptyBoard_LongHomeName() {
        var longTeamName = randomAlphabetic(MAX_TEAM_NAME + 1);
        assertThrows(TeamNameOverflowException.class, () -> scoreboard.startNewMatch(longTeamName, "teamB"));
        assertTrue(
                scoreboard.getSummary().isEmpty(),
                "Scoreboard state has changed during invalid team name being added!"
        );
    }

    /*
     * <p>
     * name  : Empty board. Invalid team names: Home team name is too long
     * desc  : Adding a new match when NO ONE other match started before, but passing home team name longer than max limited
     * verify: validation exception has thrown: TeamNameOverflowException
     *         scoreboard state should NOT be changed!
     * <p>
     */
    @Test
    void testStartMatch_EmptyBoard_LongAwayName() {
        var longTeamName = randomAlphabetic(MAX_TEAM_NAME + 1);
        assertThrows(TeamNameOverflowException.class, () -> scoreboard.startNewMatch("teamA", longTeamName));
        assertTrue(
                scoreboard.getSummary().isEmpty(),
                "Scoreboard state has changed during invalid team name being added!"
        );
    }

    /*
     * <p>
     * name  : Empty board. Invalid team names: both team names are too long
     * desc  : Adding a new match when NO ONE other match started before,
     *         but passing both team names longer than max limited
     * verify: validation exception has thrown: TeamNameOverflowException
     *         scoreboard state should NOT be changed!
     * <p>
     */
    @Test
    void testStartMatch_EmptyBoard_LongBothNames() {
        var teamA = randomAlphabetic(MAX_TEAM_NAME + 1);
        var teamB = randomAlphabetic(MAX_TEAM_NAME + 1);
        assertThrows(TeamNameOverflowException.class, () -> scoreboard.startNewMatch(teamA, teamB));
        assertTrue(
                scoreboard.getSummary().isEmpty(),
                "Scoreboard state has changed during invalid team name being added!"
        );
    }

    /*
     * <p>
     * name  : Non-Empty Board. HOME team is already playing
     * desc  : trying to add a new match when HOME team is already playing in other match
     * verify: validation exception has thrown: TeamAlreadyPlayingException
     *         scoreboard state should NOT be changed!
     * <p>
     */
    @Test
    // I prefer to keep method names short and descriptive but NOT for test methods...
    // it has to describe not only api name is testing but also input conditions
    void testStartMatch_NonEmptyBoard_HomeTeamAlreadyPlaying() {
        var homeTeam = "HomeTeam";
        var awayTeam = "AwayTeam";
        var otherTeam = "OtherTeam";
        scoreboard.startNewMatch(homeTeam, otherTeam);
        assertThrows(TeamAlreadyPlayingException.class, () -> scoreboard.startNewMatch(homeTeam, awayTeam));

        // check for previous state is the same
        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(1, matchesSize, "Started match should be single!");
        var match = matches.get(0);
        assertEquals(homeTeam, match.getHomeTeam());
        assertEquals(otherTeam, match.getAwayTeam());
        // TODO Consider if to test startSequence separately (own test)
    }

    /*
     * <p>
     * name  : Non-Empty Board. AWAY team is already playing
     * desc  : trying to add a new match when AWAY team is already playing in other match
     * verify: validation exception has thrown: TeamAlreadyPlayingException
     *         scoreboard state should NOT be changed!
     * <p>
     */
    @Test
    void testStartMatch_NonEmptyBoard_AwayTeamAlreadyPlaying() {
        var homeTeamName = "HomeTeam";
        var awayTeamName = "AwayTeam";
        var otherTeamName = "OtherTeam";
        scoreboard.startNewMatch(otherTeamName, awayTeamName);
        assertThrows(TeamAlreadyPlayingException.class, () -> scoreboard.startNewMatch(homeTeamName, awayTeamName));

        // check for previous state is the same
        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(1, matchesSize, "Started match should be single!");
        var match = matches.get(0);
        assertEquals(awayTeamName, match.getAwayTeam());
        assertEquals(otherTeamName, match.getHomeTeam());
    }

    /*
     * <p>
     * name  : Duplicated Match started
     * desc  : trying to add the same Match which has already started
     * verify: validation exception has thrown: TeamAlreadyPlayingException
     *         scoreboard state should NOT be changed!
     * <p>
     */
    @Test
    void testStartMatch_DuplicatedMatch() {
        var homeTeamName = "HomeTeam";
        var awayTeamName = "AwayTeam";

        scoreboard.startNewMatch(homeTeamName, awayTeamName);
        assertThrows(TeamAlreadyPlayingException.class, () -> scoreboard.startNewMatch(homeTeamName, awayTeamName));

        // check for previous state is the same
        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(1, matchesSize, "Started match should be single!");
        var match = matches.get(0);
        assertEquals(homeTeamName, match.getHomeTeam());
        assertEquals(awayTeamName, match.getAwayTeam());
    }

    // tests for checking correct order of Matches added going to cover in getSummary method tests

    /*
     * <p>
     * name: 'Update Empty board'
     * desc: Trying to update team[s] which are NOT playing now
     * verify: MatchNotStartedException
     * <p>
     */
    @Test
    public void testUpdate_EmptyBoard() {
        var homeTeam = "HomeTeam";
        var awayTeam = "AwayTeam";
        var newHomeScore = 1;
        var newAwayScore = 1;

        assertThrows(
                MatchNotStartedException.class,
                () -> scoreboard.updateMatchScore(homeTeam, awayTeam, newHomeScore, newAwayScore)
        );
    }

    /*
     * name: 'Update Single Match Board'
     * desc: Update scores on a Dashboard with single match on it
     * make sure: board is empty (getMatches should return empty list)
     * invoke: start new match
     * verify: single match on dashboard, scores are 0-0
     * invoke: update board with the new scores
     * verify: single match on dashboard, new scores are equal to new ones
     */
    @Test
    public void testUpdate_SingleMatch() {
        var homeTeam = "HomeTeam";
        var awayTeam = "AwayTeam";
        scoreboard.startNewMatch(homeTeam, awayTeam);

        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(1, matchesSize, "Not a single match started!");

        var match = matches.get(0);
        assertEquals(homeTeam, match.getHomeTeam());
        assertEquals(awayTeam, match.getAwayTeam());

        // verify the same value after update
        var startSeq = match.getStartSequence();

        var newHomeScore = 1;
        var newAwayScore = 1;

        scoreboard.updateMatchScore(homeTeam, awayTeam, newHomeScore, newAwayScore);

        matches = scoreboard.getSummary();
        matchesSize = matches.size();
        assertEquals(1, matchesSize, "Not a single match started!");

        match = matches.get(0);
        assertEquals(homeTeam, match.getHomeTeam());
        assertEquals(awayTeam, match.getAwayTeam());
        assertEquals(newHomeScore, match.getHomeScore());
        assertEquals(newAwayScore, match.getAwayScore());
        assertEquals(startSeq, match.getStartSequence());
    }

    /*
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
     * Decided to apply validation only for positive values
     * <p>
     */
    @Test
    public void testUpdate_MultiMatch() {
        var homeTeamA = "homeTeamA";
        var awayTeamA = "awayTeamA";

        var homeTeamB = "homeTeamB";
        var awayTeamB = "awayTeamB";

        scoreboard.startNewMatch(homeTeamA, awayTeamA);
        scoreboard.startNewMatch(homeTeamB, awayTeamB);

        // Matches should be returned in reversed order of adding it.
        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(2, matchesSize);

        // verify zero scores after matches added.
        // matchA became last
        var matchA = matches.get(1);
        assertEquals(0, matchA.getHomeScore());
        assertEquals(0, matchA.getAwayScore());

        // matchB became first
        var matchB = matches.get(0);
        assertEquals(0, matchB.getHomeScore());
        assertEquals(0, matchB.getAwayScore());

        var newHomeScoreA = 1;
        var newAwayScoreA = 2;
        var newHomeScoreB = 3;
        var newAwayScoreB = 4;

        scoreboard.updateMatchScore(homeTeamA, awayTeamA, newHomeScoreA, newAwayScoreA);
        scoreboard.updateMatchScore(homeTeamB, awayTeamB, newHomeScoreB, newAwayScoreB);

        // matches order should be inverse again due to teamB having bigger total scores now
        matches = scoreboard.getSummary();

        matchB = matches.get(0);
        matchA = matches.get(1);

        assertEquals(newHomeScoreA, matchA.getHomeScore());
        assertEquals(newAwayScoreA, matchA.getAwayScore());

        assertEquals(newHomeScoreB, matchB.getHomeScore());
        assertEquals(newAwayScoreB, matchB.getAwayScore());
    }

    /*
     * name: 'Remove single match'
     * desc: Removes single match from dashboard
     * make sure: board is empty (getMatches should return empty list)
     * invoke: start any new match and verify it
     * invoke: finishMatch which was created before
     * verify: board is empty (getMatches should return empty list)
     *
     * Note: "removing from" operation could be implemented in both ways:
     * * throw an exception if search item was not there
     * * ignore all if search item was not there
     * If we want to keep idempotency during repeated operations - then should ignore
     * I prefer to throw an exception, that is more informative
     */
    @Test
    public void testRemoveMatch_SingleMatchBoard() {
        var homeTeam = "homeTeam";
        var awayTeam = "awayTeam";

        scoreboard.startNewMatch(homeTeam, awayTeam);
        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(1, matchesSize, "Started match should be single!");
        var match = matches.get(0);
        assertEquals(homeTeam, match.getHomeTeam());
        assertEquals(awayTeam, match.getAwayTeam());

        scoreboard.removeMatch(homeTeam, awayTeam);
        matches = scoreboard.getSummary();
        assertEquals(0, matches.size());
    }

    /*
     * name: 'Remove From multi match board'
     * desc: Removes dedicated match from multi match board
     * make sure: board is empty (getMatches should return empty list)
     * invoke: start match A and verify it
     * invoke: start match B and verify it
     * invoke: remove match A, verify match B is only on the board
     */
    @Test
    public void testRemoveMatch_MultiBoard() {
        var homeTeamA = "homeTeamA";
        var awayTeamA = "awayTeamA";
        //
        var homeTeamB = "homeTeamB";
        var awayTeamB = "awayTeamB";

        scoreboard.startNewMatch(homeTeamA, awayTeamA);
        scoreboard.startNewMatch(homeTeamB, awayTeamB);
        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(2, matchesSize);

        scoreboard.removeMatch(homeTeamA, awayTeamA);
        matches = scoreboard.getSummary();
        assertEquals(1, matches.size());

        var matchB = matches.get(0);

        assertEquals(homeTeamB, matchB.getHomeTeam());
        assertEquals(awayTeamB, matchB.getAwayTeam());
    }

    /*
     * name: 'Remove match duplicated'
     * desc: Trying to removes single match from dashboard twice
     * make sure: board is empty (getMatches should return empty list)
     * invoke: start any new match and verify it
     * invoke: finishMatch which was created before
     * invoke: finishMatch which was created before AGAIN
     * verify: MatchNotStartedException has throws
     */
    @Test
    public void testRemoveMatch_DuplicatedRemove() {
        // TODO This peace of code (create single match) duplicated multiple times, consider extract method
        var homeTeam = "homeTeam";
        var awayTeam = "awayTeam";

        scoreboard.startNewMatch(homeTeam, awayTeam);
        var matches = scoreboard.getSummary();
        var matchesSize = matches.size();
        assertEquals(1, matchesSize, "Started match should be single!");
        var match = matches.get(0);
        assertEquals(homeTeam, match.getHomeTeam());
        assertEquals(awayTeam, match.getAwayTeam());

        scoreboard.removeMatch(homeTeam, awayTeam);
        matches = scoreboard.getSummary();
        assertEquals(0, matches.size());

        assertThrows(MatchNotStartedException.class, () -> scoreboard.removeMatch(homeTeam, awayTeam));
    }

    /*
     * name: 'Remove From empty board'
     * desc: Tries to remove match from empty board
     * make sure: exception has throws: MatchNotStartedException(String homeTeam, String awayTeam)
     */
    @Test
    public void testRemoveMatch_EmptyBoard() {
        assertThrows(MatchNotStartedException.class, () -> scoreboard.removeMatch("teamA", "teamB"));
        assertTrue(scoreboard.getSummary().isEmpty());
    }

    /*
        For example, if following matches are started in the specified order and their scores respectively updated:
            a. Mexico 0     - Canada 5
            b. Spain 10     - Brazil 2
            c. Germany 2    - France 2
            d. Uruguay 6    - Italy 6
            e. Argentina 3  - Australia 1
        The summary should be as follows:
            1. Uruguay 6    - Italy 6
            2. Spain 10     - Brazil 2
            3. Mexico 0     - Canada 5
            4. Argentina 3  - Australia 1
            5. Germany 2    - France 2
     */
    @Test
    public void testGetSummary_DemoExample() {
        scoreboard.startNewMatch("Mexico", "Canada");
        scoreboard.startNewMatch("Spain", "Brazil");
        scoreboard.startNewMatch("Germany", "France");
        scoreboard.startNewMatch("Uruguay", "Italy");
        scoreboard.startNewMatch("Argentina", "Australia");

        scoreboard.updateMatchScore("Mexico", "Canada", 0, 5);
        scoreboard.updateMatchScore("Spain", "Brazil", 10, 2);
        scoreboard.updateMatchScore("Germany", "France", 2, 2);
        scoreboard.updateMatchScore("Uruguay", "Italy", 6, 6);
        scoreboard.updateMatchScore("Argentina", "Australia", 3, 1);

        var summary = scoreboard.getSummary();
        summary.forEach(System.out::println);

        assertEquals(5, summary.size());

        IntStream.range(0, summary.size())
                .forEach(order -> {
                    var match = summary.get(order);
                    switch (order) {
                        case 0: {
                            assertEquals("Uruguay", match.getHomeTeam());
                            assertEquals("Italy", match.getAwayTeam());
                            break;
                        }
                        case 1: {
                            assertEquals("Spain", match.getHomeTeam());
                            assertEquals("Brazil", match.getAwayTeam());
                            break;
                        }
                        case 2: {
                            assertEquals("Mexico", match.getHomeTeam());
                            assertEquals("Canada", match.getAwayTeam());
                            break;
                        }
                        case 3: {
                            assertEquals("Argentina", match.getHomeTeam());
                            assertEquals("Australia", match.getAwayTeam());
                            break;
                        }
                        case 4: {
                            assertEquals("Germany", match.getHomeTeam());
                            assertEquals("France", match.getAwayTeam());
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("Test has written for 5 matches");
                    }
                });
    }

    /*
     * TODO good to have test case for UPDATE operation:
     *  Let's assume scoreboard could be updated from different places of the world.
     *  That means f.e. 'UpdateScore' requests during the trip from client to server
     *  could achieve different time delays.
     *  And That means 'the server', who is in charge of request's processing
     *  could receive them in wrong order.
     *  For example:
     *   Original score updates: 0 - 0, 0 - 1, 1 - 1
     *   Score updates at server: 0 - 1, 1 - 1, 0 - 0
     *   Final score state for that match become incorrect and could stay in it for a long time.
     *
     *  Obviously it's unsafe to allow UPDATE in multi-thread environment
     *  If to assume that only one score change could happen at one match withing one second,
     *  and extend API interface for UPDATE operation with match's current time
     *  then it could be a chance to apply only latest UPDATES but ignore 'past' ones.
     */

}
