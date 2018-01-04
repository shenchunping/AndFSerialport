package com.we.scp.andserialport;

/**
 * Android 串口读写实现
 * Created by asiayak on 2016/7/6.
 */
public class SimpleSerialPortImpl extends SerialPortImpl implements SerialPortImpl.OnReadListener {

    private StringBuffer mBuffer = new StringBuffer();
    private int mLength = mBuffer.length();
    private Thread mThread;
    private OnReadListener mOnReadListener;

    public SimpleSerialPortImpl(String driver, int baudrate) {
        super(driver, baudrate);
    }

    @Override
    public void onRead(final byte[] buffer, final int size) {
        synchronized (mBuffer) {
            if (buffer.length > 0) {
                //拼装帧数据
                mBuffer.append(new String(buffer));
            }
            if (mThread == null && mOnReadListener != null) {
                mThread = new ReadThread();
                mThread.start();
            }
        }

    }

    /**
     * 检测帧数据超时
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            try {
                //判断帧数据变化
                while (mBuffer.length() > 0 && mBuffer.length() > mLength) {
                    synchronized (mBuffer) {
                        mLength = mBuffer.length();
                    }
                    //延时5毫秒
                    Thread.sleep(5);
                }
                if (mBuffer.length() > 0 && mOnReadListener != null) {
                    final byte[] data = mBuffer.toString().getBytes();
                    mOnReadListener.onRead(data, data.length);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mLength = 0;
                if (mBuffer.length() > 0) {
                    mBuffer.delete(0, mBuffer.length() - 1);
                }
                mThread = null;
            }


        }
    }

    public OnReadListener getOnReadListener() {
        return mOnReadListener;
    }

    public void setOnReadListener(OnReadListener onReadListener) {
        mOnReadListener = onReadListener;
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
