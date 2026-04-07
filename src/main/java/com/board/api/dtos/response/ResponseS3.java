package com.board.api.dtos.response;

import io.awspring.cloud.s3.Location;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ResponseS3 {
  private String fileName;
  private String path;
  private String url;
  private Location location;
}
