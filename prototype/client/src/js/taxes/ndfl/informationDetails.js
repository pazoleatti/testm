'use strict';
(function() {

/*
    angular.module('sbrfNdfl.ndflDetailsForms', ['ui.router', 'sbrfNdfl.widgets'])
        .controller('informationDetailsFormsCtrl', [
            '$scope', '$timeout', 'aplanaEntityUtils', '$state', '$stateParams', 'aplanaDialogs'
            /*
            ,
             function($scope, $timeout, aplanaEntityUtils, $state, $stateParams, aplanaDialogs) {
                 $scope.customGridColumnsBuilder = function(dataOptions, gridOptions) {
                     for (var fieldName in dataOptions.metaData) {
                         var field = dataOptions.metaData[fieldName];
                         var columnDef = {};
                        columnDef.name = field.name;
                        columnDef.displayName = field.title;
                        columnDef.enableHiding = false;
                        columnDef.cellTooltip = true;
                        columnDef.visible = field.visible;
                        columnDef.width = field.width || 100;
                        if (field.type == "java.util.Date") {
                           var format = field.format == "" ? "dd.MM.yyyy" : field.format;
                           columnDef.type = 'date';
                           columnDef.cellFilter = "date:'" + format + "'";
                        }
                        gridOptions.columnDefs.push(columnDef);
                    }
                    aplanaEntityUtils.fitColumnsWidth(gridOptions, $scope.gridApi.grid.gridWidth);
                 };

                var dataStub = {
                    'list': [
                        {

                        }
                    ]
                }

                var dataForRequisites = {
                    "list": [
                         {
                             id: 1,
                             formType: {id: 1, name: "Первичная"},
                             formKind: {id: 1, name: "РНУ НДФЛ (первичная)"},
                             department: {id: 1, name: "Иркутское отделение №8586 ПАО Сбербанк"},
                             asnu: {id: 1, name: "АС \"SAP\""},
                             period: {id: 1, name: "2017; 1 квартал"},
                             state: {id: 1, name: "Создана"},
                             fileTF: "99_6100_01200021201728042017000000000000000000015000.xml",
                             creationDate: new Date(),
                             creator: "Хазиев Ленар"
                         }],
                         "metaData": [
                              {
                                   "name": "id",
                                   "type": "java.lang.String",
                                   "title": "№п/п",
                                   "width": 60,
                                   "order": 1,
                                   "visible": true
                              }
                         ]
                }

                var tableData;

                function fetchData() {
                    var data = JQuery.extend({}, dataForRepositories);
                    data.list = tableData;

                }

                function setButtonsEnabled() {
                }

                /*
                * обработчики нажатий на кнопки
                */
                /*
                // проверить
                $scope.checkButtonClick = function() {
                    // Do check
                };
                // рассчитать
                $scope.calculateButtonClick = function() {
                    // Do calculate
                }
                // принять
                $scope.acceptButtonClick = function() {

                }

        }
        */
    /*
    ]);
    */


})