// Документацию по параметрам диалогов смотри на странице
// https://github.com/m-e-conroy/angular-dialog-service, либо
// в исходных кодах модуля "dialogs.main"
(function () {
    'use strict';

    angular
        .module('aplana.dialogs', [
            'ui.bootstrap',
            'ui.bootstrap.modal',
            'dialogs.main',
            'aplana.entity-utils'
        ])

        .provider('aplanaDialogs', [function () {
            // Настройки по умолчанию. См. http://angular-ui.github.io/bootstrap/#/modal
            var _wSize = 'md';
            var _b = 'static';

            var _setOpts = function (opts) {
                var _opts = {};
                opts = opts || {};
                _opts.size = (angular.isDefined(opts.size) && ((opts.size === 'sm') || (opts.size === 'lg') || (opts.size === 'md'))) ? opts.size : _wSize;
                _opts.backdrop = (angular.isDefined(opts.backdrop)) ? opts.backdrop : _b;
                if (angular.isDefined(opts.windowClass)) {
                    _opts.windowClass = opts.windowClass;
                }
                return _opts;
            };

            this.$get = function ($uibModal, $log, dialogs, $translate) {

                return {
                    /**
                     * Сообщение об ошибке
                     *
                     * @param header строка, заголовок
                     * @param msg строка, текст сообщения
                     * @param opts объект, параметры диалогового окна
                     */
                    error: function (header, msg, opts) {
                        opts = _setOpts(opts);
                        return dialogs.error(header, msg, opts)
                    },
                    /**
                     * Диалог-ожидание.
                     *
                     * @param header строка, заголовок
                     * @param msg строка, текст сообщения
                     * @param progress число, процент завершения, может быть функцией
                     * @param opts объект, параметры диалогового окна
                     */
                    wait: function (header, msg, progress, opts) {
                        opts = _setOpts(opts);
                        return dialogs.wait(header, msg, progress, opts)
                    },
                    /**
                     * Диалог-уведомление. Сообщает пользователю информацию.
                     *
                     * @param header строка, заголовок
                     * @param msg строка, текст сообщения
                     * @param opts объект, параметры диалогового окна
                     */
                    notify: function (header, msg, opts) {
                        opts = _setOpts(opts);
                        return dialogs.notify(header, msg, opts)
                    },
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
                            labelYes : opts && opts.labelYes ? opts.labelYes : $translate.instant('DIALOGS_YES'),
                            labelNo: opts && opts.labelNo ? opts.labelNo : $translate.instant('DIALOGS_NO')
                        };
                        opts = _setOpts(opts);
                        return dialogs.create('js/common/dialogs/confirm.html', 'confirmCtrl', data, opts)
                    },

                    /**
                     * Создает произвольный диалог
                     *
                     * @param url строка, адреса html-макета
                     * @param ctrlr строка, название контроллера
                     * @param data объект, данные для передачи в контроллер
                     * @param opts объект, параметры диалогового окна
                     */
                    create: function (url, ctrlr, data, opts, ctrlAs) {
                        opts = _setOpts(opts);
                        return dialogs.create(url, ctrlr, data, opts, ctrlAs)
                    },
                    /**
                     * Стандартный диалог с произвольным содержимым тела из шаблона. Включает стандартную шапку и кнопки=
                     * Шаблон и заголовок диалога указываются в контроллере диалога:
                     *   $scope.dialog = {
                     *       header: 'Мой прекрасный заголовое',
                     *       bodyTemplate: 'path/to/template.html'
                     *   };
                     *
                     * @param ctrl контроллер для диалога
                     * @param dataOptions данные для диалога
                     * @param size размер окна
                     *
                     * @returns строка
                     */
                    show: function (ctrl, dataOptions, size) {
                        var params = {};
                        jQuery.extend(params, dataOptions);

                        var data = {
                            scope: angular.copy(params)
                        };
                        var opts = {
                            size: size ? size : "md",
                            copy: false
                        };
                        opts = _setOpts(opts);
                        return dialogs.create('js/common/dialogs/aplanaDialog.html', ctrl, data, opts)
                    },
                    /**
                     * Диалог для ввода строки
                     *
                     * @param header строка, заголовок
                     * @param msg строка, текст сообщения
                     * @param metaData объект, описание полей (смотри java-аннотацию @Meta)
                     * @param values объект, значения
                     *
                     * @returns объект
                     */
                    editObject: function (header, msg, metaData, values) {
                        var data = {
                            header: angular.copy(header),
                            msg: angular.copy(msg),
                            metaData: angular.copy(metaData),
                            values: angular.copy(values)
                        };
                        var opts = {
                            copy: false
                        };
                        opts = _setOpts(opts);
                        return dialogs.create('js/common/dialogs/edit-object.html', 'editObjectCtrl', data, opts)
                    }
                }
            }
        }])

        .controller('confirmCtrl', ['$scope', '$uibModalInstance', '$translate', 'data',
            function ($scope, $uibModalInstance, $translate, data) {
                $scope.header = (angular.isDefined(data.header)) ? data.header : $translate.instant('DIALOGS_CONFIRMATION');
                $scope.msg = (angular.isDefined(data.msg)) ? data.msg : $translate.instant('DIALOGS_CONFIRMATION_MSG');
                $scope.icon = (angular.isDefined(data.fa) && angular.equals(data.fa, true)) ? 'fa fa-check' : 'glyphicon glyphicon-check';
                $scope.labelYes = (angular.isDefined(data.labelYes)) ? data.labelYes : $translate.instant('DIALOGS_YES');
                $scope.labelNo = (angular.isDefined(data.labelNo)) ? data.labelNo : $translate.instant('DIALOGS_NO');

                $scope.no = function () {
                    $uibModalInstance.dismiss('no');
                };

                $scope.yes = function () {
                    $uibModalInstance.close('yes');
                };
            }])
        .controller('editObjectCtrl', function ($log, $scope, $uibModalInstance, data, $http, $timeout) {
            $scope.header = data.header ? data.header : 'Редактирование';
            $scope.msg = data.msg ? data.msg : undefined;
            $scope.metaData = data.metaData;
            $scope.values = data.values;

            // инициализация контролов
            $scope.fieldParams = {};
            $scope.metaData.forEach(function (field) {
                if (field.type == 'java.util.List') {
                    $scope.fieldParams[field.name] = {
                        field: field,
                        items: [],
                        searchItems: function (searchText) {
                            var url = 'rest/entity/' + field.referenceType.substring('com.ndlf.model.'.length);
                            var params = {
                                fulltext: searchText,
                                sort: field.displayField + '-asc',
                            };
                            // Получение данных
                            $http.get(url, {params: params})
                                .success(function (data) {
                                    $scope.fieldParams[field.name].items = data.list
                                })
                        }
                    }
                }
                if (field.type.startsWith('com.ndlf.model')) {
                    $scope.fieldParams[field.name] = {
                        field: field,
                        items: [],
                        searchItems: function (searchText) {
                            if (!field.predefinedValues) {
                                var url = 'rest/entity/' + field.type.substring('com.ndlf.model.'.length);
                                var params = {
                                    fulltext: searchText,
                                    sort: field.displayField + '-asc',
                                };
                                if (field.filter) {
                                    jQuery.extend(params, field.filter);
                                }
                                // Получение данных
                                $http.get(url, {params: params}).then(
                                    function (response) {
                                        $scope.fieldParams[field.name].items = response.data.list
                                    })
                            } else {
                                $scope.fieldParams[field.name].items = field.predefinedValues
                            }
                        },
                        selectClick: function () {
                            if ($scope.values[field.name] == undefined) {
                                $scope.values[field.name] = null; // Очистка значения для передачи на сервер
                            }
                        }
                    }
                }
                if (field.type == 'java.util.Date') {
                    $scope.fieldParams[field.name] = {
                        field: field,
                        is_open: false,
                        selectClick: function () {
                            $scope.fieldParams[field.name].is_open = true
                        }
                    }
                }

            });

            $timeout(function() {
                //Активируем кнопку Сохранить без изменений на форме
                $scope.editForm.$pristine = false;
            }, 100);

            $scope.save = function () {
                $uibModalInstance.close($scope.values)
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('Canceled')
            };
            $scope.hitEnter = function (evt) {
                if (angular.equals(evt.keyCode, 13) && !((editForm.$dirty && editForm.$invalid) || editForm.$pristine))
                    $scope.save();
            }
        })
        // Add default templates via $templateCache
        .run(['$templateCache','$interpolate',function($templateCache,$interpolate){
            // get interpolation symbol (possible that someone may have changed it in their application instead of using '{{}}')
            var startSym = $interpolate.startSymbol();
            var endSym = $interpolate.endSymbol();

            $templateCache.put('/dialogs/custom_confirm.html','<div class="modal-header dialog-header-confirm"><button type="button" class="close" ng-click="no()">&times;</button><h4 class="modal-title"><span class="'+startSym+'icon'+endSym+'"></span> '+startSym+'header'+endSym+'</h4></div><div class="modal-body" ng-bind-html="msg"></div><div class="modal-footer"><button type="button" class="btn btn-primary" ng-click="yes()">'+startSym+'labelYes'+endSym+'</button><button type="button" class="btn btn-default" ng-click="no()">'+startSym+'labelNo'+endSym+'</button></div>');
        }]);
}());