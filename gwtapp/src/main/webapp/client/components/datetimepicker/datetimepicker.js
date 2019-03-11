/**
 * Директивы:
 * 1) datetimepicker (Календарик)
 * Актуален и используется в aplana-datepicker
 *
 * 2) aplana-datetimepicker (Выбор даты и времени)
 * @deprecated НЕ АКТУАЛЕН И НЕ ИСПОЛЗУЕТСЯ
 *
 * Директива предназначена для создания поля выбора даты и времени
 * http://localhost:8080/#/aplana_datetimepicker
 */

// Если хранить в отдельном файле, то положение popover`а неправильное при первом открытии
angular.module("aplana/templates/datetimepicker/bootstrap-datetimepicker.html", []).run(["$templateCache", function($templateCache) {
    $templateCache.put("aplana/templates/datetimepicker/bootstrap-datetimepicker.html",
        "<div style=\"padding: 0px\" class='datetimepicker' data-ng-init=\"init()\">\n" +
        "    <table class='table-condensed'>\n" +
        "        <thead>\n" +
        "        <tr>\n" +
        "            <th class='left' data-ng-click=\"changeView(data.currentView, data.leftDate, $event)\">\n" +
        "                <i class='icon-chevron-left'/>\n" +
        "            </th>\n" +
        "            <th class='switch' colspan='5' data-ng-click=\"changeView(data.previousView, data.currentDate, $event)\">\n" +
        "                {{ data.title }}\n" +
        "            </th>\n" +
        "            <th class='right' data-ng-click=\"changeView(data.currentView, data.rightDate, $event)\">\n" +
        "                <i class='icon-chevron-right'/>\n" +
        "            </th>\n" +
        "        </tr>\n" +
        "        <tr>\n" +
        "            <th class='dow' data-ng-repeat='day in data.dayNames'>{{ day }}</th>\n" +
        "        </tr>\n" +
        "        </thead>\n" +
        "        <tbody>\n" +
        "        <tr data-ng-class='{ hide: data.currentView == \"day\" }'>\n" +
        "            <td colspan='7'>\n" +
        "                          <span class='{{ data.currentView }}'\n" +
        "                                data-ng-repeat='dateValue in data.dates'\n" +
        "                                data-ng-class='{active: dateValue.active,\n" +
        "                                past: dateValue.past,\n" +
        "                                future: dateValue.future,\n" +
        "                                now: dateValue.now,\n" +
        "                                disabled: dateValue.disabled,\n" +
        "                                weekends: dateValue.weekends,\n" +
        "                                isHolidays: dateValue.isHolidays}'\n" +
        "                                data-ng-click=\"changeView(data.nextView, dateValue.date, $event)\">\n" +
        "                              {{ dateValue.display }}\n" +
        "                          </span>\n" +
        "            </td>\n" +
        "        </tr>\n" +
        "        <tr data-ng-show='data.currentView == \"day\"' data-ng-repeat='week in data.weeks'>\n" +
        "            <td data-ng-repeat='dateValue in week.dates'\n" +
        "                data-ng-click=\"changeView(data.nextView, dateValue.date, $event)\"\n" +
        "                class='day'\n" +
        "                data-ng-class='{active: dateValue.active,\n" +
        "                 past: dateValue.past,\n" +
        "                 future: dateValue.future,\n" +
        "                 now: dateValue.now,\n" +
        "                 disabled: dateValue.disabled,\n" +
        "                 weekends: dateValue.weekends,\n" +
        "                 isHolidays: dateValue.isHolidays}'>\n" +
        "            {{ dateValue.display }}\n" +
        "            </td>\n" +
        "        </tr>\n" +
        "        </tbody>\n" +
        "    </table>\n" +
        "</div>");
}]);

angular.module('aplana.datetimepicker', ['aplana/templates/datetimepicker/bootstrap-datetimepicker.html'])

/**
 * Сервис изменения даты у сущности datetime
 * @param ngModel - datetime котороую требуется отредактировать
 * @param newValue - datetime содержит новую дату
 */
    .constant('dateTimePickerModifyNgModel', function (ngModel, newValue) {
        "use strict";

        var modifyDate = function (dateTime, momentValue) {
            if (dateTime) {
                return moment(dateTime)
                    .year(momentValue.year())
                    .month(momentValue.month())
                    .date(momentValue.date())
                    .toDate();
            } else {
                return dateTime;
            }
        };


        if (newValue) {
            var momentValue = moment(newValue);
            if (ngModel) {
                return modifyDate(ngModel, momentValue);
            } else {
                return modifyDate(moment(), momentValue);
            }

        } else {
            return undefined;
        }

    })
    /**
     * @license angular-bootstrap-datetimepicker  v0.2.1
     * (c) 2013 Knight Rider Consulting, Inc. http://www.knightrider.com
     * License: MIT
     */

    /**
     *    @author        Dale "Ducky" Lotts
     *    @since        2013-Jul-8
     *
     *    @edited by <a href="mailto:ymakogon@aplana.com">Yuriy Makogon</a>
     */

    .value('dateTimePickerConfig', {
        startView: 'day',
        minView: 'minute',
        minuteStep: 5,
        dropdownSelector: null,
        utc: false,
        withSaveTime: true,
        weekStart: 1, //0 - Sunday, 1 - Monday
        enableWeekends: false
    })
    .constant('dateTimePickerConfigValidation', function (configuration) {
        "use strict";

        var validOptions = ['startView', 'minView', 'minuteStep', 'dropdownSelector', 'weekStart', 'utc', 'withSaveTime', 'daysOfWeekDisabled', 'enableWeekends'];

        for (var prop in configuration) {
            if (configuration.hasOwnProperty(prop)) {
                if (validOptions.indexOf(prop) < 0) {
                    throw ("invalid option: " + prop);
                }
            }
        }

        // Order of the elements in the validViews array is significant.
        var validViews = ['minute', 'hour', 'day', 'month', 'year'];

        if (validViews.indexOf(configuration.startView) < 0) {
            throw ("invalid startView value: " + configuration.startView);
        }

        if (validViews.indexOf(configuration.minView) < 0) {
            throw ("invalid minView value: " + configuration.minView);
        }

        if (validViews.indexOf(configuration.minView) > validViews.indexOf(configuration.startView)) {
            throw ("startView must be greater than minView");
        }

        if (!angular.isNumber(configuration.minuteStep)) {
            throw ("minuteStep must be numeric");
        }
        if (configuration.minuteStep <= 0 || configuration.minuteStep >= 60) {
            throw ("minuteStep must be greater than zero and less than 60");
        }
        if (configuration.dropdownSelector !== null && !angular.isString(configuration.dropdownSelector)) {
            throw ("dropdownSelector must be a string");
        }
        if (!angular.isNumber(configuration.weekStart)) {
            throw ("minuteStep must be numeric");
        }
        if (configuration.weekStart <= 0 || configuration.weekStart >= 7) {
            throw ("weekStart must be greater than zero and less than 7");
        }
        if(configuration.daysOfWeekDisabled === 0 || configuration.daysOfWeekDisabled === 6){
            throw ("Weekends");
        }

        var validWithSaveTime = [true, false];
        if (validWithSaveTime.indexOf(configuration.withSaveTime) < 0) {
            throw ("invalid withSaveTime value: " + configuration.withSaveTime);
        }
    })

    .directive('datetimepicker', ['dateTimePickerConfig', 'dateTimePickerConfigValidation', 'AplanaUtils', '$timeout', 'dateTimePickerModifyNgModel', '$dateParser', 'HolidaysFactory',
        function (defaultConfig, validateConfigurationFunction, AplanaUtils, $timeout, dateTimePickerModifyNgModel, $dateParser, HolidaysFactory) {
            "use strict";

            return {
                restrict: 'A',
                require: 'ngModel',
                templateUrl: 'aplana/templates/datetimepicker/bootstrap-datetimepicker.html',
                scope: {
                    ngModel: "=",
                    onSetTime: "&",
                    minDate: "=",
                    maxDate: "=",
                    focusDate: "=",
                    enableWeekends: "="
                },
                replace: true,
                link: function (scope, element, attrs, controller) {

                    var directiveConfig = {};

                    if (attrs.datetimepickerConfig) {
                        directiveConfig = scope.$eval(attrs.datetimepickerConfig);
                    }

                    var configuration = {};

                    angular.extend(configuration, defaultConfig, directiveConfig);

                    validateConfigurationFunction(configuration);

                    if (angular.isDefined(scope.enableWeekends)) {
                        configuration.enableWeekends = scope.enableWeekends;
                    }

                    var nowMoment = moment();

                    scope.options = {
                        lang: 'ru',
                        dateType: 'string',
                        dateFormat: 'dd.MM.yyyy',
                        modelDateFormat: 'yyyy-MM-dd',
                        minDate: -Infinity,
                        maxDate: +Infinity
                    };
                    // Инициализация парсера
                    var dateParser = $dateParser({
                        format: scope.options.dateFormat,
                        lang: scope.options.lang
                    });
                    var getDate = function (newValue, key) {
                        if (newValue === 'today') {
                            var today = new Date();
                            return new Date(today.getFullYear(), today.getMonth(), today.getDate() + (key === 'maxDate' ? 1 : 0), 0, 0, 0, key === 'minDate' ? 0 : -1);
                        } else if (angular.isString(newValue) && newValue.match(/^".+"$/)) {
                            // Support {{ dateObj }}
                            return new Date(newValue.substr(1, newValue.length - 2));
                        } else if (!isNaN(parseFloat(newValue)) && isFinite(newValue)) {
                            return new Date(parseInt(newValue, 10));
                        } else {
                            return dateParser.parse(newValue, null, scope.options.modelDateFormat);
                        }
                    };


                    angular.forEach([
                        'minDate',
                        'maxDate'
                    ], function (key) {
                        // console.warn('attr.$observe(%s)', key, attrs[key]);
                        if (angular.isDefined(scope[key])) {
                            var newValue = scope[key];
                            // console.warn('attr.$observe(%s)=%o',  key, newValue, scope[key]);
                            if (newValue === 'today') {
                                var today = new Date();
                                scope.options[key] = new Date(today.getFullYear(), today.getMonth(), today.getDate() + (key === 'maxDate' ? 1 : 0), 0, 0, 0, key === 'minDate' ? 0 : -1);
                            } else if (angular.isString(newValue) && newValue.match(/^".+"$/)) {
                                // Support {{ dateObj }}
                                scope.options[key] = new Date(newValue.substr(1, newValue.length - 2));
                            } else if (!isNaN(parseFloat(newValue)) && isFinite(newValue)) {
                                scope.options[key] = new Date(parseInt(newValue, 10));
                            } else {
                                scope.options[key] = new Date(moment(newValue).format('YYYY-MM-DD'));
                            }
                        }
                        ;
                    });

                    scope.isMinMaxIntervalValid = function (date) {
                        var isMinValid = isNaN(scope.options.minDate) || date.getTime() >= scope.options.minDate;
                        var isMaxValid = isNaN(scope.options.maxDate) || date.getTime() <= scope.options.maxDate;
                        return (isMinValid && isMaxValid);
                    };

                    var build = function (holidayDates) {
                        var dataFactory = {
                            year: function (unixDate) {
                                var selectedDate = moment.utc(getTime(unixDate)).startOf('year');
                                // View starts one year before the decade starts and ends one year after the decade ends
                                // i.e. passing in a date of 1/1/2013 will give a range of 2009 to 2020
                                // Truncate the last digit from the current year and subtract 1 to get the start of the decade
                                var startDecade = (parseInt(selectedDate.year() / 10, 10) * 10);
                                var startDate = moment.utc(selectedDate).year(startDecade - 1).startOf('year');
                                var activeYear = scope.ngModel ? moment(scope.ngModel).year() : 0;
                                var nowYear = nowMoment.year();

                                var result = {
                                    'currentView': 'year',
                                    'nextView': configuration.minView === 'year' ? 'setTime' : 'month',
                                    'title': startDecade + '-' + (startDecade + 9),
                                    'leftDate': moment.utc(startDate).subtract(9, 'year').valueOf(),
                                    'rightDate': moment.utc(startDate).add(11, 'year').valueOf(),
                                    'dates': []
                                };

                                for (var i = 0; i < 12; i++) {
                                    var yearMoment = moment.utc(startDate).add(i, 'years');
                                    var dateValue = {
                                        'date': yearMoment.valueOf(),
                                        'display': yearMoment.format('YYYY'),
                                        'past': yearMoment.year() < startDecade,
                                        'future': yearMoment.year() > startDecade + 9,
                                        'active': yearMoment.year() === activeYear,
                                        'now': yearMoment.year() === nowYear,
                                        'disabled': !scope.isMinMaxIntervalValid(yearMoment.toDate())
                                    };

                                    result.dates.push(dateValue);
                                }
                                return result;
                            },

                            month: function (unixDate) {

                                var startDate = moment.utc(getTime(unixDate)).startOf('year');

                                var activeDate = scope.ngModel ? moment(scope.ngModel).format('MMMM YYYY') : 0;
                                var nowDate = nowMoment.format('MMMM YYYY');

                                var result = {
                                    'previousView': 'year',
                                    'currentView': 'month',
                                    'nextView': configuration.minView === 'month' ? 'setTime' : 'day',
                                    'currentDate': startDate.valueOf(),
                                    'title': startDate.format('YYYY'),
                                    'leftDate': moment.utc(startDate).subtract(1, 'year').valueOf(),
                                    'rightDate': moment.utc(startDate).add(1, 'year').valueOf(),
                                    'dates': []
                                };

                                for (var i = 0; i < 12; i++) {
                                    var monthMoment = moment.utc(startDate).add(i, 'months');
                                    var dateValue = {
                                        'date': monthMoment.valueOf(),
                                        'display': monthMoment.format('MMM'),
                                        'active': monthMoment.format('MMMM YYYY') === activeDate,
                                        'now': monthMoment.format('MMMM YYYY') === nowDate,
                                        'disabled': !scope.isMinMaxIntervalValid(monthMoment.toDate())
                                    };

                                    result.dates.push(dateValue);

                                }
                                return result;
                            },

                            day: function (unixDate) {

                                var selectedDate = moment.utc(getTime(unixDate));
                                var startOfMonth = moment.utc(selectedDate).startOf('month');
                                var endOfMonth = moment.utc(selectedDate).endOf('month');

                                var weekStart = configuration.weekStart % 7;
                                var startDate = moment.utc(startOfMonth).subtract(startOfMonth.day() > 0 ? startOfMonth.day() : 7, 'days');

                                var activeDate = scope.ngModel ? moment(scope.ngModel).format('DD MMMM YYYY') : '';
                                var nowDate = nowMoment.format('DD MMMM YYYY');

                                var result = {
                                    'previousView': 'month',
                                    'currentView': 'day',
                                    'nextView': configuration.minView === 'day' ? 'setTime' : 'hour',
                                    'currentDate': selectedDate.valueOf(),
                                    'title': selectedDate.format('MMMM YYYY'),
                                    'leftDate': moment.utc(startOfMonth).subtract(1, 'months').valueOf(),
                                    'rightDate': moment.utc(startOfMonth).add(1, 'months').valueOf(),
                                    'dayNames': [],
                                    'weeks': []
                                };

                                for (var dayNumber = 0; dayNumber < 7; dayNumber++) {
                                    result.dayNames.push(moment.utc().weekday(dayNumber).format('dd'));
                                }

                                function areDatesEqual(date1, date2) {
                                    return date1.getDate() == date2.getDate() &&
                                        date1.getMonth() == date2.getMonth() &&
                                        date1.getFullYear() == date2.getFullYear();

                                }
                                for (var i = 0; i < 6; i++) {
                                    var week = {dates: []};
                                    for (var j = weekStart; j < 7 + weekStart; j++) {
                                        var monthMoment = moment.utc(startDate).add((i * 7) + j, 'days');

                                        var isHoliday = false;
                                        if (holidayDates && holidayDates.length > 0) {
                                            for (var k = 0; k <= holidayDates.length; k++) {
                                                if (areDatesEqual(new Date(holidayDates[k]), monthMoment.toDate())) {
                                                    isHoliday = true;
                                                }
                                            }
                                        }
                                        var dateValue = {
                                            'date': monthMoment.valueOf(),
                                            'display': monthMoment.format('D'),
                                            'active': monthMoment.format('DD MMMM YYYY') === activeDate,
                                            'past': monthMoment.isBefore(startOfMonth),
                                            'future': monthMoment.isAfter(endOfMonth),
                                            'now': monthMoment.format('DD MMMM YYYY') === nowDate,
                                            'disabled': !scope.isMinMaxIntervalValid(monthMoment.toDate()),
                                            'isHolidays': isHoliday
                                        };
                                        week.dates.push(dateValue);
                                    }
                                    result.weeks.push(week);
                                }
                                return result;
                            },


                            hour: function (unixDate) {
                                var selectedDate = moment.utc(getTime(unixDate)).hour(0).minute(0).second(0);

                                var activeFormat = scope.ngModel ? moment(scope.ngModel).format('DD.MM.YYYY H') : '';
                                var nowDate = nowMoment.format('DD.MM.YYYY H');

                                var result = {
                                    'previousView': 'day',
                                    'currentView': 'hour',
                                    'nextView': configuration.minView === 'hour' ? 'setTime' : 'minute',
                                    'currentDate': selectedDate.valueOf(),
                                    'title': selectedDate.format('DD MMMM YYYY'),
                                    'leftDate': moment.utc(selectedDate).subtract(1, 'days').valueOf(),
                                    'rightDate': moment.utc(selectedDate).add(1, 'days').valueOf(),
                                    'dates': []
                                };

                                for (var i = 0; i < 24; i++) {
                                    var hourMoment = moment.utc(selectedDate).add(i, 'hours');
                                    var dateValue = {
                                        'date': hourMoment.valueOf(),
                                        'display': hourMoment.format('H:00'),
                                        'active': hourMoment.format('DD.MM.YYYY H') === activeFormat,
                                        'now': hourMoment.format('DD.MM.YYYY H') === nowDate
                                    };

                                    result.dates.push(dateValue);
                                }

                                return result;
                            },

                            minute: function (unixDate) {
                                var selectedDate = moment.utc(getTime(unixDate)).minute(0).second(0);

                                var activeFormat = scope.ngModel ? moment(scope.ngModel).format('DD.MM.YYYY H:mm') : '';
                                var nowDate = nowMoment.format('DD.MM.YYYY H:mm');

                                var result = {
                                    'previousView': 'hour',
                                    'currentView': 'minute',
                                    'nextView': 'setTime',
                                    'currentDate': selectedDate.valueOf(),
                                    'title': selectedDate.format('DD MMMM YYYY H:mm'),
                                    'leftDate': moment.utc(selectedDate).subtract(1, 'hours').valueOf(),
                                    'rightDate': moment.utc(selectedDate).add(1, 'hours').valueOf(),
                                    'dates': []
                                };

                                var limit = 60 / configuration.minuteStep;

                                for (var i = 0; i < limit; i++) {
                                    var hourMoment = moment.utc(selectedDate).add(i * configuration.minuteStep, 'minute');
                                    var dateValue = {
                                        'date': hourMoment.valueOf(),
                                        'display': hourMoment.format('H:mm'),
                                        'active': hourMoment.format('DD.MM.YYYY H:mm') === activeFormat,
                                        'now': hourMoment.format('DD.MM.YYYY H:mm') === nowDate
                                    };

                                    result.dates.push(dateValue);
                                }

                                return result;
                            },

                            setTime: function (unixDate) {
                                var tempDate = new Date(unixDate);
                                //http://jira.aplana.com/browse/SBRFEDOFNS-3916
                                var newDate = new Date(tempDate.getTime() /*- (-1 * tempDate.getTimezoneOffset() * 60000)*/);

                                if (scope.isMinMaxIntervalValid(newDate)) {
                                    if (angular.isFunction(scope.$parent.onSetTime)) {
                                        scope.$parent.onSetTime(newDate);
                                    }

                                    if (configuration.minView !== "hour" && configuration.minView !== "minute" && configuration.withSaveTime) {
                                        scope.ngModel = dateTimePickerModifyNgModel(scope.ngModel, newDate);
                                    } else {
                                        scope.ngModel = newDate;
                                    }

                                    scope.$parent.dismiss();
                                }
                                return dataFactory[scope.data.currentView](unixDate);
                            }
                        };
                        var getTime = function (unixDate) {
                            return unixDate;
                        };

                        var getUTCTime = function () {
                            var tempDate = (scope.ngModel ? moment(scope.ngModel).toDate() : (scope.focusDate ? moment(scope.focusDate).toDate() : new Date()));
                            return tempDate.getTime() - (tempDate.getTimezoneOffset() * 60000);
                        };

                        scope.changeView = function (viewName, unixDate, event) {

                            if (event) {
                                event.stopPropagation();
                                event.preventDefault();
                            }

                            if (viewName && (unixDate > -Infinity) && dataFactory[viewName]) {
                                scope.data = dataFactory[viewName](unixDate);
                            }
                            scope.$parent.$broadcast('POPOVER_UPDATE_POSITION');
                        };

                        scope.changeView(configuration.startView, getUTCTime());

                        $timeout(function () {
                            scope.$parent.$broadcast('POPOVER_UPDATE_POSITION');
                        }, 0);
                    };

                    if (configuration.enableWeekends) {
                        var getHolidays = HolidaysFactory.getHoliday();
                        getHolidays.then(build);
                    } else {
                        build();
                    }

                }
            };
        }])

    /**
     *    @author <a href="mailto:ymakogon@aplana.com">Yuriy Makogon</a>
     *
     *    @deprecated
     */
    .directive('dateTimeInput', ['dateTimePickerModifyNgModel', '$parse', '$exceptionHandler', function (dateTimePickerModifyNgModel, $parse, $exceptionHandler) {
        "use strict";
        return {
            require: 'ngModel',
            restrict: 'A',
            link: function (scope, element, attr, controller) {
                var ngModelGet = $parse(attr.ngModel),
                    ngModelSet = ngModelGet.assign;

                controller.$setViewValueNotDirty = function (value) {
                    this.$viewValue = value;

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

                var PRISTINE_CLASS = 'ng-pristine',
                    DIRTY_CLASS = 'ng-dirty';
                var parentForm = element.inheritedData('$formController');
                controller.$setDirty = function () {
                    if (this.$pristine) {
                        this.$dirty = true;
                        this.$pristine = false;
                        element.removeClass(PRISTINE_CLASS).addClass(DIRTY_CLASS);
                        parentForm.$setDirty();
                    }
                };

                if (!attr.dateTimeInput) {
                    throw ("Проверьте формат");
                }

                var format = attr.dateTimeInput;

                var onlyDate = (format.indexOf("H") < 0 && format.indexOf("h") < 0 && format.indexOf("m") < 0);

                function parseDate(viewValue) {
                    if (!viewValue) {
                        return null;
                    } else if (angular.isDate(viewValue) && !isNaN(viewValue)) {
                        if (!onlyDate || attr.dateTimeInputSaveTime === "true") {
                            return viewValue;
                        } else {
                            return moment(viewValue)
                                .hour(0).minute(0).second(0).milliseconds(0).toDate();
                        }
                    } else if (angular.isString(viewValue)) {
                        var result = viewValue;

                        var momentValue = moment(viewValue, format, true);
                        if (momentValue.isValid()) {
                            result = momentValue.toDate();
                        } else {
                            if (!onlyDate || attr.dateTimeInputSaveTime === "true") {
                                result = moment().toDate();
                            } else {
                                result = moment().hour(0).minute(0).second(0).milliseconds(0).toDate();
                            }
                        }

                        if (attr.dateTimeInputSaveTime === "true") {
                            return dateTimePickerModifyNgModel(controller.$modelValue, result);
                        } else {
                            return result;
                        }

                    } else {
                        return undefined;
                    }
                }

                var formatFn = function (modelValue) {
                    var result = modelValue;
                    if (modelValue) {
                        if (onlyDate) {
                            result = moment.utc(modelValue).local().format(format);
                        } else {
                            result = moment(modelValue).format(format);
                        }
                    }
                    return result;
                };

                controller.$parsers.unshift(parseDate);

                controller.$formatters.push(function (modelValue) {
                    if (angular.isDate(modelValue)) {
                        if (!onlyDate || attr.dateTimeInputSaveTime === "true") {

                        } else {
                            var correctDate = moment(modelValue)
                                .hour(0).minute(0).second(0).milliseconds(0).toDate();

                            controller.$setViewValueNotDirty(correctDate);
                            controller.$render();
                            modelValue = correctDate;
                        }
                    }
                    return modelValue;
                });

                element.bind('focusout', function () {
                    controller.$render();
                });

                scope.onSetTime = function () {
                    controller.$setDirty();
                };

                controller.$render = function ngModelRender() {
                    var date = controller.$modelValue ? formatFn(controller.$modelValue) : '';
                    element.val(date);
                };
            }
        };
    }])

    /**
     *    @deprecated
     */
    .directive('aplanaDatetimepicker', ['AplanaUtils', function (AplanaUtils) {
        return {
            restrict: 'A',
            replace: true,
            scope: true,
            templateUrl: AplanaUtils.templatePath + 'datetimepicker/datetimepicker.html',
            compile: function (element, attr) {
                element[0].removeAttribute('data-aplana-datetimepicker');
                var inputElement = element.find('input');
                var buttonElement = element.find('button');
                var dateTimeElement = angular.element('<div datetimepicker></div>');

                var ngModelAttr = attr.ngModel || AplanaUtils.findNgModel(element[0]);

                AplanaUtils.copyAttributes(element, dateTimeElement, 'data-ng-model');
                AplanaUtils.copyAttributes(element, dateTimeElement, 'data-datetimepicker-config');
                AplanaUtils.copyAttributes(element, buttonElement, ['data-placement', 'data-container']);

                element[0].removeAttribute('data-placement');
                element[0].removeAttribute('data-container');
                element[0].removeAttribute('data-datetimepicker-config');

                // Сформируем id и name для элемента
                var modelId = AplanaUtils.buildModelId(ngModelAttr);
                inputElement.attr('name', modelId);
                inputElement.attr('id', modelId);

                AplanaUtils.moveAttributes(element, inputElement);

                if (!attr.dateTimeInput) {
                    if (attr.aplanaDatetimepicker === "date" || attr.aplanaDatetimepicker === "dateWithSaveTime") {
                        inputElement.attr("data-date-time-input", "DD.MM.YYYY");
                    } else {
                        inputElement.attr("data-date-time-input", "DD.MM.YYYY HH:mm");
                    }
                }

                inputElement.attr("data-date-time-input-save-time", attr.aplanaDatetimepicker === "dateWithSaveTime");

                if (attr.aplanaDatetimepicker === "date") {
                    dateTimeElement.attr("1111");
                } else if (attr.aplanaDatetimepicker === "dateWithSaveTime") {
                    dateTimeElement.attr("data-datetimepicker-config", "{ startView: \\'day\\', minView: \\'day\\', utc: true }");
                } else {
                    dateTimeElement.attr("data-datetimepicker-config", "{ startView: \\'day\\', minView: \\'minute\\', minuteStep: 5 }");
                }

                buttonElement.attr("data-aplana-popover", "{content: '" + dateTimeElement[0].outerHTML + "'}");
            }
        };
    }])
;
