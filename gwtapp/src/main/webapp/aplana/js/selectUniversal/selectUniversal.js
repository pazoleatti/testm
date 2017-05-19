/**
 * aplana-select-universal (Универсальный селект)
 * Директива предназначена для создания всплывающего блока с произвольным контентом
 * http://localhost:8080/#/aplana_select_universal
 */
(function () {
    'use strict';

    /**
     * @name AplanaSelectUniversal
     */

    angular.module('aplana.select.universal', ['aplana.utils'])
        .constant('pushFormatters', function (ctrl, scope) {
            var formatFn = function (modelValue) {
                var result = modelValue;
                if (modelValue) {
                    if (angular.isArray(modelValue)) {
                        result = '';
                        angular.forEach(modelValue, function (item) {
                            if (!ctrl.$isEmpty(result)) {
                                result += ", ";
                            }
                            result += scope.universalShowField(item);
                        });
                    } else {
                        result = scope.universalShowField(modelValue);
                    }
                }
                return result;
            };

            //ctrl.$formatters.pop();
            ctrl.$formatters.push(formatFn);
        })

        .controller('SelectUniversalCtrl', ['$scope',
            function ($scope) {
                $scope.filter = {searchFieldName: ''};

                var config = {
                    required: true,
                    options: {
                        datatype: 'local',
                        data: [],
                        requestParameters: {filter: []},
                        width: 'auto',
                        height: 'auto',
                        rowNum: 10,
                        multiselect: false,
                        aplanaSingleSelect: true
                    },
                    init: function (ctrl) {
                        ctrl.loadComplete = function () {
                            $scope.$broadcast('POPOVER_UPDATE_POSITION');
                        };
                    }
                };

                if (!$scope.universalGridConfig) {
                    console.error("Проверьте aplana-select-universal-config");
                } else {
                    $scope.filterPlaceHolder = ($scope.universalGridConfig.searchFieldTitle) ? $scope.universalGridConfig.searchFieldTitle : 'Поиск';

                    config.options = angular.extend(config.options, $scope.universalGridConfig.options);
                    if (config.options.multiselect) {
                        config.options.aplanaSingleSelect = false;
                    }
                }


                $scope.universalGrid = config;

                if ($scope.ngModel) {
                    var selected = [];
                    selected.push($scope.ngModel);
                    $scope.universalGrid.value = selected;
                }

                $scope.applyFilter = function () {
                    if ($scope.universalGridConfig.searchFieldName) {
                        angular.forEach($scope.universalGrid.options.requestParameters.filter, function (item) {
                            if (item["property"] === $scope.universalGridConfig.searchFieldName) {
                                $scope.universalGrid.options.requestParameters.filter.splice($scope.universalGrid.options.requestParameters.filter.indexOf(item), 1);
                            }
                        });
                        if ($scope.filter.searchFieldName.length !== 0) {
                            $scope.universalGrid.options.requestParameters.filter.push(
                                {property: $scope.universalGridConfig.searchFieldName,
                                    operation: "STRING_CONTAINS_IC", value: $scope.filter.searchFieldName});
                        }
                    }
                    $scope.universalGrid.ctrl.refreshGrid();
                };

                $scope.clearFilter = function () {
                    $scope.filter = {searchFieldName: ''};
                    angular.forEach($scope.universalGrid.options.requestParameters.filter, function (item) {
                        if (item["property"] === $scope.universalGridConfig.searchFieldName) {
                            $scope.universalGrid.options.requestParameters.filter.splice($scope.universalGrid.options.requestParameters.filter.indexOf(item), 1);
                        }
                    });
                    $scope.universalGrid.ctrl.refreshGrid();
                };

                $scope.setValue = function () {
                    if ($scope.universalGrid.value[0]) {
                        if ($scope.universalGrid.options.multiselect) {
                            $scope.updateModel($scope.universalGrid.value);
                        } else {
                            if ($scope.universalGrid.value[0]) {
                                $scope.updateModel($scope.universalGrid.value[0]);
                            }
                            else {
                                $scope.updateModel(undefined);
                            }
                        }
                        $scope.dismiss();
                    } else {
                    }
                };
            }])

        .directive('aplanaSelectUniversal', ['AplanaUtils', 'pushFormatters', function (AplanaUtils, pushFormatters) {
            return {
                restrict: 'A',
                replace: true,
                transclude: true,
                templateUrl: AplanaUtils.templatePath + 'selectUniversal/selectUniversal.html',
                scope: {
                    ngModel: '=',
                    universalGridConfig: "=aplanaSelectUniversalConfig"
                },
                require: 'ngModel',
                compile: function (element, attrs) {
                    element[0].removeAttribute('data-aplana-select-universal');

                    /* Модифицируем input */
                    var inputElement = element.find('input');
                    AplanaUtils.moveAttributes(element, inputElement, ['data-placement', 'data-original-title']);

                    // Сформируем id и name для элемента
                    var ngModelAttr = attrs.ngModel || AplanaUtils.findNgModel(inputElement);

                    var modelId = AplanaUtils.buildModelId(ngModelAttr);
                    inputElement.attr('name', modelId);
                    inputElement.attr('id', modelId);

                    /* Модифицируем button */
                    var buttonElement = element.find('button');
                    AplanaUtils.moveAttributes(element, buttonElement);

                    return function (scope, element, attrs, ctrl) {
                        if (ctrl) {

                            pushFormatters(ctrl, scope);

                            ctrl.$render = function ngModelRender() {
                                var viewValue = ctrl.$modelValue;

                                if (attrs.required) {
                                    scope.inputNgCtrl.$setValidity('required', !ctrl.$isEmpty(viewValue));
                                }

                                angular.forEach(ctrl.$formatters, function (formatter) {
                                    viewValue = formatter(viewValue);
                                });

                                element.find('input').val(ctrl.$isEmpty(viewValue) ? '' : viewValue);
                            };

                            scope.updateModel = function (viewValue) {
                                ctrl.$setViewValue(viewValue);
                                ctrl.$render();
                                scope.inputNgCtrl.$pristine = false;
                                scope.inputNgCtrl.$dirty = true;
                            };
                        }
                    };
                }
            };
        }])

        /* Директива необходимая для перекрытия функции рендера input элемента */
        .directive('aplanaSelectUniversalImplementation', [ function () {
            return {
                require: 'ngModel',
                restrict: 'A',
                link: function (scope, element, attrs, ctrl) {
                    if (ctrl) {

                        scope.inputNgCtrl = ctrl;

                        scope.universalShowField = function (item) {
                            if (scope.universalGridConfig.formatter) {
                                return scope.universalGridConfig.formatter(item);
                            }
                            else {
                                return item.name || "";
                            }
                        };

                        ctrl.$setViewValue(scope.ngModel);
                        ctrl.$setPristine();
                    }
                }
            };
        }]);
}());