package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.AttachFileType;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

/**
 * Реализация прав для файлов декларации
 */
@Configurable
public abstract class DeclarationDataFilePermission extends AbstractPermission<DeclarationDataFile> {
    @Autowired
    protected DeclarationDataDao declarationDataDao;
    @Autowired
    protected TAUserService userService;
    /**
     * Право на удаление файла декларации
     */
    public static final Permission<DeclarationDataFile> DELETE = new DeletePermission(1 << 0);
    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public DeclarationDataFilePermission(long mask) {
        super(mask);
    }

    public static final class DeletePermission extends DeclarationDataFilePermission {

        public DeletePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationDataFile targetDomainObject) {
            if (DeclarationDataPermission.VIEW.isGranted(currentUser, declarationDataDao.get(targetDomainObject.getDeclarationDataId()))
                    && targetDomainObject.getFileTypeId() != AttachFileType.TYPE_1.getId()
                    && !targetDomainObject.getUserName().equals(userService.getSystemUserInfo().getUser().getName())) {
                return true;
            }

            return false;
        }
    }
}
