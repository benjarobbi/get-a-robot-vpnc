LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#
# Build vpnc-debug.h
#

VERSION=`sh ./mk-version` 

vpnc-debug.h:
	cd $(LOCAL_PATH) && $(MAKE) $@


LOCAL_SRC_FILES:= \
        config.c math_group.c \
        decrypt-utils.c dh.c \
        supp.c isakmp-pkt.c netbsd_getpass.c  \
        vpnc.c sysdep.c tunip.c vpnc-debug.c 

LOCAL_C_INCLUDES += \
	external/libgcrypt/src/ \
	external/libgpg-error/src/ \
	$(LOCAL_PATH) 

LOCAL_CFLAGS += -DVERSION=\"$VERSION\"  -D__android__ \
		-W -Wall -Wwrite-strings \

# LOCAL_MODULE_TAGS:=debug
LOCAL_MODULE:=vpnc

LOCAL_STATIC_LIBRARIES:= \
	libgcrypt \
	libgpg-error \
	libc
	
include $(BUILD_EXECUTABLE)
