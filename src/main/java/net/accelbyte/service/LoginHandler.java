package net.accelbyte.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.accelbyte.iam.account.UserAuthenticationUserLoggedInServiceGrpc;
import net.accelbyte.iam.account.UserLoggedIn;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

@Slf4j
@GRpcService
public class LoginHandler extends UserAuthenticationUserLoggedInServiceGrpc.UserAuthenticationUserLoggedInServiceImplBase {
    private final String itemIdToGrant;
    private final EntitlementService entitlementService;

    @Autowired
    public LoginHandler(
        @Value("${app.config.item_id_to_grant}") String itemIdToGrant,
        EntitlementService entitlementService
    ) {
        this.itemIdToGrant = itemIdToGrant;
        this.entitlementService = entitlementService;
        log.info("LoginHandler initialized");
    }

    @PostConstruct
    public void validate() {
        if (itemIdToGrant == null || itemIdToGrant.isEmpty()) {
            throw new IllegalArgumentException("Required envar ITEM_ID_TO_GRANT is not configured");
        }
    }

    @Override
    public void onMessage(UserLoggedIn request, StreamObserver<Empty> responseObserver) {
        String userId = request.getUserId();
        entitlementService.grantEntitlement(userId, itemIdToGrant);

        log.info("received a message: {}", request);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
