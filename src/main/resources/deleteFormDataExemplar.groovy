import groovy.transform.Field
import org.springframework.jndi.JndiTemplate

import javax.sql.DataSource

/**
 * Скрипт для удаления экземпляров форм по идентификатору.
 */

// список идентификаторов экземпляров форм которые надо удалить
@Field
def formDataExamplarIds = [
        // TODO (Ramil Timerbaev) вбить нужные идентификаторы
        -1000,
        -1001,
        -1002
]

@Field
def dataSourceName = 'java:comp/env/jdbc/TaxAccDS'

deleteFormDataExamplar()

/** Удалить формы. */
void deleteFormDataExamplar() {
    try {
        def template = new JndiTemplate()
        def DataSource dataSource = template.lookup(dataSourceName)
        def connection = dataSource.connection
        def stmt = connection.createStatement()

        // формирование запросов и выполнение
        formDataExamplarIds.each { id ->
            stmt.addBatch("delete from form_data where id = $id")
        }
        def results = stmt.executeBatch()

        def deleteForms = []        // список id  удаленных форм
        def notDeleteForms = []     // список id неудаленных/ненайденых форм
        results.eachWithIndex { def result, index ->
            def id = formDataExamplarIds[index]
            (result ? deleteForms.add(id) : notDeleteForms.add(id))
        }

        // вывод результатов
        if (deleteForms) {
            logger.info("Формы успешно удалены (${deleteForms.size()} шт.): ${deleteForms.join(', ')}")
        }
        if (notDeleteForms) {
            logger.warn("Формы не удалены (не найдены) (${notDeleteForms.size()} шт.): ${notDeleteForms.join(', ')}")
        }

        connection.close()
    } catch (Exception ex) {
        logger.error("Ошибка: ${ex.getLocalizedMessage()}")
        ex.printStackTrace()
    }
}