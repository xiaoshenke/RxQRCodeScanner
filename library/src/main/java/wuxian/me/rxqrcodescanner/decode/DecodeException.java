package wuxian.me.rxqrcodescanner.decode;

/**
 * Created by wuxian on 24/10/2016.
 */

public class DecodeException extends RuntimeException {
    String errMsg;

    public DecodeException() {
        this("unexpected error");
    }

    public DecodeException(String detailMessage) {
        super(detailMessage);

        errMsg = detailMessage;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
