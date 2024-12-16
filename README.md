# Bluetooth Printer Example for Flutter

## Introduction
After testing several outdated and problematic packages from [pub.dev](https://pub.dev), I encountered numerous issues while trying to implement Bluetooth printing for Flutter applications. To overcome these challenges, I decided to write a custom solution in **Kotlin** that ensures compatibility and reliability for thermal printers.

This repository provides a working example of how to:

- Scan for Bluetooth devices.
- Connect to a thermal printer.
- Print text and images with full control over the printer's width.

## Features
- **Reliable Bluetooth Connectivity:** Establishes stable connections with paired devices.
- **Custom Image Processing:** Converts images to black and white and resizes them to fit the printer's width.
- **ESC/POS Commands:** Uses ESC/POS commands for text and image printing.
- **Printer Compatibility:** Works with standard thermal printers.

## Requirements

- **Flutter SDK:** Version 3.0 or higher.
- **Kotlin:** Integrated into the Android project for native code.
- **Android Device:** Supports Bluetooth.
- **Thermal Printer:** Supports ESC/POS commands.

## Installation
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/bluetooth_printer_example.git
   ```

2. Navigate to the project directory:
   ```bash
   cd bluetooth_printer_example
   ```

3. Install the Flutter dependencies:
   ```bash
   flutter pub get
   ```

4. Open the Android project in Android Studio to ensure all Kotlin dependencies are resolved.

## Usage

### Method Channel Integration
The Flutter app communicates with the native Kotlin code using a **MethodChannel**. Below are the available methods:

1. **Scan Devices:**
   ```dart
   final devices = await platform.invokeMethod('scanDevices');
   ```

   Returns a list of paired Bluetooth devices.

2. **Connect to Printer:**
   ```dart
   final result = await platform.invokeMethod('connectToPrinter', {
     'printerAddress': 'XX:XX:XX:XX:XX:XX',
   });
   ```

   Connects to the printer using its Bluetooth address.

3. **Print Text:**
   ```dart
   await platform.invokeMethod('printText', {
     'text': 'Hello, World!',
   });
   ```

4. **Print Image:**
   ```dart
   await platform.invokeMethod('printImage', {
     'imageBytes': imageBytes,
   });
   ```
   Sends a bitmap image to the printer.

### Adjusting Printer Width
This project uses a default printer width of **765 pixels**. The image processing ensures that images match this width while maintaining their aspect ratio.

## How It Works

### Native Kotlin Code
The native Kotlin code handles Bluetooth device scanning, connection management, and printing commands. Key highlights include:

- **Dynamic UUID Handling:** Supports multiple UUIDs for better compatibility.
- **Bitmap Processing:** Converts images to black-and-white and resizes them to fit the printer width.
- **ESC/POS Commands:** Implements the necessary commands for initializing the printer, setting line spacing, and printing.

### Flutter Integration
The Flutter app communicates with the Kotlin code using **MethodChannels**, providing seamless integration and easy-to-use methods for printing text and images.

## Troubleshooting

1. **Connection Issues:**
   - Ensure the printer is paired with the Android device.
   - Restart the Bluetooth connection if the app fails to connect.

2. **Image Printing Problems:**
   - Verify that the image bytes are correctly passed from Flutter to Kotlin.
   - Check that the image resolution matches the printer width (765 pixels).

3. **Permissions:**
   - For Android 12 and above, ensure that **BLUETOOTH_SCAN** and **BLUETOOTH_CONNECT** permissions are granted.

## Contribution
Feel free to contribute to this project by:

- Reporting issues.
- Suggesting new features.
- Submitting pull requests.


![Screenshot_20241216-142732](https://github.com/user-attachments/assets/cb61b701-1dcb-40d6-85f0-636fbfc5b4d4)
![Screenshot_20241216-142744](https://github.com/user-attachments/assets/613cf4b2-751b-4904-bc29-21b1d1e36671)
![IMG_20241216_142905](https://github.com/user-attachments/assets/245fca4c-ab14-4362-a140-7ed0f1797925)




## Author
Developed by [Mahmoud Abdelraheem](https://github.com/mahmoudAbdelraheem).


