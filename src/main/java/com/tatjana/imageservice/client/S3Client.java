package com.tatjana.imageservice.client;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.findify.s3mock.S3Mock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public final class S3Client {

  private static final String BUCKET_NAME = "testbucket";
  private final S3Mock API;

  public S3Client(){
    API = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
  }

  @PostConstruct
  private void startAPI() {
    API.start();
    log.info("S3 API has been started");
  }

  @PreDestroy
  public void stopApi() {
    API.shutdown();
    log.info("S3 API has been shut down");
  }

  private AmazonS3 getClient() {
    EndpointConfiguration endpoint = new EndpointConfiguration("http://localhost:8001", "us-west-2");
    AmazonS3 client = AmazonS3ClientBuilder
        .standard()
        .withPathStyleAccessEnabled(true)
        .withEndpointConfiguration(endpoint)
        .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
        .build();

    client.createBucket(BUCKET_NAME);
    client.putObject(BUCKET_NAME, "thumbnail/abcd/efghabcde.jpg", "abcde.jpg");
    client.putObject(BUCKET_NAME, "thumbnail/abc/ghteri.png", "ghteri.png");
    client.putObject(BUCKET_NAME, "detail-large/abcd/efgh/nbhfthj.jpg", "nbhfthj.jpg");
    client.putObject(BUCKET_NAME, "detail-large/abcd/contents.jpg", "contents.jpg");
    return client;
  }

  public void uploadImage(String destinationPath, File image) {
    PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, destinationPath, image);
    getClient().putObject(putObjectRequest);
  }

  public byte[] downloadImage(String destinationPath) {
    try (
        InputStream in = getClient().getObject(BUCKET_NAME, destinationPath).getObjectContent()
    ) {
      BufferedImage imageFromAWS = ImageIO.read(in);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(imageFromAWS, getClient().getObject(BUCKET_NAME, destinationPath).getObjectMetadata().getContentType(), baos );
      return baos.toByteArray();
    } catch (IOException e) {
      log.info("The requested source image does not exist");
      return new byte[0];
    }
  }

  public void deleteImage(String destinationPath) {
    getClient().deleteObject(BUCKET_NAME + "/abc", destinationPath);
  }

}
