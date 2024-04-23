package sportradar.demo.football;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * TDD Test plan:
 *
 * API:
 *
 *  Start a new match, assuming initial score 0(Home team) – 0(Away team) and adding it the scoreboard.
 *  Test for:
 *  case 'Empty board'. Adding a new match when NO ONE match started before
 *  make sure board is empty
 *  add new match
 *  check the board:
 *      should show only added single match with exact same team names and team order (Home team is always left)
 */
@SpringBootTest
class FootballScoreboardApplicationTests {

    @Test
    void contextLoads() {
    }

}
