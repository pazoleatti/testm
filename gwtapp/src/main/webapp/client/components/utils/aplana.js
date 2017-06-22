(function () {
    'use strict';

    angular.module('aplana.utils', ['ngSanitize']).
        constant('AplanaTemplatePath', 'client/components/').
        constant("i18nPath", "aplana/i18n/").
        factory('AplanaUtils', ['AplanaTemplatePath', '$parse', '$sanitize', 'dateFilter', function (AplanaTemplatePath, $parse, $sanitize, dateFilter) {

            /**
             * @name #isElement
             *
             * Проверяет, что переданная нода - DOM элемент или JQuery element.
             * В случае с JQuery element - дополнительно проверяет наличие реального элемента через length
             *
             * @param {*} node Переменная для проверки.
             * @returns {boolean} True если `value` - DOM элемент (или обернутый в jQuery элемент).
             */
            function isElement(node) {
                return angular.isElement(node) && (node.length > 0);
            }

            function findNgModel(element) {
                var elementWithNgModel = element.find('[data-ng-model], [ng-model], [ngModel]');
                return elementWithNgModel.data('ngModel') || elementWithNgModel.attr('ngModel') || elementWithNgModel.attr('ng-model');
            }

            /**
             * Функция выполняет копирование атрибутов элемента sourceElement в элемент targetElement.
             * Затем удаляет скопированные атрибуты из элемента sourceElement
             *
             * @param sourceElement
             * @param targetElement
             * @param ignoredAttrs
             */
            function moveAttributes(sourceElement, targetElement, ignoredAttrs) {

                if (angular.isUndefined(sourceElement) || angular.isUndefined(targetElement)) {
                    return;
                }

                var ignoreAttributes = [];
                if (angular.isArray(ignoredAttrs)) {
                    ignoreAttributes = ignoredAttrs;
                } else if (angular.isString(ignoredAttrs)) {
                    ignoreAttributes.push(ignoredAttrs);
                }

                var attributesForMoving = [];
                angular.forEach(sourceElement[0].attributes, function (attribute) {
                    if ($.inArray(attribute.name, ignoreAttributes) === -1) {
                        attributesForMoving.push({key: attribute.name, value: attribute.value});
                    }
                });

                angular.forEach(attributesForMoving, function (item) {
                    targetElement.attr(item.key, item.value);
                    sourceElement[0].removeAttribute(item.key);
                });
            }

            /**
             * Функция выполняет только копирование атрибутов элемента sourceElement в элемент targetElement.
             * Если copyAttrs не задан, будут скопированы все атрибуты
             *
             * @param sourceElement
             * @param targetElement
             * @param copyAttrs
             */
            function copyAttributes(sourceElement, targetElement, copyAttrs) {

                if (angular.isUndefined(sourceElement) || angular.isUndefined(targetElement)) {
                    return;
                }

                var onlyAttributes = [];
                if (angular.isArray(copyAttrs)) {
                    onlyAttributes = copyAttrs;
                } else if (angular.isString(copyAttrs)) {
                    onlyAttributes.push(copyAttrs);
                }


                var attributesForCopy = [];
                angular.forEach(sourceElement[0].attributes, function (attribute) {
                    if (onlyAttributes.length === 0 || $.inArray(attribute.name, onlyAttributes) !== -1) {
                        attributesForCopy.push({key: attribute.name, value: attribute.value});
                    }
                });

                angular.forEach(attributesForCopy, function (item) {
                    targetElement.attr(item.key, item.value);
                });
            }

            /**
             * Функция делает заглавным первый символ переданной строки
             *
             * @param string
             * @returns {string}
             */
            function capitaliseFirstLetter(string) {
                //noinspection JSUnresolvedFunction
                return angular.isString(string) ? string.charAt(0).toUpperCase() + string.slice(1) : "";
            }

            /**
             * Правильная форма cуществительного рядом с числом (счетная форма).
             *
             * @param _number Число
             * @param _case1 Единственное число именительный падеж
             * @param _case2 Единственное число родительный падеж
             * @param _case3 Множественное число родительный падеж
             * @return string
             */
            function getCase(_number, _case1, _case2, _case3) {
                if (_case1 === undefined || _case1 == null || _case2 === undefined || _case2 == null || _case3 === undefined || _case3 == null || _number === '') {
                    return '';
                }
                var base = _number - Math.floor(_number / 100) * 100;
                if (isNaN(base)) {
                    return '';
                }

                var result;
                if (base > 9 && base < 20) {
                    result = _case3;

                } else {
                    var remainder = _number - Math.floor(_number / 10) * 10;

                    if (1 === remainder) {
                        result = _case1;
                    }
                    else if (0 < remainder && 5 > remainder) {
                        result = _case2;
                    }
                    else {
                        result = _case3;
                    }
                }

                return result;
            }

            /**
             * функция-фикс базовой ангуларовской функции $watchCollection {@see <a href="https://github.com/angular/angular.js/issues/2621">https://github.com/angular/angular.js/issues/2621</a>}
             *
             * @param scope scope, в котором будем слушать изменения
             * @param obj объект за которым следим
             * @param listener слушатель
             */
            function watchCollection(scope, obj, listener) {
                function isWindow(obj) {
                    return obj && obj.document && obj.location && obj.alert && obj.setInterval;
                }

                function isArrayLike(obj) {
                    if (obj == null || isWindow(obj)) {
                        return false;
                    }

                    var length = obj.length;

                    if (obj.nodeType === 1 && length) {
                        return true;
                    }

                    return angular.isString(obj) || angular.isArray(obj) || length === 0 ||
                        typeof length === 'number' && length > 0 && (length - 1) in obj;
                }

                function initWatchVal() {
                }

                var self = scope;
                var oldValue;
                var newValue;
                var changeFlipFlop = 0;
                var objGetter = $parse(obj);
                var internalArray = [];
                var internalObject = {};
                var internalLength = 0;

                // Holds simple value or reference to internalArray or internalObject.
                // The special initial value is used to ensure that the listener is called
                // when the watch is established and that oldValue = newValue.
                var internalValue = initWatchVal;

                function $watchCollectionWatch() {
                    var newLength, key, i, changeDetected;

                    newValue = objGetter(self);
                    oldValue = internalValue;
                    changeDetected = 0;

                    if (!angular.isObject(newValue)) {
                        if (internalValue !== newValue) {
                            internalValue = newValue;
                            changeDetected++;
                        }
                    } else if (isArrayLike(newValue)) {
                        newLength = newValue.length;
                        if (internalValue !== internalArray) {
                            // we are transitioning from something which was not an array into array.
                            changeDetected++;
                        } else {
                            if (internalLength !== newLength) {
                                // if lengths do not match we need to trigger change notification
                                changeDetected++;
                            } else {
                                // look for item changes
                                for (i = 0; i < newLength; i++) {
                                    if (internalValue[i] !== newValue[i]) {
                                        changeDetected++;
                                        break;
                                    }
                                }
                            }
                        }
                        if (changeDetected) {
                            // copy the items to array cache
                            internalValue = internalArray = [];
                            internalValue.length = internalLength = newLength;
                            for (i = 0; i < newLength; i++) {
                                internalValue[i] = newValue[i];
                            }
                        }
                    } else {
                        if (internalValue !== internalObject) {
                            // we are transitioning from something which was not an object into object
                            changeDetected++;
                        } else {
                            // look for item changes
                            newLength = 0;
                            for (key in newValue) {
                                if (newValue.hasOwnProperty(key)) {
                                    newLength++;
                                    if (!(internalValue.hasOwnProperty(key) &&
                                        internalValue[key] === newValue[key])) {
                                        changeDetected++;
                                        break;
                                    }
                                }
                            }
                            if (internalLength !== newLength) {
                                changeDetected++;
                            }
                        }
                        if (changeDetected) {
                            // copy the items to object cache
                            internalValue = internalObject = {};
                            internalLength = 0;
                            for (key in newValue) {
                                if (newValue.hasOwnProperty(key)) {
                                    internalLength++;
                                    internalValue[key] = newValue[key];
                                }
                            }
                        }
                    }

                    if (changeDetected) {
                        changeFlipFlop = 1 - changeFlipFlop;
                        if (oldValue === initWatchVal) {
                            oldValue = newValue;
                        }
                    }

                    return changeFlipFlop;
                }

                function $watchCollectionAction() {
                    listener(newValue, oldValue, self);
                }

                return scope.$watch($watchCollectionWatch, $watchCollectionAction);
            }

            /**
             * Функция извлекает из объекта поле по пути, переданному в виде строки. Используется для вложенных объектов,
             * когда путь содержит точки.
             *
             * @param object - корневой объект
             * @param path - строка, содержащая путь (с точками) до искомого поля объекта
             * @returns {*} - значение запрошенного поля объекта
             */
            function getObjectPropertyValueByPath(object, path) {
                if (!angular.isString(path)) {
                    return undefined;
                }

                var pathArray = path.split('.');

                var recursiveFunc = function (object, pathArray) {
                    if (angular.isObject(object)) {
                        if (pathArray.length === 1) {
                            return object[pathArray[0]];
                        } else {
                            object = object[pathArray[0]];
                            pathArray.splice(0, 1);
                            return recursiveFunc(object, pathArray);
                        }
                    } else {
                        return undefined;
                    }
                };

                return recursiveFunc(object, pathArray);

            }

            /**
             * Функция формирует строку-идентификатор из имени нг-модели
             *
             * @param ngModelAttr - имя нг-модели
             */
            function buildModelId(ngModelAttr) {
                return ngModelAttr.replace(/\./g, '_').toLowerCase();
            }

            /**
             * Функция парсит переданную дату, отбрасывает время и возвращает объект Date c выставленными в 0 полями времени
             *
             * @param date - может быть числом (миллисекунды с 01.01.1970, строкой (в ISO формате) или объектом js Date
             * @returns {Date}
             */
            function parseAndFloorDate(date) {
                return date ? floorDate(new Date(date)) : null;
            }

            /**
             * Фукнция округляет переданную дату, оставляя только год-месяц-день
             *
             * @param date - может быть числом (миллисекунды с 01.01.1970, строкой (в ISO формате) или объектом js Date
             * @returns {Date}
             */
            function floorDate(date) {
                return date ? new Date(date.getFullYear(), date.getMonth(), date.getDate()) : null;
            }

            /**
             * Функция возвращает текущую дату c выставленными в 0 полями времени
             *
             * @returns {Date}
             */
            function today() {
                return floorDate(new Date());
            }

            /**
             * Функция преобразует дату в строку в формате ISO8601 ("yyyy-MM-dd") - это необходимо для передачи даты в java контроллер как строки
             *
             * @param date - дата в формате js Date для форматирования
             */
            function formatDateForBinder(date) {
                return dateFilter(date, "yyyy-MM-dd");
            }

            // Sanitizes HTML in the values of the interpolation parameters using $sanitize
            function sanitizeParams(value) {
                if (angular.isObject(value)) {
                    var result = angular.isArray(value) ? [] : {};

                    angular.forEach(value, function (propertyValue, propertyKey) {
                        result[propertyKey] = sanitizeParams(propertyValue);
                    });

                    return result;
                } else if (angular.isNumber(value)) {
                    return value;
                } else if (typeof(value) === "boolean") {
                    return value;
                } else {
                    return he.decode($sanitize(value));
                }
            }

            // Рекурсивный обход и sanitize всех полей объекта
            var sanitizeRecursively = function (value) {
                if (value !== null) {

                    if (typeof value === 'array') {
                        value.forEach(function (element, index) {
                            //noinspection JSUnresolvedVariable
                            value[index] = AplanaUtils.sanitizeRecursively(element);
                        });
                        return value;
                    }
                    else if (typeof value === 'object') {
                        for (var prop in value) {
                            if (value.hasOwnProperty(prop)) {
                                value[prop] = sanitizeRecursively(value[prop]);
                            }
                        }
                        return value;
                    }
                    else {
                        // Нельзя просто так взять и вызвать sanitize() - сломается кодировка у текстов с кирилицей
                        // Используем специальный метод
                        return sanitizeParams(value);
                    }
                }
            };

            return {
                'templatePath': AplanaTemplatePath,
                'isElement': isElement,
                'findNgModel': findNgModel,
                'moveAttributes': moveAttributes,
                'copyAttributes': copyAttributes,
                'capitaliseFirstLetter': capitaliseFirstLetter,
                'getCase': getCase,
                'watchCollection': watchCollection,
                'getObjectPropertyValueByPath': getObjectPropertyValueByPath,
                'buildModelId': buildModelId,
                'today': today,
                'floorDate': floorDate,
                'parseAndFloorDate': parseAndFloorDate,
                'formatDateForBinder': formatDateForBinder,
                'sanitizeParams': sanitizeParams,
                'sanitizeRecursively': sanitizeRecursively
            };

        }])

        .factory('PagingBuilder', function () {
            return function (postdata) {
                //noinspection JSUnresolvedVariable
                var sort = _.isEmpty(postdata.sidx) || _.isEmpty(postdata.sord) ? undefined : postdata.sidx + ',' + postdata.sord;
                return {rows: postdata.rows, page: postdata.page, sort: sort};
            };
        })
    /**
     * A helper, data structure that acts as a map but also allows getting / removing
     * elements in the LIFO order
     */
        .factory('$$stackedMap', function () {
            return {
                createNew: function () {
                    var stack = [];

                    return {
                        add: function (key, value) {
                            stack.push({
                                key: key,
                                value: value
                            });
                        },
                        get: function (key) {
                            for (var i = 0; i < stack.length; i++) {
                                if (key === stack[i].key) {
                                    return stack[i];
                                }
                            }
                            return undefined;
                        },
                        keys: function () {
                            var keys = [];
                            for (var i = 0; i < stack.length; i++) {
                                keys.push(stack[i].key);
                            }
                            return keys;
                        },
                        top: function () {
                            return stack[stack.length - 1];
                        },
                        remove: function (key) {
                            var idx = -1;
                            for (var i = 0; i < stack.length; i++) {
                                if (key === stack[i].key) {
                                    idx = i;
                                    break;
                                }
                            }
                            return stack.splice(idx, 1)[0];
                        },
                        removeTop: function () {
                            return stack.splice(stack.length - 1, 1)[0];
                        },
                        length: function () {
                            return stack.length;
                        }
                    };
                }
            };
        });
}());