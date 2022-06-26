package org.hegemol.ymir.common.base;

/**
 * 序列化类型
 *
 * @author KevinClair
 **/
public enum SerializationTypeEnum {

    /**
     * kryo序列化
     */
    KYRO((byte) 0x01, "kryo"),

    /**
     * protostuff序列化
     */
    PROTOSTUFF((byte) 0x02, "protostuff"),

    /**
     * hessian序列化
     */
    HESSIAN((byte) 0x03, "hessian");

    private final byte code;
    private final String name;

    SerializationTypeEnum(final byte code, final String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Gets the value of code.
     *
     * @return the value of code
     */
    public byte getCode() {
        return code;
    }

    /**
     * Gets the value of name.
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * 根据code查询序列化类型
     *
     * @param code code
     * @return 序列化类型
     */
    public static SerializationTypeEnum getType(byte code) {
        for (SerializationTypeEnum each : SerializationTypeEnum.values()) {
            if (each.getCode() == code) {
                return each;
            }
        }
        return null;
    }

    /**
     * 根据name查询序列化类型
     *
     * @param name name
     * @return 序列化类型
     */
    public static SerializationTypeEnum getType(String name) {
        for (SerializationTypeEnum each : SerializationTypeEnum.values()) {
            if (each.getName().equals(name)) {
                return each;
            }
        }
        return null;
    }
}
