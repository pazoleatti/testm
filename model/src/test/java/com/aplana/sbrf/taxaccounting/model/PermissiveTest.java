package com.aplana.sbrf.taxaccounting.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PermissiveTest {

    @Test
    public void test_forbidden() {
        Permissive<String> permissive = Permissive.forbidden();
        assertThat(permissive.hasPermission()).isFalse();
        assertThat(permissive.value()).isNull();
    }

    @Test
    public void test_stringValue() {
        Permissive<String> permissive = Permissive.of("String");
        assertThat(permissive.hasPermission()).isTrue();
        assertThat(permissive.value()).isEqualTo("String");
    }

    @Test
    public void test_nullValue() {
        Permissive<Object> permissive = Permissive.of(null);
        assertThat(permissive.hasPermission()).isTrue();
        assertThat(permissive.value()).isNull();
    }
}