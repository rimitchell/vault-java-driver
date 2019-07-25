package com.bettercloud.vault.vault.mock;

import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>This class is used to mock out a Vault server in unit tests involving retry logic.  As it extends Jetty's
 * <code>AbstractHandler</code>, it can be passed to an embedded Jetty server and respond to actual (albeit localhost)
 * HTTP requests.</p>
 *
 * <p>The basic usage pattern is as follows:</p>
 *
 * <ol>
 *     <li>
 *         <code>RetriesMockVault</code> responds with HTTP 500 status codes to a designated number of requests (which
 *         can be zero).  This can be used to test retry logic.
 *     </li>
 *     <li>
 *         On subsequent HTTP requests, <code>RetriesMockVault</code> responds with a designated HTTP status code, and
 *         a designated response body.
 *     </li>
 * </ol>
 *
 * <p>Example usage:</p>
 *
 * <blockquote>
 * <pre>{@code
 * final Server server = new Server(8999);
 * server.setHandler( new RetriesMockVault(5, 200, "{\"data\":{\"value\":\"mock\"}}") );
 * server.start();
 *
 * final VaultConfig vaultConfig = new VaultConfig("http://127.0.0.1:8999", "mock_token");
 * final Vault vault = new Vault(vaultConfig);
 * final LogicalResponse response = vault.withRetries(5, 100).logical().read("secret/hello");
 * assertEquals(5, response.getRetries());
 * assertEquals("mock", response.getData().get("value"));
 *
 * VaultTestUtils.shutdownMockVault(server);
 * }</pre>
 * </blockquote>
 */
public class RetriesMockVault extends MockVault {

    private int failureCount;
    private int retryCount;
    private int mockStatus;
    private String mockResponse;
    private boolean handlePreflightQuery;

    public RetriesMockVault(final int failureCount, final int mockStatus, final String mockResponse) {
        this.failureCount = failureCount;
        this.mockStatus = mockStatus;
        this.mockResponse = mockResponse;
        this.handlePreflightQuery = true;
        this.retryCount = 0;
    }

    @Override
    public void handle(
            final String target,
            final Request baseRequest,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        response.setContentType("application/json");
        baseRequest.setHandled(true);
        if (retryCount == 0) {
            retryCount++;
            response.setStatus(200); // This allows the prefligt response to work
            System.out.println("RetriesMockVault is sending an HTTP 200 code, to allow the preflight check to pass");
            response.getWriter().println("{\"request_id\": \"0\",  \"lease_id\": \"0\",  \"renewable\": false,  \"lease_duration\": 0,  \"data\": { \"accessor\": \"0\",    \"config\": {      \"default_lease_ttl\": 0,      \"force_no_cache\": false,      \"max_lease_ttl\": 0    },    \"description\": \"\",    \"local\": false,    \"options\": {      \"version\": \"2\"    },    \"path\": \"secret\",    \"seal_wrap\": false,    \"type\": \"kv\"  }}");
        } else if (retryCount <= failureCount) {
            retryCount++;
            response.setStatus(500);
            System.out.println("RetriesMockVault is sending an HTTP 500 code, to cause a retry...");
        } else {
            System.out.println("RetriesMockVault is sending an HTTP " + mockStatus + " code, with expected success payload...");
            response.setStatus(mockStatus);
            if (mockResponse != null) {
                response.getWriter().println(mockResponse);
            }
        }
    }
}
