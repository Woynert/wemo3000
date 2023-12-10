# Wemo 3000

Remote computer shutdown service for Linux and Android with focus on good UX.

## APP

Simple Android application with a DNSSD client ([RxDNSSD](https://github.com/andriydruk/RxDNSSD)) to discover Wemo 3000 server instances to then consume the API through HTTP requests.

Build. Artifacts should appear in `app/app/build/outputs/apk/`:

```
cd app
./gradlew assembleDebug
./gradlew assembleRelease
```

## Server

REST API ([Gin Web Framework](https://github.com/gin-gonic/gin)) to shutdown the computer remotely. It features a mDNS ([ZeroConf](https://github.com/grandcat/zeroconf)) server to advertise itself to any client through all network interfaces.

Build and run release:

```
cd api
make build
WEMO3000_RELEASE=1 GIN_MODE=release wemo3000-server
```

`WEMO3000_RELEASE` must be set for the service to actually perform the shutdown functionality. See sample service at [/api/runit/wemo3000/run](/api/runit/wemo3000/run).

## Future work

- Implement a relay server so clients can reach through the internet.
- The ability for the app to detect multiple peers at once.
