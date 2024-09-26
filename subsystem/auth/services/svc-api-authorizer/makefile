build:
	GOOS=linux GOARCH=arm64 go build -tags lambda.norpc -o bin/build/bootstrap main.go
package: build
	cd bin;zip -rj svc-service-access.zip build/bootstrap -x '**/.*' -x '**/__MACOSX'