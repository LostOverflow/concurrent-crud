package sportradar.demo.football;

import lombok.NoArgsConstructor;
import sportradar.demo.football.dto.CurrentMatch;
import sportradar.demo.football.ex.TeamAlreadyPlayingException;

import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import static lombok.AccessLevel.PRIVATE;

/**
 * Let's use Scoreboard Implementation as a Singleton to prevent data storage duplication
 */
@NoArgsConstructor(access = PRIVATE)
public class FootballScoreboardImpl implements FootballScoreboard {

    private static final FootballScoreboard instance = new FootballScoreboardImpl();

    // SortedMap will take care about check for unique and order of adding
    // Key is a String for team name
    // Value is CurrentMatch for team is playing in
    // One team could play only at exact one CurrentMatch
    private final SortedMap<String, CurrentMatch> teamToMatches = new ConcurrentSkipListMap<>();

    // Using Atomic to make sure each new match will have unique sequence number event if multi-thread.
    // Some of unique ids would be wasted if match is already playing
    // TODO BTW it's dangerous for seqGen to be overflowed if client would send millions of invalid requests
    private final AtomicInteger seqGen = new AtomicInteger();

    public void startNewMatch(String homeTeam, String awayTeam) {
        var newMatch = new CurrentMatch(homeTeam, awayTeam, 0, 0, seqGen.incrementAndGet());

        // We use two separate atomic operations to register new match:
        //      * putIfAbsent HOME team
        //      * putIfAbsent AWAY team
        var existingMatch = teamToMatches.putIfAbsent(homeTeam, newMatch);
        if (existingMatch != null) {
            // teamToMatches map was not changed by this thread.
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
    }

    @Override
    public void updateScore(int homeTeamScore, int awayTeamScore) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public void removeMatch(String homeTeam, String awayTeam) {
        throw new IllegalStateException("Not implemented!");
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
