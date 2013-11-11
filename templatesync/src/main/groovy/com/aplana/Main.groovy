package com.aplana

/**
 * Утилита для синхронизации шаблонов НФ
 * http://jira.aplana.com/browse/SBRFACCTAX-4929
 *
 * Запуск командой gradle:
 * gradle run
 *
 * Сборка исполнимого приложения:
 * gradle installApp
 *
 * TODO Реализована только выгрузка из БД. Реализовать загрузку из файловой системы.
 *
 * @author Dmitriy Levykin
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 */
class Main {

    //// Константы
    def static paramsMap = [login: 'conf', pass: 'conf',
            serverAddress: 'http://172.16.127.18:9080',
            rootPath: '/taxaccounting-dist/gwtapp',
            loginPath: '/j_security_check',
            downloadPath: '/download/formTemplate/download']

// Шаблоны в файловой системе
// def resourcesPath = '../src/main/resources/form_template'
    def static resourcesPath = 'C:/form_template'

// Маппинг шаблонов
    def static templates = [deal: [
            380: 'auctions_property',
            382: 'bank_service',
            384: 'bonds_trade',
            387: 'corporate_credit',
            385: 'credit_contract',
            390: 'foreign_currency',
            391: 'forward_contracts',
            388: 'guarantees',
            389: 'interbank_credits',
            386: 'letter_of_credit',
            400: 'matrix',
            392: 'nondeliverable',
            410: 'organization_matching',
            393: 'precious_metals_deliver',
            394: 'precious_metals_trade',
            376: 'rent_provision',
            383: 'repo',
            381: 'securities',
            375: 'software_development',
            377: 'tech_service',
            379: 'trademark']
    ]

    public static void main(String[] args) {
        def api = SyncAPI.getInstance(paramsMap)
        api.login()
        templates.each { taxType ->
            taxType.value.each { template ->
                api.downloadTemplate(template.key, "$resourcesPath/${taxType.key}/${template.value}/")
            }
        }
        api.close()
    }
}
