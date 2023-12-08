package main

import (
	"net/http"
	"os/exec"
	"strings"

	"github.com/gin-gonic/gin"
)

func ping(ctx *gin.Context) {
	ctx.Status(http.StatusOK)
	hostname, err := exec.Command("hostname").Output()
	if err != nil {
		ctx.Status(http.StatusInternalServerError)
	}
	ctx.JSON(http.StatusOK, gin.H{"hostname": strings.TrimSpace(string(hostname))})
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
	engine := gin.Default()

	// discover service / check if device is ON
	engine.GET("/ping", ping)
	engine.GET("/shutdown", shutdown)
	engine.Run(":2333")
}
