// Документацию по параметрам диалогов смотри на странице
// https://github.com/m-e-conroy/angular-dialog-service, либо
// в исходных кодах модуля "dialogs.main"
(function () {
    'use strict';

    angular
        .module('app.dialogs', [
            'ui.bootstrap',
            'ui.bootstrap.modal',
            'dialogs.main'
        ])

        .provider('appDialogs', [function () {
            // Настройки по умолчанию. См. http://angular-ui.github.io/bootstrap/#/modal
            var _wSize = 'md';
            var _b = 'static';

            var _setOpts = function (opts) {
                var _opts = {};
                opts = opts || {};
                _opts.size = (angular.isDefined(opts.size) && ((opts.size === 'sm') || (opts.size === 'lg') || (opts.size === 'md'))) ? opts.size : _wSize;
                _opts.backdrop = (angular.isDefined(opts.backdrop)) ? opts.backdrop : _b;
                return _opts;
            };

            this.$get = function ($uibModal, $log, dialogs, $translate) {

                return {
                    /**
                     * Универсальный диалог вопрос-ответ, можно задавать названия кнопок в opts
                     * opts.labelYes - название кнопки "Да"
                     * opts.labelNo - название кнопки "Нет"
                     *
                     * @param header строка, заголовок
                     * @param msg строка, текст сообщения
                     * @param opts объект, параметры диалогового окна
                     */
                    confirm: function (header, msg, opts) {
                        var data = {
                            header: header,
                            msg: msg,
                            labelYes: opts && opts.labelYes ? opts.labelYes : $translate.instant('DIALOGS_YES'),
                            labelNo: opts && opts.labelNo ? opts.labelNo : $translate.instant('DIALOGS_NO')
                        };

                        opts = _setOpts(opts);
                        return dialogs.create('client/lib/templates/confirmDialog.html', 'confirmCtrl', data, opts)
                    },
                    /**
                     * Создает произвольный диалог
                     *
                     * @param url строка, адреса html-макета
                     * @param ctrlr строка, название контроллера
                     * @param data объект, данные для передачи в контроллер
                     * @param opts объект, параметры диалогового окна
                     */
                    create: function (url, ctrlr, data, opts) {
                        if (opts) {
                            opts.size = opts && opts.size ? opts.size : "lg";
                        } else {
                            opts = {
                                size: "lg"
                            }
                        }
                        opts = _setOpts(opts);
                        return dialogs.create(url, ctrlr, data, opts)
                    },
                    /**
                     * Диалог-уведомление. Сообщает пользователю информацию.
                     *
                     * @param header строка, заголовок
                     * @param msg строка, текст сообщения
                     * @param opts объект, параметры диалогового окна
                     */
                    message: function (header, msg, opts) {
                        opts = _setOpts(opts);
                        return dialogs.message(header, msg, opts)
                    }
                }
            }
        }])
        .controller('confirmCtrl', ['$scope', '$uibModalInstance', '$translate', 'data',
            function ($scope, $uibModalInstance, $translate, data) {
                $scope.header = (angular.isDefined(data.header)) ? data.header : $translate.instant('DIALOGS_CONFIRMATION');
                $scope.msg = (angular.isDefined(data.msg)) ? data.msg : $translate.instant('DIALOGS_CONFIRMATION_MSG');
                $scope.icon = (angular.isDefined(data.fa) && angular.equals(data.fa, true)) ? 'fa fa-star-o' : 'glyphicon glyphicon-star-empty';
                $scope.labelYes = (angular.isDefined(data.labelYes)) ? data.labelYes : $translate.instant('DIALOGS_YES');
                $scope.labelNo = (angular.isDefined(data.labelNo)) ? data.labelNo : $translate.instant('DIALOGS_NO');

                $scope.no = function () {
                    $uibModalInstance.dismiss('no');
                };

                $scope.yes = function () {
                    $uibModalInstance.close('yes');
                };
            }])
        // Add default templates via $templateCache
        .run(['$templateCache', '$interpolate', function ($templateCache, $interpolate) {
            // get interpolation symbol (possible that someone may have changed it in their application instead of using '{{}}')
            var startSym = $interpolate.startSymbol();
            var endSym = $interpolate.endSymbol();

            $templateCache.put('/dialogs/custom_confirm.html', '<div class="modal-header dialog-header-confirm"><button type="button" class="close" ng-click="no()">&times;</button><h4 class="modal-title"><span class="' + startSym + 'icon' + endSym + '"></span> ' + startSym + 'header' + endSym + '</h4></div><div class="modal-body" ng-bind-html="msg"></div><div class="modal-footer"><button type="button" class="btn btn-primary" ng-click="yes()">' + startSym + 'labelYes' + endSym + '</button><button type="button" class="btn btn-default" ng-click="no()">' + startSym + 'labelNo' + endSym + '</button></div>');
        }]);
}());