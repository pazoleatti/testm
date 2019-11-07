package com.aplana.sbrf.taxaccounting.service.impl.transport.edo;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.validation.ValidationResult;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.EdoMessageValidator;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EdoMessageValidatorImpl implements EdoMessageValidator {

    private final String INCOME_MESSAGE_FAIL_VALIDATION_MSG = "Входящее XML сообщение из ФП \"Фонды\", " +
            "на основании которого было создано. Транспортное Сообщение №:%s не соответствует XSD схеме.";
    private final String OUTCOME_MESSAGE_FAIL_VALIDATION_MSG = "Не выполнена проверка файла утилитой ФНС " +
            "на соответствие XSD схеме.";

    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private ValidateXMLService validateXMLService;

    @Override
    public ValidationResult validateIncomeMessage(String message) {
        RefBook declarationTypeRefBook = commonRefBookService.get(RefBook.Id.DECLARATION_TEMPLATE.getId());
        BlobData xsd = blobDataService.get(declarationTypeRefBook.getXsdId());

        ValidationResult validationResult =
                new ValidationResult(validateXMLService.validate(new Logger(), message, xsd.getName(), xsd.getInputStream()));

        if (!validationResult.isSuccess()) {
            validationResult.setMessage(INCOME_MESSAGE_FAIL_VALIDATION_MSG);
        }

        return validationResult;
    }

    @Override
    public ValidationResult validateOutcomeMessage(File file, String fileName, DeclarationData declarationData) {
        Logger validationLogger = new Logger();

        ValidationResult validationResult =
                new ValidationResult(validateXMLService.validate(declarationData, validationLogger, file, fileName, null));

        if (!validationResult.isSuccess()) {
            validationResult.setLogEntries(validationLogger.getEntries());
            validationResult.setMessage(OUTCOME_MESSAGE_FAIL_VALIDATION_MSG);
        }

        return validationResult;
    }
}
