(function () {
    'use strict'

    angular
        .module('mtsUsim.Constants', [])

        .constant('APP_CONSTANTS', {
            SESSION: {
                SELECTED_IDS: "selectedIds",
                FILTER_PRODUCTION: "filterProduction",
                FILTER_ORDERS: "filterOrders"
            },
            ENTITY_TYPE_ID: {
                REMAINS: 1,
                FORECAST: 2,
                ORDERS: 3
            },
            COMMON: {
                PAGING_SIZES:[10, 20, 50, 100, 200, 500]
            },
            CONSTRAINTS: {
                MAX_STRING_LENGTH: 4000,
                MAX_NUMBER: 1000000000000000000,
                MIN_STRING_LENGTH: 0,
                MIN_NUMBER: 0
            },
            ENTITY_STATUS: {
                REMAINS_NEW                             : {id:  1, name: 'Новый'},
                REMAINS_PUBLISHED                       : {id:  2, name: 'Опубликован'},
                REMAINS_CORRECTION                      : {id:  3, name: 'Корректировка'},
                FORECAST_NEW                            : {id:  5, name: 'Новый'},
                FORECAST_APPROVED_MR                    : {id:  6, name: 'Согласован МР'},
                FORECAST_APPROVED_BER                   : {id:  7, name: 'Согласован КЦ'},
                FORECAST_ON_CORRECTION                  : {id:  8, name: 'Корректировка'},
                FORECAST_QUERY_TO_CORRECT               : {id:  9, name: 'Запрос на корректировку'},
                FORECAST_DISTRIBUTED                    : {id: 10, name: 'Распределен по поставщикам'},
                CONSOLIDATED_FORECAST_NEW               : {id: 62, name: 'Не опубликован'},

                ORDERS_NEW                              : {id: 11, name: 'Новый'},
                ORDERS_APPROVED_MR                      : {id: 12, name: 'Согласован МР'},
                ORDERS_ON_CORRECTION                    : {id: 13, name: 'Корректировка'},
                ORDERS_QUERY_TO_CORRECT                 : {id: 14, name: 'Запрос на корректировку'},
                ORDERS_APPROVED_CC                      : {id: 15, name: 'Согласован КЦ'},
                ORDERS_TO_PRODUCTION                    : {id: 16, name: 'Переформирован на производство'},

                PRODUCTION_MR_FILE_ATTACHMENT           : {id: 19, name: 'Прикрепление файлов'},
                PRODUCTION_MR_TO_DELETE                 : {id: 54, name: 'На удалении'},
                PRODUCTION_TO_PRODUCTION                : {id: 20, name: 'Принят в производство'},
                PRODUCTION_PERFORMED                    : {id: 21, name: 'Выполнен'},
                PRODUCTION_SHIPPED                      : {id: 22, name: 'Отгружен'},
                PRODUCTION_DELIVERED                    : {id: 23, name: 'Закрыт'},
                PRODUCTION_CLOSED                       : {id: 24, name: 'Доставлен'},

                PRODUCTION_REGION_FILE_ATTACHMENT       : {id: 47, name: 'Прикрепление файлов'},
                PRODUCTION_REGION_FILES                 : {id: 55, name: 'Файлы в регионе'},
                PRODUCTION_REGION_TO_PRODUCTION         : {id: 35, name: 'Принят в производство'},
                PRODUCTION_REGION_TO_DELETE             : {id: 48, name: 'Заказ на удаление'},
                PRODUCTION_REGION_PERFORMED             : {id: 36, name: 'Выполнен'},
                PRODUCTION_REGION_ON_ROUTE              : {id: 77, name: 'На маршруте'},
                PRODUCTION_REGION_FOR_DELIVERY          : {id: 79, name: 'К доставке'},
                PRODUCTION_REGION_SHIPPED               : {id: 37, name: 'Отгружен'},
                PRODUCTION_REGION_DELIVERED             : {id: 38, name: 'Доставлен'},
                PRODUCTION_REGION_CLOSED_WITHOUT_CLAIMS : {id: 40, name: 'Закрыт без претензий'},
                PRODUCTION_REGION_CLAIM                 : {id: 80, name: 'Претензия'},
                PRODUCTION_REGION_CLOSED_CLAIMS         : {id: 39, name: 'Закрыт с претензиями'},
                PRODUCTION_REGION_NOTIFIED              : {id: 49, name: 'Оповещен'},

                CONSOLIDATED_DISTRIBUTED                : {id: 67, name: 'Опубликован'},

                PLANNING_TO_PRODUCTION                  : {id: 25, name: 'Принят в производство'},
                PLANNING_ON_APPROVAL                    : {id: 26, name: 'На согласовании'},
                PLANNING_APPROVED_MR                    : {id: 27, name: 'Согласован МР'},
                PLANNING_NOT_APPROVED_MR                : {id: 28, name: 'Не согласован МР'},
                PLANNING_APPROVED_BER                   : {id: 29, name: 'Согласован КЦ'},
                PLANNING_QUERY_TO_CORRECT               : {id: 30, name: 'Запрос на корректировку'},
                PLANNING_CORRECTION                     : {id: 31, name: 'Корректировка'},
                PLANNING_PERFORMED                      : {id: 32, name: 'Выполнен'},
                PLANNING_SHIPPED                        : {id: 33, name: 'Отгружен'},

                SHIPMENT_PERFORMED                      : {id: 41, name: 'Выполнен'},
                SHIPMENT_ON_ROUTE                       : {id: 42, name: 'На маршруте'},
                SHIPMENT_TO_DELIVERY                    : {id: 57, name: 'К доставке'},
                SHIPMENT_DELIVERED_TO_STORE             : {id: 43, name: 'Доставлен на склад'},
                SHIPMENT_CLOSED_WITHOUT_CLAIMS          : {id: 45, name: 'Закрыт без претензий'},
                SHIPMENT_CLOSED_CLAIMS                  : {id: 46, name: 'Закрыт с претензиями'},
                SHIPMENT_CLAIM                          : {id: 44, name: 'Претензия'},
                SHIPMENT_READY_FOR_SHIPMENT             : {id: 56, name: 'Готов к отгрузке'}
            },
            FILE_TYPE: {
                ORDER_ACTUAL                            :{id: 1, name: "Актуальный файл согласования Заказов-исполнений"},
                INCOME                                  :{id: 12, name: "файл прихода"},
                OUT                                     :{id: 6, name: "Файл формата OUT"},
                OUT2                                    :{id: 3, name: "Файл формата OUT2"},
                MML                                     :{id: 7, name: "Файл формата MML"},
                OOX                                     :{id: 8, name: "Файл формата 00x"},
                LNK                                     :{id: 4, name: "Файл формата LNK"},
                CLAIM                                   :{id: 29, name: "Файл претензий"}
            },
            PLAN_STATE: {
                IN_PLAN                                 :{id: 0, name: "В плане"},
                IN_PRODUCTION                           :{id: 1, name: "В производстве"},
                FOR_DELETE                              :{id: 2, name: "Заказ на удаление"}
            }
        })
}());