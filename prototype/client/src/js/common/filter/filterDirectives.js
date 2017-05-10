(function() {
    'use strict';

    angular.module('sbrfNdfl.filterDirectives', [])
        /**
         * Две кнопки "Найти", "Сбросить"
         *
         * Атрибуты:
         *   "on-search" - обработчик кнопки "Найти"
         *   "on-clear" - обработчик кнопки "Сбросить"
         */
        .directive('searchClearButtons', function () {
            return {
                restrict: 'E',
                templateUrl: 'js/common/filter/searchClearButtons.html',
                scope: {
                    onSearch: '&onSearch',
                    onClear: '&onClear'
                }
            }
        })
        /**
         * Фильтр для выпадающих списков. Выбор одного значения.
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         *   "display-field" - название поля для отображения в выпадающем списке
         *   "entityType" - название класса модели: "Region', 'macroRegion' и т.д. либо название класса Enum
         *   "title" - подпись к выпадающему списку
         *   "is-enum" - признак того, что отображаются значения enum. Значение по-умолчанию = false
         *   "depends-on" - параметр используется для обработки зависимости разных виджетов друг от друга.
         *                  Заполняется параметром, используемым в дао для фильтрации по этому полю
         *   "depends-on-value" - параметр используется для обработки зависимости разных виджетов друг от друга.
         *                  Заполняется названием поля в объекте, из которого надо брать id для фильтрации по полю из depends-on
         *   onSelect - вызов функции при выборе элемента из списка
         */
        .directive('selectFilter', ['$http', 'USER_DATA', function ($http, USER_DATA) {
            return {
                restrict: 'E',
                require: 'ngModel',
                templateUrl: 'js/common/filter/selectFilter.html',
                scope: {
                    ngModel: '=',
                    displayField: '@',
                    entityType: '@?',
                    statusEntityType: '@?',
                    title: '@',
                    isEnum: '@?',
                    items: '=?',
                    light: '=?',
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                link: function (scope, element, attr) {
                    scope.isEnum = angular.isDefined(scope.isEnum) ? scope.isEnum : false;
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                    resetPaging()
                    scope.pageSize = 30;

                    if (!angular.isDefined(scope.items) || scope.items.length == 0) {
                        scope.items = [];
                    }
                    scope.$on('selectFilterClosed', function(event, data) {
                        resetPaging();
                    })
                    scope.onSelectScrollComplete = function(searchText) {
                        if(!scope.totalCount || scope.currentPageStart <= scope.totalCount) {
                            scope.searchItems(searchText)
                        }
                    }
                    if (scope.dependsOn) {
                        scope.onClick = function() {
                            resetPaging();
                            scope.searchItems();
                        };
                        scope.$watch("dependsOnValue", function () {
                            resetPaging()
                            if (scope.ngModel && scope.dependsOnValue != undefined) {
                                scope.searchItems(undefined, true).then(function () {
                                    var hasSameValue = false;
                                    scope.items.forEach(function (item) {
                                        if (item.id == scope.ngModel) {
                                            hasSameValue = true;
                                        }
                                    });
                                    if (!hasSameValue) {
                                        scope.ngModel = undefined;
                                    }
                                });
                            }
                        });
                    }

                    function resetPaging() {
                        scope.currentPageStart = 1
                        scope.items = []
                        scope.totalCount = undefined
                    }

                    function getIndexById(list, id, start) {
                        for (var i = start ? start : 0 ; i < list.length; i++) {
                            if (list[i] && list[i].id == id) {
                                return i;
                            }
                        }
                        return -1;
                    }

                    scope.searchItems = function(searchText, dependsOnValueEvent) {
                        var searchByText = searchText && searchText != scope.previousSearchText
                        if(searchByText) {
                            scope.previousSearchText = searchText
                            resetPaging()
                        }
                        if(!searchText && scope.previousSearchText) {
                            scope.previousSearchText = undefined
                            resetPaging()
                        }
                        if (scope.statusEntityType) {
                            resetPaging()
                        }
                        var params = {
                            fulltext: searchText,
                            sort: scope.displayField + '-asc',
                        };
                        if(scope.pageSize) {
                            params['paging'] = scope.currentPageStart + ";" + (scope.currentPageStart + scope.pageSize - 1)
                            scope.currentPageStart += scope.pageSize
                        }
                        if (scope.dependsOn) {
                            params[scope.dependsOn] = scope.dependsOnValue;
                        }
                        // Получение данных
                        var url;
                        if (scope.isEnum) {
                            //Заполняется значениями из Enum
                            url = 'rest/enum/' + scope.entityType;
                        } else if (scope.statusEntityType) {
                            //Заполняется значениями статусов
                            url = 'rest/service/status/getStatuses/' + scope.statusEntityType
                        } else {
                            //Заполняется значениями сущностей
                            url = 'rest/entity/' + (scope.light ? "light/" : "") + scope.entityType;
                        }

                        return $http.get(url, {params: params})
                            .success(function (data) {
                                if(!scope.totalCount && data.total) {
                                    scope.totalCount = data.total
                                }
                                var newItems = scope.isEnum || scope.statusEntityType ? data : data.list;

                                if(scope.items[0] && scope.items[0].id && getIndexById(newItems, scope.items[0].id) >= 0) {
                                    scope.items.shift()
                                }

                                scope.items = scope.items.concat(newItems);
                                if (angular.isDefined(scope.predefinedValues)) {
                                    scope.items = scope.items.concat(scope.predefinedValues);
                                }
                                var hasValue = false;
                                scope.items.forEach(function(item) {
                                    if (item.id == scope.filterValue.id) {
                                        hasValue = true
                                    }
                                });
                                if (scope.filterValue.id && !hasValue && !searchByText && !dependsOnValueEvent) {
                                    var url = 'rest/entity/' + scope.entityType + '/' + scope.filterValue.id;
                                    $http.get(url).success(function (data) {
                                        scope.items.unshift(data)
                                    });
                                }
                            });
                    };

                    // оборачиваем в объект из-за дефекта ui-select
                    scope.filterValue = {id: scope.ngModel};

                    if ((scope.entityType == 'MacroRegion' && USER_DATA.macroRegion) ||
                        (scope.entityType == 'Vendor' && USER_DATA.user.vendor)) {
                        //Если поиск макрорегиона или поставщика, то выставляем те, которые назначены пользователю (если указаны)
                        if (scope.entityType == 'MacroRegion') {
                            scope.items = [USER_DATA.macroRegion];
                        }
                        if (scope.entityType == 'Vendor') {
                            scope.items = [USER_DATA.user.vendor];
                        }

                        scope.ngModel = scope.items[0].id;

                        scope.filterEnabled = false;
                    } else {
                        scope.filterEnabled = true;
                    }
                    scope.onSelectProcess = function() {
                        scope.ngModel = scope.filterValue.id;
                        if (scope.onSelect) {
                            scope.onSelect()
                        }
                    };
                    scope.$watch('ngModel', function (newValue) {
                        if(newValue && getIndexById(scope.items, newValue) < 0 && scope.entityType != undefined){
                            var url = 'rest/entity/' + scope.entityType + '/' + newValue;
                            $http.get(url)
                                .then(function (response) {
                                    scope.items.push(response.data)
                                });
                        }
                        scope.filterValue.id = newValue ? newValue.id || newValue : newValue;
                    });

                    var labelClass = attr.labelClass ? attr.labelClass : 'col-md-3';
                    element.find('label').parent().addClass(labelClass);
                    var inputClass = attr.inputClass ? attr.inputClass : 'col-md-9';
                    element.find('ui-select').parent().addClass(inputClass);

                    element.on('$destroy', function() {
                        scope.items = [];
                    });
                }
            }
        }])
        /**
         * Виджет выбора макрорегиона
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('macroRegionWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    labelClass: "@",
                    inputClass: "@",
                    dependsOn: '@?',
                    light: '=?',
                    items: '=?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                    '<data-select-filter' +
                    '  data-label-class="{{labelClass}}"' +
                    '  data-input-class="{{inputClass}}"' +
                    '  data-ng-model="ngModel"' +
                    '  data-display-field="name"' +
                    '  data-title="macroRegionFilterLabel"' +
                    '  data-entity-type="MacroRegion"' +
                    '  data-light="light"' +
                    '  data-items="items"' +
                    '  data-on-select="onSelect"' +
                    '  data-predefined-values="predefinedValues"' +
                    '  data-depends-on="{{dependsOn}}"' +
                    '  data-depends-on-value="dependsOnValue">' +
                    '</data-select-filter>',
                link: function (scope, element, attr) {
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                }
            }
        })
        /**
         * Виджет выбора поставщика
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('vendorWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    labelClass: "@",
                    inputClass: "@",
                    light: '=?',
                    items: '=?',
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="name"' +
                '  data-title="distributorFilterLabel"' +
                '  data-entity-type="Vendor"' +
                '  data-light="light"' +
                '  data-items="items"' +
                '  data-on-select="onSelect"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-depends-on="{{dependsOn}}"' +
                '  data-depends-on-value="dependsOnValue">' +
                '</data-select-filter>'
            }
        })
        /**
         * Виджет для выбора статуса сущности
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('statusWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    labelClass: "@",
                    inputClass: "@",
                    statusEntityType: '@',
                    items: '=',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="title"' +
                '  data-title="statusFilterLabel"' +
                '  data-items="items"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-on-select="onSelect"' +
                '  data-status-entity-type="{{statusEntityType}}">' +
                '</data-select-filter>'
            }
        })
        /**
         * Виджет выбора тарифного плана
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('tariffWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    labelClass: "@",
                    inputClass: "@",
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    light: '=?',
                    items: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="name"' +
                '  data-title="tariffFilterLabel"' +
                '  data-entity-type="Tariff"' +
                '  data-light="light"' +
                '  data-items="items"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-on-select="onSelect"' +
                '  data-depends-on="{{dependsOn}}"' +
                '  data-depends-on-value="dependsOnValue">' +
                '</data-select-filter>',
                link: function (scope, element, attr) {
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                }
            }
        })
        /**
         * Виджет выбора hlr
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('hlrWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    light: '=?',
                    items: '=?',
                    labelClass: "@",
                    inputClass: "@",
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="value"' +
                '  data-title="filter.label.hlr"' +
                '  data-entity-type="Hlr"' +
                '  data-light="light"' +
                '  data-items="items"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-on-select="onSelect"' +
                '  data-depends-on="{{dependsOn}}"' +
                '  data-depends-on-value="dependsOnValue">' +
                '</data-select-filter>',
                link: function (scope, element, attr) {
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                }
            }
        })

        /**
         * Виджет выбора hlrId
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('hlridWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    light: '=?',
                    items: '=?',
                    labelClass: "@",
                    inputClass: "@",
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="value"' +
                '  data-title="hlrFilterLabel"' +
                '  data-entity-type="Hlr"' +
                '  data-light="light"' +
                '  data-items="items"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-on-select="onSelect"' +
                '  data-depends-on="{{dependsOn}}"' +
                '  data-depends-on-value="dependsOnValue">' +
                '</data-select-filter>',
                link: function (scope, element, attr) {
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                }
            }
        })
        /**
         * Виджет выбора типа карты
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('cardTypeWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    light: '=?',
                    items: '=?',
                    labelClass: "@",
                    inputClass: "@",
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="name"' +
                '  data-title="cardFilterLabel"' +
                '  data-entity-type="CardType"' +
                '  data-light="light"' +
                '  data-items="items"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-on-select="onSelect"' +
                '  data-depends-on="{{dependsOn}}"' +
                '  data-depends-on-value="dependsOnValue">' +
                '</data-select-filter>',
                link: function (scope, element, attr) {
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                }
            }
        })
        /**
         * Виджет выбора алгоритма аутентификации
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('authAlgorythmWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    light: '=?',
                    items: '=?',
                    labelClass: "@",
                    inputClass: "@",
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="name"' +
                '  data-title="algoFilterLabel"' +
                '  data-entity-type="AuthAlgorythm"' +
                '  data-light="light"' +
                '  data-items="items"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-on-select="onSelect"' +
                '  data-depends-on="{{dependsOn}}"' +
                '  data-depends-on-value="dependsOnValue">' +
                '</data-select-filter>',
                link: function (scope, element, attr) {
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                }
            }
        })
        /**
         * Виджет выбора региона
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('regionWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    light: '=?',
                    items: '=?',
                    labelClass: "@",
                    inputClass: "@",
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="name"' +
                '  data-title="regionFilterLabel"' +
                '  data-entity-type="Region"' +
                '  data-light="light"' +
                '  data-items="items"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-on-select="onSelect"' +
                '  data-depends-on="{{dependsOn}}"' +
                '  data-depends-on-value="dependsOnValue">' +
                '</data-select-filter>',
                link: function (scope, element, attr) {
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                }
            }
        })
        /**
         * Виджет выбора транспортной компании
         *
         * Атрибуты:
         *   "label-сlass" - css-класс для контейнера с подписью
         *   "input-сlass" - css-класс для контейнера с чекбоксом
         *   "ng-model" - модель для связывания с данными
         */
        .directive('transportCompanyWidget', function () {
            return {
                restrict: 'E',
                require: 'ngModel',
                scope: {
                    ngModel: '=',
                    light: '=?',
                    items: '=?',
                    labelClass: "@",
                    inputClass: "@",
                    dependsOn: '@?',
                    dependsOnValue: '=?',
                    onSelect: '&',
                    predefinedValues: '=?'
                },
                template:
                '<data-select-filter' +
                '  data-label-class="{{labelClass}}"' +
                '  data-input-class="{{inputClass}}"' +
                '  data-ng-model="ngModel"' +
                '  data-display-field="name"' +
                '  data-title="transportCompanyFilterLabel"' +
                '  data-entity-type="TransportCompany"' +
                '  data-light="light"' +
                '  data-items="items"' +
                '  data-predefined-values="predefinedValues"' +
                '  data-on-select="onSelect"' +
                '  data-depends-on="{{dependsOn}}"' +
                '  data-depends-on-value="dependsOnValue">' +
                '</data-select-filter>',
                link: function (scope, element, attr) {
                    scope.light = angular.isDefined(scope.light) ? scope.light : false;
                }
            }
        })
} ());