package sportradar.demo.football.ex;

// Not really like checked exceptions
// because of the code and logic overhead for throws method declaration
public class TeamAlreadyPlayingException extends RuntimeException {
    public TeamAlreadyPlayingException(String msg) {
        super(msg);
    }

    // TODO add rest of exceptions to the project from test doc
}
