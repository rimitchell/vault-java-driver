package com.bettercloud.vault.api;

import java.nio.charset.StandardCharsets;

import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.json.JsonObject;
import com.bettercloud.vault.response.MountInfoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;



public class LogicalUtilities {

    // See: https://github.com/hashicorp/vault/blob/0c9b0117093df05bd7539325c500ec3fbdff982d/command/kv_helpers.go#L44
    public static MountInfoResponse kvPreflightVersionRequest(final String path, final VaultConfig config) throws VaultException {
        try {
            String nameSpace = "";
            if (config.getNameSpace() != null && !config.getNameSpace().isEmpty()) {
                nameSpace = config.getNameSpace();
            }
            // Make an HTTP request to Vault
            final RestResponse restResponse = new Rest()//NOPMD
                    .url(config.getAddress() + "/v1/sys/internal/ui/mounts/" + path)
                    .header("X-Vault-Token", config.getToken())
                    .optionalHeader("X-Vault-Namespace", nameSpace)
                    .connectTimeoutSeconds(config.getOpenTimeout())
                    .readTimeoutSeconds(config.getReadTimeout())
                    .sslVerification(config.getSslConfig().isVerify())
                    .sslContext(config.getSslConfig().getSslContext())
                    .get();

            // Validate response
            if (restResponse.getStatus() != 200) {
                throw new VaultException("Vault responded with HTTP status code: " + restResponse.getStatus()
                        + "\nResponse body: " + new String(restResponse.getBody(), StandardCharsets.UTF_8),
                        restResponse.getStatus());
            }

            return new MountInfoResponse(restResponse, 0);

        } catch (RuntimeException | VaultException | RestException e) {
            throw new VaultException(e);
        }
    }

    /**
     * Injects the supplied qualifier (either "data" or "metadata") into the second-from-the-root segment position, for a Vault
     * path to be converted for use with a Version 2 secret engine.
     *
     * @param path The Vault path to check or mutate, based on the operation.
     * @param mountInfo Information pertaining to the mount point
     * @param qualifier The String to add to the path, based on the operation.
     * @return The final path with the needed qualifier.
     */
    public static String addQualifierToPath(final MountInfoResponse mountInfo, final String path, final String qualifier) {
        if(mountInfo.getVersion() == 2) {
            final StringBuilder adjustedPath = new StringBuilder(mountInfo.getMountPath());
            if(!qualifier.isEmpty()) {
                adjustedPath.append('/').append(qualifier);
            }
            adjustedPath
                //.append('/')
                .append(path.substring(mountInfo.getMountPath().length()));

            return adjustedPath.toString();
        } else {
            return path;
        }
    }

    /**
     * In version 1 style secret engines, the same path is used for all CRUD operations on a secret.  In version 2 though, the
     * path varies depending on the operation being performed.  When reading or writing a secret, you must inject the path
     * segment "data" right after the lowest-level path segment.
     *
     * @param path      The Vault path to check or mutate, based on the operation.
     * @param mountInfo Information pertaining to the mount point
     * @return The Vault path mutated based on the operation.
     */
    public static String adjustPathForReadOrWrite(final String path, final MountInfoResponse mountInfo) {
        return addQualifierToPath(mountInfo, path, "data");
    }

    /**
     * In version 1 style secret engines, the same path is used for all CRUD operations on a secret.  In version 2 though, the
     * path varies depending on the operation being performed.  When listing secrets available beneath a path, you must inject the
     * path segment "metadata" right after the lowest-level path segment.
     *
     * @param path      The Vault path to check or mutate, based on the operation.
     * @param mountInfo Information pertaining to the mount point
     * @return The Vault path mutated based on the operation.
     */
    public static String adjustPathForList(final String path, final MountInfoResponse mountInfo) {
        final StringBuilder adjustedPath = new StringBuilder();
        adjustedPath.append(addQualifierToPath(mountInfo, path, "metadata"));
        adjustedPath.append("?list=true");
        return adjustedPath.toString();
    }

    /**
     * In version 1 style secret engines, the same path is used for all CRUD operations on a secret.  In version 2 though, the
     * path varies depending on the operation being performed.  When deleting secrets, you must inject the  path segment "metadata"
     * right after the lowest-level path segment.
     *
     * @param path      The Vault path to check or mutate, based on the operation.
     * @param mountInfo Information pertaining to the mount point
     *
     * @return The modified path
     */
    public static String adjustPathForDelete(final String path, final MountInfoResponse mountInfo) {
        return addQualifierToPath(mountInfo, path, "metadata");
    }

    /**
     * When deleting secret versions, you must inject the path segment "delete" right after the lowest-level path segment.
     *
     * @param path The Vault path to check or mutate, based on the operation.
     * @param mountInfo Information pertaining to the mount point
     *
     * @return The modified path
     */
    public static String adjustPathForVersionDelete(final String path, final MountInfoResponse mountInfo) {
        return addQualifierToPath(mountInfo, path, "delete");
    }

    /**
     * When undeleting secret versions, you must inject the path segment "undelete" right after the lowest-level path segment.
     *
     * @param path The Vault path to check or mutate, based on the operation.
     * @param mountInfo Information pertaining to the mount point
     * @return The path mutated depending on the operation.
     */
    public static String adjustPathForVersionUnDelete(final String path, final MountInfoResponse mountInfo) {
        return addQualifierToPath(mountInfo, path, "undelete");
    }

    /**
     * When destroying secret versions, you must inject the path segment "destroy" right after the lowest-level path segment.
     *
     * @param path The Vault path to check or mutate, based on the operation.
     * @param mountInfo Information pertaining to the mount point 
     * @return The path mutated depending on the operation.
     */
    public static String adjustPathForVersionDestroy(final String path, final MountInfoResponse mountInfo) {
        return addQualifierToPath(mountInfo, path, "destroy");
    }

    /**
     * In version two, when writing a secret, the JSONObject must be nested with "data" as the key.
     *
     * @param engineVersion Version of the mount we're dealing with
     * @param jsonObject The jsonObject that is going to be written.
     * @return This jsonObject mutated for the operation.
     */
    public static JsonObject jsonObjectToWriteFromEngineVersion(final Integer engineVersion, final JsonObject jsonObject) {
        if (engineVersion.equals(2)) {
            final JsonObject wrappedJson = new JsonObject();
            wrappedJson.add("data", jsonObject);
            return wrappedJson;
        } else return jsonObject;
    }
}