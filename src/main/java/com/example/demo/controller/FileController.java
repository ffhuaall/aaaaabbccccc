package com.example.demo.controller;

import com.example.demo.common.Result;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/file")
public class FileController {

    //存图片的文件路径
    private static final String UPLOAD_DIR = "D:/Hunkoufanchi/springboot/finalDegien/demo1/demo/picture/";

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "上传失败，文件不能为空");
        }

        try {
            //获取原文件名 (比如: my_photo.jpg)
            String originalFilename = file.getOriginalFilename();
            
            //提取文件后缀名 (比如: .jpg)
            String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            //生成的新文件名 (比如: 3f8a9b2c-xxxx.jpg)
            String newFileName = UUID.randomUUID().toString() + extName;

            //构建最终要保存的绝对路径文件对象
            File destFile = new File(UPLOAD_DIR + newFileName);

            //判断父目录是否存在，如果被删了会自动再建一个
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }

            //执行核心保存逻辑：将内存中的文件流写入到磁盘中
            file.transferTo(destFile);

            //返回可以在浏览器中直接访问的图片URL给前端
            String fileUrl = "http://localhost:8080/picture/" + newFileName;
            
            return Result.success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传发生异常！");
        }
    }
}