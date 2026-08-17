package servlet;

import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpellChecker {

    // ============================================================
    // GET SPELLING SUGGESTIONS
    // ============================================================

    public static List<String> getSuggestions(String word, int limit) {

        List<Suggestion> suggestions = new ArrayList<>();

        if (word == null || word.trim().isEmpty()) {
            return new ArrayList<>();
        }

        word = word.toLowerCase().trim();

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                return new ArrayList<>();
            }

            // Check whether the word already exists
            String exactSQL =
                    "SELECT word FROM dictionary_words " +
                    "WHERE LOWER(word) = ? LIMIT 1";

            try (PreparedStatement statement =
                         connection.prepareStatement(exactSQL)) {

                statement.setString(1, word);

                try (ResultSet rs = statement.executeQuery()) {

                    if (rs.next()) {
                        // Word is already correct
                        return new ArrayList<>();
                    }
                }
            }

            // Search for possible candidates
            String candidateSQL =
                    "SELECT word FROM dictionary_words " +
                    "WHERE LOWER(word) LIKE ? " +
                    "AND CHAR_LENGTH(word) BETWEEN ? AND ? " +
                    "LIMIT 5000";

            String firstLetter = word.substring(0, 1);

            int minLength =
                    Math.max(1, word.length() - 2);

            int maxLength =
                    word.length() + 2;

            try (PreparedStatement statement =
                         connection.prepareStatement(candidateSQL)) {

                statement.setString(
                        1,
                        firstLetter + "%"
                );

                statement.setInt(
                        2,
                        minLength
                );

                statement.setInt(
                        3,
                        maxLength
                );

                try (ResultSet rs =
                             statement.executeQuery()) {

                    while (rs.next()) {

                        String candidate =
                                rs.getString("word")
                                        .toLowerCase();

                        int distance =
                                levenshteinDistance(
                                        word,
                                        candidate
                                );

                        // Only reasonably close words
                        if (distance <= 2) {

                            int commonPrefix =
                                    commonPrefixLength(
                                            word,
                                            candidate
                                    );

                            boolean subsequence =
                                    isSubsequence(
                                            word,
                                            candidate
                                    );

                            suggestions.add(
                                    new Suggestion(
                                            candidate,
                                            distance,
                                            commonPrefix,
                                            subsequence
                                    )
                            );
                        }
                    }
                }
            }

            /*
             * Keep a final copy because Java lambdas
             * require captured local variables to be
             * final or effectively final.
             */
            final String inputWord = word;

            // Rank suggestions
            suggestions.sort(
                    Comparator
                            // 1. Smaller edit distance first
                            .comparingInt(
                                    Suggestion::getDistance
                            )

                            // 2. Prefer subsequence matches
                            .thenComparing(
                                    (Suggestion s) ->
                                            s.isSubsequence()
                                                    ? 0
                                                    : 1
                            )

                            // 3. Prefer larger common prefix
                            .thenComparing(
                                    Comparator.comparingInt(
                                            Suggestion::getCommonPrefix
                                    ).reversed()
                            )

                            // 4. Prefer similar length
                            .thenComparingInt(
                                    s -> Math.abs(
                                            inputWord.length()
                                                    - s.getWord().length()
                                    )
                            )
            );

            List<String> result =
                    new ArrayList<>();

            for (Suggestion suggestion :
                    suggestions) {

                if (!result.contains(
                        suggestion.getWord())) {

                    result.add(
                            suggestion.getWord()
                    );
                }

                if (result.size() >= limit) {
                    break;
                }
            }

            return result;

        } catch (Exception e) {

            e.printStackTrace();

            return new ArrayList<>();
        }
    }


    // ============================================================
    // CHECK COMPLETE TEXT
    // ============================================================

    public static String checkText(String text) {

        if (text == null ||
                text.trim().isEmpty()) {

            return "";
        }

        String[] words =
                text.split("\\s+");

        StringBuilder result =
                new StringBuilder();

        for (String originalWord : words) {

            String cleanWord =
                    originalWord.replaceAll(
                            "[^a-zA-Z]",
                            ""
                    );

            if (cleanWord.isEmpty()) {

                result.append(originalWord)
                        .append(" ");

                continue;
            }

            List<String> suggestions =
                    getSuggestions(
                            cleanWord,
                            1
                    );

            String correctedWord =
                    cleanWord;

            if (!suggestions.isEmpty()) {

                correctedWord =
                        suggestions.get(0);
            }

            String finalWord =
                    originalWord.replace(
                            cleanWord,
                            preserveCase(
                                    cleanWord,
                                    correctedWord
                            )
                    );

            result.append(finalWord)
                    .append(" ");
        }

        return result.toString().trim();
    }


    // ============================================================
    // LEVENSHTEIN DISTANCE
    // ============================================================

    private static int levenshteinDistance(
            String a,
            String b) {

        int[][] dp =
                new int[a.length() + 1]
                        [b.length() + 1];

        for (int i = 0;
             i <= a.length();
             i++) {

            dp[i][0] = i;
        }

        for (int j = 0;
             j <= b.length();
             j++) {

            dp[0][j] = j;
        }

        for (int i = 1;
             i <= a.length();
             i++) {

            for (int j = 1;
                 j <= b.length();
                 j++) {

                int cost =
                        a.charAt(i - 1)
                                == b.charAt(j - 1)
                                ? 0
                                : 1;

                dp[i][j] =
                        Math.min(
                                Math.min(
                                        dp[i - 1][j] + 1,
                                        dp[i][j - 1] + 1
                                ),
                                dp[i - 1][j - 1]
                                        + cost
                        );
            }
        }

        return dp[a.length()][b.length()];
    }


    // ============================================================
    // COMMON PREFIX
    // ============================================================

    private static int commonPrefixLength(
            String a,
            String b) {

        int length =
                Math.min(
                        a.length(),
                        b.length()
                );

        int count = 0;

        for (int i = 0;
             i < length;
             i++) {

            if (a.charAt(i) ==
                    b.charAt(i)) {

                count++;

            } else {

                break;
            }
        }

        return count;
    }


    // ============================================================
    // SUBSEQUENCE CHECK
    // ============================================================

    private static boolean isSubsequence(
            String small,
            String large) {

        int i = 0;

        for (int j = 0;
             j < large.length() &&
             i < small.length();
             j++) {

            if (small.charAt(i) ==
                    large.charAt(j)) {

                i++;
            }
        }

        return i == small.length();
    }


    // ============================================================
    // PRESERVE CAPITALIZATION
    // ============================================================

    private static String preserveCase(
            String original,
            String corrected) {

        if (original.isEmpty()) {
            return corrected;
        }

        if (Character.isUpperCase(
                original.charAt(0))) {

            return Character.toUpperCase(
                    corrected.charAt(0)
            ) + corrected.substring(1);
        }

        return corrected;
    }


    // ============================================================
    // SUGGESTION CLASS
    // ============================================================

    private static class Suggestion {

        private final String word;
        private final int distance;
        private final int commonPrefix;
        private final boolean subsequence;

        public Suggestion(
                String word,
                int distance,
                int commonPrefix,
                boolean subsequence) {

            this.word = word;
            this.distance = distance;
            this.commonPrefix = commonPrefix;
            this.subsequence = subsequence;
        }

        public String getWord() {
            return word;
        }

        public int getDistance() {
            return distance;
        }

        public int getCommonPrefix() {
            return commonPrefix;
        }

        public boolean isSubsequence() {
            return subsequence;
        }
    }
}