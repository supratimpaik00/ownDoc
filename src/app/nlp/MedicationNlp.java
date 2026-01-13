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

    public MedicationNlp() {
        this.tokenizer = loadTokenizer();
        this.posTagger = loadPosTagger();
    }

    public MedicationParseResult parse(String transcript) {
        String raw = transcript == null ? "" : transcript.trim();
        if (raw.isEmpty()) {
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
        return new MedicationParseResult(medication, dosage, days);
    }

    private Tokenizer loadTokenizer() {
        if (Files.exists(TOKEN_MODEL_PATH)) {
            try (InputStream input = Files.newInputStream(TOKEN_MODEL_PATH)) {
                return new TokenizerME(new TokenizerModel(input));
            } catch (IOException ignored) {
            }
        }
        System.out.println("OpenNLP tokenizer model missing; falling back to simple tokenization.");
        return SimpleTokenizer.INSTANCE;
    }

    private POSTaggerME loadPosTagger() {
        if (Files.exists(POS_MODEL_PATH)) {
            try (InputStream input = Files.newInputStream(POS_MODEL_PATH)) {
                return new POSTaggerME(new POSModel(input));
            } catch (IOException ignored) {
            }
        }
        System.out.println("OpenNLP POS model missing; medication extraction will be less accurate.");
        return null;
    }

    private String parseDays(String[] tokens, String[] lower, boolean[] consumed) {
        for (int i = 0; i + 2 < lower.length; i++) {
            if (!"for".equals(lower[i])) {
                continue;
            }
            Integer count = parseNumberToken(lower[i + 1]);
            if (count == null) {
                continue;
            }
            if (isDayOrWeek(lower[i + 2])) {
                mark(consumed, i, i + 2);
                return count + " " + lower[i + 2];
            }
        }
        for (int i = 0; i + 1 < lower.length; i++) {
            if (consumed[i] || consumed[i + 1]) {
                continue;
            }
            Integer count = parseNumberToken(lower[i]);
            if (count == null) {
                continue;
            }
            if (isDayOrWeek(lower[i + 1])) {
                mark(consumed, i, i + 1);
                return count + " " + lower[i + 1];
            }
        }
        return "";
    }

    private String parseDosage(String[] tokens, String[] lower, boolean[] consumed) {
        for (int i = 0; i < lower.length; i++) {
            String token = lower[i];
            if ("once".equals(token) || "twice".equals(token) || "thrice".equals(token)) {
                int end = i;
                if (i + 2 < lower.length && ("a".equals(lower[i + 1]) || "per".equals(lower[i + 1])) && isDay(lower[i + 2])) {
                    end = i + 2;
                }
                mark(consumed, i, end);
                return token + " a day";
            }
            Integer count = parseNumberToken(token);
            if (count != null && i + 1 < lower.length && isTimeToken(lower[i + 1])) {
                int idx = i + 2;
                if (idx < lower.length && ("a".equals(lower[idx]) || "per".equals(lower[idx]))) {
                    idx++;
                }
                if (idx < lower.length && isDay(lower[idx])) {
                    mark(consumed, i, idx);
                    return count == 1 ? "once a day" : count + " times a day";
                }
            }
            if ("every".equals(token) && i + 2 < lower.length) {
                Integer countEvery = parseNumberToken(lower[i + 1]);
                if (countEvery != null && isHour(lower[i + 2])) {
                    mark(consumed, i, i + 2);
                    return "every " + countEvery + " " + lower[i + 2];
                }
            }
        }
        return "";
    }

    private String extractMedication(String[] tokens, String[] lower, boolean[] consumed) {
        List<String> words = new ArrayList<>();
        String[] tags = posTagger != null ? posTagger.tag(tokens) : null;
        for (int i = 0; i < tokens.length; i++) {
            if (consumed[i]) {
                continue;
            }
            String token = tokens[i];
            String low = lower[i];
            if (STOP_WORDS.contains(low) || isPunctuation(token) || isNumberToken(low)) {
                continue;
            }
            if (tags != null && !isNounTag(tags[i])) {
                continue;
            }
            words.add(token);
        }
        if (words.isEmpty() && tags != null) {
            for (int i = 0; i < tokens.length; i++) {
                if (consumed[i]) {
                    continue;
                }
                String token = tokens[i];
                String low = lower[i];
                if (STOP_WORDS.contains(low) || isPunctuation(token) || isNumberToken(low)) {
                    continue;
                }
                words.add(token);
            }
        }
        return String.join(" ", words).trim();
    }

    private Integer parseNumberToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        if (WORD_NUMBERS.containsKey(token)) {
            return WORD_NUMBERS.get(token);
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isDayOrWeek(String token) {
        return "day".equals(token) || "days".equals(token) || "week".equals(token) || "weeks".equals(token);
    }

    private boolean isDay(String token) {
        return "day".equals(token) || "days".equals(token);
    }

    private boolean isHour(String token) {
        return "hour".equals(token) || "hours".equals(token);
    }

    private boolean isTimeToken(String token) {
        return "time".equals(token) || "times".equals(token) || "x".equals(token);
    }

    private boolean isNumberToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        for (int i = 0; i < token.length(); i++) {
            if (!Character.isDigit(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isPunctuation(String token) {
        return token.length() == 1 && !Character.isLetterOrDigit(token.charAt(0));
    }

    private boolean isNounTag(String tag) {
        return tag.startsWith("NN") || tag.startsWith("JJ");
    }

    private void mark(boolean[] consumed, int start, int end) {
        for (int i = start; i <= end && i < consumed.length; i++) {
            consumed[i] = true;
        }
    }
}
