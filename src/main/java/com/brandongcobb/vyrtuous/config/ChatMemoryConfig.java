//
//  ChatMemoryConfig.swift
//  
//
//  Created by Brandon Cobb on 7/2/25.
//
package com.brandongcobb.vyrtuous.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.chat.memory.MessageWindowChatMemory;


@Configuration
public class ChatMemoryConfig {
    
    private static ChatMemory replChatMemory = MessageWindowChatMemory.builder().build();
    
    @Bean
    public ChatMemory replChatMemory() {
        return replChatMemory;
    }
}
