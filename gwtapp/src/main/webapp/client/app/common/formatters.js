(function () {
    'use strict';

    /**
     * @description Модуль, содержащий форматтеры
     */

    angular.module('app.formatters', ['app.constants'])
    /**
     * @description Фильтр даты
     *
     * @param value - значение, которое необходимо отформатировать
     * @return Дата в формате 'dd.MM.yyyy'
     */
        .filter('dateFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) {
                    return '';
                }
                if (!value.millis) {
                    return $filter('date')(value, 'dd.MM.yyyy');
                } else {
                    return $filter('date')(value.millis, 'dd.MM.yyyy');
                }
            };
        }])
        /**
         * @description Фильтр даты без года
         *
         * @param value - значение, которое необходимо отформатировать
         * @return Дата в формате 'dd.MM'
         */
        .filter('dateWithoutYearFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) {
                    return '';
                }
                if (!value.millis) {
                    return $filter('date')(value, 'dd.MM');
                } else {
                    return $filter('date')(value.millis, 'dd.MM');
                }
            };
        }])

        /**
         * @description Фильтр даты и времени с часовым поясом GMT+3
         *
         * @param value - значение, которое необходимо отформатировать
         * @return Дата и время в формате 'dd.MM.yyyy HH:mm:ss'
         */
        .filter('dateTimeFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) {
                    return '';
                }
                if (!value.millis) {
                    return $filter('date')(value, 'dd.MM.yyyy HH:mm:ss', '+3');
                } else {
                    return $filter('date')(value.millis, 'dd.MM.yyyy HH:mm:ss', '+3');
                }
            };
        }])

        /**
         * @description Форматирует дату для передачи на сервер, на сервер используем @DateTimeFormat(pattern = "dd.MM.yyyy'T'HH:mm")
         */
        .filter('dateTimeSerializer', ['$filter', function ($filter) {
            return function (date) {
                return date ? new Date(date).format("dd.mm.yyyy'T'HH:MM") : undefined;
            };
        }])

        /**
         * @description Фильтр создаёт по коллекции объектов коллекцию их идентификаторов.
         *
         * @param items - коллекция объектов
         * @param property - имя свойства объекта, содержащее идентификатор (по-умолчанию 'id')
         * @return коллекция идентификаторов объектов
         */
        .filter('idExtractor', function () {
            return function (items, property) {
                if (!items || items.length === 0) {
                    return undefined;
                }
                if (!property) {
                    property = 'id';
                }

                var result = [];
                angular.forEach(items, function (item) {
                    result.push(_.property(property)(item));
                });

                return result;
            };
        })

        /**
         * @description Возвращает текст в зависимости от переданного числа
         *
         * @param num - число
         * @param nominative - единственное число
         * @param singular - множественное число от 2 до 4
         * @param plural - множественное число
         * @return Текст, склонение которого зависит от переданного числа
         */
        .filter('amountCasesFormatter', function () {
            return function (num, nominative, singular, plural) {
                switch (num % 100) {
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                        return plural;
                }
                switch (num % 10) {
                    case 1:
                        return nominative;
                    case 2:
                    case 3:
                    case 4:
                        return singular;
                    default: // case 0, 5-9
                        return plural;
                }
            };
        })

        /**
         * @description Форматтер для получения наименования сущности
         * @param entity Сущность
         */
        .filter('nameFormatter', function () {
            return function (entity) {
                return entity ? entity.name : "";
            };
        })

        /**
         * @description Форматтер для получения полного пути
         * @param entity Сущность
         */
        .filter('fullNameFormatter', function () {
            return function (entity) {
                return entity ? entity.fullName : "";
            };
        })

        /**
         * @description Форматтер для получения наименования сущности
         * @param entity Сущность
         */
        .filter('kppSelectFormatter', function () {
            return function (kppModel) {
                return kppModel ? kppModel.kpp : "";
            };
        })

        /**
         * @description Форматтер для получения наименования сущности
         * @param entity Сущность
         */
        .filter('kppSelectResultFormatter', ['$filter', function ($filter) {
            return function (kppModel) {
                return kppModel ? kppModel.kpp : $filter('translate')('ndfl.report.ndfl2_6XlsxReport.modal.kpp.all');
            };
        }])

        /**
         * @description Форматтер для enum DeclarationCheckCode
         */
        .filter('declarationCheckCodeEnumFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (enumName) {
                return APP_CONSTANTS.DECLARATION_CHECK_CODE[enumName];
            };
        }])

        /**
         * @description Форматтер для enum TaxRefundReflectionMode
         */
        .filter('taxRefundReflectModeEnumFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (enumName) {
                return APP_CONSTANTS.TAX_REFUND_REFLECT_MODE[enumName].shortname;
            };
        }])

        /**
         * @description Форматтер для получения наименования отчетного периода из модели НФ
         * в нужном формате "год: наименование периода: вид отчетности"
         * @param reportPeriod Отчетный период
         */
        .filter('periodFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (reportPeriod) {
                var reportPeriodTaxFormType =
                    getReportPeriodTaxFormTypeNameFromId(APP_CONSTANTS, reportPeriod.reportPeriodTaxFormTypeId);

                return reportPeriod
                    ? reportPeriod.taxPeriod.year + ": " + reportPeriod.name + ": " + reportPeriodTaxFormType
                    : "";
            };
        }])

        /**
         * @description Форматтер для получения наименования отчетного периода из модели НФ
         * в нужном формате "год: наименование периода: вид отчетности"
         * @param reportPeriod Отчетный период
         */
        .filter('periodFormatterWithoutTaxFormType', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (reportPeriod) {
                return reportPeriod ? (reportPeriod.taxPeriod.year + ": " + reportPeriod.name) : "";
            };
        }])

        /**
         * @description Форматтер для получения наименования отчетного периода из модели отчетного периода подразделения
         * в нужном формате "год: наименование периода: вид отчетности"
         * @param reportPeriod Отчетный период
         */
        .filter('departmentReportPeriodFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (departmentReportPeriod) {
                var formType = getReportPeriodTaxFormTypeNameFromId(APP_CONSTANTS, departmentReportPeriod.taxFormTypeId);
                return departmentReportPeriod.year + ": " + departmentReportPeriod.name + ": " + formType;
            };
        }])

        /**
         * @description Форматтер для получения наименования отчетного периода в нужном формате "год: наименование периода (срок корректировки)"
         * @param reportPeriod Отчетный период
         */
        .filter('periodFormatterWithCorrectionDate', ['$filter', 'APP_CONSTANTS', function ($filter, APP_CONSTANTS) {
            return function (reportPeriod) {
                if (reportPeriod) {
                    var reportPeriodTaxFormType =
                        getReportPeriodTaxFormTypeNameFromId(APP_CONSTANTS, reportPeriod.reportPeriodTaxFormTypeId);

                    var correctionDateClause = "";
                    if (reportPeriod.correctionDate) {
                        correctionDateClause = " " + $filter('translate')('createReport.correctionString', {correctionDate: $filter('date')(reportPeriod.correctionDate, 'dd.MM.yyyy')})
                    }
                    return reportPeriod.taxPeriod.year + ": " + reportPeriod.name + correctionDateClause + ": " + reportPeriodTaxFormType
                } else {
                    return "";
                }
            };
        }])

        /**
         * @description Форматтер для параметров запроса
         * @param param параметр запроса
         */
        .filter('requestParamsFormatter', function () {
            return function (param) {
                return !param ? undefined : param;
            };
        })

        /**
         * @description Форматтер для получения наименования сущности
         * @param entity Сущность
         */
        .filter('periodTypeFormatter', function () {
            return function (entity) {
                return entity ? entity.name : "";
            };
        })

        /**
         * @description Форматтер для преобразования тега корректировки из enum в boolean
         * @param correctionTag
         */
        .filter('correctionTagFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (correctionTag) {
                if (correctionTag && correctionTag.id !== undefined) {
                    switch (correctionTag.id) {
                        case APP_CONSTANTS.CORRECTION_TAG.ALL.id:
                            return undefined;
                        case APP_CONSTANTS.CORRECTION_TAG.ONLY_PRIMARY.id:
                            return false;
                        case APP_CONSTANTS.CORRECTION_TAG.ONLY_CORRECTIVE.id:
                            return true;
                    }
                }
                return undefined;
            };
        }])

        .filter('activityAttributeFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (activityAttribute) {
                if (activityAttribute && activityAttribute.id !== undefined) {
                    switch (activityAttribute.id) {
                        case APP_CONSTANTS.USER_ACTIVITY.NO.id:
                            return false;
                        case APP_CONSTANTS.USER_ACTIVITY.YES.id:
                            return true;
                    }
                }
                return undefined;
            }
        }])

        .filter('versionsVisibilityFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (versionsVisibility) {
                if (versionsVisibility && versionsVisibility.id !== undefined) {
                    switch (versionsVisibility.id) {
                        case APP_CONSTANTS.SHOW_VERSIONS.BY_DATE.id:
                            return false;
                        case APP_CONSTANTS.SHOW_VERSIONS.ALL.id:
                            return true;
                    }
                }
                return undefined;
            }
        }])

        .filter('duplicatesFilterFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (duplicatesOption) {
                if (duplicatesOption && duplicatesOption.id) {
                    switch (duplicatesOption.id) {
                        case APP_CONSTANTS.SHOW_DUPLICATES.NO.id:
                            return false;
                        case APP_CONSTANTS.SHOW_DUPLICATES.ONLY_DUPLICATES.id:
                            return true;
                        case APP_CONSTANTS.SHOW_DUPLICATES.ALL_RECORDS.id:
                            return null;
                    }
                }
                return undefined;
            }
        }])

        /**
         * @description Преобразует значение признака активности периода в текст (Открыт/Закрыт/Не задано)
         * @param value признак активности периода
         */
        .filter('activeStatusPeriodFormatter', ['$filter', function ($filter) {
            return function (value) {
                return value === null || value === undefined ? $filter('translate')('common.undefined') :
                    (value ? $filter('translate')('reportPeriod.grid.status.open') :
                        $filter('translate')('reportPeriod.grid.status.close'));
            };
        }])

        /**
         * @description Преобразует булевые значения в текст (Да/Нет)
         * @param value булевое значение
         */
        .filter('yesNoFormatter', ['$filter', function ($filter) {
            return function (value) {
                return value ?
                    $filter('translate')('yes') :
                    $filter('translate')('no');
            };
        }])

        /**
         * @description Форматтер периода формы в виде
         * "<Год>: <Название периода> (<Дата корректировки через запятую (если имеется)): <Вид отчетности>"
         * @param value признак активности периода
         */
        .filter('ndflPeriodFormatter', ['$filter', 'APP_CONSTANTS', function ($filter, APP_CONSTANTS) {
            return function (declarationData) {
                if (declarationData) {
                    var reportPeriodTaxFormType =
                        getReportPeriodTaxFormTypeNameFromId(APP_CONSTANTS, declarationData.reportPeriodTaxFormTypeId);

                    return $filter('translate')('title.period.value', {
                        year: declarationData.reportPeriodYear,
                        periodName: declarationData.reportPeriod,
                        correctionString: declarationData.correctionDate ?
                            $filter('translate')('title.period.value.correctionString', {correctionDate: $filter('date')(declarationData.correctionDate, 'dd.MM.yyyy')}) :
                            '',
                        formType: reportPeriodTaxFormType
                    });
                }
                return '';
            };
        }])

        /**
         * @description Соединяет объекты в строку через разделитель,
         * при этом в строку попадает не сам объект, а свойство объекта, полученное по пути
         * @param array массив объектов
         * @param separator разделитель (', ' по умолчанию)
         * @param path путь к полю, которое будет записано в строку через разделитель (name по-умолчанию)
         * @param checkDistinction проверять уникальность значений свойств объектов (false по умолчанию)
         */
        .filter('joinObjectsPropFormatter', function () {
            return function (array, separator, path, checkDistinction) {
                if (angular.isDefined(array)) {
                    if (array && array.length > 0) {
                        var nameArray = _.map(array, function (obj) {
                            return _.deep(obj, path || 'name');
                        });

                        if (checkDistinction) {
                            var distinctValues = [];
                            angular.forEach(nameArray, function (value) {
                                if (distinctValues.indexOf(value) === -1) {
                                    distinctValues.push(value);
                                }
                            });
                            nameArray = distinctValues;
                        }

                        return nameArray.join(separator || ', ');
                    }
                    return '';
                }

                return undefined;
            };
        })

        /**
         * @description Обрезает
         */
        .filter('truncateStringFormatter', function () {
            return function (string, limit) {
                if (string && string.length > limit) {
                    return string.substring(0, limit) + '...';
                }
                return string
            };
        })

        /**
         * @description Формирует строку значений массива через разделитель. В строку войдут ограниченное число значений, остальные обрежутся
         * @param list массив значений
         * @param delimiter разделитель
         * @param limit кол-во значений, которые будут показаны через разделитель, остальные обрежутся
         */
        .filter('joinAndTruncateFormatter', [function () {
            return function (list, delimiter, limit) {
                if (angular.isUndefined(list)) return null;
                if (!list) return '';
                if (list.length > limit) {
                    return list.slice(0, limit).join(delimiter) + '...';
                } else {
                    return list.join(delimiter);
                }
            };
        }])

        /**
         * @description Форматирует конфигурационный параметр для отображения описания сущности
         */
        .filter('configParamFormatter', function () {
            return function (entity) {
                return entity.description ? entity.description : "";
            };
        })

        /**
         * @description Форматирует значение асинхронного параметра. Если значение 0 - то на выход "", иначе, значение
         */
        .filter('asyncLimitFormatter', function () {
            return function (value) {
                return !value ? "" : value;
            };
        })

        /**
         * @description Форматтер для получения наименования записи в формате "(код) наименование"
         * @param record запись из справочника
         */
        .filter('codeNameFormatter', function () {
            return function (record) {
                if (!record) return '';
                if (!record.code && !record.name) return '';
                if (!record.code) return record.name;
                if (!record.name) return '(' + record.code + ')';
                return "(" + record.code + ") " + record.name;
            };
        })

        /**
         * @description Форматтер для получения данных о физ. лице в нужном формате
         * @param person запись из справочника физических лиц
         */
        .filter('personFormatter', function () {
            return function (person) {
                return person ? person.lastName + " " + person.firstName + " " + person.middleName : "";
            };
        })

        /**
         * @description Форматтер для получения кода из записи
         */
        .filter('codeFormatter', function () {
            return function (record) {
                return record && record.code ? record.code : "";
            };
        })

        /**
         * @description Форматтер для адресов физ. лиц
         * @param address объект адреса ФЛ
         */
        .filter('personAddressFormatter', function () {
            return function (address) {
                if (!address) return '';

                var values = [];
                // Формируем список полей в нужном порядке
                var fields = ['postalCode', 'regionCode', 'district', 'city', 'locality', 'street', 'house', 'build', 'appartment'];
                // Добавляем значения непустых полей
                fields.forEach(function (field) {
                    if (address[field]) {
                        values.push(address[field]);
                    }
                });
                return values.join(', ');
            };
        })

        /**
         * @description Фильтр даты. Если значение даты будет равно '1901-01-01', то отображаться будет '00.00.0000'
         *
         * @param value значение, которое необходимо отформатировать
         * @return Дата в формате 'dd.MM.yyyy'
         */
        .filter('dateZeroFormatter', ['$filter', 'APP_CONSTANTS', function ($filter, APP_CONSTANTS) {
            return function (value) {
                if (value === APP_CONSTANTS.DATE_ZERO.AS_DATE) {
                    return APP_CONSTANTS.DATE_ZERO.AS_STRING;
                }
                return $filter('dateFormatter')(value);
            };
        }])

        /**
         * @description Возвращяет прочерк если значение не задано
         */
        .filter('dashIfEmptyFormatter', ['$filter', function ($filter) {
            return function (value, filterName) {
                return value ? $filter(filterName)(value) : '-';
            };
        }])

        .filter('permissiveFormatter', ['$filter', function ($filter) {
            return function (data) {
                if (!data) return '';
                if (data.permission === false) {
                    return $filter('translate')('refBook.fl.table.label.permissionDenied');
                }
                if (!data.value) return '';
                return data.value;
            };
        }])

        .filter('vipTextFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (value) {
                    return $filter('translate')('refBook.fl.table.label.vip');
                } else if (value === false) {
                    return $filter('translate')('refBook.fl.table.label.notVip');
                } else {
                    return '';
                }
            }
        }])

        .filter('personIdFormatter', ['$filter', function ($filter) {
            return function (oldId, options, person) {
                if (oldId && person.recordId) {
                    if (oldId === person.recordId) {
                        return oldId;
                    } else {
                        return oldId + ' ' + $filter('translate')('refBook.fl.table.label.duplicate');
                    }
                } else {
                    return '';
                }
            }
        }])

        .filter('russianAddressFormatter', ['$filter', function ($filter) {
            return function (data) {
                if (!data) return '';
                if (data.permission === false) {
                    return $filter('translate')('refBook.fl.table.label.permissionDenied');
                }
                return $filter('personAddressFormatter')(data.value);
            }
        }])

        .filter('foreignAddressFormatter', ['$filter', function ($filter) {
            return function (data) {
                if (!data) return '';
                if (data.permission === false) {
                    return $filter('translate')('refBook.fl.table.label.permissionDenied');
                }
                if (!data.value.addressIno) return '';
                var values = [];
                if (data.value.country && data.value.country.code) {
                    var country = $filter('codeNameFormatter')(data.value.country);
                    values.push(country);
                }
                if (data.value.addressIno) {
                    values.push(data.value.addressIno);
                }
                return values.join(', ');
            }
        }])

        /**
         * @description Фильтр ДУЛ
         */
        .filter('idDocFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (value) {
                    return value.documentNumber + " - " + $filter('codeNameFormatter')(value.docType);
                }
                return ''
            };
        }])

        /**
         * Фильтр для оригинала физлица
         */
        .filter('originalFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (value) {
                    var date = $filter('dateFormatter')(value.birthDate);
                    return "(" + value.recordId + ")" + (value.lastName ? " " + value.lastName : "") + " " + (value.firstName ? " " + value.firstName : "") + " " + (value.middleName ? " " + value.middleName : "") + ", " + date
                }
                return ''
            };
        }])

        /**
         * Фильтр для простых значений - не требующих специального форматирования: строк и чисел - с учетом прав доступа
         */
        .filter('simplePermissiveFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) return '';
                if (value.permission === false) {
                    return $filter('translate')('refBook.fl.table.label.permissionDenied');
                }
                if (!value.value) {
                    return ''
                }
                return value.value;
            };
        }])

        .filter('vipOptionsFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (vipOptionIds) {
                if (!vipOptionIds) return null;
                var hasVip = !!_.find(vipOptionIds, function (el) {
                    return el.id === APP_CONSTANTS.PERSON_IMPORTANCE.VIP.id;
                });
                var hasNotVip = !!_.find(vipOptionIds, function (el) {
                    return el.id === APP_CONSTANTS.PERSON_IMPORTANCE.NOT_VIP.id;
                });
                // Если выбраны обе опции или ни одна, фильтрация по ним не нужна
                if (hasVip === hasNotVip) {
                    return null;
                } else {
                    return hasVip;
                }
            };
        }])

        .filter('departmentActivityFormatter', ['$filter', function ($filter) {
            return function (entity) {
                if (!entity) return "";
                var name = entity.name ? entity.name : "";
                var activity = (entity.active === true) ? "" : " " + $filter('translate')('refBook.fl.filter.text.department.inactive');
                return name + activity;
            };
        }])

        .filter('docTypeFormatter', ['$filter', function ($filter) {
            return function (data) {
                if (!data) return '';
                if (data.permission === false) {
                    return $filter('translate')('refBook.fl.table.label.permissionDenied');
                }
                if (!data.value) return '';
                return $filter('codeNameFormatter')(data.value.docType);
            };
        }])

        .filter('docNumberFormatter', ['$filter', function ($filter) {
            return function (data) {
                if (!data) return '';
                if (data.permission === false) {
                    return $filter('translate')('refBook.fl.table.label.permissionDenied');
                }
                if (!data.value) return '';
                if (!data.value.documentNumber) return '';
                return data.value.documentNumber;
            }
        }])

        .filter('permissiveCodeFormatter', ['$filter', function ($filter) {
            return function (data) {
                if (!data) return '';
                if (data.permission === false) {
                    return $filter('translate')('refBook.fl.table.label.permissionDenied');
                }
                return $filter('codeNameFormatter')(data.value);
            };
        }])

        /**
         * @description Форматтер для пары КПП/ОКТМО с указанием актуальности
         */
        .filter('kppOktmoPairFormatter', function () {
            return function (kppOktmoPair) {
                if (kppOktmoPair && kppOktmoPair.kpp && kppOktmoPair.oktmo) {
                    return kppOktmoPair.kpp + ' / ' + kppOktmoPair.oktmo + (kppOktmoPair.relevance ? ' (' + kppOktmoPair.relevance + ')' : '');
                } else {
                    return ''
                }
            };
        })

        /**
         * @description Форматтер для пары КПП/ОКТМО с указанием актуальности
         */
        .filter('negativeSumsSignFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (negativeSumsSign) {
                return APP_CONSTANTS.NEGATIVE_SUMS_SIGN[negativeSumsSign].name;
            };
        }])

        /**
         * @description Форматтер вида "(id) name"
         */
        .filter('idNameFormatter', function () {
            return function (record) {
                if (!record) return '';
                if (!record.id && !record.name) return '';
                if (!record.id) return record.name;
                if (!record.name) return '(' + record.id + ')';
                return "(" + record.id + ") " + record.name;
            };
        })

        /**
         * @description Формат имени пользователя: "Имя (Логин)"
         */
        .filter('userFormatter', function () {
            return function (user) {
                if (!user) return '';
                if (!user.name && !user.login) return '';
                if (!user.login) return user.name;
                if (!user.name) return '(' + user.login + ')';
                return user.name + ' (' + user.login + ')';
            };
        })

        /**
         * @description Формат для обозначения ДУЛ включаемого в отчетность
         */
        .filter('reportDocFormatter', ['$filter', function($filter) {
            return function (includeReport) {
                if(includeReport) {
                    return $filter('translate')('refBook.fl.card.tabs.lable.idDoc.includeReport');
                } else {
                    return '';
                }
            };
        }])

        /**
         * @description Формат вида отчетности
         */
        .filter('taxFormTypeFormatter', ['$filter', 'APP_CONSTANTS', function($filter, APP_CONSTANTS) {
            return function (taxFormTypeId) {
                var formType = getReportPeriodTaxFormTypeNameFromId(APP_CONSTANTS, taxFormTypeId);
                return formType ? formType : "";
            };
        }])
    ;

    function getReportPeriodTaxFormTypeNameFromId(APP_CONSTANTS, taxFormTypeId) {
        for (var reportPeriodTaxFormType in APP_CONSTANTS.TAX_FORM_TYPE) {
            if (taxFormTypeId === APP_CONSTANTS.TAX_FORM_TYPE[reportPeriodTaxFormType].id) {
                return APP_CONSTANTS.TAX_FORM_TYPE[reportPeriodTaxFormType].name;
            }
        }
    };

}());
