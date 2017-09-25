(function () {
    "use strict";
    /**
     * Модуль с утилитными методами для работы с правами доступа
     */
    angular.module('app.permissionUtils', [])
        .factory('PermissionChecker', [function () {

            var service = {
                maxInt32: Math.pow(2, 32),

                /**
                 * Сравнивает битовые маски длиннее, чем 32 бита
                 *
                 * @param permission    маска имеющихся прав доступа
                 * @param mask          маска требуемых прав доступа
                 * @returns {boolean} факт наличия прав доступа
                 */
                $compareMasks: function (permission, mask) {
                    permission = {
                        hi: Math.floor(permission / service.maxInt32),
                        lo: permission % service.maxInt32
                    };

                    mask = {
                        hi: Math.floor(mask / service.maxInt32),
                        lo: mask % service.maxInt32
                    };

                    return (mask.hi === (mask.hi & permission.hi)) && (mask.lo === (mask.lo & permission.lo));
                },

                /**
                 * Метод проверки прав доступа к объекту
                 * @param domainObject  объект, к которому проверяем права доступа
                 */
                check: function (domainObject) {
                    if (!domainObject) {
                        return false;
                    }
                    var result = true;

                    for (var i = 1; i < arguments.length; i++) {
                        result = result && service.$compareMasks(domainObject.permissions, arguments[i]);
                    }
                    return result;
                }
            };

            return service;
        }]);
}());
