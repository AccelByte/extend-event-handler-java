package net.accelbyte.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.accelbyte.iam.account.UserAuthenticationUserLoggedInServiceGrpc;
import net.accelbyte.iam.account.UserLoggedIn;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

    @Override
    public void onMessage(UserLoggedIn request, StreamObserver<Empty> responseObserver) {
        String userId = request.getUserId();
        entitlementService.grantEntitlement(userId, itemIdToGrant);

        log.info("received a message: {}", request);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
