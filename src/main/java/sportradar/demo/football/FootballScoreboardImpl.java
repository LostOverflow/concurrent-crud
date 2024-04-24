package sportradar.demo.football;

import lombok.NoArgsConstructor;
import sportradar.demo.football.dto.CurrentMatch;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import static lombok.AccessLevel.PRIVATE;

/**
 * Let's use Scoreboard Implementation as a Singleton to prevent data storage duplication
 */
@NoArgsConstructor(access = PRIVATE)
public class FootballScoreboardImpl implements FootballScoreboard {

    private static final FootballScoreboard instance = new FootballScoreboardImpl();

    private final SortedSet<CurrentMatch> matches = new ConcurrentSkipListSet<>();

    // Using Atomic to make sure each new match will have unique sequence number event if multi-thread
    private final AtomicInteger seqGen = new AtomicInteger();

    public void startNewMatch(String homeTeam, String awayTeam) {
        // var newMatch = new CurrentMatch(homeTeam, awayTeam, 0, 0, seqGen.incrementAndGet());
        // matches.add(newMatch);
    }

    @Override
    public void updateScore(int homeTeamScore, int awayTeamScore) {

    }

    @Override
    public void removeMatch(String homeTeam, String awayTeam) {

    }

    @Override
    public List<CurrentMatch> getSummary() {
        // That one makes a thread safe copy of a queue and convert it into immutable list
//        Arrays.stream(matches.toArray(new CurrentMatch[0]))
//                .sorted(Comparator.comparing())
        return List.of();
    }
}
