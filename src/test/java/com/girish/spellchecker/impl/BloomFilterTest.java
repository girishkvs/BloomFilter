package com.girish.spellchecker.impl;

import com.girish.spellchecker.SpellChecker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class BloomFilterTest {
    private static final String TEST_DICT = "src/test/java/com/girish/spellchecker/testdata/dict.txt";

    private SpellChecker spellChecker;

    @Before
    public void setup() {
        spellChecker = new BloomFilter(10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_NullInput_AddToDictionary() {
        spellChecker.addToDictionary(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_NullInput_MightContains() {
        spellChecker.mightContains(null);
    }

    @Test
    public void test_SmallDatasetSuccess() {
        spellChecker.addToDictionary("hello");
        spellChecker.addToDictionary("world");
        spellChecker.addToDictionary("foo");

        Assert.assertTrue(spellChecker.mightContains("hello"));
        Assert.assertTrue((spellChecker.mightContains("world")));
        Assert.assertTrue(spellChecker.mightContains("foo"));
        Assert.assertFalse(spellChecker.mightContains("bar"));
    }

    @Test
    public void test_LoadDictWithDefaultParameters() {
        var words = loadWords();
        spellChecker = new BloomFilter();

        words.forEach(word -> spellChecker.addToDictionary(word));

        words.forEach(word -> Assert.assertTrue(spellChecker.mightContains(word)));
    }

    @Test
    public void test_LoadRandomWordsFromDictForFPP() {
        var words = loadWords();
        var numOfWords = words.size();
        var wordsList = new ArrayList<>(words);
        spellChecker = new BloomFilter();
        var insertedWords = new HashSet<>();

        for (var i = 0; i < numOfWords / 2; i++) {
            var randomIndex = ThreadLocalRandom.current().nextInt(0, numOfWords);
            var randomWord = wordsList.get(randomIndex);
            spellChecker.addToDictionary(randomWord);
            insertedWords.add(randomWord);
        }

        var falsePositiveCount = 0;
        for (var i = 0; i < numOfWords / 2; i++) {
            var randomIndex = ThreadLocalRandom.current().nextInt(0, numOfWords);
            var randomWord = wordsList.get(randomIndex);
            if (spellChecker.mightContains(randomWord) && !insertedWords.contains(randomWord)) {
                falsePositiveCount++;
            }
        }

        var fpr = (double) falsePositiveCount / insertedWords.size();
        Assert.assertTrue("False Positive rate should be atmost equal to False Positive Probability when optimal parameters are used",
                fpr <= ((BloomFilter) spellChecker).getFalsePositiveProbability());
    }

    @Test
    public void test_UseGeneratedRandomWordsForFPP() {
        var words = loadWords();
        var numOfWords = words.size();
        spellChecker = new BloomFilter();

        words.forEach(spellChecker::addToDictionary);

        var falsePositiveCount = 0;
        for (var i = 0; i < numOfWords; i++) {
            var randomBytes = new byte[10];
            ThreadLocalRandom.current().nextBytes(randomBytes);
            var randomWord = new String(randomBytes, StandardCharsets.US_ASCII);
            if (spellChecker.mightContains(randomWord) && !words.contains(randomWord)) {
                falsePositiveCount++;
            }
        }

        var fpr = (double) falsePositiveCount / words.size();
        var fpp = ((BloomFilter) spellChecker).getFalsePositiveProbability();
        try {
            Assert.assertTrue(fpr <= fpp);
        } catch (AssertionError e) {
            System.out.println(String.format(
                    "Num of Inserted elements are way more than expected num of inserted elements. So, false positive rate (%s)" +
                    " can be more than False positive probability (%s)", fpr, fpp));
        }
    }

    @Test
    public void test_UseGeneratedRandomWordsForFPP_OptimalParameters() {
        var words = loadWords();
        var numOfWords = words.size();
        spellChecker = new BloomFilter(250000);

        words.forEach(spellChecker::addToDictionary);

        var falsePositiveCount = 0;
        for (var i = 0; i < numOfWords; i++) {
            var randomBytes = new byte[10];
            ThreadLocalRandom.current().nextBytes(randomBytes);
            var randomWord = new String(randomBytes, StandardCharsets.US_ASCII);
            if (spellChecker.mightContains(randomWord) && !words.contains(randomWord)) {
                falsePositiveCount++;
            }
        }

        var fpr = (double) falsePositiveCount / words.size();
        Assert.assertTrue("False Positive rate should be atmost equal to False Positive Probability when optimal parameters are used",
                fpr <= ((BloomFilter) spellChecker).getFalsePositiveProbability());
    }

    private Set<String> loadWords() {
        try {
            return Files.lines(Paths.get(TEST_DICT)).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Error while loading test dictionary: ", e);
        }
    }
}
