(function () {
    'use strict';
    /**
     * @description Модуль реализует обмен с сервером по стандартам REST.
     * Требуется jsGrid для запроса данных через options.angularResource,
     * а также используются методы ресурса для создания/изменения сущностей.
     *
     * Каждый ресурс по умолчанию содержит следующие HTTP-методы:
     * { 'get':   {method:'GET'},
     *  'save':   {method:'POST'},
     *  'query':  {method:'GET', isArray:true},
     *  'remove': {method:'DELETE'},
     *  'delete': {method:'DELETE'} };
     */
    angular.module('app.rest', ['ngResource'])
    /**
     * @description Конфигурация
     */
        .factory('ConfigResource', ['$resource', function ($resource) {
            return $resource('controller/rest/config', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Данные о пользователе
         */
        .factory('UserDataResource', ['$resource', function ($resource) {
            return $resource('controller/rest/userData', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Оповещения
         */
        .factory('NotificationResource', ['$resource', function ($resource) {
            return $resource('controller/rest/notification?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Данные НДФЛ по физлицу
         */
        .factory('NdflPersonResource', ['$resource', function ($resource) {
            return $resource('controller/rest/ndflPerson?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Назначения налоговых форм
         */
        .factory('DeclarationTypeAssignmentResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationTypeAssignment', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Декларация
         */
        .factory('DeclarationDataResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])
        /**
         * @description Уведомления
         */
        .factory('LogEntryResource', ['$resource', function ($resource) {
            return $resource('controller/rest/logEntry/:uuid?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Файлы и комментарии
         */
        .factory('FilesCommentsResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declaration/filesComments?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Справочники
         */
        .factory('RefBookValuesResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBookValues/:refBookId?projection=:projection', {}, {
                query: {method: 'GET', isArray: true, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Поиск лиц для формирования персонального РНУ НДФЛ по физическому лицу
         */
        .factory('RnuPerson', ['$resource', function ($resource) {
            return $resource('controller/rest/getListPerson/rnuPerson/:rnuPersons?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])


        /**
         * @description Формирование рну ндфл
         */
        .factory('RnuPersonDocument', ['$resource', function ($resource) {
            return $resource('/actions/declarationData/declarationDataId}/rnuDoc?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Поиск видов налоговых форм для создания формы. Ищутся назначенные подразделению виды форм
         * с действующей на момент начала периода версией шаблона формы указанного типа.
         */
        .factory('DeclarationTypeForCreateResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook/207/activeAndAssigned', {}, {
                query: {
                    method: 'GET', isArray: true, cache: false
                }
            });
        }])

        /**
         * @description Планировщик задач - Список задач
         */
        .factory('schedulerTaskResource', ['$resource', function ($resource) {
            return $resource('controller/rest/schedulerTask', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Общие параметры
         */
        .factory('CommonParams', ['$resource', function ($resource) {
            return $resource('controller/rest/commonParam/fetchCommonParams', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Блокировки
         */
        .factory('lockDataResource', ['$resource', function ($resource) {
            return $resource('controller/rest/locks', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         *  @description задача планировщик
         * */
        .factory('updateScheduleTask', ['$resource', function ($resource) {
            return $resource('controller/rest/schedulerTaskData/:idTaskScheduler', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Асинхронные задачи
         */
        .factory('asyncTaskResource', ['$resource', function ($resource) {
            return $resource('controller/rest/async', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Транспортные сообщения
         */
        .factory('transportMessageResource', ['$resource', function ($resource) {
            return $resource('controller/rest/transportMessages', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Список пользователей
         */
        .factory('usersResource', ['$resource', function ($resource) {
            return $resource('controller/rest/users', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        .factory('DepartmentReportPeriodResource', ['$resource', function ($resource) {
            return $resource('controller/rest/departmentReportPeriod?projection=:projection', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        .factory('ReportPeriodResource', ['$resource', function ($resource) {
            return $resource('controller/rest/reportPeriod?projection=:projection', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        .factory('ReportPeriodTypeResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBookValues/reportPeriodTypeById', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Принятие НФ
         */
        .factory('acceptDeclarationData', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/accept', {declarationDataId: '@declarationDataId'}, {
                query: {method: 'POST'}
            });
        }])

        /**
         * @description Получить изображение страницы pdf отчета
         */
        .factory('getPageImage', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/pageImage/:pageId/ndfl',
                {declarationDataId: '@declarationDataId', pageId: '@pageId'}, {
                    query: {
                        method: 'GET', isArray: false,
                        interceptor: {
                            response: function (response) {
                                response.requestUrl = response.config.url;
                                return response;
                            }
                        }
                    }
                });
        }])

        /**
         * @description Поверить НФ
         */
        .factory('checkDeclarationData', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/check', {declarationDataId: '@declarationDataId'}, {
                query: {method: 'POST'}
            });
        }])

        /**
         * @description Вернуть в создана
         */
        .factory('moveToCreatedDeclarationData', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/moveToCreated', {declarationDataId: '@declarationDataId'}, {
                query: {method: 'POST'}
            });
        }])

        /**
         * @description обновить данные строки раздела 2 КНФ
         */
        .factory('ndflIncomesAndTax', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/editNdflIncomesAndTax', {declarationDataId: '@declarationDataId'}, {
                update: {method: 'POST'}
            });
        }])

        /**
         * @description обновить данные строки раздела 3 КНФ
         */
        .factory('ndflDeduction', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/editNdflDeduction', {declarationDataId: '@declarationDataId'}, {
                update: {method: 'POST'}
            });
        }])

        /**
         * @description обновить данные строки раздела 4 КНФ
         */
        .factory('ndflPrepayment', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/editNdflPrepayment', {declarationDataId: '@declarationDataId'}, {
                update: {method: 'POST'}
            });
        }])

        /**
         * @description Получить данные о типах асинхронных задач
         */
        .factory('AsyncParamResource', ['$resource', function ($resource) {
            return $resource('controller/rest/asyncParam', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Получить данные параметров конфигурации электронной почты
         */
        .factory('EmailParamResource', ['$resource', function ($resource) {
            return $resource('controller/rest/emailParam', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Проверка валидности конфигурационных параметров электронной почты
         */
        .factory('EmailParamCheckerResource', ['$resource', function ($resource) {
            return $resource('controller/actions/emailParam/checkValidate', {}, {
                query: {method: 'POST', isArray: false, cache: false}
            });
        }])

        /**
         * @description Получить данные об общих параметрах (isArray: false)
         */
        .factory('CommonParamResource', ['$resource', function ($resource) {
            return $resource('controller/rest/commonParam?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Получить данные об общих параметрах (isArray: true)
         */
        .factory('CommonParamSelectResource', ['$resource', function ($resource) {
            return $resource('controller/rest/commonParam?projection=:projection', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Получить данные о типах макетов налоговых форм
         */
        .factory('DeclarationTypeResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationType?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Получить данные о макетах налоговых форм
         */
        .factory('DeclarationTemplateResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationTemplate/:id?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false},
                delete: {method: 'POST'}
            });
        }])

        /**
         * @description Список справочников
         */
        .factory('RefBookListResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Данные справочника
         */
        .factory('RefBookResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook/:id', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Список записей справочников
         */
        .factory('RefBookRecordResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBookRecords/:refBookId', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Справочник физических лиц
         */
        .factory('RefBookFLResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBookFL?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Настройки справочников
         */
        .factory('RefBookConfResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBookConf', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Работа со справочником подразделений
         */
        .factory('DepartmentResource', ['$resource', function ($resource) {
            return $resource('controller/rest/department/:departmentId?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Настройки подразделений
         */
        .factory('DepartmentConfigResource', ['$resource', function ($resource) {
            return $resource('controller/rest/departmentConfig', {}, {
                query: {
                    method: 'GET', isArray: false, cache: false
                }
            });
        }])

        /**
         * @description Версия записи реестра ФЛ
         */
        .factory('PersonCardResource', ['$resource', function ($resource) {
            return $resource('controller/rest/personRegistry/fetch/:id', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Справочник "Статусы налогоплательщика"
         */
        .factory('TaxPayerStateResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook/903?projection=:projection', {}, {
                query: {
                    method: 'GET', isArray: true, cache: false
                }
            });
        }])

        /**
         * @description Справочник "АСНУ"
         */
        .factory('RefBookAsnuResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook/900?projection=:projection', {}, {
                query: {
                    method: 'GET', isArray: true, cache: false
                }
            });
        }])

        /**
         * @description Справочник "ОКСМ"
         */
        .factory('RefBookCountryResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook/10?projection=:projection', {}, {
                query: {
                    method: 'GET', isArray: true, cache: false
                }
            });
        }])

        /**
         * @description Справочник "Коды документов"
         */
        .factory('RefBookDocTypeResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook/360?projection=:projection', {}, {
                query: {
                    method: 'GET', isArray: true, cache: false
                }
            });
        }])
        /**
         * @description История изменений
         */
        .factory('LogBusinessResource', ['$resource', function ($resource) {
            return $resource('controller/rest/logBusiness/:objectId?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])


        /**
         * @description Версия записи справочника "Коды вида дохода"
         */
        .factory('IncomeTypeResource', function ($resource) {
            return $resource('controller/rest/refBook/922/record'+
                '?version=:version' +
                '&recordId=:recordId'+
                '&id=:id',
                {}, {
                    query: {method: 'GET', isArray: false, cache: false}
                });
        })
    ;
}());
