package com.atguigu.gmall.product.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传的工具类
 */
public class FastDFSUtil {

    /**
     * 静态模块进行初始化配置
     */
    static {
        try {
            //读取配置文件
            ClassPathResource classPathResource = new ClassPathResource("tracker.conf");
            //实现tracker的初始化
            ClientGlobal.init(classPathResource.getPath());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param file
     * @return
     */
    public static String upload(MultipartFile file){
        try {
            //获取tracker的connection
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //通过连接构建stroage
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //文件的上传:1.文件的名字 2.文件的拓展名 3.附加参数:地址 时间 作者
            String[] strings = storageClient.upload_file(file.getBytes(),
                    StringUtils.getFilenameExtension(file.getOriginalFilename()),
                    null);

            //返回文件的url: strings[0] ==组名 strings[1]=全量路径名+文件名
            return strings[0]  + "/" + strings[1];
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件的删除
     * @param group_name
     * @param remote_filename
     * @return
     */
    public static Boolean delFile(String group_name, String remote_filename){
        try {
            //获取tracker的connection
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //通过连接构建stroage
            StorageClient storageClient = new StorageClient(trackerServer, null);
           //删除
            int i = storageClient.delete_file(group_name, remote_filename);
            //返回删除结果
            return i>=0?true:false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 文件的下载
     * @param group_name
     * @param remote_filename
     * @return
     */
    public static byte[] download(String group_name, String remote_filename){
        try {
            //获取tracker的connection
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //通过连接构建stroage
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //删除
            byte[] bytes = storageClient.download_file(group_name, remote_filename);
            //返回字节码
            return bytes;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
