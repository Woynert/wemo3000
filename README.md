## Wemo 3000

Remote computer shutdown service for linux with focus on good UX.

### Wemo 3000 Android

Simple Android application with a DNSSD client ([RxDNSSD](https://github.com/andriydruk/RxDNSSD)) to discover Wemo 3000 server instances to then consume the API through HTTP requests.

### Wemo 3000 Server

It has a REST API ([Gin Web Framework](https://github.com/gin-gonic/gin)) to shutdown the computer remotely and features a mDNS ([ZeroConf](https://github.com/grandcat/zeroconf)) server to advertise the REST API service to any client through all network interfaces.

Build:

```
cd ./api
make build
```

Run release:

```
WEMO3000_RELEASE=1 GIN_MODE=release wemo3000-server
```

> Note: `WEMO3000_RELEASE` must be set for the service to actually perform the shutdown functionality.
