/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.monitoring;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.SignalStrength;
import android.util.Base64;
import at.tugraz.ist.akm.R;

public class TelephonySignalStrength implements Closeable
{

    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_BEST = 5;

    private SignalStrength mSignalStrength = null;
    private int mSignalIconId = 0;
    private Context mContext = null;


    public TelephonySignalStrength(Context context)
    {
        mContext = context;
    }


    @Override
    public void close() throws IOException
    {
        mContext = null;
    }


    public void takeNewSignalStrength(SignalStrength signalStrength)
    {
        mSignalStrength = signalStrength;
    }


    public SignalStrength currentSignalStrength()
    {
        return mSignalStrength;
    }


    public byte[] getSignalStrengthIconBytes()
    {
        int signalStrength = getSignalStrength();
        int mSignalIconId = R.raw.signal_strength0;

        if (signalStrength >= SIGNAL_STRENGTH_BEST)
        {
            mSignalIconId = R.raw.signal_strength5;
        } else if (signalStrength >= SIGNAL_STRENGTH_GREAT)
        {
            mSignalIconId = R.raw.signal_strength4;
        } else if (signalStrength >= SIGNAL_STRENGTH_GOOD)
        {
            mSignalIconId = R.raw.signal_strength3;
        } else if (signalStrength >= SIGNAL_STRENGTH_MODERATE)
        {
            mSignalIconId = R.raw.signal_strength2;
        } else if (signalStrength >= SIGNAL_STRENGTH_POOR)
        {
            mSignalIconId = R.raw.signal_strength1;
        }

        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
                mSignalIconId);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100,
                (OutputStream) os);
        bmp = null;
        byte[] byteArray = os.toByteArray();

        return Base64.encode(byteArray, Base64.DEFAULT);
    }


    public int getSignalIconId()
    {
        return mSignalIconId;
    }


    /**
     * Signal strength for voice connection - range [0...4]
     */
    public int getSignalStrength()
    {
        int level;

        if (mSignalStrength.isGsm())
        {
            // If the connection is LTE the I don't care!
            // if ((mLteSignalStrength == -1) && (mLteRsrp == -1)
            // && (mLteRsrq == -1) && (mLteCqi == -1)) {
            level = getGsmLevel();
            // } else {
            // level = getLteLevel();
            // }
        } else
        {
            int cdmaLevel = getCdmaLevel();
            int evdoLevel = getEvdoLevel();
            if (evdoLevel == SIGNAL_STRENGTH_NONE_OR_UNKNOWN)
            {
                /* We don't know evdo, use cdma */
                level = getCdmaLevel();
            } else if (cdmaLevel == SIGNAL_STRENGTH_NONE_OR_UNKNOWN)
            {
                /* We don't know cdma, use evdo */
                level = getEvdoLevel();
            } else
            {
                /* We know both, use the lowest level */
                level = cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
            }
        }
        return level;
    }


    /**
     * Get cdma as level 0..4
     */
    private int getCdmaLevel()
    {
        final int cdmaDbm = mSignalStrength.getCdmaDbm();
        // What is Ec/Io? See:
        // http://www.telecomhall.com/what-is-ecio-and-ebno.aspx
        final int cdmaEcio = mSignalStrength.getCdmaEcio();
        int levelDbm;
        int levelEcio;

        if (cdmaDbm >= -75)
            levelDbm = SIGNAL_STRENGTH_GREAT;
        else if (cdmaDbm >= -85)
            levelDbm = SIGNAL_STRENGTH_GOOD;
        else if (cdmaDbm >= -95)
            levelDbm = SIGNAL_STRENGTH_MODERATE;
        else if (cdmaDbm >= -100)
            levelDbm = SIGNAL_STRENGTH_POOR;
        else
            levelDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        // Ec/Io are in dB*10
        if (cdmaEcio >= -90)
            levelEcio = SIGNAL_STRENGTH_GREAT;
        else if (cdmaEcio >= -110)
            levelEcio = SIGNAL_STRENGTH_GOOD;
        else if (cdmaEcio >= -130)
            levelEcio = SIGNAL_STRENGTH_MODERATE;
        else if (cdmaEcio >= -150)
            levelEcio = SIGNAL_STRENGTH_POOR;
        else
            levelEcio = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        int level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
        return level;
    }


    /**
     * Get Evdo as level 0..4
     */
    private int getEvdoLevel()
    {
        int evdoDbm = mSignalStrength.getEvdoDbm();
        int evdoSnr = mSignalStrength.getEvdoSnr();
        int levelEvdoDbm;
        int levelEvdoSnr;

        if (evdoDbm >= -65)
            levelEvdoDbm = SIGNAL_STRENGTH_GREAT;
        else if (evdoDbm >= -75)
            levelEvdoDbm = SIGNAL_STRENGTH_GOOD;
        else if (evdoDbm >= -90)
            levelEvdoDbm = SIGNAL_STRENGTH_MODERATE;
        else if (evdoDbm >= -105)
            levelEvdoDbm = SIGNAL_STRENGTH_POOR;
        else
            levelEvdoDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        if (evdoSnr >= 7)
            levelEvdoSnr = SIGNAL_STRENGTH_GREAT;
        else if (evdoSnr >= 5)
            levelEvdoSnr = SIGNAL_STRENGTH_GOOD;
        else if (evdoSnr >= 3)
            levelEvdoSnr = SIGNAL_STRENGTH_MODERATE;
        else if (evdoSnr >= 1)
            levelEvdoSnr = SIGNAL_STRENGTH_POOR;
        else
            levelEvdoSnr = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        int level = (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
        return level;
    }


    /**
     * Get gsm as level 0..4
     */
    private int getGsmLevel()
    {
        int level;

        // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
        // asu = 0 (-113dB or less) is very weak
        // signal, its better to show 0 bars to the user in such cases.
        // asu = 99 is a special case, where the signal strength is unknown.
        int asu = mSignalStrength.getGsmSignalStrength();
        if (asu <= 2 || asu == 99)
            level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        else if (asu >= 12)
            level = SIGNAL_STRENGTH_GREAT;
        else if (asu >= 8)
            level = SIGNAL_STRENGTH_GOOD;
        else if (asu >= 5)
            level = SIGNAL_STRENGTH_MODERATE;
        else
            level = SIGNAL_STRENGTH_POOR;
        return level;
    }

}
