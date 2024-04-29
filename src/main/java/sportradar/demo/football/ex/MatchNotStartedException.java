package sportradar.demo.football.ex;

public class MatchNotStartedException extends RuntimeException {
    public MatchNotStartedException(String msg) {
        super(msg);
    }
}
