package sportradar.demo.football.ex;

public class TeamNameOverflowException extends RuntimeException {
    public TeamNameOverflowException(String msg) {
        super(msg);
    }
}
