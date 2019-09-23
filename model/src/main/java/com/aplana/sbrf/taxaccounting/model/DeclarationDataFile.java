package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Информация о файлах налоговой формы(declaration).
 *
 * @author lhaziev
 */
@Getter
@Setter
@ToString
public class DeclarationDataFile implements Serializable, SecuredEntity {

    private long declarationDataId;
    private String uuid;
    private String fileName;
    private Date date;
    private String userName;
    private String userDepartmentName;
    private String note;
    private long fileTypeId;
    private String fileTypeName;
    private long permissions;
    private String fileKind;

    @Override
    public long getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }

}
