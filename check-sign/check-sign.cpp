#include <exception>
#include <io.h>
#include <stdio.h>
#include <windows.h>

typedef int BICR_HANDLE;
typedef BICR_HANDLE H_INIT;
typedef BICR_HANDLE H_PKBASE;

typedef int (__stdcall *cr_init) (int tm_flag, const char *gk, const char *uz, const char *psw, void *tm_number, int *tmn_blen, int *init_mode, H_INIT *init_handle);
typedef int (__stdcall *cr_pkbase_load) (H_INIT init_handle, const char *pk_file, int com_blen, int flag_modify, H_PKBASE* pkbase_handle);
typedef int (__stdcall *cr_check_file) (H_INIT init_handle, H_PKBASE pkbase_handle, const char *file_name, int N, int flag_del, char *userid, int *userid_blen);
typedef int (__stdcall *cr_pkbase_close) (H_PKBASE pkbase_handle);
typedef int (__stdcall *cr_uninit) (H_INIT init_handle);

const LPCSTR DLL_NAME = "bicr4.dll";
HINSTANCE pDll = NULL;

// ��������� ������� �����
bool fileExists(const char *fname) {
  return _access(fname, 0) != -1;
}

// ���������� ����� ������ �� ����
char* getErrorMsg(int errorCode) {
	char *errorMsg = "";
	switch(errorCode) {
		case  1: return "ERR_MEM, �������筮 �����";
		case  2: return "ERR_BAD_SIGN, ������� ����ୠ";
		case  3: return "ERR_BAD_LEN, ����� ���� ����ୠ";
		case  4: return "ERR_BAD_USER, ����� ���짮��⥫� ����७";
		case  5: return "ERR_CRYDRV, �訡�� �।�� ��஢����";
		case  6: return "ERR_INIT_CRYMASK, �訡�� ������஢���� �����-����";
		case  8: return "ERR_NOT_SUPPORTED, �ᯮ������� �㭪樨 �� ���-�, ������ ��� � 4.0";
		case  9: return "ERR_CRC_SK_FILE, �訡�� ����஫쭮� �㬬� 䠩�� � ������� ���箬";
		case 10: return "ERR_THREAD, �訡�� ��������筮��";
		case 11: return "ERR_NO_SIGN, � �஢��塞�� 䠩�� ��������� �������";
		case 12: return "ERR_OPEN_FILE, �訡�� ������ 䠩��";
		case 13: return "ERR_OPEN_MK_FILE, �訡�� ������ 䠩�� � ����� ���箬 ";
		case 14: return "ERR_OPEN_PUB, �訡�� ������ 䠩�� � �㡫��� ���箬";
		case 15: return "ERR_OPEN_SK_FILE, �訡�� ������ 䠩�� � ������� ���箬";
		case 17: return "ERR_READ_SK_FILE, �訡�� �⥭�� 䠩�� � ������� ���箬";
		case 18: return "ERR_READ_MK_FILE, �訡�� �⥭�� 䠩�� � �����-���箬";
		case 19: return "ERR_SIGN_NO_REG, �����䨪��� ������ �� ��ॣ����஢�� � ���";
		case 20: return "ERR_BAD_SELFTEST, ����७��� ���� ������⥪� �஢����� � �訡���";
		case 21: return "ERR_GK_READ, �訡�� �⥭�� �������� ����";
		case 22: return "ERR_UZ_READ, �訡�� �⥭�� 㧫� ������";
		case 23: return "ERR_CRC_GKUZ, �訡�� ����஫쭮� �㬬� �������� ����";
		case 24: return "ERR_GKUZ_PSW, ������ ���� �ॡ�� ����� ��஫�";
		case 25: return "ERR_DSCH, �� ������ ���稪 ��� ";
		case 26: return "ERR_CRC_TM, �訡�� ����஫쭮� �㬬� �� �⥭�� ��";
		case 27: return "ERR_LOAD_GRN_DLL, �訡�� ����㧪� ������⥪� grn.dll";
		case 28: return "ERR_STOP, ��⠭������ ���짮��⥫��";
		case 29: return "ERR_TMDRV_NOT_FOUND, �� �ࠩ��� ��";
		case 30: return "ERR_NO_TM_ATTACHED, �� �ਫ����� �� � �ꥬ����";
		case 31: return "ERR_READ_TM, �訡�� �⥭�� TM";
		case 32: return "ERR_BAD_PARAM, �訡�� � ��ࠬ���� �㭪樨";
		case 33: return "ERR_BAD_HANDLE, �訡�� ���ਯ�� (���ਬ��, �ந�室�� ���饭�� � �����⮬� ࠭�� ���ਯ��� ��� ����� ��������� ���ਯ�� �ᯮ������ ��砩��� ���祭��)";
		case 34: return "ERR_HANDLE_TYPE, ���ࠢ���� ⨯ ���ਯ�� (���ਬ��, ����� ⨯� H_USER � ᮮ⢥�����騩 ��ࠬ��� �㭪樨 ��।����� ���ਯ�� ⨯� H_PKEY)";
		case 35: return "ERR_WRITE_TM, �訡�� ����� ��";
		case 37: return "ERR_READ_NET_FILE, �訡�� �⥭�� 䠩�� �⥢�� ���祩";
		case 39: return "ERR_INIT, �訡�� ���樠����樨 ������⥪�, �� �� �맢�� cr_init";
		case 40: return "ERR_LOAD_KEY, �訡�� ����㧪� ����";
		case 42: return "ERR_NET_KEY, �訡�� �⥢��� ����";
		case 43: return "ERR_NO_CRYP, ���� �� �� ����஢��";
		case 44: return "ERR_BAD_CRYP, �訡�� ����஢���� ����";
		case 45: return "ERR_FILE_KEY, �訡�� 䠩������ ����";
		case 46: return "ERR_READ_FILE, �訡�� �⥭�� 䠩��";
		case 47: return "ERR_WRITE_FILE, �訡�� ����� 䠩��";
		case 48: return "ERR_COMPRESS, �訡�� ������ᨨ";
		case 49: return "ERR_MORE_DATA, �訡�� - ����� ���� �������筠";
		default: return "";
	}
}

// �������������
H_INIT init() {
	int USE_TM = 0;
	char *FILE_GK = "";
	char *FILE_UZ = "";
	int mode;
	H_INIT h_init = 0;
	cr_init crInit = (cr_init) GetProcAddress(pDll, "cr_init");
	crInit(USE_TM + 128, FILE_GK, FILE_UZ, "", NULL, NULL, &mode, &h_init);
	if (h_init == 0) {
		printf("Result: FAIL\nERROR: Not initialized\n");
	}
	return h_init;
}

// �������� ���
H_PKBASE loadBase(H_INIT h_init, const char *bokFile) {
	int COM_LEN = 0;
	H_PKBASE h_pkbase = 0;
	cr_pkbase_load crPkBaseLoad = (cr_pkbase_load) GetProcAddress(pDll, "cr_pkbase_load");
	int errorCode = crPkBaseLoad(h_init, bokFile, COM_LEN, 0, &h_pkbase);
	if (errorCode != 0) {
		printf("Result: FAIL\nERROR: cr_pkbase_load, %d %s\n", errorCode, getErrorMsg(errorCode));
	}
	return h_pkbase;
}

// �������� ���
int checkFile(H_INIT h_init, H_PKBASE h_pkbase, const char *fileForCheck, char *sdelFlag) {
	int N = 1; //�������� ������ ���
	int delFlag = strcmp(sdelFlag, "0"); // 1 - ������� �������, 0 - ���������
	char user_ids[100];
	user_ids[0] = 0;
	int len_ids;
	len_ids = sizeof(user_ids);
	cr_check_file cr�heckFile = (cr_check_file) GetProcAddress(pDll, "cr_check_file");
	int errorCode = cr�heckFile(h_init, h_pkbase, fileForCheck, N, delFlag, user_ids, &len_ids);
	if (errorCode != 0) {
		printf("Result: FAIL\nERROR: cr_check_file, %d %s\n", errorCode, getErrorMsg(errorCode));
	} else {
		printf("Result: SUCCESS\n");
		printf("UserId: %s\n", user_ids);
	}
	return errorCode;
}

// �������� ���
int closeBase(H_PKBASE h_pkbase) {
	cr_pkbase_close crPkBaseClose = (cr_pkbase_close) GetProcAddress(pDll, "cr_pkbase_close");
	int errorCode = crPkBaseClose(h_pkbase);
	if (errorCode != 0) {
		printf("ERROR: cr_pkbase_close, %d %s\n", errorCode, getErrorMsg(errorCode));
	}
	return errorCode;
}

// ���������������
int uninit(H_INIT h_init) {
	cr_uninit crUninit = (cr_uninit) GetProcAddress(pDll, "cr_uninit");
	int errorCode = crUninit(h_init);
	if (errorCode != 0) {
		printf("ERROR: cr_pkbase_close, %d %s\n", errorCode, getErrorMsg(errorCode));
	}
	return errorCode;
}

int main(int argc, char **argv) {
	try {
		if (argc != 4) {
			throw std::exception("ERROR: Incorrect argument count. Must be: BOK_FILE 0/1 CHECK_FILE\n");
		}
		char *sBokFile = argv[1];
		if (!fileExists(sBokFile)) {
			throw std::exception("ERROR: BOK-file must be exists\n");
		}
		char *sDelFlag = argv[2];
		if (strcmp(sDelFlag, "0") * strcmp(sDelFlag, "1") != 0) {
			throw std::exception("ERROR: Param \"del_flag\" must be \"0\" or \"1\"\n");
		}
		char *sFileForCheck = argv[3];
		if (!fileExists(sFileForCheck)) {
			throw std::exception("ERROR: File for check must be exists\n");
		}

		pDll = LoadLibrary(DLL_NAME); // �������� ����������
		if (pDll == NULL) {
			throw std::exception("ERROR: Library not found\n");
		}
		int checkResult = 0;
		H_INIT h_init = init();// �������������
		if (h_init != 0) {
			H_PKBASE h_pkbase = loadBase(h_init, sBokFile); // �������� ���
			if (h_pkbase != 0) {
				checkFile(h_init, h_pkbase, sFileForCheck, sDelFlag); // �������� ���
				closeBase(h_pkbase); // ��������� ���
			}
			uninit(h_init); // ���������������
		}
	} catch (const std::exception& ex) {
		printf("Result: FAIL\n");
		printf(ex.what());
	} 
	// ��������� ���������� �� ������
	if (pDll != NULL) {
		FreeLibrary(pDll);
	}
}