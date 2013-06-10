#include<android/log.h>
#define LOG_TAG "native"
#define LOGD(msg, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,msg, ##args)
#define LOGE(msg, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,msg, ##args)
