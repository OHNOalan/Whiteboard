# Whiteboard
A whiteboard application supporting multiple users editing the same content in various rooms.

Supported gradle tasks:

| Tasks   | Description                                          |
|:--------|:-----------------------------------------------------|
| clean   | Remove build/ directory                              |
| build   | Build the application project in build/ directory    |
| run     | Run the application or console project               |
| distZip | Create run scripts in application/build/distribution |
| distTar | Create run scripts in application/build/distribution |


## Deployment

To deploy the whiteboard server to Azure, we need to create a Docker image.

Change to the infra folder: `cd infra`

Then build the image: `docker build -t <name of image>`

Example: `docker build -t whiteboard-server-amd64`

Note that Azure only runs amd64 Docker images, not arm64 ones!

Compiling on an ARM cpu is **very slow** but doable, simply add the tag 
`--platform linux/amd64` to the `docker build` command. This is not recommended
because of how slow it is though, you should build on an x86 processor instead.

After building the docker image, tag it with your username:
`docker tag <image name>:latest <docker username>/<new name>:latest`

Example: `docker tag whiteboard-server-amd64:latest jwang1000/whiteboard-server:latest`

Then push to Docker Hub: `docker push <tagged name>:latest`

Example: `docker push jwang1000/whiteboard-server:latest`

The image should now be available to deploy as an Azure container.


## Images

<a href="https://www.flaticon.com/free-icons/pen" title="pen icons">Pen icons created by Freepik - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/logout" title="logout icons">Logout icons created by Pixel perfect - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/line" title="line icons">Line icons created by Freepik - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/eraser" title="eraser icons">Eraser icons created by Freepik - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/shapes" title="shapes icons">Shapes icons created by Freepik - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/text" title="text icons">Text icons created by Freepik - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/selection" title="selection icons">Selection icons created by Pixel perfect - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/whiteboard" title="whiteboard icons">Whiteboard icons created by Smashicons - Flaticon</a>
