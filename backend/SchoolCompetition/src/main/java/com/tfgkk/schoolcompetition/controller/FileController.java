package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Tag(name = "文件管理", description = "处理竞赛附件上传")
@RestController
@RequestMapping("/api/files")
@CrossOrigin
public class FileController {

    // 文件上传保存路径 (可以根据需要修改)
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @Operation(summary = "附件上传", description = "上传 PDF 格式的竞赛规则附件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 1. 确保上传目录存在
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 生成新文件名防止冲突 (UUID + 原始后缀)
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + suffix;

        // 3. 保存文件
        File dest = new File(uploadDir + fileName);
        try {
            file.transferTo(dest);
            // 返回文件的访问路径 (这里先返回文件名，后续通过配置静态资源映射来访问)
            return Result.success("/uploads/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件保存失败");
        }
    }
}
