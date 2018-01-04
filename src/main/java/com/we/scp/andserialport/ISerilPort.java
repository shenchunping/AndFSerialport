package com.we.scp.andserialport;

import java.io.IOException;

/**
 * Created by admin on 2016/9/5.
 */
public interface ISerilPort {

    /**
     * 开启读写
     */
    void open() throws IOException;

    /**
     * 关闭读写
     */
    void close();


    /**
     * 写出数据
     *
     * @param data
     */
    void write(byte[] data);

    /**
     * 监听读取数据
     * @param onReadListener
     */
    void setReadListener(SerialPortImpl.OnReadListener onReadListener);

}
