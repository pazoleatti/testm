/**
 * Формирование XML для декларации по транспортному налогу.
 *
 * @author auldanov
 * @since 19.03.2013 16:30
 */

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
    bildXml()
}

def bildXml(){
    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    def departmentId = declarationData.departmentId
    // получить настройки обособленного подразделения
    // TODO: переделать на версионные справочники (Marat Fayzullin 2013-08-02)
    def departmentParam = departmentService.getDepartmentParam(departmentId)
    if (departmentParam == null){
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }

    // Получить параметры по транспортному налогу
    // TODO: переделать на версионные справочники (Marat Fayzullin 2013-08-02)
    def departmentParamTransport = departmentService.getDepartmentParamTransport(departmentId)
    // получения региона по кода ОКАТО по справочнику Регионов
    def region = dictionaryRegionService.getRegionByOkatoOrg(departmentParam.okato.toString());


    def builder = new MarkupBuilder(xml)
    if (!declarationData.isAccepted()) {
        builder.Файл(ИдФайл: declarationService.generateXmlFileId(1, departmentId), ВерсПрог: departmentParamTransport.appVersion, ВерсФорм:departmentParamTransport.formatVersion) {
            Документ(
                    КНД:"1152004",
                    // TODO обсудить всплывающее окно, вынести в конф. Трансп декл
                    ДатаДок : (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"), //new Date().format("dd.MM.yyyy"),
                    Период: 34,
                    ОтчетГод: taxPeriodService.get(reportPeriodService.get(declarationData.reportPeriodId).taxPeriodId).startDate.format('yyyy'),
                    КодНО: departmentParam.taxOrganCode,
                    // TODO учесть что потом будут корректирующие периоды
                    НомКорр: "0",
                    ПоМесту: departmentParamTransport.taxPlaceTypeCode
            ){

                Integer formReorg = departmentParam.reorgFormCode != null ? Integer.parseInt(departmentParam.reorgFormCode):0;
                def svnp = [ОКВЭД: departmentParam.okvedCode]
                if (departmentParam.okvedCode) {
                    svnp.Тлф = departmentParam.phone
                }
                СвНП(svnp){
                    НПЮЛ(
                            НаимОрг: departmentParam.name,
                            ИННЮЛ: (departmentParam.inn),
                            КПП: (departmentParam.kpp)){


                        if (departmentParam.reorgFormCode){
                            СвРеоргЮЛ(
                                    ФормРеорг:departmentParam.reorgFormCode,
                                    ИННЮЛ: (formReorg in [1, 2, 3, 5, 6] ? departmentParam.reorgInn: 0),
                                    КПП: (formReorg in [1, 2, 3, 5, 6] ? departmentParam.reorgKpp: 0)
                            )
                        }
                    }
                }

                Подписант(ПрПодп: departmentParamTransport.signatoryId){
                    ФИО(
                            "Фамилия": departmentParamTransport.signatorySurname,
                            "Имя": departmentParamTransport.getSignatoryFirstName(),
                            "Отчество": departmentParamTransport.getSignatoryLastName()
                    )
                    // СвПред - Сведения о представителе налогоплательщика
                    if (departmentParam.name == 2)
                    {
                        def svPred = ["НаимДок": departmentParamTransport.approveDocName]
                        if (departmentParamTransport.approveOrgName)
                            svPred.НаимОрг = departmentParamTransport.approveOrgName
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
                                    ОКАТО: okato,
                                    НалИсчисл:row.taxSumToPay,
                                    АвПУКв1: row.amountOfTheAdvancePayment1.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    АвПУКв2: row.amountOfTheAdvancePayment2.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    АвПУКв3: row.amountOfTheAdvancePayment3.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    НалПУ: row.amountOfTaxPayable.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                            ){

                                row.rowData.each{ tRow ->
                                    def taxBenefitCode = tRow.taxBenefitCode ? Integer.parseInt(tRow.taxBenefitCode):null
                                    // TODO есть поля которые могут не заполняться, в нашем случае опираться какой логики?
                                    РасчНалТС(
                                            [
                                                    КодВидТС: tRow.tsTypeCode,
                                                    ИдНомТС: tRow.vi, //
                                                    МаркаТС: tRow.model, //
                                                    РегЗнакТС: tRow.regNumber,
                                                    НалБаза: tRow.taxBase,
                                                    ОКЕИНалБаза: tRow.taxBaseOkeiUnit
                                            ]
                                                    + (tRow.ecoClass ? [ЭкологКл: tRow.ecoClass]:[])+ //
                                                    [
                                                            ВыпускТС: tRow.years, //
                                                            ВладенТС: tRow.ownMonths,
                                                            КоэфКв: tRow.coef362,
                                                            НалСтавка: tRow.taxRate,
                                                            СумИсчисл: tRow.calculatedTaxSum,
                                                    ]
                                                    +   (tRow.benefitEndDate && tRow.benefitStartDate? [ЛьготМесТС:TimeCategory.minus(new Date(), new Date()).months]: [])+//
                                                    [
                                                            СумИсчислУпл: tRow.taxSumToPay,
                                                    ]+
                                                    (tRow.coefKl ? [КоэфКл: tRow.coefKl]:[]),
                                    ){

                                        // генерация КодОсвНал
                                        if ((taxBenefitCode != 20220 && taxBenefitCode != 20230 && taxBenefitCode != null)){

                                            def l = tRow.taxBenefitCode;
                                            /* 	2.2. Получить в справочнике «Параметры налоговых льгот» запись,
                                            *	соответствующую значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
                                            dictRegionId
                                            */
                                            def param = dictionaryTaxBenefitParamService.get(region.code, tRow.taxBenefitCode)
                                            if (param != null) {
                                                def x = l == 30200 ? "" : ((param.section.toString().size() < 4 ? "0"*(4 - param.section.toString().size()) : param.section.toString())
                                                        + (param.item.toString().size() < 4 ? "0"*(4 - param.item.toString().size()) : param.item.toString())
                                                        + (param.subitem.toString().size() < 4 ? "0"*(4 - param.subitem.toString().size()) : param.subitem.toString()))


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
                                            /* 	2.2. Получить в справочнике «Параметры налоговых льгот» запись,
                                            *	соответствующую значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
                                            */
                                            def param = dictionaryTaxBenefitParamService.get(region.code, tRow.taxBenefitCode)
                                            if (param != null) {
                                                def valX = ((param.section.toString().size() < 4 ? ("0"*(4 - param.section.toString().size())) : param.section.toString())
                                                        + (param.item.toString().size() < 4 ? ("0"*(4 - param.item.toString().size())) : param.item.toString())
                                                        + (param.subitem.toString().size() < 4 ? ("0"*(4 - param.subitem.toString().size())) : param.subitem.toString()))

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
                                            /* 	2.2. Получить в справочнике «Параметры налоговых льгот» запись,
                                            *	соответствующую значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
                                            */
                                            def param = dictionaryTaxBenefitParamService.get(region.code, tRow.taxBenefitCode)
                                            if (param != null) {
                                                def valX = ((param.section.toString().size() < 4 ? "0"*(4 - param.section.toString().size()) : param.section.toString())
                                                        + (param.item.toString().size() < 4 ? "0"*(4 - param.item.toString().size()) : param.item.toString())
                                                        + (param.subitem.toString().size() < 4 ? "0"*(4 - param.subitem.toString().size()) : param.subitem.toString()))

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