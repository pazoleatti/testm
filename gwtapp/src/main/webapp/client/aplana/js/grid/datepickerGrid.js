/**
 * aplana-datepicker-grid
 * Директива aplana-datepicker-grid служит для создания datepicker в aplana_grid
 * http://localhost:8080/#/aplana_grid
 */
(function () {
    'use strict';

    angular.module("aplana.grid.datepicker", [])
        .directive('aplanaDatepickerGrid', ['AplanaUtils', '$filter', function(AplanaUtils, $filter) {
            return {
                replace: true,
                templateUrl: AplanaUtils.templatePath + 'grid/datepickerGrid.html',
                restrict: 'A',
                require: '?ngModel',
                scope: {
                    modelObject: '=ngModel',
                    gridCtrl: '='
                },
                compile: function compile(el, attributes, transclude) {
                    var rowId = attributes.rowId;
                    var columnModelName = attributes.columnModelName;
                    return {
                        pre: function (scope, element, attrs) {
                            scope.dateInitValue = scope.modelObject.dateValue;

                            scope.setGridValue = function (value) {
                                var object = scope.gridCtrl.getRawData(rowId);

                                object[columnModelName] = value;
                                scope.gridCtrl.setCell(rowId, columnModelName, value);
                            };

                            scope.hideSelect = function () {
                                var object = scope.gridCtrl.getRawData(rowId);
                                scope.gridCtrl.setCell(rowId, columnModelName, object[columnModelName]);
                            };

                            // Вызывает при выборе значения календаря
                            scope.$onSelectValue = function(value) {
                                // datepicker возвращает тип Date,
                                // надо преобразовать в формат модели данных грида
                                // дополнить в случае других форматов
                                scope.setGridValue($filter('date')(value, 'yyyy-MM-dd'));
                                scope.gridCtrl.updateModel();
                            };

                            // Событие при скрытие окошка календаря
                            element.on('hidden.bs.popover', function () {
                                scope.hideSelect();
                            });
                        },
                        post: function postLink(scope, element, attrs, controller) {
                            // Открывает datepicker с небольшой задержкой
                            // задержка 100, при задержке 0 иногда не успевает отстроится элемент
                            setTimeout(function () {
                                element.find('.btn-popover').popover("show");
                            }, 100);
                        }
                    };
                }
            };
        }])
    ;
}());