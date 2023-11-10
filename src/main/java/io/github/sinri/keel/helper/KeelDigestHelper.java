package io.github.sinri.keel.helper;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 校验用加密（摘要）。
 * 特征是不可解密。
 *
 * @since 2.8
 */
public class KeelDigestHelper {

    private static final KeelDigestHelper instance = new KeelDigestHelper();

    private KeelDigestHelper() {

    }

    static KeelDigestHelper getInstance() {
        return instance;
    }

    /**
     * 获取raw对应的以数字和小写字母描述的MD5摘要值。
     *
     * @param raw raw string
     * @return md5 with lower digits
     * @since 1.1
     */
    @Nonnull
    public String md5(@Nonnull String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(raw.getBytes());
            return KeelHelpers.binaryHelper().encodeHexWithLowerDigits(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取raw对应的以数字和大写字母描述的MD5摘要值。
     *
     * @param raw raw string
     * @return MD5 with upper digits
     * @since 1.1
     */
    @Nonnull
    public String MD5(@Nonnull String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(raw.getBytes());
            return KeelHelpers.binaryHelper().encodeHexWithUpperDigits(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 2.8
     */
    @Nonnull
    public String SHA1(@Nonnull String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(raw.getBytes());
            return KeelHelpers.binaryHelper().encodeHexWithUpperDigits(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 2.8
     */
    @Nonnull
    public String sha1(@Nonnull String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(raw.getBytes());
            return KeelHelpers.binaryHelper().encodeHexWithLowerDigits(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 2.8
     */
    @Nonnull
    private byte[] compute_hmac_sha1(@Nonnull String raw, @Nonnull String key) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String MAC_NAME = "HmacSHA1";
        String ENCODING = "UTF-8";

        byte[] data = key.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二个参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);

        byte[] text = raw.getBytes(ENCODING);
        //完成 Mac 操作
        return mac.doFinal(text);
    }

    /**
     * @since 2.8
     */
    @Nonnull
    public String hmac_sha1_base64(@Nonnull String raw, @Nonnull String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * @since 2.8
     */
    @Nonnull
    public String hmac_sha1_hex(@Nonnull String raw, @Nonnull String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return KeelHelpers.binaryHelper().encodeHexWithLowerDigits(bytes);
    }

    /**
     * @since 2.8
     */
    public @Nonnull String HMAC_SHA1_HEX(@Nonnull String raw, @Nonnull String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return KeelHelpers.binaryHelper().encodeHexWithUpperDigits(bytes);
    }
}
