package com.atguigu.gmall.common.fastdfs;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 操作fastdsf的工具类
 */
@Component//交给spring管理
public class FastDfsClient {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    /**
     * 文件上传
     */
    public String upload(MultipartFile file) throws Exception{
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), //文件数据流
                file.getSize(),//文件大小
                StringUtils.getFilenameExtension(file.getOriginalFilename()),//文件拓展名
                null);//附加参数
        //返回全量路径
        return storePath.getFullPath();
    }

    /**
     * 文件下载
     * @return
     */
    public byte[] download(String groupName, String path) throws Exception{
        //下载文件
        InputStream inputStream = fastFileStorageClient.downloadFile(groupName, path, new DownloadCallback<InputStream>() {
            //下载发生异常返回异常数据流
            @Override
            public InputStream recv(InputStream ins) throws IOException {
                return ins;
            }
        });
        //声明输出流
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //声明缓冲区
        byte[] buffer = new byte[1024];
        //定义每次读取的数据长度
        int lengh = 0;
        //输入流转输出流
        while ((lengh = inputStream.read(buffer)) != -1){
            //输出流写入
            byteArrayOutputStream.write(buffer, 0, lengh);
        }
        //返回字节码
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 文件删除
     */
    public void delete(String groupName, String path){
        fastFileStorageClient.deleteFile(groupName, path);
    }
}
