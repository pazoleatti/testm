/**
 * aplana-treeview (Древовидная структура)
 * Директива предназначена для отображения древовидной структуры элементов
 * http://localhost:8080/#/aplana_treeview
 */
(function () {

    'use strict';

    angular.module('aplana.treeview', ['aplana.utils', 'ngSanitize'])
        .directive('aplanaTreeview', ['AplanaUtils', '$q', function (AplanaUtils, $q) {
            return {
                restrict: 'A',
                scope: {
                    options: '=',
                    treeData: '=',
                    aplanaTreeview: '@',
                    collapsed: '@',
                    treeDisabled: '@disabled',
                    multiSelect: '@',
                    treeRequired: '@',
                    modelValue: '=ngModel'
                },
                templateUrl: AplanaUtils.templatePath + 'treeview/treeview.html',
                require: '?ngModel',
                link: function (scope, el, attrs, ngModel) {
                    scope.$on('$destroy', function () {
                        scope.$broadcast("treeDestroy");

                        el.remove();
                        scope = undefined;
                    });

                    scope.options = scope.options || {};

                    var childrenParamName = scope.options.nodeChildren = scope.options.nodeChildren || 'children';

                    /**
                     * Форматтер и парсер для режима required в одном флаконе
                     * @param value
                     * @return {*} отформатированное значение модели
                     */
                    function validateTreeRequired(value) {
                        var valid = false;
                        if ((scope.multiSelect === 'true') || (scope.multiSelect === '')) {
                            value = value || [];
                            valid = value.length !== 0;
                        }
                        else {
                            value = value || undefined;
                            valid = value !== undefined;
                        }

                        ngModel.$setValidity('tree-required', valid);
                        return value;
                    }

                    /**
                     * Вернет первый попавшийся элемент с пометкой Selected
                     * @return {*} отмеченный элемент
                     */
                    function getFirstSelection() {
                        var getSelection = function (node) {
                            if (node.selected) {
                                return node;
                            }

                            if (node[childrenParamName]) {
                                for (var i = 0; i < node[childrenParamName].length; i++) {
                                    var result = getSelection(node[childrenParamName][i]);
                                    if (result) {
                                        return result;
                                    }
                                }
                            }

                            return undefined;
                        };

                        for (var i = 0; i < scope.treeData.length; i++) {
                            var result = getSelection(scope.treeData[i]);
                            if (result) {
                                return result;
                            }
                        }

                        return undefined;
                    }

                    /**
                     * Вкрнет все элементы с пометкой Selected
                     * @return {Array} все выбранные элементы
                     */
                    function getAllSelectedNodes() {
                        var getSelection = function (node, list) {
                            if (node.selected) {
                                list.push(node);
                            }

                            if (node[scope.options.nodeChildren]) {
                                angular.forEach(node[scope.options.nodeChildren], function (child) {
                                    getSelection(child, list);
                                });
                            }
                        };

                        var result = [];

                        angular.forEach(scope.treeData, function (node) {
                            getSelection(node, result);
                        });

                        return result;
                    }

                    var treeId = scope.aplanaTreeview = scope.aplanaTreeview || _.uniqueId('treeView');

                    scope.options.nodeLabel = scope.options.nodeLabel || 'label';

                    //check tree id, tree data
                    if (angular.isDefined(treeId)) {

                        if (!angular.isDefined(scope.treeData)) {
                            throw new Error('Не удалось определить объект treeData');
                        }

                        /**
                         * Фу-ия для сворачивания/разворачивания всех вершин
                         * @param collapsed требуемое состояние. true для схлопывания вершин
                         */
                        var setCollapsed = function (collapsed) {
                            var collapseNode = function (node, collapsed) {
                                node.collapsed = collapsed;

                                if (node[childrenParamName]) {
                                    for (var i = 0; i < node[childrenParamName].length; i++) {
                                        collapseNode(node[childrenParamName][i], collapsed);
                                    }
                                }
                            };

                            for (var i = 0; i < scope.treeData.length; i++) {
                                collapseNode(scope.treeData[i], collapsed);
                            }
                        };

                        //create tree object in parent scope if not exists
                        // Зарегистрируем компонент в родительском scope под именем {{scope.aplanaTreeviewNode}}
                        //Если он прийдет составным именем form.tree, например, нужно разбить его на части и
                        //шагами идти к искомому объекту
                        var treeRef = scope;
                        var parentRef = scope.$parent;
                        var parts = treeId.split(".");
                        for (var i = 0; i < parts.length; i++) {
                            if (i !== parts.length - 1) {
                                parentRef = parentRef[parts[i]];
                            }

                            if (!treeRef[parts[i]]) {
                                treeRef[parts[i]] = {};
                            }
                            treeRef = treeRef[parts[i]];
                        }
                        var lastPart = parts[parts.length - 1];
                        parentRef[lastPart] = treeRef = treeRef || {};

                        //пупбликуем метод "свернуть все вершины" в контроллер
                        treeRef.collapseAll = function () {
                            setCollapsed(true);
                            if (ngModel) {
                                ngModel.$setPristine();
                            }
                        };

                        //пупбликуем метод "развернуть все вершины" в контроллер
                        treeRef.expandAll = function () {
                            setCollapsed(false);
                            if (ngModel) {
                                ngModel.$setPristine();
                            }
                        };

                        treeRef.onSelectNodeHead = null;

                        //if node head clicks,
                        treeRef.selectNodeHead = treeRef.selectNodeHead || function (selectedNode, $event) {
                                //Collapse or Expand
                                selectedNode.collapsed = !selectedNode.collapsed;

                                if (treeRef.onSelectNodeHead) {
                                    treeRef.onSelectNodeHead(selectedNode);
                                }

                                if (ngModel) {
                                    ngModel.$setPristine();
                                }
                            };

                        treeRef.onSelectNode = null;

                        //Обработчик нажатия на вершину
                        treeRef.selectNodeLabel = treeRef.selectNodeLabel || function (selectedNode, $event, oldValue) {
                                //Срабатывает только если он не Disabled
                                if ((scope.treeDisabled === 'false') || (scope.treeDisabled === undefined)) {

                                    var executeSelect;
                                    if (treeRef.onSelectNode) {
                                        // Разрешаем выбор ноды, если onSelectNode возвращает true (новый функционал) или undefined (для совместимости)
                                        var onSelectNodeResult = treeRef.onSelectNode(selectedNode);
                                        executeSelect = $q.when(angular.isUndefined(onSelectNodeResult) ? true : onSelectNodeResult);
                                    } else {
                                        executeSelect = $q.when(true);
                                    }

                                    executeSelect.then(function (value) {
                                        if (value) {
                                            if ((scope.multiSelect === 'true') || (scope.multiSelect === '')) {
                                                /**
                                                 * В режиме множественого выбора при зажатом Ctrl добавит/снимет выделение с вершины, но остальные не тронет
                                                 * без Ctrl выделит только эту вершину
                                                 */
                                                if (angular.isUndefined($event) || $event.ctrlKey) {
                                                    if (oldValue === 'selected') {
                                                        if (treeRef.selection) {
                                                            var idx;
                                                            for (var i = 0; i < treeRef.selection.length; i++) {
                                                                if (angular.equals(selectedNode, treeRef.selection[i])) {
                                                                    idx = i;
                                                                }
                                                            }

                                                            if (idx !== undefined) {
                                                                treeRef.selection.splice(idx, 1);
                                                            }

                                                            selectedNode.selected = undefined;
                                                        }
                                                    }
                                                    else {
                                                        if (!treeRef.selection) {
                                                            treeRef.selection = [];
                                                        }
                                                        treeRef.selection.push(selectedNode);

                                                        selectedNode.selected = 'selected';
                                                    }
                                                }
                                                else {
                                                    if (treeRef.selection) {
                                                        angular.forEach(treeRef.selection, function (node) {
                                                            node.selected = undefined;
                                                        });
                                                    }

                                                    treeRef.selection = [selectedNode];

                                                    selectedNode.selected = 'selected';
                                                }
                                            }
                                            else {
                                                // В режиме одиночного выбора просто сменит текущий элемент
                                                if (treeRef.selection && treeRef.selection.selected) {
                                                    treeRef.selection.selected = undefined;
                                                }

                                                selectedNode.selected = 'selected';

                                                treeRef.selection = selectedNode;
                                            }
                                            //Обновит значение модели
                                            if (ngModel) {
                                                ngModel.$setViewValue(treeRef.selection);
                                            }
                                        }
                                    });


                                }
                            };

                        // выбираем узел в дереве по свойству объекта и его значению
                        treeRef.setSelectedByProperty = treeRef.setSelectedByProperty || function (value, property) {

                                if (scope.treeData[0][property] === undefined) {
                                    throw new Error('Необходимо указать атрибут узла - ' + property);
                                }

                                var getSelection = function (node, value, property) {
                                    if (node[property] === value) {
                                        return node;
                                    }

                                    if (node[childrenParamName]) {
                                        for (var i = 0; i < node[childrenParamName].length; i++) {
                                            var result = getSelection(node[childrenParamName][i], value, property);
                                            if (result) {
                                                return result;
                                            }
                                        }
                                    }

                                    return undefined;
                                };

                                // рекурсивно по всем данным
                                var selectedNode;
                                for (var i = 0; i < scope.treeData.length; i++) {
                                    selectedNode = getSelection(scope.treeData[i], value, property);
                                    if (selectedNode) {
                                        break;
                                    }
                                }
                                // если что-то есть то выбираем
                                if (selectedNode) {
                                    // при множественном выборе, очищается и выбирается одна вершина
                                    if ((scope.multiSelect === 'true') || (scope.multiSelect === '')) {
                                        if (!treeRef.selection) {
                                            treeRef.selection = [];
                                        }
                                        treeRef.selection.push(selectedNode);

                                        selectedNode.selected = 'selected';
                                    } else {
                                        // без множественого выбора просто устанавливается найденая вершина
                                        if (treeRef.selection && treeRef.selection.selected) {
                                            treeRef.selection.selected = undefined;
                                        }
                                        selectedNode.selected = 'selected';
                                        treeRef.selection = selectedNode;
                                    }
                                    // Вернем найденную ноду
                                    return selectedNode;
                                } else {
                                    // Вернем undefined, чтобы показать, что ничего не нашли
                                    return undefined;
                                }
                            };

                        //Проглядываем за режимом multiSelect, если он меняется - обнуляем значение
                        scope.$watch('multiSelect', function (value, oldValue) {
                            if (value !== oldValue) {
                                if ((scope.multiSelect === 'true') || (scope.multiSelect === '')) {
                                    if (treeRef.selection) {
                                        treeRef.selection.selected = undefined;
                                    }
                                    treeRef.selection = [];
                                }
                                else {
                                    if (treeRef.selection) {
                                        angular.forEach(treeRef.selection, function (node) {
                                            node.selected = undefined;
                                        });
                                    }
                                    treeRef.selection = undefined;
                                }

                                if (ngModel) {
                                    ngModel.$setViewValue(treeRef.selection);
                                }
                            }
                        });

                        //если есть связь с моделью
                        if (ngModel) {
                            /**
                             * Устанавливает режим Required
                             * @param value режим Required
                             */
                            treeRef.setRequired = function (value) {
                                value = value === 'true' || value === '';

                                if (value) {
                                    //Нужно проверить есть ли валидаторы для режима Required и добавить по необходимости
                                    var formatterExists = false;
                                    var parserExists = false;

                                    angular.forEach(ngModel.$formatters, function (formatter) {
                                        formatterExists |= formatter.name === validateTreeRequired.name;
                                    });

                                    angular.forEach(ngModel.$parsers, function (parser) {
                                        parserExists |= parser.name === validateTreeRequired.name;
                                    });

                                    if (!formatterExists) {
                                        ngModel.$formatters.unshift(validateTreeRequired);
                                    }
                                    if (!parserExists) {
                                        ngModel.$parsers.push(validateTreeRequired);
                                    }

                                    //Проверить на валиднось имующееся значение
                                    ngModel.$setValidity('tree-required', validateTreeRequired(ngModel.$viewValue) !== undefined);
                                }
                                else {
                                    //Иначе нужно удалить валидаторы
                                    var formatterIdx;
                                    var parserIdx;

                                    for (var i = 0; i < ngModel.$formatters.length; i++) {
                                        if (ngModel.$formatters[i].name === validateTreeRequired.name) {
                                            formatterIdx = i;
                                        }
                                    }

                                    for (var j = 0; j < ngModel.$parsers.length; j++) {
                                        if (ngModel.$parsers[j].name === validateTreeRequired.name) {
                                            parserIdx = j;
                                        }
                                    }

                                    if (formatterIdx !== undefined) {
                                        ngModel.$formatters.splice(formatterIdx, 1);
                                    }
                                    if (parserIdx !== undefined) {
                                        ngModel.$parsers.splice(parserIdx, 1);
                                    }

                                    //И обнулить сообщение с ошибкой
                                    ngModel.$setValidity('tree-required', true);
                                }
                                ngModel.$setPristine();
                            };

                            //Смотрим за режимом Required и реагируем на его изменение/инициализацию
                            scope.$watch('treeRequired', function (value) {
                                treeRef.setRequired(value);
                            });

                            /**
                             * Смотрим за изменениями данных, на основе которых построено дерево и меняем значение модели
                             * если в исходных данные есть выделенные вершины - инициализация ngModel пройдет по ним
                             */
                            scope.$watch('treeData', function (value, oldValue) {
                                if ((scope.multiSelect === 'true') || (scope.multiSelect === '')) {
                                    ngModel.$viewValue = getAllSelectedNodes();
                                }
                                else {
                                    ngModel.$viewValue = getFirstSelection();
                                }

                                scope.modelValue = ngModel.$viewValue;
                                ngModel.$render();
                            }, true);

                            /**
                             * $render вызывается при изменении значения модели, их нужно отразить в ui
                             */
                            ngModel.$render = function () {
                                if ((scope.multiSelect === 'true') || (scope.multiSelect === '')) {
                                    if (treeRef.selection) {
                                        angular.forEach(treeRef.selection, function (node) {
                                            node.selected = undefined;
                                        });
                                    }

                                    angular.forEach(ngModel.$viewValue, function (node) {
                                        node.selected = 'selected';
                                    });

                                    treeRef.selection = ngModel.$viewValue;
                                }
                                else {
                                    if (treeRef.selection && treeRef.selection.selected) {
                                        treeRef.selection.selected = undefined;
                                    }

                                    if (ngModel.$viewValue) {
                                        ngModel.$viewValue.selected = 'selected';
                                    }

                                    treeRef.selection = ngModel.$viewValue;
                                }
                            };
                        }

                        if (angular.isFunction(scope.options.initTree)) {
                            scope.options.initTree(treeRef);
                        }
                    } else {
                        throw new Error('Необходимо указать обязательные атрибуты директивы - aplanaTreeview и treeData');
                    }
                }
            };
        }])
        .directive('aplanaTreeviewNode', ['$compile', function ($compile) {
            return {
                restrict: 'A',
                compile: function (el, attrs) {
                    return function (scope, element, attrs) {
                        scope.$on('treeDestroy', function () {
                            element.remove();
                            scope = undefined;
                        });

                        //tree id
                        var treeId = attrs.aplanaTreeviewNode = attrs.aplanaTreeviewNode || _.uniqueId('treeView');

                        //tree model
                        var treeData = attrs.treeData;

                        //node label
                        var nodeLabel = scope.options.nodeLabel || 'label';

                        //children
                        var nodeChildren = scope.options.nodeChildren || 'children';

                        var iconFileClass = scope.options.iconFileClass || 'icon-file';

                        var iconFolderCloseClass = scope.options.iconFolderCloseClass || 'icon-folder-close';

                        var iconFolderOpenClass = scope.options.iconFolderOpenClass || 'icon-folder-open';

                        // имя поля для класса переопределённой иконки узла
                        var iconNodeClassLabel = scope.options.iconNodeClassLabel;

                        //collapsed
                        var collapsed = attrs.collapsed === 'true';

                        if (scope.node && angular.isUndefined(scope.node.collapsed)) {
                            scope.node.collapsed = collapsed;
                        } else {
                            for (var i = 0; i < scope.treeData.length; i++) {
                                if (angular.isUndefined(scope.treeData[i].collapsed)) {
                                    scope.treeData[i].collapsed = collapsed;
                                }
                            }
                        }

                        var labelFormatter = scope.options.labelFormatter;
                        var labelValue = labelFormatter ? "node | " + labelFormatter : 'node.' + nodeLabel;

                        var iconClassExpression = '{' +
                            '\'' + iconFolderCloseClass + '\': node.' + nodeChildren + '.length && node.collapsed, ' +
                            '\'' + iconFolderOpenClass + '\': node.' + nodeChildren + '.length && !node.collapsed,' +
                            '\'' + iconFileClass + '\': !node.' + nodeChildren + '.length' +
                            '}';
                        var iconClickHandler = '!node.' + nodeChildren + '.length ? ' +
                            '' + treeId + '.selectNodeLabel(node, $event): ' +
                            '' + treeId + '.selectNodeHead(node)';

                        // иконка узла может быть явно переопределена через настройки
                        if (iconNodeClassLabel) {
                            iconClassExpression = 'node.' + iconNodeClassLabel;
                        }

                        var iconNodeClassFormatterString = '';
                        // иконка узла может задаваться через форматтер
                        if (scope.options.iconNodeClassFormatter) {
                            iconClassExpression = '';
                            iconNodeClassFormatterString = 'class="{{node | ' + scope.options.iconNodeClassFormatter + '}}"';
                        }

                        //tree template
                        var template =
                            '<ul data-ng-if="!node.collapsed" data-bindonce class="aplanaTreeview">' +
                            '<li data-ng-repeat="node in ' + treeData + '">' +
                            '<i ' + iconNodeClassFormatterString + ' data-ng-class="' + iconClassExpression + '" data-ng-click="' + iconClickHandler + '"></i>' +
                            '<input type="checkbox" data-ng-show="options.checkbox" data-ng-change="' + treeId + '.selectNodeLabel(node, $event, \'{{node.selected}}\')" data-ng-disabled="treeDisabled === \'true\'" data-ng-true-value="\'selected\'" data-ng-false-value="\'false\'" data-ng-model="node.selected">' +
                            '<span data-ng-class="node.selected" data-ng-click="' + treeId + '.selectNodeLabel(node, $event, node.selected)" data-ng-bind-html="' + labelValue + '"></span>' +
                            '<div data-aplana-treeview-node="' + treeId + '" data-tree-data="node.' + nodeChildren + '" ' +
                            'data-collapsed="' + collapsed + '">' +
                            '</div>' +
                            '</li>' +
                            '</ul>';

                        //check tree id, tree data
                        if (angular.isDefined(treeId) && angular.isDefined(treeData)) {
                            //Rendering template.
                            element.html('').append($compile(template)(scope));
                        } else {
                            throw new Error('Необходимо указать обязательные атрибуты директивы - aplanaTreeview и treeData');
                        }
                    };
                }
            };
        }]);
}());