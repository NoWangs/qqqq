package com.leyou.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.config.OSSProperties;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UploadService {
    //支持的文件类型
    private List<String> suffixes= Arrays.asList("image/png", "image/jpeg", "image/bmp");
    public String upload(MultipartFile file) {
        //校验文件类型是否属于图片
        String type = file.getContentType();//拿到文件的类型
        if (!suffixes.contains(type)){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //判断图片内容
        try {
            BufferedImage read = ImageIO.read(file.getInputStream());
            if (read==null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //上传是是否异常
        //图片上传到当前服务器
        String filename = UUID.randomUUID().toString() + file.getOriginalFilename();
        try {
            file.transferTo(new File("E:\\java\\nginx\\nginx-1.16.0\\html\\"+filename));
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }

        return "http://image.leyou.com/"+filename;
    }


    @Autowired
    private OSSProperties ossProperties;

    @Autowired
    private OSS client;

    public Map<String, String> getSignature() {
        try {
            long expireTime = ossProperties.getExpireTime(); //超时时间
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, ossProperties.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessId", ossProperties.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", ossProperties.getDir());
            respMap.put("host", ossProperties.getHost());
            respMap.put("expire", String.valueOf(expireEndTime));
            // respMap.put("expire", formatISO8601Date(expiration));
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
    }

