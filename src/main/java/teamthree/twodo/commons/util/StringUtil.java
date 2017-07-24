package teamthree.twodo.commons.util;

import static java.util.Objects.requireNonNull;
import static teamthree.twodo.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Optional;

import teamthree.twodo.model.task.Deadline;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {
    private static final int CHAR_OFFSET = 97;
    private static final int ALPHABET_SIZE = 27; //Extra one to make up for one-indexing

    /**
     * Returns true if the {@code sentence} contains the {@code word}. Ignores
     * case, but a full word match is required. <br>
     * examples:
     *
     * <pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == true //not a full word match
     * </pre>
     *
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        /**
         * Changing equalsIgnoreCase to containsIgnoreCase to loosen up search
         * requirements
         */
        for (String wordInSentence : wordsInPreppedSentence) {
            if (wordInSentence.toLowerCase().contains(preppedWord.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a detailed message of the t, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer e.g. 1,
     * 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input e.g. empty string,
     * "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace),
     * "1 a" (contains letters)
     *
     * @throws NullPointerException
     *             if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    //@@author A0124399W
    /**
     * Measures the minimum edit distance between two strings. The larger the
     * int returned, the greater the difference between the strings. Can be used
     * for auto-correcting small errors in user input.
     *
     * @param word
     * @param otherWord
     * @param alphabetLength is the size of the alphabet of the language used currently
     * hard-coded for english
     * @return int Minimum edit distance between word and otherWord
     */
    public static int damerauLevenshteinDistance(String word, String otherWord, int alphabetLength) {
        final int maxDistance = word.length() + otherWord.length();
        int[][] costs = new int[word.length() + 2][otherWord.length() + 2];
        costs[0][0] = maxDistance;
        for (int i = 0; i <= word.length(); i++) {
            costs[i + 1][1] = i;
            costs[i + 1][0] = maxDistance;
        }
        for (int j = 0; j <= otherWord.length(); j++) {
            costs[1][j + 1] = j;
            costs[0][j + 1] = maxDistance;
        }
        /**
         * Note that within the matrix costs, costs has indices starting at −1
         * relative to the words and alphabet arrays(word, otherWord and da are
         * one-indexed).
         */
        int[] alphabet = new int[alphabetLength];
        Arrays.fill(alphabet, 0);
        for (int i = 1; i <= word.length(); i++) {
            int db = 0;
            for (int j = 1; j <= otherWord.length(); j++) {
                int k = alphabet[otherWord.charAt(j - 1) - CHAR_OFFSET];
                int l = db;
                int d = ((word.charAt(i - 1) == otherWord.charAt(j - 1)) ? 0 : 1);
                if (d == 0) {
                    db = j;
                }

                costs[i + 1][j + 1] = min(costs[i][j] + d, costs[i + 1][j] + 1, costs[i][j + 1] + 1,
                        costs[k][l] + (i - k - 1) + 1 + (j - l - 1));
            }
            alphabet[word.charAt(i - 1) - CHAR_OFFSET] = i;
        }
        return costs[word.length() + 1][otherWord.length() + 1];
    }

    //Helper function to find minimun among multipls integers.
    private static int min(int... nums) {
        int min = Integer.MAX_VALUE;
        for (int num : nums) {
            min = Math.min(min, num);
        }
        return min;
    }

    /**
     * Handles the requisite preparation step to carry out autocorrection.
     *
     * @param String Day user input string for day of the week
     * @return String Day shortened to just first three letters
     */
    public static String prepareDayString(String dateTime) {
        return dateTime.substring(0, Deadline.MIN_WORD_LENGTH_FOR_DAY + 1).toLowerCase();
    }

    /**
     * Attempts to auto-correct the given day string to the closest match.. If
     * no match is found, an empty Optional is returned.
     *
     * @param userDay
     * @return Optional containing corrected day string
     */
    public static Optional<String> getAutoCorrectedDay(String userDay) {
        if (userDay.length() <= Deadline.MIN_WORD_LENGTH_FOR_DAY) {
            return Optional.empty();
        }
        //we're only using 3 letters and getting distance of more than 1 would mean it is at least 60% different
        final int failThreshold = 1;
        String day = prepareDayString(userDay);
        String[] days = { "mon", "tue", "wed", "thu", "fri", "sat", "sun" };
        int[] indexAndDistance = getIndexOfMostSimilarWord(day, days);
        if (indexAndDistance[1] > failThreshold) {
            //This means there was no close match
            return Optional.empty();
        }
        return Optional.of(days[indexAndDistance[0]]);

    }

    /**
     * Finds the string most similar to the argument inside an array of strings
     * and returns its index and minimum edit distance.
     *
     * @param word
     * @param words
     * @return integer array [index, minimum edit distance]
     */
    private static int[] getIndexOfMostSimilarWord(String word, String[] words) {
        /**
         * Minimum distance variable initialised to largest int possible and
         * index variable to keep track of its position in the array
         */
        int minDist = Integer.MAX_VALUE;
        int mindex = 0;
        for (int i = 0; i < words.length; i++) {
            int currentDist;
            if (word.equals(words[i])) {
                // Short circuit for the case of exact match
                return new int[] { i, 0 };
            }
            if ((currentDist = damerauLevenshteinDistance(word, words[i], ALPHABET_SIZE)) < minDist) {
                minDist = currentDist;
                mindex = i;
            }
        }
        return new int[] { mindex, minDist };
    }

}
