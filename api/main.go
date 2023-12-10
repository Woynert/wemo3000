package main

import (
	"fmt"
	"net/http"
	"os"
	"os/exec"

	"github.com/gin-gonic/gin"
	"github.com/grandcat/zeroconf"
)

const PORT = 2333

func ping(ctx *gin.Context) {
	ctx.Status(http.StatusOK)
	hostname, err := os.Hostname()
	if err != nil {
		ctx.Status(http.StatusInternalServerError)
	}
	ctx.JSON(http.StatusOK, gin.H{"hostname": hostname})
}

func shutdown(ctx *gin.Context) {
	cmd := exec.Command("sh", "-c", "touch /tmp/wemo3000-`date +\"%T\"`")
	if _, exists := os.LookupEnv("WEMO3000_RELEASE"); exists {
		cmd = exec.Command("sh", "-c", "touch /tmp/---WEMO3000-`date +\"%T\"`")
	}

	err := cmd.Run()
	if err != nil {
		ctx.Status(http.StatusInternalServerError)
	}
	ctx.Status(http.StatusOK)
}

func main() {
	// mDNS
	server, err := zeroconf.Register("Wemo 3000 API", "_wemo3000._tcp", "local.", PORT, nil, nil)
	if err != nil {
		panic(err)
	}
	defer server.Shutdown()

	// REST
	engine := gin.Default()
	engine.GET("/ping", ping)
	engine.DELETE("/shutdown", shutdown)
	engine.Run(fmt.Sprintf(":%d", PORT))
	defer server.Shutdown()
}
