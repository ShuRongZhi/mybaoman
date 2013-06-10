#include "FileHelper.h"
#include <sys/types.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>


//判断文件是否存在
bool FileHelper::checkFileExits(std::string filename)
{
    std::string name = CacheName + filename;
    //LOGD("请求检查的文件名为:%s",filename.c_str());
    _file.open(name.c_str(),std::ios::in);
    if(!_file)
    {
        //LOGD("该文件不存在");
        return false;
    }
    else
    {
        //LOGD("该文件已存在");
        _file.close();
        return true;
    }
}

//判断文件夹是否存在
bool FileHelper::checkFolderExits(std::string foldername)
{
    if(0 == access(foldername.c_str(),F_OK))
    {
        return true;
    }
    else
    {
        return false;
    }
}

//判断缓存文件夹是否存在
bool FileHelper::checkCacheFolderExits()
{
    return this->checkFolderExits(CacheName);
}

//创建文件夹
bool FileHelper::createFolder(std::string name)
{
    if(0 == mkdir(name.c_str(), 0777))
    {
        return true;
    }
    else
    {
        return false;
    }
}

//创建缓存文件夹
bool FileHelper::createCacheFolder()
{
    return this->createFolder(CacheName);
}

//获取图片路径
std::string FileHelper::getImagePath(std::string name)
{
    return CacheName + name;
}

//获取缓存文件路径
std::string FileHelper::getCachePath()
{
    
    return CacheName;
}

//删除图片，指定完整路径名
//传递all表示删除缓存文件夹下所有文件
bool FileHelper::delImage(std::string path)
{
	if(0 == path.length())
	{
		LOGE("delImage不接受空参数!");
		return false;
	}
	if("all" == path)
	{
	    std::string command;
	    command = "rm -r " + CacheName;
	    LOGD("%s",command.c_str());
	    //调用sytem的rm-r删除文件夹
		system(command.c_str());
		return true;
	}
	else
	{
		LOGD("尝试删除文件:%s",path.c_str());
		if(-1 == remove(path.c_str()))
		{
		    LOGD("删除图片失败");
			return false;
		}
		else
		{
		    LOGD("删除图片成功");
			return true;
		}
	}
}


