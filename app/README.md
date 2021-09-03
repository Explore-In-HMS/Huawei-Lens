# Huawei Lens

Huawei Lens is a set of vision-based computing capabilities that can provide you AI-based features to make your life easier.

## Table of Contents
* [Supported Environments](#supported-environments)
* [Used Technologies](#used-technologies)
* [Focus](#focus)
* [Introduction](#introduction)
* [Feature Details](#feature-details)

## Supported Environments
* Android Studio 3.0 or later.
* Java SDK 1.8 or later.
* Android SDK version is 26 or later.
* Gradle 3.3.2.

## Used Technologies
* Huawei ML Kit

## Focus

*	The aim of the Huawei Lens is to spot  advanced features and capabilities of Huawei ML Kit by creating a mobile application that can be a reference for developers and users of HMS.

## Introduction

Huawei Lens is developed using the ML Kit of Huawei Mobile Services. Main functionalities of the Huawei Lens areas listed below:


*   QR Reader
    Huawei Lens can read QR codes

*   General Card Recognition
    Huawei Lens can read business cards

*   Text Recognition & Translation
    Huawei Lens can detect text and translate to different languages

*   Audio Recognition & Translation
    Huawei Lens can detect audio and translate to different languages

*   Object Recognition & Translation
    Huawei Lens can detect an object and translate to different languages

*   Document & Whiteboard Skew Correction
    Huawei Lens can correct document & whiteboard skew

*   Form Recognition
    Huawei Lens can recognize and detect form tables

*   Text to Speech
    Huawei Lens can convert input text into human speech


## Feature Details

**# QR Reader**

*    The module recognizes QR codes and extract the Text, URL, Contact, Phone, and SMS information from the code

*    Services:
        - HMS - Scan Kit has been used to provide QR Scanner service

*    Features:
        - The user can open the URL on the browser by scanning the QR code which contains the URL
        - The user can add all information of a new contact by scanning the QR code which contains the contact information
        - The user can make a phone call by scanning the QR code which contains the mobile number
        - The user can send a message by scanning the QR code which contains the mobile number and text message


**# Business Card Recognition**

*    Huawei Lens recognizes business cards and extracts the Name, Surname, Company, Phone, Email, Address, and other text fields from a business card.

*    Services:
        - ML Kit General Card Recognition

*    Features:


**# Text Recognition & Translation**
*    Huawei Lens detects texts from static images or on real-time camera streams. Huawei Lens detects the language of the text and translates the text to the preferred language.

*    Services:
        - ML Kit Text Recognition
        - ML Kit Language Detection
        - ML Kit Text Translation

*    Features:
        - User can capture a photo with a camera or choose the photo from the gallery
        - Application recognizes text from the photo captured or chosen
        - Application detects the language of the text in a photo and translates it to a specific language
        - Application recognizes text from real-time camera stream and translates to specific language and monitors translated text
        - Application detects the language of input text in real-time camera stream, or from any photo captured by the user

**# Audio Recognition & Translation**
*    Huawei Lens recognizes and extracts text from audio files. Huawei Lens translates text files to the preferred language.

*    Services:
        - ML Kit Audio Transcription
        - ML Kit Text Translation

*    Features:
        -  Application shall have a mini player for media file
        -  Application shall display audio transcription
        -  Application shall display translated audio transcription
        -  Application shall display transcription text sync with media player

**# Object Recognition & Translation**

*    Huawei Lens detects an object and translates object name to preferred languages.

*    Services:
        - ML Kit Image Classification or Object Detection or Custom Model (Mindspore)
        - ML Kit Text Translation

*    Features:
        - User shall capture a photo
        - User shall select language to be translated
        - Application shall classify the photo taken
        - Application shall display translated text of item(s) name

**# Document & Whiteboard Skew Correction**

*    Huawei Lens detects document or whiteboard from static images or on real-time camera streams. The skew of the document or whiteboard is corrected. Text is recognized in the image and saved.

*    Services:
        - ML Kit Document Skew Correction
        - ML Kit Text Recognition

*    Features:
        - User shall capture a photo or choose from a gallery
        - User shall apply crop action to the image
        - Apply document skew correction service to the image if needed
        - Apply OCR to the corrected image
        - User shall save the recognized text in different options such as word, pdf, text file, image, etc.

**# Form Recognition**

*    The module recognizes and returns form structure information that contains rows, columns, and coordinates of cells. You can fetch cells from images and create an excel file in accordance with the input.

*    Services:
        - ML Kit Form Recognition

*    Features:
        - User can take a photo to cover any form or pick an image from the gallery
        - User can crop or rotate the input image by using the edit function
        - User can transfer form information into an excel file
        - Use can share the files that created from images

**# Text to Speech**

*   Huawei Lens can read a text or open a document like Word or PDF. The text will be converted to speech via TTS.

*    Services:
        - ML Kit Text to Speech
        - ML Kit Language Detection

*    Features:
        -  User shall have the capability of copy & paste text or enter text manually
        -  User shall manage start/pause/resume operations of the player via buttons
        -  User shall select a file from the phone’s file system and import it to the application to be converted to speech
        -  Given text shall be obtained and converted to speech without any problem
