package com.pixelcrater.Diaro.premium.billing;

import android.text.TextUtils;
import android.util.Base64;

import com.pixelcrater.Diaro.utils.AppLog;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Security-related methods for in-app billing.
 * For a secure implementation, you should verify purchases server-side as well.
 */
public class Security {
    private static final String TAG = "IABSecurity";
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * Verifies that the data was signed with the given signature.
     *
     * @param base64PublicKey the base64-encoded public key to use for verifying.
     * @param signedData the signed JSON string (the signature is over this data)
     * @param signature the signature for the data, signed with the private key
     * @return true if the signature is valid, false otherwise
     */
    public static boolean verifyPurchase(String base64PublicKey, String signedData, String signature) {
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey) || TextUtils.isEmpty(signature)) {
            AppLog.e(TAG + ": Purchase verification failed: missing data");
            return false;
        }

        PublicKey key = generatePublicKey(base64PublicKey);
        if (key == null) {
            AppLog.e(TAG + ": Failed to generate public key");
            return false;
        }

        return verify(key, signedData, signature);
    }

    /**
     * Generates a PublicKey instance from a base64-encoded public key string.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @return PublicKey instance or null if error
     */
    private static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            AppLog.e(TAG + ": RSA algorithm not available: " + e.getMessage());
        } catch (InvalidKeySpecException e) {
            AppLog.e(TAG + ": Invalid key specification: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            AppLog.e(TAG + ": Base64 decoding failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Verifies that the signature from the server matches the computed signature on the data.
     *
     * @param publicKey public key associated with the developer account
     * @param signedData signed data from server
     * @param signature server signature
     * @return true if the signature is valid, false otherwise
     */
    private static boolean verify(PublicKey publicKey, String signedData, String signature) {
        try {
            Signature signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM);
            signatureAlgorithm.initVerify(publicKey);
            signatureAlgorithm.update(signedData.getBytes());

            byte[] signatureBytes = Base64.decode(signature, Base64.DEFAULT);
            boolean verified = signatureAlgorithm.verify(signatureBytes);

            if (!verified) {
                AppLog.e(TAG + ": Signature verification FAILED");
            }

            return verified;
        } catch (NoSuchAlgorithmException e) {
            AppLog.e(TAG + ": Signature algorithm not available: " + e.getMessage());
        } catch (InvalidKeyException e) {
            AppLog.e(TAG + ": Invalid key: " + e.getMessage());
        } catch (SignatureException e) {
            AppLog.e(TAG + ": Signature exception: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            AppLog.e(TAG + ": Base64 decoding failed: " + e.getMessage());
        }
        return false;
    }
}
