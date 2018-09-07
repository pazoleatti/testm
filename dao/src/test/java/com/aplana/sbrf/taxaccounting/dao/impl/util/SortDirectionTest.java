package com.aplana.sbrf.taxaccounting.dao.impl.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class SortDirectionTest {

    @Test
    public void test_toString_isLowerCase() {
        assertThat(SortDirection.ASC.toString()).isEqualTo("asc");
    }

    @Test
    public void test_isAsc() {
        assertThat(SortDirection.ASC.isAsc()).isTrue();
        assertThat(SortDirection.DESC.isAsc()).isFalse();
    }

    @Test
    public void test_getByAsc() {
        assertThat(SortDirection.getByAsc(true)).isEqualTo(SortDirection.ASC);
        assertThat(SortDirection.getByAsc(false)).isEqualTo(SortDirection.DESC);
    }

    @Test
    public void test_getByValue_caseInsensitive() {
        assertThat(SortDirection.getByValue("DESC")).isEqualTo(SortDirection.DESC);
        assertThat(SortDirection.getByValue("asc")).isEqualTo(SortDirection.ASC);
    }

    @Test
    public void test_getByValue_forEmpty() {
        assertThat(SortDirection.getByValue(null)).isEqualTo(SortDirection.ASC);
        assertThat(SortDirection.getByValue("")).isEqualTo(SortDirection.ASC);
    }
}