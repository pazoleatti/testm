/**
 * Ангуляровская обертка над select2 Скоприровано из https://github.com/angular-ui/ui-select2 Версия 0.0.5
 */
(function () {
    'use strict';

    /**
     * Enhanced Select2 Dropmenus
     *
     * @AJAX Mode - When in this mode, your value will be an object (or array of objects) of the data used by Select2
     *     This change is so that you do not have to do an additional query yourself on top of Select2's own query
     * @params [options] {object} The configuration options passed to $.fn.select2(). Refer to the documentation
     */
    angular.module('ui.select2', []).value('uiSelect2Config', {}).directive('uiSelect2', ['uiSelect2Config', '$timeout', 'AplanaUtils', function (uiSelect2Config, $timeout, AplanaUtils) {
        var options = {};
        if (uiSelect2Config) {
            angular.extend(options, uiSelect2Config);
        }
        return {
            require: 'ngModel',
            priority: 1,
            compile: function (tElm, tAttrs) {
                var watch,
                    repeatOption,
                    repeatAttr,
                    isSelect = tElm.is('select'),
                    isMultiple = angular.isDefined(tAttrs.multiple);

                // Enable watching of the options dataset if in use
                if (tElm.is('select')) {
                    repeatOption = tElm.find('option[ng-repeat], option[data-ng-repeat]');

                    if (repeatOption.length) {
                        repeatAttr = repeatOption.attr('ng-repeat') || repeatOption.attr('data-ng-repeat');
                        watch = jQuery.trim(repeatAttr.split('|')[0]).split(' ').pop();
                    }
                }

                return function (scope, elm, attrs, controller) {
                    // instance-specific options
                    var opts = angular.extend({}, options, scope.$eval(attrs.uiSelect2));

                    if (angular.isDefined(opts.data)) {
                        if (!angular.isDefined(opts.preFormatResult)) {
                            opts.preFormatResult = opts.formatResult;
                            opts.formatResult = function (data) {
                                data = AplanaUtils.sanitizeRecursively(data);
                                return opts.preFormatResult(data);
                            };
                        }
                    }

                    if (angular.isDefined(opts.ajax)) {
                        if (!angular.isDefined(opts.ajax.preResults)) {
                            opts.ajax.preResults = opts.ajax.results;
                            opts.ajax.results = function(data, page) {
                                data.rows = AplanaUtils.sanitizeRecursively(data.rows);
                                return opts.ajax.preResults(data, page);
                            };
                        }
                    }

                    if (angular.isDefined(opts.ajax) &&
                        (angular.isUndefined(opts.ajax.params) || angular.isUndefined(opts.ajax.params.nooverlay))) {

                        if (angular.isUndefined(opts.ajax.params)) {
                            opts.ajax.params = {};
                        }
                        opts.ajax.params.nooverlay = true;
                    }

                    /*
                     Convert from Select2 view-model to Angular view-model.
                     */
                    var convertToAngularModel = function (select2_data) {
                        var model;
                        if (opts.simple_tags) {
                            model = [];
                            angular.forEach(select2_data, function (value, index) {
                                model.push(value.id);
                            });
                        } else {
                            model = select2_data;
                        }
                        return model;
                    };

                    /*
                     Convert from Angular view-model to Select2 view-model.
                     */
                    var convertToSelect2Model = function (angular_data) {
                        var model = [];
                        if (!angular_data) {
                            return model;
                        }

                        if (opts.simple_tags) {
                            model = [];
                            angular.forEach(
                                angular_data,
                                function (value, index) {
                                    model.push({'id': value, 'text': value});
                                });
                        } else {
                            model = angular_data;
                        }
                        return model;
                    };

                    if (isSelect) {
                        // Use <select multiple> instead
                        delete opts.multiple;
                        delete opts.initSelection;
                    } else if (isMultiple) {
                        opts.multiple = true;
                    }

                    if (controller) {
                        // Watch the model for programmatic changes
                        scope.$watch(tAttrs.ngModel, function (current, old) {
                            if (!current) {
                                return;
                            }
                            if (current === old) {
                                return;
                            }
                            controller.$render();
                        }, true);
                        controller.$render = function () {
                            if (isSelect) {
                                elm.select2('val', controller.$viewValue);
                            } else {
                                if (opts.multiple) {
                                    var viewValue = controller.$viewValue;
                                    if (angular.isString(viewValue)) {
                                        viewValue = viewValue.split(',');
                                    }
                                    elm.select2(
                                        'data', convertToSelect2Model(viewValue));
                                } else {
                                    if (angular.isObject(controller.$viewValue)) {
                                        elm.select2('data', controller.$viewValue);
                                    } else if (!controller.$viewValue) {
                                        elm.select2('data', null);
                                    } else {
                                        elm.select2('val', controller.$viewValue);
                                    }
                                }
                            }
                        };

                        // Watch the options dataset for changes
                        if (watch) {
                            scope.$watch(watch, function (newVal, oldVal, scope) {
                                if (angular.equals(newVal, oldVal)) {
                                    return;
                                }
                                // Delayed so that the options have time to be rendered
                                $timeout(function () {
                                    elm.select2('val', controller.$viewValue);
                                    // Refresh angular to remove the superfluous option
                                    elm.trigger('change');
                                    if (newVal && !oldVal && controller.$setPristine) {
                                        controller.$setPristine(true);
                                    }
                                });
                            });
                        }

                        // Update valid and dirty statuses
                        controller.$parsers.push(function (value) {
                            var div = elm.prev();
                            div
                                .toggleClass('ng-invalid', !controller.$valid)
                                .toggleClass('ng-valid', controller.$valid)
                                .toggleClass('ng-invalid-required', !controller.$valid)
                                .toggleClass('ng-valid-required', controller.$valid)
                                .toggleClass('ng-dirty', controller.$dirty)
                                .toggleClass('ng-pristine', controller.$pristine);
                            return value;
                        });

                        if (!isSelect) {
                            // Set the view and model value and update the angular template manually for the ajax/multiple select2.
                            elm.bind("change", function (e) {
                                e.stopImmediatePropagation();

                                if (scope.$$phase || scope.$root.$$phase) {
                                    return;
                                }
                                scope.$apply(function () {
                                    controller.$setViewValue(
                                        convertToAngularModel(elm.select2('data')));
                                });
                            });

                            if (opts.initSelection) {
                                var initSelection = opts.initSelection;
                                opts.initSelection = function (element, callback) {
                                    initSelection(element, function (value) {
                                        controller.$setViewValue(convertToAngularModel(value));
                                        callback(value);
                                    });
                                };
                            }
                        }
                    }

                    elm.bind("$destroy", function () {
                        elm.select2("destroy");
                    });

                    function updateDisabledAttribute(value) {
                        if (angular.isString(value)) {
                            elm.select2('enable', value.toLowerCase() !== 'true');
                        } else if (typeof value === "boolean") {
                            elm.select2('enable', !value);
                        } else {
                            elm.select2('enable', true);
                        }
                    }

                    attrs.$observe('disabled', function (value) {
                        updateDisabledAttribute(value);
                    });

                    function updateReadonlyAttribute(value) {
                        if (angular.isString(value)) {
                            elm.select2('readonly', value.toLowerCase() === 'true');
                        } else if (typeof value === "boolean") {
                            elm.select2('readonly', value);
                        }  else {
                            elm.select2('readonly', false);
                        }
                    }

                    attrs.$observe('readonly', function (value) {
                        updateReadonlyAttribute(value);
                    });


                    if (attrs.ngMultiple) {
                        scope.$watch(attrs.ngMultiple, function (newVal) {
                            attrs.$set('multiple', !!newVal);
                            elm.select2(opts);
                        });
                    }

                    attrs.$observe('required', function (value) {
                            if (angular.isDefined(value)) {
                                elm.toggleClass('required', value);
                                elm.prev().toggleClass('required', value);
                            }
                        }
                    );

                    // Initialize the plugin late so that the injected DOM does not disrupt the template compiler
                    $timeout(function () {
                        elm.select2(opts);

                        // Применим значения аттрибутов disabled и readonly для созданного селекта
                        if (angular.isDefined(attrs["disabled"])) {
                            updateDisabledAttribute(attrs["disabled"]);
                        }
                        if (angular.isDefined(attrs["readonly"])) {
                            updateReadonlyAttribute(attrs["readonly"]);
                        }

                        // Set initial value - I'm not sure about this but it seems to need to be there
                        elm.val(controller.$viewValue);
                        // important!
                        controller.$render();

                        // Not sure if I should just check for !isSelect OR if I should check for 'tags' key
                        if (!opts.initSelection && !isSelect) {
                            controller.$setViewValue(
                                convertToAngularModel(elm.select2('data'))
                            );
                        }

                        // Прокидываем событие фокуса с select2 на элемент
                        elm.on("select2-focus", function (e) {
                            elm.trigger('click');
                        });

                        elm.on("select2-selecting", function (e) {
                            if (opts.selectAction) {
                                opts.selectAction(e);
                            }
                        });

                        elm.on("select2-close", function (e) {
                            if (opts.closeAction) {
                                opts.closeAction(e);
                            }
                        });
                    });
                };
            }
        };
    }]);

}());
