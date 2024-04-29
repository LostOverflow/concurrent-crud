package sportradar.demo.football.ex;

public class InvalidScoreException extends RuntimeException {
    public InvalidScoreException(String msg) {
        super(msg);
    }
}
