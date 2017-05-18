(function () {
    'use strict';
    var widgets = angular.module('sbrfNdfl.widgets', []);
    /**
     * Виджет для поля даты
     *
     * @attr label - метка поля
     * @attr required - обязательность заполнения поля, ставится метка (*). Никакой валидации не происходит
     * @attr model - модель данных
     * @attr labelWidth - ширина метки в колонках bootstrap'а
     */
    widgets.directive('muDatePicker', function () {
        return {
            restrict: 'E',
            scope: {
                required: '=',
                model: '=',
                minDate: '=?',
                maxDate: '=?',
                inputStyle: '@', // для дополнительных настроек виджета даты
                onBlur: '&'
            },
            templateUrl: 'js/common/widgets/date-picker.html',
            link: function ($scope, $element, $attributes) {
                $scope.format = "dd.MM.yyyy";
                $scope.isOpen = false;
                $scope.click = function () {
                    $scope.isOpen = true;
                    $scope.datePickerOptions = {
                        minDate: $scope.minDate,
                        maxDate: $scope.maxDate
                    }
                };
                $scope.pickerStyle = $scope.inputStyle ? $scope.inputStyle : '';
            }
        }
    });
}());