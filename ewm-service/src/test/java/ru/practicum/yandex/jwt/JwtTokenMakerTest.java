package ru.practicum.yandex.jwt;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class JwtTokenMakerTest {

    @Test
    void createSecretKey() {
        SecretKey secretKey = Jwts.SIG.HS512.key().build();
        String encodedKey = DatatypeConverter.printHexBinary(secretKey.getEncoded());
        System.out.println(encodedKey);
    }
}
