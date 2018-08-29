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
                }
                else {
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
                }
                else {
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
                }
                else {
                    return $filter('date')(value.millis, 'dd.MM.yyyy HH:mm:ss', '+3');
                }
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
                var text;
                if (num > 10 && ((num % 100) / 10) === 1) {
                    return num + " " + plural;
                }
                switch (num % 10) {
                    case 1:
                        text = nominative;
                        break;
                    case 2:
                    case 3:
                    case 4:
                        text = singular;
                        break;
                    default: // case 0, 5-9
                        text = plural;
                }
                return text;
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
         * @description Форматтер для enum DeclarationCheckCode
         */
        .filter('declarationCheckCodeEnumFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (enumName) {
                return APP_CONSTANTS.DECLARATION_CHECK_CODE[enumName];
            };
        }])

        /**
         * @description Форматтер для получения наименования отчетного периода в нужном формате "год: наименование периода"
         * @param reportPeriod Отчетный период
         */
        .filter('periodFormatter', function () {
            return function (reportPeriod) {
                return reportPeriod ? reportPeriod.taxPeriod.year + ": " + reportPeriod.name : "";
            };
        })

        /**
         * @description Форматтер для получения наименования отчетного периода в нужном формате "год: наименование периода (срок корректировки)"
         * @param reportPeriod Отчетный период
         */
        .filter('periodFormatterWithCorrectionDate', ['$filter', function ($filter) {
            return function (reportPeriod) {
                if (reportPeriod) {
                    var correctionDateClause = "";
                    if (reportPeriod.correctionDate) {
                        correctionDateClause = $filter('translate')('createReport.correctionString', {correctionDate: $filter('date')(reportPeriod.correctionDate, 'dd.MM.yyyy')})
                    }
                    return reportPeriod.taxPeriod.year + ": " + reportPeriod.name + " " + correctionDateClause
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
         * "<Год>, <Название периода><Дата корректировки через запятую (если имеется)>"
         * @param value признак активности периода
         */
        .filter('ndflPeriodFormatter', ['$filter', function ($filter) {
            return function (declarationData) {
                if (declarationData) {
                    return $filter('translate')('title.period.value', {
                        year: declarationData.reportPeriodYear,
                        periodName: declarationData.reportPeriod,
                        correctionString: declarationData.correctionDate ?
                            $filter('translate')('title.period.value.correctionString', {correctionDate: $filter('date')(declarationData.correctionDate, 'dd.MM.yyyy')}) :
                            ''
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
                return value === 0 ? "" : value;
            };
        })

        /**
         * @description Форматтер для получения наименования записи в формате "код: наименование"
         * @param record запись из справочника
         */
        .filter('codeNameFormatter', function () {
            return function (record) {
                return record ? record.code + ": " + record.name : "";
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
                // Для российских адресов
                if (address.addressType === 0) {
                    // Формируем список полей в нужном порядке
                    var fields = ['postalCode', 'regionCode', 'district', 'city', 'locality', 'street', 'house', 'build', 'appartment'];
                    // Добавляем значения непустых полей
                    fields.forEach(function (field) {
                        if (address[field]) {
                            values.push(address[field]);
                        }
                    });
                    // Для зарубежных адресов нужны только название страны и строковый адрес
                } else if (address.addressType === 1) {
                    if (address.country && address.country.name) {
                        values.push(address.country.name);
                    }
                    if (address.address) {
                        values.push(address.address);
                    }
                }
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

        .filter('permissiveFormatter', ['$filter', function ($filter) {
            return function (data) {
                if (!data) return '';
                if (data.permission === false) {
                    return $filter('translate')('refBook.fl.label.permissionDenied');
                }
                if (!data.value) return '';
                return data.value;
            };
        }])

        .filter('vipFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (value) {
                    return $filter('translate')('refBook.fl.label.vip');
                } else if (value === false) {
                    return $filter('translate')('refBook.fl.label.notVip');
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
                        return oldId + ' ' + $filter('translate')('refBook.fl.label.duplicate');
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
                    return $filter('translate')('refBook.fl.label.permissionDenied');
                }
                if (data.value && data.value.addressType === 1) {
                    return '';
                }
                return $filter('personAddressFormatter')(data.value);
            }
        }])

        .filter('foreignAddressFormatter', ['$filter', function ($filter) {
            return function (data) {
                if (!data) return '';
                if (data.permission === false) {
                    return $filter('translate')('refBook.fl.label.permissionDenied');
                }
                if (data.value && data.value.addressType === 0) {
                    return '';
                }
                return $filter('personAddressFormatter')(data.value);
            }
        }])

        .filter('citizenshipFormatter', function () {
            return function (value) {
                if (value && value.code && value.name) {
                    return '(' + value.code + ') ' + value.name;
                } else {
                    return '';
                }
            }
        })

        /**
         * Фильтр кода ДУЛ
         */
        .filter('idDocCodeFormatter', function() {
            return function (value) {
                if (value) {
                    return "(" + value.referenceObject.CODE.value + ") " + value.referenceObject.NAME.value;
                }
                return ''
            }
        })

        /**
         *  Фильтр для простых справочных значений: строк и чисел
         */
        .filter('simpleRefBookValueFormatter', function() {
            return function (value) {
                if (value) {
                    return value.value
                }
                return ''
            }
        })

        /**
         * @description Фильтр ДУЛ
         */
        .filter('idDocFormatter', function () {
            return function (value) {
                if (value) {
                    return value.DOC_NUMBER.value + " - " + "(" + value.DOC_ID.referenceObject.CODE.value + ") " + value.DOC_ID.referenceObject.NAME.value
                }
                return ''
            };
        })

        /**
         * Фильтр ОКСМ
         */
        .filter('countryFormatter', function () {
            return function (value) {
                if(value) {
                    return "(" + value.CODE.value + ") " + value.NAME.value
                }
                return ''
            };
        })

        /**
         * Фильтр АСНУ
         */
        .filter('asnuFormatter', function () {
            return function (value) {
                if (value) {
                    return "(" + value.CODE.value + ") " + value.NAME.value
                }
                return ''
            };
        })

        /**
         * Фильтр для статуса налогоплательщика
         */
        .filter('taxPayerStateFormatter', function () {
            return function (value) {
                if (value) {
                    return "(" + value.CODE.value + ") " + value.NAME.value
                }
                return ''
            };
        })

        /**
         * Фильтр для оригинала физлица
         */
        .filter('originalFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (value) {
                    var date = $filter('dateFormatter')(value.birthDate);
                    return "(" + value.recordId + ") " + value.lastName + " " + value.firstName + " " + value.middleName + ", " + date
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
                    return $filter('translate')('refBook.fl.label.permissionDenied');
                }
                if(!value.value) {
                    return ''
                }
                return value.value;
            };
        }])

        /**
         * Фильтр типа документа
         */
        .filter('idDocTypeFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) return '';
                if (value.permission === false) {
                    return $filter('translate')('refBook.fl.label.permissionDenied');
                }
                return value.value.DOC_ID.referenceObject.NAME.value;
            };
        }])

        /**
         * Фильтр серии и номера документа
         */
        .filter('idDocNumberFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) return '';
                if (value.permission === false) {
                    return $filter('translate')('refBook.fl.label.permissionDenied');
                }
                return value.value.DOC_NUMBER.value;
            };
        }])
    ;
}());