//#include <jni.h>
//#include <string>
//
//extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_edgedetectionapp_MainActivity_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

//#include <jni.h>
//#include <opencv2/opencv.hpp>
//
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_example_edgedetection_MainActivity_processFrame(JNIEnv *env, jobject thiz, jlong matAddr) {
//    cv::Mat &mat = *(cv::Mat *)matAddr;
//    cv::Canny(mat, mat, 80, 100);
//}
//


#include <jni.h>
#include <opencv2/opencv.hpp>

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgedetectionapp_MainActivity_processFrameNative(JNIEnv *env, jobject thiz, jlong matAddr) {
    cv::Mat &mat = *(cv::Mat *) matAddr;

    // Apply Canny edge detection
    cv::Canny(mat, mat, 80, 150);
}
