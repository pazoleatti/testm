(function () {
    'use strict';
    var widgets = angular.module('mtsUsim.widgets', []);

    /**
     * Виджет для поля ввода
     *
     * @attr label - метка поля
     * @attr placeholder - значение по умолчанию
     * @attr disabled - состояние активный/неактивный. По умолчанию активный
     * @attr model - модель данных
     * @attr required - обязательность заполнения поля, ставится метка (*). Никакой валидации не происходит
     */
    widgets.directive('muInput', function () {
        return {
            restrict: 'E',
            scope: {
                label: '@',
                placeholder: '@',
                disabled: '=',
                model: '=',
                required: '=',
                pattern: '=?',
                type: '@',
                onBlur: '&',
                labelWidth: "@"
            },
            templateUrl: 'js/common/widgets/input.html',
            link: function ($scope, $element, $attributes) {
                if ($attributes.type == undefined) {
                    $scope.type = "text";
                }
                if ($attributes.valueType == "int"){
                    $scope.pattern = "^-?\\d+$";
                    $scope.type = "number";
                }
                if ($attributes.valueType == "number"){
                    $scope.pattern = "^-?\\d+\\.?\\d+$";
                    $scope.type = "number";
                }
                if ($attributes.valueType == "money"){
                    $scope.pattern = "^-?\\d+(\\.\\d{1,2})?$";
                    $scope.type = "number";
                }
                if ($attributes.placeholder == undefined) {
                    $scope.placeholder = "Введите строку";
                }
                if($scope.type == "number"){
                    $scope.placeholder = "Введите число";
                }
                //Если паттерн не задали, явно прописываем, что может быть введена любая последовательность символов
                if($scope.pattern === undefined){
                    $scope.pattern = ".*";
                }
                // Изменение пропорций метки и поля
                $scope.labelGridClass = 'col-md-4';
                $scope.pickerGridClass = 'col-md-8';
                if ($attributes.labelWidth != undefined) {
                    var labelWidth = parseInt($attributes.labelWidth);
                    $scope.labelGridClass = 'col-md-' + labelWidth;
                    $scope.pickerGridClass = 'col-md-' + (12 - labelWidth);
                }
            }
        };
    });

    /**
     * Виджет для кнопок тулбара
     *
     * @attr label - название кнопки
     * @attr disabled - состояние активный/неактивный. По умолчанию неактивный. В зависимости от состояния меняется
     * иконка
     * @attr icon - имя иконки без расширения файла
     * @attr click - вызываемая функция при щелчке
     */
    widgets.directive('muButton', function () {
        return {
            restrict: 'E',
            scope: {
                label: '@',
                disabled: '=',
                icon: '@',
                click: '&'
            },
            templateUrl: 'js/common/widgets/button.html'
        }
    });

    /**
     * Виджет для выпадающего списка
     *
     * @attr label - метка выпадающего поля
     * @attr model - модель данных
     * @attr required - обязательность заполнения поля, ставится метка (*). Никакой валидации не происходит
     * @attr items - список элементов
     * @attr onSelect - вызов функции при выборе элемента из списка
     * @attr placeholder - значение по умолчанию
     * @attr disabled - состояние активный/неактивный. По умолчанию активный
     */
    widgets.directive('muSelect', ['$filter', function ($filter) {
        return {
            restrict: 'E',
            scope: {
                label: '@',
                model: '=',
                required: '=',
                items: '=',
                onSelect: '&',
                placeholder: '@',
                disabled: '=',
                labelWidth: "@"
            },
            templateUrl: 'js/common/widgets/select.html',
            link: function ($scope, $element, $attributes) {
                $scope.allowClear = $attributes.ngAllowClear ? $attributes.ngAllowClear : true;
                if ($attributes.placeholder == undefined) {
                    $scope.placeholder = $filter('translate')('filter.placeholder.all')
                }
                // Изменение пропорций метки и поля
                var labelWidth = $attributes.labelWidth ? parseInt($attributes.labelWidth) : 4;
                $scope.labelGridClass = 'col-md-' + labelWidth;
                $scope.pickerGridClass = 'col-md-' + (12 - labelWidth);
            }
        };
    }]);

    widgets.directive('muLabel', function () {
        return {
            restrict: 'E',
            scope: {
                label: '@',
                value: '=',
                labelWidth: "@",
                labelClass: "@"
            },
            templateUrl: 'js/common/widgets/label.html',
            link: function ($scope, $element, $attributes) {
                var labelWidth = $attributes.labelWidth ? parseInt($attributes.labelWidth) : 4;
                $scope.labelGridClass = 'col-md-' + labelWidth;
                $scope.pickerGridClass = 'col-md-' + (12 - labelWidth);
            }
        };
    });

    /**
     * Виджет для текстового поля
     *
     * @attr label - метка текстового поля
     * @attr required - обязательность заполнения поля, ставится метка (*). Никакой валидации не происходит
     * @attr model - модель данных
     */
    widgets.directive('muTextarea', function () {
        return {
            restrict: 'E',
            scope: {
                label: '@',
                required: '=',
                model: '='
            },
            templateUrl: 'js/common/widgets/textarea.html',
            link: function ($scope) {
                // генеририуем уникальный id
                $scope.id = Date.now();
            }
        }
    });

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
                label: '@',
                required: '=',
                model: '=',
                minDate: '=?',
                maxDate: '=?',
                labelWidth: "@",
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
                // Изменение пропорций метки и поля
                var labelWidth = $attributes.labelWidth ? parseInt($attributes.labelWidth) : 4;
                $scope.labelGridClass = 'col-md-' + labelWidth;
                $scope.pickerGridClass = 'col-md-' + (12 - labelWidth);
                $scope.pickerStyle = $scope.inputStyle ? $scope.inputStyle : '';
            }
        }
    });

    /**
     * Виджет для поля загрузки файлов
     *
     * @attr label - метка поля
     * @attr model - модель данных
     * @attr required - обязательность заполнения поля, ставится метка (*). Никакой валидации не происходит
     */
    widgets.directive('muFileUploader', function () {
        return {
            restrict: 'E',
            scope: {
                label: '@',
                model: '=',
                required: '='
            },
            templateUrl: 'js/common/widgets/file-uploader.html'
        }
    })
}());