package app.nlp;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MedicationNlp {
    private static final Path TOKEN_MODEL_PATH = Path.of("models", "en-token.bin");
    private static final Path POS_MODEL_PATH = Path.of("models", "en-pos-maxent.bin");
    private static final Map<String, Integer> WORD_NUMBERS = Map.ofEntries(
            Map.entry("one", 1),
            Map.entry("two", 2),
            Map.entry("three", 3),
            Map.entry("four", 4),
            Map.entry("five", 5),
            Map.entry("six", 6),
            Map.entry("seven", 7),
            Map.entry("eight", 8),
            Map.entry("nine", 9),
            Map.entry("ten", 10),
            Map.entry("eleven", 11),
            Map.entry("twelve", 12)
    );
    private static final Set<String> STOP_WORDS = Set.of(
            "take", "tablet", "tablets", "capsule", "capsules", "syrup",
            "for", "a", "per", "day", "days", "week", "weeks",
            "time", "times", "x", "every", "hour", "hours",
            "once", "twice", "thrice", "daily", "dose", "dosage",
            "and", "then"
    );

    private final Tokenizer tokenizer;
    private final POSTaggerME posTagger;

    // Initializes medication nlp.
    public MedicationNlp() {
        this.tokenizer = loadTokenizer();
        this.posTagger = loadPosTagger();
    }

    // Performs parse.
    public MedicationParseResult parse(String transcript) {
        String raw = transcript == null ? "" : transcript.trim();
        // Performs if.
        if (raw.isEmpty()) {
            // Performs medication parse result.
            return new MedicationParseResult("", "", "");
        }
        String[] tokens = tokenizer.tokenize(raw);
        String[] lower = new String[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            lower[i] = tokens[i].toLowerCase();
        }
        boolean[] consumed = new boolean[tokens.length];
        String days = parseDays(tokens, lower, consumed);
        String dosage = parseDosage(tokens, lower, consumed);
        String medication = extractMedication(tokens, lower, consumed);
        // Performs medication parse result.
        return new MedicationParseResult(medication, dosage, days);
    }

    // Loads tokenizer.
    private Tokenizer loadTokenizer() {
        // Performs if.
        if (Files.exists(TOKEN_MODEL_PATH)) {
            // Performs try.
            try (InputStream input = Files.newInputStream(TOKEN_MODEL_PATH)) {
                // Performs tokenizer me.
                return new TokenizerME(new TokenizerModel(input));
            } catch (IOException ignored) {
            }
        }
        System.out.println("OpenNLP tokenizer model missing; falling back to simple tokenization.");
        return SimpleTokenizer.INSTANCE;
    }

    // Loads pos tagger.
    private POSTaggerME loadPosTagger() {
        // Performs if.
        if (Files.exists(POS_MODEL_PATH)) {
            // Performs try.
            try (InputStream input = Files.newInputStream(POS_MODEL_PATH)) {
                // Performs pos tagger me.
                return new POSTaggerME(new POSModel(input));
            } catch (IOException ignored) {
            }
        }
        System.out.println("OpenNLP POS model missing; medication extraction will be less accurate.");
        return null;
    }

    // Parses days.
    private String parseDays(String[] tokens, String[] lower, boolean[] consumed) {
        for (int i = 0; i + 2 < lower.length; i++) {
            // Performs if.
            if (!"for".equals(lower[i])) {
                continue;
            }
            Integer count = parseNumberToken(lower[i + 1]);
            // Performs if.
            if (count == null) {
                continue;
            }
            // Performs if.
            if (isDayOrWeek(lower[i + 2])) {
                // Performs mark.
                mark(consumed, i, i + 2);
                return count + " " + lower[i + 2];
            }
        }
        for (int i = 0; i + 1 < lower.length; i++) {
            // Performs if.
            if (consumed[i] || consumed[i + 1]) {
                continue;
            }
            Integer count = parseNumberToken(lower[i]);
            // Performs if.
            if (count == null) {
                continue;
            }
            // Performs if.
            if (isDayOrWeek(lower[i + 1])) {
                // Performs mark.
                mark(consumed, i, i + 1);
                return count + " " + lower[i + 1];
            }
        }
        return "";
    }

    // Parses dosage.
    private String parseDosage(String[] tokens, String[] lower, boolean[] consumed) {
        for (int i = 0; i < lower.length; i++) {
            String token = lower[i];
            // Performs if.
            if ("once".equals(token) || "twice".equals(token) || "thrice".equals(token)) {
                int end = i;
                // Performs if.
                if (i + 2 < lower.length && ("a".equals(lower[i + 1]) || "per".equals(lower[i + 1])) && isDay(lower[i + 2])) {
                    end = i + 2;
                }
                // Performs mark.
                mark(consumed, i, end);
                return token + " a day";
            }
            Integer count = parseNumberToken(token);
            // Performs if.
            if (count != null && i + 1 < lower.length && isTimeToken(lower[i + 1])) {
                int idx = i + 2;
                // Performs if.
                if (idx < lower.length && ("a".equals(lower[idx]) || "per".equals(lower[idx]))) {
                    idx++;
                }
                // Performs if.
                if (idx < lower.length && isDay(lower[idx])) {
                    // Performs mark.
                    mark(consumed, i, idx);
                    return count == 1 ? "once a day" : count + " times a day";
                }
            }
            // Performs if.
            if ("every".equals(token) && i + 2 < lower.length) {
                Integer countEvery = parseNumberToken(lower[i + 1]);
                // Performs if.
                if (countEvery != null && isHour(lower[i + 2])) {
                    // Performs mark.
                    mark(consumed, i, i + 2);
                    return "every " + countEvery + " " + lower[i + 2];
                }
            }
        }
        return "";
    }

    // Performs extract medication.
    private String extractMedication(String[] tokens, String[] lower, boolean[] consumed) {
        List<String> words = new ArrayList<>();
        String[] tags = posTagger != null ? posTagger.tag(tokens) : null;
        for (int i = 0; i < tokens.length; i++) {
            // Performs if.
            if (consumed[i]) {
                continue;
            }
            String token = tokens[i];
            String low = lower[i];
            // Performs if.
            if (STOP_WORDS.contains(low) || isPunctuation(token) || isNumberToken(low)) {
                continue;
            }
            // Performs if.
            if (tags != null && !isNounTag(tags[i])) {
                continue;
            }
            words.add(token);
        }
        // Performs if.
        if (words.isEmpty() && tags != null) {
            for (int i = 0; i < tokens.length; i++) {
                // Performs if.
                if (consumed[i]) {
                    continue;
                }
                String token = tokens[i];
                String low = lower[i];
                // Performs if.
                if (STOP_WORDS.contains(low) || isPunctuation(token) || isNumberToken(low)) {
                    continue;
                }
                words.add(token);
            }
        }
        return String.join(" ", words).trim();
    }

    // Parses number token.
    private Integer parseNumberToken(String token) {
        // Performs if.
        if (token == null || token.isEmpty()) {
            return null;
        }
        // Performs if.
        if (WORD_NUMBERS.containsKey(token)) {
            return WORD_NUMBERS.get(token);
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    // Checks whether day or week.
    private boolean isDayOrWeek(String token) {
        return "day".equals(token) || "days".equals(token) || "week".equals(token) || "weeks".equals(token);
    }

    // Checks whether day.
    private boolean isDay(String token) {
        return "day".equals(token) || "days".equals(token);
    }

    // Checks whether hour.
    private boolean isHour(String token) {
        return "hour".equals(token) || "hours".equals(token);
    }

    // Checks whether time token.
    private boolean isTimeToken(String token) {
        return "time".equals(token) || "times".equals(token) || "x".equals(token);
    }

    // Checks whether number token.
    private boolean isNumberToken(String token) {
        // Performs if.
        if (token == null || token.isEmpty()) {
            return false;
        }
        for (int i = 0; i < token.length(); i++) {
            // Performs if.
            if (!Character.isDigit(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // Checks whether punctuation.
    private boolean isPunctuation(String token) {
        return token.length() == 1 && !Character.isLetterOrDigit(token.charAt(0));
    }

    // Checks whether noun tag.
    private boolean isNounTag(String tag) {
        return tag.startsWith("NN") || tag.startsWith("JJ");
    }

    // Performs mark.
    private void mark(boolean[] consumed, int start, int end) {
        for (int i = start; i <= end && i < consumed.length; i++) {
            consumed[i] = true;
        }
    }
}
