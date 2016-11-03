package wuxian.me.rxqrcodescanner;

/**
 * Created by wuxian on 3/11/2016.
 * <p>
 * QRCodeScanner state dfa.
 */

public class QRCodeScannerDFA {
    public static final int STATE_CAMERA_CLOSE_DECODE_CLOSE = 0;
    public static final int STATE_CAMERA_OPEN_DECODE_CLOSE = 1;
    public static final int STATE_CAMERA_OPEN_DECODE_OPEN = 2;
    public static final int STATE_ERROR = 3; //how can I recovery from a error state?

    public static final int ACTION_OPEN_CAMERA = 0;
    public static final int ACTION_OPEN_DECODE = 1;
    public static final int ACTION_CLOSE_DECODE = 2;
    public static final int ACTION_CLOSE_CAMERA = 3;

    private int currentState = STATE_CAMERA_CLOSE_DECODE_CLOSE;
    private IStateChangeListener statelistener;

    /**
     * be more strict or less strict??
     */
    private static int[][] dfa = new int[][]{
            {STATE_CAMERA_OPEN_DECODE_CLOSE, STATE_ERROR, STATE_CAMERA_CLOSE_DECODE_CLOSE, STATE_CAMERA_CLOSE_DECODE_CLOSE},
            {STATE_CAMERA_OPEN_DECODE_CLOSE, STATE_CAMERA_OPEN_DECODE_OPEN, STATE_CAMERA_OPEN_DECODE_CLOSE, STATE_CAMERA_CLOSE_DECODE_CLOSE},
            {STATE_CAMERA_OPEN_DECODE_OPEN, STATE_CAMERA_OPEN_DECODE_OPEN, STATE_CAMERA_OPEN_DECODE_CLOSE, STATE_ERROR},
            {STATE_ERROR, STATE_ERROR, STATE_ERROR, STATE_ERROR}
    };

    public QRCodeScannerDFA() {
        ;
    }

    public void setStateChangeListener(IStateChangeListener listener) {
        statelistener = listener;
    }

    public int next(int action) {
        return dfa[currentState][action];
    }

    public void setState(int state) {
        currentState = state;
    }

    public int getState() {
        return currentState;
    }

    interface IStateChangeListener {
        void fromStateTo(int oldState, int newState);
    }
}
