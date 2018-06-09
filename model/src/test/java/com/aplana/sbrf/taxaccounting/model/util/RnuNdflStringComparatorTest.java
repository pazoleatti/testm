package com.aplana.sbrf.taxaccounting.model.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class RnuNdflStringComparatorTest {
    @Test
    public void testComparing() {
        List<String> tested = new ArrayList<>();

        for (char c = 0; c < 128; c++) {
            tested.add(String.valueOf(c));
        }

        for (char c = 1040; c < 1104; c++) {
            tested.add(String.valueOf(c));
        }

        tested.add("ё");
        tested.add("Ё");
        tested.add("№");

        Collections.shuffle(tested, new Random(0L));

        Comparator<String> comparator = RnuNdflStringComparator.INSTANCE;
        Collections.sort(tested, comparator);

        List<String> reference = new ArrayList<>();
        for (char c = 0; c < 33; c++) {
            reference.add(String.valueOf(c));
        }

        reference.add(String.valueOf((char) 127));

        for (char c = 33; c < 65; c++) {
            reference.add(String.valueOf(c));
        }

        reference.add("[");
        reference.add("\\");
        reference.add("]");
        reference.add("^");
        reference.add("_");
        reference.add("`");
        reference.add("{");
        reference.add("|");
        reference.add("}");
        reference.add("~");
        reference.add("№");
        reference.add("A");
        reference.add("a");
        reference.add("B");
        reference.add("b");
        reference.add("C");
        reference.add("c");
        reference.add("D");
        reference.add("d");
        reference.add("E");
        reference.add("e");
        reference.add("F");
        reference.add("f");
        reference.add("G");
        reference.add("g");
        reference.add("H");
        reference.add("h");
        reference.add("I");
        reference.add("i");
        reference.add("J");
        reference.add("j");
        reference.add("K");
        reference.add("k");
        reference.add("L");
        reference.add("l");
        reference.add("M");
        reference.add("m");
        reference.add("N");
        reference.add("n");
        reference.add("O");
        reference.add("o");
        reference.add("P");
        reference.add("p");
        reference.add("Q");
        reference.add("q");
        reference.add("R");
        reference.add("r");
        reference.add("S");
        reference.add("s");
        reference.add("T");
        reference.add("t");
        reference.add("U");
        reference.add("u");
        reference.add("V");
        reference.add("v");
        reference.add("W");
        reference.add("w");
        reference.add("X");
        reference.add("x");
        reference.add("Y");
        reference.add("y");
        reference.add("Z");
        reference.add("z");
        reference.add("А");
        reference.add("а");
        reference.add("Б");
        reference.add("б");
        reference.add("В");
        reference.add("в");
        reference.add("Г");
        reference.add("г");
        reference.add("Д");
        reference.add("д");
        reference.add("Е");
        reference.add("е");
        reference.add("Ё");
        reference.add("ё");
        reference.add("Ж");
        reference.add("ж");
        reference.add("З");
        reference.add("з");
        reference.add("И");
        reference.add("и");
        reference.add("Й");
        reference.add("й");
        reference.add("К");
        reference.add("к");
        reference.add("Л");
        reference.add("л");
        reference.add("М");
        reference.add("м");
        reference.add("Н");
        reference.add("н");
        reference.add("О");
        reference.add("о");
        reference.add("П");
        reference.add("п");
        reference.add("Р");
        reference.add("р");
        reference.add("С");
        reference.add("с");
        reference.add("Т");
        reference.add("т");
        reference.add("У");
        reference.add("у");
        reference.add("Ф");
        reference.add("ф");
        reference.add("Х");
        reference.add("х");
        reference.add("Ц");
        reference.add("ц");
        reference.add("Ч");
        reference.add("ч");
        reference.add("Ш");
        reference.add("ш");
        reference.add("Щ");
        reference.add("щ");
        reference.add("Ъ");
        reference.add("ъ");
        reference.add("Ы");
        reference.add("ы");
        reference.add("Ь");
        reference.add("ь");
        reference.add("Э");
        reference.add("э");
        reference.add("Ю");
        reference.add("ю");
        reference.add("Я");
        reference.add("я");

        Assert.assertEquals(reference, tested);
    }
}
