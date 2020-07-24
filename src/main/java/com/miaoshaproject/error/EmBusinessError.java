package com.miaoshaproject.error;

public enum EmBusinessError implements CommonError {
    PARAMETER_VALIDATION_ERROR(00001, "参数不合法"),
    USER_NOT_EXIST(10001,"用户不存在"),
    UNKNOWN_ERROR(00002, "除去BusinessException之外的错误")
    ;

    private int errCode;
    private String errMsg;

    EmBusinessError(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
