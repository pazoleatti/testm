/**
 * aplana-datepicker-timepicker (Выбор даты и времени)
 * Директива предназначена для создания двух директив поля выбора даты и поля выбора времени
 * (http://localhost:8080/#/aplana_datepicker и http://localhost:8080/#/aplana_timepicker)
 *
 * http://localhost:8080/#/aplana_datepicker_timepicker
 */

angular.module('aplana.datepickerTimepicker', [])

    .directive('aplanaDatepickerTimepicker', ['AplanaUtils', function (AplanaUtils) {
        return {
            restrict: 'A',
            require: ['ngModel', '?^form'],
            scope: {
                modelValue: '=ngModel'
            },
            replace: true,
            templateUrl: AplanaUtils.templatePath + 'datepickerTimepicker/datepickerTimepicker.html',
            compile: function (element, attrs) {
                element[0].removeAttribute('data-aplana-datepicker-timepicker');
                var ngModelAttr = attrs.ngModel || AplanaUtils.findNgModel(element[0]);
                var datepicker = element.find('div[data-aplana-datepicker]');
                var timepicker = element.find('div[data-aplana-timepicker]');

                AplanaUtils.copyAttributes(element, datepicker, ['data-placement', 'data-container', 'data-ng-required']);
                AplanaUtils.copyAttributes(element, timepicker, ['data-placement', 'data-container', 'data-ng-required']);

                element[0].removeAttribute('data-placement');
                element[0].removeAttribute('data-container');
                element[0].removeAttribute('data-ng-required');

                var modelId = AplanaUtils.buildModelId(ngModelAttr);
                timepicker.attr('name', modelId + "_timepicker");
                timepicker.attr('id', modelId + "_timepicker");
                datepicker.attr('name', modelId + "_datepicker");
                datepicker.attr('id', modelId + "_datepicker");

                return function (scope, lelement, attrs, controllers) {
                    scope.data = {
                        value: scope.modelValue
                    };

                    //вычленяем контроллеры модели и формы
                    scope.ngModelCtrl = controllers[0];
                    scope.ngFormCtrl = controllers[1];

                    scope.ngModelCtrl.$render = function () {
                        scope.data.value = scope.ngModelCtrl.$modelValue;
                        scope.ngModelCtrl.$setViewValue(scope.data.value);
                    };

                    scope.$watch('data.value', function (newValue, oldValue) {
                        if (!angular.equals(newValue, oldValue)) {
                            scope.ngModelCtrl.$setViewValue(scope.data.value);
                        }
                    });

                    scope.ngModelCtrl.$modelValue = scope.modelValue;

                    scope.ngModelCtrl.$render();
                    scope.ngModelCtrl.$setPristine();
                };
            }
        };
    }]);