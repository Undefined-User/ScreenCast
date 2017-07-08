#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_nick_app_screencast_app_Factory_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
