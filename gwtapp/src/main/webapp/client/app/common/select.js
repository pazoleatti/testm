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
                    quietMillis: 100,
                    data: function (term, page) {
                        var pagingParams = {
                            count: 10,
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
                        var more = (page * 10) < data.records;
                        return {results: data.rows, more: more};
                    }
                };

                return select;
            };

            /**
             * Получить настройки выпадающего списка с возможностью использования дополнительного фильтра
             * @param isMultiple    выбор множественный
             * @param allowClear    возможна ли очистка
             * @param url           URL, по которому запрашиваются элементы списка
             * @param filterColumns объект с параметрами для фильтрации данных
             * @param filter        строка являющаяся часть sql выражения и выступающая в качестве фильтра
             * @param sortParams    параметры сортировки: property - поле, по которому выполняется сортировка, direction - порядок
             * @param formatter     фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             * @param searchField   поле, по которому выполняется поиск. Значение по умолчанию: name
             */
            this.getAjaxAdditionalFilterSelectOptions = function (isMultiple, allowClear, url, filterColumns, filter, sortParams, formatter, searchField) {
                var select = this.getAjaxSelectOptions(isMultiple, allowClear, url, filterColumns, sortParams, formatter, searchField)
                select.options.dataFilter.filter = filter;
                return select;
            };
        }])


        /**
         * Контроллер для выбора тега корретировки
         */
        .controller('SelectCorrectionTagCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var correctionTags = [APP_CONSTANTS.CORRECTION_TAG.ONLY_CORRECTIVE, APP_CONSTANTS.CORRECTION_TAG.ONLY_PRIMARY, APP_CONSTANTS.CORRECTION_TAG.ALL];
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
                    RefBookValuesResource.query({
                        refBookId: APP_CONSTANTS.REFBOOK.ASNU,
                        nooverlay: true
                    }, function (data) {
                        $scope.asnuSelect.options.data.results = data;
                    });
                };

                /**
                 *
                 */
                $scope.$on(APP_CONSTANTS.EVENTS.DEPARTMENT_AND_PERIOD_SELECTED, function (event, period, department) {
                    $scope.initSelectWithReportDeclarationTypesForCreate(period, department)
                });
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
                                periodId: period.id,
                                nooverlay: true
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
                 * @param periodId Выражение из scope, по которому отслеживается изменение периода
                 * @param departmentId Выражение из scope, по которому отслеживается изменение подразделения
                 */
                //Убрать фильтрацию data в рамках TODO: https://jira.aplana.com/browse/SBRFNDFL-2358
                $scope.initSelectWithReportDeclarationTypesForCreate = function (periodId, departmentId) {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicSingleSelectOptions(true);
                    DeclarationTypeForCreateResource.query({
                        formDataKindIdList: APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id,
                        departmentId: departmentId,
                        periodId: periodId
                    }, function (data) {
                        data = data.filter(function (declarationType) {
                            return isReportForm(declarationType);
                        });
                        $scope.declarationTypeSelect.options.data.results = data;
                    });
                };

                /**
                 * Событие возникающие при выборе подразделения и периода
                 */
                $scope.$on(APP_CONSTANTS.EVENTS.DEPARTMENT_AND_PERIOD_SELECTED, function (event, period, department) {
                    $scope.initSelectWithReportDeclarationTypesForCreate(period, department)
                });
            }])

        /**
         * Контроллер для выбора периода
         */
        .controller('SelectPeriodCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', 'ReportPeriodResource', '$filter',
            function ($scope, APP_CONSTANTS, GetSelectOption, ReportPeriodResource, $filter) {
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

                /**
                 * Инициализировать выпадающий список периодами, коорые активны и открыты для определенного подразделения
                 * @param departmentId идентификатор подразделения
                 * @param periodObject объект периода который будет выделен в списке
                 */
                $scope.initSelectWithOpenDepartmentPeriods = function (departmentId, periodObject) {
                    if (typeof(departmentId) !== 'undefined' && departmentId != null) {
                        $scope.periodSelect = GetSelectOption.getBasicSingleSelectOptions(true, true, 'periodFormatterWithCorrectionDate');
                        $scope.periodSelect.options.data = function () {
                            return angular.extend({results: $scope.results}, $scope.periodSelect.options.data);
                        };
                        ReportPeriodResource.query({
                            projection: "forDepartment",
                            departmentId: departmentId
                        }, function (data) {
                            $scope.results = data;
                            if (periodObject && data && data.length > 0) {
                                periodObject.period = data[0];
                                angular.forEach(data, function (period) {
                                    period.text = $filter('periodFormatterWithCorrectionDate')(period);
                                    if (Date.parse(periodObject.period.endDate) <= Date.parse(period.endDate)) {
                                        periodObject.period = period;
                                    }
                                });
                            }
                        });
                    }

                };

                // При событии выбора подразделения из списка, получает назначенные подразделению периоды и выбирает последний
                $scope.$on(APP_CONSTANTS.EVENTS.DEPARTMENT_SELECTED, function (event, departmentId, latestPeriod) {
                    $scope.initSelectWithOpenDepartmentPeriods(departmentId, latestPeriod);
                });
            }])

        /**
         * Контроллер для выбора подразделений
         */
        .controller('SelectDepartmentCtrl', ['$scope', '$rootScope', 'GetSelectOption', 'APP_CONSTANTS', 'RefBookValuesResource', 'DepartmentResource',
            function ($scope, $rootScope, GetSelectOption, APP_CONSTANTS, RefBookValuesResource, DepartmentResource) {
                $scope.departmentsSelect = {};

                /**
                 * Инициализировать список с ajax-подгрузкой всех подразделений.
                 */
                $scope.initSelectWithAllDepartments = function () {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(true, true, 'controller/rest/departments?projection=name', {}, {
                        property: 'id',
                        direction: 'asc'
                    });
                };

                /**
                 * Инициализировать список с загрузкой всех доступных пользователю подразделений через ajax
                 */
                $scope.initSelectWithAllAvailableDepartments = function () {
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
                 * @description Инициализировать список с загрузкой действующих доступных ТБ
                 */
                $scope.initAvailableTBSelect = function (departmentModel) {
                    $scope.departmentsSelect = GetSelectOption.getBasicSingleSelectOptions(false, true);
                    loadActiveAvailableTBs(departmentModel);
                };

                function loadActiveAvailableTBs(departmentModel) {
                    RefBookValuesResource.query({
                        refBookId: APP_CONSTANTS.REFBOOK.DEPARTMENT,
                        projection: "activeAvailableTB"
                    }, function (availableTBs) {
                        $scope.departmentsSelect.options.data.results = availableTBs;
                        if (departmentModel) {
                            departmentModel.department = getUserTB(availableTBs);
                        }
                    });
                }

                function getUserTB(departments) {
                    // значение по-умолчанию будет подразделение пользователя
                    var defaultDepartment = $scope.user.terBank && _.find(departments, function (department) {
                        return department.id === $scope.user.terBank.id;
                    });
                    // если подразделение пользователя не найдено, то первое попавшееся
                    if (!defaultDepartment) {
                        defaultDepartment = departments[0];
                    }
                    return defaultDepartment;
                }

                $scope.initTBMultiSelect = function () {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(
                        true,
                        true,
                        'controller/rest/departments?projection=tb',
                        {},
                        {
                            property: 'name',
                            direction: 'asc'
                        },
                        'departmentActivityFormatter'
                    );
                }
            }
        ])

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
                    }, "codeNameFormatter");
                };
            }
        ])

        /**
         * Контроллер для выбора признака активности пользователя.
         */
        .controller('SelectUserActivityCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var userActivities = [APP_CONSTANTS.USER_ACTIVITY.YES, APP_CONSTANTS.USER_ACTIVITY.NO];
                $scope.userActivitySelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, userActivities);
            }
        ])

        .controller('SelectPersonImportanceCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var values = [APP_CONSTANTS.PERSON_IMPORTANCE.VIP, APP_CONSTANTS.PERSON_IMPORTANCE.NOT_VIP];
                $scope.select = GetSelectOption.getBasicMultiSelectOptionsWithResults(true, values);
            }
        ])

        /**
         * Контроллер для выбора роли пользователя.
         */
        .controller('SelectUserRolesCtrl', ['$scope', '$http', 'GetSelectOption',
            function ($scope, $http, GetSelectOption) {
                $scope.userRolesSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                $http.get('controller/rest/roles').success(function (data) {
                    $scope.userRolesSelect.options.data.results = data;
                });
            }
        ])

        /**
         * Контроллер для выбора признака активности пользователя.
         */
        .controller('SelectShowVersionsCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var attributes = [APP_CONSTANTS.SHOW_VERSIONS.BY_DATE, APP_CONSTANTS.SHOW_VERSIONS.ALL];
                $scope.versionSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(false, attributes);
            }
        ])

        /**
         * Контроллер для выбора фильтрации по дубликатам.
         */
        .controller('SelectDuplicatesCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var attributes = [APP_CONSTANTS.SHOW_DUPLICATES.NO, APP_CONSTANTS.SHOW_DUPLICATES.ONLY_DUPLICATES, APP_CONSTANTS.SHOW_DUPLICATES.ALL_RECORDS];
                $scope.select = GetSelectOption.getBasicSingleSelectOptionsWithResults(false, attributes);
            }
        ])

        /**
         * Универсальный контроллер для выбора из любого справочника
         */
        .controller('SelectRefBookCtrl', ['$scope', 'GetSelectOption', '$http', "$filter", "APP_CONSTANTS",
            function ($scope, GetSelectOption, $http, $filter, APP_CONSTANTS) {
                $scope.select = {};

                // Список конфигов для разных справочников
                $scope.refBookConfig = {};
                // ОКТМО
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.OKTMO] = {
                    sort: {
                        property: "CODE",
                        direction: "asc"
                    },
                    filter: {
                        columns: ["NAME", "CODE"]
                    },
                    formatter: "codeNameFormatter"
                };
                // Коды места представления расчета
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.PRESENT_PLACE] = $scope.refBookConfig[APP_CONSTANTS.REFBOOK.OKTMO];
                // Признак лица, подписавшего документ
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.SIGNATORY_MARK] = $scope.refBookConfig[APP_CONSTANTS.REFBOOK.OKTMO];
                // Коды форм реорганизации (ликвидации) организации
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.REORGANIZATION] = $scope.refBookConfig[APP_CONSTANTS.REFBOOK.OKTMO];
                // Тип ДУЛ
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.DOC_TYPE] = $scope.refBookConfig[APP_CONSTANTS.REFBOOK.OKTMO];
                // Страны
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.COUNTRY] = $scope.refBookConfig[APP_CONSTANTS.REFBOOK.OKTMO];
                // Система-источник
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.ASNU] = $scope.refBookConfig[APP_CONSTANTS.REFBOOK.OKTMO];
                // Коды видов доходов
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.INCOME_CODE] = {
                    filter: {
                        columns: ["NAME", "CODE"]
                    },
                    formatter: "codeNameFormatter"
                };
                // Физические лица
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.PERSON] = {
                    // TODO: этот справочник слишком сложный и в таком виде нормальный поиск по нему невозможен - нужен отдельный виджет. По крайней мере можно использовать новое апи для этого справочника, чтобы быстрее получать записи
                    sort: {
                        property: "LAST_NAME",
                        direction: "asc"
                    },
                    filter: {
                        columns: ["FIRST_NAME, LAST_NAME, MIDDLE_NAME"]
                    },
                    formatter: "personFormatter"
                };
                // Адреса физических лиц TODO: этот справочник слишком сложный и в таком виде нормальный поиск по нему невозможен - нужен отдельный виджет
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.PERSON_ADDRESS] = {
                    sort: {
                        property: "ID", //TODO: тут нужна комбинация полей
                        direction: "asc"
                    },
                    filter: {
                        columns: ["REGION_CODE, POSTAL_CODE, DISTRICT, CITY, LOCALITY, STREET, HOUSE, BUILD, APARTMENT"]
                    },
                    formatter: "personAddressFormatter"
                };
                // Статусы налогоплательщика
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.TAXPAYER_STATUS] = {
                    sort: {
                        property: "CODE",
                        direction: "asc"
                    },
                    filter: {
                        columns: ["NAME", "CODE"]
                    },
                    formatter: "codeNameFormatter"
                };
                // Подразделения
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.DEPARTMENT] = {
                    sort: {
                        property: "fullName",
                        direction: "asc"
                    },
                    filter: {},
                    formatter: "fullNameFormatter"
                };
                // ОК 025-2001 (Общероссийский классификатор стран мира)
                // Виды документов, удостоверяющих личность
                // АСНУ
                // Признак кода вычета
                $scope.refBookConfig.default = {
                    sort: {
                        property: "NAME",
                        direction: "asc"
                    },
                    filter: {
                        columns: ["NAME"]
                    },
                    formatter: "nameFormatter"
                };

                /**
                 * Инициализация списка записей справочника
                 * @param refBookId идентификатор справочника, записи которого будут получены
                 * @param attributeAlias алиас атрибута записи справочника, сама которая является ссылочным значением. Используется для подгрузки полного значения в поле объекта
                 */
                $scope.initSelect = function (refBookId, attributeAlias, filter) {
                    if (attributeAlias) {
                        /**
                         * Событие первичного проставления значения в выпадашке. Используется для подгрузки "полного" значения записи справочника по ее идентификатору
                         */
                        $scope.$watch("record." + attributeAlias + ".referenceObject", function (newValue, oldValue) {
                            if (newValue) {
                                $http({
                                    method: "GET",
                                    url: "controller/rest/refBook/" + refBookId + "/record/" + newValue.id.value
                                }).success(function (record) {
                                    $scope.record[attributeAlias].value = record;
                                });
                            }
                        });

                        /**
                         * Событие изменения значения в выпадашке. Используется для изменения title
                         */
                        $scope.$watch("record." + attributeAlias + ".value", function (newValue, oldValue) {
                            if (newValue !== oldValue) {
                                $scope.attributeTitle = $filter($scope.config.formatter)(newValue);
                            }
                        });
                    }

                    $scope.config = $scope.refBookConfig[refBookId] ? $scope.refBookConfig[refBookId] : $scope.refBookConfig.default;
                    if (refBookId === APP_CONSTANTS.REFBOOK.DEPARTMENT) {
                        $scope.select = GetSelectOption.getAjaxSelectOptions(false, true, "controller/rest/refBookValues/30?projection=allDepartments",
                            {}, $scope.config.sort, $scope.config.formatter);
                    } else {
                        var isMultiple = (
                            refBookId === APP_CONSTANTS.REFBOOK.DOC_TYPE
                            || refBookId === APP_CONSTANTS.REFBOOK.COUNTRY
                            || refBookId === APP_CONSTANTS.REFBOOK.TAXPAYER_STATUS
                            || refBookId === APP_CONSTANTS.REFBOOK.ASNU
                        );
                        $scope.select = GetSelectOption.getAjaxAdditionalFilterSelectOptions(isMultiple, true, "controller/rest/refBook/" + refBookId + "/records",
                            $scope.config.filter,
                            filter ? filter : '',
                            $scope.config.sort ? $scope.config.sort : $scope.refBookConfig.default.sort,
                            $scope.config.formatter,
                            "searchPattern"
                        );
                    }
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
        ])

        /**
         * Bыборка значений из заданого массива объектов
         */
        .controller('SelectFromArrayCtrl', ['$scope', 'GetSelectOption', 'RefBookValuesResource',
            function ($scope, GetSelectOption) {
                $scope.selectFromArray = {};

                $scope.init = function (values) {
                    $scope.selectFromArray = GetSelectOption.getBasicSingleSelectOptions(false);
                    $scope.selectFromArray.options.data.results = values;
                };
            }
        ])

        /**
         * Bыборка множественного значения из заданого массива объектов
         */
        .controller('SelectMultipleFromArrayCtrl', ['$scope', 'GetSelectOption', 'RefBookValuesResource',
            function ($scope, GetSelectOption) {
                $scope.selectFromArray = {};

                $scope.init = function (values, allowClear) {
                    $scope.selectFromArray = GetSelectOption.getBasicMultipleSelectOptions(allowClear);
                    $scope.selectFromArray.options.data.results = values;
                };
            }
        ])

        /**
         * Контроллер для карточки реестра ФЛ
         */
        .controller('SelectRegistryPersonController', ['$scope', 'GetSelectOption', '$http', 'APP_CONSTANTS',
            'RefBookRecordResource', 'TaxPayerStateResource', 'RefBookAsnuResource', 'RefBookCountryResource',
            'RefBookDocTypeResource',
            function ($scope, GetSelectOption, $http, APP_CONSTANTS, RefBookRecordResource,
                      TaxPayerStateResource, RefBookAsnuResource, RefBookCountryResource, RefBookDocTypeResource) {

                /**
                 * Инициализировать выпадашку для выбора главного ДУЛ
                 * @param person изменяемое ФЛ
                 */
                $scope.initIdDocs = function (person) {
                    $scope.selectedDocs = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [], true, 'idDocFormatter');
                    person.documents.value.sort(function (obj1, obj2) {
                        var priority1 = obj1.docType.priority;
                        var priority2 = obj2.docType.priority;
                        var doc_code1 = obj1.docType.code;
                        var doc_code2 = obj2.docType.code;
                        var doc_number1 = obj1.documentNumber;
                        var doc_number2 = obj2.documentNumber;
                        if (priority1 < priority2) {
                            return -1
                        }
                        if (priority1 > priority2) {
                            return 1
                        }
                        if (doc_code1 < doc_code2) {
                            return -1
                        }
                        if (doc_code1 > doc_code2) {
                            return 1
                        }
                        if (doc_number1 < doc_number2) {
                            return -1
                        }
                        if (doc_number1 > doc_number2) {
                            return 1
                        }
                        return 0;
                    });
                    $scope.selectedDocs.options.data.results = person.documents.value;
                    /*if ($scope.selectedDocs) {
                        $scope.selectedDocs.options.data.results = person.documents.value;
                        angular.forEach($scope.selectedDocs.options.data.results, function (value) {
                            if (person.reportDoc && person.reportDoc.value
                                && value.id === person.reportDoc.value.id) {
                                person.reportDoc.value = value
                            }
                        })
                    }*/
                };

                $scope.$on("addIdDoc", function (event, person) {
                    $scope.initIdDocs(person)
                });

                /**
                 * Инициализировать выпадашку для выбора статуса Налогоплательщика
                 */
                $scope.initTaxPayerState = function () {
                    $scope.taxPayerStateSelected = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [], true, 'codeNameFormatter');
                    TaxPayerStateResource.query({
                        projection: "findAllActive"
                    }, function (data) {
                        $scope.taxPayerStateSelected.options.data.results = data;
                    });
                };

                /**
                 * Инициализировать выпадашку для выбора АСНУ
                 */
                $scope.initAsnu = function () {
                    $scope.asnuSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [], true, 'codeNameFormatter');
                    RefBookAsnuResource.query({
                        projection: "findAllActive"
                    }, function (data) {
                        $scope.asnuSelect.options.data.results = data;
                    });
                };

                /**
                 * Инициализировать выпадашку для установления важности
                 * @param person изменяемое ФЛ
                 */
                $scope.initVip = function (person) {
                    $scope.vipSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [{
                        id: 1,
                        value: true,
                        name: 'VIP'
                    }, {id: 2, value: false, name: 'не VIP'}], false);
                    angular.forEach($scope.vipSelect.options.data.results, function (value) {
                        if (value.value === person.vip) {
                            person.vipSelect = value
                        }
                    })
                };

                /**
                 * Инициализировать выпадашку со списком стран для выбора гражданста
                 */
                $scope.initCitizenship = function () {
                    $scope.citizenshipSelected = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [], true, 'codeNameFormatter');
                    RefBookCountryResource.query({
                        projection: "findAllActive"
                    }, function (data) {
                        $scope.citizenshipSelected.options.data.results = data;
                    });
                };


                /**
                 * Инициализировать выпадашку со списком стран
                 * @param person изменяемое ФЛ
                 */
                $scope.initCountry = function (person) {
                    $scope.countrySelected = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [], true, 'codeNameFormatter');
                    RefBookCountryResource.query({
                        projection: "findAllActive"
                    }, function (data) {
                        $scope.countrySelected.options.data.results = data;
                    });
                };

                /**
                 * Инициализировать выпадашку с кодами документов
                 */
                $scope.initDocType = function () {
                    $scope.docTypeSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [], true, 'codeNameFormatter');
                    RefBookDocTypeResource.query({
                        projection: "findAllActive"
                    }, function (data) {
                        $scope.docTypeSelect.options.data.results = data;
                    });
                }
            }])
    ;
}());
