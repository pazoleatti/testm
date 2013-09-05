/**
 * Формирование XML для декларации по транспортному налогу.
 *
 * @author auldanov
 * @since 19.03.2013 16:30
 */


import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
// Форма настроек обособленного подразделения: значение атрибута 11

/*
*
*/
import groovy.time.TimeCategory
import groovy.xml.MarkupBuilder

switch (formDataEvent){

// создать && обновить
    case FormDataEvent.CREATE :
        checkAndbildXml()
        break

// удалить не обрабатываем
/*case FormDataEvent.DELETE:
    break;*/

// принять
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        break;
//  отменить принятие
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
        break;
}

/**
 * Осуществление проверк при создании + генерация xml
 */
def checkAndbildXml(){

    // проверка наличия источников в стутусе принят
    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)
    if (formDataCollection == null || formDataCollection.records.isEmpty()) {
        logger.error('Отсутствуют выходные или сводные налоговые формы в статусе "Принят". Формирование декларации невозможно.')
        return
    }
    // формируем xml

    // Получить параметры по транспортному налогу
    /** Предпослденяя дата отчетного периода на которую нужно получить настройки подразделения из справочника. */
    def reportDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)
    if (reportDate != null) {
        reportDate = reportDate.getTime() - 1
    } else{
        logger.error("Ошибка определения даты конца отчетного периода")
    }

    // получаем подразделение так как в настройках хранится record_id а не значение
    department = getModRefBookValue(30, "ID = "+declarationData.departmentId)
    departmentParamTransport = getModRefBookValue(31, "DEPARTMENT_ID = "+department.record_id, reportDate)


    if (checkTransportParams(departmentParamTransport)){
        bildXml(departmentParamTransport, formDataCollection, department)
    }
}

def bildXml(def departmentParamTransport, def formDataCollection, def department){
    def departmentId = declarationData.departmentId

    def builder = new MarkupBuilder(xml)
    if (!declarationData.isAccepted()) {
        builder.Файл(ИдФайл: declarationService.generateXmlFileId(1, departmentId, declarationData.getReportPeriodId()), ВерсПрог: departmentParamTransport.APP_VERSION, ВерсФорм:departmentParamTransport.FORMAT_VERSION) {
            Документ(
                    КНД:"1152004",
                    // TODO обсудить всплывающее окно, вынести в конф. Трансп декл
                    ДатаДок : (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"), //new Date().format("dd.MM.yyyy"),
                    Период: 34,
                    ОтчетГод: reportPeriodService.get(declarationData.reportPeriodId).taxPeriod.startDate.format('yyyy'),
                    КодНО: departmentParamTransport.TAX_ORGAN_CODE,
                    // TODO учесть что потом будут корректирующие периоды
                    НомКорр: "0",
                    ПоМесту: departmentParamTransport.TAX_PLACE_TYPE_CODE.CODE
            ){
                Integer formReorg = departmentParamTransport.REORG_FORM_CODE.stringValue != null ? Integer.parseInt(departmentParamTransport.REORG_FORM_CODE.stringValue):0;
                def svnp = [ОКВЭД: departmentParamTransport.OKVED_CODE.CODE]
                if (departmentParamTransport.OKVED_CODE) {
                    svnp.Тлф = departmentParamTransport.PHONE
                }
                СвНП(svnp){
                    НПЮЛ(
                            НаимОрг: departmentParamTransport.NAME,
                            ИННЮЛ: (departmentParamTransport.INN),
                            КПП: (departmentParamTransport.KPP)){


                        if (departmentParamTransport.REORG_FORM_CODE){
                            СвРеоргЮЛ(
                                    ФормРеорг:departmentParamTransport.REORG_FORM_CODE.CODE,
                                    ИННЮЛ: (formReorg in [1, 2, 3, 5, 6] ? departmentParamTransport.REORG_INN: 0),
                                    КПП: (formReorg in [1, 2, 3, 5, 6] ? departmentParamTransport.REORG_KPP: 0)
                            )
                        }
                    }
                }

                Подписант(ПрПодп: departmentParamTransport.SIGNATORY_ID.CODE){
                    ФИО(
                            "Фамилия": departmentParamTransport.SIGNATORY_SURNAME,
                            "Имя": departmentParamTransport.SIGNATORY_FIRSTNAME,
                            "Отчество": departmentParamTransport.SIGNATORY_LASTNAME
                    )
                    // СвПред - Сведения о представителе налогоплательщика
                    if (departmentParamTransport.NAME == 2)
                    {
                        def svPred = ["НаимДок": departmentParamTransport.APPROVE_DOC_NAME]
                        if (departmentParamTransport.APPROVE_ORG_NAME)
                            svPred.НаимОрг = departmentParamTransport.APPROVE_ORG_NAME
                        СвПред(svPred)
                    }
                }




                ТрНалНД(){
                    СумНалПУ("КБК":"18210604011021000110"){
                        /*
                        * Получить сводную НФ по трансп. со статусом принята
                        * Сгруппировать строки сводной налоговой формы по атрибуту «Код по ОКАТО». (okato)
                        */
                        def formData = formDataCollection.find(departmentId, 200, FormDataKind.SUMMARY)
                        def rowsData = []
                        if (formData == null){
                            //logger.error("Не удалось получить сводную НФ по трансп. со статусом принята")
                            rowsData = []
                        }
                        else{
                            dataRowsHelper = formDataService.getDataRowHelper(formData)
                            rowsData = dataRowsHelper.getAllCached()
                        }
                        System.out.print("formData == null ->"+(formData == null))
                        // Формирование данных для СумПУ
                        def resultMap = [:]
                        rowsData.each{ row ->
                            if (row.getAlias() != "total"){
                                if (!resultMap[row.okato]) {
                                    resultMap[row.okato] = [:]
                                    resultMap[row.okato].rowData = [];

                                    resultMap[row.okato].calculationOfTaxes = 0
                                    resultMap[row.okato].taxBase = 0
                                    resultMap[row.okato].taxRate = 0
                                    resultMap[row.okato].amountOfTheAdvancePayment1 = 0
                                    resultMap[row.okato].amountOfTheAdvancePayment2 = 0
                                    resultMap[row.okato].amountOfTheAdvancePayment3 = 0
                                    resultMap[row.okato].amountOfTaxPayable = 0
                                    resultMap[row.okato].taxSumToPay = 0
                                }

                                // НалИсчисл = сумма Исчисленная сумма налога, подлежащая уплате в бюджет
                                resultMap[row.okato].calculationOfTaxes += row.taxSumToPay ?:0;
                                // суммма
                                resultMap[row.okato].taxSumToPay += row.taxSumToPay ?:0;
                                // вспомогательный taxBase
                                resultMap[row.okato].taxBase += row.taxBase ?: 0
                                // вспомогательный taxRate
                                resultMap[row.okato].taxRate += row.taxRate ?: 0
                                // АвПУКв1 = В т.ч. сумма авансовых платежей, исчисленная к уплате в бюджет за первый квартал //// Заполняется в 1, 2, 3, 4 отчетном периоде.
                                resultMap[row.okato].amountOfTheAdvancePayment1  += 0.25*resultMap[row.okato].taxBase*resultMap[row.okato].taxRate
                                // АвПУКв2 = В т.ч. сумма авансовых платежей, исчисленная к уплате в бюджет за второй квартал //// Заполняется во 2, 3, 4 отчетном периоде.
                                resultMap[row.okato].amountOfTheAdvancePayment2  += 0.25*resultMap[row.okato].taxBase*resultMap[row.okato].taxRate
                                // АвПУКв3 = В т.ч. сумма авансовых платежей, исчисленная к уплате в бюджет за третий квартал //// Заполняется во 3, 4 отчетном периоде.
                                resultMap[row.okato].amountOfTheAdvancePayment3  += 0.25*resultMap[row.okato].taxBase*resultMap[row.okato].taxRate
                                // НалПУ = НалИсчисл – (АвПУКв1+ АвПУКв2+ АвПУКв3)
                                resultMap[row.okato].amountOfTaxPayable = resultMap[row.okato].calculationOfTaxes - (
                                resultMap[row.okato].amountOfTheAdvancePayment1 + resultMap[row.okato].amountOfTheAdvancePayment2 + resultMap[row.okato].amountOfTheAdvancePayment3
                                )
                                // В случае  если полученное значение отрицательно,  - не заполняется
                                //resultMap[row.okato].amountOfTaxPayable = resultMap[row.okato].amountOfTaxPayable < 0 ? 0:resultMap[row.okato].amountOfTaxPayable;

                                // Формирование данных для РасчНалТС, собираем строки с текущим значением ОКАТО
                                resultMap[row.okato].rowData.add(row);
                            }

                        }

                        resultMap.each{ okato, row ->
                            СумПУ(
                                    ОКАТО: getRefBookValue(3, okato, "OKATO") ,
                                    НалИсчисл:row.taxSumToPay,
                                    АвПУКв1: row.amountOfTheAdvancePayment1.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    АвПУКв2: row.amountOfTheAdvancePayment2.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    АвПУКв3: row.amountOfTheAdvancePayment3.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    НалПУ: row.amountOfTaxPayable.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                            ){

                                row.rowData.each{ tRow ->
                                    def taxBenefitCode = tRow.taxBenefitCode ? tRow.taxBenefitCode:null
                                    // TODO есть поля которые могут не заполняться, в нашем случае опираться какой логики?
                                    РасчНалТС(
                                            [
                                                    КодВидТС: tRow.tsTypeCode,
                                                    ИдНомТС: tRow.vi, //
                                                    МаркаТС: tRow.model, //
                                                    РегЗнакТС: tRow.regNumber,
                                                    НалБаза: tRow.taxBase,
                                                    ОКЕИНалБаза: getRefBookValue(12, tRow.taxBaseOkeiUnit, "CODE"),
                                            ]
                                                    + (tRow.ecoClass ? [ЭкологКл: tRow.ecoClass]:[])+ //
                                                    [
                                                            ВыпускТС: tRow.years, //
                                                            ВладенТС: tRow.ownMonths,
                                                            КоэфКв: tRow.coef362,
                                                            НалСтавка: getRefBookValue(41, tRow.taxRate, "VALUE"),
                                                            СумИсчисл: tRow.calculatedTaxSum,
                                                    ]
                                                    +   (tRow.benefitEndDate && tRow.benefitStartDate? [ЛьготМесТС:TimeCategory.minus(new Date(), new Date()).months]: [])+//
                                                    [
                                                            СумИсчислУпл: tRow.taxSumToPay,
                                                    ]+
                                                    (tRow.coefKl ? [КоэфКл: tRow.coefKl]:[]),
                                    ){

                                        /* 	2.2. Получить в справочнике «Параметры налоговых льгот» запись,
                                        *	соответствующую значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
                                        */
                                        def param = null;
                                        if (tRow.taxBenefitCode != null){
                                            // получения региона по кода ОКАТО по справочнику Регионов
                                            def tOkato = getRefBookValue(3, tRow.okato, "OKATO")
                                            def region = getRegionByOkatoOrg(tOkato);

                                            def refBookProvider = refBookFactory.getDataProvider(7)

                                            def query = "DICT_REGION_ID LIKE '"+region.record_id+"' AND TAX_BENEFIT_ID LIKE '"+tRow.taxBenefitCode+"'"
                                            def params = refBookProvider.getRecords(new Date(), null, query, null).getRecords()

                                            if (params.size() == 1)
                                                param = params.get(0)
                                            else{
                                                logger.error("Ошибка при получении данных из справочника «Параметры налоговых льгот» Query: "+query)
                                            }
                                        }

                                        // генерация КодОсвНал
                                        if ((taxBenefitCode != 20220 && taxBenefitCode != 20230 && taxBenefitCode != null)){

                                            def l = tRow.taxBenefitCode;
                                            if (param != null) {
                                                def x = l == 30200 ? "" : ((param.SECTION.toString().size() < 4 ? "0"*(4 - param.SECTION.toString().size()) : param.SECTION.toString())
                                                        + (param.ITEM.toString().size() < 4 ? "0"*(4 - param.ITEM.toString().size()) : param.ITEM.toString())
                                                        + (param.SUBITEM.toString().size() < 4 ? "0"*(4 - param.SUBITEM.toString().size()) : param.SUBITEM.toString()))


                                                def kodOsnNal = (l != "" ? l.toString():"0000") +"/"+ x

                                                ЛьготОсвНал(
                                                        КодОсвНал: kodOsnNal,
                                                        СумОсвНал: tRow.benefitSum
                                                )
                                            }
                                        }

                                        // вычисление ЛьготУменСум
                                        // не заполняется если Код налоговой льготы = 30200, 20200, 20210 или 20230
                                        if (taxBenefitCode != 30200
                                                && taxBenefitCode != 20200
                                                && taxBenefitCode != 20210
                                                && taxBenefitCode != 20230
                                                && taxBenefitCode != null){

                                            // вычисление КодУменСум
                                            def valL = tRow.taxBenefitCode;
                                            if (param != null) {
                                                def valX = ((param.SECTION.toString().size() < 4 ? ("0"*(4 - param.SECTION.toString().size())) : param.SECTION.toString())
                                                        + (param.ITEM.toString().size() < 4 ? ("0"*(4 - param.ITEM.toString().size())) : param.ITEM.toString())
                                                        + (param.SUBITEM.toString().size() < 4 ? ("0"*(4 - param.SUBITEM.toString().size())) : param.SUBITEM.toString()))

                                                def kodUmenSum = (valL != "" ? valL.toString():"0000") +"/"+ valX


                                                ЛьготУменСум(КодУменСум: kodUmenSum, СумУменСум: tRow.benefitSum)
                                            }
                                        }

                                        // ЛьготСнижСтав
                                        // не заполняется если Код налоговой льготы = 30200, 20200, 20210 или 20220
                                        if (taxBenefitCode != 30200
                                                && taxBenefitCode != 20200
                                                && taxBenefitCode != 20210
                                                && taxBenefitCode != 20220
                                                && taxBenefitCode != null){

                                            // вычисление КодУменСум
                                            def valL = tRow.taxBenefitCode;
                                            if (param != null) {
                                                def valX = ((param.SECTION.toString().size() < 4 ? "0"*(4 - param.SECTION.toString().size()) : param.SECTION.toString())
                                                        + (param.ITEM.toString().size() < 4 ? "0"*(4 - param.ITEM.toString().size()) : param.ITEM.toString())
                                                        + (param.SUBITEM.toString().size() < 4 ? "0"*(4 - param.SUBITEM.toString().size()) : param.SUBITEM.toString()))

                                                def kodNizhStav = (valL != "" ? valL.toString():"0000") +"/"+ valX

                                                ЛьготСнижСтав(КодСнижСтав: kodNizhStav, СумСнижСтав: tRow.benefitSum)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Получение региона по коду ОКАТО
 * @param okato
 */
def getRegionByOkatoOrg(okato){
    /*
    * первые две цифры проверяемого кода ОКАТО
    * совпадают со значением поля «Определяющая часть кода ОКАТО»
    * справочника «Коды субъектов Российской Федерации»
    */
    // провайдер для справочника - Коды субъектов Российской Федерации
    def  refDataProvider = refBookFactory.getDataProvider(4)
    def records = refDataProvider.getRecords(new Date(), null, "OKATO_DEFINITION like '"+okato.toString().substring(0, 2)+"%'", null).getRecords()

    if (records.size() == 1){
        return records.get(0);
    } else{
        /**
         * Если первые пять цифр кода равны "71140" то код ОКАТО соответствует
         * Ямало-ненецкому АО (код 89 в справочнике «Коды субъектов Российской Федерации»)
         */

        def reg89 = records.find{
            if (it.CODE.toString().length() >= 5 ) {
                return it.CODE.toString().substring(0, 5).equals("71140")
            } else return false
        }
        if (reg89 != null) return reg89;

        /**
         * Если первые пять цифр кода равны "71100" то
         * код ОКАТО соответствует Ханты-мансийскому АО
         * (код 86 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg86 = records.find{
            if (it.CODE.toString().length() >= 5 ) {
                return it.CODE.toString().substring(0, 5).equals("71100")
            } else return false
        }
        if (reg86 != null) return reg86;

        /**
         * Если первые четыре цифры кода равны "1110"
         * то код ОКАТО соответствует Ненецкому АО
         * (код 83 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg83 = records.find{
            if (it.CODE.toString().length() >= 4 ) {
                return it.CODE.toString().substring(0, 4).equals("1110")
            } else return false
        }
        if (reg83 != null) return reg83;

        logger.error("Не удалось определить регион по коду ОКАТО")
    }
}

/**
 * Получение полного справочника
 */
def getModRefBookValue(refBookId, filter, date = new Date()){
    // провайдер для справочника
    def refBook = refBookFactory.get(refBookId);
    def refBookProvider = refBookFactory.getDataProvider(refBookId)
    // записи
    def records = refBookProvider.getRecords(date, null, filter, null).getRecords();
    if (records.size() != 1){
        throw new Exception("Ошибка получения значения из справочника refBookId = "+refBookId)
    }
    // значение справочника в виде мапы
    def record = records[0]

    // получение связанных данных
    refBook.attributes.each() { RefBookAttribute attr ->
        def ref = record[attr.alias].referenceValue;
        if (attr.attributeType  == RefBookAttributeType.REFERENCE && ref != null) {
            def attrProvider = refBookFactory.getDataProvider(attr.refBookId)
            record[attr.alias] = attrProvider.getRecordData(ref);
        }
    }
    record
}

/**
 * Получение значения (разменовываение)
 */
def getRefBookValue(refBookID, recordId, alias){
    def  refDataProvider = refBookFactory.getDataProvider(refBookID)
    def records = refDataProvider.getRecordData(recordId)

    return records != null ? records.get(alias) : null;
}

/**
 * Если не заполнены значения настроек по ТН то выдавать ошибку
 * Поля кроме:
 *  Номер контактного телефона
 *  Код формы реорганизации и ликвидации
 *  ИНН реорганизованного обособленного подразделения
 *  КПП реорганизованного обособленного подразделения
 */
def checkTransportParams(departmentParamTransport){
    def errors = []
    departmentParamTransport.each{ key, value ->
        if (!(key in ['PHONE', 'REORG_FORM_CODE', 'REORG_KPP', 'REORG_INN']) && (value.toString().equals(""))){
            errors.add(key)
        }
    }

    if (errors.size() > 0){
        def ref = refBookFactory.get(31)
        String errorLabels = ''
        errors.each{ e ->
            errorLabels += (errorLabels.equals('') ? '' : ', ')+ref.getAttribute(e).name
        }
        logger.error("Для подразделения <наименование подразделения> в форме настроек подразделения по транспортному налогу отсутствуют следующие данные: "+errorLabels)
        return false;
    }

    return true;
}