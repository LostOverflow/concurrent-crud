// always keeping eyes on top right mark in IDEA editor to understand whether 'someone' took care about warnings    --->
package sportradar.demo.football;

import lombok.Getter;
import lombok.NoArgsConstructor;
import sportradar.demo.football.dto.CurrentMatch;
import sportradar.demo.football.ex.TeamAlreadyPlayingException;

import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import static lombok.AccessLevel.PRIVATE;

/**
 * Let's use Scoreboard Implementation as a Singleton to prevent data storage duplication, etc.
 */
@Getter
@NoArgsConstructor(access = PRIVATE)
public class FootballScoreboardImpl implements FootballScoreboard {

    private static final FootballScoreboard instance = new FootballScoreboardImpl();

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

    public void startNewMatch(String homeTeam, String awayTeam) {
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
    public void updateScore(int homeTeamScore, int awayTeamScore) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public void removeMatch(String homeTeam, String awayTeam) {
        // TODO make it thread safe!
        teamToMatches.remove(homeTeam);
        teamToMatches.remove(awayTeam);
    }

    @Override
    public List<CurrentMatch> getSummary() {
        // TODO: FINISH HIM!

        // That one makes a thread safe copy of a queue and convert it into immutable list
//        Arrays.stream(matches.toArray(new CurrentMatch[0]))
//                .sorted(Comparator.comparing())
        return List.of();
    }

}
