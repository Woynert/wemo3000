package main

import (
	"net/http"
	"os/exec"

	"github.com/gin-gonic/gin"
)

func ping (ctx *gin.Context) {
    ctx.Status(http.StatusOK)
}

func shutdown (ctx *gin.Context) {
    cmd := exec.Command("sh", "-c", "touch /tmp/wemo3000-`date +\"%T\"`")
    err := cmd.Run()
    if (err != nil) {
        ctx.Status(http.StatusInternalServerError)
    }
    ctx.Status(http.StatusOK)
}

func main()  {
    engine := gin.Default()

    // discover service / check if device is ON
	engine.GET("/ping", ping)
	engine.GET("/shutdown", shutdown)

    engine.Run(":2333")
}
