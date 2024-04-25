package sportradar.demo.football.ex;

public class TeamAlreadyPlayingException extends RuntimeException {
    public TeamAlreadyPlayingException(String msg) {
        super(msg);
    }
    // Not really like checked exceptions
    // because of the code overhead for throws method declaration

    // TODO add rest of exceptions to the project from test doc
}
