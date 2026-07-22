package ph.edu.dlsu.lbycpob.hangman.repository;

import java.io.IOException;

public interface WordRepository {
    String getRandomWord(String filename) throws IOException;
}