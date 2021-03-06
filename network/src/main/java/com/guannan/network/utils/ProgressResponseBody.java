package com.guannan.network.utils;

import com.guannan.network.bean.ProgressModel;
import com.guannan.network.callback.ResultCallback;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by guannan on 2017/7/21.
 */

public class ProgressResponseBody extends ResponseBody {

    private ResponseBody mResponseBody;
    private ResultCallback mResultCallback;
    private BufferedSource mBufferedSource;

    /**
     * 构造函数，赋值
     *
     * @param responseBody   待包装的响应体
     * @param resultCallback 回调接口
     */
    public ProgressResponseBody(ResponseBody responseBody, ResultCallback resultCallback) {

        this.mResponseBody = responseBody;
        this.mResultCallback = resultCallback;
    }

    /**
     * 重写调用实际的响应体的contentType
     *
     * @return
     */
    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return
     */
    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    /**
     * 重写进行包装source
     *
     * @return
     */
    @Override
    public BufferedSource source() {

        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }


    /**
     * 读取，回调进度接口
     *
     * @param source Source
     * @return Source
     */
    private Source source(Source source) {

        return new ForwardingSource(source) {
            //当前读取字节数
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                //回调，如果contentLength()不知道长度，会返回-1
                mResultCallback.onResponseProgress(new ProgressModel(totalBytesRead, contentLength(), bytesRead == -1));
                return bytesRead;
            }
        };
    }
}
