/**
 * aplana-actualization-select
 * Выпадашка для работы со справочниками наделенными признаком/периодом актуальности. По сути просто обертка над
 * ui-select2, которая подготавливает для него параметры
 * Параметры
 * scope.selectOptions - обычные параметры выпадашки за исключением параметров ответственых за выборку данных.
 * scope.actualizationOptions = {
 *      type: "", //тип справочника(attribute - с признаком актуальнсти; period - с периодом актуальности)
 *      baseUrl: "", //url для ajax запросов данных
 *      requestOptions: function (term), //пользовательские параметры запроса
 *      searchField: "" //поле фильтрации
 *  };
 * actualDate: "", //дата, на которую выбираются актуальные значения
 * actualStartDate: "", //дата начала периода, на который выбираются актуальные значения
 * actualEndDate: "", //дата конца периода, на который выбираются актуальные значения
 * oldValue: "", //старое значение выпадашки
 */
(function () {
    'use strict';
    angular.module("aplana.actualization.select", ['aplana.utils'])
        .directive('aplanaActualizationSelect', ['AplanaUtils', function (AplanaUtils) {
            return {
                restrict: 'A',
                scope: {
                    selectOptions: '=',
                    actualizationOptions: '=',
                    actualDate: '=',
                    actualStartDate: '=',
                    actualEndDate: '=',
                    oldValue: '=',
                    modelValue: '=ngModel'
                },
                replace: true,
                require: 'ngModel',
                templateUrl: AplanaUtils.templatePath + 'select2/select2.html',
                compile: function (element, attrs) {
                    return {
                        pre: function (scope, element, attrs, ngModel) {
                            /**
                             * Проверяет актуальность значения
                             * @param value значение
                             * @returns {boolean} факт актуальности
                             */
                            function checkActual(value) {
                                var actual = true;
                                if (!!value) {
                                    if (Array.isArray(value)) {
                                        for(var i = 0; i < value.length; ++i) {
                                            if (!checkActual(value[i])) {
                                                return false;
                                            }
                                        }
                                        return actual;
                                    }

                                    if (scope.actualizationOptions.type === 'attribute') {
                                        actual = !!value.actual;
                                    } else if (scope.actualizationOptions.type === 'period') {
                                        if ((getActualStartDate() == null) && (getActualEndDate() == null)) {
                                            actual = true;
                                        } else if (getActualStartDate() == null) {
                                            actual = AplanaUtils.parseAndFloorDate(value.startDate) <= getActualEndDate();
                                        } else if (getActualEndDate() == null) {
                                            actual = !value.endDate || (AplanaUtils.parseAndFloorDate(value.endDate) >= getActualStartDate());
                                        } else {
                                            actual = (AplanaUtils.parseAndFloorDate(value.startDate) <= getActualEndDate()) &&
                                                (!value.endDate || (AplanaUtils.parseAndFloorDate(value.endDate) >= getActualStartDate()));
                                        }
                                    } else if (scope.actualizationOptions.type === 'date') {
                                        actual = angular.isDefined(value.startDate) &&
                                            (AplanaUtils.parseAndFloorDate(value.startDate) <= getActualDate()) &&
                                            ((!value.endDate) || (AplanaUtils.parseAndFloorDate(value.endDate) >= getActualDate()));
                                    }
                                }

                                return actual;
                            }

                            function getActualDate() {
                                var actualDate = getParameterValue(scope.actualDate);
                                return actualDate ? AplanaUtils.parseAndFloorDate(actualDate) : AplanaUtils.today();
                            }

                            function getActualStartDate() {
                                var actualStartDate = getParameterValue(scope.actualStartDate);
                                return actualStartDate ? AplanaUtils.parseAndFloorDate(actualStartDate) : null;
                            }

                            function getActualEndDate() {
                                var actualEndDate = getParameterValue(scope.actualEndDate);
                                return actualEndDate ? AplanaUtils.parseAndFloorDate(actualEndDate) : null;
                            }

                            /**
                             * Метод для получения значения параметра-значения и параметра-функции
                             * @param parameter параметр
                             * @returns {*} значение параметра
                             */
                            function getParameterValue(parameter) {
                                return angular.isFunction(parameter) ? parameter() : parameter;
                            }

                            /**
                             * Валидатор актуальности
                             * @param value значение
                             * @returns {*} обработанное значение
                             */
                            function actualValidator(value) {
                                var valid = checkActual(value);
                                ngModel.$setValidity('actual', valid);
                                if (!valid) {
                                    ngModel.$dirty = true;
                                }
                                return value;
                            }

                            if ((scope.actualizationOptions.type !== 'attribute') &&
                                (scope.actualizationOptions.type !== 'period') &&
                                (scope.actualizationOptions.type !== 'date')) {

                                throw new Error('Значение поля actualizationOptions.type должно быть в диапазоне ["attribute", "period", "date"]');
                            }

                            scope.preparedOptions = angular.extend({}, scope.selectOptions, {
                                formatResultCssClass: function (data) {
                                    return checkActual(data) ? '' : 'aplanaOutdatedReference';
                                },
                                ajax: {
                                    url: scope.actualizationOptions.baseUrl,
                                    quietMillis: 100,
                                    data: function (term, page) {
                                        var requestOptions = {};
                                        if (scope.actualizationOptions.type === 'attribute') {
                                            requestOptions.onlyActual = true;
                                        } else if ((scope.actualizationOptions.type === 'date')) {
                                            requestOptions.actualDate = AplanaUtils.formatDateForBinder(getActualDate());
                                        } else if ((scope.actualizationOptions.type === 'period')) {
                                            if (angular.isDate(getActualStartDate())) {
                                                requestOptions.actualStartDate = AplanaUtils.formatDateForBinder(getActualStartDate());
                                            }
                                            if (angular.isDate(getActualEndDate())) {
                                                requestOptions.actualEndDate = AplanaUtils.formatDateForBinder(getActualEndDate());
                                            }
                                        }

                                        if (angular.isFunction(scope.actualizationOptions.requestOptions)) {
                                            angular.extend(requestOptions, scope.actualizationOptions.requestOptions(term));
                                        }

                                        angular.extend(requestOptions, {
                                            term: term,
                                            rows: 10,
                                            page: page
                                        });

                                        if (!!getParameterValue(scope.oldValue)) {
                                            requestOptions.oldValue = getParameterValue(scope.oldValue);
                                        }

                                        return requestOptions;
                                    },
                                    results: function (data, page) {
                                        var more = page < data.total;
                                        return {results: data.rows, more: more};
                                    }
                                }
                            });

                            scope.$watch(function () {
                                return getParameterValue(scope.actualDate);
                            }, function () {
                                actualValidator(ngModel.$viewValue);
                            }, true);

                            scope.$watch(function () {
                                return getParameterValue(scope.actualStartDate);
                            }, function () {
                                actualValidator(ngModel.$viewValue);
                            }, true);

                            scope.$watch(function () {
                                return getParameterValue(scope.actualEndDate);
                            }, function () {
                                actualValidator(ngModel.$viewValue);
                            }, true);

                            ngModel.$parsers.push(actualValidator);
                            ngModel.$formatters.unshift(actualValidator);
                        }
                    };
                }
            };
        }]);
}());