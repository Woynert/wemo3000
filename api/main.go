package main

import (
	"fmt"
	"log"
	"sync/atomic"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/grandcat/zeroconf"
)

const PORT = 2333
const ADDR_CHECK_DELAY = 1 * time.Second
const MDNS_REFRESH_DELAY = 15 * time.Second

func setupMDNS() (*zeroconf.Server, error) {
	serverZero, err := zeroconf.Register("Wemo 3000 API", "_wemo3000._tcp", "local.", PORT, nil, nil)
	return serverZero, err
}

func main() {

	// mDNS

	serverZero, err := setupMDNS()
	if err != nil {
		panic(err)
	}

	// refresh if any ip changes

	l, err := ListenNetlink()
	if err != nil {
		panic(err)
	}

	var needsRefresh int32
	atomic.StoreInt32(&needsRefresh, 0)

	go func() {
		for {
			msgs, err := l.ReadMsgs()
			if err != nil {
				log.Printf("Could not read netlink: %s\n", err)
			}
			for _, m := range msgs {
				if IsNewAddr(&m) {
					atomic.StoreInt32(&needsRefresh, 1)
				}
			}
			time.Sleep(ADDR_CHECK_DELAY)
		}
	}()

	go func() {
		for {
			if atomic.LoadInt32(&needsRefresh) == 1 {
				atomic.StoreInt32(&needsRefresh, 0)

				log.Printf("Updating mDNS\n")
				serverZero.Shutdown()
				serverZero, err = setupMDNS()
				if err != nil {
					panic(err)
				}
			}
			time.Sleep(MDNS_REFRESH_DELAY)
		}
	}()

	// REST

	engine := gin.Default()
	engine.GET("/ping", ping)
	engine.DELETE("/shutdown", shutdown)
	engine.Run(fmt.Sprintf(":%d", PORT))
	defer serverZero.Shutdown()
}
