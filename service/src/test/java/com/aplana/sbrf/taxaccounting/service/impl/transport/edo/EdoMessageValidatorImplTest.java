package com.aplana.sbrf.taxaccounting.service.impl.transport.edo;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.validation.ValidationResult;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class EdoMessageValidatorImplTest {

    @InjectMocks
    private EdoMessageValidatorImpl edoMessageValidator;

    @Mock
    private ValidateXMLService validateXMLService;
    @Mock
    private CommonRefBookService commonRefBookService;
    @Mock
    private BlobDataService blobDataService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateIncomeMessageTestPositive() {
        String xsdId = "xsdId";
        String xsdName = "xsdName";
        InputStream xsdInputStream = IOUtils.toInputStream("inputStream");

        RefBook refBook = new RefBook();
        refBook.setXsdId(xsdId);

        BlobData blobData = new BlobData();
        blobData.setName(xsdName);
        blobData.setInputStream(xsdInputStream);

        when(commonRefBookService.get(anyLong())).thenReturn(refBook);
        when(blobDataService.get(xsdId)).thenReturn(blobData);
        when(validateXMLService.validate(any(Logger.class), anyString(), eq(xsdName), eq(xsdInputStream)))
                .thenReturn(true);

        ValidationResult validationResult = edoMessageValidator.validateIncomeMessage("message");
        assertTrue(validationResult.isSuccess());
    }

    @Test
    public void validateIncomeMessageTestNegative() {
        String xsdId = "xsdId";
        String xsdName = "xsdName";
        InputStream xsdInputStream = IOUtils.toInputStream("inputStream");

        RefBook refBook = new RefBook();
        refBook.setXsdId(xsdId);

        BlobData blobData = new BlobData();
        blobData.setName(xsdName);
        blobData.setInputStream(xsdInputStream);

        when(commonRefBookService.get(anyLong())).thenReturn(refBook);
        when(blobDataService.get(xsdId)).thenReturn(blobData);
        when(validateXMLService.validate(any(Logger.class), anyString(), eq(xsdName), eq(xsdInputStream)))
                .thenReturn(false);

        ValidationResult validationResult = edoMessageValidator.validateIncomeMessage("message");
        assertFalse(validationResult.isSuccess());
        assertEquals("Входящее XML сообщение из ФП \"Фонды\", на основании которого было создано. " +
                "Транспортное Сообщение №:%s не соответствует XSD схеме.", validationResult.getMessage());
    }

    @Test
    public void validateOutcomeMessageTestPositive() throws Exception {
        when(validateXMLService.validate(any(DeclarationData.class), any(Logger.class), any(File.class), anyString(), anyString()))
                .thenReturn(true);

        ValidationResult validationResult =
                edoMessageValidator.validateOutcomeMessage(emptyTempFile(), "fileName", new DeclarationData());
        assertTrue(validationResult.isSuccess());
    }

    @Test
    public void validateOutcomeMessageTestNegative() throws Exception {
        when(validateXMLService.validate(any(DeclarationData.class), any(Logger.class), any(File.class), anyString(), anyString()))
                .thenReturn(false);

        ValidationResult validationResult =
                edoMessageValidator.validateOutcomeMessage(emptyTempFile(), "fileName", new DeclarationData());
        assertFalse(validationResult.isSuccess());
        assertEquals("Не выполнена проверка файла утилитой ФНС на соответствие XSD схеме.", validationResult.getMessage());
    }

    private File emptyTempFile() throws IOException {
        return File.createTempFile("prefix", "sufix");
    }
}