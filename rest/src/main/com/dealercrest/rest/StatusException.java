package com.dealercrest.rest;

public class StatusException extends RuntimeException {

  private final int code;

  public StatusException(int code){
    this(code, "internal error happens");
  }

  public StatusException(int code, String message) {
    super(message);
    this.code = code;
  }

  public StatusException(int code, String message, Throwable t){
    super(message,t);
    this.code= code;
  }

  public int getCode(){
    return code;
  }

}
