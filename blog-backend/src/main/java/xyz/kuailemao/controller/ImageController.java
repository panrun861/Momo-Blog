package xyz.kuailemao.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String mainBucketName;

    /**
     * 前端访问图片的中转接口
     * @param objectName MinIO 桶内的文件路径（比如 article/articleImage/xxx.jpg）
     * @param response 响应对象，用于返回图片流
     */
    @GetMapping("/{objectName:.+}")
    public void getImage(@PathVariable String objectName, HttpServletResponse response) {
        try {
            // 1. （可选）权限校验：比如仅登录用户可访问
            // Long userId = SecurityUtils.getUserId();
            // if (userId == null) {
            //     response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            //     return;
            // }

            // 2. 从 MinIO 读取文件流
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(mainBucketName)
                            .object(objectName)
                            .build()
            );

            // 3. 设置响应头，告诉前端这是图片
            response.setContentType("image/jpeg"); // 可根据文件后缀动态设置
            response.setHeader("Cache-Control", "max-age=86400"); // 浏览器缓存 1 天

            // 4. 将文件流写入响应
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}