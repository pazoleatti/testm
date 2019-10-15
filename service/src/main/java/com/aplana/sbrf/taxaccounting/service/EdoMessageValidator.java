package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.validation.ValidationResult;

import java.io.File;

public interface EdoMessageValidator {

    /**
     * Валидация входящего сообщения от ЭДО на соответствие XSD-схеме
     * <a href="https://conf.aplana.com/pages/viewpage.action?pageId=27176073">РНУ НДФЛ</a>.
     *
     * @param message XML-сообщение от ЭДО
     * @return контекст результата валидации
     */
    ValidationResult validateIncomeMessage(String message);

    /**
     * Валидация исходящего файла ОНФ в ЭДО на соответствие XSD-схеме декларации
     *
     * @param file XML-файл декларации
     * @param fileName имя XML-файла декларации
     * @param declarationData декларация
     * @return контекст реузльтата валидации
     */
    ValidationResult validateOutcomeMessage(File file, String fileName, DeclarationData declarationData);
}
