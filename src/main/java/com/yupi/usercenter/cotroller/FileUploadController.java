package com.yupi.usercenter.cotroller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.common.Result;
import com.yupi.usercenter.model.domain.CaptchaVo;
import com.yupi.usercenter.model.SaveMinioRequest;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.FileService;
import com.yupi.usercenter.service.UserService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@RequestMapping("/admin")
@RestController
//@CrossOrigin
//@CrossOrigin(origins = {"http://localhost:3000"},allowCredentials = "true")
public class FileUploadController {

    @Autowired
    private FileService service;


    /**
     * 上传图片到minio
     *
     * @param file
     * @return
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws ErrorResponseException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidResponseException
     * @throws XmlParserException
     * @throws InternalException
     */
    @PostMapping("/upload")
    public BaseResponse<String> upload(@RequestParam MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String url = service.upload(file);
        return Result.ok(url);
    }

    @GetMapping("/login/captcha")
    public BaseResponse<CaptchaVo> getCaptcha() {
        CaptchaVo captcha = service.getCaptcha();
        return Result.ok(captcha);
    }

    @Resource
    private UserService userService;

    /**
     * 根据前端传来的id和url保存到数据库中
     *
     * @param saveMinioRequest
     * @return
     */
    @PostMapping("/save/picture/mysql")
    public BaseResponse<Boolean> savePictureToMysql(@RequestBody SaveMinioRequest saveMinioRequest) {
        if (saveMinioRequest == null) {
            throw new RuntimeException("url为空");
        }
        User user = userService.getById(saveMinioRequest.getId());
        user.setAvatarUrl(saveMinioRequest.getUrl());
        boolean b = userService.updateById(user);
        return Result.ok(b);
    }
}