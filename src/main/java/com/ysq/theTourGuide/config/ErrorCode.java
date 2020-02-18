package com.ysq.theTourGuide.config;

public enum ErrorCode {
    INVALID_PARAMETERS("202","未接受到正确的参数或未接受到参数"),
    UNKNOWERROR("203","未知错误"),
    NOEXIST("204","你操作的实体不存在"),
    ISEXIST("205","已存在")
    ;
    private String code;
    private String msg;
    private ErrorCode(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
