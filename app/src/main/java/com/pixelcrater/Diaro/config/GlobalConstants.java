package com.pixelcrater.Diaro.config;

import java.util.ArrayList;

/**
 * Created by abhishek9851 on 05.03.18.
 */

public interface GlobalConstants {

    // Entries attachment types
    public static final String PHOTO = "photo";

    public static final String DIR_MEDIA = "media";
    public static final String DIR_MEDIA_PHOTO = "media/photo";
    public static final String DIR_MEDIA_AUDIO = "media/audio";
    public static final String DIR_MEDIA_PDF = "media/pdf";
    public static final String DIR_PROFILE = "profile";
    public static final String FILENAME_PROFILE = "profile.jpg";

    public static final int IMAGE_MAX_W_H = 2048;
    public static final int IMAGE_COMPRESS_QUALITY = 90;

    // ######### Dropbox Constants ##########
    // Dropbox key and secret
    public static final String DROPBOX_KEY = "gcta3jjx84z9va0";

    public static final int BATCH_UPLOAD_SIZE = 500;
    public static final int BATCH_DELETE_SIZE = 500;

    // Dropbox directories
    public static final String DROPBOX_PATH_MEDIA = "/media";
    public static final String DROPBOX_PATH_BACKUP = "/backup";
    public static final String DROPBOX_PATH_DATA = AppConfig.USE_PLAIN_JSON ? "/data_json" : "/data";
    public static final String DROPBOX_PATH_DATA_ENTRIES = DROPBOX_PATH_DATA + "/entries";
    public static final String DROPBOX_PATH_DATA_FOLDERS = DROPBOX_PATH_DATA + "/folders";
    public static final String DROPBOX_PATH_DATA_TAGS = DROPBOX_PATH_DATA + "/tags";
    public static final String DROPBOX_PATH_DATA_MOODS = DROPBOX_PATH_DATA + "/moods";
    public static final String DROPBOX_PATH_DATA_LOCATIONS = DROPBOX_PATH_DATA + "/locations";
    public static final String DROPBOX_PATH_DATA_ATTACHMENTS = DROPBOX_PATH_DATA + "/attachments";
    public static final String DROPBOX_PATH_DATA_TEMPLATES = DROPBOX_PATH_DATA + "/templates";
    public static final String DROPBOX_PATH_PROFILE = "/profile";
    public static final String DROPBOX_FILENAME_PROFILE = "profile.jpg";
    public static final String DROPBOX_FILEPATH_PROFILE_PHOTO = DROPBOX_PATH_PROFILE + "/" + DROPBOX_FILENAME_PROFILE;

    // Backup file names in zip
    public static final String FILENAME_V1_DIARO_EXPORT_XML = "DiaroExport.xml";
    public static final String FILENAME_V1_DIARO_EXPORT_ENCRYPTED_DRXML = "DiaroExport.drxml";
    public static final String FILENAME_V2_DIARO_EXPORT_XML = "DiaroBackup.xml";
    public static final String FILENAME_V2_DIARO_EXPORT_ENCRYPTED_DENC = "DiaroBackup.denc";

    // Api Urls
    public static final String DIARO_HOME_URL = "https://diaroapp.com";
    public static final String DIARO_ENTRY_URL = DIARO_HOME_URL + "/entry/";

    public static final String API_BASE_URL = "https://diaroapp.com/api/";
    public static final String API_LABS_BASE_URL = "https://sandstorm-software.com/api/";

    public static final String API_ADD_PAYMENT = "add_payment";
    public static final String API_CHECK_PRO = "check_pro";
    public static final String API_FORGOT_PASSWORD = "forgot_password";
    public static final String API_SIGNIN = "signin";
    public static final String API_SIGN_UP = "signup";
    public static final String API_FORGOT_SECURITY_CODE = "forgot_security_code";
    public static final String API_SET_DROPBOX_CREDENTIALS = "set_dropbox_credentials";
    public static final String API_GET_WEATHER = "get_weather";
    public static final String API_PING = "ping";

    // Diaro encryption key used for encrypting files (backup/sync)
    public static final String ENCRYPTION_KEY = "a27dce5748e6d41348294d3ebd8087e4";

    public static final String MOPUB_KEY = "33076014e9f546e496df8ee14a10983a";
    public static final String ADMOB_BANNER_KEY = "ca-app-pub-5690497443789842/2947219214";
    public static final String ADMOB_NATIVE_KEY = "ca-app-pub-5690497443789842/9565892850";
    public static final String ADMOB_FULLSCREEEN_KEY = "ca-app-pub-5690497443789842/5131900775";
    public static final String ADMOB_FULLSCREEEN_EXITENTRY = "ca-app-pub-5690497443789842/9715034881";

    public static final String ADMOB_FULLSCREEEN_KEY_TEST = "ca-app-pub-3940256099942544/1033173712";

    public static final String TEST_DEVICE_ID = "1D41E2CCA64FAD607066A9A72547F9C3";

    public static final String SUPPORT_EMAIL = "support@diaroapp.com";

    // -------------------- IAP -------------------------------------------

    // Google Play license key for this application
    public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy8deXK9GkDJCLnPOTV8nP7j7AIegHuSt8U0y2fJCIoF5Egch0hrgV83SOYhv3W3svROZHMWNeuO0tJazXXNT90G1XLvE4A4B4j+33LoIqBSEgGjDD0qBq89S+HsYSeivEU51jRlgsnp0wZsP1z79owy4B/S8SbaBUDCB+yXyFQcHe5w91GHLAFyhFnMk4p8xMCKPw5206FRxlLPcyvI2n3U7fyq3X5V0aBRcdGn6NIiyHpD9jw4FAv/iVb6bVkPO8zQUeoXd7EZOlLip83lCHNANILELu5E1cdfNF82yCzi2rpHj2awW+5IsxDGpXJ2aVjqzsXqBzIIh89JqwY4YqwIDAQAB";
    public static final String encryptedBase64EncodedPublicKey =
            "ANKxKbNokT/GFPnGIjUGgRN/Mh4P/h96kmZyTptE6zixNveCy4KQ2JrAv5FefizgaAczFNCVCLZo" +
                    "TCQTY6JCgQD9Y+R7NRg8Z4gFtSqdZ38fAsDtD/EPRQXnouZ/X8dF8XuQU8srO2AlXf4An3C5U39e" +
                    "x/fzLc+QgiFos3PcHvaxLDhlAOTbw/JRliacX0xjC1rpjoMfDqjdDwt37bOImdqUHxtpWIeMG9xC" +
                    "Bf7FRTx55ickxgQd57niw9Yn7+LMmegS8hwWCcXOM/9Iuh/XQQY74linU9tB16GWa8f6992Iq+Qa" +
                    "/x+Gvy73O0ICuUgaKbbDfovOS/VPY+po9gH0zeBm+7rm38Dl8VJuRF2Y8yia6kDNONScDriS9tUT" +
                    "FefMG4zDrQPXuWCV8UQ0+MvIrmFanAk1af2FboTT6VWa+Z8cqjzt65aBXztKJQFLIoG7jgL6HoxB" +
                    "C2hVV74kOHf86HrCSOBQU/C8u75Al3ncj7TUGPitm/IYahgnhqKaGflh7RBqp3YCfqmXBIR8g0f8" +
                    "LA==";

    // Merchant ID: 08157909215620556146
    public static final String encryptedMerchantId = "Q2SG39yurR53EbfkrNbEx/1AiNEmh46jr67dh6GM3rE=";

    // Google Play & Amazon in-app purchase SKU
    // Lifetime
    public static final String MANAGED_PURCHASE_PRO_VERSION = "diaro_pro_version";

    // old
    public static final String SUBSCRIPTION_PURCHASE_PRO_YEARLY = "subscription_pro_yearly"; // 5usd / year

    // new
    public static final String SUBSCRIPTION_PURCHASE_PREMIUM_MONTHLY = "subscription_pro_monthly";
    public static final String SUBSCRIPTION_PURCHASE_PREMIUM_3_MONTHLY = "subscription_pro_quarterly";
    public static final String SUBSCRIPTION_PURCHASE_PREMIUM_YEARLY = "subscription_premium_yearly"; // 9.99 usd / year
    public static final String SUBSCRIPTION_PURCHASE_PREMIUM_YEARLY_TRAIL = "subscription_pro_yearly_trail";  // 9.99 usd / year with 7 days trail


    ArrayList<String> activeSubscriptionsList = new ArrayList<String>() {{
        add(GlobalConstants.SUBSCRIPTION_PURCHASE_PRO_YEARLY);

        add(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_MONTHLY);
        add(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_3_MONTHLY);
        add(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_YEARLY);
        add(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_YEARLY_TRAIL);
    }};


    // Server info
    public static final String PAYMENT_SYSTEM_ANDROID = "Android";
    public static final String PAYMENT_PRODUCT_DIARO_PRO = "Diaro PRO";
    public static final String PAYMENT_PRODUCT_DIARO_PRO_Yearly = "Diaro PRO Yearly";

    // New
    public static final String PAYMENT_PRODUCT_DIARO_PREMIUM_MONTHLY = "Diaro PREMIUM Monthly";
    public static final String PAYMENT_PRODUCT_DIARO_PREMIUM_QUARTERLY = "Diaro PREMIUM Quarterly";
    public static final String PAYMENT_PRODUCT_DIARO_PREMIUM_YEARLY = "Diaro PREMIUM Yearly";
    public static final String PAYMENT_PRODUCT_DIARO_PREMIUM_YEARLY_TRAIL = "Diaro PREMIUM Yearly Trail";

    public static final String PAYMENT_TYPE_GOOGLE = "Android in-app";
    public static final String PAYMENT_TYPE_AMAZON = "Amazon in-app";

    // -------------------- IAP -------------------------------------------

}
