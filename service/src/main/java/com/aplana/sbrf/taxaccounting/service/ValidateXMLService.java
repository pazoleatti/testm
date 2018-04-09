package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.File;
import java.io.InputStream;

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

    /**
     * Вылидирует произвольный xml-файл по произвольной xsd-схеме с использованием схематрона (schematron)
     * @param userInfo данные пользователя
     * @param logger логгер, в который пишутся ошибки валидации
     * @param isErrorFatal true - ошибки в лог пишутся с уровнем LogLevel#ERROR, иначе - LogLevel#WARNING
     * @param xmlFileName название xml-файла
     * @param xmlFile содержимое xml-файла
     * @param xsdFileName название xsd-файла
     * @param xsdStream содержимое xsd
     * @return файл корректен?
     */
    boolean validate(TAUserInfo userInfo, Logger logger, boolean isErrorFatal, String xmlFileName, File xmlFile, String xsdFileName, InputStream xsdStream);
}
