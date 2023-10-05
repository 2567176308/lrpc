package org.lrpc.core.serializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerializerWrapper {
    private byte code;
    private String type;
    private Serializer serializer;
}
