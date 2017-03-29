package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Адресообразующий объект
 *
 * @author Andrey Drunk
 */
public class AddressObject extends IdentityObject<Long> {

    /**
     * Идентификатор родительского узла
     */
    private Long parentId;

    /**
     * Наименование объекта оптимизи
     */
    private String formalName;

    /**
     * Сокращенное наименование типа
     */
    private String shortName;

    /**
     * Почтовый индекс
     */
    private String postalCode;

    /**
     * Уровень в текущей иерархии
     */
    private int level;

    /**
     * Признак листового узла, true - листовой, false-нет
     */
    private boolean leaf;

    /**
     * Код Региона
     */
    private String regionCode;

    /**
     * Наименование региона
     */
    private String regionName;

    /**
     * Признак того что элемент адреса прошел проверку по справочнику фиас
     */
    private boolean valid;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getFormalName() {
        return formalName;
    }

    public void setFormalName(String formalName) {
        this.formalName = formalName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("regionCode", regionCode)
                .append("shortName", shortName)
                .append("formalName", formalName)
                .append("valid", valid)
                .append("leaf", leaf)
                .toString();
    }
}
