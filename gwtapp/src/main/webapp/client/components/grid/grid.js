/**
 * aplana-grid (Отображение таблицы)
 * Директива aplana-grid служит для отображения таблицы с кучей возможностей
 * http://localhost:8080/#/aplana_grid
 */
(function () {
    'use strict';

    /* Директива для AplanaGrid */
    angular.module("aplana.grid", ['aplana.utils', 'aplana.webStorage'])
        .factory('GridSettingsStorage', ['AplanaUtils', '$webStorage', '$rootScope', function (AplanaUtils, $webStorage, $rootScope) {
            var predefinedStores = {
                gridPageSettings: 'gridPageSettings',
                gridColSettings: 'gridColSettings'
            };

            var service = {
                /**
                 * Получает данные о пагинации из хранилища
                 *
                 * @param gridName имя грида
                 * @returns данные о пагинации
                 */
                getPageSettings: function (gridName) {
                    var localSettings = $webStorage.get(predefinedStores.gridPageSettings, gridName, false);
                    var sessionSettings = $webStorage.get(predefinedStores.gridPageSettings, gridName, true);

                    return (!!localSettings || !!sessionSettings) ? angular.extend({}, localSettings, sessionSettings) : undefined;
                },

                /**
                 * Сохраняет данные о пагинации в хранилище
                 *
                 * @param grid ссылка на грид
                 * @returns {boolean} факт успешности выполнения операции
                 */
                setPageSettings: function (grid) {
                    var gridName = grid.jqGrid('getGridParam', 'gridName');

                    var localSettings = {
                        rowNum: parseInt(grid.jqGrid('getGridParam', 'rowNum'), 10),
                        sortname: grid.jqGrid('getGridParam', 'sortname'),
                        sortorder: grid.jqGrid('getGridParam', 'sortorder'),
                        rowFilterShow: grid.jqGrid('getGridParam', 'rowFilterShow')
                    };

                    var sessionSettings = {
                        page: grid.jqGrid('getGridParam', 'page')
                    };

                    $rootScope.$broadcast('UPDATE_GIRD_HEIGHT');
                    var result = true;
                    result = result && $webStorage.set(predefinedStores.gridPageSettings, gridName, sessionSettings, true);
                    result = result && $webStorage.set(predefinedStores.gridPageSettings, gridName, localSettings, false);

                    return result;
                },

                /**
                 * Загружает настройки грида в указанный объект (как правило его конфиг)
                 *
                 * @param gridConfig конфиг для загрузки
                 */
                loadPageSettings: function (gridConfig) {
                    var gridName = gridConfig.gridName;

                    var savedOptions = service.getPageSettings(gridName);

                    if (!!savedOptions) {
                        gridConfig.page = savedOptions.page;
                        gridConfig.rowNum = savedOptions.rowNum;
                        gridConfig.sortname = savedOptions.sortname;
                        gridConfig.sortorder = savedOptions.sortorder;
                        gridConfig.rowFilterShow = savedOptions.rowFilterShow;
                    }
                },

                /**
                 * Метод для получения из хранилища сохраненных данных о колонках грида
                 *
                 * @param gridName имя грида
                 * @returns {*} данные о колонках
                 */
                getColSettings: function (gridName) {
                    return $webStorage.get(predefinedStores.gridColSettings, gridName, false);
                },

                /**
                 * Метод для сохранения текущих натсроек грида в хранилище
                 *
                 * @param grid ссылка на грид
                 */
                applyColSettings: function (grid) {
                    var gridName = grid.jqGrid('getGridParam', 'gridName');

                    var colSettings = {
                        colModel: [],
                        colNames: grid.jqGrid('getGridParam', 'colNames')
                    };

                    var colModel = grid.jqGrid('getGridParam', 'colModel');
                    for (var idx = 0; idx < colModel.length; idx++) {
                        colSettings.colModel.push({
                            name: colModel[idx].name,
                            width: colModel[idx].width,
                            general:  colModel[idx].general
                        });
                    }
                    $rootScope.$broadcast('UPDATE_GIRD_HEIGHT');
                    $webStorage.set(predefinedStores.gridColSettings, gridName, colSettings, false);
                },

                /**
                 * Моетод для восстановления настроек грида по умолчанию
                 *
                 * @param grid          ссылка на грид
                 * @param gridElement элемент грида
                 * @param initialConfig исходный конфиг грида
                 */
                resetSettings: function (grid, gridElement, initialConfig) {
                    var gridName = grid.jqGrid('getGridParam', 'gridName');

                    var savedColOptions = service.getColSettings(gridName);

                    //Восстанавливает настройки колонок
                    if (!!savedColOptions) {
                        var initialColIds = _.map(initialConfig.colModel, function (item) {
                            return item.name;
                        });
                        var savedColIds = _.map(savedColOptions.colModel, function (item) {
                            return item.name;
                        });

                        //Системынх колонок может быть три: cb (check box), rn (row number), subGrid
                        var sysColCount = savedColIds.length - initialColIds.length;

                        var permutation = [];
                        for (var idx = 0; idx < sysColCount; idx++) {
                            permutation.push(idx);
                        }

                        for (var i = 0; i < initialColIds.length; i++) {
                            if (savedColIds.indexOf(initialColIds[i]) !== -1) {
                                permutation.push(savedColIds.indexOf(initialColIds[i]));
                            }
                        }

                        grid.remapColumns(permutation, true, false);

                        angular.forEach(initialConfig.colModel, function (column) {
                            grid.jqGrid('setColWidth', column.name, column.width, false);
                        });
                    }

                    //Восстанавливаем настройки сортировки и пагинации
                    service.getPageSettings(gridName);

                    grid.jqGrid('setGridParam', {
                        page: 1
                    });
                    if (!initialConfig.sortname) {
                        grid.jqGrid("setGridParam", {
                            sortname: initialConfig.sortname || "",
                            sortorder: initialConfig.sortorder || "",
                            lastsort: 0
                        }).trigger('reloadGrid');
                        gridElement.find("span.s-ico").each(function () {
                            $(this).hide();
                        });
                    } else {
                        grid.jqGrid("sortGrid", initialConfig.sortname, true, initialConfig.sortorder);
                    }

                    $rootScope.$broadcast('UPDATE_GIRD_HEIGHT');
                    $webStorage.remove(predefinedStores.gridColSettings, gridName, false);
                },

                /**
                 * Метод для загрузки сохраненных настроек в конфиг
                 *
                 * @param gridConfig конфиг для загрузки
                 */
                loadColSettings: function (gridConfig) {
                    var gridName = gridConfig.gridName;

                    var savedOptions = service.getColSettings(gridName);

                    if (!!savedOptions) {
                        var oldColModel = angular.copy(gridConfig.colModel);
                        var newColModel = [];

                        var newColNames = [];

                        for (var idx = 0; idx < savedOptions.colModel.length; idx++) {
                            var column = _.find(oldColModel, function (item) {
                                return item.name === savedOptions.colModel[idx].name;
                            });

                            if (!!column) {
                                oldColModel = _.without(oldColModel, column);

                                column.width = savedOptions.colModel[idx].width;
                                newColModel.push(column);

                                newColNames.push(savedOptions.colNames[idx]);
                            }
                        }

                        if (oldColModel.length === 0) {
                            gridConfig.colModel = newColModel;
                            gridConfig.colNames = newColNames;
                        } else {
                            $webStorage.remove(predefinedStores.gridColSettings, gridName, false);
                        }
                    }
                }
            };

            return service;
        }])
        .directive('aplanaGrid', ['$rootScope', '$gridStack', '$timeout', 'AplanaUtils', 'PagingBuilder', 'GridSettingsStorage',
            function ($rootScope, $gridStack, $timeout, AplanaUtils, $pagingBuilder, GridSettingsStorage) {
                /**
                 * достает элемент таблицы <table> из шаблона директивы
                 * @param el шаблон директивы
                 */
                var getGrid = function (el) {
                    return angular.element(el.find('table')[0]);
                };

                /**
                 * достает элемент пагинатора  <div>, из шаблона директивы
                 * @param el шаблон директивы
                 */
                var getPaginator = function (el) {
                    return angular.element(el.find('div.footer')[0]);
                };

                /**
                 * достает элемент легенды <div>, из шаблона директивы
                 * @param el шаблон директивы
                 */
                var getLegend = function (el) {
                    return angular.element(el.find('div.legend')[0]);
                };

                /**
                 * достает элемент панельки количества всех записей <div>, из шаблона директивы
                 * @param el шаблон директивы
                 */
                var getViewRecords = function (el) {
                    return angular.element(el.find('div.viewrecords')[0]);
                };

                /**
                 * Показывает/скрывает цвет строки.
                 * @param tr строка
                 * @param rowStyle объект содержащий имя класс или стиль.
                 * @param show флаг выделения строки
                 */
                var applyRowStyle = function (tr, rowStyle, show) {
                    if (rowStyle) {
                        if (rowStyle.className) {
                            if (show) {
                                tr.addClass(rowStyle.className);
                            } else {
                                tr.removeClass(rowStyle.className);
                            }
                        }

                        if (rowStyle.style) {
                            if (show) {
                                tr.css(rowStyle.style, rowStyle.value);
                            } else {
                                tr.css(rowStyle.style, "");
                            }
                        }
                    }
                };

                var applyAttr = function (tr, attr, attrValue) {
                    tr.attr(attr, attrValue);
                };

                return {
                    restrict: 'A',
                    templateUrl: AplanaUtils.templatePath + 'grid/grid.html',
                    scope: {
                        initGrid: '&',
                        gridOptions: '=',
                        gridData: '=',
                        gridCtrl: '=',
                        modelValue: '=ngModel',
                        gridRequired: '@',
                        gridKeepSelection: '@',
                        gridDisableAdjustWidth: '=',
                        gridEnableAdjustWidthByColumns: '=',
                        gridParentAdjustWidthCssSelector: '@',
                        gridRefreshButton: '=',
                        gridFillSpace: '=',
                        gridFillSpaceContainerSelector: '@',
                        gridFillSpaceContainerSelectorTop: '@',
                        gridFillSpaceViewSelector: '@',
                        gridStorePageSettings: '=',
                        gridStoreColSettings: '=',
                        gridHideLegend: '@'
                    },
                    require: '?ngModel',
                    link: function (scope, element, attrs, ngModel) {
                        function validateGridRequired(value) {
                            value = value || [];

                            if (scope.gridRequired === 'true' || scope.gridRequired === '') {
                                ngModel.$setValidity('grid-required', value.length !== 0);
                            }
                            else {
                                ngModel.$setValidity('grid-required', true);
                            }

                            return value;
                        }

                        scope.grid = getGrid(element);
                        scope.paginator = getPaginator(element);
                        scope.legend = getLegend(element);
                        scope.viewrecords = getViewRecords(element);

                        scope.$rawData = undefined;
                        scope.$multiSelect = false;

                        scope.gridParams = {};

                        scope.$phaseSaveCall = function (method) {
                            if (scope.$root.$$phase) {
                                method();
                            }
                            else {
                                scope.$apply(method());
                            }
                        };

                        scope.$on('$destroy', function () {
                            scope.grid.jqGrid('gridUnload');

                            $gridStack.unRegisterAdjustWidthGrid(scope.grid);
                            scope.gridCtrl.unRegisterGridElement();
                            if (scope.$adjustTimer) {
                                $timeout.cancel(scope.$adjustTimer);
                            }
                            scope.grid.remove();
                            scope.grid = undefined;
                            scope.paginator.remove();
                            scope.paginator = undefined;
                            scope.legend.remove();
                            scope.legend = undefined;
                            scope.viewrecords.remove();
                            scope.viewrecords = undefined;

                            element.remove();
                            element = undefined;

                            gridConfig = {};
                            scope.gridCtrl = {};
                            scope = undefined;
                        });

                        scope.hasStoredSettings = function () {
                            var pageSettings = GridSettingsStorage.getPageSettings(scope.grid.jqGrid('getGridParam', 'gridName'));

                            var comparePageSettings = function (storedSettings, initialSettings) {
                                var isStringsEquals = function (first, second) {
                                    return (!first && !second) || (first === second);
                                };

                                return !!storedSettings && !!initialSettings &&
                                    (!isStringsEquals(storedSettings.sortname, initialSettings.sortname) || !isStringsEquals(storedSettings.sortorder, initialSettings.sortorder));
                            };

                            return !!GridSettingsStorage.getColSettings(scope.grid.jqGrid('getGridParam', 'gridName')) ||
                                (!!pageSettings && comparePageSettings(pageSettings, initialConfig));
                        };

                        // Публикуем контроллер грида в родительский scope
                        scope.gridCtrl = {
                            grid: undefined,
                            required: undefined,
                            gridNodeArray: undefined,

                            $getKeyAttribute: function () {
                                var keyAttribute = 'id';

                                var colModel = scope.grid.getGridParam('colModel');
                                angular.forEach(colModel, function (colInfo) {
                                    if (colInfo.key) {
                                        keyAttribute = colInfo.name;
                                    }
                                });
                                return keyAttribute;
                            },

                            getRawData: function (rowId) {
                                if (angular.isUndefined(rowId)) {
                                    return scope.$rawData || [];
                                }

                                var keyAttribute = scope.gridCtrl.$getKeyAttribute();

                                var result = null;
                                angular.forEach(scope.$rawData, function (row) {
                                    /**
                                     * Тут именно ==, не менять на ===
                                     * jqGrid отдает Id рядка только строкой, а в модели может быть число.
                                     * Экзотические варианты, вроде даты в поле id, не учитываются
                                     */
                                    if (AplanaUtils.getObjectPropertyValueByPath(row, keyAttribute) == rowId) {
                                        result = row;
                                    }
                                });

                                return result;
                            },
                            setCell: function (rowId, colName, data) {
                                this.grid.setCell(rowId, colName, data);
                            },
                            getCell: function (rowId, colName) {
                                this.grid.getCell(rowId, colName);
                            },
                            /**
                             * Сохраняет ссылку на грид
                             */
                            registerGridElement: function (grid) {
                                this.grid = grid;
                            },

                            /**
                             * Удаляет ссылку на грид
                             */
                            unRegisterGridElement: function () {
                                this.grid = undefined;
                            },

                            /**
                             * Возвращает ссылку на jqGrid
                             */
                            getGrid: function () {
                                return this.grid;
                            },

                            /**
                             * возвращает все выделенные строки
                             */
                            getAllSelectedRows: function () {
                                if (ngModel) {
                                    return ngModel.$viewValue;
                                }
                                else {
                                    return scope.gridCtrl.$getPageSelection();
                                }
                            },

                            /**
                             * возвращает все выделенные строки на текущей странице
                             */
                            $getPageSelection: function () {
                                var g = scope.grid;

                                var data = [];
                                var rows = [];
                                if (g.getGridParam('multiselect')) {
                                    rows = g.getGridParam('selarrrow');
                                } else {
                                    var row = g.jqGrid('getGridParam', 'selrow');
                                    if (row) {
                                        rows = [row];
                                    }
                                }

                                var dataGetter = scope.gridCtrl.getRawData;
                                angular.forEach(rows, function (rowId) {
                                    data.push(dataGetter(rowId));
                                });

                                return data;
                            },

                            /**
                             *  перезагружает данные в грид с текущими параметрами
                             */
                            refreshGrid: function (page) {
                                var g = scope.grid;
                                scope.gridOptions.disableAutoLoad = false;
                                g[0].refreshIndex();
                                g.trigger("reloadGrid", [
                                    {page: page}
                                ]);
                                this.applyGrouping();
                                fillLastColumn();
                                $timeout(function () {
                                    fillHeight();
                                }, 0);
                            },

                            /**
                             * перезагружает данные в грид. переустанавливает параметр data
                             * @param data новый массив объектов для отображения
                             */
                            refreshGridData: function (data) {
                                if (scope.gridCtrl.getGrid) {
                                    var arrData = data || [];
                                    scope.gridCtrl.getGrid().jqGrid('clearGridData');
                                    scope.gridCtrl.getGrid().jqGrid('setGridParam', {data: arrData});
                                    scope.gridCtrl.refreshGrid();
                                }
                            },

                            /**
                             *  Подгоняет размеры таблицы под размер родителя. $timeout необходим
                             *  для гарантии вычисления ширины после построения DOM модели.
                             */
                            adjustGridWidthToParent: function () {
                                var that = this;
                                scope.$adjustTimer = $timeout(function () {
                                    $gridStack.adjustGridWidth(that.grid);
                                }, 0);
                            },
                            applyRowFilter: function () {
                                //noinspection JSUnresolvedVariable
                                if (scope.gridOptions.rowFilter) {
                                    var rowStyle, rowId, tr = scope.gridParams.gridItself.find("tr.jqgrow");
                                    $(tr).show();
                                    for (var i = 0; i < tr.length; i++) {
                                        rowId = tr[i].id;
                                        //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                                        rowStyle = scope.gridOptions.rowFilter(scope.gridCtrl.getRawData(rowId), scope.gridOptions.rowFilterParam);
                                        applyRowStyle(tr.eq(i), rowStyle, scope.gridOptions.rowFilterShow);
                                    }
                                }

                                if (scope.gridStorePageSettings) {
                                    scope.grid.jqGrid('setGridParam', {rowFilterShow: scope.gridOptions.rowFilterShow});
                                    GridSettingsStorage.setPageSettings(scope.grid);

                                    //Эти операции вызываются jqgrid'ом, angular при них не знает,
                                    //нужно вызвать $digest для обновления страницы (кнопки сброса настроек)
                                    if (!scope.$root.$$phase) {
                                        scope.$digest();
                                    }
                                }
                            },
                            toggleGroupTree: function () {
                                var target = $(this);
                                var currentTr = target.closest('tr');
                                var rowId = currentTr.attr('id');
                                scope.gridCtrl.toggleGroupTreeRecursive(rowId, true);
                            },
                            toggleGroupTreeRecursive: function (rowId, changeState) {
                                var tr = scope.gridParams.gridItself.find('tr[id="' + rowId + '"]')[0];
                                var jTr = $(tr);
                                var state = jTr.attr('grid-group-row-collapse-state');

                                if (changeState) {
                                    if (state === 'open') {
                                        applyAttr(jTr, 'grid-group-row-collapse-state', 'close');
                                    } else {
                                        applyAttr(jTr, 'grid-group-row-collapse-state', 'open');
                                    }
                                }

                                state = jTr.attr('grid-group-row-collapse-state');
                                var childrenArray;

                                if (state === 'open') {
                                    childrenArray = scope.gridCtrl.getTreeChildren(rowId);

                                    angular.forEach(childrenArray, function (child) {
                                        var childRowId = AplanaUtils.getObjectPropertyValueByPath(child, scope.gridCtrl.$getKeyAttribute());
                                        var trChild = scope.gridParams.gridItself.find('tr[id="' + childRowId + '"]')[0];

                                        $(trChild).show();
                                        scope.gridCtrl.toggleGroupTreeRecursive(childRowId, false);
                                    });
                                } else {
                                    childrenArray = scope.gridCtrl.getTreeRecursiveChildren(rowId);

                                    angular.forEach(childrenArray, function (child) {
                                        var childRowId = AplanaUtils.getObjectPropertyValueByPath(child, scope.gridCtrl.$getKeyAttribute());
                                        var trChild = scope.gridParams.gridItself.find('tr[id="' + childRowId + '"]')[0];

                                        $(trChild).hide();
                                    });
                                }
                            },
                            clickGroupTreeCheckbox: function () {
                                var currentTr = $(this).closest('tr');
                                var checkbox = currentTr.find('.cbox');
                                var isChecked = checkbox[0].checked;

                                // При выборе родительского элемента, автоматом выбираются дочерние элементы
                                // При снятии отметки с родительского элемента, отметка снимается и с дочерних
                                var rowId = currentTr.attr('id');
                                var childrenArray = scope.gridCtrl.getTreeRecursiveChildren(rowId);
                                angular.forEach(childrenArray, function (child) {
                                    var childRowId = AplanaUtils.getObjectPropertyValueByPath(child, scope.gridCtrl.$getKeyAttribute());
                                    var checkbox = scope.gridParams.gridItself.find('tr[id="' + childRowId + '"]').find('.cbox[type="checkbox"]')[0];

                                    if (checkbox.checked !== isChecked) {
                                        scope.grid.setSelection(childRowId, true);
                                    }
                                });

                                // Обновим модель
                                if (ngModel) {
                                    scope.$apply(read());
                                }

                                angular.forEach(childrenArray, function (child) {
                                    var childRowId = AplanaUtils.getObjectPropertyValueByPath(child, scope.gridCtrl.$getKeyAttribute());
                                    var checkbox = scope.gridParams.gridItself.find('tr[id="' + childRowId + '"]').find('.cbox[type="checkbox"]')[0];

                                    if (scope.gridCtrl.hasTreeChildren(childRowId)) {
                                        scope.gridCtrl.updateGrayTreeCheckbox(childRowId);
                                    }
                                });
                                scope.gridCtrl.updateGrayTreeCheckbox(rowId);
                            },
                            clickMultiselectCheckbox: function () {
                                // При любом действии - убрать серые чекбоксы
                                applyRowStyle(scope.gridParams.gridItself.find('tr').find('.cbox[type="checkbox"]'), {className: 'opacityCbox'}, false);
                            },
                            clickSubGroupTreeCheckbox: function () {
                                var currentTr = $(this).closest('tr');
                                var rowId = currentTr.attr('id');

                                // При снятии отметки с одного или нескольких элементов галка у родительского элемента становится серой
                                var parentArray = scope.gridCtrl.getTreeRecursiveParent(rowId);

                                angular.forEach(parentArray, function (parent) {
                                    var parentId = AplanaUtils.getObjectPropertyValueByPath(parent, scope.gridCtrl.$getKeyAttribute());
                                    scope.gridCtrl.updateGrayTreeCheckbox(parentId);
                                });
                            },
                            updateGrayTreeCheckbox: function (rowId) {
                                setTimeout(function () {
                                    // При снятии отметки с одного или нескольких элементов галка у родительского элемента становится серой
                                    var checkbox = scope.gridParams.gridItself.find('tr[id="' + rowId + '"]').find('.cbox[type="checkbox"]')[0];

                                    if (checkbox.checked && !scope.gridCtrl.isAllChildrenTreeSelect(rowId)) {
                                        applyRowStyle($(checkbox), {className: 'opacityCbox'}, true);
                                    } else {
                                        applyRowStyle($(checkbox), {className: 'opacityCbox'}, false);
                                    }
                                }, 0);
                            },
                            getGrayCheckbox: function () {
                                var result = [];

                                angular.forEach(scope.gridParams.gridItself.find('.cbox.opacityCbox[type="checkbox"]'), function (element) {
                                    var tr = $(element).closest('tr');
                                    var rowId = $(tr).attr('id');

                                    result.push(scope.gridCtrl.getRawData(rowId));
                                });

                                return result;
                            },
                            isAllChildrenTreeSelect: function (rowId) {
                                var childrenArray = scope.gridCtrl.getTreeRecursiveChildren(rowId);
                                var selectedArray = scope.gridCtrl.getAllSelectedRows();

                                for (var i = 0; i < childrenArray.length; ++i) {
                                    if (selectedArray.indexOf(childrenArray[i]) === -1) {
                                        return false;
                                    }
                                }

                                return true;
                            },
                            hasTreeChildren: function (rowId) {
                                var node = this.gridNodeArray[rowId];
                                var nextNode = node.nextNode;

                                return nextNode && nextNode.level > node.level;
                            },
                            getTreeLevel: function (rowId) {
                                var result = [];
                                var entity = this.getRawData(rowId);

                                if (scope.gridOptions.groupConfig.tree.rootEntity) {
                                    entity = AplanaUtils.getObjectPropertyValueByPath(entity, scope.gridOptions.groupConfig.tree.rootEntity);
                                }

                                var parent = AplanaUtils.getObjectPropertyValueByPath(entity, scope.gridOptions.groupConfig.tree.parent);

                                while (parent) {
                                    result.push(parent);
                                    parent = AplanaUtils.getObjectPropertyValueByPath(parent, scope.gridOptions.groupConfig.tree.parent);
                                }

                                return result.length;
                            },
                            getTreeRecursiveParent: function (rowId) {
                                var result = [];
                                var parentNode = this.gridNodeArray[rowId].parent;

                                while (parentNode) {
                                    result.push(scope.gridCtrl.getRawData(parentNode.rowId));
                                    parentNode = parentNode.parent;
                                }

                                return result;
                            },
                            getTreeChildren: function (rowId) {
                                var result = [];

                                angular.forEach(this.gridNodeArray[rowId].child, function (childNode) {
                                    result.push(scope.gridCtrl.getRawData(childNode.rowId));
                                });

                                return result;
                            },
                            getTreeRecursiveChildren: function (rowId) {
                                var result = [];

                                angular.forEach(this.gridNodeArray[rowId].child, function (childNode) {
                                    result.push(scope.gridCtrl.getRawData(childNode.rowId));
                                    if (childNode.child.length > 0) {
                                        result.push.apply(result, scope.gridCtrl.getTreeRecursiveChildren(childNode.rowId));
                                    }
                                });

                                return result;
                            },
                            applyGrouping: function () {
                                if (scope.gridOptions.groupConfig) {
                                    var tr = scope.gridParams.gridItself.find("tr.jqgrow");

                                    // Для грида-дерева
                                    if (scope.gridOptions.groupConfig.tree) {
                                        scope.gridCtrl.applyTreeGrouping();
                                    }

                                    if (scope.gridOptions.groupConfig.noTree) {
                                        scope.gridCtrl.applyNoTreeGrouping();
                                    }
                                }
                            },
                            createStructure: function () {
                                this.gridNodeArray = {};

                                var prevNode;
                                var parentStack = [];

                                for (var i = 0; i < scope.$rawData.length; i++) {
                                    var rowId = AplanaUtils.getObjectPropertyValueByPath(scope.$rawData[i], scope.gridCtrl.$getKeyAttribute());
                                    var level = scope.gridCtrl.getTreeLevel(rowId);

                                    var node = {
                                        rowId: rowId,
                                        level: level,
                                        nextNode: null,
                                        prevNode: prevNode,
                                        parent: null,
                                        child: []
                                    };
                                    this.gridNodeArray[rowId] = node;

                                    if (prevNode) {
                                        prevNode.nextNode = node;

                                        if (prevNode.level < node.level) {
                                            node.parent = prevNode;
                                            node.parent.child.push(node);
                                            parentStack.push(prevNode);
                                        } else if (prevNode.level === node.level) {
                                            node.parent = prevNode.parent;
                                            if (node.parent) {
                                                node.parent.child.push(node);
                                            }
                                        } else if (prevNode.level > node.level) {
                                            for (var s = 0; s < (prevNode.level - node.level); ++s) {
                                                node.parent = parentStack.pop();
                                            }

                                            node.parent = parentStack[parentStack.length - 1];

                                            if (node.parent) {
                                                node.parent.child.push(node);
                                            }
                                        }
                                    } else {
                                        node.parent = null;
                                    }

                                    prevNode = node;
                                }
                            },
                            applyTreeGrouping: function () {
                                var trArray = scope.gridParams.gridItself.find("tr.jqgrow");
                                this.createStructure();

                                for (var i = 0; i < trArray.length; i++) {
                                    var tr = trArray.eq(i);
                                    var rowId = trArray[i].id;
                                    var level = this.gridNodeArray[rowId].level;

                                    applyRowStyle(tr, {className: 'grid-group-row'}, true);
                                    applyAttr(tr, 'grid-group-level', level);

                                    var hasChildren = this.hasTreeChildren(rowId);

                                    // Если это группирующая строка
                                    if (hasChildren) {
                                        applyRowStyle(tr, {className: 'grid-group-row-parent'}, true);
                                        applyRowStyle(tr, {className: 'grid-group-row-parent-open'}, true);
                                        applyAttr(tr, 'grid-group-row-collapse-state', 'open');
                                        var clickableCollapse = $(tr.find(".grid-group-row-collapse")[level]);
                                        applyRowStyle(clickableCollapse, {className: 'grid-group-row-collapse-clickable'}, true);
                                    }

                                    // С. Юшкова: "Выделять жирным, только тех, у кого есть дети и первый уровень"
                                    if (hasChildren || level === 0) {
                                        applyRowStyle(tr, {style: 'font-weight', value: 'bold'}, true);
                                    }
                                }

                                // Действие для свернуть/развернуть группу
                                // Вначале убираем старое событие, если оно есть
                                grid.off("click", "td:has(.grid-group-row-collapse-clickable)", scope.gridCtrl.toggleGroupTree);
                                grid.on("click", "td:has(.grid-group-row-collapse-clickable)", scope.gridCtrl.toggleGroupTree);

                                // В гриде нет чекбоксов - не надо добавлять события
                                if (!scope.gridOptions.groupConfig.noSelect) {
                                    // Событие для чекбокса у группы
                                    // Вначале убираем старое событие, если оно есть
                                    grid.off("click", ".grid-group-row-parent td:not(.unselectableTd)", scope.gridCtrl.clickGroupTreeCheckbox);
                                    grid.on("click", ".grid-group-row-parent td:not(.unselectableTd)", scope.gridCtrl.clickGroupTreeCheckbox);

                                    // Событие для чекбокса в группе
                                    // Вначале убираем старое событие, если оно есть
                                    grid.off("click", ".grid-group-row td:not(.unselectableTd)", scope.gridCtrl.clickSubGroupTreeCheckbox);
                                    grid.on("click", ".grid-group-row td:not(.unselectableTd)", scope.gridCtrl.clickSubGroupTreeCheckbox);

                                    // Событие для чекбокса в мультиселекте
                                    grid.off("click", ".ui-jqgrid-labels .cbox[type=checkbox]", scope.gridCtrl.clickMultiselectCheckbox);
                                    grid.on("click", ".ui-jqgrid-labels .cbox[type=checkbox]", scope.gridCtrl.clickMultiselectCheckbox);
                                }
                            },
                            applyNoTreeGrouping: function () {
                                var tr = scope.gridParams.gridItself.find("tr.jqgrow");
                                var prevRow;
                                var prevGroupId;

                                for (var i = 0; i < tr.length; i++) {
                                    var rowId = tr[i].id;
                                    var groupId = this.getGroupId(rowId);

                                    applyRowStyle(tr.eq(i), {className: 'grid-group-row'}, true);
                                    // Добавляем псевдострочку для родительской записи, если ее нет.
                                    // Меняем только DOM, модель и данные не меняются
                                    // != - может быть сравнение строки и числа
                                    if (!prevRow || (prevGroupId !== groupId)) {
                                        var groupObject = this.getGroup(rowId);

                                        // клонируем колонку
                                        var cloneTr = tr.eq(i).clone();
                                        cloneTr.attr('group', groupId);
                                        cloneTr.removeAttr('id');

                                        // Проставляем значения для родительско строки
                                        var colModel = scope.grid.getGridParam('colModel');
                                        for (var j = 0; j < colModel.length; ++j) {
                                            var index = colModel[j].index;
                                            var formatter = colModel[j].formatter;

                                            if (index) {
                                                var value = AplanaUtils.getObjectPropertyValueByPath(groupObject, scope.gridOptions.groupConfig.noTree.groupColumnModel[index]);
                                                var formatValue = colModel[j].groupModel ? formatter(value, null, groupObject) : value;
                                                var escapeIndex = index.replace('.', '\\.');
                                                cloneTr.find('[aria-describedby$="_' + escapeIndex + '"]').html(formatValue ? formatValue : '');
                                            }
                                        }

                                        cloneTr.find('.cbox').remove();

                                        applyRowStyle(cloneTr, {className: 'grid-group-row-group'}, true);
                                        // Настройка группировки
                                        applyRowStyle(cloneTr, {style: 'font-weight', value: 'bold'}, true);
                                        applyRowStyle(cloneTr, {className: 'grid-group-row-pseudo'}, true);
                                        applyRowStyle(cloneTr, {className: 'grid-group-row'}, true);

                                        applyAttr(cloneTr, 'grid-group-row-collapse-state', 'open');
                                        applyRowStyle(cloneTr.find(".grid-group-row-collapse"), {className: 'grid-group-row-collapse-clickable'}, true);

                                        cloneTr.insertBefore(tr.eq(i));
                                    }

                                    applyAttr(tr.eq(i), 'groupId', groupId);
                                    prevRow = tr.eq(i);
                                    prevGroupId = groupId;

                                    tr.eq(i).show();
                                }

                                // Действие для свернуть/развернуть группу
                                // Вначале убираем старое событие, если оно есть
                                grid.off("click", "td:has(.grid-group-row-collapse-clickable)", scope.gridCtrl.toggleGroupNoTree);
                                grid.on("click", "td:has(.grid-group-row-collapse-clickable)", scope.gridCtrl.toggleGroupNoTree);
                            },
                            getGroup: function (rowId) {
                                return AplanaUtils.getObjectPropertyValueByPath(this.getRawData(rowId), scope.gridOptions.groupConfig.noTree.groupObject);
                            },
                            getGroupId: function (rowId) {
                                return AplanaUtils.getObjectPropertyValueByPath(this.getRawData(rowId), scope.gridOptions.groupConfig.noTree.groupId);
                            },
                            toggleGroupNoTree: function () {
                                var currentCollapse = $(this).find('.grid-group-row-collapse-clickable');
                                var currentTr = currentCollapse.closest('tr');
                                var groupId = currentTr.attr('group');
                                var state = currentTr.attr('grid-group-row-collapse-state');

                                if (state === 'open') {
                                    applyAttr(currentTr, 'grid-group-row-collapse-state', 'close');
                                } else {
                                    applyAttr(currentTr, 'grid-group-row-collapse-state', 'open');
                                }

                                var trArray = scope.gridParams.gridItself.find('tr[groupId="' + groupId + '"]');

                                trArray.each(function (index, tr) {
                                    if ($(tr).is(':hidden')) {
                                        $(tr).show();
                                    } else {
                                        $(tr).hide();
                                    }
                                });
                            },
                            clickGroupNoTreeCheckbox: function () {
                                var currentCB = $(this);
                                var currentTr = currentCB.closest('tr');
                                var groupId = currentTr.attr('group');
                                var isChecked = this.checked;

                                var checkboxes = scope.gridParams.gridItself.find('tr[groupId="' + groupId + '"]').find('.cbox[type="checkbox"]');

                                checkboxes.each(function (index, checkbox) {
                                    if (checkbox.checked !== isChecked) {
                                        scope.grid.setSelection($(this).closest('tr').attr('id'), true);
                                    }
                                });

                                // Обновим модель
                                if (ngModel) {
                                    scope.$apply(read());
                                }

                            },
                            /**
                             * Получает количество всех записей
                             */
                            getCountRecords: function () {
                                return scope.grid.jqGrid('getGridParam', 'records');
                            },

                            resetSettings: function () {
                                if (scope.gridStorePageSettings || scope.gridStoreColSettings) {
                                    GridSettingsStorage.resetSettings(scope.grid, scope.gridParams.gridItself, initialConfig);
                                    fillLastColumn();
                                }
                            },

                            /**
                             * Проверяет содержится ли запись с указанным идентификатором на текущей странице
                             *
                             * @param recordId идентификатор замиси
                             * @returns {*} факт нахождения записи на текущей странице
                             */
                            containsRecord: function (recordId) {
                                var ids = scope.grid.jqGrid('getDataIDs');

                                var softContains = function (collection, value) {
                                    var result = false;

                                    angular.forEach(collection, function (item) {
                                        //в jqgrid'е идентификаторы могут являться строками, сравнение должно быть ==
                                        result = result || (item === value);
                                    });

                                    return result;
                                };

                                return softContains(ids, recordId);
                            },
                            updateModel: function () {
                                if (ngModel) {
                                    setTimeout(function () {
                                        scope.$apply(read());
                                    }, 0);
                                }
                            },
                            beforeSelectRow: undefined,
                            onSelectRow: undefined,
                            onSelectAll: undefined,
                            gridComplete: undefined,
                            loadComplete: undefined
                        };

                        // Чтобы id и name div и table не дублировались,
                        // явно задаем id и name у table
                        scope.grid.divId = element[0].attributes.id.value;
                        scope.grid.tableId = 'tbl_' + element[0].attributes.id.value;
                        scope.grid.attr('id', scope.grid.tableId);
                        scope.grid.attr('name', scope.grid.tableId);

                        // получаем параметры снаружи
                        var gridConfig = angular.copy(scope.gridOptions);
                        var initialConfig = angular.copy(scope.gridOptions);

                        if (attrs.ngModel) {
                            //Заполним gridName, если оно пусто
                            var gridId = AplanaUtils.buildModelId(attrs.ngModel);
                            if (!gridConfig.gridName) {
                                gridConfig.gridName = gridId;
                            }
                        }

                        if (scope.gridStorePageSettings) {
                            GridSettingsStorage.loadPageSettings(gridConfig);
                            scope.gridOptions.rowFilterShow = gridConfig.rowFilterShow;
                        }

                        if (scope.gridStoreColSettings) {
                            GridSettingsStorage.loadColSettings(gridConfig);
                        }

                        // включим пейджинг, если не указано обратное
                        if (attrs.gridNoPaging === undefined) {
                            scope.paginator.attr('id', scope.grid.attr('id') + "paginator");
                            gridConfig.pager = '#' + scope.paginator.attr('id');
                        } else {
                            // По умолчанию в jqGrid количество отображаемых строк на странице равно 20,
                            // а при отключеном пейджинге нужно отображать все записи.
                            // Не работает с datatype=local, нам нужно при перезагрузке данных менять не только data,
                            // но и rowNum:  scope.grid.jqGrid('setGridParam', { data: data, rowNum: data.length );
                            // а так же не забывать задавать rowNum в конфиге грида
                            if (gridConfig.datatype !== 'local') {
                                gridConfig.rowNum = -1;
                            }

                            // Флаг отображения панельки отображающее количество всех записей
                            scope.viewRecordWithNoPaging = gridConfig.viewrecords;
                        }

                        // adjust grid width feature
                        var disableAdjustWidth = scope.gridDisableAdjustWidth;
                        var enableAdjustWidthByColumns = scope.gridEnableAdjustWidthByColumns;
                        var parentAdjustWidthCssSelector = scope.gridParentAdjustWidthCssSelector;
                        if (!disableAdjustWidth) {
                            $gridStack.registerAdjustWidthGrid(scope.grid, gridConfig, enableAdjustWidthByColumns, parentAdjustWidthCssSelector);
                        }

                        // получим данные, если указаны
                        var data = scope.gridData;
                        if (data) {
                            gridConfig.data = data;

                            gridConfig.data.forEach(function (element, index) {
                                gridConfig.data[index] = AplanaUtils.sanitizeRecursively(element);
                            });
                        }

                        scope.$multiSelect = gridConfig.multiselect;
                        if (gridConfig.aplanaSingleSelect) {
                            gridConfig.multiselect = true;
                            scope.$multiSelect = false;
                        }

                        if (gridConfig.multiselect) {
                            if (element[0].className.indexOf("cbr-firstColCheckBox-grid") === -1) {
                                element[0].className += " cbr-firstColCheckBox-grid";
                            }
                        }

                        if (gridConfig.datatype === 'angularResource') {
                            gridConfig.datatype = function (postdata) {
                                var requestParameters = (_.isFunction(gridConfig.requestParameters) ?
                                    gridConfig.requestParameters() : gridConfig.requestParameters) || {};
                                requestParameters = angular.extend($pagingBuilder(postdata), requestParameters);
                                requestParameters.pagingParams = JSON.stringify({
                                    page: requestParameters.page,
                                    count: requestParameters.rows,
                                    startIndex: requestParameters.page === 1 ? 0 : requestParameters.rows * (requestParameters.page - 1),
                                    property: postdata.sidx,
                                    direction: postdata.sord
                                });

                                var gridLoadData = function () {
                                    gridConfig.angularResource.query(requestParameters, function (data) {
                                        scope.gridParams.bdiv.css({
                                            height: 'auto'
                                        });

                                        if (data && data.rows) {
                                            data.rows.forEach(function (element, index) {
                                                data.rows[index] = AplanaUtils.sanitizeRecursively(element);
                                            });
                                        }

                                        scope.grid[0].addJSONData(data);
                                        scope.grid[0].p.loadComplete.call(scope.grid[0], data);

                                        if (ngModel) {
                                            scope.$phaseSaveCall(ngModel.$render);
                                        }

                                        // вызываем подстройку по ширине родителя т.к. возможно
                                        // что новые данные привели к вертикальной полосе прокрутки
                                        // scope.gridCtrl.adjustGridWidthToParent();
                                        fillHeight();
                                    });
                                };

                                if (!scope.gridOptions.disableAutoLoad) {
                                    gridLoadData();
                                }
                            };
                        }

                        var checkAllSelection = function () {
                            var grid = scope.grid;
                            var checkbox = element.find("#cb_" + grid[0].id)[0];
                            if (checkbox) {
                                checkbox.checked = (grid.getRowData().length === scope.gridCtrl.$getPageSelection().length) &&
                                    (scope.gridCtrl.$getPageSelection().length !== 0);
                                checkbox = undefined;
                            }
                        };

                        /**
                         * Заполняет последней колонкой пустое пространство в теле грида
                         */
                        var fillLastColumn = function () {
                            if (scope && scope.gridFillSpace) {
                                var table = scope.gridParams.bdiv.find("table");

                                var gridWidth = scope.gridParams.bdiv[0].clientWidth;
                                var tableWidth = table.width();

                                var hasScroll = scope.gridParams.bdiv[0].scrollHeight > scope.gridParams.bdiv.height();
                                if (gridWidth + (hasScroll ? 20 : 0) > tableWidth) {
                                    var colmodel = scope.grid.jqGrid('getGridParam', 'colModel');

                                    //На это изменение колонки не нужно реагироват при сохранении настроек грида
                                    scope.$ignoreColChanges = true;
                                    var generalColumn = colmodel.length - 1;
                                    colmodel.forEach(function (column) {
                                        if (column.general){
                                            generalColumn = colmodel.indexOf(column);
                                        }
                                    });
                                    scope.grid.jqGrid('setColWidth', generalColumn, table[0].grid.headers[generalColumn].width + gridWidth - tableWidth, false);
                                    scope.$ignoreColChanges = false;
                                }
                            }
                        };

                        /**
                         * Включает подстройку ширины грида при расширении последней колонки.
                         */
                        angular.extend(gridConfig, {
                            resizeStart: function (event, index) {
                                scope.gridParams.allowResize = true;

                                if (gridConfig.fullScreen !== undefined) {
                                    // Если изменяется последний столбец и правая граница последнего столбца уперлась в границу грида.
                                    if (this.grid.headers.length === index + 1 && $(this).width() >= this.grid.width - scope.gridParams.scrollOffset) {
                                        scope.gridParams.width = this.grid.headers[index].width;
                                    }
                                }
                            },
                            resizeStop: function (newwidth, index) {
                                if (scope.gridParams.allowResize) {
                                    scope.gridParams.allowResize = false;

                                    if (gridConfig.fullScreen !== undefined) {
                                        if (this.grid.headers.length === index + 1) {
                                            if (newwidth >= scope.gridParams.width) {
                                                // Последний столбец расширяется сразу на 100
                                                scope.grid.jqGrid('setColWidth', index, this.grid.headers[index].width + 100, false);

                                                // Скролл до конца вправо
                                                scope.gridParams.bdiv.scrollLeft($(this).width() - this.grid.width + 100);
                                            }
                                        }
                                    }
                                }

                                if (scope.gridStoreColSettings && !scope.$ignoreColChanges) {
                                    GridSettingsStorage.applyColSettings(scope.grid);

                                    //Эти операции вызываются jqgrid'ом, angular при них не знает,
                                    //нужно вызвать $digest для обновления страницы (кнопки сброса настроек)
                                    if (!scope.$root.$$phase) {
                                        scope.$digest();
                                    }
                                }

                                if (scope.gridCtrl.resizeStop) {
                                    scope.gridCtrl.resizeStop(newwidth, index);
                                }
                            }
                        });

                        angular.extend(gridConfig, {
                            deepempty: true,
                            hidegrid: false,
                            sortable: function () {
                                if (scope.gridStoreColSettings) {
                                    GridSettingsStorage.applyColSettings(scope.grid);

                                    //Эти операции вызываются jqgrid'ом, angular при них не знает,
                                    //нужно вызвать $digest для обновления страницы (кнопки сброса настроек)
                                    if (!scope.$root.$$phase) {
                                        scope.$digest();
                                    }
                                }
                            },
                            beforeSelectRow: function (rowId, e) {
                                if (!!gridConfig.disableRowSelect) {
                                    return $(e.target).is("input:checkbox");
                                }
                                if (!scope.$multiSelect) {
                                    scope.grid.resetSelection();
                                }
                                if (scope.gridCtrl.beforeSelectRow) {
                                    return scope.gridCtrl.beforeSelectRow(rowId, e);
                                }
                                // Не реагировать на клик по +/-
                                if ($(e.target).is(".grid-group-row-collapse") || $(e.target).is(".unselectableTd")) {
                                    return false;
                                }
                                // Не ставить чекбокс для псевдозаписей
                                if ($(e.target).parent().is(".grid-group-row-pseudo")) {
                                    return false;
                                }
                                return true;
                            },
                            onSelectRow: function (rowId, status, e) {
                                if (scope.gridCtrl.onSelectRow) {
                                    scope.gridCtrl.onSelectRow(rowId, status, e);
                                }
                                if (scope.gridOptions.onSelectRow) {
                                    scope.gridOptions.onSelectRow(rowId, status, e);
                                }
                                if (ngModel) {
                                    read();
                                    if ((!scope.$$phase) && (!scope.$root.$$phase)) {
                                        scope.$apply();
                                    }
                                    //scope.$apply(read());
                                }

                                checkAllSelection();
                            },
                            onSelectAll: function (aRowids, status) {
                                if (!scope.$multiSelect) {
                                    scope.grid.resetSelection();
                                }
                                if (scope.gridCtrl.onSelectAll) {
                                    scope.gridCtrl.onSelectAll(aRowids, status);
                                }
                                if (scope.gridOptions.onSelectAll) {
                                    scope.gridOptions.onSelectAll(aRowids, status);
                                }

                                // выставляем значения у чекбоксов в группировках
                                scope.grid.find("input.groupHeader").prop('checked', status);

                                if (ngModel) {
                                    scope.$apply(read());
                                }
                            },
                            loadComplete: function (data) {
                                if ((data.total >= 1) && (data.page > data.total)) {
                                    scope.gridCtrl.refreshGrid(data.total);

                                    return;
                                }

                                if (scope.gridCtrl.loadComplete) {
                                    scope.gridCtrl.loadComplete(data);
                                }

                                if (scope.gridStorePageSettings) {
                                    GridSettingsStorage.setPageSettings(scope.grid);

                                    //Эти операции вызываются jqgrid'ом, angular при них не знает,
                                    //нужно вызвать $digest для обновления страницы (кнопки сброса настроек)
                                    if (!scope.$root.$$phase) {
                                        scope.$digest();
                                    }
                                }

                                scope.$rawData = data.rows || data;

                                $rootScope.$broadcast('GRID_CHANGED_MSG');
                            },
                            afterInsertRow: function (rowId, rowdata, rowelem) {
                                if (scope.gridOptions.rowFilter) {
                                    var tr = scope.gridParams.gridItself.find("tr.jqgrow#" + rowId);

                                    var rowStyle = scope.gridOptions.rowFilter(rowelem, scope.gridOptions.rowFilterParam);
                                    applyRowStyle(tr, rowStyle, scope.gridOptions.rowFilterShow);
                                }
                            }
                        });

                        // добавляем колонки для группировки
                        if (scope.gridOptions.groupConfig) {
                            var cellAttrFunction = function (rowId, tv, rawObject, cm, rdata) {
                                return ' style="cursor: pointer" class="unselectable unselectableTd"';
                            };

                            for (var i = 0; i < scope.gridOptions.groupConfig.level; ++i) {
                                gridConfig.colNames.unshift('');
                                gridConfig.colModel.unshift({
                                    name: 'parent_' + i,
                                    index: 'parent_' + i,
                                    width: 15,
                                    sortable: false,
                                    align: 'center',
                                    formatter: scope.gridOptions.groupConfig.colModel.formatter,
                                    sorttype: scope.gridOptions.groupConfig.colModel.sorttype,
                                    cellattr: cellAttrFunction,
                                    groupModel: true
                                });
                            }

                            gridConfig.sortname = 'parent_0';

                            // Принудительно дизейблим все сортировка и ресайзы
                            for (var j = 0; j < gridConfig.colModel.length; ++j) {
                                gridConfig.colModel[j].resizable = false;
                                gridConfig.colModel[j].sortable = false;
                            }
                            gridConfig.sortable = false;
                        }

                        var grid = null;

                        // строим грид
                        scope.buildGrid = function () {
                            scope.grid.jqGrid(gridConfig);
                            scope.grid.navGrid(gridConfig.pager, {
                                search: false,
                                edit: false,
                                add: false,
                                del: false,
                                refresh: gridConfig.datatype !== 'local',
                                refreshstate: "current"
                            });

                            scope.viewrecords.appendTo(element.find("div.ui-jqgrid.ui-widget"));
                            // легенда должна быть частью грида, переместим ее в нужный контейнер
                            scope.legend.appendTo(element.find("div.ui-jqgrid.ui-widget"));

                            // Запоминаются параметры грида для режима fullScreen
                            grid = $("#" + scope.grid.divId);
                            scope.gridParams.gridItself = grid;
                            scope.gridParams.scrollOffset = scope.grid.jqGrid('getGridParam', 'scrollOffset');
                            scope.gridParams.bdiv = grid.find('.ui-jqgrid-bdiv');
                            scope.gridParams.hdiv = grid.find('.ui-jqgrid-hdiv');
                            scope.gridParams.fullScreen = gridConfig.fullScreen;
                            scope.grid.jqGrid("setGridParam", {"fullScreen": scope.gridParams.fullScreen});

                            stopOuterScroll(scope.gridParams.bdiv);

                            scope.grid.setGridParam({
                                gridComplete: function (data) {
                                    if (scope.gridCtrl.gridComplete) {
                                        scope.gridCtrl.gridComplete(data);
                                    }
                                    if (ngModel && (scope.$rawData !== undefined)) {
                                        if (scope.gridKeepSelection === 'true') {
                                            /**
                                             * были проблемы с ajax загрузкой данных, он тоже вызывает digest
                                             */
                                            scope.$phaseSaveCall(ngModel.$render);
                                        }
                                        else {
                                            ngModel.$setViewValue([]);
                                        }

                                        if (scope.$root.$$phase) {
                                            ngModel.$setPristine();
                                        }
                                        else {
                                            scope.$apply(ngModel.$setPristine());
                                        }
                                    }
                                    setTimeout(function () {
                                        fillLastColumn();
                                    }, 0);
                                }
                            });

                            // сохраним ссылку на грид
                            scope.gridCtrl.registerGridElement(scope.grid);
                        };

                        // Перестраивает грид
                        scope.gridCtrl.rebuildGrid = function () {
                            scope.grid.gridUnload();

                            angular.extend(gridConfig, {
                                colNames: scope.gridOptions.colNames,
                                colModel: scope.gridOptions.colModel
                            });

                            scope.grid = getGrid(element);
                            scope.paginator = getPaginator(element);
                            scope.legend = getLegend(element);
                            scope.viewrecords = getViewRecords(element);
                            scope.$rawData = undefined;
                            scope.$multiSelect = !!scope.gridOptions;
                            scope.gridParams = {};

                            scope.grid.divId = element[0].attributes.id.value;
                            scope.grid.tableId = 'tbl_' + element[0].attributes.id.value;
                            scope.grid.attr('id', scope.grid.tableId);
                            scope.grid.attr('name', scope.grid.tableId);

                            scope.buildGrid();

                            $timeout(function () {
                                fillHeight();
                                fillLastColumn();
                            }, 0);
                        };

                        // строим грид
                        scope.buildGrid();

                        // подгружаем в модель изменения в гриде
                        function read() {
                            if (scope.$rawData === undefined) {
                                return;
                            }

                            var modelValue = [];
                            if (scope.gridKeepSelection === 'true') {
                                var rows = scope.$rawData;
                                var keyAttribute = scope.gridCtrl.$getKeyAttribute();
                                var keys = [];
                                modelValue = ngModel.$viewValue || [];

                                angular.forEach(rows, function (row) {
                                    keys.push(_.deep(row, keyAttribute));
                                });

                                var i = 0;
                                while (i < modelValue.length) {
                                    if (_.contains(keys, _.deep(modelValue[i], keyAttribute))) {
                                        modelValue.splice(i, 1);
                                    }
                                    else {
                                        i++;
                                    }
                                }

                                modelValue = modelValue.concat(scope.gridCtrl.$getPageSelection());
                            }
                            else {
                                modelValue = scope.gridCtrl.$getPageSelection();
                            }

                            if (!scope.$multiSelect) {
                                modelValue = modelValue.slice(-1);
                            }
                            ngModel.$setViewValue(modelValue);
                        }

                        //если есть связь с моделью
                        if (ngModel) {
                            ngModel.$parsers.push(validateGridRequired);
                            ngModel.$formatters.unshift(validateGridRequired);

                            scope.gridCtrl.setRequired = function () {
                                validateGridRequired(ngModel.$viewValue);
                                ngModel.$setPristine();
                            };

                            //подключем фильтры и валидаторы
                            scope.$watch('gridRequired', function (value) {
                                scope.gridCtrl.setRequired(value);
                            });

                            //при смене модели меняем выбранные строки
                            ngModel.$render = function () {
                                scope.grid.resetSelection();

                                if (ngModel.$viewValue) {
                                    if (!scope.$multiSelect && ngModel.$viewValue && (ngModel.$viewValue.length > 1)) {
                                        ngModel.$setViewValue(ngModel.$viewValue.slice(-1));
                                    }

                                    var keyAttribute = scope.gridCtrl.$getKeyAttribute();
                                    for (var i = 0; i < ngModel.$viewValue.length; i++) {
                                        var elementId = _.deep(ngModel.$viewValue[i], keyAttribute);
                                        if (!angular.isUndefined(elementId)) {
                                            var rawData = scope.gridCtrl.getRawData(elementId);
                                            if (rawData) {
                                                angular.extend(ngModel.$viewValue[i], rawData);
                                            }

                                            scope.grid.setSelection(elementId, false);
                                        }
                                    }
                                }

                                checkAllSelection();
                            };
                        }

                        /**
                         * Растягивает грид до заполнения контейнера
                         */
                        function fillHeight() {
                            if (scope && scope.gridFillSpace && !element.hasClass('flex-grid')) {
                                if ($(scope.gridFillSpaceContainerSelector).is(':visible') && !element.hasClass('full-height-grid')) {
                                    var container = $(scope.gridFillSpaceContainerSelector);
                                    var containerTop = $(scope.gridFillSpaceContainerSelectorTop);
                                    var containerMinHeight = parseInt($(scope.gridFillSpaceContainerSelector).css('min-height'), 10);
                                    var view = $(scope.gridFillSpaceViewSelector);
                                    var table = scope.gridParams.bdiv.find("table");

                                    var containerHeight = container.height();
                                    var viewHeight = view.height();
                                    var tableHeight = table.height();
                                    var bdivHeight = scope.gridParams.bdiv.height();
                                    var containerTopHeight = containerTop.height();

                                    if (!!containerMinHeight) {
                                        containerHeight = containerHeight - (bdivHeight - tableHeight);
                                        if (containerHeight < containerMinHeight) {
                                            containerHeight = containerMinHeight;
                                        }
                                    }
                                    viewHeight = viewHeight - (bdivHeight - tableHeight);

                                    if (!!containerMinHeight && (viewHeight > containerMinHeight)) {
                                        scope.gridParams.bdiv.css({
                                            height: 'auto'
                                        });
                                    } else {
                                        if (scope.gridFillSpaceContainerSelectorTop === undefined) {
                                            scope.gridParams.bdiv.css({
                                                height: Math.max(tableHeight, containerHeight - viewHeight + tableHeight)
                                            });

                                        } else { // если указан gridFillSpaceContainerSelectorTop то высоту считает так:
                                            //высота всего грида = (родительский контейнер).высота минус  (контейнер для фильтра).высота
                                            var tempHeight = containerHeight - containerTopHeight;
                                            // высота панели по "восстановить по умолчанию"
                                            var heightRestore = scope.gridParams.gridItself.find('.ui-jqgrid-restore').height();

                                            scope.gridParams.bdiv.parents('.ui-jqgrid').css({
                                                height: tempHeight - heightRestore
                                            });

                                            // вычитаем высоту заголовков таблицы. пагинацию и легенду
                                            var tempOther = scope.gridParams.gridItself.find('.ui-jqgrid-htable').outerHeight() +
                                                scope.gridParams.gridItself.find('.footer').outerHeight() +
                                                scope.gridParams.gridItself.find('.viewrecords').outerHeight() +
                                                scope.gridParams.gridItself.find('.legend').outerHeight();
                                            // отдельно меняем высоту внутренней части грида, иначе скролл отображается не корректно
                                            scope.gridParams.bdiv.css({
                                                height: tempHeight - tempOther - heightRestore
                                            });
                                        }
                                    }
                                } else if (element.hasClass('full-height-grid')) {
                                    var elementHeight = element.height() - element.find('.ui-jqgrid-restore').height();
                                    var tableHeight = scope.gridParams.bdiv.find("table").height();
                                    var hdivHeight = scope.gridParams.hdiv.height();
                                    var pagerHeight = 30;
                                    var hscrollHeight = 17;
                                    if (elementHeight > hdivHeight + tableHeight + pagerHeight + hscrollHeight) {
                                        // если таблица меньше, чем есть свободного места, то растягиваем её
                                        scope.gridParams.bdiv.css({
                                            height: elementHeight - hdivHeight - pagerHeight
                                        });
                                    } else {
                                        // высота таблицы вычисляется по содержимому
                                        scope.gridParams.bdiv.css({
                                            height: 'auto'
                                        });
                                    }
                                }
                            }
                            fillLastColumn();
                        }

                        if (scope.gridOptions.grouping) {
                            // Событие для чекбокса в группировке
                            grid.on("change", ".groupHeader[type=checkbox]", function () {
                                var currentCB = $(this);
                                var isChecked = this.checked;

                                //if group header is checked, to check all child checkboxes
                                var checkboxes = currentCB.closest('tr').nextUntil('tr.jqgroup').find('.cbox[type="checkbox"]');

                                checkboxes.each(function (index, checkbox) {
                                    if (!checkbox.checked || !isChecked) {
                                        scope.grid.setSelection($(this).closest('tr').attr('id'), true);
                                    }
                                });

                                // Обновим модель
                                if (ngModel) {
                                    scope.$apply(read());
                                }
                            });

                            // Событие для чекбоксов в группе
                            grid.on("change", ".cbox[type=checkbox]", function () {
                                var currentCB = $(this);
                                var isChecked = this.checked;
                                var trGroupSelector = "tr." + scope.grid.tableId + "ghead_0";

                                var groupCheckbox =
                                    currentCB.closest('tr')
                                        .prevAll(trGroupSelector + ":first")
                                        .nextUntil(trGroupSelector)
                                        .andSelf()
                                        .find('[type="checkbox"]');

                                var childCheckbox = groupCheckbox.filter('.cbox');
                                var headerCheckbox = groupCheckbox.filter(".groupHeader");

                                var isAllChecked = !isChecked ? false : childCheckbox.filter(":checked").length === childCheckbox.length;
                                headerCheckbox.prop("checked", isAllChecked);
                            });
                        }

                        var getPagerSelector = function () {
                            return element.find(".ui-pg-selbox");
                        };

                        var pageSizeInput = angular.element(
                            "<label class='ui-page-size-label'></label>" +
                            "<input id='page_size_input' class='ui-page-size-input' type='text'/>");
                        pageSizeInput.val(gridConfig.rowNum);
                        pageSizeInput.keyup(function ($event) {
                            if ($event.keyCode === 13) {
                                var newRowNum = Number(this.value);
                                if (newRowNum > 0 && newRowNum <= 300) {
                                    var $selects = getPagerSelector();
                                    var $firstOption = $selects.find("option:nth-child(1)");
                                    $firstOption.text(newRowNum);
                                    $firstOption.val(newRowNum);
                                    $selects.first().val(newRowNum);
                                    $selects.first().trigger("change");
                                    this.blur();
                                } else {
                                    this.value = getPagerSelector().first().val();
                                }
                            }
                        });
                        getPagerSelector().first().after(pageSizeInput);
                        getPagerSelector().hide();

                        $timeout(function () {
                            fillHeight();
                            fillLastColumn();
                        }, 0);

                        function refreshHeightTimeout() {
                            $timeout(function () {
                                fillHeight();
                            }, 10);
                        }

                        scope.$on('WINDOW_RESIZED_MSG', function () {
                            refreshHeightTimeout();
                        });

                        scope.$on('TAB_CHANGED_MSG', function () {
                            refreshHeightTimeout();
                        });

                        scope.$on('COLLAPSE_TOGGLED_MSG', function () {
                            //нет $timeout'а, для устранения лагов отрисовки (iVolegov)
                            fillHeight();
                        });

                        scope.$on('UPDATE_VALIDATION_MESSAGE_POSITION', function () {
                            refreshHeightTimeout();
                        });

                        // обработка сообщения для пересчёта высоты грида
                        scope.$on('UPDATE_GIRD_HEIGHT', function () {
                            refreshHeightTimeout();
                        });

                        // что-то выбрано в селекте. надо пересчитать высоту, так как меняется размер фильтра
                        scope.$on('SELECT2_CHANGE', function () {
                            refreshHeightTimeout();
                        });

                        // закрытия dropdown селекта. надо пересчитать высоту, так как меняется размер фильтра
                        scope.$on('SELECT2_CLOSE', function () {
                            refreshHeightTimeout();
                        });

                        // открытие dropdown селекта. надо пересчитать высоту, так как меняется размер фильтра
                        scope.$on('SELECT2_OPEN', function () {
                            refreshHeightTimeout();
                        });

                        // инициализиуем грид в контроллере
                        scope.initGrid({ctrl: scope.gridCtrl});
                        scope.gridCtrl.applyGrouping();

                        // если указан параметр data-grid-refresh-button='fasle', то скрываем кнопку обновления грида
                        if (angular.isDefined(scope.gridRefreshButton) && !scope.gridRefreshButton) {
                            scope.grid.refreshButton = angular.element(element.find('td#refresh_tbl_' + element[0].attributes.id.value)[0]);
                            scope.grid.refreshButton.attr("hidden", "true");
                        }
                    }
                };
            }])
        .factory('$gridStack', ['$$stackedMap', '$timeout', 'AplanaUtils', function ($$stackedMap, $timeout) {
            var stackedAdjustWidthGrids = $$stackedMap.createNew();
            var $gridStack = {};

            $gridStack.registerAdjustWidthGrid = function (_grid, _gridConfig, _enableAdjustWidthByColumns, _parentAdjustWidthCssSelector) {
                // ширина по контейнеру
                _gridConfig.autowidth = true;
                // запрет авто подстройки ширины колонок
                _gridConfig.shrinkToFit = false;

                _gridConfig.resizeStop = function () {
                    adjustWidth(_grid, _enableAdjustWidthByColumns, _parentAdjustWidthCssSelector);
                };

                // должна выполниться вне текущего стекового фрейма, но перед тем,
                // как браузер отрисует представление. В случае отсутствия timeout в модальных окнах
                // ширина контейнера не будет вычислена правильно в следствии того что DOM модель не построена
                var timer = $timeout(function () {
                    adjustWidth(_grid, _enableAdjustWidthByColumns, _parentAdjustWidthCssSelector);
                }, 0);
                stackedAdjustWidthGrids.add(_grid, {
                    timer: timer,
                    enableAdjustWidthByColumns: _enableAdjustWidthByColumns,
                    parentAdjustWidthCssSelector: _parentAdjustWidthCssSelector
                });
            };

            $gridStack.unRegisterAdjustWidthGrid = function (_grid) {
                var adjustRegistration = stackedAdjustWidthGrids.remove(_grid);
                if (adjustRegistration && adjustRegistration.timer) {
                    $timeout.cancel(adjustRegistration.timer);
                }
            };

            /**
             * Подстроить ширину всех зарегистрированных таблиц сразу
             */
            $gridStack.adjustAllGridsWidthInstantly = function () {
                var grids = stackedAdjustWidthGrids.keys();
                for (var i = 0; i < grids.length; i++) {
                    var grid = grids[i]; // grid как ключ
                    $gridStack.adjustGridWidth(grid);
                }
            };

            /**
             * Подстроить ширину всех зарегистрированных таблиц вне текущего стекового фрейма
             */
            $gridStack.adjustAllGridsWidth = function () {
                $timeout($gridStack.adjustAllGridsWidthInstantly, 0);
            };

            /**
             * Приватная функция
             */
            function adjustWidth(_grid, _enableAdjustWidthByColumns, _parentAdjustWidthCssSelector) {
                var offset = -2; // grid left + right borders
                var gridParent;
                if (_parentAdjustWidthCssSelector === undefined) {
                    gridParent = _grid.closest('.ui-jqgrid', $(document)).parent();
                } else {
                    /* Находим родителя из указанного css селектора.
                     * В некоторых случаях нужно задавать родителя до приделов которого будет расширяться grid.
                     * Например для случая когда grid расположен внутри таблицы.
                     */
                    gridParent = _grid.closest(_parentAdjustWidthCssSelector, $(document));
                }

                var parentWidth = $(gridParent).innerWidth();
                if (parentWidth != null && parentWidth > 0) {
                    var width = parentWidth + offset;
                    if (_enableAdjustWidthByColumns) {
                        var hbox = _grid.closest('.ui-jqgrid').find('div.ui-jqgrid-hbox');
                        // реальная ширина заголовка
                        var colsWidth = hbox.innerWidth();

                        if (colsWidth > width) {
                            _grid.setGridWidth(width);
                        } else {
                            _grid.setGridWidth(colsWidth);
                        }
                    } else {
                        _grid.setGridWidth(width);
                    }

                } else {
                    // skip
                }
            }

            $gridStack.adjustGridWidth = function (_grid) {
                var gridEntry = stackedAdjustWidthGrids.get(_grid);
                if (gridEntry) {
                    var options = gridEntry.value;
                    adjustWidth(_grid, options.enableAdjustWidthByColumns, options.parentAdjustWidthCssSelector);
                }
            };

            $(window).resize(function () {
                $gridStack.adjustAllGridsWidthInstantly();
            });

            return $gridStack;
        }])
        /**
         * Фильтр добавляет чекбокс к заголовку группы
         */
        .filter('checkboxGrouping', function () {
            return function (groupName) {
                return '<input type="checkbox" class="groupHeader" />&nbsp;' + groupName;
            };
        })
        /**
         * Фильтр формирует поле с коллапсом для группировки
         */
        .filter('groupFormatter', function () {
            return function () {
                return '<span class="grid-group-row-collapse noselect" style="font-size: large;"></span>';
            };
        })
        /**
         * Фильтр формирует строку для сортировки иерархической структуры
         * Сортировка происходит по id: вначале идет родитель, потом - все его дети.
         *
         * columnIdName - название уникального идентификатора
         * columnParentIdName - название уникального идентификатора родителя
         * sortColumnName - поле, по которому сортируются сущности
         * objectRoot - корневая сущность (необязательный параметр)
         * _level - количество уровней в иерарахии (необязательный параметр =1 по умолчанию)
         */
        .filter('multiGroupSortType', ['AplanaUtils', function (AplanaUtils) {
            var pad = "0000000000";

            var lpad = function (id) {
                var str = id.toString();
                return pad.substring(0, pad.length - str.length) + str;
            };

            var hash = function (id, sort) {
                return lpad(sort) + '!' + lpad(id);
            };

            return function (columnIdName, parentEntityName, sortColumnName, objectRoot, _level) {
                var treeObjectHash = function (_obj) {
                    var object = objectRoot ? AplanaUtils.getObjectPropertyValueByPath(_obj, objectRoot) : _obj;
                    var level = _level ? _level : 1;

                    var result = [
                        hash(
                            AplanaUtils.getObjectPropertyValueByPath(object, columnIdName),
                            AplanaUtils.getObjectPropertyValueByPath(object, sortColumnName)
                        )
                    ];

                    var entity = object;
                    for (var i = 0; i < level; ++i) {
                        var parentEntity = AplanaUtils.getObjectPropertyValueByPath(entity, parentEntityName);

                        if (parentEntity) {
                            result.unshift(hash(
                                AplanaUtils.getObjectPropertyValueByPath(parentEntity, columnIdName),
                                AplanaUtils.getObjectPropertyValueByPath(parentEntity, sortColumnName)
                            ));
                        } else {
                            result.push(hash('0', '0'));
                        }

                        entity = parentEntity;
                    }

                    return result.join('_');
                };

                return function (cell, obj) {
                    return treeObjectHash(obj);
                };
            };
        }])

        /**
         * Фильтр формирует строку для сортировки иерархической структуры
         * Сортировка происходит по id: вначале идет родитель, потом - все его дети.
         *
         * columnIdName - название уникального идентификатора
         * columnParentIdName - название уникального идентификатора родителя
         * columnSortName - название поле, по которому сортируется сущность
         * columnParentSortName - название поля, по которому сортируется группировка
         */
        .filter('groupSortCustomType', ['AplanaUtils', function (AplanaUtils) {
            var pad = "0000000000";

            var lpad = function (id) {
                var str = id.toString();
                return pad.substring(0, pad.length - str.length) + str;
            };

            var hash = function (id, sort) {
                return lpad(sort) + '!' + lpad(id);
            };

            return function (columnIdName, columnParentIdName, columnSortName, columnParentSortName) {
                return function (cell, obj) {
                    var id = AplanaUtils.getObjectPropertyValueByPath(obj, columnIdName);
                    var sort = AplanaUtils.getObjectPropertyValueByPath(obj, columnSortName);

                    var parentId = AplanaUtils.getObjectPropertyValueByPath(obj, columnParentIdName);
                    var parentSortId = AplanaUtils.getObjectPropertyValueByPath(obj, columnParentSortName);

                    return hash(parentId, parentSortId) + '_' + hash(id, sort);
                };
            };
        }]);
}());