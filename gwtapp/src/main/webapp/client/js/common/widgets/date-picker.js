(function () {
    'use strict';
    var widgets = angular.module('datePicker', []);
    /**
     * @description Виджет для поля даты
     *
     * @attr required - обязательность заполнения поля, ставится метка (*). Никакой валидации не происходит
     * @attr model - модель данных
     * @attr minDate - самая ранняя дата, которую можно выбрать
     * @attr maxDate - самая поздняя дата, которую можно выбрать
     * @attr inputStyle - дополнительная настройка поля ввода
     * @attr onBlur - метод, который срабатывает при событии onBlur
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
            templateUrl: 'client/js/common/widgets/date-picker.html',
            link: function ($scope, $element, $attributes) {
                $scope.format = "dd.MM.yyyy";
                $scope.isOpen = false;
                $scope.click = function () {
                    $scope.isOpen = true;
                    $scope.datePickerOptions = {
                        minDate: $scope.minDate,
                        maxDate: $scope.maxDate
                    };
                };
                $scope.pickerStyle = $scope.inputStyle ? $scope.inputStyle : '';
            }
        };
    });
}());