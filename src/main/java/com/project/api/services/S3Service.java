package com.project.api.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.project.api.dtos.response.ResponseS3;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
  private final S3Template s3Template;
  private static final String IMAGE_BUCKET = "board-image";

  public ResponseS3 uploadImage(final MultipartFile file) throws IOException {
    final String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    final String uuid = UUID.randomUUID().toString();
    final String key = now.concat("/").concat(uuid);
    final S3Resource resource = s3Template.upload(IMAGE_BUCKET, key, file.getInputStream());

    return ResponseS3.builder()//
        .fileName(uuid)//
        .path(resource.getURI().getPath())//
        .url(resource.getURL().toString())//
        .location(resource.getLocation())//
        .build();
  }

  public ResponseS3 uploadImage(final MultipartFile file, final boolean notUseUUID)
      throws IOException {
    final String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    final String fileName = file.getOriginalFilename();
    if (notUseUUID) {
      final String key = now.concat("/").concat(fileName);
      final S3Resource resource = s3Template.upload(IMAGE_BUCKET, key, file.getInputStream());

      return ResponseS3.builder()//
          .fileName(fileName)//
          .path(resource.getURI().getPath())//
          .url(resource.getURL().toString())//
          .location(resource.getLocation())//
          .build();
    }


    return uploadImage(file);
  }

  public S3Resource downloadImage(final String key) {
    return s3Template.download(IMAGE_BUCKET, key);
  }

}
