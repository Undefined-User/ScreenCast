package dev.nick.app.screencast.modle;

import android.support.annotation.Keep;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Tornaco on 2017/7/29.
 * Licensed with Apache.
 */
@Builder
@Getter
@Keep
public class PayExtra {
    private String nick;
    private String ad;
    private String date;
}
