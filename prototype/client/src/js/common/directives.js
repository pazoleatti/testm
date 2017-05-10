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
         * Обработка скролла выпадашки, трубется для загрузки содержимого выпадашки порциями. Необходимо для фикса тормозов в IE, если в выпадашке несколько сотен строк
         * в атрибуте on-scroll-complete указывается функция для загрузки следующей порции данных
         */
        .directive('onScrollComplete',  ['$timeout', function ($timeout) {
            return function (scope, elm, attr) {
                var raw = elm[0];
                var delay = false;

                elm.bind('scroll', function (event) {
                    var percent = Math.abs((raw.scrollTop / (raw.offsetHeight - raw.scrollHeight)) * 100);
                    if (!delay || delay && percent >= 100) {
                        delay = true;
                        $timeout(function () {
                            delay = false
                        }, 100);
                        if (percent > attr.scrollPercent) {
                            scope.$apply(attr.onScrollComplete)
                        }
                    }
                });
            };
        }])

        /**
         * Обработка закрытия выпадающего списка
         * в атрибуте on-select-close указывается имя события, которое нужно передать в верхние scope
         */
        .directive('onSelectClose',  ['$timeout', function ($timeout) {
            return function (scope, elm, attr) {
                var eventName = attr.onSelectClose;

                scope.$on('uis:close', function(event, data) {
                    scope.$emit(eventName);
                })
            };
        }])

        /**
         * Исправляет баг в стандартном компоненте Angular ui-select:
         * При указании свойства multiply не работает валидация этого поля (например data-ng-required="true")
         */
        .directive('requireMultiple', function () {
            return {
                require: 'ngModel',
                link: function postLink(scope, element, attrs, ngModel) {
                    ngModel.$validators.required = function (value) {
                        return angular.isArray(value) && value.length > 0;
                    };
                }
            };
        })

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

        //Исправляет баг: для mu-textarea не работает проверка на обязательность
        .directive('requiredTextarea', function () {
            return {
                require: 'ngModel',
                link: function postLink(scope, element, attrs, ngModel) {
                    ngModel.$validators.requiredTextarea = function (value) {
                        return value.length === 0;
                    };
                }
            };
        })
        //Запрещает/разрешает редактирование вложенных input'ов с классом ui-select-search в зависимости от значения атрибута
        .directive('ngDisableSearch', function () {
            return {
                restrict: 'A',
                multiElement: true,
                link: function (scope, element, attr) {
                    scope.$watch(attr.ngDisableSearch, function (value) {
                        var input = element[0].querySelector("input.ui-select-search");
                        if (input) {
                            if (value) {
                                input.setAttribute("disabled", "disabled");
                            } else {
                                input.removeAttribute("disabled");
                            }
                        }
                    })
                }
            }
        });
}());