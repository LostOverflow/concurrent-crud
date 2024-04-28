package sportradar.demo.football.ex;

public class EmptyTeamNameException extends RuntimeException {
    public EmptyTeamNameException(String msg) {
        super(msg);
    }
}
