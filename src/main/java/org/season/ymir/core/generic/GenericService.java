package org.season.ymir.core.generic;

/**
 * 泛化调用实现
 *
 * @author KevinClair
 **/
public interface GenericService {

    /**
     * 泛化调用实现
     *
     * @param interfaceName  接口名
     * @param method         方法名
     * @param parameterTypes 参数类型
     * @param args           请求参数
     * @return 返回参数，object类型;
     */
    Object invoke(String interfaceName, String method, String[] parameterTypes, Object[] args);
}
