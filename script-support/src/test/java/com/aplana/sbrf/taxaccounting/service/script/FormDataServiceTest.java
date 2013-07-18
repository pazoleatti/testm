package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("FormDataServiceTest.xml")
public class FormDataServiceTest {

    @Autowired
    FormDataService formDataService;

    @Test
    public void test(){
        FormData formData1 = new FormData();
        formData1.setId(1L);
        FormData formData2 = new FormData();
        formData2.setId(2L);
        System.out.println("dataRowHelperImpl: " + formDataService.getDataRowHelper(formData1));
        System.out.println("dataRowHelperImpl: " + formDataService.getDataRowHelper(formData2));
        System.out.println("dataRowHelperImpl: " + formDataService.getDataRowHelper(formData1));
        System.out.println("dataRowHelperImpl: " + formDataService.getDataRowHelper(formData2));

    }

    @Test(expected = ServiceException.class)
    public void testException(){
        formDataService.getDataRowHelper(new FormData());
    }
}
