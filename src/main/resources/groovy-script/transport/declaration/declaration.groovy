// Форма настроек обособленного подразделения: значение атрибута 11

/*
*
*/

// получить настройки обособленного подразделения
def departmentParam = departmentService.getDepartmentParam(departmentId)
// Получить параметры по транспортному налогу
def departmentParamTransport = departmentService.getDepartmentParamTransport(departmentId)
// получения региона по кода ОКАТО по справочнику Регионов
def region = dictionaryRegionService.getRegionByOkatoOrg(departmentParam.okato.toString());

// TODO
xml.Файл(ИдФайл: declarationService.generateXmlFileId(1, departmentId), ВерсПрог: departmentParamTransport.appVersion, ВерсФорм:departmentParamTransport.formatVersion) {
    Документ(
            КНД:"1152004",
            // TODO обсудить всплывающее окно, вынести в конф. Трансп декл
            ДатаДок: new Date().format("MM.dd.yy"),
            Период: 34,
            ОтчетГод: taxPeriodService.get(reportPeriodService.get(reportPeriodId).taxPeriodId).startDate.year,
            КодНО: departmentParam.taxOrganCode,
            // TODO учесть что потом будут корректирующие периоды
            НомКорр: "0",
            ПоМесту: departmentParam.taxOrganCode
    ){

        def formReorg = departmentParam.reorgFormCode ?: "";
        def svnp = [ОКВЭД: departmentParam.okvedCode]
        if (departmentParam.okvedCode) {
            svnp.Тлф = departmentParam.phone
        }
        СвНП(svnp){
            НПЮЛ(
                    НаимОрг: departmentParam.name,
                    ИННЮЛ: (formReorg in [1, 2, 3, 5, 6] ? departmentParam.reorgInn: 0),
                    КПП: (formReorg in [1, 2, 3, 5, 6] ? departmentParam.reorgKpp: 0)){


                if (departmentParam.reorgFormCode){
                    СвРеоргЮЛ(
                            ФормРеорг:departmentParam.reorgFormCode,
                            ИННЮЛ: (formReorg in [1, 2, 3, 5, 6] ? departmentParam.reorgInn: 0),
                            КПП: (formReorg in [1, 2, 3, 5, 6] ? departmentParam.reorgKpp: 0)
                    )
                }
            }
        }
    }

    Подписант(ПрПодп: departmentParamTransport.signatoryId){
        ФИО(
                "Фамилия": departmentParamTransport.signatorySurname,
                "Имя": departmentParamTransport.signatoryFirstname,
                "Отчество": departmentParamTransport.signatoryLasttname
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
                rowsData = formData.getDataRows()
            }
            System.out.print("formData == null ->"+(formData == null))
            // Формирование данных для СумПУ
            def resultMap = [:]
            rowsData.each{ row ->
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
                }

                // НалИсчисл = сумма Исчисленная сумма налога, подлежащая уплате в бюджет
                resultMap[row.okato].calculationOfTaxes += row.taxSumToPay;
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
                resultMap[row.okato].amountOfTaxPayable = resultMap[row.okato].amountOfTaxPayable < 0 ? 0:resultMap[row.okato].amountOfTaxPayable;

                // Формирование данных для РасчНалТС, собираем строки с текущим значением ОКАТО
                resultMap[row.okato].rowData.add(row);

            }

            resultMap.each{ okato, row ->
                СумПУ(
                        ОКАТО: okato,
                        НалИсчисл:row.taxSumToPay,
                        АвПУКв1: row.amountOfTheAdvancePayment1,
                        АвПУКв2: row.amountOfTheAdvancePayment2,
                        АвПУКв3: row.amountOfTheAdvancePayment3,
                        НалПУ: row.amountOfTaxPayable
                ){

                    row.rowData.each{ tRow ->
                        // TODO есть поля которые могут не заполняться, в нашем случае опираться какой логики?
                        РасчНалТС(
                                КодВидТС: tRow.tsTypeCode,
                                ИдНомТС: tRow.vi, //
                                МаркаТС: tRow.model, //
                                РегЗнакТС: tRow.regNumber,
                                НалБаза: tRow.taxBase,
                                ОКЕИНалБаза: tRow.taxBaseOkeiUnit,
                                ЭкологКл: tRow.ecoClass, //
                                ВыпускТС: tRow.years, //
                                ВладенТС: tRow.ownMonths,
                                КоэфКв: tRow.coef362,
                                НалСтавка: tRow.taxRate,
                                СумИсчисл: tRow.calculatedTaxSum,
                                ЛьготМесТС: tRow.benefitEndDate - tRow.benefitStartDate,//
                                КоэфКл: tRow.coefKl,//
                                СумИсчислУпл: tRow.taxSumToPay
                        ){

                            // генерация КодОсвНал
                            if ((tRow.taxBenefitCode != 20220 && tRow.taxBenefitCode != 20230)){

                                def l = tRow.taxBenefitCode;
                                /* 	2.2. Получить в справочнике «Параметры налоговых льгот» запись,
                                *	соответствующую значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
                                dictRegionId
                                */
                                def param = dictionaryTaxBenefitParamService.get(region.code, tRow.taxBenefitCode)
                                if (param == null) {
                                    throw new Exception("region.code = "+ region.code+ ", tRow.taxBenefitCode " +tRow.taxBenefitCode)
                                }


                                def x = l == 30200 ? "" : ((param.section.toString().size() < 4 ? "0"*(4 - param.section.toString().size()) : param.section.toString())
                                        + (param.item.toString().size() < 4 ? "0"*(4 - param.item.toString().size()) : param.item.toString())
                                        + (param.subitem.toString().size() < 4 ? "0"*(4 - param.subitem.toString().size()) : param.subitem.toString()))


                                def kodOsnNal = (l != "" ? l.toString():"0000") +"/"+ x


                                ЛьготОсвНал(
                                        КодОсвНал: kodOsnNal,
                                        СумОсвНал: tRow.benefitSum
                                )
                            }

                            // вычисление ЛьготУменСум
                            // не заполняется если Код налоговой льготы = 30200, 20200, 20210 или 20230
                            if (tRow.taxBenefitCode != 30200
                                    && tRow.taxBenefitCode != 20200
                                    && tRow.taxBenefitCode != 20210
                                    && tRow.taxBenefitCode != 20230){

                                // вычисление КодУменСум
                                def valL = tRow.taxBenefitCode;
                                /* 	2.2. Получить в справочнике «Параметры налоговых льгот» запись,
                                *	соответствующую значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
                                */
                                def param = dictionaryTaxBenefitParamService.get(region.code, tRow.taxBenefitCode)

                                def valX = ((param.section.toString().size() < 4 ? ("0"*(4 - param.section.toString().size())) : param.section.toString())
                                        + (param.item.toString().size() < 4 ? ("0"*(4 - param.item.toString().size())) : param.item.toString())
                                        + (param.subitem.toString().size() < 4 ? ("0"*(4 - param.subitem.toString().size())) : param.subitem.toString()))

                                def kodUmenSum = (valL != "" ? valL.toString():"0000") +"/"+ valX


                                ЛьготУменСум(КодУменСум: kodUmenSum, СумУменСум: tRow.benefitSum)
                            }

                            // ЛьготСнижСтав
                            // не заполняется если Код налоговой льготы = 30200, 20200, 20210 или 20220
                            if (tRow.taxBenefitCode != 30200
                                    && tRow.taxBenefitCode != 20200
                                    && tRow.taxBenefitCode != 20210
                                    && tRow.taxBenefitCode != 20220){

                                // вычисление КодУменСум
                                def valL = tRow.taxBenefitCode;
                                /* 	2.2. Получить в справочнике «Параметры налоговых льгот» запись,
                                *	соответствующую значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
                                */
                                def param = dictionaryTaxBenefitParamService.get(region.code, tRow.taxBenefitCode)

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
