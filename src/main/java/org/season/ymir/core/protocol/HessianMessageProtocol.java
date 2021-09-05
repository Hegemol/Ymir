package org.season.ymir.core.protocol;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.season.ymir.common.model.InvocationMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * HessianMessageProtocol.
 *
 * @author KevinClair
 **/
public class HessianMessageProtocol implements MessageProtocol {

    @Override
    public byte[] serialize(final Object object) throws Exception {
        Hessian2Output hessianOutput = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
            hessianOutput = new Hessian2Output(outputStream);
            hessianOutput.writeObject(object);
            hessianOutput.flush();
            return outputStream.toByteArray();
        } finally {
            if (Objects.nonNull(hessianOutput)) {
                hessianOutput.close();
            }
        }
    }

    @Override
    public InvocationMessage deserialize(final byte[] data) throws Exception {
        Hessian2Input hessian2Input = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);) {
            hessian2Input = new Hessian2Input(inputStream);
            return (InvocationMessage) hessian2Input.readObject();
        } finally {
            if (Objects.nonNull(hessian2Input)){
                hessian2Input.close();
            }
        }
    }
}
