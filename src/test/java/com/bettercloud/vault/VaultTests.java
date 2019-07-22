package com.bettercloud.vault;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


/**
 * Unit tests for the various <code>Vault</code> constructors.
 */
public class VaultTests {

    @Test
    public void testDefaultVaultConstructor() {
        VaultConfig vaultConfig = new VaultConfig();
        Vault vault = new Vault(vaultConfig);
        Assert.assertNotNull(vault);
    }

    @Test
    public void testNameSpaceProvidedVaultConstructor() throws VaultException {
        VaultConfig vaultConfig = new VaultConfig().nameSpace("testNameSpace");
        Vault vault = new Vault(vaultConfig);
        Assert.assertNotNull(vault);
    }

    @Test
    public void testNameSpaceProvidedVaultConstructorCannotBeEmpty() {
        try {
            VaultConfig vaultConfig = new VaultConfig().nameSpace("");
        } catch (VaultException e) {
            Assert.assertEquals(e.getMessage(), "A namespace cannot be empty.");
        }
    }
}