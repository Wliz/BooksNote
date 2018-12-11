# Docker


##基本命令
- docker search: Search the Docker Hub for images
- docker pull(docker image pull): Pull an image or a repository from a registry
- docker images(dokcer image ls): List images
- docker create: Create a new container
- docker start(docker container start): Start one or more stopped containers
- docker run: Run a command in a new container
- docker attach: Attach to a running container
- docker ps: List containers
- docker logs: Fetch the logs of a container
- docker restart: Restart a container
- docker stop: Stop one or more running containers
- docker kill: Kill one or more running containers
- docker rm: Remove one or more containers
- docker commit: create a new image from a container's changes
- docker tag: Create a tag TARGET_IMAGE that refers to SOURCE_IMAGE
- docker save: Save one or more images to a tar archive(Streamed to STDOUT by default)
- docker load: Load an image from a tar archive or STDIN
- docke network: Manage networks

## Dockerfile构建镜像
- FROM: 基础镜像
- WORDIR: 工作目录
- COPY/ADD: 复制文件或目录到镜像
- ENV: 设置环境变量
- RUN: 在构建过程中执行命令（build过程中）
- CMD: 指定容器默认运行的程序（仅一个程序），只有最后一个起作用
- ENTRYPOINT: 为容器指定默认运行程序(和CMD搭配，作为默认参数传递给ENTRYPOINT),但不会被docker run覆盖