/**
 * $aplanaModal (Модальные окна)
 * Сервис предназначен для отображения модального окна с настраиваемым содержимым.
 * Модуль взят из ui-bootstrap 0.6.0
 * http://localhost:8080/#/aplana_modal
 */
angular.module('aplana.modal', ['aplana.utils'])
/**
 * A helper directive for the $modal service. It creates a backdrop element.
 */
    .directive('modalBackdrop', ['$modalStack', '$timeout', 'AplanaUtils', function ($modalStack, $timeout, AplanaUtils) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: AplanaUtils.templatePath + 'modal/backdrop.html',
            link: function (scope, element, attrs) {

                //trigger CSS transitions
                $timeout(function () {
                    scope.animate = true;
                });

                scope.close = function (evt) {
                    var modal = $modalStack.getTop();
                    if (modal && modal.value.backdrop && modal.value.backdrop !== 'static') {
                        evt.preventDefault();
                        evt.stopPropagation();
                        $modalStack.dismiss(modal.key, 'backdrop click');
                    }
                };
            }
        };
    }])

    .factory('$modalStack', ['$document', '$compile', '$rootScope', '$$stackedMap', '$templateCache', '$http', 'AplanaUtils', '$timeout',
        function ($document, $compile, $rootScope, $$stackedMap, $templateCache, $http, AplanaUtils, $timeout) {

            var backdropDomEl, backdropScope;
            var openedWindows = $$stackedMap.createNew();
            var $modalStack = {};

            function backdropIndex() {
                var topBackdropIndex = -1;
                var opened = openedWindows.keys();
                for (var i = 0; i < opened.length; i++) {
                    if (openedWindows.get(opened[i]).value.backdrop) {
                        topBackdropIndex = i;
                    }
                }
                return topBackdropIndex;
            }

            $rootScope.$watch(backdropIndex, function (newBackdropIndex) {
                if (backdropScope) {
                    backdropScope.index = newBackdropIndex;
                }
            });

            function removeModalWindow(modalInstance) {

                var modalWindow = openedWindows.get(modalInstance).value;

                //clean up the stack
                openedWindows.remove(modalInstance);

                //remove window DOM element
                modalWindow.modalDomEl.remove();

                //remove backdrop if no longer needed
                if ((backdropIndex() === -1) && angular.isDefined(backdropDomEl)) {
                    backdropDomEl.remove();
                    backdropDomEl = undefined;

                    backdropScope.$destroy();
                    backdropScope = undefined;
                }

                //destroy scope
                modalWindow.modalScope.$destroy();
            }

            function dismissTopModal() {
                $timeout(function() {
                    var modal = openedWindows.top();
                    if (modal && modal.value.keyboard) {
                        $rootScope.$apply(function () {
                            modal.value.modalScope.modalCloseCallback();
                        });
                    }
                });
            }

            $document.bind('keydown', function (evt) {
                if (evt.which === 27) {
                    dismissTopModal();
                }

                if (evt.which === 8 && !($(evt.target).is('input') || $(evt.target).is('textarea'))) {
                    evt.preventDefault();
                }
            });

            $rootScope.$on('$locationChangeSuccess', function (event, newUrl, oldUrl) {
                dismissTopModal();
            });

            $modalStack.clearFormLeaveListeners = function(modalInstance) {
                if (modalInstance.clearFormLeaveListeners) {
                    modalInstance.clearFormLeaveListeners();
                }
            };

            $modalStack.open = function (modalInstance, modal) {

                openedWindows.add(modalInstance, {
                    deferred: modal.deferred,
                    modalScope: modal.scope,
                    backdrop: modal.backdrop,
                    keyboard: modal.keyboard
                });
                modal.scope.index = openedWindows.length() - 1;
                var openedWindow = openedWindows.top();

                var body = $document.find('body').eq(0), currBackdropIndex = backdropIndex();

                /**
                 * Функция растягивает модальное окно по вертикали
                 */
                function expandModalToWindow(modalDomElement) {
                    // управление смещением по вертикали
                    var visibleHeight = $(window).height();

                    var modalHeader = modalDomElement.find('div.modal-header');
                    var headerHeight = modalHeader.outerHeight(true);

                    var modalFooter = modalDomElement.find('div.modal-footer');
                    var footerHeight = modalFooter.outerHeight(true);

                    var verticalModalOffest = 40;
                    var maxModalBodyHeightThreshold = 46;
                    var isExceededThreshold = false;

                    var modalBody = modalDomElement.find('div.modal-body');

                    $(modalBody).scroll(function () {
                        $rootScope.$broadcast('WINDOW_SCROLLED_MSG');
                    });

                    $(modalBody).resize(function () {
                        $rootScope.$broadcast('WINDOW_RESIZED_MSG');
                    });


                    // задаем максимальную высоту контейнера окна
                    if (angular.isUndefined(modalBody.css("max-height"))) {
                        modalBody.css("max-height", function () {
                            var topP = parseInt(modalBody.css("padding-top"), 10);
                            var bottomP = parseInt(modalBody.css("padding-bottom"), 10);
                            var paddings = topP + bottomP;
                            var maxModalBodyHeight = visibleHeight - headerHeight - footerHeight - paddings - 2 * verticalModalOffest;
                            // максимальная высота меньше порога
                            if (maxModalBodyHeight < maxModalBodyHeightThreshold) {
                                maxModalBodyHeight = maxModalBodyHeightThreshold;
                                isExceededThreshold = true;
                            }

                            return maxModalBodyHeight;
                        });
                    }
                    $timeout(function () {
                        var dragging = false;
                        var mouseStartPos = {};
                        var modalStartPos = {};
                        modalHeader.on('mousedown', function (e) {
                            dragging = true;
                            mouseStartPos.x = e.clientX;
                            mouseStartPos.y = e.clientY;
                            modalStartPos.left = modalDomElement[0].offsetLeft;
                            modalStartPos.top = modalDomElement[0].offsetTop;
                            e.preventDefault();
                        });
                        $document.on('mouseup', function (e) {
                            dragging = false;
                            e.preventDefault();
                        });
                        $document.on('mousemove', function (e) {
                            if (dragging) {
                                modalDomElement.css("left", (modalStartPos.left - (mouseStartPos.x - e.clientX)) + "px");
                                modalDomElement.css("top", (modalStartPos.top - (mouseStartPos.y - e.clientY)) + "px");
                                e.preventDefault();
                            }
                        });

                        //modalDomElement.draggable({handle: ".modal-header", containment: "document", cursor: 'move'});
                        modalDomElement.css("left", ($(document).width() - modalDomElement.width()) / 2);
                        modalDomElement.css("top", ($(document).height() - modalDomElement.height()) / 2);
                        modalDomElement.css("display", "block");
                    }, 0);
                }

                $http.get(AplanaUtils.templatePath + 'modal/window.html', {cache: $templateCache}).then(function (result) {
                    var angularDomEl = angular.element(result.data);
                    angularDomEl.attr('window-class', modal.windowClass);
                    angularDomEl.append(modal.content);

                    var modalDomEl = $compile(angularDomEl)(modal.scope);
                    openedWindow.value.modalDomEl = modalDomEl;
                    body.append(modalDomEl);

                    if (currBackdropIndex >= 0 && !backdropDomEl) {
                        backdropScope = $rootScope.$new(true);
                        backdropScope.index = currBackdropIndex;
                        backdropScope.onTop = modal.scope.onTop;
                        backdropDomEl = $compile('<div modal-backdrop></div>')(backdropScope);
                        body.append(backdropDomEl);
                    }

                    expandModalToWindow(modalDomEl);

                });

            };

            $modalStack.close = function (modalInstance, result) {
                var modal = openedWindows.get(modalInstance);
                if (modal) {
                    modal.value.deferred.resolve(result);
                    removeModalWindow(modalInstance);
                }
            };

            $modalStack.dismiss = function (modalInstance, reason) {
                var modalWindow = openedWindows.get(modalInstance).value;
                if (modalWindow) {
                    modalWindow.deferred.reject(reason);
                    removeModalWindow(modalInstance);
                }
            };

            $modalStack.getTop = function () {
                return openedWindows.top();
            };

            return $modalStack;
        }])

    .provider('$aplanaModal', function () {

        var $modalProvider = {
            options: {
                backdrop: 'static', //can be also false or 'static'
                keyboard: true
            },
            $get: ['$injector', '$rootScope', '$q', '$http', '$templateCache', '$controller', '$modalStack',
                function ($injector, $rootScope, $q, $http, $templateCache, $controller, $modalStack) {

                    var $modal = {};

                    function getTemplatePromise(options) {
                        return options.template ? $q.when(options.template) :
                            $http.get(options.templateUrl, {cache: $templateCache}).then(function (result) {
                                return result.data;
                            });
                    }

                    function getResolvePromises(resolves) {
                        var promisesArr = [];
                        angular.forEach(resolves, function (value, key) {
                            if (angular.isFunction(value) || angular.isArray(value)) {
                                promisesArr.push($q.when($injector.invoke(value)));
                            }
                        });
                        return promisesArr;
                    }

                    $modal.open = function (modalOptions) {
                        var modalResultDeferred = $q.defer();
                        var modalOpenedDeferred = $q.defer();

                        //prepare an instance of a modal to be injected into controllers and returned to a caller
                        var modalInstance = {
                            result: modalResultDeferred.promise,
                            opened: modalOpenedDeferred.promise,
                            close: function (result) {
                                $modalStack.close(modalInstance, result);
                            },
                            dismiss: function (reason) {
                                $modalStack.dismiss(modalInstance, reason);
                            }
                        };

                        //merge and clean up options
                        modalOptions = angular.extend({}, $modalProvider.options, modalOptions);
                        modalOptions.resolve = modalOptions.resolve || {};

                        //verify options
                        if (!modalOptions.template && !modalOptions.templateUrl) {
                            throw new Error('One of template or templateUrl options is required.');
                        }

                        var templateAndResolvePromise =
                            $q.all([getTemplatePromise(modalOptions)].concat(getResolvePromises(modalOptions.resolve)));


                        templateAndResolvePromise.then(function resolveSuccess(tplAndVars) {

                            var modalScope = (modalOptions.scope || $rootScope).$new();
                            modalInstance.updateTitle = function (title) {
                                modalScope.title = title;
                            };
                            modalScope.$close = modalInstance.close;
                            modalScope.$dismiss = modalInstance.dismiss;
                            modalScope.title = modalOptions.title;
                            modalScope.titleIcon = modalOptions.titleIcon;
                            modalScope.modalHeaderClass = modalOptions.modalHeaderClass;
                            modalScope.onTop = modalOptions.onTop;
                            modalScope.modalCloseCallback = function () {
                                if (angular.isFunction(modalOptions.closeCallback)) {
                                    if (modalOptions.closeCallback(modalScope)) {
                                        modalInstance.dismiss();
                                    }
                                } else {
                                    modalInstance.dismiss();
                                }
                            };

                            var ctrlInstance, ctrlLocals = {};
                            var resolveIter = 1;

                            //controllers
                            if (modalOptions.controller) {
                                ctrlLocals.$scope = modalScope;
                                ctrlLocals.$modalInstance = modalInstance;
                                angular.forEach(modalOptions.resolve, function (value, key) {
                                    ctrlLocals[key] = tplAndVars[resolveIter++];
                                });

                                ctrlInstance = $controller(modalOptions.controller, ctrlLocals);
                            }

                            if (modalOptions.windowClass) {
                                modalScope.windowClass = modalOptions.windowClass;
                            }

                            $modalStack.open(modalInstance, {
                                scope: modalScope,
                                deferred: modalResultDeferred,
                                content: tplAndVars[0],
                                backdrop: modalOptions.backdrop,
                                keyboard: modalOptions.keyboard
                            });

                        }, function resolveError(reason) {
                            modalResultDeferred.reject(reason);
                        });

                        templateAndResolvePromise.then(function () {
                            modalOpenedDeferred.resolve(true);
                        }, function () {
                            modalOpenedDeferred.reject(false);
                        });

                        return modalInstance;
                    };

                    return $modal;
                }]
        };

        return $modalProvider;
    });