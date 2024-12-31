package com.yupi.usercenter.service.impl;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.yupi.usercenter.model.domain.CaptchaVo;
import com.yupi.usercenter.model.domain.RedisConstant;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.FileService;
import com.yupi.usercenter.util.minio.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private MinioConfig properties;

    @Autowired
    private MinioClient client;

    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        boolean bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucketName()).build());
        if (!bucketExists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucketName()).build());
            client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(properties.getBucketName()).config(createBucketPolicyConfig(properties.getBucketName())).build());
        }

        String filename = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        client.putObject(PutObjectArgs.builder().
                bucket(properties.getBucketName()).
                object(filename).
                stream(file.getInputStream(), file.getSize(), -1).
                contentType(file.getContentType()).build());

        return String.join("/", properties.getEndpoint(), properties.getBucketName(), filename);


    }
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);

        String code = specCaptcha.text().toLowerCase();
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        String image = specCaptcha.toBase64();
        redisTemplate.opsForValue().set(key, code, RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC,TimeUnit.SECONDS);

        return new CaptchaVo(image, key);
    }

    private String createBucketPolicyConfig(String bucketName) {
        return "{\n" +
                "  \"Statement\" : [ {\n" +
                "    \"Action\" : \"s3:GetObject\",\n" +
                "    \"Effect\" : \"Allow\",\n" +
                "    \"Principal\" : \"*\",\n" +
                "    \"Resource\" : \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "  } ],\n" +
                "  \"Version\" : \"2012-10-17\"\n" +
                "}";
    }
    }


