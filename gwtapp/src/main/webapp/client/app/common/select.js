(function () {
    'use strict';

    /**
     * @description Модуль, содержащий типовые селекты
     */
    angular.module('app.select.common', ['ui.select2', 'app.formatters'])
    /**
     * Сервис для получения настройки select2 с учётом url
     */
        .service("GetSelectOption", ['$filter', function ($filter) {
            /**
             * Создать основу для настроек выпадающего списка. Определяется все, кроме data и ajax
             * @param isMultiple Выбор множественный
             * @param allowClear Возможна ли очистка
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска
             */
            var buildSelectOptionsTemplate = function (isMultiple, allowClear, formatter) {
                return {
                    options: {
                        formatSelection: $filter(formatter),
                        formatResult: $filter(formatter),
                        multiple: isMultiple,
                        allowClear: allowClear
                    }
                };
            };

            var buildBasicSelectOptions = function (isMultiple, allowClear, formatter) {
                if (formatter === undefined) {
                    formatter = 'nameFormatter';
                }
                var select = buildSelectOptionsTemplate(isMultiple, allowClear, formatter);
                select.options.data = {text: $filter(formatter)};
                return select;
            };

            /**
             * Получить настройки выпадающего списка единичного выбора с постоянным набором элементов. Задано все, кроме data.results
             * @param allowClear Возможна ли очистка
             * @param allowSearch Доступен поиск, отображается поле ввода для поиска среди элементов списка. По умолчанию недоступен
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             */
            this.getBasicSingleSelectOptions = function (allowClear, allowSearch, formatter) {
                var select = buildBasicSelectOptions(false, allowClear, formatter);
                if (!allowSearch) {
                    select.options.minimumResultsForSearch = -1;
                }
                return select;
            };

            /**
             * Получить настройки выпадающего списка множественного выбора с постоянным набором элементов. Задано все, кроме data.results
             * @param allowClear Возможна ли очистка
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             */
            this.getBasicMultipleSelectOptions = function (allowClear, formatter) {
                return buildBasicSelectOptions(true, allowClear, formatter);
            };

            /**
             * Получить настройки выпадающего списка единичного выбора с постоянным набором элементов
             * @param allowClear Возможна ли очистка
             * @param results Элементы списка
             * @param allowSearch Доступен поиск, отображается поле ввода для поиска среди элементов списка. По умолчанию недоступен
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             */
            this.getBasicSingleSelectOptionsWithResults = function (allowClear, results, allowSearch, formatter) {
                var select = this.getBasicSingleSelectOptions(allowClear, allowSearch, formatter);
                select.options.data.results = results;
                return select;
            };

            /**
             * Получить настройки выпадающего списка множественного выбора с постоянным набором элементов
             * @param allowClear Возможна ли очистка
             * @param results Элементы списка
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             */
            this.getBasicMultiSelectOptionsWithResults = function (allowClear, results, formatter) {
                var select = this.getBasicMultipleSelectOptions(allowClear, formatter);
                select.options.data.results = results;
                return select;
            };

            /**
             * Получить настройки выпадающего списка, элементы которого загружаются по AJAX
             * @param isMultiple Выбор множественный
             * @param allowClear Возможна ли очистка
             * @param url URL, по которому запрашиваются элементы списка
             * @param dataFilter Объект с параметрами для фильтрации данных
             * @param sortParams Параметры сортировки: property - поле, по которому выполняется сортировка, direction - порядок
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             * @param searchField Поле, по которому выполняется поиск. Значение по умолчанию: name
             */
            this.getAjaxSelectOptions = function (isMultiple, allowClear, url, dataFilter, sortParams, formatter, searchField) {
                if (formatter === undefined) {
                    formatter = 'nameFormatter';
                }
                if (searchField === undefined) {
                    searchField = "name";
                }

                //Создать список с общими для всех типов параметрами
                var select = buildSelectOptionsTemplate(isMultiple, allowClear, formatter);

                //Добавить в него объект, поля которого используются для фильтрации данных
                select.options.dataFilter = dataFilter;

                select.options.ajax = {
                    url: url,
                    quietMillis: 200,
                    data: function (term, page) {
                        var pagingParams = {
                            count: 50,
                            page: page,
                            property: sortParams.property,
                            direction: sortParams.direction
                        };
                        var dataObject = {pagingParams: JSON.stringify(pagingParams)};
                        dataObject[searchField] = term;
                        //Добавить в запрос поля для фильтрации данных
                        if (select.options.dataFilter) {
                            angular.forEach(select.options.dataFilter, function (value, key) {
                                dataObject[key] = value;
                            });
                        }
                        return dataObject;
                    },
                    results: function (data, page) {
                        var more = (page * 50) < data.records;
                        return {results: data.rows, more: more};
                    }
                };

                return select;
            };
        }])


        /**
         * Контроллер для выбора тега корретировки
         */
        .controller('SelectCorrectionTagCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var correctionTags = [APP_CONSTANTS.CORRETION_TAG.ONLY_CORRECTIVE, APP_CONSTANTS.CORRETION_TAG.ONLY_PRIMARY, APP_CONSTANTS.CORRETION_TAG.ALL];
                $scope.correctionTagSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(false, correctionTags);
            }])

        /**
         * Контроллер для выбора типа формы
         */
        .controller('SelectDeclarationKindCtrl', ['$scope', '$rootScope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, $rootScope, APP_CONSTANTS, GetSelectOption) {
                var declarationKinds = [];

                $scope.initSingleSelectAllKinds = function () {
                    declarationKinds = [APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY, APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED, APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS];
                    $scope.declarationKindSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, declarationKinds);
                };

                $scope.initMultiSelectForNdflFilter = function () {
                    if ($rootScope.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_CONTROL_NS) || $rootScope.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_CONTROL_UNP)) {
                        declarationKinds = [APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY, APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED];
                    } else if ($rootScope.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_OPER)) {
                        declarationKinds = [APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY];
                    }

                    $scope.declarationKindSelect = GetSelectOption.getBasicMultiSelectOptionsWithResults(true, declarationKinds);
                };
            }])

        /**
         * Контроллер для выбора состояния налоговой
         */
        .controller('SelectDeclarationStateCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var declarationStates = [APP_CONSTANTS.STATE.CREATED, APP_CONSTANTS.STATE.PREPARED, APP_CONSTANTS.STATE.ACCEPTED];
                $scope.stateSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, declarationStates);
            }])

        /**
         * Контроллер для выбора состояния документа
         */
        .controller('SelectDocStateCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var docStates = [APP_CONSTANTS.DOC_STATE.ACCEPTED, APP_CONSTANTS.DOC_STATE.REFUSED, APP_CONSTANTS.DOC_STATE.REVISION, APP_CONSTANTS.DOC_STATE.SUCCESSFUL, APP_CONSTANTS.DOC_STATE.ERROR];
                $scope.docStateSelect = GetSelectOption.getBasicMultiSelectOptionsWithResults(true, docStates);
            }])

        /**
         * Контроллер для выбора АСНУ
         */
        .controller('SelectAsnuCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', 'RefBookValuesResource',
            function ($scope, APP_CONSTANTS, GetSelectOption, RefBookValuesResource) {

                $scope.initMultipleSelectAsnu = function () {
                    $scope.asnuSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                    RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.ASNU}, function (data) {
                        $scope.asnuSelect.options.data.results = data;
                    });
                };

                $scope.initSingleSelectAsnu = function () {
                    $scope.asnuSelect = GetSelectOption.getBasicSingleSelectOptions(true);
                    RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.ASNU}, function (data) {
                        $scope.asnuSelect.options.data.results = data;
                    });
                };
            }])

        /**
         * Контроллер для выбора вида формы
         */
        .controller('SelectDeclarationTypeCtrl', ['$scope', '$rootScope', 'APP_CONSTANTS', 'GetSelectOption',
            'RefBookValuesResource', 'DeclarationTypeForCreateResource',
            function ($scope, $rootScope, APP_CONSTANTS, GetSelectOption, RefBookValuesResource, DeclarationTypeForCreateResource) {
                $scope.declarationTypeSelect = {};

                //TODO: https://jira.aplana.com/browse/SBRFNDFL-2358 Сделать селекты, общие для отчетных и налоговых форм, убрать лишние функции
                var isTaxForm = function (declarationType) {
                    return declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY.id || declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED.id;
                };

                var isReportForm = function (declarationType) {
                    return declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.id || declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2.id || declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL.id;
                };

                /**
                 * Инициализировать список со всеми видами форм
                 */
                //Убрать фильтрацию data в рамках TODO: https://jira.aplana.com/browse/SBRFNDFL-2358
                $scope.initSelectWithAllDeclarationTypes = function () {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                    RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.DECLARATION_TYPE}, function (data) {
                        data = data.filter(function (declarationType) {
                            return isTaxForm(declarationType);
                        });
                        $scope.declarationTypeSelect.options.data.results = data;
                    });
                };

                /**
                 * Инициализировать список со всеми видами форм
                 */
                //Убрать фильтрацию data в рамках TODO: https://jira.aplana.com/browse/SBRFNDFL-2358
                $scope.initSelectWithTaxAndReportDeclarationTypes = function () {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                    RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.DECLARATION_TYPE}, function (data) {
                        data = data.filter(function (declarationType) {
                            return isTaxForm(declarationType) || isReportForm(declarationType);
                        });
                        $scope.declarationTypeSelect.options.data.results = data;
                    });
                };

                /**
                 * Инициализировать список с видами форм, которые можно создать
                 * @param declarationKind Тип налоговой формы
                 * @param periodObject Выражение из scope, по которому отслеживается изменение периода
                 * @param departmentObject Выражение из scope, по которому отслеживается изменение подразделения
                 */
                //Убрать фильтрацию data в рамках TODO: https://jira.aplana.com/browse/SBRFNDFL-2358
                $scope.initSelectWithDeclarationTypesForCreate = function (declarationKind, periodObject, departmentObject) {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicSingleSelectOptions(true);
                    //Список обновляется при изменении отчетного периода и подразделения
                    $scope.$watchGroup([periodObject, departmentObject], function (newValues) {
                        var period = newValues[0];
                        var department = newValues[1];
                        if (declarationKind && period && department) {
                            DeclarationTypeForCreateResource.query({
                                formDataKindIdList: declarationKind,
                                departmentId: department.id,
                                periodId: period.id
                            }, function (data) {
                                data = data.filter(function (declarationType) {
                                    if (declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY.id) {
                                        return isTaxForm(declarationType) && $rootScope.declarationPrimaryCreateAllowed;
                                    }
                                    return isTaxForm(declarationType) && $rootScope.declarationConsolidatedCreateAllowed;
                                });
                                $scope.declarationTypeSelect.options.data.results = data;
                            });
                        }
                    });
                };

                /**
                 * Инициализировать список со всеми видами отчетных форм
                 */
                //Убрать фильтрацию data в рамках TODO: https://jira.aplana.com/browse/SBRFNDFL-2358
                $scope.initSelectWithAllReportDeclarationTypes = function () {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                    RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.DECLARATION_TYPE}, function (data) {
                        data = data.filter(function (declarationType) {
                            return isReportForm(declarationType);
                        });
                        $scope.declarationTypeSelect.options.data.results = data;
                    });
                };

                /**
                 * Инициализировать список с видами отчетных форм, которые можно создать
                 * @param declarationKind Тип налоговой формы
                 * @param periodObject Выражение из scope, по которому отслеживается изменение периода
                 * @param departmentObject Выражение из scope, по которому отслеживается изменение подразделения
                 */
                //Убрать фильтрацию data в рамках TODO: https://jira.aplana.com/browse/SBRFNDFL-2358
                $scope.initSelectWithReportDeclarationTypesForCreate = function (declarationKind, periodObject, departmentObject) {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicSingleSelectOptions(true);
                    //Список обновляется при изменении отчетного периода и подразделения
                    $scope.$watchGroup([periodObject, departmentObject], function (newValues) {
                        var period = newValues[0];
                        var department = newValues[1];
                        if (declarationKind && period && department) {
                            DeclarationTypeForCreateResource.query({
                                formDataKindIdList: declarationKind,
                                departmentId: department.id,
                                periodId: period.id
                            }, function (data) {
                                data = data.filter(function (declarationType) {
                                    return isReportForm(declarationType);
                                });
                                $scope.declarationTypeSelect.options.data.results = data;
                            });
                        }
                    });
                };
            }])

        /**
         * Контроллер для выбора периода
         */
        .controller('SelectPeriodCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', 'ReportPeriodResource',
            function ($scope, APP_CONSTANTS, GetSelectOption, ReportPeriodResource) {
                $scope.periodSelect = {};

                /**
                 * Заполнить список, загрузив данные из указанной проекции, и определить последний период
                 * @param projection Проекция
                 * @param latestPeriod Последний период
                 */
                var fillSelectListAndFindLatestPeriod = function (projection, latestPeriod) {
                    ReportPeriodResource.query({
                        projection: projection
                    }, function (data) {
                        $scope.periodSelect.options.data.results = data;
                        if (latestPeriod && data && data.length > 0) {
                            latestPeriod.period = data[0];
                            angular.forEach(data, function (period) {
                                if (Date.parse(latestPeriod.period.endDate) < Date.parse(period.endDate)) {
                                    latestPeriod.period = period;
                                }
                            });
                        }
                    });
                };

                /**
                 * Добавить в список все отчетные периоды и определить последний
                 */
                $scope.initSelectWithAllPeriods = function (latestPeriod) {
                    $scope.periodSelect = GetSelectOption.getBasicMultipleSelectOptions(true, 'periodFormatter');
                    fillSelectListAndFindLatestPeriod("all", latestPeriod);
                };

                /**
                 * Добавить в список открытые отчетные периоды и определить последний
                 */
                $scope.initSelectWithOpenPeriods = function (latestPeriod) {
                    $scope.periodSelect = GetSelectOption.getBasicSingleSelectOptions(true, true, 'periodFormatter');
                    fillSelectListAndFindLatestPeriod("opened", latestPeriod);
                };

                /**
                 * @description Инициализировать выпадающий список пустым массивом
                 */
                $scope.initEmptySelect = function () {
                    $scope.periodSelect = GetSelectOption.getBasicSingleSelectOptions(true, true, 'periodFormatter');
                    $scope.periodSelect.options.data.results = [];

                };
                // При событии выбора подразделения из списка, получает назначенные подразделению периоды и выбирает последний
                $scope.$on(APP_CONSTANTS.EVENTS.DEPARTMENT_SELECTED, function (event, departmentId) {
                    ReportPeriodResource.query({
                        projection: "forDepartment",
                        departmentId: departmentId
                    }, function (data) {
                        $scope.$emit(APP_CONSTANTS.EVENTS.LAST_PERIOD_SELECT, data[0]);
                        $scope.periodSelect.options.data.results = data;
                    });
                });
            }])

        /**
         * Контроллер для выбора подразделений
         */
        .controller('SelectDepartmentCtrl', ['$scope', '$rootScope', 'GetSelectOption', 'APP_CONSTANTS', 'RefBookValuesResource', 'DepartmentResource',
            function ($scope, $rootScope, GetSelectOption, APP_CONSTANTS, RefBookValuesResource, DepartmentResource) {
                $scope.departmentsSelect = {};

                /**
                 * Инициализировать список с загрузкой всех подразделений через ajax
                 */
                $scope.initSelectWithAllDepartments = function () {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(true, true, "controller/rest/refBookValues/30?projection=allDepartments", {}, {
                        property: "fullName",
                        direction: "asc"
                    }, "fullNameFormatter");
                };

                /**
                 * Инициализировать список с загрузкой всех достуных для бизнес-администрирования подразделений через ajax
                 */
                $scope.initSelectWithBADepartments = function () {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(true, true, "controller/rest/refBookValues/30?projection=BADepartments", {}, {
                        property: "fullName",
                        direction: "asc"
                    }, "fullNameFormatter");
                };

                /**
                 * Инициализировать список с загрузкой всех достуных для назначения исполнителями подразделений через ajax
                 */
                $scope.initSelectWithDestinationDepartments = function () {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(true, true, "controller/rest/refBookValues/30?projection=destinationDepartments", {}, {
                        property: "fullName",
                        direction: "asc"
                    }, "fullNameFormatter");
                };

                /**
                 * Инициализировать список с загрузкой всех подразделений через ajax
                 */
                $scope.initSingleSelectWithAllDepartments = function () {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(false, true, "controller/rest/refBookValues/30?projection=allDepartments", {}, {
                        property: "fullName",
                        direction: "asc"
                    }, "fullNameFormatter");
                };

                /**
                 * Инициализировать список с загрузкой подразделений с открытым периодом через ajax
                 * @param periodObject Выражение из scope, по которому отслеживается изменение периода
                 */
                $scope.initActiveDepartmentSelectWithOpenPeriod = function (periodObject) {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(false, true, "controller/rest/refBookValues/30?projection=activeDepartmentsWithOpenPeriod", {}, {
                        property: "fullName",
                        direction: "asc"
                    }, "fullNameFormatter");
                    $scope.$watch(periodObject, function (period) {
                        if (period) {
                            $scope.departmentsSelect.options.dataFilter = {reportPeriodId: period.id};
                        }
                    });
                };
                /**
                 * Инициализировать список с загрузкой действующих доступных ТБ для создания отчётности через ajax
                 * @param periodObject Выражение из scope, по которому отслеживается изменение периода
                 * @param userTBDepartment Объект из scope, по которому проставляется ТБ пользователя
                 */
                $scope.initActiveAvailableTBSelect = function (periodObject, userTBDepartment) {
                    $scope.departmentsSelect = GetSelectOption.getBasicSingleSelectOptions(true, true, "fullNameFormatter");
                    RefBookValuesResource.query({
                        refBookId: APP_CONSTANTS.REFBOOK.DEPARTMENT,
                        projection: "activeAvailableTB"
                    }, function (data) {
                        $scope.departmentsSelect.options.data.results = data;
                        angular.forEach(data, function (department) {
                            if (userTBDepartment.id === department.id) {
                                userTBDepartment.department = department;
                            }
                        });
                    });
                    $scope.$watch(periodObject, function (period) {
                        if (period) {
                            $scope.departmentsSelect.options.dataFilter = {reportPeriodId: period.id};
                        }
                    });
                };

                /**
                 * @description Инициализировать список с загрузкой действующих доступных ТБ для фильтрации настроек подразделений
                 */
                $scope.initAvailableTBSelect = function () {
                    $scope.departmentsSelect = GetSelectOption.getBasicSingleSelectOptions(true, true, "fullNameFormatter");
                    RefBookValuesResource.query({
                        refBookId: APP_CONSTANTS.REFBOOK.DEPARTMENT,
                        projection: "activeAvailableTB"
                    }, function (availableTBs) {
                        $scope.departmentsSelect.options.data.results = availableTBs;
                        // получаем тербанк пользователя
                        DepartmentResource.query({
                                departmentId: $rootScope.user.department.id,
                                projection: 'fetchParentTB'
                            }, function (userTB) {
                            // находим в списке тербанков тербанк пользователя и инициируем событие для выделения этого тербанка в выпадающем списке
                                angular.forEach(availableTBs, function (department) {
                                    if (userTB.id === department.id) {
                                        $scope.$emit(APP_CONSTANTS.EVENTS.USER_TB_SELECT, department);
                                    }
                                });
                            }
                        );
                    });
                };

            }])

        /**
         * Контроллер для выбора периода корректировки
         */
        .controller('SelectCorrectPeriodCtrl', ['$scope', 'GetSelectOption',
            function ($scope, GetSelectOption) {
                $scope.periodSelect = {};

                /**
                 * Инициализация списка с загрузкой доступных периодов корректировки
                 */
                $scope.initCorrectPeriods = function (departmentId) {
                    $scope.periodSelect = GetSelectOption.getAjaxSelectOptions(false, true, "controller/rest/departmentReportPeriod?projection=closedWithoutCorrection", {departmentId: departmentId}, {},
                        "periodFormatter");
                };

                $scope.initReportPeriodType = function () {
                    $scope.periodSelect = GetSelectOption.getAjaxSelectOptions(false, true, "controller/rest/refBookValues/reportPeriodType", {}, {},
                        'periodTypeFormatter');
                };
            }
        ])

        /**
         * Контроллер для выбора конфигурационного параметра
         */
        .controller('SelectConfigParamCtrl', ['$scope', 'GetSelectOption', 'CommonParamResource',
            function ($scope, GetSelectOption, CommonParamResource) {
                $scope.commonParamSelect = {};

                /**
                 * Инициализация списка с загрузкой доступных конфигурационных параметров
                 */
                $scope.initCommonParam = function () {
                    $scope.commonParamSelect = GetSelectOption.getBasicSingleSelectOptions(true, true, "configParamFormatter");
                    CommonParamResource.query({
                        projection: 'notCreated',
                        pagingParams: {
                            page: 1,
                            count: 10000
                        }
                    }, function (data) {
                        if (data && data.rows) {
                            var id = 0;
                            angular.forEach(data.rows, function (item) {
                                item.id = id;
                                id++;
                            });
                            $scope.commonParamSelect.options.data.results = data.rows;
                        }
                    });
                };
            }
        ])

        /**
         * Контроллер для выбора ОКТМО
         */
        .controller('SelectOktmoCtrl', ['$scope', 'GetSelectOption',
            function ($scope, GetSelectOption) {
                $scope.oktmoSelect = {};

                /**
                 * Инициализация полного списка ОКТМО
                 */
                $scope.initSelectWithAllOktmo = function () {
                    $scope.oktmoSelect = GetSelectOption.getAjaxSelectOptions(false, true, "controller/rest/refBookValues/96", {}, {
                        property: "name",
                        direction: "asc"
                    }, "oktmoFormatter");
                };
            }
        ])

        /**
         * Контроллер для выбора типов налоговых форм
         */
        .controller('SelectFormTypeCtrl', ['$scope', 'GetSelectOption', 'RefBookValuesResource', 'APP_CONSTANTS',
            function ($scope, GetSelectOption, RefBookValuesResource, APP_CONSTANTS) {
                $scope.formTypeSelect = {};

                $scope.initSelectWithAllFormTypes = function () {
                    $scope.formTypeSelect = GetSelectOption.getBasicSingleSelectOptions(true);
                    RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.DECLARATION_DATA_TYPE_REF_BOOK}, function (data) {
                        $scope.formTypeSelect.options.data.results = data;
                    });
                };
            }
        ]);
}());
