package net.accelbyte.service;

import lombok.extern.slf4j.Slf4j;
import net.accelbyte.sdk.api.platform.models.EntitlementGrant;
import net.accelbyte.sdk.api.platform.models.FulfillmentRequest;
import net.accelbyte.sdk.api.platform.models.FulfillmentResult;
import net.accelbyte.sdk.api.platform.operations.fulfillment.FulfillItem;
import net.accelbyte.sdk.api.platform.wrappers.Fulfillment;
import net.accelbyte.sdk.core.AccelByteSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;

@Service
@Slf4j
public class EntitlementService {
    private final String namespace;
    private final Fulfillment fulfillment;

    @Autowired
    public EntitlementService(
        @Value("${plugin.grpc.config.namespace}") String namespace,
        AccelByteSDK sdk
    ) {
        this.namespace = namespace;
        this.fulfillment = new Fulfillment(sdk);
    }

    public void grantEntitlement(String userId, String itemId) {
        FulfillmentResult fulfillmentResult;
        try {
            FulfillmentRequest body = FulfillmentRequest.builder()
                    .itemId(itemId)
                    .quantity(1)
                    .source(EntitlementGrant.Source.REWARD.name())
                    .build();
            FulfillItem fulfillItemParam = FulfillItem.builder()
                    .namespace(namespace)
                    .userId(userId)
                    .body(body)
                    .build();
            fulfillmentResult = fulfillment.fulfillItem(fulfillItemParam);
        } catch (Exception e) {
            String reason = String.format("could not grant item to user, because: '%s'", e.getMessage());
            throw new ServerErrorException(reason, e);
        }

        if (fulfillmentResult != null) {
            for (var entitlementItem : fulfillmentResult.getEntitlementSummaries()) {
                log.info("entitlementId: {}", entitlementItem.getId());
            }
        }
    }
}

