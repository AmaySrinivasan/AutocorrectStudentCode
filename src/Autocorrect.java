import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 *
 * @author Zach Blick
 * @author Amay Srinivasan
 */
public class Autocorrect {

    /**
     * Constucts an instance of the Autocorrect class.
     *
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    // Array that sorts all the dictionary words
    private String[] words;
    // Maximum edit distance allowed for suggestions
    private int threshold;

    // Instance of Autocorrect class
    public Autocorrect(String[] words, int threshold) {
        this.words = words;
        this.threshold = threshold;
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     *
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distnace, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        // List that stores all the valid suggestions
        ArrayList<String> matches = new ArrayList<>();
        // Checks each word in the dictionary
        for (String word : words) {
            // Optimization that skips words that don't have matching consecutive letters
            if (!hasMatchingPair(typed, word)) {
                continue;
            }
            // Computes the edit distance and only keeps words within the threshold
            int distance = editDistance(typed, word);
            if (distance <= threshold) {
                matches.add(word);
            }
        }
        // Sorts the results using a custom comparator
        // Goes by smaller edit distance first, and then alphabetical order if they're equal edit distances
        Collections.sort(matches, new Comparator<String>() {
            public int compare(String a, String b) {
                int distA = editDistance(typed, a);
                int distB = editDistance(typed, b);
                // First sort is edit distance
                if (distA != distB) {
                    return distA - distB;
                }
                // Second is alphabetical
                return a.compareTo(b);
            }
        });
        // Converts an arraylist to an array
        return matches.toArray(new String[0]);
    }

    // Optimization to check if each word in the dictionary shares consecutive letters with typed word
    private boolean hasMatchingPair(String typed, String word) {
        for (int i = 0; i < typed.length() - 1; i++) {
            // Takes a pair of consecutive letters
            String pair = typed.substring(i, i + 2);
            // Checks if the dictionary word contains this pair
            if (word.contains(pair)) {
                return true;
            }
        }
        return false;
    }

    // Does Levenshtein edit distance b/w two strings using dynamic programming tabulation
    private int editDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        // The best case is converting from an empty string
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        // Fills the dp table
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                // If the characters match, then no extra cost
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                }
                // Otherwise, take the minimum of the insertion/deletion/replacement
                else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     *
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary) {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Runs the program in the terminal
    public static void main(String[] args) {
        // Loads the larger dictionary
        String[] words = loadDictionary("large");
        // Creates the autocorrect object with the threshold set at 2
        Autocorrect ac = new Autocorrect(words, 2);
        Scanner s = new Scanner(System.in);
        // Keep running
        while (true) {
            System.out.println("Enter a word: ");
            String typed = s.nextLine();
            // Gets the suggestions
            String[] suggestions = ac.runTest(typed);
            // Prints the results
            if (suggestions.length == 0) {
                System.out.println("No matches found");
            } else {
                System.out.println("Suggestions: ");
                for (String a : suggestions) {
                    System.out.println(a);
                }
            }
            System.out.println();
        }
    }
}