/**
 * aplana-link (Кнопка с иконкой)
 * Директива предназначена для создания кнопки с иконкой в виде ссылки
 * http://localhost:8080/#/aplana_link
 */
(function () {
    "use strict";

    angular.module('aplana.link', ['aplana.utils'])
        .directive('aplanaLink', ['$interpolate', 'AplanaUtils', function ($interpolate, AplanaUtils) {

            return {
                restrict: 'A',
                templateUrl: AplanaUtils.templatePath + 'link/link.html',
                replace: true,
                scope: {
                    aplanaLink: '=',
                    aplanaLinkClass: '=',// css-класс для внутреннего тега i (иконка)
                    aplanaLinkClassTagA: '=', // css-класс для самого тега a. поумолчанию класс '.aplanaLink'
                    aplanaLinkClick: '&',
                    aplanaCaret: '@',
                    aplanaDisabled: '@'
                },
                link: function (scope, element) {
                    // css-класс по умолчанию
                    if (scope.aplanaLinkClassTagA === undefined){
                        scope.aplanaLinkClassTagA = 'aplanaLink';
                    }

                    // aplanaCaret и aplanaDisabled приходят в виде строки. ожидается 'true' или 'false'
                    scope.isTrue = function (arg) {
                        return arg === 'true';
                    };
                    scope.hasText = function () {
                        return scope.aplanaLink != null && scope.aplanaLink.length !== 0;
                    };
                    element.on("keypress", function (event) {
                        if (event.keyCode === 13) {
                            event.preventDefault();
                            return false;
                        }
                    });
                    element.on("click", function (event) {
                        if (element.hasClass('disabled')) {
                            event.preventDefault();
                        } else {
                            scope.$apply(scope.aplanaLinkClick());
                        }
                    });
                }
            };
        }]);
}());

