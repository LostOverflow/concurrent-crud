// always keeping eyes on top right mark in IDEA editor to understand whether 'someone' took care about warnings    --->
package sportradar.demo.football;

import lombok.Getter;
import sportradar.demo.football.dto.CurrentMatch;
import sportradar.demo.football.ex.MatchLockedTimeout;
import sportradar.demo.football.ex.MatchNotStartedException;
import sportradar.demo.football.ex.TeamAlreadyPlayingException;
import sportradar.demo.football.validator.MatchValidator;
import sportradar.demo.football.validator.SportRadarMatchValidator;

import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

/**
 * Let's use Scoreboard Implementation as a Singleton to prevent data storage duplication, etc.
 */
public class FootballScoreboardImpl extends FootballScoreboardTemplate {

    @Getter
    private static final FootballScoreboard instance = new FootballScoreboardImpl(new SportRadarMatchValidator());

    // See example first:
    // data: [Argentina 0 - Brazil 0]
    // stored into map:
    // "Argentina" -> CurrentMatch[homeTeam: Argentina; awayTeam: Brazil; homeScore: 0; awayScore: 0, ...]
    // "Brazil"    -> CurrentMatch[homeTeam: Argentina; awayTeam: Brazil; homeScore: 0; awayScore: 0, ...]
    //
    // SortedMap will take care about checking for unique and ordered items.
    // Actually... I am going to use only single team name as a Key!
    // Why:
    // * We have to check uniqueness of each separate team
    // * Keeping concatenated team names (homeTeamName + awayTeamName) as a key
    //   will NOT guarantee unique check of separate team names
    //
    // Key is a String for team name (both home and away teams are stored separately)
    // Value is CurrentMatch for team is playing in it
    //
    // One team could play only at exact one CurrentMatch
    //
    // I decided to use AtomicReference<CurrentMatch> instead of simple CurrentMatch and that's why:
    // map will contain always EVEN or ZERO entries as a [ teamName -> CurrentMatch ]
    // where CurrentMatch is always duplicated for HOME and AWAY team
    // thus we have got dangerous conditions when:
    //  * HOME reference to CurrentMatch could be changed
    //  * but AWAY reference - still refer to old value
    // which is leading to data inconsistency
    // AtomicReference will be always the same for both HOME and AWAY teams
    // Any change to target CurrentMatch from any thread
    // will switch reference for both teams simultaneously
    private final SortedMap<String, AtomicReference<CurrentMatch>> teamToMatches = new ConcurrentSkipListMap<>();

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
        var newMatchRef = new AtomicReference<>(newMatch);

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
        var existingMatch = teamToMatches.putIfAbsent(homeTeam, newMatchRef);

        if (existingMatch != null) {
            // teamToMatches map was not changed by this thread! need not clear it.
            throw new TeamAlreadyPlayingException("Home team is already playing!");
        }
        // FIXME sometime
        // At this point incorrect behaviour still could happen:
        // if current request is invalid (home team being added is not playing, but away team is playing)
        // we will throw an exception for other threads which could probably valid!
        // Example:
        // current thread:
        //  * adding invalid Match(teamA, teamB) when teamB is already playing, teamA has been added (first step only)
        // other thread:
        //  * adding valid Match(teamA, teamC) when teamA is absent on board, teamC is absent on board
        // Other thread will get false-negative exception
        // !
        // Update: attempted to fix with lock, need for test

        existingMatch = teamToMatches.putIfAbsent(awayTeam, newMatchRef);
        if (existingMatch != null) {
            // We added homeTeam to teamToMatches map
            // But awayTeam already playing somewhere else
            // So we need to rollback homeTeam from map to guarantee 'all-or-nothing' changes
            teamToMatches.remove(homeTeam);
            throw new TeamAlreadyPlayingException("Away team is already playing!");
        }
        // now, when BOTH teams inserted into map, let's unlock match to be able to read/update/delete
        newMatch.getMatchLock().unlock();
    }

    @Override
    public void doUpdateMatchScore(String homeTeam, String awayTeam, int homeNewScore, int awayNewScore) {
        // TODO add validation at least to escape negative values

        // Going to catch and hold Match Lock reference during atomic computation map operation.
        // Will use it for rollback if first team be updated, but NOT updated for second one
        var matchLockRef = new AtomicReference<Lock>();

        // HOME team
        var currMatch = teamToMatches.computeIfPresent(homeTeam, (key, matchRef) -> {
            // trying to lock match only (not changing scores yet)
            // it should be isolated from read/delete/update by other threads
            try {
                var lock = matchRef.get().getMatchLock();
                var isLocked = lock.tryLock(100L, MILLISECONDS);
                if (!isLocked) {
                    // Advice to trying again without any delays!
                    throw new MatchLockedTimeout("Match is currently locked, try UPDATE MATCH SCORE again!");
                }
                matchLockRef.set(lock);
                System.out.println("UPDATE MATCH SCORE: Lock was set for HOME team: [" + homeTeam + "]");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return matchRef;
        });

        if (currMatch == null) {
            throw new MatchNotStartedException("Could not found HOME team on the matches board!");
        }

        // AWAY team
        currMatch = teamToMatches.computeIfPresent(awayTeam, (team, matchRef) -> {
            // Creating immutable copy of CurrentMatch
            // Assigning NEW team scores using input parameters
            // But EXISTING value for startSequence
            var updatedMatch = new CurrentMatch(
                    homeTeam, awayTeam, homeNewScore, awayNewScore, matchRef.get().getStartSequence()
            );
            matchRef.set(updatedMatch);
            // Match reference was not changed! only target reference to CurrentMatch
            return matchRef;
        });

        // Release the lock on CurrentMatch which was set on first step
        matchLockRef.get().unlock();
        System.out.println("UPDATE MATCH SCORE: Lock was released for HOME team: [" + homeTeam + "]");

        if (currMatch == null) {
            throw new MatchNotStartedException("Could not found AWAY team on the matches board!");
        }
    }

    @Override
    public void doRemoveMatch(String homeTeam, String awayTeam) {
        // Lock HOME team to disable it from read/delete/update
        var matchRef = teamToMatches.get(homeTeam);

        if (matchRef == null) {
            throw new MatchNotStartedException("REMOVE MATCH: Could not found HOME team on the matches board!");
        }

        try {
            var lock = matchRef.get().getMatchLock();
            var isLocked = lock.tryLock(100L, MILLISECONDS);
            if (!isLocked) {
                // Advice to trying again without any delays!
                throw new MatchLockedTimeout("REMOVE MATCH: Match is currently locked, try again!");
            }
            System.out.println("REMOVE MATCH: Lock was set for HOME team: [" + homeTeam + "]");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // order does not matter for removing teams
        teamToMatches.remove(homeTeam);
        teamToMatches.remove(awayTeam);
    }

    /*
     * Going to prevent from fetching match list if any of list matches is locked.
     * Actually, lock or not before reading is the question of ISOLATION LEVEL (in terms of RDBMS)
     * So it depends on business requirements / or tech lead decision.
     * I am going  to Lock only matches which are exists in map.
     * It's still possible to insert new matches during read but not update/delete
     *
     * Some time later... thinking about "edge cases" I could see an issue:
     * Assume next TWO teams started to play:
     *  "Mamas - 0, Papas     - 0"
     *  "Sons  - 0, Daughters - 0"
     * Read operation started to select all matches and has read first item from list (Mamas - Papas)
     * Next moment both teams stopped to play and swapped between each other, now it looks like:
     *  "Mamas - 0, Daughters - 0"
     *  "Papas - 0, Sons      - 0"
     * Read operation will continue to fetch next item and will get next result:
     * "Mamas - 0, Papas     - 0"
     * "Papas - 0, Sons      - 0"
     * -- Papas are playing in both teams which is not possible
     * TODO try to fix it
     *  we could introduce one more Lock - which will Lock full access to cache map
     *  prefer to to keep as is for a while.
     *
     */
    @Override
    public List<CurrentMatch> getSummary() {
        // TODO implement locking if necessary by tech requirements
        return teamToMatches.values().stream()
                .map(AtomicReference::get)
                .distinct()
                .sorted()
                .collect(toList());
    }

}
