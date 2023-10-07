package org.lrpc.core.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lrpc.core.compress.impl.GzipCompressor;
import org.lrpc.core.serializer.Serializer;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompressorWrapper {

    private byte code;
    private String type;
    private Compressor compressor;


}
