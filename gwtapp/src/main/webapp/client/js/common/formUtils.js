(function () {
    "use strict";
    // Модуль с набором утилит формы
    angular.module('app.formUtils', [])
        .service('FormUtils', function () {
            var service = {
                /**
                 * проверка на изменения в указанной форме по всем полям формы и с использованием дополнительного алгоритма проверки
                 */
                isModified: function (form, customCheck) {
                    var result = false;
                    if (form) {
                        var formControllerPrototype = form.constructor.prototype;

                        angular.forEach(_.keys(form), function (key) {
                            var value = form[key];

                            if (angular.isDefined(value) && (value !== null) && key !== '$$parentForm') {
                                //Если это форма - проверим ее поля по отдельности, а не ее флаг $pristine
                                if (angular.isObject(value) && formControllerPrototype.isPrototypeOf(value)) {
                                    result |= service.isModified(value, customCheck);
                                } else if (value.$pristine !== undefined) {
                                    result |= !value.$pristine;
                                }
                            }
                        });

                        if (!result && customCheck) {
                            result |= angular.isFunction(customCheck) ? customCheck() : customCheck;
                        }
                    }
                    return result;
                },
                /**
                 * Проверяет есть ли на форме ошибки и есть ли среди них ошибки не связанные c @param keyNames
                 */
                hasOnlyKeyErrors: function (form, keyNames) {
                    var keys = [];
                    if (angular.isString(keyNames)) {
                        keys = [keyNames];
                    } else if (angular.isArray(keyNames)) {
                        keys = keyNames;
                    }

                    var hasErrorKey = function (errorKey) {
                        return _.some(keys || [], function (item) {
                            return errorKey === item;
                        });
                    };

                    var notOnlyThisKeyErrors = false;
                    var hasErrors = false;

                    angular.forEach(_.keys(form), function (key) {
                        var editor = form[key];

                        hasErrors |= editor.$invalid;

                        if (!!editor.$error) {
                            angular.forEach(_.keys(editor.$error), function (errorKey) {
                                if ((!hasErrorKey(errorKey)) && editor.$error[errorKey]) {
                                    notOnlyThisKeyErrors = true;
                                }
                            });
                        }
                    });

                    return {
                        hasErrors: hasErrors,
                        onlyThisKeyErrors: !notOnlyThisKeyErrors
                    };
                },
                /**
                 * Проверяет есть ли на форме ошибки и если среди них ошибке не связанные с актуальностью полей
                 */
                hasOnlyActualizationErrors: function (form) {
                    return this.hasOnlyKeyErrors(form, ['actual']);
                },
                /**
                 * Проверяет есть ли на форме ошибки и если среди них ошибке не связанные с ФИАС
                 */
                hasOnlyFiasErrors: function (form) {
                    return this.hasOnlyKeyErrors(form, ['fias']);
                },

                /**
                 * Возвращает строку с разметкой кнопок "добавить/удалить"
                 */
                getButtonHtml: function (isSelect, id) {
                    var addClass = isSelect ? "" : "disabled";
                    var removeClass = isSelect ? "disabled" : "";

                    return  '<span class="addButton ' + addClass + '">' +
                        '<i data-row-id = "' + id + '" class="grid-btn grid-btn-add"></i>' +
                        '</span>' +
                        '<span class="removeButton ' + removeClass + '">' +
                        '<i data-row-id = "' + id + '" class="grid-btn grid-btn-remove"></i>' +
                        '</span>';
                },

                /**
                 * Переключает классы disabled у кнопок "добавить/удалить" в гриде
                 *
                 * @param gridId id грида в html
                 * @param rowArray массив строк которые надо установить в состояние additable
                 * @param additable true - кнопка add активна, remove задизейблена
                 */
                toggleButton: function (gridId, rowArray, additable) {
                    angular.forEach(rowArray, function(row) {
                        var rowId = row.id;

                        var tr = $('#' + gridId).find('tr[id="' + rowId + '"]');

                        var addButton = tr.find('.addButton');
                        var removeButton = tr.find('.removeButton');

                        if (additable) {
                            addButton.removeClass('disabled');
                            removeButton.addClass('disabled');
                        } else {
                            addButton.addClass('disabled');
                            removeButton.removeClass('disabled');
                        }
                    });
                },

                /**
                 * Добавление обработчиков событий для кнопок +/- в гриде
                 *
                 * @param gridId id грида в html
                 * @param grid объект грида, используется grid.ctrl
                 * @param addButtonAction действие при нажатии на плюсик
                 * @param removeButtonAction действие при нажатии на минус
                 */
                initAddRemoveButtonGrid: function(gridId, grid, addButtonAction, removeButtonAction) {
                    // Обработчик событий для кнопки добавить
                    $(document).undelegate('#' + gridId + ' .addButton:not(.disabled)', 'click');
                    $(document).delegate('#' + gridId + ' .addButton:not(.disabled)', 'click', function (event) {
                        var rowId = $(event.target).attr('data-row-id');
                        var currentRow = grid.ctrl.getRawData(rowId);

                        addButtonAction(rowId, currentRow);
                    });

                    // Обработчик событий для кнопки удалить
                    $(document).undelegate('#' + gridId + ' .removeButton:not(.disabled)', 'click');
                    $(document).delegate('#' + gridId + ' .removeButton:not(.disabled)', 'click', function (event) {
                        var rowId = $(event.target).attr('data-row-id');
                        var currentRow = grid.ctrl.getRawData(rowId);

                        removeButtonAction(rowId, currentRow);
                    });
                },
                /**
                 * Создает нормальных объект из xml
                 * источник: https://davidwalsh.name/convert-xml-json
                 *
                 * @param xml
                 */
                xmlToJson: function(xml) {
                    // Create the return object
                    var obj = {};

                    if (xml.nodeType == 1) { // element
                        // do attributes
                        if (xml.attributes.length > 0) {
                            obj["@attributes"] = {};
                            for (var j = 0; j < xml.attributes.length; j++) {
                                var attribute = xml.attributes.item(j);
                                obj["@attributes"][attribute.nodeName] = attribute.nodeValue;
                            }
                        }
                    } else if (xml.nodeType == 3) { // text
                        obj = xml.nodeValue;
                    }

                    // do children
                    if (xml.hasChildNodes()) {
                        for(var i = 0; i < xml.childNodes.length; i++) {
                            var item = xml.childNodes.item(i);
                            var nodeName = item.nodeName;
                            if (nodeName == "#text") {
                                return item.nodeValue;
                            } else if (typeof(obj[nodeName]) == "undefined") {
                                obj[nodeName] = this.xmlToJson(item);
                            } else {
                                if (typeof(obj[nodeName].push) == "undefined") {
                                    var old = obj[nodeName];
                                    obj[nodeName] = [];
                                    obj[nodeName].push(old);
                                }
                                obj[nodeName].push(this.xmlToJson(item));
                            }
                        }
                    }
                    return obj;
                }
            };

            return service;
        });
}());
