package com.board.api.services.common;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.board.api.common.base.BusinessException;
import com.board.api.dtos.response.ResponseS3;
import io.awspring.cloud.s3.ObjectMetadata;
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

  // 허용 확장자 → 저장 시 Content-Type. SVG·HTML 등 브라우저에서 스크립트가 실행될 수 있는 형식은 제외합니다.
  private static final Map<String, String> IMAGE_CONTENT_TYPES =
      Map.of("jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "gif", "image/gif",
          "webp", "image/webp");
  private static final Pattern KEY_PATTERN = Pattern.compile("(\\d{8}/[^?#\\s]+)");

  /**
   * 이미지 파일인지 확인하고, S3에 저장할 Content-Type을 반환합니다.
   * Content-Type은 클라이언트 값이 아닌 허용 확장자 기준으로 정해 HTML 등으로 서빙되지 않게 합니다.
   */
  private String resolveImageContentType(final MultipartFile file) {
    final String extension = Objects
        .requireNonNullElse(FilenameUtils.getExtension(file.getOriginalFilename()), "")
        .toLowerCase(Locale.ROOT);
    final String contentType = IMAGE_CONTENT_TYPES.get(extension);
    final String requestType = Objects.requireNonNullElse(file.getContentType(), "");

    if (contentType == null || !requestType.toLowerCase(Locale.ROOT).startsWith("image/")) {
      throw new BusinessException("이미지 파일(jpg, png, gif, webp)만 업로드할 수 있습니다.",
          HttpStatus.BAD_REQUEST);
    }

    return contentType;
  }

  private S3Resource upload(final String key, final MultipartFile file, final String contentType)
      throws IOException {
    return s3Template.upload(IMAGE_BUCKET, key, file.getInputStream(),
        ObjectMetadata.builder().contentType(contentType).build());
  }

  private ResponseS3 baseUploadImage(final MultipartFile file, final String contentType)
      throws IOException {
    final String now =
        LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    final String uuid = UUID.randomUUID().toString();
    final String key = now.concat("/").concat(uuid);
    final S3Resource resource = upload(key, file, contentType);

    return ResponseS3.builder()//
        .fileName(uuid)//
        .path(resource.getURI().getPath())//
        .url(resource.getURL().toString())//
        .location(resource.getLocation())//
        .build();
  }

  /**
   * 이미지 업로드. 이미지 형식이 아니면 BusinessException, 업로드 실패 시 null을 반환합니다.
   */
  public ResponseS3 uploadImage(final MultipartFile file) {
    final String contentType = resolveImageContentType(file);
    try {
      return baseUploadImage(file, contentType);
    } catch (Exception e) {
      log.error("s3 upload error : ", e);
    }

    return null;
  }

  public ResponseS3 uploadImage(final MultipartFile file, final boolean useUUID) {
    final String contentType = resolveImageContentType(file);
    try {
      if (useUUID) {
        return baseUploadImage(file, contentType);
      }

      final String now =
          LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
      final String fileName = file.getOriginalFilename();
      final String key = now.concat("/").concat(fileName);
      final S3Resource resource = upload(key, file, contentType);

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

  public boolean deleteImage(final String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return false;
    }

    try {
      final String key = extractKey(fileUrl);
      if (key != null && !key.isBlank()) {
        s3Template.deleteObject(IMAGE_BUCKET, key);
        log.info("s3 delete success for key: {}", key);
        return true;
      }
    } catch (Exception e) {
      log.error("s3 delete error for url {}: ", fileUrl, e);
    }

    return false;
  }

  /**
   * 이미지 URL에서 S3 key(yyyyMMdd/uuid 또는 yyyyMMdd/filename)를 추출합니다. 없으면 null.
   */
  public String extractKey(final String fileUrl) {
    if (fileUrl == null) {
      return null;
    }
    final Matcher matcher = KEY_PATTERN.matcher(fileUrl);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

}
