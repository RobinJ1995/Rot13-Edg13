package be.robinj.rot13;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

    @Test
    public void everyLowercaseLetterShiftsByThirteen() {
        // Each of the 26 letters must map to the letter 13 positions further
        // along, wrapping around the alphabet.
        for (int i = 0; i < 26; i++) {
            char in = (char) ('a' + i);
            char expected = (char) ('a' + ((i + 13) % 26));
            assertEquals("rotate('" + in + "')",
                    String.valueOf(expected), MainActivity.rotate(String.valueOf(in)));
        }
    }

    @Test
    public void everyUppercaseLetterShiftsByThirteen() {
        for (int i = 0; i < 26; i++) {
            char in = (char) ('A' + i);
            char expected = (char) ('A' + ((i + 13) % 26));
            assertEquals("rotate('" + in + "')",
                    String.valueOf(expected), MainActivity.rotate(String.valueOf(in)));
        }
    }

    @Test
    public void singleLetterBoundaries() {
        // The four corners of each 13-letter half.
        assertEquals("n", MainActivity.rotate("a"));
        assertEquals("z", MainActivity.rotate("m"));
        assertEquals("a", MainActivity.rotate("n"));
        assertEquals("m", MainActivity.rotate("z"));
        assertEquals("N", MainActivity.rotate("A"));
        assertEquals("Z", MainActivity.rotate("M"));
        assertEquals("A", MainActivity.rotate("N"));
        assertEquals("M", MainActivity.rotate("Z"));
    }

    @Test
    public void charactersAdjacentToLettersAreUnchanged() {
        // The ASCII characters immediately outside the A-Z / a-z ranges must
        // never be shifted: '@'(64) before 'A', '['(91) after 'Z',
        // '`'(96) before 'a', '{'(123) after 'z'.
        assertEquals("@[`{", MainActivity.rotate("@[`{"));
    }

    @Test
    public void whitespaceIsPreserved() {
        // Spaces, tabs and newlines separating letters must survive intact.
        assertEquals("uryyb\tjbeyq", MainActivity.rotate("hello\tworld"));
        assertEquals("n o\tp\nq", MainActivity.rotate("a b\tc\nd"));
    }

    @Test
    public void unicodeCharactersAreUnchanged() {
        // Accented and non-Latin characters fall outside the ASCII letter
        // ranges and must pass through untouched.
        assertEquals("Pnsé ñ 你好", MainActivity.rotate("Café ñ 你好"));
    }

    @Test
    public void digitsInterspersedWithLetters() {
        assertEquals("n1o2p3", MainActivity.rotate("a1b2c3"));
    }

    @Test
    public void isSelfInverseAcrossPrintableAscii() {
        // Applying ROT13 twice to every printable ASCII character must yield
        // the identity, and a single application must actually change the
        // letters (i.e. it is not a no-op).
        StringBuilder sb = new StringBuilder();
        for (char c = 32; c < 127; c++) {
            sb.append(c);
        }
        String original = sb.toString();
        String once = MainActivity.rotate(original);
        assertEquals(original, MainActivity.rotate(once));
        // A single application must genuinely transform the input (the 52
        // letters shift), so it is not a silent pass-through.
        assertNotEquals(original, once);
    }

    @Test
    public void longTextRoundTrips() {
        String original =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                        + "The five boxing wizards jump quickly! 0123456789";
        assertEquals(original, MainActivity.rotate(MainActivity.rotate(original)));
    }

    @Test
    public void knownCiphertextPair() {
        // "Why did the chicken cross the road?" is a classic ROT13 sample.
        assertEquals("Jul qvq gur puvpxra pebff gur ebnq?",
                MainActivity.rotate("Why did the chicken cross the road?"));
    }
}
