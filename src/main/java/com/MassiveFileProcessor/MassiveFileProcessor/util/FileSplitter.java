package com.MassiveFileProcessor.MassiveFileProcessor.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FileSplitter {

    /**
     * Divide un archivo en chunks basados en líneas completas
     * 
     * @param file   Archivo a dividir
     * @param chunks Número de chunks deseados
     * @return Lista de rangos [startByte, endByte] para cada chunk
     */
    public List<long[]> splitFile(File file, int chunks) throws IOException {
        List<long[]> chunksPositions = new ArrayList<>();
        long fileSize = file.length();

        if (fileSize == 0) {
            throw new IOException("Archivo vacío");
        }

        long chunkSize = fileSize / chunks;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long start = 0;

            for (int i = 0; i < chunks; i++) {
                long end = (i == chunks - 1) ? fileSize : start + chunkSize;

                if (end < fileSize) {
                    // Ajustar al inicio de la siguiente línea
                    raf.seek(end);
                    while (raf.getFilePointer() < fileSize) {
                        int b = raf.read();
                        if (b == '\n') {
                            end = raf.getFilePointer();
                            break;
                        }
                        if (b == -1) {
                            end = fileSize;
                            break;
                        }
                    }
                }

                chunksPositions.add(new long[] { start, end });
                start = end;
            }
        }

        log.info("Archivo dividido en {} chunks", chunksPositions.size());
        return chunksPositions;
    }

}
