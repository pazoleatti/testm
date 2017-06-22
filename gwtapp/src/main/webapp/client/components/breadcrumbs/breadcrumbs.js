/**
 * aplana-breadcrumbs (Хлебные крошки)
 * Директива предназначена для отображения "хлебных крошек" на страницах с использованием ui-router
 * http://localhost:8080/#/aplana_breadcrumbs
 */
(function () {
    "use strict";

    angular.module('aplana.breadcrumbs', ['aplana.utils'])
        .factory('$breadcrumbs',
            ['$rootScope', '$location', '$state',
                function ($rootScope, $location, $state) {

                    var breadcrumbs = [];
                    var breadcrumbsService = {};

                    //we want to update breadcrumbs only when a route is actually changed
                    //as $location.path() will get updated imediatelly (even if route change fails!)
                    $rootScope.$on('$stateChangeSuccess', function () {

                        var pathElements = $state.$current.path;

                        var result = [], i;
                        var breadcrumbPath = function (index) {
                            return '/' + (pathElements.slice(0, index + 1)).join('/');
                        };

                        var breadcrumbName = function (index) {
                            var result = pathElements[index];
                            if (angular.isDefined($state.$current.path[index].data)) {
                                result = angular.isDefined($state.$current.path[index].data.title) ? $state.$current.path[index].data.title : result;
                            }

                            return result;
                        };

                        for (i = 0; i < pathElements.length; i++) {
                            result.push({name: breadcrumbName(i), path: breadcrumbPath(i)});
                        }

                        breadcrumbs = result;
                    });

                    breadcrumbsService.getAll = function () {
                        return breadcrumbs;
                    };

                    breadcrumbsService.getFirst = function () {
                        return breadcrumbs[0] || {};
                    };

                    return breadcrumbsService;
                }])


        .directive('aplanaBreadcrumbs',
            ['$breadcrumbs', 'AplanaUtils', function (breadcrumbs, AplanaUtils) {
                return {
                    templateUrl: AplanaUtils.templatePath + 'breadcrumbs/breadcrumbs.html',
                    link: function (scope) {
                        scope.breadcrumbs = breadcrumbs;
                        scope.pathPrefix = '#';
                    }
                };
            }]);
}());
