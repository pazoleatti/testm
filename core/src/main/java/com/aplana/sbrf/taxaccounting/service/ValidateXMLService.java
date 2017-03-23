package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.File;

public interface ValidateXMLService {

    /**
     * Адаптер для проверки xml с помощью сторонней библиотеки.
     * Exe-файл находится в ресурсах. Sources для сборки exe в модуле schematron.
     * При возвращении результата ориентируется на вывод библиотеки(ключевое слово SUCCESS),
     * в случае ошибки выводятся сообщения в лог.
     * @param data декларация
     * @param userInfo информация пользователя
     * @return true-если валидация прошла успешно
     */
    boolean validate(DeclarationData data, TAUserInfo userInfo, Logger logger, boolean isErrorFatal, File xmlFile, String fileName, String xsdBlobDataId);
}
