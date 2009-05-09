LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#
# Build vpnc-debug.h
#



LOCAL_SRC_FILES:= \
	make-tun-device.c

LOCAL_CFLAGS += -fPIC -DPIC

# LOCAL_MODULE_TAGS:=debug
LOCAL_MODULE:=make-tun-device

include $(BUILD_EXECUTABLE)
