package main

import (
	"net/http"
	"os"
	"os/exec"

	"github.com/gin-gonic/gin"
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
	if _, exists := os.LookupEnv("WEMO3000_RELEASE"); exists {
		cmd = exec.Command("poweroff")
	}

	err := cmd.Run()
	if err != nil {
		ctx.Status(http.StatusInternalServerError)
	}
	ctx.Status(http.StatusOK)
}
