package com.aplana.sbrf.taxaccounting.service.impl;


import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@ContextConfiguration("ConfigurationServiceTest.xml")
public class ConfigurationServiceTest {

    @Autowired
    private ConfigurationService service;

    @Autowired
    private ConfigurationDao configurationDao;


    @Test
    public void test_getParamIntValue_forCorrectValue_returnsIntValue() {
        Configuration paramWithCorrectIntValue = new Configuration();
        paramWithCorrectIntValue.setValue("128");
        when(configurationDao.fetchByEnum(any(ConfigurationParam.class))).thenReturn(paramWithCorrectIntValue);

        Integer intValue = service.getParamIntValue(ConfigurationParam.ENCRYPT_DLL);

        assertThat(intValue).isEqualTo(128);
    }

    @Test
    public void test_getParamIntValue_forNull_returnsNull() {
        Configuration paramWithNullValue = new Configuration();
        paramWithNullValue.setValue(null);
        when(configurationDao.fetchByEnum(any(ConfigurationParam.class))).thenReturn(paramWithNullValue);

        Integer intValue = service.getParamIntValue(ConfigurationParam.ENCRYPT_DLL);

        assertThat(intValue).isNull();
    }

    @Test
    public void test_getParamIntValue_forStringValue_returnsNull() {
        Configuration paramWithStringValue = new Configuration();
        paramWithStringValue.setValue("String Value");
        when(configurationDao.fetchByEnum(any(ConfigurationParam.class))).thenReturn(paramWithStringValue);

        Integer intValue = service.getParamIntValue(ConfigurationParam.ENCRYPT_DLL);

        assertThat(intValue).isNull();
    }


    @Test
    public void test_checkRowsEditCountParam_forValueLessThanParam_isSuccessful() {
        ConfigurationService serviceMock = spy(service);
        doReturn(10).when(serviceMock).getParamIntValue(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT);

        ActionResult result = serviceMock.checkRowsEditCountParam(9);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void test_checkRowsEditCountParam_forTheSameValueAsParam_isSuccessful() {
        ConfigurationService serviceMock = spy(service);
        doReturn(10).when(serviceMock).getParamIntValue(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT);

        ActionResult result = serviceMock.checkRowsEditCountParam(10);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void test_checkRowsEditCountParam_forValueMoreThanParam_isUnsuccessful() {
        ConfigurationService serviceMock = spy(service);
        doReturn(10).when(serviceMock).getParamIntValue(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT);

        ActionResult result = serviceMock.checkRowsEditCountParam(11);

        assertThat(result.isSuccess()).isFalse();
    }
}
