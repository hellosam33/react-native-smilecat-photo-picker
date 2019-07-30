# react-native-smilecat-photo-picker

## Getting started

`$ npm install react-native-smilecat-photo-picker --save`

### Mostly automatic installation

`$ react-native link react-native-smilecat-photo-picker`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-smilecat-photo-picker` and add `SmilecatPhotoPicker.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libSmilecatPhotoPicker.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.SmilecatPhotoPickerPackage;` to the imports at the top of the file
  - Add `new SmilecatPhotoPickerPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-smilecat-photo-picker'
  	project(':react-native-smilecat-photo-picker').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-smilecat-photo-picker/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-smilecat-photo-picker')
  	```


## Usage
```javascript
import SmilecatPhotoPicker from 'react-native-smilecat-photo-picker';

// TODO: What to do with the module?
SmilecatPhotoPicker;
```
