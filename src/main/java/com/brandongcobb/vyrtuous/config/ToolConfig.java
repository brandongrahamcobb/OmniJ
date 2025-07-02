//
//  ToolConfig.swift
//  
//
//  Created by Brandon Cobb on 7/2/25.
//
package com.brandongcobb.vyrtuous.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import java.util.List;

@Configuration
public class ToolConfig {

    @Bean
    public List<ToolCallback> toolCallbacks(ToolService toolService) {
        return List.of(ToolCallbacks.from(toolService));
    }
}
