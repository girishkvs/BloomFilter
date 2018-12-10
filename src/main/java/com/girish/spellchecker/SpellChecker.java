package com.girish.spellchecker;

public interface SpellChecker {

    void addToDictionary(String word);

    boolean mightContains(String word);
}
