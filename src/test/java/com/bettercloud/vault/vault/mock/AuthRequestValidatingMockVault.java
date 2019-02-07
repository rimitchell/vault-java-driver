package com.bettercloud.vault.vault.mock;

import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Predicate;

public class AuthRequestValidatingMockVault extends MockVault {
    private Predicate<HttpServletRequest> validator;

    private final String validResponse = "{\n" +
            "  \"renewable\": true,\n" +
            "  \"auth\": {\n" +
            "    \"lease_duration\": 1800000,\n" +
            "    \"metadata\": {\n" +
            "      \"role_tag_max_ttl\": \"0\",\n" +
            "      \"instance_id\": \"i-de0f1344\",\n" +
            "      \"ami_id\": \"ami-fce36983\",\n" +
            "      \"role\": \"dev-role\",\n" +
            "      \"auth_type\": \"ec2\"\n" +
            "    },\n" +
            "    \"policies\": [\n" +
            "      \"default\",\n" +
            "      \"dev\"\n" +
            "    ],\n" +
            "    \"accessor\": \"20b89871-e6f2-1160-fb29-31c2f6d4645e\",\n" +
            "    \"client_token\": \"c9368254-3f21-aded-8a6f-7c818e81b17a\"\n" +
            "  }\n" +
            "}";


    public AuthRequestValidatingMockVault(Predicate<HttpServletRequest> validator) {
        this.validator = validator;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        baseRequest.setHandled(true);
        if (validator.test(request)) {
            response.setStatus(200);
            response.getWriter().println(validResponse);
        } else {
            response.setStatus(400);
            response.getWriter().println("");
        }
    }
}
