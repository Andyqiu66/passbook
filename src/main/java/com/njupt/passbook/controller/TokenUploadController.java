package com.njupt.passbook.controller;

import com.njupt.passbook.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h1>PassTemplate Token Upload</h1>
 * */
@Slf4j
@Controller
public class TokenUploadController {

    /** redis 客户端 */
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public TokenUploadController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/upload")
    public String upload(){
        return "upload";
    }

    @PostMapping("/token")
    public String tokenFileUpload(@RequestParam("merchantsId") String merchantsId,
                                  @RequestParam("passTemplateId") String passTemplateId,
                                  @RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirectAttributes){
        if (null==passTemplateId||file.isEmpty()){
            redirectAttributes.addFlashAttribute("message","passTemplateId is null or file is empty");
            return "redirect:/uploadStatus";
        }

        try{
            File cur= new File(Constants.TOKEN_DIR+merchantsId);//获取用户上传的文件
            if (!cur.exists()){
                log.info("Create File:{}",cur.mkdir());
            }
            Path path= Paths.get(Constants.TOKEN_DIR,merchantsId,passTemplateId);
            Files.write(path,file.getBytes());

            if (!writeTokenToRedis(path,passTemplateId)){
                redirectAttributes.addFlashAttribute("message",
                        "write token error");
            }else{
                redirectAttributes.addFlashAttribute("message",
                        "You successfully uploaded '"+file.getOriginalFilename()+"'");
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }

        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus(){
        return "uploadStatus";
    }


    /**
     * <h2>将 token 写入 redis</h2>
     * @param path {@link Path}
     * @param key redis key
     * @return true/false
     * */
    private boolean writeTokenToRedis(Path path,String key){

        Set<String> tokens;//保证用户分配到的token不会是重复的，如果商户上传的token出现重复的话需要跟商户去报错，告诉商户token重复

        try(Stream<String> stream = Files.lines(path)){
            tokens=stream.collect(Collectors.toSet());
        } catch (IOException ex){
            ex.printStackTrace();
            return false;
        }

        //单机的Redis支持Pipelined操作，如果是集群的话，Sprintboot RedisTemplate是不支持Redis的pipelined操作的
        if (!CollectionUtils.isEmpty(tokens)){
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for(String token:tokens){
                    connection.sAdd(key.getBytes(),token.getBytes());
                }
                return null;
            });
            return true;
        }
        return false;
    }
}
