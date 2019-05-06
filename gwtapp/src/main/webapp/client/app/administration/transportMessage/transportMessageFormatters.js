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

        .filter('tmStateFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (stateId) {
                return APP_CONSTANTS.TRANSPORT_MESSAGE_STATE[stateId];
            }
        }])

        .filter('tmNdflReportLinkFormatter', function () {
            return function (declarationId) {
                if (!declarationId) return '';
                return '<a href="index.html#/taxes/ndfl/ndflReport/' + declarationId + '">' + declarationId + '</a>';
            }
        })

        .filter('tmGridBodyFileLinkFormatter', function () {
            return function (fileName, options) {
                if (!fileName || !options) return '';
                return '<a target="_self" href="controller/rest/transportMessages/' + options.rowId + '/bodyFile">' + fileName + '</a>';
            }
        })

        .filter('tmGridFileLinkFormatter', function () {
            return function (file, options) {
                if (!file || !file.name || !options) return '';
                return '<a target="_self" href="controller/rest/transportMessages/' + options.rowId + '/file">' + file.name + '</a>'
            }
        })

        .filter('tmWindowFileLinkFormatter', function () {
            return function (message) {
                if (!message || !message.blob || !message.blob.name) return '';
                return '<a target="_self" href="controller/rest/transportMessages/' + message.id + '/file">' + message.blob.name + '</a>'
            }
        })
}());
