/**
 * aplana-input-float (Дробный ввод)
 * Директива <input> c атрибутом type='float' предназначена для создания поля ввода вещественных чисел.
 * http://localhost:8080/#/aplana_input_float
 */
(function () {
    'use strict';

    angular.module('aplana.inputFloat', [])
        .directive('input', ['$filter', function ($filter) {

            function isEmpty(value) {
                return angular.isUndefined(value) || value === '' || value === null || value !== value;
            }

            return {
                restrict: 'E',
                require: 'ngModel',
                link: function (scope, element, attr, ctrl) {
                    var keyPressed = 0;

                    if (attr.type !== 'float') {
                        return;
                    }

                    var fractionSize = parseInt(attr.fractionSize, 10) || 0;

                    var NUMBER_REGEXP = new RegExp("^\\s*((\\-|\\+)?|(\\-|\\+)?(\\d+|(\\d*([,.]\\d{0," + fractionSize + "}))))\\s*$");

                    /**
                     * Если value - строка - преобразует в число. Если есть запятая - заменит на точку
                     */
                    function stringToFloat(value) {
                        if (angular.isString(value)) {
                            value = (value === '' ? null : (isEmpty(value) ? value : stringIsNumber(value) ? (stringIsSign(value) ? value : parseFloat(value.replace(',', '.'))) : value));
                        }
                        return value;
                    }

                    function maskToEdit(value) {
                        var valueWithoutSpaces = value.replace(/\s/g, '').replace(',', '.');
                        valueWithoutSpaces = parseFloat(valueWithoutSpaces);
                        if (!isNaN(valueWithoutSpaces)) {
                            return valueWithoutSpaces;
                        }
                        return value;
                    }

                    /**
                     *  Если в значении число - отформатируем его фильтром 'number' в соответствии с настройками локали
                     */
                    function maskValue(value) {
                        if (angular.isNumber(value)) {
                            value = $filter('number')(value.toFixed(fractionSize), fractionSize);
                        }
                        return value ? value : "";
                    }

                    /**
                     *  Если в значении число - отформатируем его c учетом fractionSize
                     */
                    function fractionSizeFormatter(value) {
                        if (angular.isNumber(value)) {
                            return Number(value.toFixed(fractionSize));
                        } else {
                            return value;
                        }
                    }

                    /**
                     *  Проверяет строку на соответствие шаблону числа
                     */
                    function stringIsNumber(value) {
                        return NUMBER_REGEXP.test(value);
                    }

                    /**
                     *  Проверяет строку на соответствие шаблону +/-
                     */
                    function stringIsSign(value) {
                        return value === "-" || value === "+";
                    }

                    /**
                     *  Валидирует строку value. Если value не похоже на число - выдает ошибку валидатора.
                     */
                    function validateStringAsNumber(value) {
                        if (keyPressed === 9 || keyPressed === 13) {
                            return value.replace(/\s/g, '');
                        } else {
                            var empty = isEmpty(value);
                            if (empty || stringIsNumber(value)) {
                                ctrl.$setValidity('number', true);
                            } else {
                                ctrl.$setValidity('number', false);
                            }
                            return value;
                        }
                    }

                    /**
                     *  Валидирует value на пустоту или Number
                     */
                    function validateNumber(value) {
                        if (isEmpty(value) || angular.isNumber(value)) {
                            ctrl.$setValidity('number', true);
                            return value;
                        } else {
                            ctrl.$setValidity('number', false);
                            return value;
                        }
                    }

                    /**
                     *  Валидирует строку value. Если value не похоже на число - выдает ошибку валидатора.
                     */
                    function checkIsNumber(value) {
                        if (angular.isNumber(value)) {
                            return value;
                        } else {
                            return null;
                        }
                    }

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

                    // Обработчики событий
                    element.bind('keydown', function (event) {
                        keyPressed = event.keyCode;
                    });
                    element.bind('focusout', function (event) {
                        var element = this;
                        //При очень быстром переключении, возможна следующая ситуация: событие focusout для поля срабатывает быстрее, чем валидаторы
                        //для того, чтобы это не происходило устанавливаем timer c задержкой 0
                        setTimeout(function () {
                            element.value = maskValue(stringToFloat(element.value));
                        }, 0);
                    });
                    element.bind('paste', function (event) {
                        var elementChange = this;
                        setTimeout(function () {
                            elementChange.value = maskToEdit(elementChange.value);
                            scope.$apply(function () {
                                ctrl.$setViewValue(elementChange.value);
                            });
                        }, 0);
                    });
                    element.bind('focus', function (event) {
                        if (angular.isDefined(ctrl.$modelValue)) {
                            if (angular.isNumber(ctrl.$modelValue)) {
                                this.value = ctrl.$modelValue.toFixed(fractionSize);
                            } else {
                                this.value = ctrl.$modelValue ? ctrl.$modelValue : '';
                            }
                            setCursor(this);
                        }
                    });

                    function firstFormatter(value) {
                        return value;
                    }

                    function lastFormatter(value) {
                        return value;
                    }


                    // В новом Angular для всех input автоматом вводится приведение к строке в первом форматтере. Очистим форматеры, чтобы работать с чистым значением из модели
                    ctrl.$formatters =[];
                    // Последовательность форматирования значения для отображения в элементе. unshift - потому что порядок вызова "с конца"
                    ctrl.$formatters.unshift(stringToFloat);
                    ctrl.$formatters.unshift(validateNumber);
                    ctrl.$formatters.unshift(fractionSizeFormatter);
                    ctrl.$formatters.unshift(maskValue);

                    // Последовательность парсинга значения из элемента для передачи в модель. push - потому что порядок вызова "с начала"
                    ctrl.$parsers.push(validateStringAsNumber);
                    ctrl.$parsers.push(stringToFloat);
                    ctrl.$parsers.push(fractionSizeFormatter);
                    ctrl.$parsers.push(checkIsNumber);

                    // Валидаторы
                    // Минимальное значение
                    if (attr.min) {
                        var min = parseFloat(attr.min);
                        var minValidator = function (value) {
                            if (!isEmpty(value) && value < min) {
                                ctrl.$setValidity('min', false);
                                return value;
                            } else {
                                ctrl.$setValidity('min', true);
                                return value;
                            }
                        };
                        ctrl.$parsers.push(minValidator);
                        ctrl.$formatters.push(minValidator);
                    }

                    // Максимальное значение
                    if (attr.max) {
                        var max = parseFloat(attr.max);
                        var maxValidator = function (value) {
                            if (!isEmpty(value) && value > max) {
                                ctrl.$setValidity('max', false);
                                return value;
                            } else {
                                ctrl.$setValidity('max', true);
                                return value;
                            }
                        };
                        ctrl.$parsers.push(maxValidator);
                        ctrl.$formatters.push(maxValidator);
                    }


                }
            };
        }]);
}());
