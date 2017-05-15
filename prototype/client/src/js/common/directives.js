(function () {
    'use strict';

    angular.module('aplana.sbrfNdfl.directives', [])
        /**
         * Проверка наличия прав у пользователя на доступ к компоненту
         */
        .directive('ngCheckOperation', ['USER_DATA', '$animate', 'aplanaEntityUtils', function (USER_DATA, $animate, aplanaEntityUtils) {
            return {
                restrict: 'A',
                multiElement: true,
                link: function (scope, element, attr) {
                    scope.$watch(attr.ngCheckOperation, function (value) {
                        //Проверяем доступность операции по переданному значению
                        var availability = aplanaEntityUtils.checkOperation(value);
                        $animate[availability ? 'removeClass' : 'addClass'](element, 'ng-hide', {
                            tempClasses: 'ng-hide-animate'
                        });
                    });
                }
            }
        }])

        /**
         * Исправляет баг в datepicker, когда берется локальная дата клиента вместо даты UTC
         */
        .directive('datepickerTimezone', function () {
            return {
                restrict: 'A',
                priority: 1,
                require: 'ngModel',
                link: function (scope, element, attrs, ctrl) {
                    ctrl.$formatters.push(function (value) {
                        if (!value) {
                            return null;
                        }
                        var date = new Date(value);
                        date = new Date(date.getTime() + (60000 * date.getTimezoneOffset()));
                        return date;
                    });

                    ctrl.$parsers.push(function (value) {
                        if (value == null) {
                            return null;
                        }
                        var date = new Date(value.getTime() - (60000 * value.getTimezoneOffset()));
                        return date;
                    });
                }
            };
        })

        /**
         * Проверяет введенное значение на соответствие типу integer
         */
        .directive('integer', function () {
            return {
                require: 'ngModel',
                link: function (scope, elm, attrs, ngModel) {
                    ngModel.$validators.integer = function (modelValue, viewValue) {
                        if (ngModel.$isEmpty(modelValue)) {
                            // consider empty models to be valid
                            return true;
                        }
                        var INTEGER_REGEXP = /^\-?\d+$/;
                        if (INTEGER_REGEXP.test(viewValue)) {
                            // it is valid
                            return true;
                        }

                        // it is invalid
                        return false;
                    };
                }
            };
        })
        //Кастомная проверка минимального значения числа. Отличается тем, что ее можно навешивать на input не только типа number, но и например text
        .directive('ngMin', function () {
            return {
                restrict: 'A',
                require: 'ngModel',
                link: function (scope, elem, attr, ctrl) {
                    scope.$watch(attr.ngMin, function () {
                        ctrl.$setViewValue(ctrl.$viewValue);
                    });
                    scope.isEmpty = function(value) {
                        return angular.isUndefined(value) || value === '' || value === null || value !== value
                    };
                    scope.minValidator = function (value) {
                        var min = attr.ngMin || 0;
                        if (!scope.isEmpty(value) && value < min) {
                            ctrl.$setValidity('ngMin', false);
                            return undefined;
                        } else {
                            ctrl.$setValidity('ngMin', true);
                            return value;
                        }
                    };

                    ctrl.$parsers.push(scope.minValidator);
                    ctrl.$formatters.push(scope.minValidator);
                }
            };
        })
        //Кастомная проверка максимального значения числа. Отличается тем, что ее можно навешивать на input не только типа number, но и например text
        .directive('ngMax', function () {
            return {
                restrict: 'A',
                require: 'ngModel',
                link: function (scope, elem, attr, ctrl) {
                    scope.$watch(attr.ngMax, function () {
                        ctrl.$setViewValue(ctrl.$viewValue);
                    });
                    scope.isEmpty = function(value) {
                        return angular.isUndefined(value) || value === '' || value === null || value !== value
                    };
                    scope.maxValidator = function (value) {
                        var max = attr.ngMax || Infinity;
                        if (!scope.isEmpty(value) && value > max) {
                            ctrl.$setValidity('ngMax', false);
                            return undefined;
                        } else {
                            ctrl.$setValidity('ngMax', true);
                            return value;
                        }
                    };

                    ctrl.$parsers.push(scope.maxValidator);
                    ctrl.$formatters.push(scope.maxValidator);
                }
            };
        })

        //Проверка записи на заблоикрованность в справочнике
        .directive('delete', function () {
            return {
                require: 'ngModel',
                link: function (scope, elem, attrs, ngModel) {
                    ngModel.$validators.delete = function (modelValue, viewValue) {
                        if (ngModel.$isEmpty(modelValue)) {
                            // consider empty models to be valid
                            return true;
                        }

                        if (viewValue){
                            return !viewValue.deleted;
                        }

                        ////ngModel.$setViewValue(ngModel.$viewValue);
                        return false;
                    };
                }
            };
        })
}());