/**
 * aplana-time-hint (подсказка о времени)
 * Директива предназначена для создания иконки с подсказкой о времени.
 * http://localhost:8080/#/aplana_time_hint
 */

angular.module('aplana.timeHint', [])

/**
 *  @name TimeHintService
 *
 *  Сервис для инициализации данных.
 */
    .service('TimeHintService', [function () {
        "use strict";
        this.init = function (scope, attrs) {
                //Текущее смещение относительно UTC
                var currentTimezoneOffsetInHour = -(new Date().getTimezoneOffset() / 60);

                var config = scope.$eval(attrs.aplanaTimeHintConfig);

                //Конфигурация по умолчанию
                if (!angular.isObject(config)) {
                    config = {
                        fromTimezone: 'Москвы',
                        timezone: 3,
                        timeOnTimezoneTitle: 'Москве'
                    };
                }

                if (!angular.isNumber(config.timezone)) {
                    config.timezone = 0;
                }
                //Смещение отностительно config.timezone
                var currentOffset = currentTimezoneOffsetInHour - config.timezone;

                scope.hint = angular.extend(config, {
                    currentOffsetTitle: 'Сдвиг:',
                    currentOffset: currentOffset,
                    fromTimezone: (currentOffset > 0 ? '+' : '') + currentOffset + ' ч. от ' + config.fromTimezone,
                    timeOnTimezoneTitle: 'Время по ' + config.timeOnTimezoneTitle + ':'
                });

                //Вычисление времени в указаной временной зоне
                var calcTime = function (offset, date) {
                    var d = angular.isDate(date) ? date : new Date(date);
                    var utc = d.getTime() + (d.getTimezoneOffset() * 60000);
                    return new Date(utc + (3600000 * offset));
                };

                scope.$watch(attrs.ngModel, function (newValue) {
                    if (newValue) {
                        scope.showHint = true;
                        scope.hint.timeOnTimezone = calcTime(scope.hint.timezone, newValue);
                    } else {
                        scope.showHint = false;
                    }
                });

        };
    }])

/**
 * Директива компонента подсказки о времени.
 */
    .directive('aplanaTimeHint', ['AplanaUtils', 'TimeHintService', function (AplanaUtils, TimeHintService) {
        "use strict";
        return {
            restrict: 'A',
            replace: true,
            scope: true,
            templateUrl: AplanaUtils.templatePath + 'timeHint/time-hint.html',
            compile: function (element, attrs) {
                element[0].removeAttribute('data-aplana-time-hint');

                return function (scope, element, attrs, controller) {
                    TimeHintService.init(scope, attrs);
                };
            }
        };
    }]
);
