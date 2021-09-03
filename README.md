# ![lens logo](https://i.imgur.com/QhsEsLK.jpg)  Huawei Lens

![Latest Version](https://img.shields.io/badge/latestVersion-1.0-yellow) ![Kotlin](https://img.shields.io/badge/language-kotlin-blue) ![Minimum SDK Version](https://img.shields.io/badge/minSDK-24-orange) ![Android Gradle Version](https://img.shields.io/badge/androidGradleVersion-4.1.1-green) ![Gradle Version](https://img.shields.io/badge/gradleVersion-6.5-brown)

## App View

(gif)


## Screenshots

![QR](https://i.imgur.com/c1hFkQE.jpg)![BCR](https://i.imgur.com/tmoK6zV.jpg)![Identity](https://i.imgur.com/gdodpBQ.jpg)![Translate](https://i.imgur.com/FEX0quz.jpeg)

![OCR](https://i.imgur.com/1p8JvkN.jpg)![Form](https://i.imgur.com/7rqqNFO.jpg)![TTS](https://i.imgur.com/7if3D6f.jpg)![Audio](https://i.imgur.com/kAnAbPW.jpg)



# 📄 Introduction 
Huawei Lens is a set of Machine-Learning based computing capabilities that can provide you AI-based features to make your life easier. Huawei Lens is developed using the ML Kit and Scan Kit of Huawei Mobile Services. Main functionalities of the Huawei Lens are listed below:

-   QR Reader  
-   Business Card Recognition  
-   Object Recognition & Translation  
-   Document & Whiteboard Skew Correction  
-   Form Recognition  
-   Text Recognition & Translation
-  Text to Speech
- Audio Transcription
  

##  About HUAWEI ML Kit
HUAWEI ML Kit allows your apps to easily leverage Huawei's long-term proven expertise in machine learning to support diverse artificial intelligence (AI) applications throughout a wide range of industries. Thanks to Huawei's technology accumulation, ML Kit provides diversified leading machine learning capabilities that are easy to use, helping you develop various AI apps. To discover more, visit: [Huawei MLKit](https://developer.huawei.com/consumer/en/hms/huawei-mlkit/)
##  About HUAWEI Scan Kit
HUAWEI Scan Kit scans and parses all major 1D and 2D barcodes and generates QR codes, helping you quickly build barcode scanning functions into your apps. To discover more, visit: [Huawei Scan Kit](https://developer.huawei.com/consumer/en/hms/huawei-scankit/)

## About HUAWEI Crash Service
Detect, classify, and prioritize crashes in real time, on an intelligent basis, while presenting all of the relevant information to facilitate easy crash resolution. To discover more, visit: [Huawei Crash Service](https://developer.huawei.com/consumer/en/agconnect/crash/)

## 🔧Technical Information
* Project Design Pattern: MVP (Model-View-Presenter)
* Project Software Language: Kotlin
* Kotlin Version: 1.4.21
* Android Studio Version: 4.1.1
* Gradle Version: 6.5

## What You Will Need
**⚙️Hardware Requirements**
- A computer that can run Android Studio.
- An Android phone for debugging.

**⚙️Software Requirements**
- Android SDK package
- Android Studio 3.X
- HMS Core (APK) 4.X or later

# Getting Started
Huawei Lens uses HUAWEI services. In order to use them, you have to [create an app](https://developer.huawei.com/consumer/en/doc/distribution/app/agc-create_app) first. Before getting started, please [sign-up](https://id1.cloud.huawei.com/CAS/portal/userRegister/regbyemail.html?service=https%3A%2F%2Foauth-login1.cloud.huawei.com%2Foauth2%2Fv2%2Flogin%3Faccess_type%3Doffline%26client_id%3D6099200%26display%3Dpage%26flowID%3D6d751ab7-28c0-403c-a7a8-6fc07681a45d%26h%3D1603370512.3540%26lang%3Den-us%26redirect_uri%3Dhttps%253A%252F%252Fdeveloper.huawei.com%252Fconsumer%252Fen%252Flogin%252Fhtml%252FhandleLogin.html%26response_type%3Dcode%26scope%3Dopenid%2Bhttps%253A%252F%252Fwww.huawei.com%252Fauth%252Faccount%252Fcountry%2Bhttps%253A%252F%252Fwww.huawei.com%252Fauth%252Faccount%252Fbase.profile%26v%3D9f7b3af3ae56ae58c5cb23a5c1ff5af7d91720cea9a897be58cff23593e8c1ed&loginUrl=https%3A%2F%2Fid1.cloud.huawei.com%3A443%2FCAS%2Fportal%2FloginAuth.html&clientID=6099200&lang=en-us&display=page&loginChannel=89000060&reqClientType=89) for a HUAWEI developer account.
After creating the application, you need to [generate a signing certificate fingerprint](https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#3). Then you have to set this fingerprint to the application you created in AppGallery Connect.
- Go to "My Projects" in AppGallery Connect.
- Find your project from the project list and click the app on the project card.
- On the Project Setting page, set SHA-256 certificate fingerprint to the SHA-256 fingerprint you've generated.
![AGC-Fingerprint](https://communityfile-drcn.op.hicloud.com/FileServer/getFile/cmtyPub/011/111/111/0000000000011111111.20200511174103.08977471998788006824067329965155:50510612082412:2800:6930AD86F3F5AF6B2740EF666A56165E65A37E64FA305A30C5EFB998DA38D409.png?needInitFileName=true?needInitFileName=true?needInitFileName=true?needInitFileName=true)

## Using the Application

**🔲 QR Reader**
The module recognizes QR codes and performs Read Text, Open URL, Add Contact, Call Phone Number, and Send SMS. 

**💳 Business Card Recognition**
A business card can be scanned and all the information can be saved on phone memory, such as Name, Last Name, Company Name, Phone Number, Fax Number, Email, Address.

**🔭 Identify**
App detects objects on camera and translates object names into preferred language. User can take a photo or select an image from gallery.

**📜Document & Whiteboard Skew Correction**
Photos of a document or a whiteboard can be visually repaired and corrected. Text can be converted to an image or exported as pdf, word, txt files. 

**📋 Form Recognition**
Photos a form table that contains rows, columns, cells can converted to excel file. The module will fetch all information from each cell and create an output file.

**🌐 Real Time Translation**
The app detects and reads texts on camera or from gallery photos. User can translate the text on preferred language.

**🔊 Text to Speech**
Imported Word or PDF files or an input text can be converted to speech and read out.

**🎤 Audio Transcription**
A real time voice record or an imported audio file will be transcripted into text. Transcription result can be scrolled via audio player, and can be translated into preferred language. Transcription result also can be exported in pdf, word, txt formats.

## Contributors
- Cagatay Kizildag
- Anil Ari
- Berke Coban
- Enes Inkaya
- Erdal Kaymak
- Fatih Ozturk
- Mustafa Surucu
- Umit Kose


# Features 
### QR Reader Features:

1. The user can open the URL on the browser by scanning the QR code which contains the URL
2. The user can add all information of a new contact by scanning the QR code which contains the contact information
3. The user can make a phone call by scanning the QR code which contains the mobile number
4. The user can send a message by scanning the QR code which contains the mobile number and text message


### Business Card Recognition Features:

1. User can open camera, scan if any QR code exists on business cards. If it exists, code will be read properly to add card owner as a contact
2. User can select a card image from devices’ gallery to add card owner as a contact
3. User can scan business card from real time camera and add card owner as a contact
4. The module recognizes exact name and surname of the card owner and defines them as name/surname in related fields to save information
5. The module recognizes title of the card owner and defines it as a “Title” to save information
6. The module recognizes phone numbers and fax information from the card they will be saved in related fields when adding card owner as a contact
7. The module recognizes exact company name from the other text fields of the card and it will be saved in related field when adding card owner as a contact
8. The module recognizes email information within the texts fields of business card and it will be saved in related field when adding card owner as a contact
9. The module recognizes Address information with all details and it will be saved in related field when adding card owner as a contact
10. The module can add recognized information to contacts and call intent
11. All recognized texts will be located in correct fields of “Add Contact” section when saving card information


### Text Recognition & Translation Features:


1. User can capture a photo with a camera or choose the photo from the gallery
2. App recognizes text from the photo captured or chosen
3. App detects the language of the text in a photo and translates it to a specific language
4. App recognizes text from real-time camera stream and translates to specific language and monitors translated text
5. App detects the language of input text in real-time camera stream, or from any photo captured by the user

### Audio Transcription Features:


1. App generates audio transcriptions in real time from English, French and Chinese languages
2. App generates audio transcription from imported audio files
3. App can generate audio transcriptions up to 5 hours for both in real time and from file
4. App has a mini player for media file
5. App displays transcription text synchronized with media player or displays transcription text as a list view
6. App translates audio transcription into selected language
7. App exports audio transcription as .docx, .pdf, .txt file


### Object Recognition & Translation Features:


1. User can capture a photo or pick an image from gallery to use this service
2. User can select the target language from the list
3. Application can classify the objects within the photo which is taken
4. Application displays translated name of item(s) in the list


### Document & Whiteboard Skew Correction Features:


1. User can capture photos with camera or pick an image from the gallery
2. User can edit (crop, rotate) the image which is selected
3. Document Skew Correction service will be applied to the image if needed
4. Text Recognition will be applied for selected images
5. User can save the recognized text in different options like .pdf, .docx or .txt
6. User can share the saved documents


### Text to Speech Features:


1. User can copy and paste any text or enter text manually into the input area
2. User can select a file from devices’ file system and import to input area
3. Given text will be obtained and converted to speech without any issue
4. User can manage start/pause/resume operations of play button
5. The volume and speed settings of the player can be adjusted by user
6. User can select male or female voice timbres for supported languages
7. Application highlights the sentences that being played synchronized with the speaker


### Form Recognition Features:


1. User can take a photo to cover any form or pick an image from the gallery
2. User can crop or rotate the input image by using the edit function
3. User can transfer form information into an excel file
4. Use can share the files that created from images

## Infrastructure, Technologies, Libraries Used 
* [Kotlin ](https://kotlinlang.org/)
* [Android CameraX ](https://developer.android.com/training/camerax)
* [Android Navigation ](https://developer.android.com/jetpack/androidx/releases/navigation)
* [Android Multidex](https://developer.android.com/jetpack/androidx/releases/multidex)
* [Android Recyclerview ](https://developer.android.com/guide/topics/ui/layout/recyclerview)
* [Android Cardview ](https://developer.android.com/guide/topics/ui/layout/cardview)
* [Android Palette KTX Module](https://developer.android.com/reference/kotlin/androidx/palette/graphics/package-summary#extension-functions-summary)
* [Google Flexbox](https://github.com/google/flexbox-layout)
* [Google Gson](https://github.com/google/gson)
* [iTextPdf](https://github.com/itext/itextpdf)
* [Apache POI](https://poi.apache.org/)
* [RxAndroid](https://github.com/ReactiveX/RxAndroid)
* [Picasso](https://github.com/square/picasso)

## Useful Links 
* [Huawei Developers Medium Page EN](https://medium.com/huawei-developers)
* [Huawei Developers Medium Page TR](https://medium.com/huawei-developers-tr) 
* [Huawei Developers Forum](https://forums.developer.huawei.com/forumPortal/en/home)


# Licence
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
