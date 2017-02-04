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
     * Уровень в текущей иерархии
     */
    private int level;

    /**
     * Признак листового узла, true - листовой, false-нет
     */
    private boolean isLeaaf;

    /**
     * Аддрес в виде строки с разделителями \\регион\\район\\город\\улица
     */
    private String addressPath;

    /**
     * Код Региона
     */
    private String regionCode;


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

    public boolean isLeaaf() {
        return isLeaaf;
    }

    public void setLeaaf(boolean leaaf) {
        isLeaaf = leaaf;
    }

    public String getAddressPath() {
        return addressPath;
    }

    public void setAddressPath(String addressPath) {
        this.addressPath = addressPath;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("parentId", parentId)
                .append("formalName", formalName)
                .append("level", level)
                .append("isLeaaf", isLeaaf)
                .append("addressPath", addressPath)
                .append("regionCode", regionCode)
                .append("leaaf", isLeaaf())
                .toString();
    }
}
