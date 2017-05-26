/**
 * aplana-set-null-if-empty (Установка пустого значения в null)
 * Директива предназначена для замены пустых значений модели с "" на null,
 * т.к. на некоторых полях сущностей стоит RegExp и при "" он валится.
 * http://localhost:8080/#/aplana_set_null_if_empty
 */
(function () {
    'use strict';

    angular.module('aplana.setNullIfEmpty', [])
        .directive('aplanaSetNullIfEmpty', [function () {
            return {
                restrict: 'A',
                require: 'ngModel',
                link: function (scope, element, attr, ctrl) {
                    ctrl.$parsers.push(function (value) {
                        if (ctrl.$isEmpty(value)) {
                            return null;
                        } else {
                            return value;
                        }
                    });
                }
            };
        }]);

}());