//
//  LoadContextStatus.swift
//  
//
//  Created by Brandon Cobb on 6/25/25.
//


package com.brandongcobb.vyrtuous.domain;

public class LoadContextStatus {

    private final boolean success;
    private final String message;

    public LoadContextStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
