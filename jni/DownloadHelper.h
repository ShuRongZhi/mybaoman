#include <string>
#include<android/log.h>
#include "curl/curl.h"
#include "debugOut.h"

class DownloadHelper{
    public:
        DownloadHelper(){
            //LOGD("DownloadHelper构造函数");
        }
        ~DownloadHelper(){
            //LOGD("DonwloadHelper析构函数");
        }
        //获取图片函数，如果图片已缓存直接返回路径，否则调用download下载
        //如果失败返回error
        std::string getImage(std::string,std::string);
        //下载图片函数
        bool download(std::string);
        
        
        //错误码的设置和获取
        void setError(int);
        int getError();
   private:
        int errCode;
        CURL *curl;
        CURLcode res;
};


