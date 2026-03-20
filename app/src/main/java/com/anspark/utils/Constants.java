package com.anspark.utils;

public final class Constants {
    private Constants() {
    }

    // Dla emulatora Androida
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";

    // Lub dla prawdziwego telefonu (zmień na IP Twojego serwera)
    // public static final String BASE_URL = "http://192.168.1.XXX:8080/api/";

    public static final boolean USE_MOCK_DATA = false;
    public static final int NETWORK_TIMEOUT_SECONDS = 20;
    public static final int PAGE_SIZE = 20;
}
