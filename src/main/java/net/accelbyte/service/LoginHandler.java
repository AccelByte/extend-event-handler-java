package net.accelbyte.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.accelbyte.iam.account.UserAuthenticationUserLoggedInServiceGrpc;
import net.accelbyte.iam.account.UserLoggedIn;
import net.accelbyte.sdk.api.platform.models.EntitlementGrant;
import net.accelbyte.sdk.api.platform.models.FulfillmentRequest;
import net.accelbyte.sdk.api.platform.models.FulfillmentResult;
import net.accelbyte.sdk.api.platform.operations.fulfillment.FulfillItem;
import net.accelbyte.sdk.api.platform.wrappers.Fulfillment;
import net.accelbyte.sdk.core.AccelByteSDK;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ServerErrorException;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@GRpcService
public class LoginHandler extends UserAuthenticationUserLoggedInServiceGrpc.UserAuthenticationUserLoggedInServiceImplBase {
    private final String namespace;
    private final String itemIdToGrant;
    private final Fulfillment fulfillment;

    @Autowired
    public LoginHandler(
        @Value("${plugin.grpc.config.namespace}") String namespace,
        @Value("${app.config.item_id_to_grant}") String itemIdToGrant,
        AccelByteSDK sdk
    ) {
        this.fulfillment = new Fulfillment(sdk);
        this.namespace = namespace;
        this.itemIdToGrant = itemIdToGrant;
        log.info("LoginHandler initialized");
    }

    @PostConstruct
    public void validate() {
        if (itemIdToGrant == null || itemIdToGrant.isEmpty()) {
            throw new IllegalArgumentException("Required envar ITEM_ID_TO_GRANT is not configured");
        }
    }

    private void grantEntitlement(String userId, String itemId) {
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

    @Override
    public void onMessage(UserLoggedIn request, StreamObserver<Empty> responseObserver) {
        String userId = request.getUserId();
        grantEntitlement(userId, itemIdToGrant);

        log.info("received a message: {}", request);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
