/**
 * $dialogs (Диалоговые окна)
 * Сервис предназначен для отображения стандартных диалоговых окон
 * http://localhost:8080/#/aplana_dialogs
 */
(function () {
    "use strict";

    angular.module('aplana.modal.dialogs', [
        'aplana.modal',
        'aplana.utils',
        'ngSanitize',
        'aplana.field',
        'aplana.submitValid',
        'aplana.focus'
    ])
        .constant('confirmDialogDefaultOptions', {
            title: 'Подтверждение',
            titleIcon: 'icon-check',
            okBtnCaption: 'Согласен',
            cancelBtnCaption: 'Отмена',
            content: 'Выполнить действие?',
            controller: 'ConfirmDlgDefaultCtrl'
        })
        .constant('commentDialogDefaultOptions', {
            title: 'Отказать',
            titleIcon: 'icon-cbr-windows-cancel_work',
            okBtnCaption: 'Отказать',
            cancelBtnCaption: 'Отмена',
            content: '',
            comment: '',
            controller: 'CommentDlgDefaultCtrl',
            requiredComment: true
        })
        .constant('messageDialogDefaultOptions', {
            title: 'Информация',
            titleIcon: 'icon-cbr-windows-to_user',
            closeBtnCaption: 'Закрыть',
            content: '',
            controller: 'MessageDlgDefaultCtrl'
        })
        .constant('confirmQuestionDialogDefaultOptions', {
            title: 'Подтверждение',
            titleIcon: 'icon-check',
            okBtnCaption: 'Да',
            noBtnCaption: 'Нет',
            cancelBtnCaption: 'Отмена',
            content: 'Выполнить действие?',
            controller: 'ConfirmQuestionDlgDefaultCtrl'
        })
        .constant('errorDialogDefaultOptions', {
            title: 'Ошибка',
            titleIcon: 'icon-cbr-windows-to_user',
            modalHeaderClass: 'modal-header-error',
            closeBtnCaption: 'Закрыть',
            content: '',
            controller: 'MessageDlgDefaultCtrl'
        })
        .constant('warningDialogDefaultOptions', {
            title: 'Предупреждение',
            titleIcon: 'icon-cbr-windows-to_user',
            modalHeaderClass: 'modal-header-warning',
            closeBtnCaption: 'Закрыть',
            content: '',
            controller: 'MessageDlgDefaultCtrl'
        })
        .factory('$dialogs', [
            'AplanaTemplatePath',
            '$aplanaModal',
            'confirmDialogDefaultOptions',
            'warningDialogDefaultOptions',
            'commentDialogDefaultOptions',
            'messageDialogDefaultOptions',
            'confirmQuestionDialogDefaultOptions',
            'errorDialogDefaultOptions',
            function (AplanaTemplatePath, $aplanaModal, confirmDialogDefaultOptions, warningDialogDefaultOptions,
                      commentDialogDefaultOptions, messageDialogDefaultOptions, confirmQuestionDialogDefaultOptions, errorDialogDefaultOptions) {
                return {
                    confirmDialog: function (dialogOptions) {
                        var defaultOptions = angular.copy(confirmDialogDefaultOptions);
                        defaultOptions.templateUrl = AplanaTemplatePath + 'modal/confirm.html';
                        dialogOptions = angular.extend(defaultOptions, dialogOptions);
                        dialogOptions.resolve = {
                            options: function () {
                                return dialogOptions;
                            }
                        };

                        var modalInstance = $aplanaModal.open(dialogOptions);

                        modalInstance.result.then(function () {
                            if (angular.isDefined(dialogOptions.okBtnClick)) {
                                dialogOptions.okBtnClick();
                            }
                        }, function () {
                            if (angular.isDefined(dialogOptions.cancelBtnClick)) {
                                dialogOptions.cancelBtnClick();
                            }
                        });

                        return modalInstance;
                    },
                    commentDialog: function (dialogOptions) {
                        var defaultOptions = angular.copy(commentDialogDefaultOptions);
                        defaultOptions.templateUrl = AplanaTemplatePath + 'modal/comment.html';
                        dialogOptions = angular.extend(defaultOptions, dialogOptions);
                        dialogOptions.resolve = {
                            options: function () {
                                return dialogOptions;
                            }
                        };

                        var modalInstance = $aplanaModal.open(dialogOptions);

                        modalInstance.result.then(function () {
                            if (angular.isDefined(dialogOptions.okBtnClick)) {
                                dialogOptions.okBtnClick();
                            }
                        }, function () {
                            if (angular.isDefined(dialogOptions.cancelBtnClick)) {
                                dialogOptions.cancelBtnClick();
                            }
                        });

                        return modalInstance;
                    },
                    messageDialog: function (dialogOptions) {
                        var defaultOptions = angular.copy(messageDialogDefaultOptions);
                        defaultOptions.templateUrl = AplanaTemplatePath + 'modal/message.html';
                        dialogOptions = angular.extend(defaultOptions, dialogOptions);
                        dialogOptions.resolve = {
                            options: function () {
                                return dialogOptions;
                            }
                        };

                        var modalInstance = $aplanaModal.open(dialogOptions);

                        modalInstance.result.then(function () {
                        }, function () {
                            if (angular.isDefined(dialogOptions.closeBtnClick)) {
                                dialogOptions.closeBtnClick();
                            }
                        });

                        return modalInstance;
                    },
                    confirmQuestionDialog: function (dialogOptions) {
                        var defaultOptions = angular.copy(confirmQuestionDialogDefaultOptions);
                        defaultOptions.templateUrl = AplanaTemplatePath + 'modal/confirmQuestion.html';
                        dialogOptions = angular.extend(defaultOptions, dialogOptions);
                        dialogOptions.resolve = {
                            options: function () {
                                return dialogOptions;
                            }
                        };

                        var modalInstance = $aplanaModal.open(dialogOptions);

                        modalInstance.result.then(function (reply) {
                            if (reply === true) {
                                if (angular.isDefined(dialogOptions.okBtnClick)) {
                                    dialogOptions.okBtnClick();
                                }
                            } else {
                                if (angular.isDefined(dialogOptions.noBtnClick)) {
                                    dialogOptions.noBtnClick();
                                }
                            }

                        }, function () {
                            if (angular.isDefined(dialogOptions.cancelBtnClick)) {
                                dialogOptions.cancelBtnClick();
                            }
                        });

                        return modalInstance;
                    },
                    errorDialog: function (dialogOptions) {
                        var defaultOptions = angular.copy(errorDialogDefaultOptions);
                        defaultOptions.templateUrl = AplanaTemplatePath + 'modal/error.html';
                        dialogOptions = angular.extend(defaultOptions, dialogOptions);
                        dialogOptions.resolve = {
                            options: function () {
                                return dialogOptions;
                            }
                        };

                        var modalInstance = $aplanaModal.open(dialogOptions);

                        modalInstance.result.then(function () {
                        }, function () {
                            if (angular.isDefined(dialogOptions.closeBtnClick)) {
                                dialogOptions.closeBtnClick();
                            }
                        });

                        return modalInstance;
                    },
                    warningDialog: function (dialogOptions) {
                        var defaultOptions = angular.copy(warningDialogDefaultOptions);
                        defaultOptions.templateUrl = AplanaTemplatePath + 'modal/warning.html';
                        dialogOptions = angular.extend(defaultOptions, dialogOptions);
                        dialogOptions.resolve = {
                            options: function () {
                                return dialogOptions;
                            }
                        };

                        var modalInstance = $aplanaModal.open(dialogOptions);

                        modalInstance.result.then(function () {
                        }, function () {
                            if (angular.isDefined(dialogOptions.closeBtnClick)) {
                                dialogOptions.closeBtnClick();
                            }
                        });

                        return modalInstance;
                    },
                    /**
                     * Сообщение об ошибке
                     *
                     * @param dialogOptions объект, параметры диалогового окна
                     */
                    errorWithStack: function (dialogOptions) {
                        var defaultOptions = angular.copy(errorDialogDefaultOptions);
                        defaultOptions.controller = 'StackTraceCtrl';
                        defaultOptions.templateUrl = AplanaTemplatePath + 'modal/stack.html';
                        dialogOptions = angular.extend(defaultOptions, dialogOptions);
                        dialogOptions.resolve = {
                            options: function () {
                                return dialogOptions;
                            }
                        };

                        var modalInstance = $aplanaModal.open(dialogOptions);
                        modalInstance.result.then(function () {
                        }, function () {
                            if (angular.isDefined(dialogOptions.closeBtnClick)) {
                                dialogOptions.closeBtnClick();
                            }
                        });

                        return modalInstance;
                    }
                };
            }
        ]
    ).controller('ConfirmDlgDefaultCtrl',
        ['$scope', '$modalInstance', 'options',
            function ($scope, $modalInstance, options) {
                $scope.options = options;

                $scope.okBtnClick = function () {
                    $modalInstance.close();
                };

                $scope.cancelBtnClick = function () {
                    $modalInstance.dismiss();
                };
            }
        ]
    ).controller('CommentDlgDefaultCtrl',
        ['$scope', '$modalInstance', 'options',
            function ($scope, $modalInstance, options) {

                $scope.options = options;

                $scope.submitForm = function () {
                    $modalInstance.close(options.comment);
                };

                $scope.cancelBtnClick = function () {
                    $modalInstance.dismiss();
                };
            }
        ]
    ).controller('MessageDlgDefaultCtrl',
        ['$scope', '$modalInstance', 'options',
            function ($scope, $modalInstance, options) {
                $scope.options = options;

                $scope.closeBtnClick = function () {
                    $modalInstance.dismiss();
                };
            }
        ]
    ).controller('ConfirmQuestionDlgDefaultCtrl',
        ['$scope', '$modalInstance', 'options',
            function ($scope, $modalInstance, options) {
                $scope.options = options;

                $scope.okBtnClick = function () {
                    $modalInstance.close(true);
                };

                $scope.noBtnClick = function () {
                    $modalInstance.close(false);
                };

                $scope.cancelBtnClick = function () {
                    $modalInstance.dismiss();
                };
            }
        ]
    ).controller('StackTraceCtrl',
        ['$scope', '$modalInstance', 'options',
            function ($scope, $modalInstance, options) {
                $scope.header = options.message;
                $scope.stackTrace = options.addMessage;
                $scope.needDetails = false;

                if (!Array.isArray($scope.stackTrace.exceptionCause)) {
                    $scope.stackTrace.exceptionCause = [$scope.stackTrace.exceptionCause];
                }
                $scope.showDetails = function () {
                    $scope.needDetails = true;
                };
                $scope.cancel = function () {
                    $modalInstance.dismiss();
                };
            }
        ]
    );
}());