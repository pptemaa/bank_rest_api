package com.example.bankcards.exception;

public class TransferException extends RuntimeException{
    public TransferException(String massage){
        super(massage);
    }
}
