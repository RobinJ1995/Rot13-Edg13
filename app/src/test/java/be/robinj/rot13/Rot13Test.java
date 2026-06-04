package be.robinj.rot13;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Rot13Test {

    @Test
    public void emptyString() {
        assertEquals("", MainActivity.rotate(""));
    }

    @Test
    public void lowercaseAtoM() {
        assertEquals("nopqrstuvwxyz", MainActivity.rotate("abcdefghijklm"));
    }

    @Test
    public void lowercaseNtoZ() {
        assertEquals("abcdefghijklm", MainActivity.rotate("nopqrstuvwxyz"));
    }

    @Test
    public void uppercaseAtoM() {
        assertEquals("NOPQRSTUVWXYZ", MainActivity.rotate("ABCDEFGHIJKLM"));
    }

    @Test
    public void uppercaseNtoZ() {
        assertEquals("ABCDEFGHIJKLM", MainActivity.rotate("NOPQRSTUVWXYZ"));
    }

    @Test
    public void fullLowercaseAlphabet() {
        assertEquals("nopqrstuvwxyzabcdefghijklm", MainActivity.rotate("abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    public void fullUppercaseAlphabet() {
        assertEquals("NOPQRSTUVWXYZABCDEFGHIJKLM", MainActivity.rotate("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
    }

    @Test
    public void mixedCase() {
        assertEquals("Uryyb, Jbeyq!", MainActivity.rotate("Hello, World!"));
    }

    @Test
    public void numbersUnchanged() {
        assertEquals("1234567890", MainActivity.rotate("1234567890"));
    }

    @Test
    public void punctuationUnchanged() {
        assertEquals("!@#$% ^&*()", MainActivity.rotate("!@#$% ^&*()"));
    }

    @Test
    public void involution() {
        // ROT13 applied twice must return the original string
        String original = "The Quick Brown Fox Jumps Over The Lazy Dog!";
        assertEquals(original, MainActivity.rotate(MainActivity.rotate(original)));
    }

    @Test
    public void boundaryLetters() {
        // n and a are the critical boundary chars for each half of the alphabet
        assertEquals("an", MainActivity.rotate("na"));
        assertEquals("AN", MainActivity.rotate("NA"));
    }
}
