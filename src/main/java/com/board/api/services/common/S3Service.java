package com.board.api.services.common;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.board.api.dtos.response.ResponseS3;
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

  private ResponseS3 baseUploadImage(final MultipartFile file) throws IOException {
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

  public ResponseS3 uploadImage(final MultipartFile file) {
    try {
      return baseUploadImage(file);
    } catch (Exception e) {
      log.error("s3 upload error : ", e);
    }

    return null;
  }

  public ResponseS3 uploadImage(final MultipartFile file, final boolean useUUID) {
    try {
      if (useUUID) {
        return baseUploadImage(file);
      }

      final String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
      final String fileName = file.getOriginalFilename();
      final String key = now.concat("/").concat(fileName);
      final S3Resource resource = s3Template.upload(IMAGE_BUCKET, key, file.getInputStream());

      return ResponseS3.builder()//
          .fileName(fileName)//
          .path(resource.getURI().getPath())//
          .url(resource.getURL().toString())//
          .location(resource.getLocation())//
          .build();
    } catch (Exception e) {
      log.error("s3 upload error : ", e);
    }

    return null;
  }

  public S3Resource downloadImage(final String key) {
    return s3Template.download(IMAGE_BUCKET, key);
  }

}
