package com.bol.katalog;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PublishMojoTest {
    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public WireMockRule apiStub = new WireMockRule(0);

    private PublishMojo mojo;

    private String token;

    @Before
    public void before() throws Exception {
        File pom = new File("src/test/resources/basic-project");
        assertThat(pom).exists();

        mojo = (PublishMojo) rule.lookupConfiguredMojo(pom, "publish");
        assertThat(mojo).isNotNull();

        rule.setVariableValueToObject(mojo, "katalogUrl", "http://localhost:" + apiStub.port());

        token = (String) rule.getVariableValueFromObject(mojo, "token");
    }

    @Test
    public void can_upload_to_existing_version() throws Exception {
        // Does the version exist? Yes.
        apiStub.stubFor(
                get(urlEqualTo("/api/v1/versions/find/group1_ns1/sc1/1.2.3"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .willReturn(
                                okJson("{ \"id\": \"id-for-version\" }")
                        ));

        // Upload the artifact
        apiStub.stubFor(
                post(urlEqualTo("/api/v1/artifacts"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .withMultipartRequestBody(
                                aMultipart().withName("file")
                        )
                        .withMultipartRequestBody(
                                aMultipart().withBody(equalTo("id-for-version"))
                        )
                        .willReturn(
                                okJson("{ \"id\": \"id-for-artifact\" }")
                        )
        );

        mojo.execute();
    }

    @Test
    public void can_publish_to_new_version() throws Exception {
        // Does the version exist? No.
        apiStub.stubFor(
                get(urlEqualTo("/api/v1/versions/find/group1_ns1/sc1/1.2.3"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .willReturn(notFound()));

        // Find the schema id to use to create the new version.
        apiStub.stubFor(
                get(urlEqualTo("/api/v1/schemas/find/group1_ns1/sc1"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .willReturn(
                                okJson("{ \"id\": \"id-for-schema\" }")
                        ));

        // Create the new version
        apiStub.stubFor(
                post(urlEqualTo("/api/v1/versions"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .withRequestBody(equalToJson("{ \"schemaId\": \"id-for-schema\", \"version\": \"1.2.3\" }"))
                        .willReturn(
                                okJson("{ \"id\": \"id-for-version\" }")
                        ));

        // Upload the artifact
        apiStub.stubFor(
                post(urlEqualTo("/api/v1/artifacts"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .withMultipartRequestBody(
                                aMultipart().withName("file")
                        )
                        .withMultipartRequestBody(
                                aMultipart().withBody(equalTo("id-for-version"))
                        )
                        .willReturn(
                                okJson("{ \"id\": \"id-for-artifact\" }")
                        )
        );

        mojo.execute();
    }
}

