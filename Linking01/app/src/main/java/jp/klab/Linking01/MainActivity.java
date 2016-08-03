/**
 *
 * Linking01
 *
 * - 連携ずみ Linking デバイスの LED を点灯させる
 * - Linking デバイスからの通知を受け取る
 *
 */

package jp.klab.Linking01;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nttdocomo.android.sdaiflib.Define;
import com.nttdocomo.android.sdaiflib.NotifyNotification;
import com.nttdocomo.android.sdaiflib.SendOther;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private Context mCtx;
    private Button mButtonLED;

    private static final byte LINKING_IF_PATTERN_ID = 0x20; //LEDパターンの設定項目ID（固定値）
    private static final byte LINKING_IF_COLOR_ID = 0x30;   //LED色の設定項目ID（固定値）
    private static final byte COLOR_ID_RED = 0x01;  // 点灯色
    private static final byte BLINK_PATTERN = 0x22; // 点灯パターン

    private NotifyNotification mNotifyNotification;
    private MyNotificationInterface mMyNotificationInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mButtonLED = (Button)findViewById(R.id.buttonLED);
        mButtonLED.setOnClickListener(this);
        // Linking デバイスからの通知受信用
        mMyNotificationInterface = new MyNotificationInterface();
        mNotifyNotification = new NotifyNotification(this, mMyNotificationInterface);
    }

    @Override
    public void onClick(View v) {
        // LED ボタン押下で連携ずみデバイスあてに LED 点灯指示を送る
        if (v == (View)mButtonLED) {
            SendOther sendOther = new SendOther(this);
            sendOther.setIllumination(
                    new byte[] {
                            LINKING_IF_PATTERN_ID,
                            BLINK_PATTERN,
                            LINKING_IF_COLOR_ID,
                            COLOR_ID_RED
                    });
            sendOther.send();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotifyNotification.release();
    }

    private class MyNotificationInterface implements NotifyNotification.NotificationInterface {
        @Override
        public void onNotify() { // 通知を受信した
            // Linking デバイスからの通知内容は SharedPreferences に記録される
            SharedPreferences preference =
                    getSharedPreferences(Define.NotificationInfo, Context.MODE_PRIVATE);
            int DEVICE_ID = preference.getInt("DEVICE_ID", -1);
            int DEVICE_BUTTON_ID = preference.getInt("DEVICE_BUTTON_ID", -1);
            // Toast 表示
            Toast.makeText(mCtx, "onNotify: DEVICE_ID=" + DEVICE_ID +
                    " DEVICE_BUTTON_ID=" + DEVICE_BUTTON_ID, Toast.LENGTH_SHORT).show();
            // 音も鳴らす
            ToneGenerator toneGenerator
                           = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING);
        }
    }
}
