(function () {
    'use strict';
    var widgets = angular.module('aplana.datePicker', []);
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
    widgets.directive('datePicker', function () {
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
            templateUrl: 'client/components/datePicker/date-picker.html',
            link: function ($scope) {
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