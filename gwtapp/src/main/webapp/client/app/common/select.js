(function () {
    'use strict';

    /**
     * @description Модуль, содержащий типовые селекты
     */
    angular.module('app.select.common', ['ui.select2', 'app.formatters', 'app.ndflFL'])
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
                            var dataFilter = select.options.dataFilter;
                            if (angular.isFunction(dataFilter)) {
                                dataFilter = dataFilter();
                            }
                            angular.forEach(dataFilter, function (value, key) {
                                if (angular.isObject(value) && !angular.isArray(value)) {
                                    dataObject[key] = JSON.stringify(value);
                                } else {
                                    dataObject[key] = value;
                                }
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
                var select = this.getAjaxSelectOptions(isMultiple, allowClear, url, filterColumns, sortParams, formatter, searchField);
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
                $scope.initSingleSelectDeclarationState = function () {
                    $scope.stateSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true,
                        [APP_CONSTANTS.STATE.CREATED, APP_CONSTANTS.STATE.PREPARED, APP_CONSTANTS.STATE.ACCEPTED]);
                };

                $scope.initSingleSelect2NdflFLState = function () {
                    $scope.stateSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true,
                        [APP_CONSTANTS.STATE.CREATED, APP_CONSTANTS.STATE.ISSUED]);
                };

                $scope.initMultiSelectDeclarationState = function () {
                    $scope.stateSelect = GetSelectOption.getBasicMultiSelectOptionsWithResults(true,
                        [APP_CONSTANTS.STATE.CREATED, APP_CONSTANTS.STATE.PREPARED, APP_CONSTANTS.STATE.ACCEPTED]);
                };
            }]
        )

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

                // Множественный выбор в формате "(код) название"
                $scope.initMultipleCodeNameSelectAsnu = function () {
                    $scope.asnuSelect = GetSelectOption.getBasicMultipleSelectOptions(true, 'codeNameFormatter');
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

                $scope.$on(APP_CONSTANTS.EVENTS.DEPARTMENT_AND_PERIOD_SELECTED, function (event, period, department) {
                    $scope.initSelectWithReportDeclarationTypesForCreate(period, department);
                });
            }]
        )

        /**
         * Контроллер для выбора типов КНФ
         */
        .controller('SelectKnfTypeCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', '$http',
            function ($scope, APP_CONSTANTS, GetSelectOption, $http) {

                $scope.initMultipleSelectKnfType = function () {
                    $scope.knfTypeSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                    loadKnfTypes();
                };

                $scope.initSingleSelectKnfType = function () {
                    $scope.knfTypeSelect = GetSelectOption.getBasicSingleSelectOptions(true);
                    loadKnfTypes();
                };

                function loadKnfTypes() {
                    $http({
                        method: "GET",
                        url: "controller/rest/refBook/" + APP_CONSTANTS.REFBOOK.KNF_TYPE + "/records",
                        params: {
                            columns: ["NAME", "CODE"],
                            searchPattern: "",
                            filter: "",
                            pagingParams: null
                        }
                    }).success(function (data) {
                        $scope.knfTypeSelect.options.data.results = data.rows;
                    });
                }
            }]
        )

        /**
         * Контроллер для выбора вида формы
         */
        .controller('SelectDeclarationTypeCtrl', ['$scope', '$rootScope', 'APP_CONSTANTS', 'GetSelectOption',
            'RefBookValuesResource', 'DeclarationTypeForCreateResource', 'PermissionChecker',
            function ($scope, $rootScope, APP_CONSTANTS, GetSelectOption, RefBookValuesResource, DeclarationTypeForCreateResource, PermissionChecker) {
                $scope.declarationTypeSelect = {};

                var isTaxForm = function (declarationType) {
                    return declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY.id || declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED.id;
                };

                var isReportForm = function (declarationType) {
                    return declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.id
                        || declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2.id
                        || declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL.id;
                };

                /**
                 * Инициализировать список со всеми видами налоговых форм
                 */
                $scope.initSelectWithAllDeclarationTypes = function () {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                    $scope.declarationTypeSelect.options.data.results = [
                        APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY, APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED
                    ];
                };

                /**
                 * Инициализировать список со всеми видами форм
                 */
                $scope.initSelectWithTaxAndReportDeclarationTypes = function () {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                    $scope.declarationTypeSelect.options.data.results = [
                        APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY,
                        APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED,
                        APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1,
                        APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2,
                        APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL
                    ];
                };

                /**
                 * Инициализировать список с видами форм, которые можно создать
                 */
                $scope.initSelectWithDeclarationTypesForCreate = function () {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicSingleSelectOptions(false);
                    $scope.declarationTypeSelect.options.data.results = [];
                    if (PermissionChecker.check($scope.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_PRIMARY)) {
                        $scope.declarationTypeSelect.options.data.results.push(APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY);
                    }
                    if (PermissionChecker.check($scope.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_CONSOLIDATED)) {
                        $scope.declarationTypeSelect.options.data.results.push(APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED);
                    }
                };

                /**
                 * Инициализировать список со всеми видами отчетных форм
                 */
                $scope.initSelectWithAllReportDeclarationTypes = function () {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicMultipleSelectOptions(true);
                    $scope.declarationTypeSelect.options.data.results = [
                        APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1,
                        APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2,
                        APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL
                    ];
                };

                /**
                 * Инициализировать список с видами налоговых форм, доступных при открытии нового периода
                 */
                $scope.initSelectWithOpenPeriodDeclarationTypes = function () {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicSingleSelectOptions(true);
                    $scope.declarationTypeSelect.options.data.results = [
                        APP_CONSTANTS.TAX_FORM_TYPE.REPORT_2_NDFL_1,
                        APP_CONSTANTS.TAX_FORM_TYPE.REPORT_2_NDFL_2,
                        APP_CONSTANTS.TAX_FORM_TYPE.REPORT_6_NDFL,
                        APP_CONSTANTS.TAX_FORM_TYPE.APP_2
                    ];
                };

                /**
                 * Инициализировать список с видами отчетных форм, которые можно создать
                 * @param knf КНФ, из которой брать пары КПП/ОКТМО
                 * @param departmentModelPath путь в scope до модели подразделения
                 * @param periodModelPath путь в scope до модели периода
                 */
                $scope.initSelectWithReportDeclarationTypesForCreate = function (knf, departmentModelPath, periodModelPath) {
                    $scope.declarationTypeSelect = GetSelectOption.getBasicSingleSelectOptions(true);
                    var department = _.deep($scope, departmentModelPath);
                    var period = _.deep($scope, periodModelPath);

                    loadReportFormTypesPeriod(knf, department, period);

                    $scope.$watchCollection("[" + departmentModelPath + ", " + periodModelPath + "]", function (newValues, oldValues) {
                        var department = newValues && newValues[0], oldDepartment = oldValues && oldValues[0];
                        var period = newValues && newValues[1], oldPeriod = oldValues && oldValues[1];
                        if (department && (!oldDepartment || department.id !== oldDepartment.id) ||
                            period && (!oldPeriod || period.id !== oldPeriod.id)
                        ) {
                            loadReportFormTypesPeriod(knf, department, period);
                        }
                    });
                };

                /**
                 * Загружает актуальные в указанном периоде типы отчетных форм, на которые есть назначения подразделения
                 */
                function loadReportFormTypes(knf, department, period) {
                    if (department && period) {
                        DeclarationTypeForCreateResource.query({
                            formDataKindIdList: APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id,
                            departmentId: department.id,
                            periodId: period.id
                        }, function (data) {
                            data = data.filter(function (declarationType) {
                                return isReportForm(declarationType) && (!knf || isReportTypeAvailableByKnfType(declarationType, knf.knfType));
                            });
                            $scope.declarationTypeSelect.options.data.results = data;
                        });
                    }
                }

                /**
                 * Загружает тип отчетной формы из указанного в периоде
                 */
                function loadReportFormTypesPeriod(knf, department, period) {
                    if (department && period) {
                        $scope.declarationTypeSelect.options.data.results = [];
                        if (period.text.indexOf(APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL.name) > -1 ){
                            $scope.declarationTypeSelect.options.data.results.push(APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL);
                        }
                        if (period.text.indexOf(APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.name) > -1 ){
                            $scope.declarationTypeSelect.options.data.results.push(APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1);
                        }
                        if (period.text.indexOf(APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2.name) > -1 ){
                            $scope.declarationTypeSelect.options.data.results.push(APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2);
                        }
                    }
                }

                /**
                 * Определение доступности создания отчетной формы указанного типа по типу КНФ
                 */
                function isReportTypeAvailableByKnfType(declarationType, knfType) {
                    return knfType.id !== APP_CONSTANTS.KNF_TYPE.BY_NONHOLDING_TAX.id || declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2.id;
                }

                /**
                 * Событие возникающие при выборе подразделения и периода
                 */
                $scope.$on(APP_CONSTANTS.EVENTS.DEPARTMENT_AND_PERIOD_SELECTED, function (event, period, departmentId) {
                    $scope.initSelectWithReportDeclarationTypesForCreate(period, departmentId);
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
                 * @param reportPeriodModelPath путь к модели в scope
                 */
                var fillSelectListAndFindLatestPeriod = function (projection, reportPeriodModelPath) {
                    ReportPeriodResource.query({
                        projection: projection
                    }, function (data) {
                        $scope.periodSelect.options.data.results = data;
                        if (reportPeriodModelPath && data && data.length > 0) {
                            var defaultPeriod = data[0];
                            angular.forEach(data, function (period) {
                                if (Date.parse(defaultPeriod.endDate) < Date.parse(period.endDate)) {
                                    defaultPeriod = period;
                                }
                            });
                            _.deep($scope, reportPeriodModelPath, defaultPeriod);
                        }
                    });
                };

                /**
                 * Добавить в список все отчетные периоды и определить последний
                 */
                $scope.initSelectWithAllPeriods = function (reportPeriodModelPath) {
                    $scope.periodSelect = GetSelectOption.getBasicMultipleSelectOptions(true, 'periodFormatter');
                    fillSelectListAndFindLatestPeriod("all", reportPeriodModelPath);
                };

                /**
                 * Список периодов для формы 2-НДФЛ (ФЛ)
                 */
                $scope.initSelectWithAllPeriodsFor2NdflFL = function (reportPeriodModelPath) {
                    $scope.periodSelect = GetSelectOption.getBasicMultipleSelectOptions(true, 'periodFormatter');
                    fillSelectListAndFindLatestPeriod("for2NdflFL", reportPeriodModelPath);
                };

                /**
                 * Список периодов для формы 2-НДФЛ (ФЛ)
                 */
                $scope.initSingleSelectWithAllPeriodsFor2NdflFL = function (reportPeriodModelPath) {
                    $scope.periodSelect = GetSelectOption.getBasicSingleSelectOptions(true, true, 'periodFormatter');
                    fillSelectListAndFindLatestPeriod("for2NdflFL", reportPeriodModelPath);
                };

                /**
                 * @description Инициализировать выпадающий список пустым массивом
                 */
                $scope.initEmptySelect = function () {
                    $scope.periodSelect = GetSelectOption.getBasicSingleSelectOptions(true, true);
                    $scope.periodSelect.options.data.results = [];
                };

                /**
                 * Инициализировать выпадающий список периодами, коорые активны и открыты для определенного подразделения
                 *
                 * @param departmentModelPath путь в scope до модели подразделения
                 * @param modelPath путь до объекта в scope, содержащем значение периода, для установки его значения через _.deep
                 * @param allowClear добавлять ли крестик очистки
                 */
                $scope.initSelectWithOpenDepartmentPeriods = function (departmentModelPath, modelPath, allowClear) {
                    $scope.periodSelect = GetSelectOption.getBasicSingleSelectOptions(!!allowClear, true, 'periodFormatterWithCorrectionDate');
                    $scope.periodSelect.options.data = function () {
                        // select2 копирует results и его потом изменить уже нельзя, поэтому используем функцию, возвращяющую последние запрошенные данные
                        return angular.extend({results: $scope.results}, $scope.periodSelect.options.data);
                    };
                    var department = _.deep($scope, departmentModelPath);
                    loadPeriodsAndSetDefault(department, modelPath);
                    $scope.$watch(departmentModelPath, function (newValue, oldValue) {
                        if (newValue && (!oldValue || newValue.id !== oldValue.id)) {
                            loadPeriodsAndSetDefault(newValue, modelPath);
                        }
                    });
                };

                function loadPeriodsAndSetDefault(department, modelPath) {
                    if (department) {
                        ReportPeriodResource.query({
                            projection: "forDepartment",
                            departmentId: department.id
                        }, function (periods) {
                            $scope.results = periods;
                            if (periods && periods.length > 0) {
                                angular.forEach(periods, function (period) {
                                    // поиск работает по полю text
                                    period.text = $filter('periodFormatterWithCorrectionDate')(period);
                                });
                                // если выбранный период есть в списке, то оставляем, иначе установливаем самый последний
                                var currentValue = _.deep($scope, modelPath);
                                if (!currentValue || !_.find(periods, function (period) {
                                    return period.id === currentValue.id && (
                                        !period.correctionDate && !currentValue.correctionDate || period.correctionDate === currentValue.correctionDate);
                                })) {
                                    var defaultValue = periods[0];
                                    _.deep($scope, modelPath, defaultValue);
                                    angular.forEach(periods, function (period) {
                                        if (Date.parse(defaultValue.endDate) <= Date.parse(period.endDate)) {
                                            defaultValue = period;
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }])

        /**
         * Контроллер для выбора подразделений
         */
        .controller('SelectDepartmentCtrl', ['$scope', '$rootScope', 'GetSelectOption', 'APP_CONSTANTS', 'RefBookValuesResource', '$http',
            function ($scope, $rootScope, GetSelectOption, APP_CONSTANTS, RefBookValuesResource, $http) {
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
                 * @param departmentFilter дополнительные параметры запроса
                 * @param modelPath путь до объекта из ng-model, для установки его значения через _.deep, по сути передача параметра по ссылке
                 */
                $scope.initActiveDepartmentSelectWithOpenPeriod = function (departmentFilter, modelPath) {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(false, false, "controller/rest/refBookValues/30?projection=allByFilter",
                        {filter: departmentFilter}, {
                            property: "fullName",
                            direction: "asc"
                        }, "fullNameFormatter");

                    setDefaultValue();

                    // устанавливает первое подразделение из списка
                    function setDefaultValue() {
                        $http({
                            method: "GET",
                            url: $scope.departmentsSelect.options.ajax.url,
                            params: $scope.departmentsSelect.options.ajax.data()
                        }).then(function (response) {
                            if (response.data && response.data.rows) {
                                _.deep($scope, modelPath, response.data.rows[0]);
                            } else {
                                _.deep($scope, modelPath, null);
                            }
                        });
                    }

                    return setDefaultValue;
                };

                /**
                 * @description Инициализировать список с загрузкой действующих доступных ТБ
                 */
                $scope.initAvailableTBSelect = function (departmentModel, allowClear) {
                    $scope.departmentsSelect = GetSelectOption.getBasicSingleSelectOptions(!!allowClear, true);
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
                };
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
                    $scope.oktmoSelect = GetSelectOption.getAjaxSelectOptions(false, false, "controller/rest/refBookValues/96", {}, {
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
                var codeNameConfig = {
                    sort: {
                        property: "CODE",
                        direction: "asc"
                    },
                    filter: {
                        columns: ["NAME", "CODE"]
                    },
                    formatter: "codeNameFormatter"
                };
                // ОКТМО
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.OKTMO] = codeNameConfig;
                // Коды места представления расчета
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.PRESENT_PLACE] = codeNameConfig;
                // Признак лица, подписавшего документ
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.SIGNATORY_MARK] = codeNameConfig;
                // Коды форм реорганизации (ликвидации) организации
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.REORGANIZATION] = codeNameConfig;
                // Тип ДУЛ
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.DOC_TYPE] = codeNameConfig;
                // Страны
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.COUNTRY] = codeNameConfig;
                // Система-источник
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.ASNU] = codeNameConfig;
                // Признак кода вычета
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.DEDUCTION_MARK] = codeNameConfig;
                // Коды, определяющие налоговый (отчётный) период
                $scope.refBookConfig[APP_CONSTANTS.REFBOOK.PERIOD_CODE] = codeNameConfig;
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
                $scope.initSelect = function (refBookId, attributeAlias, filter, isMultiple) {
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
                        isMultiple = angular.isDefined(isMultiple) ? isMultiple : (
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

                $scope.initBasicSingleSelect = function (refBookId) {
                    $scope.select = GetSelectOption.getBasicSingleSelectOptions(false);
                    $http({
                        method: "GET",
                        url: "controller/rest/refBook/" + refBookId + "/records"
                    }).success(function (data) {
                        $scope.select.options.data.results = data.rows;
                    });
                };

                $scope.initBasicMultipleSelect = function (refBookId) {
                    $scope.select = GetSelectOption.getBasicMultipleSelectOptions(true);
                    $http({
                        method: "GET",
                        url: "controller/rest/refBook/" + refBookId + "/records"
                    }).success(function (data) {
                        $scope.select.options.data.results = data.rows;
                    });
                };
            }
        ])

        /**
         * Контроллер для выбора АСНУ
         */
        .controller('SelectFormKppCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', '$filter',
            function ($scope, APP_CONSTANTS, GetSelectOption, $filter) {
                $scope.initSelectKppByTB = function (filter) {
                    $scope.select = GetSelectOption.getAjaxSelectOptions(true, true, "controller/rest/departmentConfig/kppSelect",
                        filter, {}, 'kppSelectFormatter', "kpp");
                };

                $scope.initSelectKppByFormId = function (filter) {
                    $scope.select = GetSelectOption.getAjaxSelectOptions(true, true, "controller/rest/ndflPerson/kppSelect",
                        filter, {}, 'kppSelectFormatter', "kpp", "kppSelectResultFormatter");
                    $scope.select.options.placeholder = $filter('translate')('ndfl.report.ndfl2_6XlsxReport.modal.kpp.all');
                };
            }]
        )

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

                $scope.initBasicSingleSelectFromArray = function (values, allowClear) {
                    $scope.selectFromArray = GetSelectOption.getBasicSingleSelectOptions(!!allowClear);
                    $scope.selectFromArray.options.data.results = toArray(values);
                };

                $scope.initBasicMultipleSelectFromArray = function (values, allowClear) {
                    $scope.selectFromArray = GetSelectOption.getBasicMultipleSelectOptions(!!allowClear);
                    $scope.selectFromArray.options.data.results = toArray(values);
                };

                function toArray(values) {
                    var array = [];
                    if (angular.isArray(values)) {
                        array = values;
                    } else {
                        var keys = Object.keys(values);
                        for (var i in keys) {
                            var key = keys[i];
                            array.push(values[key]);
                        }
                    }
                    return array;
                }
            }
        ])

        /**
         * Контроллер для карточки реестра ФЛ
         */
        .controller('SelectRegistryPersonController', ['$scope', 'GetSelectOption', '$http', 'APP_CONSTANTS',
            'RefBookRecordResource', 'TaxPayerStateResource', 'RefBookAsnuResource', 'RefBookCountryResource',
            'RefBookDocTypeResource', 'RefBookValuesResource',
            function ($scope, GetSelectOption, $http, APP_CONSTANTS, RefBookRecordResource,
                      TaxPayerStateResource, RefBookAsnuResource, RefBookCountryResource, RefBookDocTypeResource,
                      RefBookValuesResource) {

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
                            return -1;
                        }
                        if (priority1 > priority2) {
                            return 1;
                        }
                        if (doc_code1 < doc_code2) {
                            return -1;
                        }
                        if (doc_code1 > doc_code2) {
                            return 1;
                        }
                        if (doc_number1 < doc_number2) {
                            return -1;
                        }
                        if (doc_number1 > doc_number2) {
                            return 1;
                        }
                        return 0;
                    });
                    $scope.selectedDocs.options.data.results = person.documents.value;
                };

                $scope.$on("addIdDoc", function (event, person) {
                    $scope.initIdDocs(person);
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

                $scope.initSingleAsnu = function (asnu) {
                    $scope.asnuSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [asnu], true, 'codeNameFormatter');
                    $scope.asnuSelect.options.data.results = [asnu];
                };

                /**
                 * Инициализировать выпадашку для установления важности
                 * @param person изменяемое ФЛ
                 */
                $scope.initVip = function (person) {
                    $scope.vipSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(false, [{
                        id: 1,
                        value: true,
                        name: 'VIP'
                    }, {id: 2, value: false, name: 'не VIP'}], false);
                    angular.forEach($scope.vipSelect.options.data.results, function (value) {
                        if (value.value === person.vip) {
                            person.vipSelect = value;
                        }
                    });
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
                };

                /**
                 * Инициализировать список ТБ
                 * @param presentedTbIdList
                 */
                $scope.loadTBs = function (presentedTbIdList) {
                    $scope.departmentsSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [], true, 'nameFormatter');
                    RefBookValuesResource.query({
                        refBookId: APP_CONSTANTS.REFBOOK.DEPARTMENT,
                        presentedTbIdList: presentedTbIdList,
                        projection: "activeTBExcluding"
                    }, function (availableTBs) {
                        $scope.departmentsSelect.options.data.results = availableTBs;
                    });
                };

                $scope.initSingleTb = function (tb) {
                    $scope.departmentsSelect = GetSelectOption.getBasicSingleSelectOptionsWithResults(true, [tb], true, 'codeNameFormatter');
                    $scope.departmentsSelect.options.data.results = [tb];
                };
            }]
        )

        /**
         * Bыборка пар КПП/ОКТМО
         */
        .controller('SelectKppOktmoPairsCtrl', ['$scope', 'GetSelectOption', 'RefBookValuesResource',
            function ($scope, GetSelectOption) {
                $scope.kppOkmtoPairsSelect = {};
                // если этого не сделать, то это приводит к неправильному отображению списка, если поле не заполнено
                // (при создании подразделения)
                if ($scope.mode !== 'CREATE' && $scope.record && !$scope.record.relatedKppOktmo.hasOwnProperty("kpp")) {
                    $scope.record.relatedKppOktmo = null;
                }

                /**
                 * Определение пар КПП/ОКТМО по параметрам Тербанк и период, если он задан
                 *
                 * @param modelPath путь в scope до модели со значением select2
                 * @param knf КНФ, из которой брать пары КПП/ОКТМО
                 * @param departmentModelPath путь в scope до модели подразделения
                 * @param periodModelPath путь в scope до модели периода
                 * @param endDatePath путь в scope до даты окончания действия настройки подразделения
                 */
                $scope.initSelectKppOktmoPairsByParams = function (modelPath, knf, departmentModelPath, periodModelPath, endDatePath) {
                    if (!knf) {
                        if (departmentModelPath && periodModelPath) {
                            $scope.$watchCollection("[" + departmentModelPath + "," + periodModelPath + "]", function (newValues, oldValues) {
                                // при изменении зависимых параметров "подразделение" и "период" сбрасываем значение
                                var department = newValues && newValues[0],
                                    oldDepartment = oldValues && oldValues[0];
                                var period = newValues && newValues[1],
                                    oldPeriod = oldValues && oldValues[1];

                                if (department && (!oldDepartment || department.id !== oldDepartment.id) ||
                                    period && (!oldPeriod || period.id !== oldPeriod.id)
                                ) {
                                    _.deep($scope, modelPath, []);
                                }
                            });
                        } else if (departmentModelPath && endDatePath) {
                            $scope.$watchCollection("[" + departmentModelPath + "," + endDatePath + "]", function (newValues, oldValues) {
                                // при изменении зависимых параметров "подразделение" и "дата окончания действия настройки подразделения"
                                // сбрасываем значение
                                var department = newValues && newValues[0],
                                    oldDepartment = oldValues && oldValues[0];
                                var endDate = newValues && new Date(newValues[1]).setHours(0, 0, 0, 0),
                                    oldEndDate = oldValues && new Date(oldValues[1]).setHours(0, 0, 0, 0);

                                if (department && (!oldDepartment || department.id !== oldDepartment.id) ||
                                    endDate && (!oldEndDate || endDate !== oldEndDate)
                                ) {
                                    _.deep($scope, modelPath, null);
                                }
                            });
                        }
                    }

                    $scope.kppOkmtoPairsSelect = getKppOktmoSelector(true);
                    $scope.kppOkmtoPairSelect = getKppOktmoSelector(false);

                    function getFilter() {
                        return {
                            filter: {
                                declarationId: knf && knf.id,
                                departmentId: knf ? knf.departmentId : (departmentModelPath ? _.deep($scope, departmentModelPath).id : null),
                                reportPeriodId: knf ? knf.reportPeriodId : (periodModelPath ? _.deep($scope, periodModelPath).id : null),
                                relevanceDate: endDatePath ? _.deep($scope, endDatePath) : null
                            }
                        };
                    }

                    function getKppOktmoSelector(isMultiple) {
                        return GetSelectOption.getAjaxSelectOptions(isMultiple, true, "controller/rest/departmentConfig/kppOktmoPairsSelect",
                            getFilter, {
                                property: "kpp, oktmo",
                                direction: "asc"
                                }, "kppOktmoPairFormatter");
                    }
                };
            }
        ])

        /**
         * Контроллер для выбора подсистемы АС УН
         */
        .controller('SelectSubsystemCtrl', ['$scope', '$rootScope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, $rootScope, APP_CONSTANTS, GetSelectOption) {
                $scope.subsystemSelect = GetSelectOption.getAjaxSelectOptions(false, true, "controller/rest/subsystems", {}, {});
            }
        ])

        /**
         * Выбор по объектам вида id: name
         */
        .controller('SelectByKeyValueCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', '$filter',
            function ($scope, APP_CONSTANTS, GetSelectOption) {

                $scope.initSingleSelect = function (map) {
                    $scope.select = GetSelectOption.getBasicSingleSelectOptions(true);
                    $scope.select.options.data.results = toArray(map);
                };

                $scope.initMultipleSelect = function (map) {
                    $scope.select = GetSelectOption.getBasicMultipleSelectOptions(true);
                    $scope.select.options.data.results = toArray(map);
                };

                function toArray(map) {
                    var array = [];
                    var keys = Object.keys(map);
                    for (var i in keys) {
                        var key = keys[i];
                        var value = map[key];
                        array.push({id: key, name: value});
                    }
                    return array;
                }
            }
        ])

        /**
         * Контроллер для выбор режима формирования отчета "РНУ НДФЛ по всем ФЛ"
         */
        .controller('SelectReportGenerationTypeCtrl', ['$scope', '$rootScope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, $rootScope, APP_CONSTANTS, GetSelectOption) {
                $scope.reportGenerationTypeSelect = {};

                /**
                 * Инициализировать список с видами форм, которые можно создать
                 */
                $scope.initSelectWithReportGenerationTypesForCreate = function (filterIsClear, selectedRows) {
                    $scope.reportGenerationTypeSelect = GetSelectOption.getBasicSingleSelectOptions(false);
                    $scope.reportGenerationTypeSelect.options.data.results = [];
                    $scope.reportGenerationTypeSelect.options.data.results.push(APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.ALL_DATA);
                    if (!filterIsClear) {
                        $scope.reportGenerationTypeSelect.options.data.results.push(APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.BY_FILTER_SELECTED);
                    }
                    if (selectedRows && selectedRows.length !== 0) {
                        $scope.reportGenerationTypeSelect.options.data.results.push(APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.SELECTED_ON_PAGE);
                    }

                };
            }
        ])

        /**
         * Контроллер для выбора типа КНФ по виду отчетности
         */
        .controller('SelectKnfTypeCtrlByReportPeriod', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var periodModelPath = 'declarationData.period';
                $scope.knfTypeSelect = {};

                /**
                 * Определение возможных типов КНФ по виду отчетности
                 *
                 * @param periodModelPath путь в scope до модели периода
                 */
                $scope.initSelectKnfTypeByReportPeriodTaxFormType = function() {
                    $scope.knfTypeSelect = GetSelectOption.getBasicSingleSelectOptions(false);
                    $scope.knfTypeSelect.options.data.results = getKnfTypesByPeriod();

                    $scope.$watch(periodModelPath, function (newValue, oldValue) {
                        if (!!newValue && (!oldValue || newValue.id !== oldValue.id)) {
                            var knfTypes = getKnfTypesByPeriod();
                            $scope.declarationData.knfType = knfTypes[0];
                            $scope.knfTypeSelect.options.data.results = knfTypes;
                        }
                    });
                }

                function getKnfTypesByPeriod() {
                    var period = _.deep($scope, periodModelPath);
                    switch(period.reportPeriodTaxFormTypeId) {
                        case APP_CONSTANTS.TAX_FORM_TYPE.REPORT_2_NDFL_1.id:
                        case APP_CONSTANTS.TAX_FORM_TYPE.REPORT_6_NDFL.id:
                            return [APP_CONSTANTS.KNF_TYPE.ALL, APP_CONSTANTS.KNF_TYPE.BY_KPP];
                        case APP_CONSTANTS.TAX_FORM_TYPE.REPORT_2_NDFL_2.id:
                            return [APP_CONSTANTS.KNF_TYPE.BY_NONHOLDING_TAX];
                        case APP_CONSTANTS.TAX_FORM_TYPE.APP_2.id:
                            return [APP_CONSTANTS.KNF_TYPE.FOR_APP2];
                    }
                }
            }
        ]);
}());
