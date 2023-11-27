package message.frame;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import message.Message;
import message.MessageException;

public class Frame extends Message {

    /* Construtor sem-argumento */
    public Frame(Operation operation, short flag) throws MessageException {
        super(operation, flag);
    }

    /* Construtor uni-argumento */
    public Frame(Operation operation, short flag, byte[] payload) throws MessageException {
        super(operation, flag, payload);
    }

    /* Construtor multi-argumento */
    public Frame(Operation operation, short flag, List<byte[]> payload) throws MessageException {
        super(operation, flag, payload);
    }

    /* Transformação do frame em binário */
    public byte[] toByte() {

        byte[] aaa = new byte[1000];

        DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(aaa));
    }
}
