package sportradar.demo.football.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentMatch {
    private final String homeTeamName;
    private final String awayTeamName;
    // to guarantee atomic updates for both team scores
    // I prefer to replace target CurrentMatch objects with new scores
    // AtomicInteger will not solve the issue
    private final Integer homeTeamScore;
    private final Integer awayTeamScore;
}
