package com.lrpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyCompletableFuture {
    public static void main(String[] args) {
//        获取子线程的结果，并返回给主线程
        CompletableFuture<Integer> integerCompletableFuture = new CompletableFuture<>();

        new Thread(() -> {

            int i = 8;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            integerCompletableFuture.complete(8);
        }).start();
        Integer i = null;
        try {
            i = integerCompletableFuture.get(4, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        System.out.println(i);
    }

}
