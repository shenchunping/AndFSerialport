package com.we.scp.andserialport;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * Android 串口读写实现
 * Created by asiayak on 2016/7/6.
 */
public class SerialPortImpl implements ISerilPort {

    protected SerialPort mSerialPort; //JNI读写接口
    protected OutputStream mOutputStream; //输入流
    protected InputStream mInputStream;//输出流


    private ReadThread mReadThread;
    private OnReadListener onReadListener;
    private int bufferSize = 256;


    private String driver;
    private int baudrate;
    private boolean run;


    public SerialPortImpl(String driver, int baudrate) {
        this.driver = driver;
        this.baudrate = baudrate;
    }

    /**
     * @param path     驱动文件
     * @param baudrate 波特率
     * @return
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    public SerialPort getSerialPort(String path, int baudrate) throws SecurityException, IOException,
            InvalidParameterException {
        if (TextUtils.isEmpty(path) || baudrate == -1) {
            throw new InvalidParameterException();
        }
        mSerialPort = new SerialPort(new File(path), baudrate, 0);
        return mSerialPort;

    }

    @Override
    public void open() throws IOException {
        if (mSerialPort == null) {
            mSerialPort = getSerialPort(driver, baudrate);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mReadThread = new ReadThread();
            mReadThread.start();
            run = true;
        }
    }

    /**
     * 关闭串口
     */
    public void close() {
        try {
            if (mReadThread != null) {
                mReadThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        mSerialPort = null;
        run = false;
    }

    @Override
    public void write(byte[] data) {
        try {
            mOutputStream.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setReadListener(OnReadListener onReadListener) {
        this.onReadListener = onReadListener;
    }

    /**
     * 读取数据线程
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            while (!isInterrupted() && mInputStream != null) {
                run = true;
                int size;
                try {
                    byte[] buffer = new byte[bufferSize];
                    if (mInputStream == null) {
                        return;
                    }
                    size = mInputStream.read(buffer, 0, buffer.length);
                    if (size > 0 && onReadListener != null) {
                        onReadListener.onRead(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            close();
        }
    }

    public String getDriver() {
        return driver;
    }

    public boolean isRun() {
        return run;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * 串口数据读写
     * Created by admin on 2016/9/5.
     */
    public interface OnReadListener {

        /**
         * 接收到写入数据
         *
         * @param buffer
         * @param size
         */
        void onRead(final byte[] buffer, final int size);
    }
}
