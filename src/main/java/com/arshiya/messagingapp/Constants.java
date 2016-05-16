package com.arshiya.messagingapp;

/**
 * Created by arshiya on 8/5/16.
 */
public class Constants {

    public static int MESSAGE_TYPE_ALL = 0;
    public static int MESSAGE_TYPE_INBOX = 1;
    public static int MESSAGE_TYPE_SENT = 2;
    public static int MESSAGE_TYPE_DRAFT = 3;
    public static int MESSAGE_TYPE_OUTBOX = 4;
    public static int MESSAGE_TYPE_FAILED = 5;
    public static int MESSAGE_TYPE_QUEUED = 6;


    public static int NONE = -1;
    public static int COMPLETE = 0;
    public static int PENDING = 32;
    public static int FAILED = 64;

    public static final String SENT = "SMS_SENT";
    public static final String DELIVERED = "SMS_DELIVERED";

}
