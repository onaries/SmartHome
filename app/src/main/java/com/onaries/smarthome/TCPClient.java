package com.onaries.smarthome;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by SW on 2015-11-16.
 */
public class TCPClient extends AsyncTask <Object, Void, String> {

    protected static String SERV_IP; //server ip
    protected static int PORT;
    private boolean dataReady = false;
    private char sendData;
    public String recvData = null;
    Context context;

    public TCPClient(String addr, int port, char sData, Context context) {
        SERV_IP = addr;
        PORT = port;
        sendData = sData;
        dataReady = false;
        this.context = context;
    }

    @Override
    protected String doInBackground(Object... params) {
        // TODO Auto-generated method stub

        try {
            Log.d("TCP", "server connecting");
            InetAddress serverAddr = InetAddress.getByName(SERV_IP);
            Socket sock = new Socket(serverAddr, PORT);

            DataInputStream input = new DataInputStream(sock.getInputStream());
            DataOutputStream output = new DataOutputStream(sock.getOutputStream());


            try {
                //	데이터 송신 부분!
                WriteSocket(output, sendData);
                recvData = input.readLine();

                Log.d("TCP", recvData);

            } catch (IOException e) {
                Log.e("TCP", "don't send message!");
                e.printStackTrace();
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            Toast.makeText(context, R.string.server_no_connect, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return recvData;
    }

    public void WriteSocket(DataOutputStream data, char sData) throws IOException {
        //	data send
        data.write(sData);
    }
}
