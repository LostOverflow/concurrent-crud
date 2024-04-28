// always keeping eyes on top right mark in IDEA editor to understand whether 'someone' took care about warnings    --->
package sportradar.demo.football;

import lombok.Getter;
import sportradar.demo.football.dto.CurrentMatch;
import sportradar.demo.football.ex.TeamAlreadyPlayingException;
import sportradar.demo.football.validator.MatchValidator;
import sportradar.demo.football.validator.SportRadarMatchValidator;

import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;

/**
 * Let's use Scoreboard Implementation as a Singleton to prevent data storage duplication, etc.
 */
public class FootballScoreboardImpl extends FootballScoreboardTemplate {

    @Getter
    private static final FootballScoreboard instance = new FootballScoreboardImpl(new SportRadarMatchValidator());

    // SortedMap will take care about checking for unique and ordered items.
    // Actually... I am going to use only single team name as a Key!
    // Why:
    // * We have to check uniqueness of each separate team
    // * Keeping concatenated team names (homeTeamName + awayTeamName) as a key
    //   will NOT guarantee unique check of separate team names
    //
    // Key is a String for team name (both home and away teams are stored separately)
    // Value is CurrentMatch for team is playing in
    //
    // One team could play only at exact one CurrentMatch
    private final SortedMap<String, CurrentMatch> teamToMatches = new ConcurrentSkipListMap<>();

    // Using Atomic to make sure each new match will have unique sequence number
    // Some of unique ids would be wasted if match is already playing
    // TODO BTW it's dangerous for seqGen to be overflowed if client would send millions of invalid requests
    private final AtomicInteger seqGen = new AtomicInteger();

    private FootballScoreboardImpl(MatchValidator matchValidator) {
        super(matchValidator);
    }

    @Override
    public void clearAllMatches() {
        teamToMatches.clear();
    }

    @Override
    public void doStartNewMatch(String homeTeam, String awayTeam) {
        // TODO we could avoid waisted ids of seqGen if implement lazy generation of it:
        //  for example using lambda: () -> seqGen.incrementAndGet();
        var newMatch = new CurrentMatch(homeTeam, awayTeam, 0, 0, seqGen.incrementAndGet());

        // It's better to explain what's going on in code below:
        // When we need to guarantee for unique check for BOTH:
        // * Team(single team name) added to the ScoreBoard
        // * Match(two team names) added to the ScoreBoard
        // then 'Houston, we have a problem'
        // It needs to lock some of 'other' threads (using synchronization or semaphores etc.)
        // which could probably want to add more matches to the board
        // I decided to use two steps 'check' which is very similar(but not the same)
        // to transactions deadlock resolver behaviour in rdbms:
        // 1. Try adding homeTeam. If success - then other threads are impossible to do first step,
        //    and now we are secure for the second step
        // 2. Try adding awayTeam.
        var existingMatch = teamToMatches.putIfAbsent(homeTeam, newMatch);
        if (existingMatch != null) {
            // teamToMatches map was not changed by this thread! need not clear it.
            throw new TeamAlreadyPlayingException("Home team is already playing!");
        }
        existingMatch = teamToMatches.putIfAbsent(awayTeam, newMatch);
        if (existingMatch != null) {
            // We added homeTeam to teamToMatches map
            // But awayTeam already playing somewhere else
            // So we need to rollback homeTeam from map to guarantee 'all-or-nothing' changes
            teamToMatches.remove(homeTeam);
            throw new TeamAlreadyPlayingException("Away team is already playing!");
        }
        // TODO good for test example:
        // add Match(teamA, teamB) -> Match(teamB, teamC)
    }

    @Override
    public void doUpdateMatchScore(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore) {
        // TODO add validation at least to escape negative values

        // Going to catch and hold previous Match reference during atomic computation map operation.
        // Will use it for rollback if first team be updated but NOT updated for second one
        var oldMatch = new AtomicReference<>();

        var newMatch = teamToMatches.computeIfPresent(homeTeam, (key, oldValue) -> {
            // Creating immutable copy of CurrentMatch
            // Assigning NEW team scores using input parameters
            // But EXISTING value for startSequence
            var newValue = new CurrentMatch(
                    homeTeam, awayTeam, homeTeamScore, awayTeamScore, oldValue.getStartSequence()
            );
            oldMatch.set(oldValue);
            return newValue;
        });

        if (newMatch == null) {
            // TODO to be continued
        }


//        var newMatch = new CurrentMatch(homeTeam, awayTeam, homeTeamScore, awayTeamScore);
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public void doRemoveMatch(String homeTeam, String awayTeam) {
        // TODO make it thread safe!
        teamToMatches.remove(homeTeam);
        teamToMatches.remove(awayTeam);
    }

    @Override
    public List<CurrentMatch> getSummary() {
        // TODO: no guarantees for Atomic copy of map.. thing about how to fix it
        return teamToMatches.values().stream()
                .distinct()
                .sorted()
                .collect(toList());
    }

}
