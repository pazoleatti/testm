package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.Country;
import com.aplana.sbrf.taxaccounting.model.identification.DocType;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация алгоритма Выбор главного ДУЛа, возвращает индекс документа который должнен быть выбран главным
 *
 * @author Andrey Drunk
 */
public class SelectPersonDocumentCalc {

    /**
     * Шаблон паттерна выбора только цифр из строки
     */
    private static final String NON_DIGITS_REGEXP = "[^0-9]";


    /**
     * Код РФ по справочнику "ОК 025-2001 (Общероссийский классификатор стран мира)"
     */
    private static final String RUS_CODE = "643";

    /**
     * Статусы налогоплательщика 5 - Налогоплательщик - иностранный гражданин (лицо без гражданства) признан беженцем или получивший временное убежище на территории Российской Федерации, не является налоговым резидентом Российской Федерации
     */
    private static final String STATUS_5 = "5";

    /**
     *
     */
    private static final String RUS_PASSPORT_21 = "21";
    private static final String RUS_PASSPORT_TEMP_14 = "14";

    /**
     *
     */
    private static final String DOC_CODE_10 = "10";
    private static final String DOC_CODE_12 = "12";
    private static final String DOC_CODE_15 = "15";

    /**
     *
     */
    private static final String DOC_CODE_13 = "13";
    private static final String DOC_CODE_11 = "11";
    private static final String DOC_CODE_18 = "18";

    /**
     * Реализация алгоритма Выбор главного ДУЛа, возвращает индекс документа который должнен быть выбран главным
     *
     * @param personDocumentList список документов ФЛ
     * @return индекс главного документа в списке
     */
    public static PersonDocument selectIncludeReportDocument(NaturalPerson naturalPerson, List<PersonDocument> personDocumentList) {

        if (personDocumentList == null || personDocumentList.isEmpty()) {
            return null;
        }

        //Если запись одна
        if (personDocumentList.size() == 1) {
            return personDocumentList.get(0);
        }

        //гражданство (необяз.)
        Country citizenship = naturalPerson.getCitizenship();
        String citizenshipCode = citizenship != null ? citizenship.getCode() : null;

        //статус (обяз.) по умолчанию = 1 (резидент РФ)
        String taxPayerStatusCode = naturalPerson.getTaxPayerStatus().getCode();

        if (citizenshipCode == null || citizenshipCode.isEmpty()) {
            //4. Если ЗСФЛ."Гражданство" пуст
            return selectEmptyCitezenship(personDocumentList);
        } else if (citizenshipCode.equals(RUS_CODE)) {
            //1. Если ЗСФЛ."Гражданство" = RUS
            return selectRussianCitizenshipDocuments(personDocumentList);
        } else if (!citizenshipCode.equals(RUS_CODE) && !taxPayerStatusCode.equals(STATUS_5)) {
            //2. Если ЗСФЛ."Гражданство" ≠ RUS и не пуст и ЗСФЛ."Статус налогоплательщика"  ≠  5
            return selectNonRussianDocuments(personDocumentList);
        } else if (!citizenshipCode.equals(RUS_CODE) && taxPayerStatusCode.equals(STATUS_5)) {
            //3. Если ЗСФЛ."Гражданство" ≠ RUS и ЗСФЛ."Статус налогоплательщика" = 5
            return selectNonRussianTaxpayerDocuments(personDocumentList);
        } else {
            return selectMinimalPriorityDocument(personDocumentList);
        }

    }

    /**
     * 2. Если ЗСФЛ."Гражданство" = RUS
     *
     * @param personDocumentList
     * @return
     */
    private static PersonDocument selectRussianCitizenshipDocuments(List<PersonDocument> personDocumentList) {

        List<PersonDocument> rusPassports = findDocumentsByTypeCode(personDocumentList, RUS_PASSPORT_21);

        if (rusPassports.size() == 1) {
            //а. Если есть один <ДУЛ>."Код ДУЛ" = 21, то у данной записи устанавливаем <ДУЛ>."Включается в отчетность" = 1
            return rusPassports.get(0);
        } else if (rusPassports.size() > 1) {
            //b. Если есть несколько <ДУЛ>."Код ДУЛ" = 21
            if (isSeriesNumEquals(rusPassports)) {
                //Если вторые две цифры у <ДУЛ> равны -> выбрать запись, у которой последние шесть цифр больше
                return selectMaximumPassportDocumentNum(rusPassports);
            } else {
                //Если вторые две цифры у <ДУЛ> не равны -> выбрать запись, у которой вторые две цифры больше
                return selectMaximumPassportSeriesNum(rusPassports);
            }
        } else {
            //c. Иначе ( <ДУЛ>."Код ДУЛ ≠ 21)
            List<PersonDocument> rusPassportsTemp = findDocumentsByTypeCode(personDocumentList, RUS_PASSPORT_TEMP_14);
            if (rusPassportsTemp.size() == 1) {
                //i. Если есть один <ДУЛ>."Код ДУЛ = 14
                return rusPassportsTemp.get(0);
            } else if (rusPassportsTemp.size() > 1) {
                //ii. Если есть несколько <ДУЛ>."Код ДУЛ" = 14 -> Выбрать запись с большим значением номера
                return selectMaximumDocumentNumber(rusPassportsTemp);
            } else {
                //iii. Иначе (ДУЛ>."Код ДУЛ ≠ 14 -> Выбирается запись  с минимальным значением ЗСФЛ."Документы, удостоверяющие личность".Приоритет
                return selectMinimalPriorityDocument(personDocumentList);
            }
        }

    }

    /**
     * 3. Если ЗСФЛ."Гражданство" ≠ RUS и не пуст и ЗСФЛ."Статус налогоплательщика"  ≠  5
     *
     * @param personDocumentList
     * @return
     */
    private static PersonDocument selectNonRussianDocuments(List<PersonDocument> personDocumentList) {
        PersonDocument documentCode10 = selectDocumentByCode(personDocumentList, DOC_CODE_10);
        if (documentCode10 != null) {
            return documentCode10;
        } else {
            PersonDocument documentCode12 = selectDocumentByCode(personDocumentList, DOC_CODE_12);
            if (documentCode12 != null) {
                return documentCode12;
            } else {
                PersonDocument documentCode15 = selectDocumentByCode(personDocumentList, DOC_CODE_15);
                if (documentCode15 != null) {
                    return documentCode15;
                } else {
                    return selectMinimalPriorityDocument(personDocumentList);
                }
            }
        }
    }

    /**
     * 4. Если ЗСФЛ."Гражданство" ≠ RUS и ЗСФЛ."Статус налогоплательщика" = 5
     *
     * @param personDocumentList
     * @return
     */
    private static PersonDocument selectNonRussianTaxpayerDocuments(List<PersonDocument> personDocumentList) {
        PersonDocument documentCode13 = selectDocumentByCode(personDocumentList, DOC_CODE_13);
        if (documentCode13 != null) {
            return documentCode13;
        } else {
            PersonDocument documentCode11 = selectDocumentByCode(personDocumentList, DOC_CODE_11);
            if (documentCode11 != null) {
                return documentCode11;
            } else {
                PersonDocument documentCode18 = selectDocumentByCode(personDocumentList, DOC_CODE_18);
                if (documentCode18 != null) {
                    return documentCode18;
                } else {
                    return selectMinimalPriorityDocument(personDocumentList);
                }
            }
        }
    }

    /**
     * Если ЗСФЛ."Гражданство" пуст
     *
     * @param personDocumentList
     * @return
     */
    private static PersonDocument selectEmptyCitezenship(List<PersonDocument> personDocumentList) {
        PersonDocument documentCode12 = selectDocumentByCode(personDocumentList, DOC_CODE_12);
        if (documentCode12 != null) {
            return documentCode12;
        } else {
            PersonDocument documentCode15 = selectDocumentByCode(personDocumentList, DOC_CODE_15);
            if (documentCode15 != null) {
                return documentCode15;
            } else {
                return selectMinimalPriorityDocument(personDocumentList);
            }
        }
    }

    /**
     * Выбрать документ по коду типа, если найдено несколько документов то выбирается документ с наибольшим номером
     *
     * @param personDocumentList
     * @param code
     * @return
     */
    private static PersonDocument selectDocumentByCode(List<PersonDocument> personDocumentList, String code) {
        List<PersonDocument> selectedDocumentsList = findDocumentsByTypeCode(personDocumentList, code);
        if (selectedDocumentsList.size() == 1) {
            //Если есть один <ДУЛ>."Код ДУЛ = code, то
            return selectedDocumentsList.get(0);
        } else if (selectedDocumentsList.size() > 1) {
            //Если есть несколько <ДУЛ>."Код ДУЛ" = code -> Выбрать запись с большим значением номера
            return selectMaximumDocumentNumber(selectedDocumentsList);
        } else {
            return null;
        }
    }

    /**
     * Проверяет условие "Если вторые две цифры у всех <ДУЛ> равны"
     *
     * @param personDocumentList список документов
     * @return
     */
    private static boolean isSeriesNumEquals(List<PersonDocument> personDocumentList) {
        Integer currentSeriesDocNumber = extractSeriesDigits(personDocumentList.get(0).getDocumentNumber());
        if (currentSeriesDocNumber != null) {
            for (PersonDocument personDocument : personDocumentList) {
                Integer seriesDocNumber = extractSeriesDigits(personDocument.getDocumentNumber());
                if (!currentSeriesDocNumber.equals(seriesDocNumber)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    /**
     * Выбрать запись, у которой вторые две цифры больше
     *
     * @param personDocumentList
     * @return
     */
    private static PersonDocument selectMaximumPassportSeriesNum(List<PersonDocument> personDocumentList) {
        PersonDocument maxSeriesPersonDocument = null;
        Integer maxSeriesNumber = null;
        for (PersonDocument personDocument : personDocumentList) {
            if (maxSeriesPersonDocument == null || maxSeriesNumber == null) {
                maxSeriesPersonDocument = personDocument;
                maxSeriesNumber = extractSeriesDigits(personDocument.getDocumentNumber());
            }
            Integer docSeries = extractSeriesDigits(personDocument.getDocumentNumber());
            if (docSeries != null && (docSeries > maxSeriesNumber)) {
                maxSeriesNumber = docSeries;
                maxSeriesPersonDocument = personDocument;
            }
        }
        return maxSeriesPersonDocument;
    }


    /**
     * Из номера документа получить вторые две цифры в виде целого числа
     *
     * @param documentNumber
     * @return
     */
    public static Integer extractSeriesDigits(String documentNumber) {
        String docNumber = removeAllNonDigits(documentNumber);
        if (docNumber != null && docNumber.length() >= 4) {
            return Integer.parseInt(docNumber.substring(2, 4));
        } else {
            return null;
        }
    }


    /**
     * Выбрать запись, у которой последние шесть цифр больше
     *
     * @param personDocumentList
     * @return
     */
    private static PersonDocument selectMaximumPassportDocumentNum(List<PersonDocument> personDocumentList) {

        PersonDocument maxNumberPersonDocument = null;
        Integer maxDocNumber = null;

        for (PersonDocument personDocument : personDocumentList) {
            if (maxNumberPersonDocument == null || maxDocNumber == null) {
                maxNumberPersonDocument = personDocument;
                maxDocNumber = extractPassportDocumentsDigits(personDocument.getDocumentNumber());
            }
            Integer docNumber = extractPassportDocumentsDigits(personDocument.getDocumentNumber());
            if (docNumber != null && (docNumber > maxDocNumber)) {
                maxDocNumber = docNumber;
                maxNumberPersonDocument = personDocument;
            }
        }

        return maxNumberPersonDocument;
    }

    /**
     * Получить последние шесть цифр номера пасспорта
     *
     * @param documentNumber
     * @return
     */
    public static Integer extractPassportDocumentsDigits(String documentNumber) {
        String docNumber = removeAllNonDigits(documentNumber);
        if (docNumber != null && docNumber.length() >= 6) {
            return Integer.parseInt(docNumber.substring(docNumber.length() - 6, docNumber.length()));
        } else {
            return null;
        }
    }


    /**
     * Удалить из <ДУЛ>."Серия и номер ДУЛ" все символы кроме цифр и выбрать запись с большим значением номера
     *
     * @param personDocumentList
     * @return
     */
    private static PersonDocument selectMaximumDocumentNumber(List<PersonDocument> personDocumentList) {

        PersonDocument maxNumberPersonDocument = null;
        BigInteger maxDocNumber = null;

        for (PersonDocument personDocument : personDocumentList) {
            //initialize
            if (maxNumberPersonDocument == null || maxDocNumber == null) {
                maxNumberPersonDocument = personDocument;
                maxDocNumber = extractDocNumberDigits(personDocument.getDocumentNumber());
            }

            //compare
            BigInteger docNumber = extractDocNumberDigits(personDocument.getDocumentNumber());
            if (docNumber != null && docNumber.compareTo(maxDocNumber) > 0) {
                maxDocNumber = docNumber;
                maxNumberPersonDocument = personDocument;
            }
        }

        return maxNumberPersonDocument;
    }

    /**
     * Найти все документы по коду типа
     *
     * @param personDocumentList
     * @param docTypeCode
     * @return
     */
    private static List<PersonDocument> findDocumentsByTypeCode(List<PersonDocument> personDocumentList, String docTypeCode) {
        List<PersonDocument> result = new ArrayList<PersonDocument>();
        for (PersonDocument personDocument : personDocumentList) {
            DocType docType = personDocument.getDocType();
            if (docType != null && docType.getCode() != null && docTypeCode.equals(docType.getCode())) {
                result.add(personDocument);
            }
        }
        return result;
    }


    /**
     * Из серии и номера документа получить число для сравнения
     *
     * @param documentNumber
     * @return
     */
    public static BigInteger extractDocNumberDigits(String documentNumber) {
        String docNumber = removeAllNonDigits(documentNumber);
        if (docNumber != null && !docNumber.isEmpty()) {
            return new BigInteger(docNumber);
        } else {
            return null;
        }
    }

    /**
     * Выбрать из списка документ с минимальным приоритетом
     *
     * @param personDocumentList должно выполнятся условие, список не пустой и в нем более 1 документа
     * @return
     */
    private static PersonDocument selectMinimalPriorityDocument(List<PersonDocument> personDocumentList) {

        PersonDocument minimalPriorPersonDocument = null;

        for (PersonDocument personDocument : personDocumentList) {

            if (personDocument.getDocType() != null && personDocument.getDocType().getPriority() != null) {

                //initialize
                if (minimalPriorPersonDocument == null) {
                    minimalPriorPersonDocument = personDocument;
                }

                Integer priority = personDocument.getDocType().getPriority();

                if (priority < minimalPriorPersonDocument.getDocType().getPriority()) {
                    minimalPriorPersonDocument = personDocument;
                }

            }

        }
        return minimalPriorPersonDocument;
    }

    /**
     * Удалить все символы из строки кроме цифр
     *
     * @param docNumberBody
     * @return
     */
    private static String removeAllNonDigits(String docNumberBody) {
        if (docNumberBody != null) {
            return docNumberBody.replaceAll(NON_DIGITS_REGEXP, "");
        } else {
            return null;
        }
    }

}
