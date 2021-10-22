package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.fastdfs.FastDfsClient;
import com.atguigu.gmall.product.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传的控制层
 */
@RestController
@RequestMapping(value = "/admin/product")
public class FileController {

    //文件访问的前缀地址
    @Value("${fileServer.url}")
    private String url;

    @Autowired
    private FastDfsClient fastDfsClient;

    /**
     * 文件的上传
     * @param file
     * @return
     */
    @PostMapping(value = "/fileUpload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception{
        return url + fastDfsClient.upload(file);
    }
}
