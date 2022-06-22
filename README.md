# Bitmap manipulation

## Introduction

A proof-of-concept image manipulation application requires some extra touches from you!
The front-end coding has been completed and it is the main business logic that needs implementation only!

## Problem Statement
Three operations should be performed on a selected image prior initiating a share:

1. Scaling it down, so that none of its dimensions exceeds 1,024 pixels.
2. Inverting all the pixel colors.
3. Adding two EXIF tags for a description & GPS position.

For now, it is fine if we pass some hardcoded values, that is those specified in the `MainActivity.java` file.

## Hints

1. Keep the aspect ratio of the input image untouched.
2. Pay special attention to potential OOM problems.

## Note

Please be careful when editing `build.gradle` in your project. This task as it is doesn’t require any changes to it. It is generally ok to add new dependencies but changing or removing existing dependencies or configuration can cause the project and verification tests to not function in the expected way and give a unreliable score.
