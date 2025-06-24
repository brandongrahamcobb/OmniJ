//
//  Tool.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.tools;

interface Tool<I, O> {
    String getName();
    O run(I input) throws Exception;
}
