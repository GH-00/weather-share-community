package com.springproject.weathersharecommunity.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.springproject.weathersharecommunity.domain.Image;
import com.springproject.weathersharecommunity.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class S3FileUploadService {
    private final UploadService s3Service;

    @Autowired
    ImageRepository imageRepository;

    //Multipart를 통해 전송된 파일을 업로드 하는 메소드
    public List<Image> uploadImage(List<MultipartFile> files){
        //반환값
        List<Image> imageList = new ArrayList<>();

        for(MultipartFile multipartFile : files) {

//            String originFileExtension;
//            String contentType = multipartFile.getContentType();
//
//            if(ObjectUtils.isEmpty(contentType)){
//                break;
//            }
//            else{
//                if(contentType.contains("image/jpeg"))
//                    originFileExtension = ".jpg";
//                else if(contentType.contains("image/png"))
//                    originFileExtension = ".png";
//                else
//                    break;
//            }
//
//            String new_file_name = System.nanoTime() + originFileExtension;

            String fileName = createFileName(multipartFile.getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(multipartFile.getSize());
            objectMetadata.setContentType(multipartFile.getContentType());
            try (InputStream inputStream = multipartFile.getInputStream()) {
                s3Service.uploadFile(inputStream, objectMetadata, fileName);
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format("파일 변환 중 오류 발생 ($s)", multipartFile.getOriginalFilename()));
            }

            Image image = new Image(fileName, s3Service.getFileUrl(fileName), multipartFile.getSize());
            imageRepository.save(image);
            imageList.add(image);
//            return s3Service.getFileUrl(fileName);
        }

        return imageList;
    }

    //기존 확장자명 유지한 채로 유니크한 파일 이름을 생성하는 로직
    private String createFileName(String originalFileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(originalFileName));
    }

    private String getFileExtension(String fileName) {
        try{
            return fileName.substring(fileName.lastIndexOf("."));
        } catch(StringIndexOutOfBoundsException e){
            throw new IllegalArgumentException(String.format("잘못된 형식의 파일 (%s) 입니다.", fileName));
        }
    }
}
