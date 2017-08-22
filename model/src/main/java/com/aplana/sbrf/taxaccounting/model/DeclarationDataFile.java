package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Информация о файлах налоговой формы(declaration).
 *
 * @author lhaziev
 */
public class DeclarationDataFile implements Serializable, AuthorisableEntity {
	private static final long serialVersionUID = -1566841683151489811L;

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

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDepartmentName() {
        return userDepartmentName;
    }

    public void setUserDepartmentName(String userDepartmentName) {
        this.userDepartmentName = userDepartmentName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getFileTypeId() {
        return fileTypeId;
    }

    public void setFileTypeId(long fileTypeId) {
        this.fileTypeId = fileTypeId;
    }

    public String getFileTypeName() {
        return fileTypeName;
    }

    public void setFileTypeName(String fileTypeName) {
        this.fileTypeName = fileTypeName;
    }

    @Override
    public long getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "DeclarationDataFile{" +
                "declarationDataId=" + declarationDataId +
                ", uuid='" + uuid + '\'' +
                ", fileName='" + fileName + '\'' +
                ", date=" + date +
                ", userName='" + userName + '\'' +
                ", userDepartmentName='" + userDepartmentName + '\'' +
                ", note='" + note + '\'' +
                ", fileTypeId=" + fileTypeId +
                ", fileTypeName='" + fileTypeName + '\'' +
                '}';
    }
}
