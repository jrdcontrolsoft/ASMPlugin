#include <jni.h>
#include "winapi.h"
#include <windows.h>
#include <winnt.h>

DWORD AbsoluteSeek(HANDLE, DWORD);
BOOL ReadBytes(HANDLE, LPVOID, DWORD);
DWORD ScanExeHeader(const char *);
BOOL DetectWinDOSString(const char *);

JNIEXPORT jstring JNICALL Java_de_fh_1zwickau_asmplugin_WinApi_getShortName(JNIEnv *env, jclass clazz, jstring longname) {
  if (longname == NULL) {
    jclass exc = env->FindClass("java/lang/NullPointerException");

    if (exc != NULL)
      env->ThrowNew(exc, "Longname is NULL!");

    return env->NewStringUTF("");
  }

  const jbyte* lname = env->GetStringUTFChars(longname, NULL);
  char sname[MAX_PATH];
  GetShortPathName(lname, sname, MAX_PATH);
  return env->NewStringUTF(sname);
}

JNIEXPORT void JNICALL Java_de_fh_1zwickau_asmplugin_WinApi_shExecute(JNIEnv *env, jclass, jstring filename, jstring params, jstring directory, jlong show) {
  if ((filename == NULL) || (params == NULL) || (directory == NULL)) {
    jclass exc = env->FindClass("java/lang/NullPointerException");

    if (exc != NULL) {
      if (filename == NULL) env->ThrowNew(exc, "Filename is NULL!");
      if (params == NULL) env->ThrowNew(exc, "Parameter is NULL!");
      if (directory == NULL) env->ThrowNew(exc, "Directory is NULL!");
    }

    return;
  }

  const jbyte* fname = env->GetStringUTFChars(filename, NULL);
  const jbyte* prm = env->GetStringUTFChars(params, NULL);
  const jbyte* dir = env->GetStringUTFChars(directory, NULL);

  ShellExecute(GetDesktopWindow(), "open", fname, prm, dir, show);
}

JNIEXPORT jboolean JNICALL Java_de_fh_1zwickau_asmplugin_WinApi_isWinNT(JNIEnv *, jclass) {
  OSVERSIONINFO osvi;

  osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);

  if(GetVersionEx(&osvi) != FALSE) {
    if (osvi.dwPlatformId == VER_PLATFORM_WIN32_NT)
      return true;
  }

  return false;
}

JNIEXPORT jboolean JNICALL Java_de_fh_1zwickau_asmplugin_WinApi_isWin32Exe(JNIEnv * env, jclass, jstring filename) {
  const jbyte* fname = env->GetStringUTFChars(filename, NULL);

  if ((ScanExeHeader(fname) > 0) || (DetectWinDOSString(fname) == TRUE)) return true;

  return false;
}

JNIEXPORT jboolean JNICALL Java_de_fh_1zwickau_asmplugin_WinApi_isWinConsole(JNIEnv * env, jclass, jstring filename) {
  const jbyte* fname = env->GetStringUTFChars(filename, NULL);

  if (ScanExeHeader(fname) == 2) return true;

  return false;
}

JNIEXPORT jstring JNICALL Java_de_fh_1zwickau_asmplugin_WinApi_getWindowsDirectoryAPI(JNIEnv *env, jclass) {
  char wdir[MAX_PATH];
  GetWindowsDirectory(wdir, MAX_PATH);
  return env->NewStringUTF(wdir);
}

JNIEXPORT jstring JNICALL Java_de_fh_1zwickau_asmplugin_WinApi_getWindowsSystemDirectoryAPI(JNIEnv *env, jclass) {
  char wsdir[MAX_PATH];
  GetSystemDirectory(wsdir, MAX_PATH);
  return env->NewStringUTF(wsdir);
}

DWORD AbsoluteSeek(HANDLE hFile, DWORD offset) {
  DWORD newOffset;

  if ((newOffset = SetFilePointer(hFile, offset, NULL, FILE_BEGIN)) == 0xFFFFFFFF)
    return 0;

  return newOffset;
}

BOOL ReadBytes(HANDLE hFile, LPVOID buffer, DWORD size) {
  DWORD bytes;

  if (!ReadFile(hFile, buffer, size, &bytes, NULL)) {
    return FALSE;
  } else if (size != bytes) {
    return FALSE;
  }

  return TRUE;
}

DWORD ScanExeHeader(const char * filename) {
  HANDLE hImage;
  DWORD CoffHeaderOffset;
  DWORD SectionOffset;
  DWORD MoreDosHeader[16];
  ULONG ntSignature;
  IMAGE_DOS_HEADER image_dos_header;
  IMAGE_FILE_HEADER image_file_header;
  IMAGE_OPTIONAL_HEADER image_optional_header;

  hImage = CreateFile(filename, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

  if (INVALID_HANDLE_VALUE == hImage)
    return 0;

  if (ReadBytes(hImage, &image_dos_header, sizeof(IMAGE_DOS_HEADER)) == FALSE) {
    CloseHandle(hImage);
    return 0;
  }

  if (IMAGE_DOS_SIGNATURE != image_dos_header.e_magic) {
    CloseHandle(hImage);
    return 0;
  }

  if (ReadBytes(hImage, MoreDosHeader, sizeof(MoreDosHeader)) == FALSE) {
    CloseHandle(hImage);
    return 0;
  }

  CoffHeaderOffset = AbsoluteSeek(hImage, image_dos_header.e_lfanew) + sizeof(ULONG);

  if (CoffHeaderOffset == 0) {
    CloseHandle(hImage);
    return 0;
  }

  if (ReadBytes(hImage, &ntSignature, sizeof(ULONG)) == FALSE) {
    CloseHandle(hImage);
    return 0;
  }

  if (IMAGE_NT_SIGNATURE != ntSignature) {
    CloseHandle(hImage);
    return 0;
  }

  SectionOffset = CoffHeaderOffset + IMAGE_SIZEOF_FILE_HEADER + IMAGE_SIZEOF_NT_OPTIONAL_HEADER;

  if (ReadBytes(hImage, &image_file_header, IMAGE_SIZEOF_FILE_HEADER) == FALSE) {
    CloseHandle(hImage);
    return 1;
  }

  if (ReadBytes(hImage, &image_optional_header, IMAGE_SIZEOF_NT_OPTIONAL_HEADER) == FALSE) {
    CloseHandle(hImage);
    return 1;
  }

  if ((image_optional_header.Subsystem == IMAGE_SUBSYSTEM_WINDOWS_CUI) || (image_optional_header.Subsystem == IMAGE_SUBSYSTEM_OS2_CUI) || (image_optional_header.Subsystem == IMAGE_SUBSYSTEM_POSIX_CUI)) {
    CloseHandle(hImage);
    return 2;
  }

  CloseHandle(hImage);

  return 1;
}

BOOL DetectWinDOSString(const char * filename) {
  HANDLE hFile;
  char buffer[256];
  char find[] = "this program cannot be run in dos mode\0";

  hFile = CreateFile(filename, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

  if (INVALID_HANDLE_VALUE == hFile)
    return FALSE;

  if (ReadBytes(hFile, buffer, sizeof(buffer)) == FALSE) {
    CloseHandle(hFile);
    return FALSE;
  }

  for (int i = 0; i < 255; i++) {
    if (buffer[i] < ' ') {
      buffer[i] = ' ';
    } else if ((buffer[i] >= 'A') && (buffer[i] <= 'Z')) {
      buffer[i] += 32;
    }
  }

  buffer[255] = '\0';

  if (strstr(buffer, find) != NULL) {
    CloseHandle(hFile);
    return TRUE;
  }

  CloseHandle(hFile);

  return FALSE;
}