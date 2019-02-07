package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    static final String RUS_CODE = "643";

    /**
     * Статусы налогоплательщика 5 - Налогоплательщик - иностранный гражданин (лицо без гражданства) признан беженцем или получивший временное убежище на территории Российской Федерации, не является налоговым резидентом Российской Федерации
     */
    private static final String STATUS_5 = "5";

    /**
     *
     */
    static final String RUS_PASSPORT_21 = "21";
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
    public static IdDoc selectIncludeReportDocument(NaturalPerson naturalPerson, List<IdDoc> personDocumentList) {

        if (personDocumentList == null || personDocumentList.isEmpty()) {
            return null;
        }

        //Если запись одна
        if (personDocumentList.size() == 1) {
            return personDocumentList.get(0);
        }

        //гражданство (необяз.)
        RefBookCountry citizenship = naturalPerson.getCitizenship();
        String citizenshipCode = citizenship != null ? citizenship.getCode() : null;

        //статус (обяз.) по умолчанию = 1 (резидент РФ)
        String taxPayerStatusCode = naturalPerson.getTaxPayerState() != null ? naturalPerson.getTaxPayerState().getCode() : null;

        if (citizenshipCode == null || citizenshipCode.isEmpty()) {
            //4. Если ЗСФЛ."Гражданство" пуст
            return selectEmptyCitezenship(personDocumentList);
        } else if (citizenshipCode.equals(RUS_CODE)) {
            //1. Если ЗСФЛ."Гражданство" = RUS
            return selectRussianCitizenshipDocuments(personDocumentList);
        } else if (!citizenshipCode.equals(RUS_CODE) && !STATUS_5.equals(taxPayerStatusCode)) {
            //2. Если ЗСФЛ."Гражданство" ≠ RUS и не пуст и ЗСФЛ."Статус налогоплательщика"  ≠  5
            return selectNonRussianDocuments(personDocumentList);
        } else if (!citizenshipCode.equals(RUS_CODE) && STATUS_5.equals(taxPayerStatusCode)) {
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
    private static IdDoc selectRussianCitizenshipDocuments(List<IdDoc> personDocumentList) {

        List<IdDoc> rusPassports = findDocumentsByTypeCode(personDocumentList, RUS_PASSPORT_21);

        if (rusPassports.size() == 1) {
            //а. Если есть один <ДУЛ>."Код ДУЛ" = 21, то у данной записи устанавливаем <ДУЛ>."Включается в отчетность" = 1

            return rusPassports.get(0);
        } else if (rusPassports.size() > 1) {
            //b. Если есть несколько <ДУЛ>."Код ДУЛ" = 21
            List<IdDoc> maximumPassportSeriesNum = selectMaximumPassportSeriesNum(rusPassports);
            if (maximumPassportSeriesNum.size() == 1) {
                //Если вторые две цифры у <ДУЛ> не равны -> выбрать запись, у которой вторые две цифры больше
                return maximumPassportSeriesNum.get(0);
            }
            //Если вторые две цифры у <ДУЛ> равны -> выбрать запись, у которой последние шесть цифр больше
            return selectMaximumPassportDocumentNum(rusPassports);
        } else {
            //c. Иначе ( <ДУЛ>."Код ДУЛ ≠ 21)
            List<IdDoc> rusPassportsTemp = findDocumentsByTypeCode(personDocumentList, RUS_PASSPORT_TEMP_14);
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
    private static IdDoc selectNonRussianDocuments(List<IdDoc> personDocumentList) {
        IdDoc documentCode10 = selectDocumentByCode(personDocumentList, DOC_CODE_10);
        if (documentCode10 != null) {
            return documentCode10;
        } else {
            IdDoc documentCode12 = selectDocumentByCode(personDocumentList, DOC_CODE_12);
            if (documentCode12 != null) {
                return documentCode12;
            } else {
                IdDoc documentCode15 = selectDocumentByCode(personDocumentList, DOC_CODE_15);
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
    private static IdDoc selectNonRussianTaxpayerDocuments(List<IdDoc> personDocumentList) {
        IdDoc documentCode13 = selectDocumentByCode(personDocumentList, DOC_CODE_13);
        if (documentCode13 != null) {
            return documentCode13;
        } else {
            IdDoc documentCode11 = selectDocumentByCode(personDocumentList, DOC_CODE_11);
            if (documentCode11 != null) {
                return documentCode11;
            } else {
                IdDoc documentCode18 = selectDocumentByCode(personDocumentList, DOC_CODE_18);
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
    private static IdDoc selectEmptyCitezenship(List<IdDoc> personDocumentList) {
        IdDoc documentCode12 = selectDocumentByCode(personDocumentList, DOC_CODE_12);
        if (documentCode12 != null) {
            return documentCode12;
        } else {
            IdDoc documentCode15 = selectDocumentByCode(personDocumentList, DOC_CODE_15);
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
    private static IdDoc selectDocumentByCode(List<IdDoc> personDocumentList, String code) {
        List<IdDoc> selectedDocumentsList = findDocumentsByTypeCode(personDocumentList, code);
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
    private static boolean isSeriesNumEquals(List<IdDoc> personDocumentList) {
        Integer currentSeriesDocNumber = extractSeriesDigits(personDocumentList.get(0).getDocumentNumber());
        if (currentSeriesDocNumber != null) {
            for (IdDoc personDocument : personDocumentList) {
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
     * Выбрать записи, у которых вторые две цифры имеют наибольшее значение
     *
     * @param personDocumentList список объектов ДУЛ для сравнения
     * @return  список объектов ДУЛ у которых самые большие 2 цифры
     */
    private static List<IdDoc> selectMaximumPassportSeriesNum(List<IdDoc> personDocumentList) {
        Collections.sort(personDocumentList, new Comparator<IdDoc>() {
            @Override
            public int compare(IdDoc o1, IdDoc o2) {
                Integer o2Series = extractSeriesDigits(o2.getDocumentNumber());
                Integer o1Series = extractSeriesDigits(o1.getDocumentNumber());
                if (o1Series == null && o2Series == null) {
                    return 0;
                } else if (o2Series == null) {
                    return 1;
                } else if (o1Series == null) {
                    return -1;
                }
                return new SeriesComparator().compare(o2Series, o1Series);
            }
        });
        Integer maxSeriesNumber = extractSeriesDigits(personDocumentList.get(0).getDocumentNumber());
        List<IdDoc> toReturn = new ArrayList<>();

        for (IdDoc personDocument : personDocumentList) {
            Integer docSeries = extractSeriesDigits(personDocument.getDocumentNumber());
            if (docSeries != null && (docSeries.equals(maxSeriesNumber))) {
                toReturn.add(personDocument);
            } else {
                break;
            }
        }
        return toReturn;
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
    private static IdDoc selectMaximumPassportDocumentNum(List<IdDoc> personDocumentList) {

        IdDoc maxNumberPersonDocument = null;
        Integer maxDocNumber = null;

        for (IdDoc personDocument : personDocumentList) {
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
    private static IdDoc selectMaximumDocumentNumber(List<IdDoc> personDocumentList) {

        IdDoc maxNumberPersonDocument = null;
        BigInteger maxDocNumber = null;

        for (IdDoc personDocument : personDocumentList) {
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
    private static List<IdDoc> findDocumentsByTypeCode(List<IdDoc> personDocumentList, String docTypeCode) {
        List<IdDoc> result = new ArrayList<IdDoc>();
        for (IdDoc personDocument : personDocumentList) {
            RefBookDocType docType = personDocument.getDocType();
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
    private static IdDoc selectMinimalPriorityDocument(List<IdDoc> personDocumentList) {

        IdDoc minimalPriorPersonDocument = null;

        for (IdDoc personDocument : personDocumentList) {

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

    public static class SeriesComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(offset(o1), offset(o2));
        }

        private Integer offset(Integer i) {
            if (i == 97) {
                return 0;
            } else if (i == 98) {
                return 1;
            } else if (i == 99) {
                return 2;
            } else {
                return i + 3;
            }
        }
    }

}
