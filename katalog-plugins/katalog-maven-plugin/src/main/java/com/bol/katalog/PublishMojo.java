package com.bol.katalog;

import feign.Feign;
import feign.Logger;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "publish", defaultPhase = LifecyclePhase.INSTALL)
public class PublishMojo extends AbstractMojo {
    @Parameter(required = true)
    private String katalogUrl = null;

    @Parameter(required = true)
    private String token = null;

    @Parameter(required = true)
    private Specification[] specifications = null;

    public void execute() throws MojoExecutionException {
        try {
            final KatalogApi client = Feign.builder()
                    .decoder(new JacksonDecoder())
                    .encoder(new FormEncoder(new JacksonEncoder()))
                    .logger(new Slf4jLogger())
                    .logLevel(Logger.Level.FULL)
                    .errorDecoder(new KatalogErrorDecoder())
                    .requestInterceptor(new AuthInterceptor(token))
                    .target(KatalogApi.class, katalogUrl);

            final ParsedToken parsedToken = new ParsedToken(token);

            final Publisher publisher = new Publisher(client, parsedToken);

            for (Specification spec : specifications) {
                publisher.publish(spec);
            }
        } catch (Throwable e) {
            throw new MojoExecutionException("There was a problem publishing specifications", e);
        }
    }
}
