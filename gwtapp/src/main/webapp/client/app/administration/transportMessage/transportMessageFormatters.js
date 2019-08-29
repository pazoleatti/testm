(function () {
    'use strict';

    /**
     * Форматтеры, использующиеся в модуле "Транспортные сообщения".
     */
    angular.module('app.transportMessage')

        .filter('tmTypeFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (typeId) {
                return APP_CONSTANTS.TRANSPORT_MESSAGE_TYPE[typeId];
            }
        }])

        .filter('tmContentTypeFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (contentTypeId) {
                return APP_CONSTANTS.TRANSPORT_MESSAGE_CONTENT_TYPE[contentTypeId];
            }
        }])

        // Вид формы (SBRFNDFL-8318)
//        .filter('tmDeclarationTypeFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
//            return function (declarationTypeId) {
//                return APP_CONSTANTS.TRANSPORT_MESSAGE_DECLARATION_TYPE[declarationTypeId];
//            }
//        }])

        .filter('tmStateFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (stateId) {
                return APP_CONSTANTS.TRANSPORT_MESSAGE_STATE[stateId];
            }
        }])

        .filter('tmNdflReportLinkFormatter', ['APP_CONSTANTS', 'PermissionChecker', '$rootScope',
            function (APP_CONSTANTS, PermissionChecker, $rootScope) {
                return function (declarationId) {
                    if (!declarationId) return '';
                    if (PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                        return '<a href="index.html#/taxes/ndfl/ndflReport/' + declarationId + '">' + declarationId + '</a>';
                    } else {
                        return declarationId;
                    }
                }
        }])

        .filter('tmGridBodyFileLinkFormatter', ['APP_CONSTANTS', 'PermissionChecker', '$rootScope',
            function (APP_CONSTANTS, PermissionChecker, $rootScope) {
                return function (fileName, options) {
                    if (!fileName || !options) return '';
                    if (PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                        return '<a target="_self" href="controller/rest/transportMessages/' + options.rowId + '/bodyFile">' + fileName + '</a>';
                    } else {
                        return fileName;
                    }
                }
        }])

        .filter('tmGridFileLinkFormatter', ['APP_CONSTANTS', 'PermissionChecker', '$rootScope',
            function (APP_CONSTANTS, PermissionChecker, $rootScope) {
                return function (file, options) {
                    if (!file || !file.name || !options) return '';
                    if (PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                        return '<a target="_self" href="controller/rest/transportMessages/' + options.rowId + '/file">' + file.name + '</a>'
                    } else {
                        return file.name;
                    }
                }
        }])

        .filter('tmWindowFileLinkFormatter', ['APP_CONSTANTS', 'PermissionChecker', '$rootScope',
            function (APP_CONSTANTS, PermissionChecker, $rootScope) {
                return function (message) {
                    if (!message || !message.blob || !message.blob.name) return '';
                    if (PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                        return '<a target="_self" href="controller/rest/transportMessages/' + message.id + '/file">' + message.blob.name + '</a>'
                    } else {
                        return message.blob.name
                    }
                }
        }])
}());
