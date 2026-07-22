package ph.edu.dlsu.lbycpob.hangman.render;

import java.io.IOException;
import java.util.List;

public interface HangmanRenderer {
    List<String> render(int guessesRemaining) throws IOException;
}