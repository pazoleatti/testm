package com.aplana.taxaccounting

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
 * @author Dmitriy Levykin
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 */
class Main {

    //// Константы
    // production-mode params
//    def static paramsMap = [login: 'conf', pass: 'conf',
//            serverAddress: 'http://172.16.127.18:9080',
//            rootPath: '/taxaccounting-dist/gwtapp',
//            loginPath: '/j_security_check',
//            downloadPath: '/download/formTemplate/download']
    // dev-mode params
    def static paramsMap = [login: 'conf', pass: 'conf',
            serverAddress: 'http://127.0.0.1:8888',
            rootPath: '',
            loginPath: '/j_security_check',
            downloadPath: '/download/formTemplate/download',
            uploadPath: '/download/formTemplate/upload']

    // Шаблоны в файловой системе
    // def static resourcesPath = '../src/main/resources/form_template'
    def static resourcesPath = 'C:/form_template'

    // Маппинг шаблонов
    def static templates = [
            deal:[
                    380:'auctions_property',
                    382:'bank_service',
                    384:'bonds_trade',
                    387:'corporate_credit',
                    385:'credit_contract',
                    390:'foreign_currency',
                    391:'forward_contracts',
                    388:'guarantees',
                    389:'interbank_credits',
                    386:'letter_of_credit',
                    400:'matrix',
                    392:'nondeliverable',
                    410:'organization_matching',
                    393:'precious_metals_deliver',
                    394:'precious_metals_trade',
                    376:'rent_provision',
                    383:'repo',
                    381:'securities',
                    375:'software_development',
                    377:'tech_service',
                    379:'trademark'
            ],
            income:[
                    500:'advanceDistribution',
                    362:'f7_8',
                    302:'income_complex',
                    301:'income_simple',
                    303:'outcome_complex',
                    304:'outcome_simple',
                    306:'output1',
                    307:'output2',
                    308:'output3',
                    502:'rnu107',
                    395:'rnu108',
                    396:'rnu1110',
                    367:'rnu111',
                    374:'rnu112',
                    369:'rnu115',
                    368:'rnu116',
                    370:'rnu117',
                    373:'rnu118',
                    371:'rnu119',
                    364:'rnu12',
                    378:'rnu120',
                    321:'rnu14',
                    499:'rnu16',
                    501:'rnu17',
                    322:'rnu22',
                    323:'rnu23',
                    324:'rnu25',
                    325:'rnu26',
                    326:'rnu27',
                    329:'rnu30',
                    328:'rnu31',
                    330:'rnu32_1',
                    331:'rnu32_2',
                    332:'rnu33',
                    333:'rnu36_1',
                    334:'rnu38_1',
                    335:'rnu38_2',
                    337:'rnu39_2',
                    316:'rnu4',
                    338:'rnu40_1',
                    339:'rnu40_2',
                    340:'rnu44',
                    341:'rnu45',
                    342:'rnu46',
                    344:'rnu47',
                    343:'rnu48_1',
                    313:'rnu48_2',
                    312:'rnu49',
                    317:'rnu5',
                    365:'rnu50',
                    345:'rnu51',
                    346:'rnu53',
                    348:'rnu55',
                    349:'rnu56',
                    353:'rnu57',
                    318:'rnu6',
                    351:'rnu60',
                    352:'rnu61',
                    352:'rnu62',
                    355:'rnu64',
                    311:'rnu7',
                    504:'rnu70_1',
                    357:'rnu70_2',
                    356:'rnu71_1',
                    503:'rnu71_2',
                    358:'rnu72',
                    366:'rnu75',
                    320:'rnu8'
            ],
            transport:[
                    202:'benefit_vehicles',
                    201:'vehicles',
                    200:'summary',
            ]
    ]

    public static void main(String[] args) {
        def api = SyncAPI.getInstance(paramsMap)
        api.login()
        try {
            downloadAll(api)
            // uploadAll(api)
        }
        finally {
            api.close()
        }
    }

    private static void downloadAll(SyncAPI api) {
        templates.each { taxType ->
            taxType.value.each { template ->
                api.downloadTemplate(template.key, "$resourcesPath/${taxType.key}/${template.value}/")
            }
        }
    }

    private static void uploadAll(SyncAPI api) {
        templates.each { taxType ->
            taxType.value.each { template ->
                api.uploadTemplate(template.key, "$resourcesPath/${taxType.key}/${template.value}/")
            }
        }
    }
}
