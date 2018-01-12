/**
 * aplana-timepicker (Выбор времени)
 * Директива предназначена для создания поля выбора времени
 * http://localhost:8080/#/aplana_timepicker
 */

angular.module('aplana.timepicker', ['aplana.timeHint'])

/**
 * Сервис изменения времени у сущности datetime
 * @param ngModel - datetime котороую требуется отредактировать
 * @param newValue - datetime содержит новое время
 */
    .constant('timePickerModifyNgModel', function (ngModel, newValue) {
        "use strict";

        var modifyTime = function (dateTime, momentValue) {
            if (dateTime) {
                return moment(dateTime)
                    .hour(momentValue.hour())
                    .minute(momentValue.minute())
                    .second(momentValue.second())
                    .milliseconds(momentValue.milliseconds())
                    .toDate();
            } else {
                return dateTime;
            }
        };

        if (newValue) {
            var momentValue = moment(newValue);
            if (ngModel) {
                return modifyTime(ngModel, momentValue);
            } else {
                return momentValue.toDate();
            }
        } else {
            return undefined;
        }
    })

/**
 * angular-ui-bootstrap timepicker
 * http://angular-ui.github.io/bootstrap/

 * Version: 0.11.0 - 2014-05-01
 * License: MIT
 *
 * @YMakogon Логику компоненты не менял, поправил только шаблон
 */
    .constant('timepickerConfig', {
        hourStep: 1,
        minuteStep: 1,
        showMeridian: true,
        meridians: null,
        readonlyInput: false,
        mousewheel: true
    })

    .controller('TimepickerController', ['$scope', '$attrs', '$parse', '$log', '$locale', 'timepickerConfig', function ($scope, $attrs, $parse, $log, $locale, timepickerConfig) {
        var selected = new Date(),
            ngModelCtrl = {$setViewValue: angular.noop}, // nullModelCtrl
            meridians = angular.isDefined($attrs.meridians) ? $scope.$parent.$eval($attrs.meridians) : timepickerConfig.meridians || $locale.DATETIME_FORMATS.AMPMS;

        this.init = function (ngModelCtrl_, inputs) {
            ngModelCtrl = ngModelCtrl_;
            ngModelCtrl.$render = this.render;

            var hoursInputEl = inputs.eq(0),
                minutesInputEl = inputs.eq(1);

            var mousewheel = angular.isDefined($attrs.mousewheel) ? $scope.$parent.$eval($attrs.mousewheel) : timepickerConfig.mousewheel;
            if (mousewheel) {
                this.setupMousewheelEvents(hoursInputEl, minutesInputEl);
            }

            $scope.readonlyInput = angular.isDefined($attrs.readonlyInput) ? $scope.$parent.$eval($attrs.readonlyInput) : timepickerConfig.readonlyInput;
            this.setupInputEvents(hoursInputEl, minutesInputEl);
        };

        var hourStep = timepickerConfig.hourStep;
        if ($attrs.hourStep) {
            $scope.$parent.$watch($parse($attrs.hourStep), function (value) {
                hourStep = parseInt(value, 10);
            });
        }

        var minuteStep = timepickerConfig.minuteStep;
        if ($attrs.minuteStep) {
            $scope.$parent.$watch($parse($attrs.minuteStep), function (value) {
                minuteStep = parseInt(value, 10);
            });
        }

        // 12H / 24H mode
        $scope.showMeridian = timepickerConfig.showMeridian;
        if ($attrs.showMeridian) {
            $scope.$parent.$watch($parse($attrs.showMeridian), function (value) {
                $scope.showMeridian = !!value;

                if (ngModelCtrl.$error.time) {
                    // Evaluate from template
                    var hours = getHoursFromTemplate(), minutes = getMinutesFromTemplate();
                    if (angular.isDefined(hours) && angular.isDefined(minutes)) {
                        selected.setHours(hours);
                        refresh();
                    }
                } else {
                    updateTemplate();
                }
            });
        }

        // Get $scope.hours in 24H mode if valid
        function getHoursFromTemplate() {
            var hours = parseInt($scope.hours, 10);
            var valid = ( $scope.showMeridian ) ? (hours > 0 && hours < 13) : (hours >= 0 && hours < 24);
            if (!valid) {
                return undefined;
            }

            if ($scope.showMeridian) {
                if (hours === 12) {
                    hours = 0;
                }
                if ($scope.meridian === meridians[1]) {
                    hours = hours + 12;
                }
            }
            return hours;
        }

        function getMinutesFromTemplate() {
            var minutes = parseInt($scope.minutes, 10);
            return ( minutes >= 0 && minutes < 60 ) ? minutes : undefined;
        }

        function pad(value) {
            //return ( angular.isDefined(value) && value.toString().length < 2 ) ? '0' + value : value;
            return value;
        }

        // Respond on mousewheel spin
        this.setupMousewheelEvents = function (hoursInputEl, minutesInputEl) {
            var isScrollingUp = function (e) {
                if (e.originalEvent) {
                    e = e.originalEvent;
                }
                //pick correct delta variable depending on event
                var delta = (e.wheelDelta) ? e.wheelDelta : -e.deltaY;
                return (e.detail || delta > 0);
            };

            hoursInputEl.bind('mousewheel wheel', function (e) {
                $scope.$apply((isScrollingUp(e)) ? $scope.incrementHours() : $scope.decrementHours());
                e.preventDefault();
            });

            minutesInputEl.bind('mousewheel wheel', function (e) {
                $scope.$apply((isScrollingUp(e)) ? $scope.incrementMinutes() : $scope.decrementMinutes());
                e.preventDefault();
            });

        };

        this.setupInputEvents = function (hoursInputEl, minutesInputEl) {
            if ($scope.readonlyInput) {
                $scope.updateHours = angular.noop;
                $scope.updateMinutes = angular.noop;
                return;
            }

            var invalidate = function (invalidHours, invalidMinutes) {
                //ngModelCtrl.$setViewValue(null);
                ngModelCtrl.$setValidity('time', false);
                if (angular.isDefined(invalidHours)) {
                    $scope.invalidHours = invalidHours;
                }
                if (angular.isDefined(invalidMinutes)) {
                    $scope.invalidMinutes = invalidMinutes;
                }
            };

            $scope.updateHours = function () {
                var hours = getHoursFromTemplate();

                if (angular.isDefined(hours)) {
                    selected.setHours(hours);
                    refresh('h');
                } else {
                    invalidate(true);
                }
            };

            hoursInputEl.bind('blur', function (e) {
                if (!$scope.invalidHours && $scope.hours < 10) {
                    $scope.$apply(function () {
                        $scope.hours = pad($scope.hours);
                    });
                }
            });

            $scope.updateMinutes = function () {
                var minutes = getMinutesFromTemplate();

                if (angular.isDefined(minutes)) {
                    selected.setMinutes(minutes);
                    refresh('m');
                } else {
                    invalidate(undefined, true);
                }
            };

            minutesInputEl.bind('blur', function (e) {
                if (!$scope.invalidMinutes && $scope.minutes < 10) {
                    $scope.$apply(function () {
                        $scope.minutes = pad($scope.minutes);
                    });
                }
            });

        };

        this.render = function () {
            var date = ngModelCtrl.$modelValue ? new Date(ngModelCtrl.$modelValue) : null;

            if (isNaN(date)) {
                ngModelCtrl.$setValidity('time', false);
                $log.error('Timepicker directive: "ng-model" value must be a Date object, a number of milliseconds since 01.01.1970 or a string representing an RFC2822 or ISO 8601 date.');
            } else {
                if (date) {
                    selected = date;
                }
                makeValid();
                updateTemplate();
            }
        };

        // Call internally when we know that model is valid.
        function refresh(keyboardChange) {
            makeValid();
            ngModelCtrl.$setViewValue(new Date(selected));
            updateTemplate(keyboardChange);
        }

        function makeValid() {
            ngModelCtrl.$setValidity('time', true);
            $scope.invalidHours = false;
            $scope.invalidMinutes = false;
        }

        function updateTemplate(keyboardChange) {
            var hours = selected.getHours(), minutes = selected.getMinutes();

            if ($scope.showMeridian) {
                hours = ( hours === 0 || hours === 12 ) ? 12 : hours % 12; // Convert 24 to 12 hour system
            }

            $scope.hours = keyboardChange === 'h' ? hours : pad(hours);
            $scope.minutes = keyboardChange === 'm' ? minutes : pad(minutes);
            $scope.meridian = selected.getHours() < 12 ? meridians[0] : meridians[1];
        }

        function addMinutes(minutes) {
            var dt = new Date(selected.getTime() + minutes * 60000);
            selected.setHours(dt.getHours(), dt.getMinutes());
            refresh();
        }

        $scope.incrementHours = function () {
            addMinutes(hourStep * 60);
        };
        $scope.decrementHours = function () {
            addMinutes(-hourStep * 60);
        };
        $scope.incrementMinutes = function () {
            addMinutes(minuteStep);
        };
        $scope.decrementMinutes = function () {
            addMinutes(-minuteStep);
        };
        $scope.toggleMeridian = function () {
            addMinutes(12 * 60 * (( selected.getHours() < 12 ) ? 1 : -1));
        };

    }])

    .directive('uiTimepicker', ['AplanaUtils', function (AplanaUtils) {
        return {
            restrict: 'EA',
            require: ['uiTimepicker', '?^ngModel'],
            controller: 'TimepickerController',
            replace: true,
            scope: {},
            templateUrl: AplanaUtils.templatePath + 'timepicker/bootstrap-timepicker.html',
            link: function (scope, element, attrs, ctrls) {
                var timepickerCtrl = ctrls[0], ngModelCtrl = ctrls[1];

                if (ngModelCtrl) {
                    timepickerCtrl.init(ngModelCtrl, element.find('input'));
                }
            }
        };
    }])

/**
 *    @author <a href="mailto:ymakogon@aplana.com">Yuriy Makogon</a>
 */
    .directive('aplanaTimeInput', ['timePickerModifyNgModel', function (timePickerModifyNgModel) {
        "use strict";
        return {
            require: ['ngModel', '?^form'],
            scope: {
                modelValue: '=ngModel'
            },
            restrict: 'A',
            link: function (scope, element, attrs, controllers) {
                //вычленяем контроллеры модели и формы
                scope.ngModelCtrl = controllers[0];
                scope.ngFormCtrl = controllers[1];

                var format = "HH:mm";

                var formatFn = function (modelValue) {
                    if (modelValue) {
                        return moment(modelValue).format(format);
                    }

                    return null;
                };

                function parseTime(viewValue) {
                    if (!viewValue) {
                        return null;
                    } else if (angular.isDate(viewValue) && !isNaN(viewValue)) {
                        return viewValue;
                    } else if (angular.isString(viewValue)) {
                        var result = viewValue;

                        var momentValue = moment(viewValue, format, true);
                        if (momentValue.isValid()) {
                            result = momentValue.toDate();
                        } else {
                            result = moment().toDate();
                        }

                        return timePickerModifyNgModel(scope.ngModelCtrl.$modelValue, result);
                    } else {
                        return undefined;
                    }
                }

                scope.ngModelCtrl.$parsers.unshift(parseTime);
                scope.ngModelCtrl.$formatters.unshift(formatFn);

                scope.ngModelCtrl.$render = function ngModelRender() {
                    var date = scope.ngModelCtrl.$modelValue ? formatFn(scope.ngModelCtrl.$modelValue) : '';
                    scope.ngModelCtrl.$setViewValue(date);
                    element.val(date);
                };


                element.bind('focusout', scope.ngModelCtrl.$render);

                scope.ngModelCtrl.$modelValue = scope.modelValue;

                scope.ngModelCtrl.$render();
                scope.ngModelCtrl.$setPristine();
            }
        };
    }])

/**
 * Директива компонента выбора времени.
 */
    .directive('aplanaTimepicker', ['AplanaUtils', 'TimeHintService', function (AplanaUtils, TimeHintService) {
        return {
            restrict: 'A',
            replace: true,
            scope: true,
            templateUrl: AplanaUtils.templatePath + 'timepicker/timepicker.html',
            compile: function (element, attrs) {
                element[0].removeAttribute('data-aplana-timepicker');
                var inputElement = element.find('input');
                var buttonElement = element.find('button');
                var timeElement = angular.element('<div ui-timepicker show-meridian="false" minute-step="15"></div>');

                var ngModelAttr = attrs.ngModel || AplanaUtils.findNgModel(element[0]);

                AplanaUtils.copyAttributes(element, timeElement, 'data-ng-model');
                AplanaUtils.copyAttributes(element, timeElement, 'minute-step');
                element[0].removeAttribute('minute-step');

                AplanaUtils.copyAttributes(element, buttonElement, ['data-placement', 'data-container']);

                element[0].removeAttribute('data-placement');
                element[0].removeAttribute('data-container');

                // Сформируем id и name для элемента
                var modelId = AplanaUtils.buildModelId(ngModelAttr);
                inputElement.attr('name', modelId);
                inputElement.attr('id', modelId);

                AplanaUtils.moveAttributes(element, inputElement);

                inputElement.attr("data-aplana-time-input", "");

                buttonElement.attr("data-aplana-popover", "{content: '" + timeElement[0].outerHTML + "'}");

                return function (scope, element, attrs, controller) {
                    TimeHintService.init(scope, attrs);
                };
            }
        };
    }])
;
