/**
 * aplana-ajax-submit (Отправка формы)
 * Без этой директивы ajaxForm переходит на другую страницу
 * http://localhost:8080/#/ajaxSubmit
 */
(function () {
    'use strict';
    angular.module('aplana.ajaxSubmit', [])
        .directive('aplanaAjaxSubmit', [function () {
            return {
                scope: {
                    options: "=aplanaAjaxSubmit"
                },
                link: function (scope, iElement) {
                    iElement.ajaxForm(scope.options);
                }
            };
        }]);
}());