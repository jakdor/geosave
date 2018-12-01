# Introduction

First off, thank you for considering contributing to GeoSave. It's still in it's infancy today, but hopefully with your help I'll become great tool one day.

## Help we are looking for

GeoSave is an open source project and we love to receive contributions from our community — you! There are many ways to contribute, from writing translations, submitting bug reports/fixes and feature requests or writing new code which can be incorporated into GeoSave itself.

# Your First Contribution

## Getting started

1. create your own fork of the code,
2. do the changes in your fork,
3. add your name or nick in copyright header,
4. send a pull request,
5. discuss changes, make fixes
6. Bingo! Your code is now part of GeoSave, thank you!

## How to report a bug

If you find a security vulnerability, do NOT open an issue. Email jakub.dorda@gmail.com instead.

For any other issue open a bug report.

When filing an issue, make sure to answer these five questions, it would make it easier to find and reproduce:
1. What version of GeoSave are you using?
2. What Android API level are on you on and vendor name?
3. What did you do?
4. What did you expect to see?
5. What did you see instead?

Don't forget to mark issue with "Bug" label.

## How to suggest a feature or enhancement

Open new issue with "Feature" label to start discussion on your idea.

# How do I compile GeoSave? - obtaining API keys

Before you are able to compile GeoSave yourself there are couple steps you need to take.

1. Create free Firebase account - [firebase.google.com](https://firebase.google.com/)
2. In settings tab set package name to "com.jakdor.geosave"
3. Enable services: Authentication (email, Google, anonymous), Cloud Firestore, Storage
4. Follow guide on how to create debug keystore - [developers.google.com/android/guides/client-auth](https://developers.google.com/android/guides/client-auth)
5. Copy SHA1, SHA256 fingerprints to firebase app config - settings tab
6. Copy "debug.keystore" to geosave/geosave/app/debug.keystore
7. Download "google-service.json" from settings tab - place it in geosave/geosave/app/src/dev/google-service.json
8. Obtain Google Maps SDK for Android API key, don't worry it's free as well - [developers.google.com/maps/documentation/android-sdk/signup](https://developers.google.com/maps/documentation/android-sdk/signup)
9. Create new file "apikeys.properties" in geosave/geosave/apikeys.properties
10. Your "apikeys.properties" should look like this:
```
maps.key=<Your maps sdk key>
```

Project is separated into two flavors - dev/production. You are going to use dev environment.
Switch to correct flavour: Build -> Select Build variant -> devDebug

You should now be able to compile GeoSave. Remember to create tests only under dev flavor.

Don't ask me for production API keys, you are not going to get them.
