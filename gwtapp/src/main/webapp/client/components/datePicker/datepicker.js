/**
 * aplana-datepicker (Выбор даты)
 * Директива предназначена для выбора даты
 * http://localhost:8080/#/aplana_datepicker
 */
(function () {
    'use strict';

    /**
     * Директива - обертка, предназначенная для формирования поля datepicker c кнопкой и обернутым дивом
     */
    angular.module('aplana.datepicker', ['aplana.utils', 'mgcrea.ngStrap.helpers.dateParser', 'aplana.datetimepicker', 'aplana.popover'])
        .directive('aplanaDatepicker', ['AplanaUtils', function (AplanaUtils) {
            return {
                restrict: 'A',
                replace: true,
                transclude: true,
                scope: true,
                templateUrl: AplanaUtils.templatePath + 'datePicker/datepicker.html',
                compile: function (element, attrs) {
                    element[0].removeAttribute('data-aplana-datepicker');
                    var inputElement = element.find('input');
                    var buttonElement = element.find('button');
                    var dateTimeElement = angular.element('<div datetimepicker></div>');
                    var ngModelAttr = attrs.ngModel || AplanaUtils.findNgModel(element[0]);

                    AplanaUtils.copyAttributes(element, dateTimeElement, 'data-ng-model');
                    AplanaUtils.copyAttributes(element, dateTimeElement, 'data-datetimepicker-config');
                    AplanaUtils.copyAttributes(element, dateTimeElement, 'data-min-date');
                    AplanaUtils.copyAttributes(element, dateTimeElement, 'data-max-date');
                    AplanaUtils.copyAttributes(element, dateTimeElement, 'data-focus-date');
                    AplanaUtils.copyAttributes(element, dateTimeElement, 'data-enable-weekends');
                    AplanaUtils.copyAttributes(element, dateTimeElement, 'data-date-format');
                    AplanaUtils.copyAttributes(element, buttonElement, ['data-placement', 'data-container', 'data-ng-disabled']);

                    element[0].removeAttribute('data-placement');
                    element[0].removeAttribute('data-container');
                    element[0].removeAttribute('data-datetimepicker-config');

                    // Сформируем id и name для элемента
                    var modelId = AplanaUtils.buildModelId(ngModelAttr);
                    inputElement.attr('name', modelId);
                    inputElement.attr('id', modelId);

                    AplanaUtils.moveAttributes(element, inputElement);
                    //element.addClass('input-append');

                    if (attrs.aplanaDatepicker === "dateWithSaveTime") {
                        attrs.aplanaDatepicker = "date";
                    } else {
                        inputElement.attr('data-aplana-date-without-time', '');
                    }

                    inputElement.attr('data-aplana-date-input', attrs.aplanaDatepicker);

                    dateTimeElement.attr("data-datetimepicker-config", "{ startView: \\'day\\', minView: \\'day\\' }");
                    buttonElement.attr("data-aplana-popover", "{content: '" + dateTimeElement[0].outerHTML + "'}");
                }
            };
        }])

        .directive('aplanaDateInput', ['dateFilter', '$dateParser',
            function (dateFilter, $dateParser) {
                var isNumeric = function (n) {
                    return !isNaN(parseFloat(n)) && isFinite(n);
                };
                return {
                    require: 'ngModel',
                    restrict: 'A',
                    link: function (scope, element, attr, controller) {
                        scope.options = {
                            lang: 'ru',
                            dateType: 'string',
                            dateFormat: angular.isDefined(attr.dateFormat) ? attr.dateFormat : 'dd.MM.yyyy',
                            modelDateFormat: 'yyyy-MM-dd',
                            minDate: -Infinity,
                            maxDate: +Infinity
                        };
                        scope.mask = scope.options.dateFormat.replace(/[dmy]/gi, '9');

                        scope.options.roundTime = attr.aplanaDateWithoutTime ? true : false;

                        if (attr.aplanaDateInput !== "") {
                            scope.options.dateType = attr.aplanaDateInput;
                        }

                        // Инициализация парсера
                        var dateParser = $dateParser({
                            format: scope.options.dateFormat,
                            lang: scope.options.lang
                        });

                        // Смотрим на изменения атрибутов
                        // На будущее: требуется доработка и тестирование minDate и maxDate
                        angular.forEach([
                            'minDate',
                            'maxDate'
                        ], function (key) {
                            //console.warn('attr.$observe(%s)', key, attr[key]);
                            angular.isDefined(attr[key]) && attr.$observe(key, function (newValue) {
                                newValue = scope.$eval(newValue);
                                //console.warn('attr.$observe(%s)=%o', key, newValue);
                                if (!angular.isDefined(newValue)) {
                                    scope.options[key] = key === 'maxDate' ? scope.options.maxDate : scope.options.minDate;
                                } else if (newValue === 'today') {
                                    var today = new Date();
                                    scope.options[key] = new Date(today.getFullYear(), today.getMonth(), today.getDate() + (key === 'maxDate' ? 1 : 0), 0, 0, 0, key === 'minDate' ? 0 : -1);
                                } else if (angular.isString(newValue) && newValue.match(/^".+"$/)) {
                                    // Support {{ dateObj }}
                                    scope.options[key] = new Date(newValue.substr(1, newValue.length - 2));
                                } else if (isNumeric(newValue)) {
                                    scope.options[key] = new Date(parseInt(newValue, 10));
                                } else {
                                    scope.options[key] = dateParser.parse(newValue, null, scope.options.modelDateFormat);//new Date(newValue);
                                }
                            });
                        });

                        // viewValue -> $parsers -> modelValue
                        // Внимание! Парсеры обрабатываются с конца массива парсеров $parsers
                        // Данный парсер сработает вторым, он нужен для обработки ситуации с не корректным вводом даты
                        // Устанавливает дату равной текущей,
                        controller.$parsers.unshift(function (viewValue) {
                            if (controller.$error.date) {

                                controller.$setValidity('date', true);

                                var today;
                                if (angular.isDate(controller.$dateValue)) {
                                    today = controller.$dateValue;
                                } else {
                                    today = null;
                                    controller.$setViewValue(null);
                                }

                                var isMinValid = today == null || isNaN(scope.options.minDate) || today.getTime() >= scope.options.minDate;
                                var isMaxValid = today == null || isNaN(scope.options.maxDate) || today.getTime() <= scope.options.maxDate;

                                if (!isMinValid) {
                                    controller.$dateValue = new Date(scope.options.minDate);
                                } else if (!isMaxValid) {
                                    controller.$dateValue = new Date(scope.options.maxDate);
                                } else {
                                    controller.$dateValue = today;
                                }

                                if (!controller.$dateValue) {
                                    return null;
                                } else if (scope.options.dateType === 'string') {
                                    return dateFilter(controller.$dateValue, scope.options.modelDateFormat || scope.options.dateFormat);
                                } else if (scope.options.dateType === 'number') {
                                    return controller.$dateValue.getTime();
                                } else if (scope.options.dateType === 'iso') {
                                    return controller.$dateValue.toISOString();
                                } else {
                                    return new Date(controller.$dateValue);
                                }

                            }

                            return viewValue;
                        });
                        // viewValue -> $parsers -> modelValue
                        // Данный парсер сработает первым
                        controller.$parsers.unshift(function (viewValue) {
                            //console.warn('$parser("%s"): viewValue=%o', element.attr('data-ng-model'), viewValue);
                            // Корректная обработка Null значения, сбрасываем валидацию и значение
                            if (!viewValue) {
                                controller.$setValidity('date', true);
                                return null;
                            }

                            var parsedDate = dateParser.parse(viewValue, controller.$dateValue);
                            if (!parsedDate || isNaN(parsedDate.getTime())) {
                                controller.$setValidity('date', false);
                                return null;
                            } else {
                                var isMinValid = isNaN(scope.options.minDate) || parsedDate.getTime() >= scope.options.minDate;
                                var isMaxValid = isNaN(scope.options.maxDate) || parsedDate.getTime() <= scope.options.maxDate;
                                var isValid = isMinValid && isMaxValid;
                                controller.$setValidity('date', isValid);
                                controller.$setValidity('min', isMinValid);
                                controller.$setValidity('max', isMaxValid);
                                // Если все проверки пройдены обновим модель
                                // if (isValid) {
                                controller.$dateValue = parsedDate;
                                // }
                            }
                            if (scope.options.dateType === 'string') {
                                return dateFilter(parsedDate, scope.options.modelDateFormat || scope.options.dateFormat);
                            } else if (scope.options.dateType === 'number') {
                                return controller.$dateValue.getTime();
                            } else if (scope.options.dateType === 'iso') {
                                return controller.$dateValue.toISOString();
                            } else {
                                return new Date(controller.$dateValue);
                            }
                        });

                        // modelValue -> $formatters -> viewValue
                        controller.$formatters.push(function (modelValue) {
                            //console.warn('$formatter("%s"): modelValue=%o (%o)', element.attr('data-ng-model'), modelValue, typeof modelValue);
                            var date;
                            if (angular.isUndefined(modelValue) || modelValue === null) {
                                date = NaN;
                            } else if (angular.isDate(modelValue)) {
                                date = modelValue;
                            } else if (scope.options.dateType === 'string') {
                                date = dateParser.parse(modelValue, null, scope.options.modelDateFormat);
                            } else {
                                date = new Date(modelValue);
                            }

                            controller.$dateValue = date;
                            return controller.$dateValue;
                        });

                        controller.getRenderValue = function(value) {
                            return !value || isNaN(value.getTime()) ? '' : dateFilter(value, scope.options.dateFormat);
                        };

                        // viewValue -> element
                        controller.$render = function () {
                            if (!element.is(':focus')) {
                                element.val(!controller.$dateValue || isNaN(controller.$dateValue.getTime()) ? '' : dateFilter(controller.$dateValue, scope.options.dateFormat));
                            }
                        };

                        //Функция которую вызывает календарик
                        scope.onSetTime = function (value) {
                            controller.$setDirty();

                            if (scope.$onSelectValue) {
                                scope.$onSelectValue(value);
                            }
                        };

                        element.bind('focusout', function () {
                            //Корректно обрабатываем ситуацию когда поле ввода пусто
                            if (!controller.$viewValue) {
                                controller.$dateValue = undefined;
                            }
                            controller.$render();
                        });

                        var PRISTINE_CLASS = 'ng-pristine',
                            DIRTY_CLASS = 'ng-dirty';
                        var parentForm = element.inheritedData('$formController');
                        if (parentForm) {
                            controller.$setDirty = function () {
                                if (this.$pristine) {
                                    this.$dirty = true;
                                    this.$pristine = false;
                                    element.removeClass(PRISTINE_CLASS).addClass(DIRTY_CLASS);
                                    parentForm.$setDirty();
                                }
                            };
                        }
                    }
                };
            }])

        /*------------------------*/
        .factory('HolidaysFactory', ['$http', '$q', function ($http, $q) {
            var holydays = [];
            var request;
            return {
                getHoliday: function() {
                    if(holydays.length !== 0){
                        $q.when(holydays);
                    }else{
                        if(!request){
                            request = $http.get('rest/holidays')
                                .then(function(response) {
                                    if (typeof response.data === 'object') {
                                        holydays = response.data;
                                        return holydays;
                                    }
                                });
                        }
                    }
                    return request;
                }
            };
        }])

        /**
         * Директива предназначена для установки времени на начало дня
         */
        .directive('aplanaDateWithoutTime', ['$parse', '$exceptionHandler', 'dateFilter',
            function ($parse, $exceptionHandler, dateFilter) {
                return {
                    require: 'ngModel',
                    restrict: 'A',
                    link: function (scope, element, attr, controller) {

                        var ngModelGet = $parse(attr.ngModel),
                            ngModelSet = ngModelGet.assign;

                        controller.$setViewValueNotDirty = function (viewValue) {
                            var value = dateFilter(viewValue, scope.mask);

                            angular.forEach(this.$parsers, function (fn) {
                                value = fn(value);
                            });

                            if (this.$modelValue !== value) {
                                this.$modelValue = value;
                                ngModelSet(scope, value);
                                angular.forEach(this.$viewChangeListeners, function (listener) {
                                    try {
                                        listener();
                                    } catch (e) {
                                        $exceptionHandler(e);
                                    }
                                });
                            }
                        };

                        // Этот форматер выставляет время на начало дня
                        controller.$formatters.push(function (modelValue) {
                            if (!isNaN(modelValue) && angular.isDate(modelValue)) {

                                var correctDate = new Date(modelValue.getFullYear(), modelValue.getMonth(), modelValue.getDate(), 0, 0, 0, 0);

                                controller.$setViewValueNotDirty(correctDate);
                                controller.$render();
                                modelValue = correctDate;

                            }
                            return modelValue;
                        });

                        controller.$parsers.unshift(function (viewValue) {
                            if (!isNaN(viewValue) && angular.isDate(viewValue)) {

                                controller.$dateValue = new Date(viewValue.getFullYear(), viewValue.getMonth(), viewValue.getDate(), 0, 0, 0, 0);

                                if (scope.options.dateType === 'string') {
                                    return dateFilter(controller.$dateValue, scope.options.modelDateFormat || scope.options.dateFormat);
                                } else if (scope.options.dateType === 'number') {
                                    return controller.$dateValue.getTime();
                                } else if (scope.options.dateType === 'iso') {
                                    return controller.$dateValue.toISOString();
                                } else {
                                    return new Date(controller.$dateValue);
                                }
                            }
                            return viewValue;

                        });
                    }
                };
            }]);


}());
