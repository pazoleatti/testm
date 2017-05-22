/**
 * aplana-tabs (Вкладки с настраиваемым содержимым)
 * Директива предназначена для отображения вкладок с настраиваемым содержимым
 * http://localhost:8080/#/aplana_tabs
 */
(function () {
    'use strict';

    angular.module('aplana.tabs', [ 'ngSanitize', 'aplana.utils'])
        // tabs controller
        .controller('SingleLineTabsCtrl', [ '$scope', '$attrs', function (scope, $attrs) {
            function calculateFitTabOnWindow(_element, _containerUlWidth, _sumWidth, _tab, _tabIndex, _tabsLength) {
                var sw = _sumWidth;

                // перед вычислением ширины элемента сделаем видимым элемент т.е.
                // как бы очищаем состояние перед вычислением notFitOnScreen свойства,
                // которое управляет видимостью
                _element.css("cssText", "display: inline !important");
                var elOuterWidth = _element.outerWidth();
                _element.css("cssText", "");

                sw = sw + elOuterWidth;

                if (sw >= _containerUlWidth) {
                    // помечаем что вкладка не помещается на экран
                    _tab.notFitOnScreen = true;
                } else {
                    _tab.notFitOnScreen = false;
                }
                return sw;
            }

            /*
             * Константные элементы
             */
            scope.initConstantElements = function (_elementTabs) {
                scope.tabsUl = _elementTabs.find("ul.nav-tabs");
                scope.tabsControl = _elementTabs.find("ul.nav-tabs li.cbr-tabsControl");
                scope.notFitOnScreenTabsContainer = _elementTabs.find("ul.nav-tabs li.cbr-notFitOnScreenTabs");
            };

            /*
             * суммарная (накопительная) ширина закладок.
             * Переменную необходимо хранить в scope для того чтобы сохранять ее значение между вызовами
             * функции scope.fitTabOnWindow из дочерней директивы aplanaTabElement.
             */
            scope.sumWidth = 0;

            /*
             * Флаг устанавливается в TRUE если активная вкладка не помещается на экран.
             */
            scope.activeTabIsNotFitOnWindow = false;

            scope.fitTabOnWindow = function (_element, _tab, _tabIndex) {
                if (_tabIndex === 0) {
                    // инициализируем общие переменные для закладок
                    scope.activeTabIsNotFitOnWindow = false;
                    scope.showNotFitOnScreenTabsContainer = false;
                    scope.containerUlWidth = scope.tabsUl.outerWidth();

                    var leftOffset = scope.tabsControl.outerWidth();
                    var rightOffset = scope.notFitOnScreenTabsContainer.outerWidth();
                    // суммарный отступ контейнера справа и слева
                    scope.sumWidth = leftOffset + rightOffset;
                }
                scope.sumWidth = calculateFitTabOnWindow(_element, scope.containerUlWidth, scope.sumWidth, _tab, _tabIndex, scope.tabModel.length);

                if (_tab.notFitOnScreen && _tab.active) {
                    scope.activeTabIsNotFitOnWindow = true;
                }
                if (_tab.notFitOnScreen) {
                    scope.showNotFitOnScreenTabsContainer = true;
                }
            };

            scope.updateFitTabsOnWindow = function () {
                angular.forEach(scope.tabModel, function (item, index) {
                    var el = angular.element(item.elementLi);
                    scope.fitTabOnWindow(el, item, index, scope.tabModel.length);
                });
            };
        } ])
        // tabs
        .directive('aplanaTabs', [ '$parse', '$gridStack', 'AplanaUtils', function ($parse, $gridStack, AplanaUtils) {

            return {
                restrict: 'A',
                controller: 'SingleLineTabsCtrl',
                scope: true,
                transclude: true,
                templateUrl: AplanaUtils.templatePath + 'tabs/tabs.html',
                replace: true,
                link: function (scope, iElement, iAttrs) {
                    scope.$on('$destroy', function () {
                        $(window).off('resize', scope.resizeHandler);

                        scope.tabsUl.remove();
                        scope.tabsUl = undefined;

                        scope.tabsControl.remove();
                        scope.tabsControl = undefined;

                        scope.notFitOnScreenTabsContainer.remove();
                        scope.notFitOnScreenTabsContainer = undefined;

                        iElement.remove();
                        iElement = undefined;

                        scope.tabModel = [];
                        scope.tabButton = undefined;
                        scope.ctrl = undefined;
                        scope = undefined;
                    });

                    var getter = $parse(iAttrs.aplanaTabs), setter = getter.assign, value = getter(scope);
                    scope.ctrl = getter(scope.$parent) || {};
                    scope.tabModel = scope.$parent[iAttrs.tabModel] || [];
                    scope.tabButton = scope.$parent[iAttrs.tabButton];
                    scope.warningKeys = scope.$parent[iAttrs.warningKeys];

                    scope.initConstantElements(iElement);
                    scope.resizeHandler = function () {
                        scope.updateFitTabsOnWindow();
                        scope.$apply();
                    };
                    $(window).resize(scope.resizeHandler);

                    scope.$getContent = function (tab) {
                        if (angular.isDefined(tab.content)) {
                            return "content";
                        } else if (angular.isDefined(tab.htmlContent)) {
                            return "htmlContent";
                        } else if (tab.active || tab.fetchTab || tab.$fetched) {
                            tab.$fetched = true;
                            return "contentUrl";
                        } else {
                            return null;
                        }
                    };

                    scope.ctrl.getTabsWithErrors = function () {
                        var tabsWithErrors = [];

                        angular.forEach(scope.tabModel, function (tab) {
                            if (angular.isFunction(tab.$hasErrors) && tab.$hasErrors()) {
                                tabsWithErrors.push(tab);
                            }
                        });

                        return tabsWithErrors;
                    };

                    scope.ctrl.getTabsWithWarnings = function () {
                        var tabsWithWarnings = [];

                        angular.forEach(scope.tabModel, function (tab) {
                            if (angular.isFunction(tab.$hasWarnings) && tab.$hasWarnings()) {
                                tabsWithWarnings.push(tab);
                            }
                        });

                        return tabsWithWarnings;
                    };

                    scope.ctrl.isTabExists = function ($index) {
                        return angular.isDefined(scope.tabModel[$index]);
                    };

                    scope.$watch("ctrl.getTabsWithWarnings().length", function (newValue, oldValue) {
                        scope.$broadcast("UPDATE_VALIDATION_MESSAGE_POSITION");
                    });
                    scope.$watch("ctrl.getTabsWithErrors().length", function (newValue, oldValue) {
                        scope.$broadcast("UPDATE_VALIDATION_MESSAGE_POSITION");
                    });

                    scope.ctrl.activateTabByIndex = function ($index) {
                        var ind = parseInt($index, 10);
                        if (!scope.ctrl.isTabExists(ind)) {
                            return;
                        }
                        angular.forEach(scope.tabModel, function (item, index) {
                            scope.tabModel[index].active = index === ind;
                        });

                        scope.updateFitTabsOnWindow();
                        $gridStack.adjustAllGridsWidth();

                        scope.$broadcast('TAB_CHANGED_MSG');
                    };

                    scope.ctrl.getActiveTab = function () {
                        angular.forEach(scope.tabModel, function (item, index) {
                            if (scope.tabModel[index].active) {
                                return scope.tabModel[index];
                            }
                            return null;
                        });
                    };

                    scope.ctrl.removeTabByIndex = function ($index) {
                        if (!scope.ctrl.isTabExists($index)) {
                            return;
                        }
                        var tab = scope.tabModel[$index];
                        var wasActive = tab.active;
                        scope.tabModel.splice($index, 1);
                        if (wasActive) {
                            scope.ctrl.activateTabByIndex($index >= scope.tabModel.length ? scope.tabModel.length - 1 : $index);
                        }
                        scope.updateFitTabsOnWindow();
                    };

                    scope.ctrl.closeTabByIndex = function ($index) {
                        var ind = parseInt($index, 10);
                        if (!scope.ctrl.isTabExists(ind)) {
                            return;
                        }
                        var tab = scope.tabModel[ind];
                        if (angular.isFunction(tab.close)) {
                            tab.close(ind);
                        } else if (tab.close) {
                            scope.ctrl.removeTabByIndex(ind);
                        }
                    };

                    scope.ctrl.getTabByIndex = function ($index) {
                        return iElement.find('div.tab-pane:eq(' + $index + ')');
                    };

                    scope.ctrl.createTab = function (config) {
                        scope.tabModel.push(config);
                    };

                }
            };
        } ])

        // tabs url
        .directive('aplanaTabsUrl',
            [ '$parse', 'AplanaUtils', '$location', '$q', '$window', function ($parse, AplanaUtils, $location, $q, $window) {

                return {
                    restrict: 'A',
                    controller: 'SingleLineTabsCtrl',
                    scope: true,
                    transclude: true,
                    templateUrl: AplanaUtils.templatePath + 'tabsUrl/tabsUrl.html',

                    link: function (scope, iElement, iAttrs, singleLineTabsCtrl) {
                        scope.$on('$destroy', function () {
                            $(window).off('resize', scope.resizeHandler);
                            if (!angular.isUndefined(scope.$lastTabWatcher)) {
                                scope.$lastTabWatcher();
                            }

                            scope.tabsUl.remove();
                            scope.tabsUl = undefined;

                            scope.tabsControl.remove();
                            scope.tabsControl = undefined;

                            scope.notFitOnScreenTabsContainer.remove();
                            scope.notFitOnScreenTabsContainer = undefined;

                            scope.currentTab = undefined;
                            scope.ctrl = undefined;
                            scope.tabModel = undefined;
                            scope.tabButton = undefined;

                            iElement.remove();
                            iElement = undefined;

                            scope = undefined;
                        });

                        var getter = $parse(iAttrs.aplanaTabsUrl), setter = getter.assign, value = getter(scope);

                        scope.ctrl = getter(scope.$parent) || {};
                        scope.tabModel = scope.$parent[iAttrs.tabModel] || [];
                        scope.tabButton = scope.$parent[iAttrs.tabButton];
                        scope.tabLockLast = angular.isDefined(iAttrs.tabLockLast);

                        //Отслеживаем количество вкладок, если остаётся одна, то убираем возможность закрытие этой вкладки
                        if (scope.tabLockLast === true) {
                            scope.$lastTabWatcher = scope.$watch('tabModel.length', function () {
                                if (scope.tabModel.length === 1) {
                                    scope.tabModel[0].close = false;
                                } else {
                                    scope.tabModel.forEach(function (item) {
                                        item.close = true;
                                    });
                                }
                            });
                        }

                        scope.initConstantElements(iElement);

                        // инициализируем листенеры
                        //В осле есть баг: http://stackoverflow.com/questions/1852751/window-resize-event-firing-in-internet-explorer
                        //он кастует ресайз окна на каждый чих, даже если онкно свой размер не меняло, нужно самим следить за его размерами
                        var lastWindow = {};
                        angular.element($window).resize(function () {
                            // В IE8 $window.innerHeight = undefined. Приходится брать angular.element($window)
                            var windowElement = angular.element($window);
                            if (angular.isUndefined(lastWindow.width) || angular.isUndefined(lastWindow.height) ||
                                (lastWindow.width !== windowElement.innerWidth()) || (lastWindow.height !== windowElement.innerHeight())) {

                                scope.updateFitTabsOnWindow();
                                scope.$apply();

                                lastWindow = {
                                    width: windowElement.innerWidth(),
                                    height: windowElement.innerHeight()
                                };
                            }
                        });

                        scope.$getContent = function (tab) {
                            if (angular.isDefined(tab.content)) {
                                return "content";
                            } else if (angular.isDefined(tab.htmlContent)) {
                                return "htmlContent";
                            } else {
                                return "contentUrl";
                            }
                        };

                        scope.ctrl.isTabExists = function ($index) {
                            return angular.isDefined(scope.tabModel[$index]);
                        };

                        scope.ctrl.isTabExistsWithUrl = function (url) {
                            var res = false;
                            angular.forEach(scope.tabModel, function (item, index) {
                                res |= item.url === url;
                            });
                            return res;
                        };

                        scope.ctrl.getActiveTab = function () {
                            angular.forEach(scope.tabModel, function (item, index) {
                                if (scope.tabModel[index].active) {
                                    return scope.tabModel[index];
                                }
                                return null;
                            });
                        };

                        scope.ctrl.removeTabByIndex = function ($index) {
                            if (!scope.ctrl.isTabExists($index) || scope.tabModel.length === 1) {
                                return;
                            }
                            var wasActive = scope.tabModel[$index].active;
                            scope.tabModel.splice($index, 1);
                            if (wasActive) {
                                scope.ctrl.activateTabByIndex($index >= scope.tabModel.length ? scope.tabModel.length - 1 : $index);
                            }
                            scope.updateFitTabsOnWindow();
                        };

                        scope.ctrl.closeTabByIndex = function ($index) {
                            if (!scope.ctrl.isTabExists($index) || scope.tabModel.length === 1) {
                                return;
                            }
                            var tab = scope.tabModel[$index];

                            var callEvent = function () {
                                if (angular.isFunction(tab.closeEvent)) {
                                    return tab.closeEvent({
                                        url: tab.url,
                                        open: false
                                    });
                                }
                            };

                            $q.when(callEvent()).then(
                                function () {
                                    if (angular.isFunction(tab.close)) {
                                        tab.close($index);
                                    } else if (tab.close) {
                                        scope.ctrl.removeTabByIndex($index);
                                    }
                                }
                            );
                        };

                        scope.ctrl.activateTabByUrl = function ($url) {
                            $location.path($url);
                        };

                        scope.ctrl.activateTab = function (tab) {
                            scope.ctrl.activateTabByUrl(tab.url);
                        };

                        scope.ctrl.activateTabByIndex = function ($index) {
                            var tab = scope.tabModel[$index];
                            if (!angular.isDefined(tab)) {
                                return;
                            }
                            scope.ctrl.activateTab(tab);
                        };

                        /**
                         * Приватный метод поиска табы по URL
                         * @private
                         * @param $url адрес страницы
                         */
                        var findTabByUrl = function ($url) {
                            var res = {};
                            angular.forEach(scope.tabModel, function (item, index) {
                                if (item.url === $url) {
                                    res = scope.tabModel[index];
                                } else {
                                    scope.tabModel[index].active = false;
                                }
                            });
                            return res;
                        };

                        scope.ctrl.createTab = function (config) {
                            if (!scope.ctrl.isTabExistsWithUrl(config.url)) {
                                if (angular.isFunction(config.closeEvent)) {
                                    config.closeEvent({
                                        url: config.url,
                                        open: true
                                    });
                                }
                                scope.tabModel.push(config);
                                scope.ctrl.activateTabByIndex(scope.tabModel.length - 1);
                            } else {
                                scope.ctrl.activateTabByUrl(config.url);
                            }
                        };

                        scope.currentTab = findTabByUrl(scope.ctrl.tabUrl);
                        scope.currentTab.active = true;
                    }
                };
            } ])

        // tab элемент li
        .directive('aplanaTabElement', [ 'AplanaUtils', '$timeout', function (AplanaUtils, $timeout) {
            return {
                restrict: 'A',
                replace: false,
                scope: {
                    tabsLength: '@',
                    tabIndex: '@',
                    warningKeys: '=',
                    tab: '='
                },
                link: function (scope, element, attrs) {
                    scope.$on('$destroy', function () {
                        resizeWatcher();
                        resizeWatcher = undefined;

                        titleSpan.remove();
                        titleSpan = undefined;

                        element.remove();
                        element = undefined;

                        scope = undefined;
                    });

                    var titleSpan = element.find("a span");

                    var tab = scope.tab;
                    // запоминаем ссылку на LI
                    tab.elementLi = element[0];
                    var resizeWatcher = scope.$watch(function () {
                        return titleSpan;
                    }, function (heading) {
                        if (heading) {
                            var tabsLength = parseInt(scope.tabsLength, 10);
                            var tabIndex = parseInt(scope.tabIndex, 10);
                            scope.$parent.fitTabOnWindow(element, tab, tabIndex, tabsLength);
                        }
                    });

                    tab.$hasErrors = function () {
                        var valid = true;

                        if (angular.isFunction(tab.form)) {
                            var form = tab.form();

                            var errors = [];
                            if (!!form) {
                                var formErrors = _.filter(_.keys(form.$error), function (key) {
                                    return !!form.$error[key];
                                });
                                errors = _.filter(formErrors, function (key) {
                                    return !_.contains(scope.warningKeys, key);
                                });
                            }

                            var hasErrors = errors.length !== 0;
                            valid = !(!!form && hasErrors && form.attempt);
                        }

                        return !valid;
                    };

                    tab.$hasWarnings = function () {
                        var valid = true;

                        if (angular.isFunction(tab.form)) {
                            var form = tab.form();

                            var warnings = [];
                            if (!!form) {
                                var formErrors = _.filter(_.keys(form.$error), function (key) {
                                    return !!form.$error[key];
                                });
                                warnings = _.filter(formErrors, function (key) {
                                    return _.contains(scope.warningKeys, key);
                                });
                            }


                            var hasWarnings = warnings.length !== 0;
                            valid = !(!!form && hasWarnings && form.attempt);
                        }

                        return !valid;
                    };

                    tab.getTitle = function () {
                        return angular.isFunction(tab.title) ? tab.title() : tab.title;
                    };
                }
            };
        } ]);

}());