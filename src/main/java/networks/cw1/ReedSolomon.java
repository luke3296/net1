package networks.cw1;

/**
 * Inspiration and algorithm taken from https://github.com/Backblaze/JavaReedSolomon
 *
 */


public class ReedSolomon {
    private final int partCount;
    private final int parityCount;
    private final int totalPartCount;
    private final Matrix matrix;

    private static final int DATA_PART = 4;
    private static final int PARITY_PART = 2;


    private final byte[][] parityRows;

    public ReedSolomon(int partCount, int parityCount) {
        if(256 < parityCount + partCount) {
            throw new IllegalArgumentException("too many parts - 256 max");
        }

        this.parityCount = parityCount;
        this.partCount = partCount;
        this.totalPartCount = partCount + parityCount;
        Matrix vandermonde = vandermonde(totalPartCount, partCount);
        this.matrix = vandermonde.times(vandermonde.submatrix(0, 0, partCount, partCount));
        parityRows = new byte[parityCount][];
        for(int i = 0; i < parityCount; i++) {
            parityRows[i] = matrix.getRow(partCount + i);
        }
    }

    public byte[][] encodePacketData(byte[] data) {
        final int storedSize = data.length + Integer.BYTES;
        final int partSize = (storedSize + DATA_PART - 1) / DATA_PART;

        byte[][] parts = new byte[totalPartCount][partSize];
        for(int i = 0; i < DATA_PART; i++) {
            System.arraycopy(data, i * partSize, parts[i], 0, partSize);
        }
        return parts;
    }


    private void checkBuffersAndSizes(byte [] [] parts, int offset, int byteCount) {
        // The number of buffers should be equal to the number of
        // data shards plus the number of parity shards.
        if (parts.length != totalPartCount) {
            throw new IllegalArgumentException("wrong number of parts: " + parts.length);
        }

        // All of the shard buffers should be the same length.
        int shardLength = parts[0].length;
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].length != shardLength) {
                throw new IllegalArgumentException("Parts are different sizes");
            }
        }

        // The offset and byteCount must be non-negative and fit in the buffers.
        if (offset < 0) {
            throw new IllegalArgumentException("offset is negative: " + offset);
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount is negative: " + byteCount);
        }
        if (shardLength < offset + byteCount) {
            throw new IllegalArgumentException("buffers to small: " + byteCount + offset);
        }
    }

    public static Matrix vandermonde(int rows, int cols) {
        Matrix result = new Matrix(rows, cols);
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                result.set(r, c, Galois.exp((byte)r, c));
            }
        }
        return result;
    }
}
