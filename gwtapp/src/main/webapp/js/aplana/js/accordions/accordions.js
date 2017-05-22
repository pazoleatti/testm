/**
 * aplana-accordions (Всплывающие панели)
 * Директива предназначена для отображения всплывающей панели
 * http://localhost:8080/#/aplana_accordions
 */
(function () {
    'use strict';
    angular.module('aplana.accordions', [ 'ngSanitize', 'aplana.utils' ])
        //
        .directive('aplanaAccordions', [ '$parse', '$timeout', 'AplanaUtils', function ($parse, $timeout, AplanaUtils) {
            return {
                restrict: 'A',
                scope: {
                    accordionsModel: '=',
                    closeOthers: '@'
                },
                templateUrl: AplanaUtils.templatePath + 'accordions/accordions.html',
                replace: true,
                link: function (scope, element, attrs) {
                    var getter = $parse(attrs.aplanaAccordions);
                    scope.ctrl = getter(scope.$parent) || {};
                    scope.accordionsModel = scope.accordionsModel || [];

                    scope.$getContent = function (accordion) {
                        if (angular.isDefined(accordion.content)) {
                            return "content";
                        } else if (angular.isDefined(accordion.htmlContent)) {
                            return "htmlContent";
                        } else {
                            return "contentUrl";
                        }
                    };

                    scope.ctrl.toggleGroup = function (_group, isOpen) {
                        if (isOpen) {
                            _group.isOpen = true;
                            _group.bodyElement.addClass("in");
                            _group.toggleElement.removeClass("collapsed");
                        } else {
                            _group.isOpen = false;
                            _group.bodyElement.removeClass("in");
                            _group.toggleElement.addClass("collapsed");
                        }
                    };

                    scope.ctrl.closeOthers = function (openGroup) {
                        if (scope.closeOthers === "true") {
                            angular.forEach(scope.accordionsModel, function (group) {
                                if (group !== openGroup) {
                                    scope.ctrl.toggleGroup(group, false);
                                }
                            });
                        }
                    };

                    function initAccordionGroups() {
                        angular.forEach(scope.accordionsModel, function (group) {
                            if (group.isOpen === true) {
                                scope.ctrl.closeOthers(group);
                            }
                        });
                    }

                    $timeout(function () {
                        initAccordionGroups();
                    }, 0);

                }
            };
        } ])
        //
        .directive('aplanaAccordionGroup', [ 'AplanaUtils', function (AplanaUtils) {
            return {
                restrict: 'A',
                scope: {
                    accordionGroupModel: '='
                },
                link: function (scope, element, attrs) {

                    var bodyElement = element.find('div.accordion-body');
                    var toggleElement = element.find('a.accordion-toggle');
                    var accordionGroup = scope.accordionGroupModel;
                    accordionGroup.bodyElement = bodyElement;
                    accordionGroup.toggleElement = toggleElement;

                    var parentScope = scope.$parent;
                    parentScope.ctrl.toggleGroup(accordionGroup, accordionGroup.isOpen);

                    toggleElement.bind('click', function (event) {
                        var group = scope.accordionGroupModel;
                        if (group.isOpen === true) {
                            // закрыть если открыта
                            parentScope.ctrl.toggleGroup(group, false);
                        } else {
                            parentScope.ctrl.toggleGroup(group, true);
                            parentScope.ctrl.closeOthers(group);
                        }
                    });
                }
            };
        } ]);
}());