package org.season.ymir.common.register;

/**
 * TODO
 *
 * @author KevinClair
 */
public interface ServiceRegister {

    void registerBean(final ServiceBean bean) throws Exception;

    ServiceBean getBean(final String name) throws Exception;
}
