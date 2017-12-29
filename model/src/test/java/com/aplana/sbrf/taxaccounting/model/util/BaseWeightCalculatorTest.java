package com.aplana.sbrf.taxaccounting.model.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BaseWeightCalculatorTest {

    @Test
    public void prepareStringDulTest() {

        String dulFixture = "4157889998";

        assertEquals(dulFixture, BaseWeightCalculator.prepareStringDul("4 1 5 7 8 8 9998"));
        assertEquals(dulFixture, BaseWeightCalculator.prepareStringDul("41-57-889998—"));
        assertEquals(dulFixture, BaseWeightCalculator.prepareStringDul("---4157//889//998"));
        assertEquals(dulFixture, BaseWeightCalculator.prepareStringDul(",  415788,99,9,8"));
        assertNotEquals(dulFixture, BaseWeightCalculator.prepareStringDul("41 57й889998"));
        assertNotEquals(dulFixture, BaseWeightCalculator.prepareStringDul("41g57 889998"));
    }
}
