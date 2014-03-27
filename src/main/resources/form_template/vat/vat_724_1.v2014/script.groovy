import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Отчёт о суммах начисленного НДС по операциям Банка
 *
 * formTemplateId=600
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        break
    case FormDataEvent.CALCULATE:
        break
    case FormDataEvent.CHECK:
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        break
    case FormDataEvent.COMPOSE:
        break
}