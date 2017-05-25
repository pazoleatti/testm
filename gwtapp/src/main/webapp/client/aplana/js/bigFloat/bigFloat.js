/**
 * aplana-big-float (input для больших вещественных значений)
 * Директива aplana-big-float служит для отображения вещественных значений в котороых больше 15 цифр
 * http://localhost:8080/#/aplana_alert
 */
(function () {
    'use strict';
    angular.module("aplana.bigFloat", ['aplana.utils'])
        //форматтер для чисел, умеет работать и с числами и со строками, которые содержат числа
        .filter('bigFloatFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (angular.isDefined(value) && angular.isNumber(value)) {
                    return $filter("number")(value);
                } else if (!!value && $.isNumeric(value)) {
                    var integerPart = value.split(".")[0] || "";
                    var fractionalPart = value.split(".")[1] || "";

                    var processedPart = "";
                    //обрабатываем целую часть с конца и вставляем пробел после каждого третьего символа
                    for (var idx = 0; idx < integerPart.length; idx++) {
                        var processedIdx = integerPart.length - idx - 1;
                        if ((idx !== 0) && (idx % 3 === 0)) {
                            processedPart = " " + processedPart;
                        }
                        processedPart = integerPart[processedIdx] + processedPart;
                    }
                    integerPart = processedPart;

                    return !!fractionalPart ? integerPart + "." + fractionalPart : integerPart;
                }

                return !!value ? value : "";
            };
        }])
        .directive('aplanaBigFloat', ['$filter', function ($filter) {
            return {
                restrict: 'A',
                require: 'ngModel',
                link: function (scope, element, attr, ctrl) {
                    element.bind('focusout', function (event) {
                        //При очень быстром переключении, возможна следующая ситуация: событие focusout для поля срабатывает быстрее, чем валидаторы
                        //для того, чтобы это не происходило устанавливаем timer c задержкой 0
                        setTimeout(function () {
                            ctrl.$setViewValue($filter('bigFloatFormatter')(formatField(ctrl.$viewValue)));
                            ctrl.$render();
                        }, 0);
                    });
                    element.bind('focus', function (event) {
                        if (angular.isDefined(ctrl.$modelValue)) {
                            ctrl.$setViewValue(ctrl.$modelValue ? ctrl.$modelValue : '');
                        } else {
                            ctrl.$setViewValue(this.value.replace(/\s/g, ''));
                        }
                        ctrl.$render();
                        setCursor(this);
                    });

                    /**
                     * При установке значения в Input по событию focus, IE8 сдвигает каретку в начало строки.
                     * Функция служит для установки каретки в конец.
                     */
                    function setCursor(elem) {
                        // Проверка на IE8
                        if (navigator.userAgent.toLowerCase().indexOf('msie') !== -1) {
                            var rng = elem.createTextRange();
                            rng.moveEnd('textedit');
                            rng.moveStart('textedit');
                            rng.select();
                            rng.scrollIntoView();
                        }
                        return false;
                    }

                    function formatField(value) {
                        if (angular.isUndefined(value) || value == null) {
                            return null;
                        }

                        if (angular.isNumber(value)) {
                            return value.toString();
                        }

                        if (value !== "0") { // Строка задана и не равна "0"
                            value = value.replace(/\s/g, '') // Удаляет текстовые символы
                                         .replace(',', '.'); // Заменяем запятую на точку

                            if (value.indexOf('0') === 0) {
                                value = value.replace(/^[0]+/g, "");  // Удаляем любое число нулей из начала строки
                                if (value === '') {//На случай ввода нескольких 0
                                    value = '0';
                                }
                            }

                            if (value.indexOf('.') === 0) { // Если строка начинается с точки - поставим перед ней 0
                                value = '0' + value;
                            }
                        }

                        return value;
                    }

                    function validMax(value) {
                        if (angular.isDefined(value) && value != null && attr.max) {

                            if (angular.isNumber(value)) {
                                value = value.toString();
                            }

                            var valueList = value.split(".");

                            if (!angular.isUndefined(valueList[0]) && parseInt(valueList[0], 10) > parseInt(attr.max, 10)) {
                                return false;
                            } else {
                                if (!angular.isUndefined(valueList[0]) && parseInt(valueList[0], 10) === parseInt(attr.max, 10)) {
                                    if (!angular.isUndefined(valueList[1]) && parseInt(valueList[1], 10) > 0) {
                                        return false;
                                    } else {
                                        return true;
                                    }
                                } else {
                                    return true;
                                }
                            }
                        }

                        return value;
                    }

                    function validMin(value) {
                        if (angular.isDefined(value) && value != null && attr.min) {

                            if (angular.isNumber(value)) {
                                value = value.toString();
                            }

                            var valueList = value.split(".");
                            if (!angular.isUndefined(valueList[0]) && parseInt(valueList[0], 10) < parseInt(attr.min, 10)) {
                                return false;
                            } else {
                                return true;
                            }
                        }

                        return true;
                    }

                    ctrl.$parsers.push(formatField);

                    ctrl.$validators.max = validMax;
                    ctrl.$validators.min = validMin;

                    ctrl.$formatters.unshift($filter('bigFloatFormatter'));
                }
            };
        }]);
}());

