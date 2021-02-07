package com.tatjana.imageservice.controller;

import com.tatjana.imageservice.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/image")
@RequiredArgsConstructor
public class ImageController {

  private final ImageService imageService;

  @GetMapping(value = {"/show/{imageType}/{seo}/", "/show/{imageType}/"})
  public byte[] getImage(@PathVariable("imageType") String imageType,
                         @PathVariable("seo") Optional<String> optionalSeo,
                         @RequestParam String reference) {
    return imageService.getImage(imageType, reference);
  }

  @DeleteMapping(value = "/flush/{imageType}")
  public void flushImage(@PathVariable("imageType") String imageType, @RequestParam String reference) {
    imageService.flushImage(imageType, reference);
  }

}
