package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.File;
import java.io.InputStream;

@ScriptExposed
public interface ValidateXMLService {

    /**
     * Адаптер для проверки xml с помощью сторонней библиотеки.
     * Exe-файл находится в ресурсах. Sources для сборки exe в модуле schematron.
     * При возвращении результата ориентируется на вывод библиотеки(ключевое слово SUCCESS),
     * в случае ошибки выводятся сообщения в лог.
     *
     * @param data декларация
     * @return true-если валидация прошла успешно
     */
    boolean validate(DeclarationData data, Logger logger, File xmlFile, String fileName, String xsdBlobDataId);

    /**
     * Валидирует произвольный xml-файл по произвольной xsd-схеме с использованием схематрона (schematron)
     *
     * @param logger      логгер, в который пишутся ошибки валидации
     * @param xmlFileName название xml-файла
     * @param xmlFile     содержимое xml-файла
     * @param xsdFileName название xsd-файла
     * @param xsdStream   содержимое xsd
     * @return файл корректен?
     */
    boolean validate(Logger logger, String xmlFileName, File xmlFile, String xsdFileName, InputStream xsdStream);

    /**
     * Валидирует произвольный XML в виде строки по произвольной xsd-схеме с использованием схематрона (schematron)
     *
     * @param logger      логгер, в который пишутся ошибки валидации
     * @param data        содержимое XML в виде строки
     * @param xsdFileName название xsd-файла
     * @param xsdStream   содержимое xsd
     * @return файл корректен?
     */
    boolean validate(Logger logger, String data, String xsdFileName, InputStream xsdStream);
}
