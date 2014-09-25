package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetFormDataHandlerTest extends Assert {

    GetFormDataHandler getFormDataHandler = new GetFormDataHandler();
    FormData formData;
    DataRowService dataRowService = mock(DataRowService.class);

    @Before
    public void init() {
        formData = new FormData();
        formData.setId(1L);
        formData.setManual(false);
        ReflectionTestUtils.setField(getFormDataHandler, "dataRowService", dataRowService);
    }

    @Test
    public void testFillFormAndTemplateDataFalse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        when(dataRowService.getRowCount(any(TAUserInfo.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(1);
        Method checkManualMode = getFormDataHandler.getClass().getDeclaredMethod("checkManualMode", FormData.class, boolean.class);
        checkManualMode.setAccessible(true);
        checkManualMode.invoke(getFormDataHandler, formData, false);
        assertFalse(formData.isManual());
    }

    @Test
    public void testFillFormAndTemplateDataTrue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        when(dataRowService.getRowCount(any(TAUserInfo.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(0);
        Method checkManualMode = getFormDataHandler.getClass().getDeclaredMethod("checkManualMode", FormData.class, boolean.class);
        checkManualMode.setAccessible(true);
        checkManualMode.invoke(getFormDataHandler, formData, true);
        assertTrue(formData.isManual());
    }
}