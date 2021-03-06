package com.dasset.wallet.core.script;

import com.dasset.wallet.core.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An element that is either an opcode or a raw byte array (signature, pubkey, etc).
 */
public class ScriptChunk {
    public final int    opcode;
    @Nullable
    public final byte[] data;
    private      int    startLocationInProgram;

    public ScriptChunk(int opcode, byte[] data) {
        this(opcode, data, -1);
    }

    public ScriptChunk(int opcode, byte[] data, int startLocationInProgram) {
        this.opcode = opcode;
        this.data = data;
        this.startLocationInProgram = startLocationInProgram;
    }

    public boolean equalsOpCode(int opcode) {
        return opcode == this.opcode;
    }

    /**
     * If this chunk is a single byte of non-pushdata content (could be ScriptOpCodes.OP_RESERVED or some invalid Opcode)
     */
    public boolean isOpCode() {
        return opcode > ScriptOpCodes.OP_PUSHDATA4;
    }

    /**
     * Returns true if this chunk is pushdata content, including the single-byte pushdatas.
     */
    public boolean isPushData() {
        return opcode <= ScriptOpCodes.OP_16;
    }

    public int getStartLocationInProgram() {
        checkState(startLocationInProgram >= 0);
        return startLocationInProgram;
    }

    /**
     * Called on a pushdata chunk, returns true if it uses the smallest possible way (according to BIP62) to push the data.
     */
    public boolean isShortestPossiblePushData() {
        checkState(isPushData());
        if (data.length == 0) {
            return opcode == ScriptOpCodes.OP_0;
        }
        if (data.length == 1) {
            byte b = data[0];
            if (b >= 0x01 && b <= 0x10) {
                return opcode == ScriptOpCodes.OP_1 + b - 1;
            }
            if (b == 0x81) {
                return opcode == ScriptOpCodes.OP_1NEGATE;
            }
        }
        if (data.length < ScriptOpCodes.OP_PUSHDATA1) {
            return opcode == data.length;
        }
        if (data.length < 256) {
            return opcode == ScriptOpCodes.OP_PUSHDATA1;
        }
        if (data.length < 65536) {
            return opcode == ScriptOpCodes.OP_PUSHDATA2;
        }

        // can never be used, but implemented for completeness
        return opcode == ScriptOpCodes.OP_PUSHDATA4;
    }

    public void write(OutputStream stream) throws IOException {
        if (isOpCode()) {
            checkState(data == null);
            stream.write(opcode);
        } else if (data != null) {
            checkNotNull(data);
            if (opcode < ScriptOpCodes.OP_PUSHDATA1) {
                checkState(data.length == opcode);
                stream.write(opcode);
            } else if (opcode == ScriptOpCodes.OP_PUSHDATA1) {
                checkState(data.length <= 0xFF);
                stream.write(ScriptOpCodes.OP_PUSHDATA1);
                stream.write(data.length);
            } else if (opcode == ScriptOpCodes.OP_PUSHDATA2) {
                checkState(data.length <= 0xFFFF);
                stream.write(ScriptOpCodes.OP_PUSHDATA2);
                stream.write(0xFF & data.length);
                stream.write(0xFF & (data.length >> 8));
            } else if (opcode == ScriptOpCodes.OP_PUSHDATA4) {
                checkState(data.length <= Script.MAX_SCRIPT_ELEMENT_SIZE);
                stream.write(ScriptOpCodes.OP_PUSHDATA4);
                Utils.uint32ToByteStreamLE(data.length, stream);
            } else {
                throw new RuntimeException("Unimplemented");
            }
            stream.write(data);
        } else {
            stream.write(opcode); // smallNum
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (isOpCode()) {
            buf.append(ScriptOpCodes.getOpCodeName(opcode));
        } else if (data != null) {
            // Data chunk
            buf.append(ScriptOpCodes.getPushDataName(opcode));
            buf.append("[");
            buf.append(Utils.bytesToHexString(data));
            buf.append("]");
        } else {
            // Small num
            buf.append(Script.decodeFromOpN(opcode));
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScriptChunk other = (ScriptChunk) o;

        if (opcode != other.opcode) {
            return false;
        }
        if (startLocationInProgram != other.startLocationInProgram) {
            return false;
        }
        if (!Arrays.equals(data, other.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = opcode;
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        result = 31 * result + startLocationInProgram;
        return result;
    }
}
