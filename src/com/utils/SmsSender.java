package com.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

/**
 * @author adji
 *
 */
public class SmsSender {

	public static void sendSmsToContact (String message, String phoneNumber, Context context) {
		PendingIntent pi = PendingIntent.getActivity(context, 0,
				new Intent(context, SmsSender.class), 0);                
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, pi, null);
	}
	
	public static void sendSmsToContacts (String message, String[] phoneNumbers, Context context) {
		PendingIntent pi = PendingIntent.getActivity(context, 0,
				new Intent(context, SmsSender.class), 0);                
		SmsManager sms = SmsManager.getDefault();
		for (String phoneNumber : phoneNumbers) {
			sms.sendTextMessage(phoneNumber, null, message, pi, null);
		}
	}

}
