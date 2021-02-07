package com.tatjana.imageservice.exception;

import java.util.function.Supplier;

public class ImageException extends RuntimeException {

  public ImageException(String message, Object...o) { super(String.format(message, o));}

  public static Supplier<ImageException> supplier(String message, Object...args) {
    return () -> new ImageException(String.format(message, args));
  }

}
