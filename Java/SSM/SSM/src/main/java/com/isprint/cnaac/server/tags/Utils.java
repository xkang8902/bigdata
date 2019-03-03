/*
 * Created by Roy Feb 10, 2015 3:17:07 PM.                          
 * Copyright (c) 2000-2015 AnXunBen. All rights reserved. 
 */

package com.isprint.cnaac.server.tags;

class Utils {
    
    public static String BLOCK = "__yessafe__jsp__override__";
    
    static String getOverrideVariableName(String name) {
        return BLOCK + name;
    }
    
    
}