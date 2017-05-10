(function () {
    "use strict";
    /**
     * Сервис для хранения всякого в web storage
     * Основное применение: хранение настроек таблицы, фильтров
     */
    angular.module('aplana.webStorage', [])
        .factory('$webStorage', [function () {
            //Разделитель частей уникального идентификатора
            var uuidDelimiter = '/';

            //Функция проверки доступности web storage
            var isStoreAvailable = function () {
                return angular.isDefined(Storage);
            };

            var predefinedStores = {
                gridPageSettings: 'gridPageSettings',
                gridColSettings: 'gridColSettings',
                filterSettings: 'filterSettings'
            };

            var getStorage = function (useSession) {
                return useSession ? sessionStorage : localStorage;
            };

            var service = {
                /**
                 * Список преднастроенных хранилищ
                 */
                predefinedStores: predefinedStores,

                /**
                 * Метод очистки локального хранилища
                 *
                 * @returns {boolean} результат выполнения операции
                 */
                clearStore: function () {
                    if (isStoreAvailable()) {
                        localStorage.clear();
                        sessionStorage.clear();
                        return true;
                    } else {
                        return false;
                    }
                },

                /**
                 * Метод сохранения данных в хранилише
                 *
                 * @param store         имя виртуального хранилища (класс сохраняемых сщностей: настройки фильтра, настройки грида и тд)
                 * @param key           ключ записи
                 * @param value         сохраняемые данные
                 * @param useSession    флаг исполльзования временного хранилища в сессии (например для пагинатора или фильтра)
                 * @returns {boolean} результат сохранения, false - при недоступности хранилища
                 */
                set: function (store, key, value, useSession) {
                    if (isStoreAvailable()) {
                        getStorage(useSession).setItem(store + uuidDelimiter + key, JSON.stringify(value));
                        return true;
                    } else {
                        return false;
                    }
                },
                /**
                 * Метод получения данных из хранилища
                 *
                 * @param store имя виртуального хранилища (класс сохраняемых сщностей: настройки фильтра, настройки грида и тд)
                 * @param key   ключ записи
                 * @param useSession    флаг исполльзования временного хранилища в сессии (например для пагинатора или фильтра)
                 * @returns {} данные
                 */
                get: function (store, key, useSession) {
                    if (isStoreAvailable()) {
                        return angular.isDefined(getStorage(useSession)[store + uuidDelimiter + key]) ?
                            JSON.parse(getStorage(useSession).getItem(store + uuidDelimiter + key)) : undefined;
                    } else {
                        return undefined;
                    }
                },
                /**
                 * Удаляет запись с указанным ключем из хранилища
                 *
                 * @param store имя виртуального хранилища (класс сохраняемых сщностей: настройки фильтра, настройки грида и тд)
                 * @param key   ключ записи
                 * @param useSession    флаг исполльзования временного хранилища в сессии (например для пагинатора или фильтра)
                 * @returns {boolean} результат выполнения операции
                 */
                remove: function (store, key, useSession) {
                    if (isStoreAvailable()) {
                        getStorage(useSession).removeItem(store + uuidDelimiter + key);
                        return true;
                    } else {
                        return false;
                    }
                }
            };

            return service;
        }]);
}());