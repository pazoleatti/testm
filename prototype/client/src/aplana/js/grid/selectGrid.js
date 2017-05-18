/**
 * aplana-select-grid
 * Директива aplana-select-grid служит для создания ui-select2 в aplana_grid
 * http://localhost:8080/#/aplana_grid
 */
(function () {
    'use strict';

    angular.module("aplana.grid.select", [])
        .directive('aplanaSelectGrid', ['AplanaUtils', function(AplanaUtils) {
            return {
                replace: true,
                templateUrl: AplanaUtils.templatePath + 'grid/selectGrid.html',
                restrict: 'A',
                require: '?ngModel',
                scope: {
                    select2Options: '=',
                    modelValue: '=ngModel',
                    gridCtrl: '='
                },
                compile: function compile(el, attributes, transclude) {
                    var rowId = attributes.rowId;
                    var columnModelName = attributes.columnModelName;

                    return {
                        pre: function (scope, element, attrs, ngModel) {
                            scope.setGridValue = function (value) {
                                var object = scope.gridCtrl.getRawData(rowId);
                                object[columnModelName] = value;
                                scope.gridCtrl.setCell(rowId, columnModelName, value);
                            };

                            scope.hideSelect = function () {
                                var object = scope.gridCtrl.getRawData(rowId);
                                scope.gridCtrl.setCell(rowId, columnModelName, object[columnModelName]);
                            };

                            scope.preparedOptions = angular.extend({
                                selectAction: function (evt) {
                                    scope.setGridValue(evt.object);
                                    scope.gridCtrl.updateModel();
                                },
                                closeAction: function () {
                                    scope.hideSelect();
                                }
                            }, scope.select2Options);
                        },
                        post: function postLink(scope, element, attrs, controller) {
                            setTimeout(function () {
                                element.select2("open");
                            }, 0);
                        }
                    };
                }
            };
        }]);
}());