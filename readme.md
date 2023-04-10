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


## Usage

To run the project, start up the `infra` project first. This is the server of the whiteboard.

After the server has started, run the whiteboard application to open one instance. You may run multiple instances at a time.

The first screen you will see is the login screen. Create a username and password and click on register to create an account.

After successfully logging in, you will see the whiteboard pop up. By default, you will be in a new room.

Rooms are whiteboard spaces where users can collaborate together in real-time. Only users in the same room can see each other's edits.

The room code at the top left shows the code of the room that you are currently in. 

You may join a new room by typing in a valid room code and click join, or you can create a new room via the new room button.

You can logout on the top left and sign in as a different user if you wish.

Changes are automatically saved in the cloud. As long as you remember your room number, every time you join that room, your drawings would be loaded.

You may also save drawings locally as a whiteboard file with the dropdown menu under file.

You can undo and redo changes to the whiteboard under the edit dropdown menu.

All undo and redo items are stored globally on a per room basis. That means anyone in the same room can undo and redo entities in that room.

This concludes a brief overview of using the whiteboard. Please see features section for detailed list of features.


## Features

### Dropdown Menu

File > Save: Save the current drawing as a whiteboard file locally.

File > Save As: Prompt for file location and save the current drawing as a whiteboard file locally.

File > Load: Load a whiteboard file locally and replace the current drawing with the contents of that file.

File > Export to Image: Export the current drawing as a PNG image file.

File > Exit: Exits the application.

Edit > Undo: If there are undo-able actions, undo the top action globally for all users in the room. Otherwise it does nothing. This supports undoing created drawings, deleted drawings, text box changes, shape changes, segment line changes, entity movements, clear screens, and load files.

Edit > Redo: If there are redo-able actions, redo the top action globally for all users in the room. Otherwise it does nothing. This supports redoing created drawings, deleted drawings, text box changes, shape changes, segment line changes, entity movements, clear screens, and load files.

Edit > Clear: Clears all the drawings off the canvas.

View > Zoom In: Zoom into the canvas.

View > Zoom Out: Zoom out of the canvas.

View > Reset Zoom: Reset the zoom of the canvas.

### Tools

Pen: Freely draws a line on the canvas. When selected, you can change its color and thickness.

Eraser: Erases items off the canvas. When selected, you can change the thickness of the eraser.

Text: Creates a text box on the canvas. Click and drag on the canvas to specify the size of the text box. After the text box is created, you can edit the text box using the select tool.

Shape: Creates shapes on the canvas. When selected, you can change the color, fill, and type of shape to draw. Possible choices for shapes: rectangle, square, ellipse, and circle. Click and drag on the canvas to specify the size property of the shapes.

Line: Draws a straight line on the canvas. When selected, you can change the color and thickness of the line. Click and drag on the canvas to create straight lines.

Select: Edits text box and selects items on the canvas to move it around. To edit a text box, click on the text box such that it highlights. Then click again to edit the text and formatting of the text box. Click and drag to create a selection region that could capture multiple items on the canvas. With the selection region, click and drag to move the items in the region around.

### User Account

Registration: You can create an account on the login screen with a username and password.

Login: You can login to your existing account on the login screen with a username and password.

Remember Me: You can keep your account logged in by checking the remember me checkbox. This will store a cryptographically signed token on your computer that can sign you back in next time without the need of credentials.

Logout: You can logout of the current account.

### Rooms

Each room is identified by a room code. You can view your room code on the top left. You can join a room by replacing the room code input with a valid target code. By joining a room, you will clear your existing canvas, and load up whatever items on the canvas the joined room has. You may also create a new room. By doing so, your canvas will be cleared and you will be granted a blank canvas. Rooms are kept indefinitely, and you can rejoin old rooms. This allows a user to load multiple different whiteboards from the cloud if they remember the room codes.

### Synchronization

All drawing items on the canvas are synchronized in realtime between all users in a single given room. This includes text changes and formatting as well as moving items around using the select tool. All synchronization will be saved on the database automatically. When you clear your canvas or load up an existing drawing from a local file, these changes will also be propagated to all the users in the room.


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
