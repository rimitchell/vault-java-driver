package com.bettercloud.vault;

import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.api.LogicalUtilities;
import com.bettercloud.vault.response.MountInfoResponse;
import com.bettercloud.vault.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;


public class LogicalUtilitiesTests {

    @Test
    public void addQualifierToPathTests() {
        MountInfoResponse mountInfo = new MountInfoResponse("test", 2);
        String qualifierOutput = LogicalUtilities.addQualifierToPath(mountInfo, "test", "");
        Assert.assertEquals("test", qualifierOutput);

        mountInfo = new MountInfoResponse("before", 2);
        String qualifierOutput2 = LogicalUtilities.addQualifierToPath(mountInfo, "before/", "test");
        Assert.assertEquals("before/test/", qualifierOutput2);

        mountInfo = new MountInfoResponse("before1", 2);
        String qualifierOutput3 = LogicalUtilities.addQualifierToPath(mountInfo, "before1/before2", "test");
        Assert.assertEquals("before1/test/before2", qualifierOutput3);
    }

    @Test
    public void adjustPathForReadOrWriteTests() {
        MountInfoResponse mountInfoV2 = new MountInfoResponse("test", 2);
        MountInfoResponse mountInfoV1 = new MountInfoResponse("test", 1);

        String readOutputV2 = LogicalUtilities.adjustPathForReadOrWrite("test/", mountInfoV2);
        Assert.assertEquals("test/data/", readOutputV2);

        String readOutputV2WithSlash = LogicalUtilities.adjustPathForReadOrWrite("test//", mountInfoV2);
        Assert.assertEquals("test/data//", readOutputV2WithSlash);

        String writeOutputV2 = LogicalUtilities.adjustPathForReadOrWrite("test/", mountInfoV2);
        Assert.assertEquals("test/data/", writeOutputV2);

        String writeOutputV2WithSlash = LogicalUtilities.adjustPathForReadOrWrite("test//", mountInfoV2);
        Assert.assertEquals("test/data//", writeOutputV2WithSlash);

        String readOutputV1 = LogicalUtilities.adjustPathForReadOrWrite("test", mountInfoV1);
        Assert.assertEquals("test", readOutputV1);

        String writeOutputV1 = LogicalUtilities.adjustPathForReadOrWrite("test", mountInfoV1);
        Assert.assertEquals("test", writeOutputV1);
    }

    @Test
    public void adjustPathForListTests() {
        MountInfoResponse mountInfoV2 = new MountInfoResponse("test", 2);
        MountInfoResponse mountInfoV1 = new MountInfoResponse("", 1);

        String listOutputV2 = LogicalUtilities.adjustPathForList("test", mountInfoV2);
        Assert.assertEquals("test/metadata?list=true", listOutputV2);

        String listOutputV2WithSlash = LogicalUtilities.adjustPathForList("test/", mountInfoV2);
        Assert.assertEquals("test/metadata/?list=true", listOutputV2WithSlash);

        String listOutputV1 = LogicalUtilities.adjustPathForList("test", mountInfoV1);
        Assert.assertEquals("test?list=true", listOutputV1);
    }

    // @Test
    // public void adjustPathForDeleteTests() {
    //     String deleteOutputV2 = LogicalUtilities.adjustPathForDelete("test", Logical.logicalOperations.deleteV2);
    //     Assert.assertEquals(deleteOutputV2, "test/metadata/");

    //     String deleteOutputV2WithSlash = LogicalUtilities.adjustPathForDelete("test/", Logical.logicalOperations.deleteV2);
    //     Assert.assertEquals(deleteOutputV2WithSlash, "test/metadata//");

    //     String deleteOutputV1 = LogicalUtilities.adjustPathForDelete("test", Logical.logicalOperations.deleteV1);
    //     Assert.assertEquals(deleteOutputV1, "test");
    // }

    // @Test
    // public void adjustPathForVersionDeleteTests() {
    //     String versionDeleteOutput = LogicalUtilities.adjustPathForVersionDelete("test");
    //     Assert.assertEquals(versionDeleteOutput, "test/delete/");

    //     String versionDeleteOutputWithSlash = LogicalUtilities.adjustPathForVersionDelete("test/");
    //     Assert.assertEquals(versionDeleteOutputWithSlash, "test/delete//");
    // }

    // @Test
    // public void adjustPathForVersionUnDeleteTests() {
    //     String versionDeleteOutput = LogicalUtilities.adjustPathForVersionUnDelete("test");
    //     Assert.assertEquals(versionDeleteOutput, "test/undelete/");

    //     String versionDeleteOutputWithSlash = LogicalUtilities.adjustPathForVersionUnDelete("test/");
    //     Assert.assertEquals(versionDeleteOutputWithSlash, "test/undelete//");
    // }

    // @Test
    // public void adjustPathForVersionDestroyTests() {
    //     String versionDeleteOutput = LogicalUtilities.adjustPathForVersionDestroy("test");
    //     Assert.assertEquals(versionDeleteOutput, "test/destroy/");

    //     String versionDeleteOutputWithSlash = LogicalUtilities.adjustPathForVersionDestroy("test/");
    //     Assert.assertEquals(versionDeleteOutputWithSlash, "test/destroy//");
    // }

    // @Test
    // public void jsonObjectToWriteFromEngineVersionTests() {
    //     JsonObject jsonObjectV2 = new JsonObject().add("test", "test");
    //     JsonObject jsonObjectFromEngineVersionV2 = LogicalUtilities.jsonObjectToWriteFromEngineVersion(2, jsonObjectV2);
    //     Assert.assertEquals(jsonObjectFromEngineVersionV2.get("data"), jsonObjectV2);

    //     JsonObject jsonObjectV1 = new JsonObject().add("test", "test");
    //     JsonObject jsonObjectFromEngineVersionV1 = LogicalUtilities.jsonObjectToWriteFromEngineVersion(1, jsonObjectV1);
    //     Assert.assertNull(jsonObjectFromEngineVersionV1.get("data"));
    // }
}
