/**
 * aplana-field (Поле ввода)
 * Директива предназначена для создания обертки вокруг полей ввода любых типов
 * http://localhost:8080/#/aplana_field
 */
(function () {
    'use strict';

    angular.module('aplana.field', ['aplana.utils'])
        .directive('aplanaField', ['$compile', '$http', '$templateCache', '$interpolate', 'AplanaUtils', '$rootScope', '$timeout',
            function ($compile, $http, $templateCache, $interpolate, AplanaUtils, $rootScope, $timeout) {

                var findInputElement = function (element) {
                    return angular.element(
                        element.find('div[data-ui-select2]')[0] ||
                        element.find('div[data-aplana-grid]')[0] ||
                        element.find('div[data-aplana-treeview]')[0] ||
                        element.find('div[data-aplana-actualization-select]')[0] ||
                        element.find('div[data-aplana-datepicker-timepicker]')[0] ||
                        element.find('div[data-aplana-datepicker]')[0] ||
                        element.find('input')[0] || element.find('textarea')[0] || element.find('select')[0]
                    );
                };

                var findTranscludedElement = function (element) {
                    return angular.element(element.find('[data-aplana-transclude]')[0]);
                };

                return {
                    restrict: 'A',
                    priority: 100,        // We need this directive to happen before ng-model
                    terminal: true,       // We are going to deal with this element
                    require: '?^form',     // If we are in a form then we can access the ngModelController
                    compile: function compile(element) {

                        element[0].removeAttribute("data-aplana-field");
                        element[0].setAttribute("style", "width: 100%");


                        // Find all the [data-aplana-validator] child elements and extract their validation message info
                        var validationMessages = [];
                        angular.forEach(element.find('[data-aplana-validator]'), function (validatorElement) {
                            validatorElement = angular.element(validatorElement);
                            validationMessages.push({
                                key: validatorElement.data('key'),
                                getMessage: $interpolate(validatorElement.text()),
                                className: validatorElement[0].className
                            });
                            validatorElement.remove();
                        });

                        // Load up the template for this kind of field
                        var getFieldElement = function () {
                            return $http.get(AplanaUtils.templatePath + 'field/custom.html', {cache: $templateCache}).then(function (response) {
                                var newElement = angular.element(response.data);

                                angular.forEach(element[0].attributes, function (attribute) {
                                    var value = attribute.value;
                                    var key = attribute.name;
                                    newElement.attr(key, value);
                                });

                                return newElement;
                            });
                        };

                        /**
                         * @param {{modelId}} attrs
                         * @param {{hideName}} data
                         */
                        return function (scope, element, attrs, formController) {
                            // We have to wait for the field element template to be loaded
                            getFieldElement().then(function (newElement) {
                                    var labelAttr = attrs.label;
                                    var labelContent = "";
                                    if (angular.isDefined(labelAttr) && angular.isString(labelAttr)) {
                                        // Find the content that will go into the new label
                                        if (labelAttr.indexOf("{{") > -1) {
                                            // Выражение содержит {{}} - используем $interpolate
                                            labelContent = $interpolate(labelAttr)(scope);
                                        } else {
                                            // Иначе - попробуем использовать $eval
                                            try {
                                                labelContent = scope.$eval(labelAttr);
                                            } catch (e) {
                                                // $eval может выбросить ошибку - в этом случае просто вернем значение аттрибута
                                                labelContent = labelAttr;
                                            }
                                        }

                                        // Update the label's contents
                                        var labelElement = newElement.find('label');
                                        labelElement.html(labelContent);
                                    }

                                    // Our template will have its own child scope
                                    var childScope = scope.$new();

                                    // пропускает некоторые ошибки, если на них нет сообщений
                                    childScope.filterValidity = function (id) {
                                        var form = angular.isDefined(childScope.$form) ? childScope.$form[id] : undefined;
                                        if (form) {
                                            angular.forEach(['mask', 'parse'], function (key) {
                                                if (form.$error[key] && !childScope.$validationMessages[key]) {
                                                    form.$setValidity(key, true);
                                                }
                                            });
                                        }
                                        return true;
                                    };

                                    // Функция реализует "позднее связывание" между контроллерами ngModel поля ввода, формы и сообщений валидатора
                                    childScope.getFieldByModelId = function (id) {
                                        return angular.isDefined(childScope.$form) ? childScope.$form[id] : undefined;
                                    };

                                    childScope.hideHint = function (id) {
                                        childScope.$form.isHintHidden = false;
                                        childScope.$form[id].isHintHidden = true;
                                    };

                                    // TODO: Consider moving this validator stuff into its own directive
                                    // and use a directive controller to wire it all up
                                    // KSS попробовал реализовать через другую директиву - стокнулся с проблемой: не компилируются вложенные валидаторы при использовании атрибута ng-options из-за параметра terminal. Пока оставил так
                                    childScope.$validationMessages = {};
                                    angular.forEach(validationMessages, function (validationMessage) {
                                        // We need to watch incase it has interpolated values that need processing
                                        //  scope.$watch(validationMessage.getMessage, function (message) {
                                        // KSS 08.04.2014 Отключил возможность изменения содержимого сообщения валидатора в целях ускорения работы JS. На данный момент этот функционал нигде не используется
                                        childScope.$validationMessages[validationMessage.key] = {
                                            message: validationMessage.getMessage(scope),
                                            className: validationMessage.className
                                        };
                                    });

                                    //Вставляем контент в элемент шаблона с атрибутом data-aplana-transclude
                                    var transcludedElement = findTranscludedElement(newElement);
                                    if (AplanaUtils.isElement(transcludedElement)) {
                                        transcludedElement.replaceWith(element[0].innerHTML);
                                    } else {
                                        throw new Error('Не найден элемент с директивой data-aplana-transclude');
                                    }

                                    var hasAttribute = function (element, attrName) {
                                        return angular.isDefined(element.attr(attrName));
                                    };

                                    var inputElement = findInputElement(newElement);

                                    // Если в параметрах задан modelId - используем его
                                    if (angular.isDefined(attrs.modelId)) {
                                        childScope.$modelId = attrs.modelId.toLowerCase();


                                        if (!hasAttribute(inputElement, "name")) {
                                            inputElement.attr('name', childScope.$modelId);
                                        }
                                        // если у элемента нет id то вставляем сгенерированный
                                        if (!hasAttribute(inputElement, "id")) {
                                            inputElement.attr('id', childScope.$modelId);
                                        }
                                    } else {
                                        // Иначе - попытаемся сформировать сами

                                        // Generate an id for the input from the ng-model expression
                                        // (we need to replace dots with something to work with browsers and also form scope)
                                        // (We couldn't do this in the compile function as we need the scope to
                                        // be able to calculate the unique id)
                                        // Если у текущего элемента нет атрибута ngModel - попытаемся найти атрибут во вложенных элементах
                                        var ngModelAttr = attrs.ngModel || AplanaUtils.findNgModel(newElement);

                                        if (angular.isUndefined(ngModelAttr)) {
                                            throw new Error('Не найдена директива ngModel');
                                        }

                                        childScope.$modelId = AplanaUtils.buildModelId(ngModelAttr);//.toLowerCase();// + '_' + childScope.$id;

                                        // Wire up the input (id and name) and its label (for)
                                        // (We need to set the input element's name here before we compile.
                                        // If we leave it to interpolation, the formController doesn't pick it up)

                                        if ((attrs.hideName !== 'true') && !hasAttribute(inputElement, "name")) {
                                            inputElement.attr('name', childScope.$modelId);
                                        }
                                        // если у элемента нет id то вставляем сгенерированный
                                        if (!hasAttribute(inputElement, "id")) {
                                            inputElement.attr('id', childScope.$modelId);
                                        }

                                        newElement.find('label').attr('for', childScope.$modelId);
                                    }

                                    childScope.updateHintHideByKey = function (key) {
                                        var modelElement = childScope.$form[key];

                                        if (modelElement) {
                                            modelElement.isHintHidden = false;
                                        }
                                    };

                                    var activeElement = findInputElement(newElement);
                                    activeElement.attr('data-ng-click', "updateHintHideByKey('"+ $(activeElement).attr("name") + "')");

                                    // We must compile our new element in the postLink function rather than in the compile function
                                    // (i.e. after any parent form element has been linked)
                                    // otherwise the new input won't pick up the FormController
                                    $compile(newElement)(childScope, function (clone) {
                                        // Особенность: на этой строке вложенные директивы уже должны быть полностью инициализированы
                                        // Нельзя менять DOM структуру и набор атрибутов вложенных элементов внутри этой функции.
                                        // Также нельзя использовать асинхронные запросы в функциях compile вложенных директив -
                                        //  т.к. они выполнятся после выполнения компиляции данной директивы и не будут корректно встроены в общую структуру

                                        // Replace original element with our new element
                                        element.empty();  // replaceWith отрабатывал не так, как надо
                                        element.append(clone);
                                    });

                                    //Элемент подсказки
                                    var hint = element.find('div.help-bottom-popup');
                                    //Высота стрелки у подсказки
                                    var heightHelpArrow = hint.find('.help-top-arrow').height();
                                    var divControls = element.find('div.controls')[0];
                                    var fieldContent;

                                    //Отображается ли подсказка в модальном окне
                                    var isInModal = ($(hint).parents('.modal').length !== 0);
                                    var modalFooter; //Шапка модального окна
                                    var modalHeader; //Подвал модального окна
                                    if (isInModal) {
                                        modalFooter = $(hint).parents('.modal').find('.modal-footer');
                                        modalHeader = $(hint).parents('.modal').find('.modal-header');
                                    }
                                    var isInTab = ($(hint).parents('.tab-pane.ng-scope').length !== 0);
                                    var tabModel;
                                    if (isInTab) {
                                        tabModel = $(hint).parents('.tab-pane.ng-scope');
                                    }

                                    // управление позицией всплывающих подсказок
                                var updateHintPosition = function () {
                                    //Если мы оказались на невидимой вкладке - ничего не меняем
                                    if ($(divControls).is(":visible") && (!isInTab || tabModel.hasClass('active'))) {
                                        if (angular.isUndefined(fieldContent) && ($(divControls).find("div.field-content").length !== 0)) {
                                            fieldContent = $(divControls).find("div.field-content")[0];
                                        }

                                        var bounds;
                                        if (angular.isUndefined(fieldContent)) {
                                            try {
                                                // Bug IE: вызов getBoundingClientRect на не добавленном объекте
                                                bounds = divControls.getBoundingClientRect();
                                            } catch (e) {
                                                return;
                                            }
                                        } else {
                                            bounds = fieldContent.getBoundingClientRect();
                                        }

                                        //Если у нашего контейнера нулевые размеры - значит он невидимый и мы
                                        // позиционируем ошибку далеко, где ее не видно
                                        // Игнорируем, если объект в коллапсе, т.к. при его сворачивании
                                        // ошибку не видно без переноса
                                        if ((bounds.top === 0) && (bounds.left === 0) ||
                                            angular.isUndefined(bounds.top) || angular.isUndefined(bounds.left)) {

                                            hint.css('position', 'fixed');
                                            hint.css('left', 2000);
                                            hint.css('top', 0);
                                        } else {
                                            // Bug в IE8: bounds.height = undefined, поэтому высоту элемента будем
                                            // вычислять по формуле: bounds.bottom - bounds.top
                                            var height = (bounds.height) ? bounds.height :
                                                (bounds.bottom ? bounds.bottom - bounds.top : 0);
                                            //hint.css('position', 'fixed');
                                            //hint.css('left', bounds.left);

                                            //Если модальное окно, то проверить выход за границы
                                            if (isInModal) {
                                                // Значение скролла страницы
                                                var scrollTop = -$(window).scrollTop();

                                                //Если выходит за нижниюю границу контента модального окна, то hint там и оставляем
                                                if (modalFooter.offset().top + scrollTop <= bounds.top + height) {
                                                    /*hint.css('top', modalFooter.offset().top + scrollTop);*/
                                                } else {
                                                    //Если выходит за верхнюю границу контента модального окна, то hint там и оставляем
                                                    var maxTop = modalHeader.offset().top + modalHeader.height() +
                                                        parseInt(modalHeader.css('padding-top'), 10) + parseInt(modalHeader.css('padding-bottom'), 10) + heightHelpArrow + scrollTop;
                                                    if (maxTop >= bounds.top + height) {
                                                        //Вычисляем место прижатия hint-а: начало шапки модального окна + высота шапки + отступы шапки сверху и снихзу + высота стрелки-подсказки
                                                        /*hint.css('top', maxTop);*/
                                                    } else {
                                                        /*hint.css('top', bounds.top + height);*/
                                                    }
                                                }
                                            } else {
                                                /* hint.css('top', bounds.top + height);*/
                                            }
                                        }
                                    }
                                };

                                    var updateHintHide = function () {
                                        for (var key in childScope.$form) {
                                            if (childScope.$form.hasOwnProperty(key)) {
                                                var modelElement = childScope.$form[key];
                                                // Выбираем из всех полей $form только те, которые похожи на модель angular по признаку наличия функции $isEmpty
                                                if (angular.isObject(modelElement) && angular.isFunction(modelElement.$isEmpty)) {
                                                    modelElement.isHintHidden = false;
                                                }
                                            }
                                        }
                                    };



                                    scope.$on('WINDOW_RESIZED_MSG', function () {
                                        $timeout(function () {
                                            updateHintPosition();
                                        }, 0);
                                    });

                                    scope.$on('WINDOW_SCROLLED_MSG', function () {
                                        $timeout(function () {
                                            updateHintPosition();
                                        }, 0);
                                    });

                                    scope.$on('UPDATE_VALIDATION_MESSAGE_POSITION', function () {
                                        $timeout(function () {
                                            updateHintPosition();
                                        }, 0);
                                    });

                                    scope.$on('TAB_CHANGED_MSG', function () {
                                        $timeout(function () {
                                            updateHintPosition();
                                        }, 0);
                                    });

                                    scope.$on('GRID_CHANGED_MSG', function () {
                                        $timeout(function () {
                                            updateHintPosition();
                                        }, 0);
                                    });

                                    var getModelErrors = function () {
                                        if (childScope.getFieldByModelId(childScope.$modelId)) {
                                            return childScope.getFieldByModelId(childScope.$modelId).$error;
                                        } else {
                                            return undefined;
                                        }
                                    };

                                    AplanaUtils.watchCollection(childScope, getModelErrors, function () {
                                        updateHintPosition();
                                    });

                                    // Only after the new element has been compiled do we have access to the ngModelController
                                    // (i.e. formController[childScope.name])
                                    if (formController) {
                                        childScope.$form = formController;
                                        childScope.$watch('$form.attempt', function () {
                                                updateHintPosition();
                                            }
                                        );
                                        childScope.$watch('$form.isHintHidden', function (newValue, oldValue) {
                                                // Если валидаторы формы показывались, а теперь должны быть скрыты - обновим их флаги скрытия функцией updateHintHide (SBRFEDOFNS-1822)
                                                if (!oldValue && newValue) {
                                                    updateHintHide();
                                                }
                                            }
                                        );
                                    }
                                    updateHintPosition();
                                }
                            );
                        };
                    }
                };
            }]);
}());
