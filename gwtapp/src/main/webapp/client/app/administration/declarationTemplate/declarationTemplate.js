(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Макет налоговой формы"
     */
    angular.module('app.declarationTemplate', ['app.templateChecksTab', 'app.templateInfoTab'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('declarationTemplate', {
                url: '/administration/declarationTemplate/{declarationTemplateId}',
                templateUrl: 'client/app/administration/declarationTemplate/declarationTemplate.html?v=${buildUuid}',
                controller: 'DeclarationTemplateCtrl'
            });
        }])

        .controller('DeclarationTemplateCtrl', ['$scope', '$filter', '$stateParams', 'DeclarationTemplateResource', '$logPanel', '$dialogs', 'APP_CONSTANTS', 'BlobDataResource',
            function ($scope, $filter, $stateParams, DeclarationTemplateResource, $logPanel, $dialogs, APP_CONSTANTS, BlobDataResource) {
                $scope.declarationTemplate = {formType: {}};
                $scope.templateChecksGrid = {};

                // Загружаем данные по макету
                function loadTemplate() {
                    DeclarationTemplateResource.query({
                        projection: 'fetchOne',
                        id: $stateParams.declarationTemplateId
                    }, function (declarationTemplate) {
                        $scope.declarationTemplate = declarationTemplate;
                        $scope.declarationTemplate.yearFrom = new Date(declarationTemplate.version).getUTCFullYear();
                        if (declarationTemplate.versionEnd) {
                            $scope.declarationTemplate.yearTo = new Date(declarationTemplate.versionEnd).getUTCFullYear();
                        }
                    });
                }

                loadTemplate();

                // Табы
                $scope.templateTabsCtrl = {};
                $scope.infoTab = {
                    title: $filter('translate')('declarationTemplate.tabs.info'),
                    contentUrl: 'client/app/administration/declarationTemplate/tabs/infoTab.html?v=${buildUuid}',
                    fetchTab: true,
                    active: true
                };
                $scope.checksTab = {
                    title: $filter('translate')('declarationTemplate.tabs.checks'),
                    contentUrl: 'client/app/administration/declarationTemplate/tabs/checksTab.html?v=${buildUuid}',
                    fetchTab: true
                };
                $scope.templateTabs = [$scope.infoTab, $scope.checksTab];

                // Сохранение
                $scope.save = function (formsExistWarningConfirmed) {
                    // Создаются даты актуальности версии из значений годов из элем. ввода
                    $scope.declarationTemplate.version = $scope.declarationTemplate.yearFrom ? new Date($scope.declarationTemplate.yearFrom, 0, 1).format(dateFormat.masks.isoDate) : null;
                    $scope.declarationTemplate.versionEnd = $scope.declarationTemplate.yearTo ? new Date($scope.declarationTemplate.yearTo, 0, 1).format(dateFormat.masks.isoDate) : null;

                    if (checkBeforeSave()) {
                        DeclarationTemplateResource.save({
                                projection: "updateTemplate"
                            }, {
                                declarationTemplate: $scope.declarationTemplate,
                                checks: getChecks(),
                                formsExistWarningConfirmed: formsExistWarningConfirmed
                            }, function (result) {
                                if (result.uuid) {
                                    $logPanel.open('log-panel-container', result.uuid);
                                }
                                if (result.confirmNeeded) {
                                    confirmAndTryAgain('declarationTemplate.warning.formsExist.save', $scope.save);
                                } else if (result.success) {
                                    loadTemplate();
                                }
                            }
                        );
                    }
                };

                // Отмена изменений
                $scope.cancel = function () {
                    loadTemplate();
                    // Если был загружен новый xsd, то при отмене удаляем его
                    $scope.deleteNewXsd();
                };

                // Удаляет загруженный xsd файл, но ещё не привязанный к макету
                $scope.deleteNewXsd = function () {
                    if ($scope.declarationTemplate.xsdId && $scope.declarationTemplate.isXsdNew) {
                        BlobDataResource.delete({uuid: $scope.declarationTemplate.xsdId});
                    }
                };

                // Ввод/Вывод из действия
                $scope.updateStatus = function (formsExistWarningConfirmed) {
                    DeclarationTemplateResource.save({
                            projection: "updateStatus"
                        }, {
                            templateId: $scope.declarationTemplate.id,
                            formsExistWarningConfirmed: formsExistWarningConfirmed
                        }, function (result) {
                            if (result.uuid) {
                                $logPanel.open('log-panel-container', result.uuid);
                            }
                            if (result.confirmNeeded) {
                                confirmAndTryAgain('declarationTemplate.warning.formsExist.updateStatus', $scope.updateStatus);
                            } else if (result.success) {
                                $scope.declarationTemplate.status = result.status;
                            }
                        }
                    );
                };

                // Формирует наименование кнопки Ввод/Вывод из действия
                $scope.$watch('declarationTemplate.status', function () {
                    $scope.updateStatusBtnName = $filter('translate')($scope.declarationTemplate.status === APP_CONSTANTS.VERSIONED_OBJECT_STATUS.NORMAL ?
                        "declarationTemplate.button.deactivate" : "declarationTemplate.button.activate")
                });

                // Проверки перед сохранением
                function checkBeforeSave() {
                    if (!$scope.declarationTemplate.version) {
                        $dialogs.errorDialog({content: $filter("translate")("declarationTypeJournal.error.yearFromUndefined")});
                        return false;
                    }
                    if ($scope.declarationTemplate.versionEnd && $scope.declarationTemplate.version < $scope.declarationTemplate.versionEnd) {
                        $dialogs.errorDialog({content: $filter("translate")("declarationTypeJournal.error.badYears")});
                        return false;
                    }
                    if (!$scope.declarationTemplate.name) {
                        $dialogs.errorDialog({content: $filter("translate")("declarationTypeJournal.error.nameUndefined")});
                        return false;
                    }
                    return true;
                }

                // Возвращяет данные из таблицы проверок с фатальностями
                function getChecks() {
                    $scope.templateChecksGrid.ctrl.grid.editCell(0, 0, false);
                    var ids = $scope.templateChecksGrid.ctrl.grid.getDataIDs();
                    var checks = [];
                    _.each(ids, function (rowId) {
                        checks.push($scope.templateChecksGrid.ctrl.grid.getLocalRow(rowId));
                    });
                    return checks;
                }

                // Выводит предупреждение, и если П. согласен, то запускает операцию занова
                function confirmAndTryAgain(message, func) {
                    $dialogs.confirmDialog({
                        content: $filter('translate')(message),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            func(true);
                        }
                    });
                }
            }
        ])
}());