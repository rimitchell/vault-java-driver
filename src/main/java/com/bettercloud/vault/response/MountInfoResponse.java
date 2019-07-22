package com.bettercloud.vault.response;

import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.json.Json;
import com.bettercloud.vault.json.JsonObject;
import com.bettercloud.vault.json.JsonValue;
import com.bettercloud.vault.rest.RestResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a container for the information returned by Vault in when query about mount information
 */
public class MountInfoResponse extends VaultResponse {

    private JsonObject dataObject = null;
    private String mountPath;
    private int version;

    /**
     * @param restResponse The raw HTTP response from Vault.
     * @param retries      The number of retry attempts that occurred during the API call (can be zero).
     */
    public MountInfoResponse(final RestResponse restResponse, final int retries) {
        super(restResponse, retries);
        parseResponseData();
    }

    /**
     * @param mountPath The raw HTTP response from Vault.
     * @param version      The number of retry attempts that occurred during the API call (can be zero).
     */
    public MountInfoResponse(final String mountPath, final int version) {
        super(null, 0);
        this.mountPath = mountPath;
        this.version = version;
    }

    public JsonObject getDataObject() {
        return dataObject;
    }

    public String getMountPath() {
        return mountPath;
    }

    public int getVersion() {
        return version;
    }

    private void parseResponseData() {
        try {
            final String jsonString = new String(getRestResponse().getBody(), StandardCharsets.UTF_8);
            JsonObject jsonObject = Json.parse(jsonString).asObject();
            this.dataObject = jsonObject.get("data").asObject();

            this.mountPath = jsonObject.get("path").asString();

            final JsonObject optionObject = jsonObject.get("options").asObject();
            if( !optionObject.isNull() ) {
                this.version = Integer.parseInt(optionObject.get("versions").asString());
            } else {
                this.version = 1;
            }
        } catch (Exception ignored) {
        }
    }
}
