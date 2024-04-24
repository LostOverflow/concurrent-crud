package sportradar.demo.football.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CurrentMatch implements Comparable<CurrentMatch> {

    private final String homeTeamName;
    private final String awayTeamName;
    // to guarantee atomic updates for both team scores
    // I prefer to replace target CurrentMatch objects with new scores
    // AtomicInteger will not solve the issue
    private final Integer homeTeamScore;
    private final Integer awayTeamScore;

    // StartSequence assigned during CurrentMatch is being created.
    // Each new created CurrentMatch should have unique, always incremented number
    private final Integer startSequence;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CurrentMatch other)) {
            return false;
        }
        // Let's check CurrentMatch equality by ordered team names
        // due to scores could be changed during match is running
        return this.homeTeamName.equals(other.homeTeamName) &&
                this.awayTeamName.equals(other.awayTeamName);
    }

    @Override
    public String toString() {
        return "#" + startSequence + " " + homeTeamName + " " + homeTeamScore + " - " + awayTeamName + " " + awayTeamScore;
    }

    @Override
    public int compareTo(CurrentMatch o) {
        var thisScores = this.homeTeamScore + this.awayTeamScore;
        var otherScores = o.homeTeamScore + o.awayTeamScore;
        // according to business requirements we have to compare CurrentMatch
        // by total scores, and if the same scores then compare by added sequence desc
        if (thisScores == otherScores) {
            return -1 * this.startSequence.compareTo(o.startSequence);
        }
        return thisScores > otherScores ? 1 : -1;
    }

}
