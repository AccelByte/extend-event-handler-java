package net.accelbyte.config;

import net.accelbyte.sdk.core.AccelByteSDK;
import net.accelbyte.sdk.core.client.OkhttpClient;
import net.accelbyte.sdk.core.repository.DefaultConfigRepository;
import net.accelbyte.sdk.core.repository.DefaultTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerErrorException;

import javax.annotation.PostConstruct;

@Configuration
public class AppConfig {

    @Value("${app.config.item_id_to_grant}")
    private String itemIdToGrant;

    @PostConstruct
    public void validateItemIdToGrant() {
        if (itemIdToGrant == null || itemIdToGrant.isEmpty()) {
            throw new IllegalArgumentException("Required envar ITEM_ID_TO_GRANT is not configured");
        }
    }

    @Bean
    public AccelByteSDK provideAccelbyteSdk() {
        AccelByteSDK sdk = new AccelByteSDK(
                new OkhttpClient(), new DefaultTokenRepository(), new DefaultConfigRepository());
        boolean isSuccess = sdk.loginClient();
        if (!isSuccess) {
            throw new ServerErrorException("failed to sdk.loginClient()", new IllegalArgumentException());
        }
        return sdk;
    }

}
