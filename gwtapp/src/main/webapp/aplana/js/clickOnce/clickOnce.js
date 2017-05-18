/**
 * aplana-click-once (Одиночный клик)
 * Директива предотвращает повторное (быстрое) нажатие на кнопку
 * http://localhost:8080/#/aplana_clickOnce
 */
(function () {
    'use strict';
    angular.module('aplana.clickOnce', ['aplana.overlay'])
        .directive('aplanaClickOnce', [ '$timeout', '$overlayService', function ($timeout, $overlayService) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    var isSubmitType = false;
                    var type = element.attr("type");
                    var disabled_delay = 1000;
                    if (type && type.toLowerCase() === 'submit') {
                        isSubmitType = true;
                    }

                    element.bind('click', function (event) {
                        /**
                         * Если кнопка или input имеет тип type='submit', она будет заблокирована формой при отправке
                         * Подробнее в модуле submitValid.
                         *
                         * В том случае если кнопка не submit тогда устанавливаем атрибут
                         * disabled="true" при этом повторное нажатие на кнопку станет
                         * недоступным.
                         */
                        if (!isSubmitType) {
                            element.addClass("disabled");
                            element.attr('disabled', 'disabled');

                            var unlockButtons = function () {
                                $timeout(function () {
                                    if ($overlayService.isActive()) {
                                        unlockButtons();
                                        return;
                                    }

                                    if (isSubmitType) {
                                        element.disableSubmit = null;
                                    }

                                    element.removeClass("disabled");
                                    element.removeAttr("disabled");
                                }, disabled_delay);
                            };

                            unlockButtons();
                        }
                    });
                }
            };
        } ]);
}());