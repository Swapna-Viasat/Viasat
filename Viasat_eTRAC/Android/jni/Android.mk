# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


LOCAL_PATH := $(call my-dir)

L_DEFS := -DHAVE_CONFIG_H -UAF_INET6
L_CFLAGS := $(L_DEFS)

L_COMMON_HDR := ./src/Condition.h \
			./src/gnu_getopt.h \
			./src/Locale.h \
			./src/Reporter.h \
			./src/Thread.h \
			./src/config.win32.h \
			./src/headers.h \
			./src/Mutex.h \
			./src/service.h \
			./src/util.h \
			./src/Extractor.h \
			./src/inet_aton.h \
			./src/iperf-int.h \
			./src/report_CSV.h \
			./src/snprintf.h \
			./src/version.h \
			./src/gettimeofday.h \
			./src/List.h \
			./src/report_default.h \
			./src/SocketAddr.h \
			./src/Client.hpp \
			./src/Listener.hpp \
			./src/Server.hpp \
			./src/Timestamp.hpp \
			./src/delay.hpp \
			./src/PerfSocket.hpp \
			./src/Settings.hpp

L_COMMON_SRC := ./src/Extractor.c \
			./src/Locale.c \
			./src/Reporter.c \
			./src/sockets.c \
			./src/gnu_getopt.c \
			./src/ReportCSV.c \
			./src/service.c \
			./src/stdio.c \
			./src/gnu_getopt_long.c \
			./src/ReportDefault.c \
			./src/SocketAddr.c \
			./src/tcp_window_size.c \
			./src/Client.cpp \
			./src/List.cpp \
			./src/main.cpp \
			./src/Server.cpp \
			./src/Launch.cpp \
			./src/Listener.cpp \
			./src/PerfSocket.cpp \
			./src/Settings.cpp

L_COMMON_COMPAT := ./src/headers_slim.h \
			./src/error.c \
			./src/inet_ntop.c \
			./src/signal.c \
			./src/string.c \
			./src/gettimeofday.c \
			./src/inet_pton.c \
			./src/snprintf.c \
			./src/Thread.c \
			./src/delay.cpp

iperf_SOURCES := $(L_COMMON_HDR) $(L_COMMON_SRC) $(L_COMMON_COMPAT)

include $(CLEAR_VARS)
LOCAL_MODULE := iperf
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_MODULE_TAGS := tests eng
LOCAL_CFLAGS := $(L_CFLAGS) -fPIE
LOCAL_LDFLAGS += -fPIE -pie
LOCAL_SRC_FILES := $(iperf_SOURCES)
include $(BUILD_EXECUTABLE)




