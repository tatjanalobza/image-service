package com.tatjana.imageservice.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties("configuration")
@NoArgsConstructor
@AllArgsConstructor
public class ImageConfiguration {

  private Map<ImageType, Config> images;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Config {
    private int height;
    private int width;
    private int quality;
    private ScaleType scaleType;
    private long fillColor;
    private Type type;
    private String sourceName;

    public enum ScaleType {
      CROP, FILL, SKEW;
    }
    public enum Type {
      JPG, PNG;
    }
  }
  public enum ImageType {
    THUMBNAIL, DETAIL_LARGE;
  }

}
