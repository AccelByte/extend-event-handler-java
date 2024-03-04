FROM ngrok/ngrok as ngrok_image

FROM --platform=$BUILDPLATFORM alpine:3.19.1
WORKDIR /workspace
RUN apk --no-cache add bash curl docker-cli-buildx git jq make gradle gcompat \
        && apk add --update-cache --upgrade curl \
        && apk add --no-cache -X http://dl-cdn.alpinelinux.org/alpine/edge/testing grpcurl
COPY --from=ngrok_image /bin/ngrok /bin/ngrok
