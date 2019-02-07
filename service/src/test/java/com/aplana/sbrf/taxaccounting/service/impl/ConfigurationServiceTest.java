package com.aplana.sbrf.taxaccounting.service.impl;


import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;


/**
 * Перенастроил класс для Spring-тестов, но все проверявшиеся здесь методы были удалены, остался пустым.
 * Надеюсь, вам пригодится для будущих тестов. Удалять жалко, т.к. всё настроено.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("ConfigurationServiceTest.xml")
public class ConfigurationServiceTest {

    @Autowired
    private ConfigurationService service;

    @Test
    public void removeMe() {
        assertThat(service).isNotNull();
    }
}
