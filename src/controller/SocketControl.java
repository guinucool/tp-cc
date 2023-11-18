package controller;

import java.io.*;
import java.util.*;

import files.*;
import message.*;
import socket.*;

public class SocketControl {
    
    private SocketRunner runner;

    /* Constructors */
    public SocketControl(SocketRunner runner) {
        this.runner = runner;
    }

    /* Control */
    public void sendRegister(int port) throws IOException, SocketControlException {

        TrackMessage msg = new TrackMessage(TrackMessage.Type.REQUEST, 100, Arrays.asList("" + port));
        this.runner.sendMessage(msg);

        TrackMessage res = this.runner.listenMessage();

        if (res.getCode() != 0 || res.getType() != TrackMessage.Type.RESPONSE)
            throw new SocketControlException.RunnerCloseConnectionException("" + res.getCode());
    }

    public void sendFile(NodeFile file) throws IOException, SocketControlException {

        TrackMessage msg = file.toMessage();
        this.runner.sendMessage(msg);

        TrackMessage res = this.runner.listenMessage();

        if (res.getCode() == 201 && res.getType() == TrackMessage.Type.RESPONSE)
            throw new SocketControlException.RepeatedFilenameException(res.getArguments().get(0));

        if (res.getCode() == 202 && res.getType() == TrackMessage.Type.RESPONSE)
            throw new SocketControlException.FileRefusedException(res.getArguments().get(0));

        if (res.getCode() != 0 || res.getType() != TrackMessage.Type.RESPONSE)
            throw new SocketControlException.RunnerCloseConnectionException("" + res.getCode());
    }

    public void sendDisconnect() throws IOException, SocketControlException {

        TrackMessage msg = new TrackMessage(TrackMessage.Type.REQUEST, 101, Arrays.asList());
        this.runner.sendMessage(msg);

        TrackMessage res = this.runner.listenMessage();

        if (res.getCode() != 0 || res.getType() != TrackMessage.Type.RESPONSE)
            throw new SocketControlException.RunnerCloseConnectionException("" + res.getCode());
    }

    public void close() {
        this.runner.close();
    }
}
