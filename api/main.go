package main

import (
	"net/http"
	"os"
	"os/exec"

	"github.com/gin-gonic/gin"
	"github.com/grandcat/zeroconf"
)

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
	err := cmd.Run()
	if err != nil {
		ctx.Status(http.StatusInternalServerError)
	}
	ctx.Status(http.StatusOK)
}

func main() {
	// mDNS
	server, err := zeroconf.Register("Wemo 3000 API", "_wemo3000._tcp", "local.", 42424, []string{"txtv=0", "lo=1", "la=2"}, nil)
	if err != nil {
		panic(err)
	}
	defer server.Shutdown()

	// REST
	engine := gin.Default()
	engine.GET("/ping", ping)
	engine.GET("/shutdown", shutdown)
	engine.Run(":2333")
	defer server.Shutdown()
}
