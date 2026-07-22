package ph.edu.dlsu.lbycpob.hangman.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ph.edu.dlsu.lbycpob.hangman.model.GameState;
import ph.edu.dlsu.lbycpob.hangman.service.HangmanService;
import ph.edu.dlsu.lbycpob.hangman.statistics.GameStatistics;
import ph.edu.dlsu.lbycpob.hangman.statistics.StatisticsWriter;

@Controller
public class GameController {

    private static final String SESSION_KEY = "gameState";

    private final HangmanService hangmanService;
    private final StatisticsWriter statisticsWriter;

    public GameController(HangmanService hangmanService, StatisticsWriter statisticsWriter) {
        this.hangmanService = hangmanService;
        this.statisticsWriter = statisticsWriter;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/game/start")
    public String startGame(@RequestParam("filename") String filename, HttpSession session) {
        GameState state = new GameState();
        state.setFilename(filename.trim());

        String word = hangmanService.getRandomWord(state.getFilename());
        state.setSecretWord(word);
        state.setGuessesRemaining(HangmanService.MAX_GUESSES);
        state.setMessage("A new word has been chosen. It has " + word.length() + " letter(s). Good luck!");

        session.setAttribute(SESSION_KEY, state);
        return "redirect:/game/play";
    }

    @GetMapping("/game/play")
    public String play(HttpSession session, Model model) {
        GameState state = (GameState) session.getAttribute(SESSION_KEY);
        if (state == null) {
            return "redirect:/";
        }

        String hint = hangmanService.createHint(state.getSecretWord(), state.getGuessedLetters());
        String displayHint = hangmanService.formatHintForDisplay(hint);
        String art = hangmanService.getHangmanArtAsString(state.getGuessesRemaining());

        model.addAttribute("state", state);
        model.addAttribute("hint", hint);
        model.addAttribute("displayHint", displayHint);
        model.addAttribute("hangmanArt", art);
        model.addAttribute("alphabet", hangmanService.getAlphabet());
        return "play";
    }

    @PostMapping("/game/guess")
    public String guess(@RequestParam("letter") String letterInput, HttpSession session) {
        GameState state = (GameState) session.getAttribute(SESSION_KEY);
        if (state == null || state.isGameOver()) {
            return "redirect:/game/play";
        }

        String cleaned = letterInput.trim().toUpperCase();
        if (cleaned.length() != 1 || cleaned.charAt(0) < 'A' || cleaned.charAt(0) > 'Z') {
            state.setMessage("Please enter a single letter from A to Z.");
            session.setAttribute(SESSION_KEY, state);
            return "redirect:/game/play";
        }

        char letter = cleaned.charAt(0);
        if (state.getGuessedLetters().indexOf(letter) >= 0) {
            state.setMessage("You already guessed \"" + letter + "\". Choose a different letter.");
            session.setAttribute(SESSION_KEY, state);
            return "redirect:/game/play";
        }

        state.setGuessedLetters(state.getGuessedLetters() + letter);

        if (state.getSecretWord().indexOf(letter) >= 0) {
            String hint = hangmanService.createHint(state.getSecretWord(), state.getGuessedLetters());
            if (!hint.contains("-")) {
                state.setGameOver(true);
                state.setWon(true);
                state.setStatistics(state.getStatistics().withGame(true, state.getGuessesRemaining()));
                state.setMessage("You win! The word was \"" + state.getSecretWord() + "\". " + state.getGuessesRemaining() + " guess(es) remaining.");
            } else {
                state.setMessage("Correct! \"" + letter + "\" is in the word.");
            }
        } else {
            state.setGuessesRemaining(state.getGuessesRemaining() - 1);
            if (state.getGuessesRemaining() == 0) {
                state.setGameOver(true);
                state.setWon(false);
                state.setStatistics(state.getStatistics().withGame(false, 0));
                state.setMessage("You lose. The word was \"" + state.getSecretWord() + "\".");
            } else {
                state.setMessage("Incorrect! \"" + letter + "\" is not in the word. " + state.getGuessesRemaining() + " guess(es) left.");
            }
        }

        session.setAttribute(SESSION_KEY, state);
        return "redirect:/game/play";
    }

    @PostMapping("/game/again")
    public String playAgain(HttpSession session) {
        GameState old = (GameState) session.getAttribute(SESSION_KEY);
        if (old == null) {
            return "redirect:/";
        }

        GameState fresh = new GameState();
        fresh.setFilename(old.getFilename());
        fresh.setStatistics(old.getStatistics());

        String word = hangmanService.getRandomWord(old.getFilename());
        fresh.setSecretWord(word);
        fresh.setGuessesRemaining(HangmanService.MAX_GUESSES);
        fresh.setMessage("New round! The word has " + word.length() + " letter(s). Good luck!");

        session.setAttribute(SESSION_KEY, fresh);
        return "redirect:/game/play";
    }

    @GetMapping("/game/stats")
    public String stats(HttpSession session, Model model) {
        GameState state = (GameState) session.getAttribute(SESSION_KEY);
        if (state == null || state.getStatistics().gamesPlayed() == 0) {
            return "redirect:/";
        }

        GameStatistics s = state.getStatistics();
        model.addAttribute("stats", s);

        statisticsWriter.writeStats(
                s.gamesPlayed(),
                s.gamesWon(),
                s.gamesPlayed() - s.gamesWon(),
                s.winPercentage(),
                s.bestGuessesRemaining());

        session.invalidate();
        return "stats";
    }

    @GetMapping("/game/reset")
    public String reset(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}