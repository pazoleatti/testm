package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат создания новой сущности
 */
public class CreateResult<IdType extends Number> extends ActionResult {
    /**
     * ID созданной сущности
     */
    private IdType entityId;

    public IdType getEntityId() {
        return entityId;
    }

    public void setEntityId(IdType entityId) {
        this.entityId = entityId;
    }
}
