/*
 * Created by Roy Oct 14, 2014 2:28:26 PM.                          
 * Copyright (c) 2000-2014 AnXunBen. All rights reserved. 
 */
package com.isprint.cnaac.server.domain.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JsonResult implements Serializable {

    private static final long serialVersionUID = 6071196029362896442L;

    private String errorCode;
    
    private String message;

    private Map<Object, Object> item = new HashMap<Object, Object>();
    
    public JsonResult(){
        
    }

    public String getErrorCode() {
            return errorCode;
    }

    public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
    }

    public String getMessage() {
            return message;
    }

    public void setMessage(String message) {
            this.message = message;
    }

    public Map<Object, Object> getItem() {
            return item;
    }

    public void setItem(Map<Object, Object> item) {
            this.item = item;
    }

}
