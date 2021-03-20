package org.season.ymir.common.base;

/**
 * 服务状态枚举
 *
 * @author KevinClair
 */
public enum ServiceStatusEnum {

    /**
     * SUCCESS
     */
    SUCCESS(200, "SUCCESS"),
    /**
     * ERROR
     */
    ERROR(500, "ERROR"),
    /**
     * NOT FOUND
     */
    NOT_FOUND(404, "NOT FOUND");

    private int code;

    private String desc;

    ServiceStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
