package sportradar.demo.football.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Data
@RequiredArgsConstructor
/*
 * Could sound quite strange, but I prefer as fewer comments in code as possible, and Now I have to explain why:
 *  * Comments in code means something 'unclear', 'strange', 'magical' etc.
 *    that's why you have to put your comment here - to explain why it looks so strange.
 *  * Comments explaining really obvious things: var x = 1; // let's assign 1 to new variable x!
 * Self-descriptive code is really cool!
 * BTW At
 *  this.current(demo -> project.getName() + "I have to do it vice versa");
 *
 * For example: I prefer NOT TO explain why I decided to implement Comparable...
 * of course someone is going to Compare it!
 * */
public class CurrentMatch implements Comparable<CurrentMatch> {

    private final String homeTeam;
    private final String awayTeam;
    // to guarantee atomic updates for both team scores
    // I prefer to replace target CurrentMatch objects with new scores
    // AtomicInteger will not solve the issue
    private final Integer homeScore;
    private final Integer awayScore;

    // StartSequence should be assigned during CurrentMatch is being created.
    // Each new created CurrentMatch should have unique, always incremented number.
    // Need not guarantee for strict sequence, could have some spaces between: 1, 2, 3, 5, 10, 11...
    private final Integer startSequence;

    // Never wanted to bring complexity to the code
    // but seems it required for correct update/insert/delete logic
    private final Lock matchLock;

    public CurrentMatch(String homeTeam, String awayTeam, int homeScore, int awayScore, int startSequence) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.startSequence = startSequence;
        // new CurrentMatch is always being created in locked state
        // when reference to match be inserted into map for HOME team
        // it should never be able to read/update/delete it from map
        // because data is not consistent yet until AWAY team be inserted
        var newLock = new ReentrantLock();
        newLock.lock();
        matchLock = newLock;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CurrentMatch other)) {
            return false;
        }
        // Let's check CurrentMatch equality by team names
        // because the scores could be changed during match is running
        return this.homeTeam.equals(other.homeTeam) &&
                this.awayTeam.equals(other.awayTeam);
    }

    @Override
    public String toString() {
        return "#" + startSequence + " " + homeTeam + " " + homeScore + " - " + awayTeam + " " + awayScore;
    }

    @Override
    public int compareTo(CurrentMatch o) {
        var thisScores = this.homeScore + this.awayScore;
        var otherScores = o.homeScore + o.awayScore;
        // according to business requirements we have to compare CurrentMatch
        // by total scores, and if the same scores then compare by added sequence desc
        if (thisScores == otherScores) {
            return -1 * this.startSequence.compareTo(o.startSequence);
        }
        return thisScores > otherScores ? -1 : 1;
    }

}
