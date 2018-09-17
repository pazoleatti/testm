package com.aplana.sbrf.taxaccounting.dao.impl.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class SortDirectionTest {

    @Test
    public void test_toString_isLowerCase() {
        assertThat(SortDirection.ASC.toString()).isEqualTo("asc");
    }

    @Test
    public void test_opposite() {
        assertThat(SortDirection.ASC.opposite()).isEqualTo(SortDirection.DESC);
        assertThat(SortDirection.DESC.opposite()).isEqualTo(SortDirection.ASC);
    }

    @Test
    public void test_isAsc() {
        assertThat(SortDirection.ASC.isAsc()).isTrue();
        assertThat(SortDirection.DESC.isAsc()).isFalse();
    }

    @Test
    public void test_of_boolean() {
        assertThat(SortDirection.of(true)).isEqualTo(SortDirection.ASC);
        assertThat(SortDirection.of(false)).isEqualTo(SortDirection.DESC);
    }

    @Test
    public void test_of_string_isCaseInsensitive() {
        assertThat(SortDirection.of("DESC")).isEqualTo(SortDirection.DESC);
        assertThat(SortDirection.of("asc")).isEqualTo(SortDirection.ASC);
    }

    @Test
    public void test_of_forEmptyString() {
        assertThat(SortDirection.of(null)).isEqualTo(SortDirection.ASC);
        assertThat(SortDirection.of("")).isEqualTo(SortDirection.ASC);
    }
}