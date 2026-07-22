package ph.edu.dlsu.lbycpob.hangman.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ph.edu.dlsu.lbycpob.hangman.render.HangmanRenderer;
import ph.edu.dlsu.lbycpob.hangman.repository.WordRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class HangmanService {

    private static final Logger log = LoggerFactory.getLogger(HangmanService.class);

    public static final int MAX_GUESSES = 8;

    private static final String[] FALLBACK_WORDS = {
            "JAVA", "HANGMAN", "COMPUTER", "KEYBOARD", "PROGRAM", "ALGORITHM"
    };

    private final WordRepository wordRepository;
    private final HangmanRenderer renderer;
    private final Random random;

    public HangmanService(WordRepository wordRepository, HangmanRenderer renderer, Random random) {
        this.wordRepository = Objects.requireNonNull(wordRepository);
        this.renderer = Objects.requireNonNull(renderer);
        this.random = Objects.requireNonNull(random);
    }

    public String getRandomWord(String filename) {
        Objects.requireNonNull(filename, "filename must not be null");
        try {
            return wordRepository.getRandomWord(filename);
        } catch (IOException e) {
            log.warn("Could not load words from \"{}\": {}. Using built-in fallback.", filename, e.getMessage());
            return FALLBACK_WORDS[random.nextInt(FALLBACK_WORDS.length)];
        }
    }

    public String createHint(String secretWord, String guessedLetters) {
        Objects.requireNonNull(secretWord, "secretWord must not be null");
        Objects.requireNonNull(guessedLetters, "guessedLetters must not be null");

        String upperWord = secretWord.toUpperCase();
        String upperGuessed = guessedLetters.toUpperCase();

        StringBuilder hint = new StringBuilder(upperWord.length());
        for (int i = 0; i < upperWord.length(); i++) {
            char c = upperWord.charAt(i);
            hint.append(upperGuessed.indexOf(c) >= 0 ? c : '-');
        }
        return hint.toString();
    }

    public String formatHintForDisplay(String hint) {
        Objects.requireNonNull(hint, "hint must not be null");
        StringBuilder sb = new StringBuilder(hint.length() * 2);
        for (int i = 0; i < hint.length(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(hint.charAt(i));
        }
        return sb.toString();
    }

    public List<String> getHangmanArt(int guessesRemaining) {
        try {
            return renderer.render(guessesRemaining);
        } catch (IOException e) {
            log.error("Could not load hangman art for guessesRemaining={}", guessesRemaining, e);
            return List.of("[art unavailable]");
        }
    }

    public String getHangmanArtAsString(int guessesRemaining) {
        return String.join("\n", getHangmanArt(guessesRemaining));
    }

    public List<Character> getAlphabet() {
        List<Character> alphabet = new ArrayList<>(26);
        for (char c = 'A'; c <= 'Z'; c++) {
            alphabet.add(c);
        }
        return alphabet;
    }
}