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
                        allowClear: allowClear,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };
            };

            /**
             * Получить настройки выпадающего списка с постоянным набором элементов. Задано все, кроме data.results
             * @param isMultiple Выбор множественный
             * @param allowClear Возможна ли очистка
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             */
            this.getBasicSelectOptions = function (isMultiple, allowClear, formatter) {
                if (formatter === undefined) {
                    formatter = 'nameFormatter';
                }
                var select = buildSelectOptionsTemplate(isMultiple, allowClear, formatter);
                select.options.data = {text: $filter(formatter)};
                return select;
            };

            /**
             * Получить настройки выпадающего списка с постоянным набором элементов
             * @param isMultiple Выбор множественный
             * @param allowClear Возможна ли очистка
             * @param results Элементы списка
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             */
            this.getBasicSelectOptionsWithResults = function (isMultiple, allowClear, results, formatter) {
                var select = this.getBasicSelectOptions(isMultiple, allowClear, formatter);
                select.options.data.results = results;
                return select;
            };

            /**
             * Получить настройки выпадающего списка, элементы которого загружаются по AJAX
             * @param isMultiple Выбор множественный
             * @param allowClear Возможна ли очистка
             * @param url URL, по которому запрашиваются элементы списка
             * @param dataFilter Объект с параметрами для фильтрации данных
             * @param formatter Фильтр, получающий из сущности текст, который выводится в списке и используется для поиска. По умолчанию nameFormatter
             * @param searchField Поле, по которому выполняется поиск. Значение по умолчанию: name
             */
            this.getAjaxSelectOptions = function (isMultiple, allowClear, url, dataFilter, formatter, searchField) {
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
                        var dataObject = {pagingParams: JSON.stringify({count: 50, page: page})};
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
                $scope.correctionTagSelect = GetSelectOption.getBasicSelectOptionsWithResults(false, false, correctionTags);
            }])

        /**
         * Контроллер для выбора типа формы
         */
        .controller('SelectDeclarationKindCtrl', ['$scope', '$rootScope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, $rootScope, APP_CONSTANTS, GetSelectOption) {
                var declarationKinds = [];
                if ($rootScope.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_CONTROL_NS) || $rootScope.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_CONTROL_UNP)) {
                    declarationKinds = [APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY, APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED];
                } else if ($rootScope.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_OPER)) {
                    declarationKinds = [APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY];
                }

                $scope.declarationKindSelect = GetSelectOption.getBasicSelectOptionsWithResults(true, true, declarationKinds);
            }])

        /**
         * Контроллер для выбора состояния налоговой
         */
        .controller('SelectDeclarationStateCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption',
            function ($scope, APP_CONSTANTS, GetSelectOption) {
                var declarationStates = [APP_CONSTANTS.STATE.CREATED, APP_CONSTANTS.STATE.PREPARED, APP_CONSTANTS.STATE.ACCEPTED];
                $scope.stateSelect = GetSelectOption.getBasicSelectOptionsWithResults(false, true, declarationStates);
            }])

        /**
         * Контроллер для выбора АСНУ
         */
        .controller('SelectAsnuCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', 'RefBookValuesResource',
            function ($scope, APP_CONSTANTS, GetSelectOption, RefBookValuesResource) {
                $scope.asnuSelect = GetSelectOption.getBasicSelectOptions(true, true);
                RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.ASNU}, function (data) {
                    $scope.asnuSelect.options.data.results = data;
                });
            }])

        /**
         * Контроллер для выбора вида формы
         */
        .controller('SelectDeclarationTypeCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', 'RefBookValuesResource', 'DeclarationTypeForCreateResource',
            function ($scope, APP_CONSTANTS, GetSelectOption, RefBookValuesResource, DeclarationTypeForCreateResource) {
                $scope.declarationTypeSelect = GetSelectOption.getBasicSelectOptions(false, true);

                /**
                 * Инициализировать список со всеми видами форм
                 */
                $scope.initSelectWithAllDeclarationTypes = function () {
                    RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.DECLARATION_TYPE}, function (data) {
                        $scope.declarationTypeSelect.options.data.results = data;
                    });
                };

                /**
                 * Инициализировать список с видами форм, которые можно создать
                 * @param declarationKind Тип налоговой формы
                 * @param periodObject Выражение из scope, по которому отслеживается изменение периода
                 * @param departmentObject Выражение из scope, по которому отслеживается изменение подразделения
                 */
                $scope.initSelectWithDeclarationTypesForCreate = function (declarationKind, periodObject, departmentObject) {
                    //Список обновляется при изменении отчетного периода и подразделения
                    $scope.$watchGroup([periodObject, departmentObject], function (newValues) {
                        var period = newValues[0];
                        var department = newValues[1];
                        if (declarationKind && period && department) {
                            DeclarationTypeForCreateResource.query({
                                declarationKind: declarationKind.id,
                                departmentId: department.id,
                                periodId: period.id
                            }, function (data) {
                                $scope.declarationTypeSelect.options.data.results = data;
                            });
                        }
                    });
                };
            }])

        /**
         * Контроллер для выбора периода
         */
        .controller('SelectPeriodCtrl', ['$scope', 'APP_CONSTANTS', 'GetSelectOption', 'RefBookValuesResource',
            function ($scope, APP_CONSTANTS, GetSelectOption, RefBookValuesResource) {
                $scope.periodSelect = {};

                /**
                 * Заполнить список, загрузив данные из указанной проекции, и определить последний период
                 * @param projection Проекция
                 * @param latestPeriod Последний период
                 */
                var fillSelectListAndFindLatestPeriod = function (projection, latestPeriod) {
                    RefBookValuesResource.query({
                        refBookId: APP_CONSTANTS.REFBOOK.PERIOD,
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
                    $scope.periodSelect = GetSelectOption.getBasicSelectOptions(true, true, 'periodFormatter');
                    fillSelectListAndFindLatestPeriod("allPeriods", latestPeriod);
                };

                /**
                 * Добавить в список открытые отчетные периоды и определить последний
                 */
                $scope.initSelectWithOpenPeriods = function (latestPeriod) {
                    $scope.periodSelect = GetSelectOption.getBasicSelectOptions(false, true, 'periodFormatter');
                    fillSelectListAndFindLatestPeriod("openPeriods", latestPeriod);
                };
            }])

        /**
         * Контроллер для выбора подразделений
         */
        .controller('SelectDepartmentCtrl', ['$scope', 'GetSelectOption',
            function ($scope, GetSelectOption) {
                $scope.departmentsSelect = {};

                /**
                 * Инициализировать список с загрузкой всех подразделений через ajax
                 */
                $scope.initSelectWithAllDepartments = function () {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(true, true, "controller/rest/refBookValues/30?projection=allDepartments");
                };

                /**
                 * Инициализировать список с загрузкой подразделений с открытым периодом через ajax
                 * @param periodObject Выражение из scope, по которому отслеживается изменение периода
                 */
                $scope.initDepartmentSelectWithOpenPeriod = function (periodObject) {
                    $scope.departmentsSelect = GetSelectOption.getAjaxSelectOptions(false, true, "controller/rest/refBookValues/30?projection=departmentsWithOpenPeriod");
                    $scope.$watch(periodObject, function (period) {
                        if (period) {
                            $scope.departmentsSelect.options.dataFilter = {reportPeriodId: period.id};
                        }
                    });
                };
            }])
    ;
}());
