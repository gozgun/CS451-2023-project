package cs451;

public class Constants {
    public static final int ARG_LIMIT_CONFIG = 7;

    // indexes for id
    public static final int ID_KEY = 0;
    public static final int ID_VALUE = 1;

    // indexes for hosts
    public static final int HOSTS_KEY = 2;
    public static final int HOSTS_VALUE = 3;

    // indexes for output
    public static final int OUTPUT_KEY = 4;
    public static final int OUTPUT_VALUE = 5;

    // indexes for config
    public static final int CONFIG_VALUE = 6;

    // sizes for queues
    public static final int DELIVERER_QUEUE_SIZE = 20000;
    public static final int CLIENT_QUEUE_SIZE = 10000;
    public static final int CLIENT_ACK_QUEUE_SIZE = 20000;
    public static final int SL_MAP_SIZE = 100000000;
}
