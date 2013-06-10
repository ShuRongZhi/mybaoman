#include "DownloadHelper.h"
#include "FileHelper.h"

//缓存文件名
std::string CachePath;

//设置下载结构体
struct DownInfo{
  //文件名
  std::string _ItemID;
  std::string _ImageID;
  //文件输出
  FILE * _stream;
}_downInfo;


//回调函数,用于获取数据
static size_t WirteToFile(void *buffer, size_t size, size_t nmemb, void *stream)
{
  LOGD("缓冲区大小: %d",size*nmemb);
  struct DownInfo *out = (struct DownInfo *)stream;
  if(out && !out->_stream)
  {
    //以  漫画ID_图片ID  的格式作为文件名
    std::string file_name = CachePath + out->_ItemID + "_" + out->_ImageID;
   //以二进制的方法打开文件
    LOGD("文件名:%s",file_name.c_str());
    out->_stream=fopen(file_name.c_str(), "wb+");
    if(!out->_stream)
    {        
        LOGE("无法打开目标文件写入!");
        return -1;
    }
  }
  size_t res = fwrite(buffer, size, nmemb, out->_stream);
  //LOGD("fwrite返回%d",res);
  return res;
} 


std::string DownloadHelper::getImage(std::string ItemID,std::string ImageID)
{
    FileHelper l_mFileHelper;   
    std::string imageName = ItemID + "_" + ImageID;
    //取得缓存路径
    CachePath = l_mFileHelper.getCachePath();
    //判断图片是否已缓存
    if(l_mFileHelper.checkFileExits(imageName))
    {
        LOGD("图片已缓存");
        //如果图片已缓存，则直接返回图片路径
        return l_mFileHelper.getImagePath(imageName);
    }
    else
    {
      LOGD("图片未缓存，向网络请求图片");
      //给结构体设置成员变量
      _downInfo._ItemID = ItemID;
      _downInfo._ImageID = ImageID;
      std::string url;
      //生成获取图片url
      url = "http://wanzao2.b0.upaiyun.com/system/pictures/" + ItemID +"/original/" + ImageID;
      //判断下载是否成功
      if(download(url))
      {
        return l_mFileHelper.getImagePath(imageName);
      }
      else
      {
        //下载失败，删除失败文件
      	l_mFileHelper.delImage(l_mFileHelper.getImagePath(imageName));
        return "error";
      }
      
    }
}

bool DownloadHelper::download(std::string url)
{
    bool returnValue = true;
    if(0 == url.length())
    {
        LOGE("请求资源的地址为空");
        return false;
    }
    else
    {
        LOGD("请求的URL地址为:%s",url.c_str());   
        //初始化 curl_global_init
        curl_global_init(CURL_GLOBAL_DEFAULT);
        curl = curl_easy_init(); 
        if(curl) {
            curl_easy_setopt(curl, CURLOPT_URL,url.c_str()); // 设置访问链接
            curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION,WirteToFile); // 设置回调函数
            curl_easy_setopt(curl, CURLOPT_WRITEDATA, &_downInfo); //设置回调参数（在回调函数内可以调用)
            res = curl_easy_perform(curl); //执行获得curl返回参数用于判断成功与否
            curl_easy_cleanup(curl);
            
            if(CURLE_OK == res)
            {
                LOGD("下载成功!");
                returnValue = true;   
            }
            else
            {
                LOGE("下载失败！");
                setError(-1);
                returnValue = false;
            }
            
        }
     
        if(_downInfo._stream)
        {
            //关闭文件
            if(0 == fclose(_downInfo._stream))
            {
                LOGD("关闭文件成功！");
            }
            else
            {
                LOGE("关闭文件失败!");
            }
            _downInfo._stream = NULL;
        }
        
        LOGD("清空资源");
        curl_global_cleanup();
    }
    return returnValue;
}

//设置错误码，赋值给成员变量errCode
void DownloadHelper::setError(int err)
{
    errCode = err;
}

//取得成员变量errCode的值返回
int DownloadHelper::getError()
{
    return errCode;
}

