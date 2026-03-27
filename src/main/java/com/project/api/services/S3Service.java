package com.project.api.services;

import java.io.InputStream;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
  private final S3Template s3Template;
  private static final String IMAGE_BUCKET = "board-image";

  public void uploadImage(final String key, final InputStream inputStream) {
    final String refineKey = key.startsWith("/") ? key.substring(1) : key;
    s3Template.upload(IMAGE_BUCKET, refineKey, inputStream);
  }

  public Resource downloadImage(final String key) {
    return s3Template.download(IMAGE_BUCKET, key);
  }

}
