package uk.co.emil.borconi.carnotifier;


import android.app.Notification;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class MyCarExtender implements NotificationCompat.Extender {

    private Bitmap mLargeIcon;
    private int mColor = Notification.COLOR_DEFAULT;

    private static final String TAG = "CarExtender";

    private static final String EXTRA_CAR_EXTENDER = "android.car.EXTENSIONS";
    private static final String EXTRA_LARGE_ICON = "large_icon";
    private static final String EXTRA_CONVERSATION = "car_conversation";
    private static final String EXTRA_COLOR = "app_color";
    public MyCarExtender() {
    }

    public MyCarExtender(Bitmap mLargeIcon,  int mColor) {
        this.mLargeIcon = mLargeIcon;
        this.mColor = mColor;

    }


    @Override
    public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
        Bundle carExtensions = new Bundle();

        if (mLargeIcon != null) {
            carExtensions.putParcelable(EXTRA_LARGE_ICON, mLargeIcon);
        }
        if (mColor != Notification.COLOR_DEFAULT) {
            carExtensions.putInt(EXTRA_COLOR, mColor);
        }


        carExtensions.putBundle(EXTRA_CONVERSATION, new Bundle());


        builder.getExtras().putBundle(EXTRA_CAR_EXTENDER, carExtensions);
        return builder;
    }


}
