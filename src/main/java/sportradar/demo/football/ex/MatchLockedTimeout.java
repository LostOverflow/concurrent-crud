package sportradar.demo.football.ex;

public class MatchLockedTimeout extends RuntimeException {
    public MatchLockedTimeout(String msg) {
        super(msg);
    }
}
