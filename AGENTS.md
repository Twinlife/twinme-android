This project is the twinme and Skred private and secure messaging Android application.

## Layout

- `webrtc-framework`: Java 8 submodule providing WebRTC and SQLCipher support as native shared library.
- `image-cropper`: image cropper.
- `zxing`: QR-code scanner.
- `twinlife-framework`: Java 8 submodule shared with the signaling server
   and the application (the server has its own branch).  Defines the services
   to connect to the signaling server and manages generic abstractions in the SQLCipher database.
   The `org.twinlife.twinlife` classes and interfaces are all located in that module
   under src/main/java, src/android/java and src/engine/java for the server specific part.
- `twinme-framework`: Java 8 submodule for high level abstractions on top of
   `twinlife-framework`.  This module is shared with the server.
- `twinme-android-common`: Java 8 submodule providing Android services and application utilities.
- the `org.twinlife.twinme` classes and interfaces are located either
  in `twinme-framework` or in `twinme-android-common` or app directories.

## Rules

- Implementation is exclusively in Java version 8 maximum (Android & server compatibility).
- Read top-level `CODING_GUIDELINES.md` and follow the submodule `CODING_GUIDELINES.md` for all code changes.
- You are not allowed to commit or push in git.


