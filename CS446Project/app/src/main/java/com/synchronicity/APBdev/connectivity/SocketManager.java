package com.synchronicity.APBdev.connectivity;


        import android.os.Parcelable;

        import java.net.ServerSocket;

public interface SocketManager {

    interface ActionListener {
        void onReceive();
    }

    void sendData(Parcelable parcelable);
    void sendDataByPath(String pathToData);
    void sendSignal(int signal);
    void setOnReceiveDataListener(SocketManager.ActionListener actionListener);
    ServerSocket getServerSocket();
    void cleanUp();


}
