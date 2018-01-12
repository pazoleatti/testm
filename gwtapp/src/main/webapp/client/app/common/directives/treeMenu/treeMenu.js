(function () {
    'use strict';

    /**
     * @description Модуль, содержащий директивы для работы с многоуровневым меню
     */

    angular.module('app.treeMenu', [])
        /**
         * @description Директива для многоуровневого меню
         */
        .directive('tree', function() {
            return {
                restrict: "E",
                replace: true,
                scope: {
                    tree: '='
                },
                templateUrl: 'client/app/common/directives/treeMenu/tree.html'
            };
        })

        /**
         * @description Директива для перехода по ссылке в многоуровневом меню
         */
        .directive('leaf', function($compile) {
            return {
                restrict: "E",
                replace: true,
                scope: {
                    leaf: "="
                },
                templateUrl: 'client/app/common/directives/treeMenu/leaf.html',
                link: function(scope, element) {
                    if (angular.isArray(scope.leaf.subtree)) {
                        element.append("<tree tree='leaf.subtree'></tree>");
                        element.addClass('dropdown-submenu');
                        $compile(element.contents())(scope);
                    }
                }
            };
        });
}());