package com.example.payment.connector.util;

import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.Curve;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import com.nimbusds.jose.jwk.KeyOperation;
import java.util.Set;

public class SCAKeyGenerator {

    public static void main(String[] args) {

        try {
            KeyPairGenerator keyPairGenerator =
                    KeyPairGenerator.getInstance("EC");

            ECGenParameterSpec ecSpec =
                    new ECGenParameterSpec("secp256r1");

            keyPairGenerator.initialize(ecSpec);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

            ECKey publicJwk = new ECKey.Builder(
                    Curve.P_256,
                    publicKey
            )
                    .keyOperations(Set.of(KeyOperation.VERIFY))
                    .build();

            ECKey privateJwk = new ECKey.Builder(
                    Curve.P_256,
                    publicKey
            )
                    .privateKey(privateKey)
                    .keyOperations(Set.of(KeyOperation.SIGN))
                    .build();

            System.out.println("publicKey:");
            System.out.println(publicJwk.toJSONString());

            System.out.println();

            System.out.println("privateKey:");
            System.out.println(privateJwk.toJSONString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SCA keys", e);
        }
    }
}
