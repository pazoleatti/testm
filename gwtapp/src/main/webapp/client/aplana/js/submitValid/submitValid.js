/**
 * aplana-submit-valid (Действие при отправке формы)
 * Директива предназначена для указания JavaScript функции, вызываемой при отправке формы
 * http://localhost:8080/#/aplana_submit_valid
 */
(function () {
    'use strict';

    angular.module('aplana.submitValid', ['aplana.overlay'])
        .directive('aplanaSubmitValid', ['$parse', '$timeout', '$overlayService', function ($parse, $timeout, $overlayService) {
            return {
                require: 'form',
                link: function (scope, formElement, attributes, form) {
                    var disabled_delay = 1000;
                    var setFormField = function (form, field, value) {
                        var formControllerPrototype = form.constructor.prototype;

                        form[field] = value;
                        for (var key in form) {
                            // angular 1.4.7 - добавилось значение $$parentForm и появляется stackoverflow
                            if (angular.isDefined(form[key]) && angular.isObject(form[key]) && formControllerPrototype.isPrototypeOf(form[key]) && key != '$$parentForm' ) {
                                setFormField(form[key], field, value);
                            }
                        }
                    };

                    form.setPristine = function () {
                        form.$setPristine();
                    };

                    form.setAttempt = function (value) {
                        setFormField(form, 'attempt', value);
                    };

                    setFormField(form, 'attempt', false);
                    setFormField(form, 'isHintHidden', true);

                    // При браузерном автокомплите никакие события не срабатывают - данные в ng-model не меняются
                    $('form[name="' + attributes.name + '"] input').on('blur click mouseover', function () {
                        $(this).change();
                    });

                    formElement.bind('submit', function (event) {
                        // Отправку формы можно инициировать несколько раз, если несколько раз нажать enter имеея в фокусе
                        // элемент ввода.
                        // Нужно заблокировать такое поведение и допускать только одну отправку формы за раз.
                        // Если формв отправляется - прервем обработку
                        var disableSubmit = formElement.attr("disableSubmit");
                        if (disableSubmit && disableSubmit === 'true') {
                            return false;
                        }
                        formElement.attr("disableSubmit", "true");

                        // Заблокируем все кнопки отправки
                        var buttons = $(formElement).find("button[type='submit']");

                        buttons.addClass("disabled");
                        buttons.attr('disabled', 'disabled');

                        // Как только обработка закончится - снимем флаг и блокировку кнопок
                        var unlockForm = function () {
                            $timeout(function () {
                                if ($overlayService.isActive()) {
                                    unlockForm();
                                    return;
                                }

                                formElement.removeAttr("disableSubmit");

                                buttons.removeClass("disabled");
                                buttons.removeAttr("disabled");
                            }, disabled_delay);
                        };

                        unlockForm();

                        setFormField(form, 'attempt', true);
                        setFormField(form, 'isHintHidden', true);

                        if (!scope.$$phase) {
                            scope.$apply();
                        }

                        var fn = $parse(attributes.aplanaSubmitValid);

                        if (form.$valid) {
                            if (!angular.isUndefined(fn)) {
                                var result;
                                scope.$apply(function () {
                                    result = fn(scope, {$event: event});
                                });
                                return result;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    });

                }
            };
        }]);
}());
