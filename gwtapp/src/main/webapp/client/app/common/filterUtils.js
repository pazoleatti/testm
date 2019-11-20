(function () {
        "use strict";
        /**
         * @description Вспомогательные утилиты для фильтров в журналах
         */
        angular.module('app.filterUtils', [])
        /**
         * @description Стандартный контроллер фильтра.
         *
         * Родительский контроллер журнала должен иметь следующий код:         *
         * $scope.refreshGrid = function(page) {
                    $scope.referencesGrid.ctrl.refreshGrid(page);
                };         *
         * $scope.searchFilter = {};
         *
         * В конфиге грида:
         * ...
         * requestParameters: function () {
             *               return {
             *                   filter: $scope.searchFilter.ajaxFilter
             *               };
             *           },
         * ...
         */
            .factory('CommonFilterUtils', [
                function() {
                    var service = {
                        isEmpty: function(object) {
                            return Object.keys(object).every(function (key) {
                                var filterCondition = object[key];
                                var isFilterConditionEmpty = !filterCondition.operator
                                    || !filterCondition.operator.unary && !filterCondition.argument2;
                                return !filterCondition
                                    || filterCondition.condition && isFilterConditionEmpty
                                    || angular.isObject(filterCondition) && isEmpty(filterCondition);
                            });
                        }
                    };
                    return service;
                }
            ])

            .controller('CommonFilterCtrl', ['$scope', '$webStorage', '$rootScope',
                function ($scope, $webStorage, $rootScope) {
                    // Триггер показа инфозаписей "Сброс"
                    $scope.searchFilter.isClear = false;

                    // Параметры фильтры (модельные поля для name, code и т.д.)
                    if (!$scope.searchFilter.params) {
                        $scope.searchFilter.params = {};
                    }

                    // Операции для работы с хранилищем для фильтра
                    $scope.filterStorage = {
                        // Сохраняет параметры фильтра в webStorage
                        save: function () {
                            if ($scope.filterStorage.isNeedStore()) {
                                $webStorage.set('filterStorage', $scope.searchFilter.filterName, $scope.searchFilter.params, true);
                            }
                        },
                        // Записывает на форму данные из webStorage
                        restore: function () {
                            if ($scope.filterStorage.isNeedStore()) {
                                $scope.searchFilter.params = $webStorage.get('filterStorage', $scope.searchFilter.filterName, $scope.searchFilter.params, true);
                            }
                        },
                        // Использовать или нет webStorage
                        isNeedStore: function () {
                            return !!$scope.searchFilter.filterName;
                        },
                        // Если данные в webStorage по данному фильтру
                        isEmpty: function () {
                            if ($scope.filterStorage.isNeedStore()) {
                                var valueFromStorage = $webStorage.get('filterStorage', $scope.searchFilter.filterName, $scope.searchFilter.params, true);
                                return _.isEmpty(valueFromStorage);
                            } else {
                                return true;
                            }
                        },
                        // Очищает webStorage для текущего фильтра
                        clear: function () {
                            if ($scope.filterStorage.isNeedStore()) {
                                $webStorage.remove('filterStorage', $scope.searchFilter.filterName, true);
                            }
                        }
                    };

                    // Если следует выводить надпись "сброс", то isClear становится true
                    //noinspection JSValidateJSDoc
                    /**
                     * @param searchFilter.isClearByFilterParams
                     */
                    $scope.updateIsClear = function () {
                        if ($scope.searchFilter.isClearByFilterParams) {
                            $scope.searchFilter.isClearByFilterParams();
                        } else {
                            $scope.searchFilter.isClear = !_.isEmpty($scope.searchFilter.ajaxFilter);
                        }
                    };

                    // Если следует выводить надпись "сброс", то isClear становится true
                    $scope.updateHideExtended = function () {
                        if (angular.isUndefined($scope.searchFilter.hideExtendedFilter)) {
                            $scope.searchFilter.hideExtendedFilter = !$scope.searchFilter.isClear;
                        }
                    };

                    $scope.fillAjaxFilter = function () {
                        $scope.searchFilter.ajaxFilter = [];

                        // собираем фильтр
                        if ($scope.searchFilter.fillFilterParams) {
                            // Кастомный код заполнения фильтра
                            $scope.searchFilter.fillFilterParams();
                        }
                    };

                    /* Функция заполняет фильтр поиска значениями полей */
                    $scope.fillSearchFilter = function () {
                        // Заполняем ajaxFilter из params
                        $scope.fillAjaxFilter();

                        // Если следует выводить надпись "сброс", то isClear становится true
                        $scope.updateIsClear();

                        // Сохраняем фильтр в хранилище
                        $scope.filterStorage.save();

                        // Обновляем грид
                        $scope.refreshGrid(1);
                    };

                    // Выполнение поиска
                    $scope.submitSearch = function () {
                        $scope.fillSearchFilter();

                        if($scope.searchFilter.onSearchSubmit) {
                            $scope.searchFilter.onSearchSubmit();
                        }
                    };

                    // Сброс фильтра
                    $scope.resetFilter = function () {
                        // Очищаем фильтр
                        $scope.searchFilter.ajaxFilter = [];
                        $scope.searchFilter.params = {};

                        // Убираем надпись "Сброс"
                        $scope.searchFilter.isClear = false;

                        // Кастомный код очистки фильтра
                        //noinspection JSValidateJSDoc
                        /**
                         * @param searchFilter.resetFilterParams
                         */
                        if ($scope.searchFilter.resetFilterParams) {
                            $scope.searchFilter.resetFilterParams();
                        }

                        $scope.fillSearchFilter();
                    };

                    // Установка значений по умолчанию
                    if ($scope.searchFilter.initFilter) {
                        $scope.searchFilter.initFilter();
                    }

                    // Установка значений в фильтр
                    if (!$scope.filterStorage.isEmpty()) {
                        $scope.filterStorage.restore();
                    }

                    $scope.fillAjaxFilter();

                    $scope.updateIsClear();
                    $scope.updateHideExtended();

                    if($scope.searchFilter.onCreateComplete) {
                        $scope.searchFilter.onCreateComplete();
                    }
                }
            ])
        ;
    }()
)
;

