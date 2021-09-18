package com.my.fmall.manage.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * author:zxy
 *
 * @create 2021-09-15 22:34
 */
@RestController
@CrossOrigin
@Slf4j
public class FileUploadController {


    @Value("${fileServer.url}")
    String fileUrl;

    @RequestMapping(value = "fileUpload",method = RequestMethod.POST)
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        String imgUrl=fileUrl;
        if(file!=null){
            System.out.println("multipartFile = " + file.getName()+"|"+file.getSize());
            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getTrackerServer();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            String filename= file.getOriginalFilename();
            String extName = StringUtils.substringAfterLast(filename, ".");

//            String[] upload_file = storageClient.upload_file(file.getOriginalFilename(), extName, null); //获取本地文件
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            imgUrl=fileUrl ;
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                 /*
                s = group1
                s = M00/00/00/wKhD2106tuSAY9S9AACGx2c4tJ4084.jpg
                 */
                log.info("path:",path);
                imgUrl+="/"+path;

            }

        }
//        http://192.168.91.128/group1/M00/00/00/wKhbgGFCA7yAfhlcAALiizx_HqM430.jpg
        log.info("imgUrl:",imgUrl);
        return imgUrl;
    }
}
