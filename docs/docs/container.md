---
sidebar_position: 3
title: Container Usage
---

## container usage

```shell
docker run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -it ghcr.io/appthreat/atom atom --help
# podman run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -it ghcr.io/appthreat/atom atom --help
```

Example for java project.

```shell
docker run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -it ghcr.io/appthreat/atom atom -l java -o /app/app.atom /app
# podman run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -it ghcr.io/appthreat/atom atom -l java -o /app/app.atom /app
```