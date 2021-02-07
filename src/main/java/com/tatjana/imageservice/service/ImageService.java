package com.tatjana.imageservice.service;

import com.tatjana.imageservice.client.S3Client;
import com.tatjana.imageservice.exception.ImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.tatjana.imageservice.configuration.ImageConfiguration.ImageType;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

  private final S3Client client;

  public byte[] getImage(String imageType, String reference) {
    if (!Arrays.stream(ImageType.values()).map(Enum::toString).collect(Collectors.toList()).contains(imageType)) {
      log.info("The predefined type {} does not exist", imageType);
      throw new ImageException("Specified type does not exist");
    } else {
      String decodedReference = decodeUrl(reference);
      String destinationPath = imageType + "/" + decodedReference;
      if(downloadImageFromS3(destinationPath).length == 0) {
        byte[] originalImage = downloadImageFromS3("original/" + decodedReference);
        //based on the image type, get configuration and perform resizing using the methods below (cropImage(), skewImage()).
        // Bellow assignment is only for compilation purposes
        byte[] modifiedImage = originalImage;

        uploadImageToS3(destinationPath, modifiedImage); //upload newly resized image to S3
        return originalImage; //return resized image
      } else {
        return downloadImageFromS3(destinationPath);
      }
    }
  }

  public void flushImage(String imageType, String reference) {
    String decodedReference = decodeUrl(reference);
    String destinationPath = imageType + "/" + decodedReference;
    if ("original".equalsIgnoreCase(imageType)) {
      //find all images with other prefixes and same original image name
    } else {
      client.deleteImage(destinationPath);
    }
  }

  private byte[] downloadImageFromS3(String destinationPath) {
    return client.downloadImage(destinationPath);
  }

  @Retryable (value = IOException.class, maxAttempts = 2, backoff = @Backoff(delay = 200))
  private void uploadImageToS3(String destinationPath, byte[] image) {
    try {
      File file = new File(destinationPath);
      FileUtils.writeByteArrayToFile(file, image);
      client.uploadImage(destinationPath, file);
    } catch (IOException e) {
      log.warn("There is a problem writing the new image to S3 storage");
    }
  }

  @Recover
  void recover(IOException e, String destinationPath, byte[] image) {
    log.error("Error uploading to S3 storage");
    throw new ImageException("There is a problem writing the new image to S3 storage");
  }

  private String decodeUrl (String encoded) {
    return URLDecoder.decode(encoded, UTF_8);
  }
  private BufferedImage cropImage(BufferedImage image, int x, int y, int width, int height) {
    return image.getSubimage(x, y, width, height);
  }

  private BufferedImage skewImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
    Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
    return outputImage;
  }

  //method for filling
}
